begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|Pool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/** Simple object pool of limited size. Implemented as a lock-free ring buffer;  * may fail to produce items if there are too many concurrent users. */
end_comment

begin_class
specifier|public
class|class
name|FixedSizedObjectPool
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Pool
argument_list|<
name|T
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FixedSizedObjectPool
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Ring buffer has two "markers" - where objects are present ('objects' list), and where they are    * removed ('empty' list). This class contains bit shifts and masks for one marker's components    * within a long, and provides utility methods to get/set the components.    * Marker consists of (examples here for 'objects' list; same for 'empty' list):    *  - the marker itself. Set to NO_MARKER if list is empty (e.g. no objects to take from pool),    *    otherwise contains the array index of the first element of the list.    *  - the 'delta'. Number of elements from the marker that is being modified. Each concurrent    *    modification (e.g. take call) increments this to claim an array index. Delta elements    *    from the marker cannot be touched by other threads. Delta can never overshoot the other    *    marker (or own marker if other is empty), or overflow MAX_DELTA. If delta is set to    *    NO_DELTA, it means the marker has been modified during 'take' operation and list cannot    *    be touched (see below). In any of these cases, take returns null.    *  - the 'refcount'/'rc'. Number of operations occurring on the marker. Each e.g. take incs    *    this; when the last of the overlapping operations decreases the refcount, it 'commits'    *    the modifications by moving the marker according to delta and resetting delta to 0.    *    If the other list does not exist, it's also created (i.e. first 'offer' to a new pool with    *    empty 'objects' list will create the 'objects' list); if the list is being exhausted to empty    *    by other op (e.g. pool has 2 objects, 2 takes are in progress when offer commits), the    *    marker of the other list is still reset to new location, and delta is set to NO_DELTA,    *    preventing operations on the lists until the exhausting ops commit and set delta to 0.    */
specifier|private
specifier|static
specifier|final
class|class
name|Marker
block|{
comment|// Currently the long must fit 2 markers. Setting these bit sizes determines the balance
comment|// between max pool size allowed and max concurrency allowed. This balance here is not what we
comment|// want (up to 254 of each op while only 65535 objects limit), but it uses whole bytes and is
comment|// good for now. Delta and RC take the same number of bits; usually it doesn't make sense to
comment|// have more delta.
specifier|private
specifier|static
specifier|final
name|long
name|MARKER_MASK
init|=
literal|0xffffL
decl_stmt|,
name|DELTA_MASK
init|=
literal|0xffL
decl_stmt|,
name|RC_MASK
init|=
literal|0xffL
decl_stmt|;
specifier|public
name|Marker
parameter_list|(
name|int
name|markerShift
parameter_list|,
name|int
name|deltaShift
parameter_list|,
name|int
name|rcShift
parameter_list|)
block|{
name|this
operator|.
name|markerShift
operator|=
name|markerShift
expr_stmt|;
name|this
operator|.
name|deltaShift
operator|=
name|deltaShift
expr_stmt|;
name|this
operator|.
name|rcShift
operator|=
name|rcShift
expr_stmt|;
block|}
name|int
name|markerShift
decl_stmt|,
name|deltaShift
decl_stmt|,
name|rcShift
decl_stmt|;
specifier|public
specifier|final
name|long
name|setMarker
parameter_list|(
name|long
name|dest
parameter_list|,
name|long
name|val
parameter_list|)
block|{
return|return
name|setValue
argument_list|(
name|dest
argument_list|,
name|val
argument_list|,
name|markerShift
argument_list|,
name|MARKER_MASK
argument_list|)
return|;
block|}
specifier|public
specifier|final
name|long
name|setDelta
parameter_list|(
name|long
name|dest
parameter_list|,
name|long
name|val
parameter_list|)
block|{
return|return
name|setValue
argument_list|(
name|dest
argument_list|,
name|val
argument_list|,
name|deltaShift
argument_list|,
name|DELTA_MASK
argument_list|)
return|;
block|}
specifier|public
specifier|final
name|long
name|setRc
parameter_list|(
name|long
name|dest
parameter_list|,
name|long
name|val
parameter_list|)
block|{
return|return
name|setValue
argument_list|(
name|dest
argument_list|,
name|val
argument_list|,
name|rcShift
argument_list|,
name|RC_MASK
argument_list|)
return|;
block|}
specifier|public
specifier|final
name|long
name|getMarker
parameter_list|(
name|long
name|src
parameter_list|)
block|{
return|return
name|getValue
argument_list|(
name|src
argument_list|,
name|markerShift
argument_list|,
name|MARKER_MASK
argument_list|)
return|;
block|}
specifier|public
specifier|final
name|long
name|getDelta
parameter_list|(
name|long
name|src
parameter_list|)
block|{
return|return
name|getValue
argument_list|(
name|src
argument_list|,
name|deltaShift
argument_list|,
name|DELTA_MASK
argument_list|)
return|;
block|}
specifier|public
specifier|final
name|long
name|getRc
parameter_list|(
name|long
name|src
parameter_list|)
block|{
return|return
name|getValue
argument_list|(
name|src
argument_list|,
name|rcShift
argument_list|,
name|RC_MASK
argument_list|)
return|;
block|}
specifier|private
specifier|final
name|long
name|setValue
parameter_list|(
name|long
name|dest
parameter_list|,
name|long
name|val
parameter_list|,
name|int
name|offset
parameter_list|,
name|long
name|mask
parameter_list|)
block|{
return|return
operator|(
name|dest
operator|&
operator|(
operator|~
operator|(
name|mask
operator|<<
name|offset
operator|)
operator|)
operator|)
operator|+
operator|(
name|val
operator|<<
name|offset
operator|)
return|;
block|}
specifier|private
specifier|final
name|long
name|getValue
parameter_list|(
name|long
name|src
parameter_list|,
name|int
name|offset
parameter_list|,
name|long
name|mask
parameter_list|)
block|{
return|return
operator|(
name|src
operator|>>>
name|offset
operator|)
operator|&
name|mask
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|(
name|long
name|markers
parameter_list|)
block|{
return|return
literal|"{"
operator|+
name|getMarker
argument_list|(
name|markers
argument_list|)
operator|+
literal|", "
operator|+
name|getDelta
argument_list|(
name|markers
argument_list|)
operator|+
literal|", "
operator|+
name|getRc
argument_list|(
name|markers
argument_list|)
operator|+
literal|"}"
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|long
name|NO_MARKER
init|=
name|Marker
operator|.
name|MARKER_MASK
decl_stmt|,
name|NO_DELTA
init|=
name|Marker
operator|.
name|DELTA_MASK
decl_stmt|,
name|MAX_DELTA
init|=
name|NO_DELTA
operator|-
literal|1
decl_stmt|,
name|MAX_SIZE
init|=
name|NO_MARKER
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|NO_INDEX
init|=
literal|0
decl_stmt|;
comment|// The array index can't be reserved.
comment|// See Marker class comment.
specifier|private
specifier|static
specifier|final
name|Marker
name|OBJECTS
init|=
operator|new
name|Marker
argument_list|(
literal|48
argument_list|,
literal|40
argument_list|,
literal|32
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Marker
name|EMPTY
init|=
operator|new
name|Marker
argument_list|(
literal|16
argument_list|,
literal|8
argument_list|,
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|state
decl_stmt|;
specifier|private
specifier|final
name|PoolObjectHelper
argument_list|<
name|T
argument_list|>
name|helper
decl_stmt|;
specifier|private
specifier|final
name|T
index|[]
name|pool
decl_stmt|;
specifier|public
name|FixedSizedObjectPool
parameter_list|(
name|int
name|size
parameter_list|,
name|PoolObjectHelper
argument_list|<
name|T
argument_list|>
name|helper
parameter_list|)
block|{
name|this
argument_list|(
name|size
argument_list|,
name|helper
argument_list|,
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|FixedSizedObjectPool
parameter_list|(
name|int
name|size
parameter_list|,
name|PoolObjectHelper
argument_list|<
name|T
argument_list|>
name|helper
parameter_list|,
name|boolean
name|doTraceLog
parameter_list|)
block|{
if|if
condition|(
name|size
operator|>
name|MAX_SIZE
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Size must be<= "
operator|+
name|MAX_SIZE
argument_list|)
throw|;
block|}
name|this
operator|.
name|helper
operator|=
name|helper
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|T
index|[]
name|poolTmp
init|=
operator|(
name|T
index|[]
operator|)
operator|new
name|Object
index|[
name|size
index|]
decl_stmt|;
name|pool
operator|=
name|poolTmp
expr_stmt|;
comment|// Initially, all deltas and rcs are 0; empty list starts at 0; there are no objects to take.
name|state
operator|=
operator|new
name|AtomicLong
argument_list|(
name|OBJECTS
operator|.
name|setMarker
argument_list|(
literal|0
argument_list|,
name|NO_MARKER
argument_list|)
argument_list|)
expr_stmt|;
name|casLog
operator|=
name|doTraceLog
condition|?
operator|new
name|CasLog
argument_list|()
else|:
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|take
parameter_list|()
block|{
name|T
name|result
init|=
name|pool
operator|.
name|length
operator|>
literal|0
condition|?
name|takeImpl
argument_list|()
else|:
literal|null
decl_stmt|;
return|return
operator|(
name|result
operator|==
literal|null
operator|)
condition|?
name|helper
operator|.
name|create
argument_list|()
else|:
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|offer
parameter_list|(
name|T
name|t
parameter_list|)
block|{
name|tryOffer
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|pool
operator|.
name|length
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|boolean
name|tryOffer
parameter_list|(
name|T
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
operator|||
name|pool
operator|.
name|length
operator|==
literal|0
condition|)
return|return
literal|false
return|;
comment|// 0 size means no-pooling case - passthru.
name|helper
operator|.
name|resetBeforeOffer
argument_list|(
name|t
argument_list|)
expr_stmt|;
return|return
name|offerImpl
argument_list|(
name|t
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|T
name|result
init|=
name|takeImpl
argument_list|()
decl_stmt|;
while|while
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|takeImpl
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|T
name|takeImpl
parameter_list|()
block|{
name|long
name|oldState
init|=
name|reserveArrayIndex
argument_list|(
name|OBJECTS
argument_list|,
name|EMPTY
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldState
operator|==
name|NO_INDEX
condition|)
return|return
literal|null
return|;
comment|// For whatever reason, reserve failed.
name|long
name|originalMarker
init|=
name|OBJECTS
operator|.
name|getMarker
argument_list|(
name|oldState
argument_list|)
decl_stmt|,
name|delta
init|=
name|OBJECTS
operator|.
name|getDelta
argument_list|(
name|oldState
argument_list|)
decl_stmt|;
name|int
name|arrayIndex
init|=
operator|(
name|int
operator|)
name|getArrayIndex
argument_list|(
name|originalMarker
argument_list|,
name|delta
argument_list|)
decl_stmt|;
name|T
name|result
init|=
name|pool
index|[
name|arrayIndex
index|]
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|throwError
argument_list|(
name|oldState
argument_list|,
name|arrayIndex
argument_list|,
literal|"null"
argument_list|)
expr_stmt|;
block|}
name|pool
index|[
name|arrayIndex
index|]
operator|=
literal|null
expr_stmt|;
name|commitArrayIndex
argument_list|(
name|OBJECTS
argument_list|,
name|EMPTY
argument_list|,
name|originalMarker
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|boolean
name|offerImpl
parameter_list|(
name|T
name|t
parameter_list|)
block|{
name|long
name|oldState
init|=
name|reserveArrayIndex
argument_list|(
name|EMPTY
argument_list|,
name|OBJECTS
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldState
operator|==
name|NO_INDEX
condition|)
return|return
literal|false
return|;
comment|// For whatever reason, reserve failed.
name|long
name|originalMarker
init|=
name|EMPTY
operator|.
name|getMarker
argument_list|(
name|oldState
argument_list|)
decl_stmt|,
name|delta
init|=
name|EMPTY
operator|.
name|getDelta
argument_list|(
name|oldState
argument_list|)
decl_stmt|;
name|int
name|arrayIndex
init|=
operator|(
name|int
operator|)
name|getArrayIndex
argument_list|(
name|originalMarker
argument_list|,
name|delta
argument_list|)
decl_stmt|;
if|if
condition|(
name|pool
index|[
name|arrayIndex
index|]
operator|!=
literal|null
condition|)
block|{
name|throwError
argument_list|(
name|oldState
argument_list|,
name|arrayIndex
argument_list|,
literal|"non-null"
argument_list|)
expr_stmt|;
block|}
name|pool
index|[
name|arrayIndex
index|]
operator|=
name|t
expr_stmt|;
name|commitArrayIndex
argument_list|(
name|EMPTY
argument_list|,
name|OBJECTS
argument_list|,
name|originalMarker
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|throwError
parameter_list|(
name|long
name|oldState
parameter_list|,
name|int
name|arrayIndex
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|long
name|newState
init|=
name|state
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|casLog
operator|!=
literal|null
condition|)
block|{
name|casLog
operator|.
name|dumpLog
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|String
name|msg
init|=
literal|"Unexpected "
operator|+
name|type
operator|+
literal|" at "
operator|+
name|arrayIndex
operator|+
literal|"; state was "
operator|+
name|toString
argument_list|(
name|oldState
argument_list|)
operator|+
literal|", now "
operator|+
name|toString
argument_list|(
name|newState
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|AssertionError
argument_list|(
name|msg
argument_list|)
throw|;
block|}
specifier|private
name|long
name|reserveArrayIndex
parameter_list|(
name|Marker
name|from
parameter_list|,
name|Marker
name|to
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|oldVal
init|=
name|state
operator|.
name|get
argument_list|()
decl_stmt|,
name|marker
init|=
name|from
operator|.
name|getMarker
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|delta
init|=
name|from
operator|.
name|getDelta
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|rc
init|=
name|from
operator|.
name|getRc
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|toMarker
init|=
name|to
operator|.
name|getMarker
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|toDelta
init|=
name|to
operator|.
name|getDelta
argument_list|(
name|oldVal
argument_list|)
decl_stmt|;
if|if
condition|(
name|marker
operator|==
name|NO_MARKER
condition|)
return|return
name|NO_INDEX
return|;
comment|// The list is empty.
if|if
condition|(
name|delta
operator|==
name|MAX_DELTA
condition|)
return|return
name|NO_INDEX
return|;
comment|// Too many concurrent operations; spurious failure.
if|if
condition|(
name|delta
operator|==
name|NO_DELTA
condition|)
return|return
name|NO_INDEX
return|;
comment|// List is drained and recreated concurrently.
if|if
condition|(
name|toDelta
operator|==
name|NO_DELTA
condition|)
block|{
comment|// Same for the OTHER list; spurious.
comment|// TODO: the fact that concurrent re-creation of other list necessitates full stop is not
comment|//       ideal... the reason is that the list NOT being re-created still uses the list
comment|//       being re-created for boundary check; it needs the old value of the other marker.
comment|//       However, NO_DELTA means the other marker was already set to a new value. For now,
comment|//       assume concurrent re-creation is rare and the gap before commit is tiny.
return|return
name|NO_INDEX
return|;
block|}
assert|assert
name|rc
operator|<=
name|delta
assert|;
comment|// There can never be more concurrent takers than uncommitted ones.
name|long
name|newDelta
init|=
name|incDeltaValue
argument_list|(
name|marker
argument_list|,
name|toMarker
argument_list|,
name|delta
argument_list|)
decl_stmt|;
comment|// Increase target list pos.
if|if
condition|(
name|newDelta
operator|==
name|NO_DELTA
condition|)
return|return
name|NO_INDEX
return|;
comment|// Target list is being drained.
name|long
name|newVal
init|=
name|from
operator|.
name|setRc
argument_list|(
name|from
operator|.
name|setDelta
argument_list|(
name|oldVal
argument_list|,
name|newDelta
argument_list|)
argument_list|,
name|rc
operator|+
literal|1
argument_list|)
decl_stmt|;
comment|// Set delta and refcount.
if|if
condition|(
name|setState
argument_list|(
name|oldVal
argument_list|,
name|newVal
argument_list|)
condition|)
return|return
name|oldVal
return|;
block|}
block|}
specifier|private
name|void
name|commitArrayIndex
parameter_list|(
name|Marker
name|from
parameter_list|,
name|Marker
name|to
parameter_list|,
name|long
name|originalMarker
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|oldVal
init|=
name|state
operator|.
name|get
argument_list|()
decl_stmt|,
name|rc
init|=
name|from
operator|.
name|getRc
argument_list|(
name|oldVal
argument_list|)
decl_stmt|;
name|long
name|newVal
init|=
name|from
operator|.
name|setRc
argument_list|(
name|oldVal
argument_list|,
name|rc
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// Decrease refcount.
assert|assert
name|rc
operator|>
literal|0
assert|;
if|if
condition|(
name|rc
operator|==
literal|1
condition|)
block|{
comment|// We are the last of the concurrent operations to finish. Commit.
name|long
name|marker
init|=
name|from
operator|.
name|getMarker
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|delta
init|=
name|from
operator|.
name|getDelta
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|otherMarker
init|=
name|to
operator|.
name|getMarker
argument_list|(
name|oldVal
argument_list|)
decl_stmt|,
name|otherDelta
init|=
name|to
operator|.
name|getDelta
argument_list|(
name|oldVal
argument_list|)
decl_stmt|;
assert|assert
name|rc
operator|<=
name|delta
assert|;
comment|// Move marker according to delta, change delta to 0.
name|long
name|newMarker
init|=
name|applyDeltaToMarker
argument_list|(
name|marker
argument_list|,
name|otherMarker
argument_list|,
name|delta
argument_list|)
decl_stmt|;
name|newVal
operator|=
name|from
operator|.
name|setDelta
argument_list|(
name|from
operator|.
name|setMarker
argument_list|(
name|newVal
argument_list|,
name|newMarker
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|otherMarker
operator|==
name|NO_MARKER
condition|)
block|{
comment|// The other list doesn't exist, create it at the first index of our op.
assert|assert
name|otherDelta
operator|==
literal|0
assert|;
name|newVal
operator|=
name|to
operator|.
name|setMarker
argument_list|(
name|newVal
argument_list|,
name|originalMarker
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|otherDelta
operator|>
literal|0
operator|&&
name|otherDelta
operator|!=
name|NO_DELTA
operator|&&
name|applyDeltaToMarker
argument_list|(
name|otherMarker
argument_list|,
name|marker
argument_list|,
name|otherDelta
argument_list|)
operator|==
name|NO_MARKER
condition|)
block|{
comment|// The other list will be exhausted when it commits. Create new one pending that commit.
name|newVal
operator|=
name|to
operator|.
name|setDelta
argument_list|(
name|to
operator|.
name|setMarker
argument_list|(
name|newVal
argument_list|,
name|originalMarker
argument_list|)
argument_list|,
name|NO_DELTA
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|setState
argument_list|(
name|oldVal
argument_list|,
name|newVal
argument_list|)
condition|)
return|return;
block|}
block|}
specifier|private
name|boolean
name|setState
parameter_list|(
name|long
name|oldVal
parameter_list|,
name|long
name|newVal
parameter_list|)
block|{
name|boolean
name|result
init|=
name|state
operator|.
name|compareAndSet
argument_list|(
name|oldVal
argument_list|,
name|newVal
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|&&
name|casLog
operator|!=
literal|null
condition|)
block|{
name|casLog
operator|.
name|log
argument_list|(
name|oldVal
argument_list|,
name|newVal
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|long
name|incDeltaValue
parameter_list|(
name|long
name|markerFrom
parameter_list|,
name|long
name|otherMarker
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
if|if
condition|(
name|delta
operator|==
name|pool
operator|.
name|length
condition|)
return|return
name|NO_DELTA
return|;
comment|// The (pool-sized) list is being fully drained.
name|long
name|result
init|=
name|delta
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|getArrayIndex
argument_list|(
name|markerFrom
argument_list|,
name|result
argument_list|)
operator|==
name|getArrayIndex
argument_list|(
name|otherMarker
argument_list|,
literal|1
argument_list|)
condition|)
block|{
return|return
name|NO_DELTA
return|;
comment|// The list is being drained, cannot increase the delta anymore.
block|}
return|return
name|result
return|;
block|}
specifier|private
name|long
name|applyDeltaToMarker
parameter_list|(
name|long
name|marker
parameter_list|,
name|long
name|markerLimit
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
if|if
condition|(
name|delta
operator|==
name|NO_DELTA
condition|)
return|return
name|marker
return|;
comment|// List was recreated while we were exhausting it.
if|if
condition|(
name|delta
operator|==
name|pool
operator|.
name|length
condition|)
block|{
assert|assert
name|markerLimit
operator|==
name|NO_MARKER
assert|;
comment|// If we had the entire pool, other list couldn't exist.
return|return
name|NO_MARKER
return|;
comment|// We exhausted the entire-pool-sized list.
block|}
name|marker
operator|=
name|getArrayIndex
argument_list|(
name|marker
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// Just move the marker according to delta.
if|if
condition|(
name|marker
operator|==
name|markerLimit
condition|)
return|return
name|NO_MARKER
return|;
comment|// We hit the limit - the list was exhausted.
return|return
name|marker
return|;
block|}
specifier|private
name|long
name|getArrayIndex
parameter_list|(
name|long
name|marker
parameter_list|,
name|long
name|delta
parameter_list|)
block|{
name|marker
operator|+=
name|delta
expr_stmt|;
if|if
condition|(
name|marker
operator|>=
name|pool
operator|.
name|length
condition|)
block|{
name|marker
operator|-=
name|pool
operator|.
name|length
expr_stmt|;
comment|// Wrap around at the end of buffer.
block|}
return|return
name|marker
return|;
block|}
specifier|static
name|String
name|toString
parameter_list|(
name|long
name|markers
parameter_list|)
block|{
return|return
name|OBJECTS
operator|.
name|toString
argument_list|(
name|markers
argument_list|)
operator|+
literal|", "
operator|+
name|EMPTY
operator|.
name|toString
argument_list|(
name|markers
argument_list|)
return|;
block|}
comment|// TODO: Temporary for debugging. Doesn't interfere with MTT failures (unlike LOG.debug).
specifier|private
specifier|final
specifier|static
class|class
name|CasLog
block|{
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|log
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|offset
init|=
operator|new
name|AtomicLong
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
specifier|public
name|CasLog
parameter_list|()
block|{
name|size
operator|=
literal|1
operator|<<
literal|14
comment|/* 256Kb in longs */
expr_stmt|;
name|log
operator|=
operator|new
name|long
index|[
name|size
index|]
expr_stmt|;
block|}
specifier|public
name|void
name|log
parameter_list|(
name|long
name|oldVal
parameter_list|,
name|long
name|newVal
parameter_list|)
block|{
name|int
name|ix
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|offset
operator|.
name|incrementAndGet
argument_list|()
operator|<<
literal|1
operator|)
operator|&
operator|(
name|size
operator|-
literal|1
operator|)
argument_list|)
decl_stmt|;
name|log
index|[
name|ix
index|]
operator|=
name|oldVal
expr_stmt|;
name|log
index|[
name|ix
operator|+
literal|1
index|]
operator|=
name|newVal
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|dumpLog
parameter_list|(
name|boolean
name|doSleep
parameter_list|)
block|{
if|if
condition|(
name|doSleep
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{         }
block|}
name|int
name|logSize
init|=
operator|(
name|int
operator|)
name|offset
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// TODO: dump the end if wrapping around?
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|logSize
condition|;
operator|++
name|i
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"CAS history dump: "
operator|+
name|FixedSizedObjectPool
operator|.
name|toString
argument_list|(
name|log
index|[
name|i
operator|<<
literal|1
index|]
argument_list|)
operator|+
literal|" => "
operator|+
name|FixedSizedObjectPool
operator|.
name|toString
argument_list|(
name|log
index|[
operator|(
name|i
operator|<<
literal|1
operator|)
operator|+
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|offset
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|CasLog
name|casLog
decl_stmt|;
block|}
end_class

end_unit


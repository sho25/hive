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
name|hadoop
operator|.
name|hive
operator|.
name|druid
operator|.
name|json
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonCreator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonProperty
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|IndexSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|indexing
operator|.
name|RealtimeTuningConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|indexing
operator|.
name|TuningConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|realtime
operator|.
name|appenderator
operator|.
name|AppenderatorConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|segment
operator|.
name|writeout
operator|.
name|SegmentWriteOutMediumFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Period
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * This class is copied from druid source code  * in order to avoid adding additional dependencies on druid-indexing-service.  */
end_comment

begin_class
specifier|public
class|class
name|KafkaTuningConfig
implements|implements
name|TuningConfig
implements|,
name|AppenderatorConfig
block|{
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_ROWS_PER_SEGMENT
init|=
literal|5_000_000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|boolean
name|DEFAULT_RESET_OFFSET_AUTOMATICALLY
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxRowsInMemory
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxBytesInMemory
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxRowsPerSegment
decl_stmt|;
annotation|@
name|Nullable
specifier|private
specifier|final
name|Long
name|maxTotalRows
decl_stmt|;
specifier|private
specifier|final
name|Period
name|intermediatePersistPeriod
decl_stmt|;
specifier|private
specifier|final
name|File
name|basePersistDirectory
decl_stmt|;
annotation|@
name|Deprecated
specifier|private
specifier|final
name|int
name|maxPendingPersists
decl_stmt|;
specifier|private
specifier|final
name|IndexSpec
name|indexSpec
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|reportParseExceptions
decl_stmt|;
annotation|@
name|Deprecated
specifier|private
specifier|final
name|long
name|handoffConditionTimeout
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|resetOffsetAutomatically
decl_stmt|;
annotation|@
name|Nullable
specifier|private
specifier|final
name|SegmentWriteOutMediumFactory
name|segmentWriteOutMediumFactory
decl_stmt|;
specifier|private
specifier|final
name|Period
name|intermediateHandoffPeriod
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|logParseExceptions
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxParseExceptions
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxSavedParseExceptions
decl_stmt|;
annotation|@
name|JsonCreator
specifier|public
name|KafkaTuningConfig
parameter_list|(
annotation|@
name|JsonProperty
argument_list|(
literal|"maxRowsInMemory"
argument_list|)
annotation|@
name|Nullable
name|Integer
name|maxRowsInMemory
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"maxBytesInMemory"
argument_list|)
annotation|@
name|Nullable
name|Long
name|maxBytesInMemory
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"maxRowsPerSegment"
argument_list|)
annotation|@
name|Nullable
name|Integer
name|maxRowsPerSegment
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"maxTotalRows"
argument_list|)
annotation|@
name|Nullable
name|Long
name|maxTotalRows
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"intermediatePersistPeriod"
argument_list|)
annotation|@
name|Nullable
name|Period
name|intermediatePersistPeriod
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"basePersistDirectory"
argument_list|)
annotation|@
name|Nullable
name|File
name|basePersistDirectory
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"maxPendingPersists"
argument_list|)
annotation|@
name|Nullable
name|Integer
name|maxPendingPersists
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"indexSpec"
argument_list|)
annotation|@
name|Nullable
name|IndexSpec
name|indexSpec
parameter_list|,
comment|// This parameter is left for compatibility when reading existing configs, to be removed in Druid 0.12.
annotation|@
name|JsonProperty
argument_list|(
literal|"buildV9Directly"
argument_list|)
annotation|@
name|Nullable
name|Boolean
name|buildV9Directly
parameter_list|,
annotation|@
name|Deprecated
annotation|@
name|JsonProperty
argument_list|(
literal|"reportParseExceptions"
argument_list|)
annotation|@
name|Nullable
name|Boolean
name|reportParseExceptions
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"handoffConditionTimeout"
argument_list|)
annotation|@
name|Nullable
name|Long
name|handoffConditionTimeout
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"resetOffsetAutomatically"
argument_list|)
annotation|@
name|Nullable
name|Boolean
name|resetOffsetAutomatically
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"segmentWriteOutMediumFactory"
argument_list|)
annotation|@
name|Nullable
name|SegmentWriteOutMediumFactory
name|segmentWriteOutMediumFactory
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"intermediateHandoffPeriod"
argument_list|)
annotation|@
name|Nullable
name|Period
name|intermediateHandoffPeriod
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"logParseExceptions"
argument_list|)
annotation|@
name|Nullable
name|Boolean
name|logParseExceptions
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"maxParseExceptions"
argument_list|)
annotation|@
name|Nullable
name|Integer
name|maxParseExceptions
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"maxSavedParseExceptions"
argument_list|)
annotation|@
name|Nullable
name|Integer
name|maxSavedParseExceptions
parameter_list|)
block|{
comment|// Cannot be a static because default basePersistDirectory is unique per-instance
specifier|final
name|RealtimeTuningConfig
name|defaults
init|=
name|RealtimeTuningConfig
operator|.
name|makeDefaultTuningConfig
argument_list|(
name|basePersistDirectory
argument_list|)
decl_stmt|;
name|this
operator|.
name|maxRowsInMemory
operator|=
name|maxRowsInMemory
operator|==
literal|null
condition|?
name|defaults
operator|.
name|getMaxRowsInMemory
argument_list|()
else|:
name|maxRowsInMemory
expr_stmt|;
name|this
operator|.
name|maxRowsPerSegment
operator|=
name|maxRowsPerSegment
operator|==
literal|null
condition|?
name|DEFAULT_MAX_ROWS_PER_SEGMENT
else|:
name|maxRowsPerSegment
expr_stmt|;
comment|// initializing this to 0, it will be lazily initialized to a value
comment|// @see server.src.main.java.org.apache.druid.segment.indexing.TuningConfigs#getMaxBytesInMemoryOrDefault(long)
name|this
operator|.
name|maxBytesInMemory
operator|=
name|maxBytesInMemory
operator|==
literal|null
condition|?
literal|0
else|:
name|maxBytesInMemory
expr_stmt|;
name|this
operator|.
name|maxTotalRows
operator|=
name|maxTotalRows
expr_stmt|;
name|this
operator|.
name|intermediatePersistPeriod
operator|=
name|intermediatePersistPeriod
operator|==
literal|null
condition|?
name|defaults
operator|.
name|getIntermediatePersistPeriod
argument_list|()
else|:
name|intermediatePersistPeriod
expr_stmt|;
name|this
operator|.
name|basePersistDirectory
operator|=
name|defaults
operator|.
name|getBasePersistDirectory
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxPendingPersists
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|indexSpec
operator|=
name|indexSpec
operator|==
literal|null
condition|?
name|defaults
operator|.
name|getIndexSpec
argument_list|()
else|:
name|indexSpec
expr_stmt|;
name|this
operator|.
name|reportParseExceptions
operator|=
name|reportParseExceptions
operator|==
literal|null
condition|?
name|defaults
operator|.
name|isReportParseExceptions
argument_list|()
else|:
name|reportParseExceptions
expr_stmt|;
name|this
operator|.
name|handoffConditionTimeout
operator|=
name|handoffConditionTimeout
operator|==
literal|null
condition|?
name|defaults
operator|.
name|getHandoffConditionTimeout
argument_list|()
else|:
name|handoffConditionTimeout
expr_stmt|;
name|this
operator|.
name|resetOffsetAutomatically
operator|=
name|resetOffsetAutomatically
operator|==
literal|null
condition|?
name|DEFAULT_RESET_OFFSET_AUTOMATICALLY
else|:
name|resetOffsetAutomatically
expr_stmt|;
name|this
operator|.
name|segmentWriteOutMediumFactory
operator|=
name|segmentWriteOutMediumFactory
expr_stmt|;
name|this
operator|.
name|intermediateHandoffPeriod
operator|=
name|intermediateHandoffPeriod
operator|==
literal|null
condition|?
operator|new
name|Period
argument_list|()
operator|.
name|withDays
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
else|:
name|intermediateHandoffPeriod
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|reportParseExceptions
condition|)
block|{
name|this
operator|.
name|maxParseExceptions
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|maxSavedParseExceptions
operator|=
name|maxSavedParseExceptions
operator|==
literal|null
condition|?
literal|0
else|:
name|Math
operator|.
name|min
argument_list|(
literal|1
argument_list|,
name|maxSavedParseExceptions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|maxParseExceptions
operator|=
name|maxParseExceptions
operator|==
literal|null
condition|?
name|TuningConfig
operator|.
name|DEFAULT_MAX_PARSE_EXCEPTIONS
else|:
name|maxParseExceptions
expr_stmt|;
name|this
operator|.
name|maxSavedParseExceptions
operator|=
name|maxSavedParseExceptions
operator|==
literal|null
condition|?
name|TuningConfig
operator|.
name|DEFAULT_MAX_SAVED_PARSE_EXCEPTIONS
else|:
name|maxSavedParseExceptions
expr_stmt|;
block|}
name|this
operator|.
name|logParseExceptions
operator|=
name|logParseExceptions
operator|==
literal|null
condition|?
name|TuningConfig
operator|.
name|DEFAULT_LOG_PARSE_EXCEPTIONS
else|:
name|logParseExceptions
expr_stmt|;
block|}
specifier|public
specifier|static
name|KafkaTuningConfig
name|copyOf
parameter_list|(
name|KafkaTuningConfig
name|config
parameter_list|)
block|{
return|return
operator|new
name|KafkaTuningConfig
argument_list|(
name|config
operator|.
name|maxRowsInMemory
argument_list|,
name|config
operator|.
name|maxBytesInMemory
argument_list|,
name|config
operator|.
name|maxRowsPerSegment
argument_list|,
name|config
operator|.
name|maxTotalRows
argument_list|,
name|config
operator|.
name|intermediatePersistPeriod
argument_list|,
name|config
operator|.
name|basePersistDirectory
argument_list|,
name|config
operator|.
name|maxPendingPersists
argument_list|,
name|config
operator|.
name|indexSpec
argument_list|,
literal|true
argument_list|,
name|config
operator|.
name|reportParseExceptions
argument_list|,
name|config
operator|.
name|handoffConditionTimeout
argument_list|,
name|config
operator|.
name|resetOffsetAutomatically
argument_list|,
name|config
operator|.
name|segmentWriteOutMediumFactory
argument_list|,
name|config
operator|.
name|intermediateHandoffPeriod
argument_list|,
name|config
operator|.
name|logParseExceptions
argument_list|,
name|config
operator|.
name|maxParseExceptions
argument_list|,
name|config
operator|.
name|maxSavedParseExceptions
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|int
name|getMaxRowsInMemory
parameter_list|()
block|{
return|return
name|maxRowsInMemory
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|long
name|getMaxBytesInMemory
parameter_list|()
block|{
return|return
name|maxBytesInMemory
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|Integer
name|getMaxRowsPerSegment
parameter_list|()
block|{
return|return
name|maxRowsPerSegment
return|;
block|}
annotation|@
name|JsonProperty
annotation|@
name|Override
annotation|@
name|Nullable
specifier|public
name|Long
name|getMaxTotalRows
parameter_list|()
block|{
return|return
name|maxTotalRows
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|Period
name|getIntermediatePersistPeriod
parameter_list|()
block|{
return|return
name|intermediatePersistPeriod
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|File
name|getBasePersistDirectory
parameter_list|()
block|{
return|return
name|basePersistDirectory
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
annotation|@
name|Deprecated
specifier|public
name|int
name|getMaxPendingPersists
parameter_list|()
block|{
return|return
name|maxPendingPersists
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|IndexSpec
name|getIndexSpec
parameter_list|()
block|{
return|return
name|indexSpec
return|;
block|}
comment|/**    * Always returns true, doesn't affect the version being built.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"SameReturnValue"
argument_list|)
annotation|@
name|Deprecated
annotation|@
name|JsonProperty
specifier|public
name|boolean
name|getBuildV9Directly
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
specifier|public
name|boolean
name|isReportParseExceptions
parameter_list|()
block|{
return|return
name|reportParseExceptions
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|long
name|getHandoffConditionTimeout
parameter_list|()
block|{
return|return
name|handoffConditionTimeout
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|boolean
name|isResetOffsetAutomatically
parameter_list|()
block|{
return|return
name|resetOffsetAutomatically
return|;
block|}
annotation|@
name|Override
annotation|@
name|JsonProperty
annotation|@
name|Nullable
specifier|public
name|SegmentWriteOutMediumFactory
name|getSegmentWriteOutMediumFactory
parameter_list|()
block|{
return|return
name|segmentWriteOutMediumFactory
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|Period
name|getIntermediateHandoffPeriod
parameter_list|()
block|{
return|return
name|intermediateHandoffPeriod
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|boolean
name|isLogParseExceptions
parameter_list|()
block|{
return|return
name|logParseExceptions
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|int
name|getMaxParseExceptions
parameter_list|()
block|{
return|return
name|maxParseExceptions
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|int
name|getMaxSavedParseExceptions
parameter_list|()
block|{
return|return
name|maxSavedParseExceptions
return|;
block|}
specifier|public
name|KafkaTuningConfig
name|withBasePersistDirectory
parameter_list|(
name|File
name|dir
parameter_list|)
block|{
return|return
operator|new
name|KafkaTuningConfig
argument_list|(
name|maxRowsInMemory
argument_list|,
name|maxBytesInMemory
argument_list|,
name|maxRowsPerSegment
argument_list|,
name|maxTotalRows
argument_list|,
name|intermediatePersistPeriod
argument_list|,
name|dir
argument_list|,
name|maxPendingPersists
argument_list|,
name|indexSpec
argument_list|,
literal|true
argument_list|,
name|reportParseExceptions
argument_list|,
name|handoffConditionTimeout
argument_list|,
name|resetOffsetAutomatically
argument_list|,
name|segmentWriteOutMediumFactory
argument_list|,
name|intermediateHandoffPeriod
argument_list|,
name|logParseExceptions
argument_list|,
name|maxParseExceptions
argument_list|,
name|maxSavedParseExceptions
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|KafkaTuningConfig
name|that
init|=
operator|(
name|KafkaTuningConfig
operator|)
name|o
decl_stmt|;
return|return
name|maxRowsInMemory
operator|==
name|that
operator|.
name|maxRowsInMemory
operator|&&
name|maxRowsPerSegment
operator|==
name|that
operator|.
name|maxRowsPerSegment
operator|&&
name|maxBytesInMemory
operator|==
name|that
operator|.
name|maxBytesInMemory
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|maxTotalRows
argument_list|,
name|that
operator|.
name|maxTotalRows
argument_list|)
operator|&&
name|maxPendingPersists
operator|==
name|that
operator|.
name|maxPendingPersists
operator|&&
name|reportParseExceptions
operator|==
name|that
operator|.
name|reportParseExceptions
operator|&&
name|handoffConditionTimeout
operator|==
name|that
operator|.
name|handoffConditionTimeout
operator|&&
name|resetOffsetAutomatically
operator|==
name|that
operator|.
name|resetOffsetAutomatically
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|intermediatePersistPeriod
argument_list|,
name|that
operator|.
name|intermediatePersistPeriod
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|basePersistDirectory
argument_list|,
name|that
operator|.
name|basePersistDirectory
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|indexSpec
argument_list|,
name|that
operator|.
name|indexSpec
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|segmentWriteOutMediumFactory
argument_list|,
name|that
operator|.
name|segmentWriteOutMediumFactory
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|intermediateHandoffPeriod
argument_list|,
name|that
operator|.
name|intermediateHandoffPeriod
argument_list|)
operator|&&
name|logParseExceptions
operator|==
name|that
operator|.
name|logParseExceptions
operator|&&
name|maxParseExceptions
operator|==
name|that
operator|.
name|maxParseExceptions
operator|&&
name|maxSavedParseExceptions
operator|==
name|that
operator|.
name|maxSavedParseExceptions
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|maxRowsInMemory
argument_list|,
name|maxRowsPerSegment
argument_list|,
name|maxBytesInMemory
argument_list|,
name|maxTotalRows
argument_list|,
name|intermediatePersistPeriod
argument_list|,
name|basePersistDirectory
argument_list|,
name|maxPendingPersists
argument_list|,
name|indexSpec
argument_list|,
name|reportParseExceptions
argument_list|,
name|handoffConditionTimeout
argument_list|,
name|resetOffsetAutomatically
argument_list|,
name|segmentWriteOutMediumFactory
argument_list|,
name|intermediateHandoffPeriod
argument_list|,
name|logParseExceptions
argument_list|,
name|maxParseExceptions
argument_list|,
name|maxSavedParseExceptions
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"KafkaTuningConfig{"
operator|+
literal|"maxRowsInMemory="
operator|+
name|maxRowsInMemory
operator|+
literal|", maxRowsPerSegment="
operator|+
name|maxRowsPerSegment
operator|+
literal|", maxTotalRows="
operator|+
name|maxTotalRows
operator|+
literal|", maxBytesInMemory="
operator|+
name|maxBytesInMemory
operator|+
literal|", intermediatePersistPeriod="
operator|+
name|intermediatePersistPeriod
operator|+
literal|", basePersistDirectory="
operator|+
name|basePersistDirectory
operator|+
literal|", maxPendingPersists="
operator|+
name|maxPendingPersists
operator|+
literal|", indexSpec="
operator|+
name|indexSpec
operator|+
literal|", reportParseExceptions="
operator|+
name|reportParseExceptions
operator|+
literal|", handoffConditionTimeout="
operator|+
name|handoffConditionTimeout
operator|+
literal|", resetOffsetAutomatically="
operator|+
name|resetOffsetAutomatically
operator|+
literal|", segmentWriteOutMediumFactory="
operator|+
name|segmentWriteOutMediumFactory
operator|+
literal|", intermediateHandoffPeriod="
operator|+
name|intermediateHandoffPeriod
operator|+
literal|", logParseExceptions="
operator|+
name|logParseExceptions
operator|+
literal|", maxParseExceptions="
operator|+
name|maxParseExceptions
operator|+
literal|", maxSavedParseExceptions="
operator|+
name|maxSavedParseExceptions
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit


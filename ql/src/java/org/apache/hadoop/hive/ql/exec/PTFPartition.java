begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ConcurrentModificationException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|ql
operator|.
name|exec
operator|.
name|persistence
operator|.
name|PTFRowContainer
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|serde2
operator|.
name|SerDe
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/*  * represents a collection of rows that is acted upon by a TableFunction or a WindowFunction.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|PTFPartition
block|{
specifier|protected
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|PTFPartition
operator|.
name|class
argument_list|)
decl_stmt|;
name|SerDe
name|serDe
decl_stmt|;
name|StructObjectInspector
name|inputOI
decl_stmt|;
name|StructObjectInspector
name|outputOI
decl_stmt|;
specifier|private
specifier|final
name|PTFRowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|elems
decl_stmt|;
specifier|protected
name|PTFPartition
parameter_list|(
name|HiveConf
name|cfg
parameter_list|,
name|SerDe
name|serDe
parameter_list|,
name|StructObjectInspector
name|inputOI
parameter_list|,
name|StructObjectInspector
name|outputOI
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|serDe
operator|=
name|serDe
expr_stmt|;
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|int
name|containerNumRows
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|cfg
argument_list|,
name|ConfVars
operator|.
name|HIVEJOINCACHESIZE
argument_list|)
decl_stmt|;
name|elems
operator|=
operator|new
name|PTFRowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|(
name|containerNumRows
argument_list|,
name|cfg
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|elems
operator|.
name|setSerDe
argument_list|(
name|serDe
argument_list|,
name|outputOI
argument_list|)
expr_stmt|;
name|elems
operator|.
name|setTableDesc
argument_list|(
name|PTFRowContainer
operator|.
name|createTableDesc
argument_list|(
name|inputOI
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|HiveException
block|{
name|elems
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|SerDe
name|getSerDe
parameter_list|()
block|{
return|return
name|serDe
return|;
block|}
specifier|public
name|StructObjectInspector
name|getInputOI
parameter_list|()
block|{
return|return
name|inputOI
return|;
block|}
specifier|public
name|StructObjectInspector
name|getOutputOI
parameter_list|()
block|{
return|return
name|outputOI
return|;
block|}
specifier|public
name|Object
name|getAt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|elems
operator|.
name|getAt
argument_list|(
name|i
argument_list|)
return|;
block|}
specifier|public
name|void
name|append
parameter_list|(
name|Object
name|o
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|elems
operator|.
name|size
argument_list|()
operator|==
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Cannot add more than %d elements to a PTFPartition"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
throw|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|Object
argument_list|>
name|l
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|o
argument_list|,
name|inputOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|elems
operator|.
name|add
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
operator|(
name|int
operator|)
name|elems
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
throws|throws
name|HiveException
block|{
name|elems
operator|.
name|first
argument_list|()
expr_stmt|;
return|return
operator|new
name|PItr
argument_list|(
literal|0
argument_list|,
name|size
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|range
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
assert|assert
operator|(
name|start
operator|>=
literal|0
operator|)
assert|;
assert|assert
operator|(
name|end
operator|<=
name|size
argument_list|()
operator|)
assert|;
assert|assert
operator|(
name|start
operator|<=
name|end
operator|)
assert|;
return|return
operator|new
name|PItr
argument_list|(
name|start
argument_list|,
name|end
argument_list|)
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|elems
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
class|class
name|PItr
implements|implements
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
block|{
name|int
name|idx
decl_stmt|;
specifier|final
name|int
name|start
decl_stmt|;
specifier|final
name|int
name|end
decl_stmt|;
specifier|final
name|int
name|createTimeSz
decl_stmt|;
name|PItr
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|this
operator|.
name|idx
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
expr_stmt|;
name|createTimeSz
operator|=
name|PTFPartition
operator|.
name|this
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
name|checkForComodification
argument_list|()
expr_stmt|;
return|return
name|idx
operator|<
name|end
return|;
block|}
specifier|public
name|Object
name|next
parameter_list|()
block|{
name|checkForComodification
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|PTFPartition
operator|.
name|this
operator|.
name|getAt
argument_list|(
name|idx
operator|++
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|final
name|void
name|checkForComodification
parameter_list|()
block|{
if|if
condition|(
name|createTimeSz
operator|!=
name|PTFPartition
operator|.
name|this
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConcurrentModificationException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getIndex
parameter_list|()
block|{
return|return
name|idx
return|;
block|}
specifier|private
name|Object
name|getAt
parameter_list|(
name|int
name|i
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|PTFPartition
operator|.
name|this
operator|.
name|getAt
argument_list|(
name|i
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|lead
parameter_list|(
name|int
name|amt
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|i
init|=
name|idx
operator|+
name|amt
decl_stmt|;
name|i
operator|=
name|i
operator|>=
name|end
condition|?
name|end
operator|-
literal|1
else|:
name|i
expr_stmt|;
return|return
name|getAt
argument_list|(
name|i
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|lag
parameter_list|(
name|int
name|amt
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|i
init|=
name|idx
operator|-
name|amt
decl_stmt|;
name|i
operator|=
name|i
operator|<
name|start
condition|?
name|start
else|:
name|i
expr_stmt|;
return|return
name|getAt
argument_list|(
name|i
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|resetToIndex
parameter_list|(
name|int
name|idx
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|idx
operator|<
name|start
operator|||
name|idx
operator|>=
name|end
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Object
name|o
init|=
name|getAt
argument_list|(
name|idx
argument_list|)
decl_stmt|;
name|this
operator|.
name|idx
operator|=
name|idx
operator|+
literal|1
expr_stmt|;
return|return
name|o
return|;
block|}
annotation|@
name|Override
specifier|public
name|PTFPartition
name|getPartition
parameter_list|()
block|{
return|return
name|PTFPartition
operator|.
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|idx
operator|=
name|start
expr_stmt|;
block|}
block|}
empty_stmt|;
comment|/*    * provide an Iterator on the rows in a Partiton.    * Iterator exposes the index of the next location.    * Client can invoke lead/lag relative to the next location.    */
specifier|public
specifier|static
interface|interface
name|PTFPartitionIterator
parameter_list|<
name|T
parameter_list|>
extends|extends
name|Iterator
argument_list|<
name|T
argument_list|>
block|{
name|int
name|getIndex
parameter_list|()
function_decl|;
name|T
name|lead
parameter_list|(
name|int
name|amt
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|T
name|lag
parameter_list|(
name|int
name|amt
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/*      * after a lead and lag call, allow Object associated with SerDe and writable associated with      * partition to be reset      * to the value for the current Index.      */
name|Object
name|resetToIndex
parameter_list|(
name|int
name|idx
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|PTFPartition
name|getPartition
parameter_list|()
function_decl|;
name|void
name|reset
parameter_list|()
throws|throws
name|HiveException
function_decl|;
block|}
specifier|public
specifier|static
name|PTFPartition
name|create
parameter_list|(
name|HiveConf
name|cfg
parameter_list|,
name|SerDe
name|serDe
parameter_list|,
name|StructObjectInspector
name|inputOI
parameter_list|,
name|StructObjectInspector
name|outputOI
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
operator|new
name|PTFPartition
argument_list|(
name|cfg
argument_list|,
name|serDe
argument_list|,
name|inputOI
argument_list|,
name|outputOI
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|StructObjectInspector
name|setupPartitionOutputOI
parameter_list|(
name|SerDe
name|serDe
parameter_list|,
name|StructObjectInspector
name|tblFnOI
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|tblFnOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
return|;
block|}
block|}
end_class

end_unit


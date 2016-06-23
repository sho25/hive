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
name|io
operator|.
name|orc
package|;
end_package

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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|io
operator|.
name|BatchToRowReader
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|io
operator|.
name|NullWritable
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
name|mapred
operator|.
name|RecordReader
import|;
end_import

begin_comment
comment|/** BatchToRowReader that returns the rows readable by ORC IOs. */
end_comment

begin_class
specifier|public
class|class
name|OrcOiBatchToRowReader
extends|extends
name|BatchToRowReader
argument_list|<
name|OrcStruct
argument_list|,
name|OrcUnion
argument_list|>
block|{
specifier|public
name|OrcOiBatchToRowReader
parameter_list|(
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|vrbReader
parameter_list|,
name|VectorizedRowBatchCtx
name|vrbCtx
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|includedCols
parameter_list|)
block|{
name|super
argument_list|(
name|vrbReader
argument_list|,
name|vrbCtx
argument_list|,
name|includedCols
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|OrcStruct
name|createStructObject
parameter_list|(
name|Object
name|previous
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|childrenTypes
parameter_list|)
block|{
name|int
name|numChildren
init|=
name|childrenTypes
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|previous
operator|instanceof
name|OrcStruct
operator|)
condition|)
block|{
return|return
operator|new
name|OrcStruct
argument_list|(
name|numChildren
argument_list|)
return|;
block|}
name|OrcStruct
name|result
init|=
operator|(
name|OrcStruct
operator|)
name|previous
decl_stmt|;
name|result
operator|.
name|setNumFields
argument_list|(
name|numChildren
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|protected
name|OrcUnion
name|createUnionObject
parameter_list|(
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|childrenTypes
parameter_list|,
name|Object
name|previous
parameter_list|)
block|{
return|return
operator|(
name|previous
operator|instanceof
name|OrcUnion
operator|)
condition|?
operator|(
name|OrcUnion
operator|)
name|previous
else|:
operator|new
name|OrcUnion
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setStructCol
parameter_list|(
name|OrcStruct
name|structObj
parameter_list|,
name|int
name|i
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|structObj
operator|.
name|setFieldValue
argument_list|(
name|i
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|getStructCol
parameter_list|(
name|OrcStruct
name|structObj
parameter_list|,
name|int
name|i
parameter_list|)
block|{
return|return
name|structObj
operator|.
name|getFieldValue
argument_list|(
name|i
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|getUnionField
parameter_list|(
name|OrcUnion
name|unionObj
parameter_list|)
block|{
return|return
name|unionObj
operator|.
name|getObject
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUnion
parameter_list|(
name|OrcUnion
name|unionObj
parameter_list|,
name|byte
name|tag
parameter_list|,
name|Object
name|object
parameter_list|)
block|{
name|unionObj
operator|.
name|set
argument_list|(
name|tag
argument_list|,
name|object
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * VectorMapDesc.  *  * Extra vector information just for the PartitionDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|VectorPartitionDesc
block|{
specifier|private
specifier|static
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// Data Type Conversion Needed?
comment|//
comment|// VECTORIZED_INPUT_FILE_FORMAT:
comment|//    No data type conversion check?  Assume ALTER TABLE prevented conversions that
comment|//    VectorizedInputFileFormat cannot handle...
comment|//
specifier|public
specifier|static
enum|enum
name|VectorMapOperatorReadType
block|{
name|NONE
block|,
name|VECTORIZED_INPUT_FILE_FORMAT
block|}
specifier|private
specifier|final
name|VectorMapOperatorReadType
name|vectorMapOperatorReadType
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|needsDataTypeConversionCheck
decl_stmt|;
specifier|private
name|boolean
index|[]
name|conversionFlags
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|typeInfos
decl_stmt|;
specifier|private
name|VectorPartitionDesc
parameter_list|(
name|VectorMapOperatorReadType
name|vectorMapOperatorReadType
parameter_list|,
name|boolean
name|needsDataTypeConversionCheck
parameter_list|)
block|{
name|this
operator|.
name|vectorMapOperatorReadType
operator|=
name|vectorMapOperatorReadType
expr_stmt|;
name|this
operator|.
name|needsDataTypeConversionCheck
operator|=
name|needsDataTypeConversionCheck
expr_stmt|;
name|conversionFlags
operator|=
literal|null
expr_stmt|;
name|typeInfos
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
specifier|static
name|VectorPartitionDesc
name|createVectorizedInputFileFormat
parameter_list|()
block|{
return|return
operator|new
name|VectorPartitionDesc
argument_list|(
name|VectorMapOperatorReadType
operator|.
name|VECTORIZED_INPUT_FILE_FORMAT
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorPartitionDesc
name|clone
parameter_list|()
block|{
name|VectorPartitionDesc
name|result
init|=
operator|new
name|VectorPartitionDesc
argument_list|(
name|vectorMapOperatorReadType
argument_list|,
name|needsDataTypeConversionCheck
argument_list|)
decl_stmt|;
name|result
operator|.
name|conversionFlags
operator|=
operator|(
name|conversionFlags
operator|==
literal|null
condition|?
literal|null
else|:
name|Arrays
operator|.
name|copyOf
argument_list|(
name|conversionFlags
argument_list|,
name|conversionFlags
operator|.
name|length
argument_list|)
operator|)
expr_stmt|;
name|result
operator|.
name|typeInfos
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|typeInfos
argument_list|,
name|typeInfos
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|VectorMapOperatorReadType
name|getVectorMapOperatorReadType
parameter_list|()
block|{
return|return
name|vectorMapOperatorReadType
return|;
block|}
specifier|public
name|boolean
name|getNeedsDataTypeConversionCheck
parameter_list|()
block|{
return|return
name|needsDataTypeConversionCheck
return|;
block|}
specifier|public
name|void
name|setConversionFlags
parameter_list|(
name|boolean
index|[]
name|conversionFlags
parameter_list|)
block|{
name|this
operator|.
name|conversionFlags
operator|=
name|conversionFlags
expr_stmt|;
block|}
specifier|public
name|boolean
index|[]
name|getConversionFlags
parameter_list|()
block|{
return|return
name|conversionFlags
return|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getTypeInfos
parameter_list|()
block|{
return|return
name|typeInfos
return|;
block|}
specifier|public
name|void
name|setTypeInfos
parameter_list|(
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfoList
parameter_list|)
block|{
name|typeInfos
operator|=
name|typeInfoList
operator|.
name|toArray
argument_list|(
operator|new
name|TypeInfo
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getNonPartColumnCount
parameter_list|()
block|{
return|return
name|typeInfos
operator|.
name|length
return|;
block|}
block|}
end_class

end_unit


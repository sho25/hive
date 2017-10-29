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
name|type
operator|.
name|DataTypePhysicalVariation
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
comment|/**  * VectorTableScanDesc.  *  * Extra parameters beyond TableScanDesc just for the VectorTableScanOperator.  *  * We don't extend TableScanDesc because the base OperatorDesc doesn't support  * clone and adding it is a lot work for little gain.  */
end_comment

begin_class
specifier|public
class|class
name|VectorTableScanDesc
extends|extends
name|AbstractVectorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|int
index|[]
name|projectedColumns
decl_stmt|;
specifier|private
name|String
index|[]
name|projectedColumnNames
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|projectedColumnTypeInfos
decl_stmt|;
specifier|private
name|DataTypePhysicalVariation
index|[]
name|projectedColumnDataTypePhysicalVariation
decl_stmt|;
specifier|public
name|VectorTableScanDesc
parameter_list|()
block|{   }
specifier|public
name|void
name|setProjectedColumns
parameter_list|(
name|int
index|[]
name|projectedColumns
parameter_list|)
block|{
name|this
operator|.
name|projectedColumns
operator|=
name|projectedColumns
expr_stmt|;
block|}
specifier|public
name|int
index|[]
name|getProjectedColumns
parameter_list|()
block|{
return|return
name|projectedColumns
return|;
block|}
specifier|public
name|void
name|setProjectedColumnNames
parameter_list|(
name|String
index|[]
name|projectedColumnNames
parameter_list|)
block|{
name|this
operator|.
name|projectedColumnNames
operator|=
name|projectedColumnNames
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getProjectedColumnNames
parameter_list|()
block|{
return|return
name|projectedColumnNames
return|;
block|}
specifier|public
name|void
name|setProjectedColumnTypeInfos
parameter_list|(
name|TypeInfo
index|[]
name|projectedColumnTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|projectedColumnTypeInfos
operator|=
name|projectedColumnTypeInfos
expr_stmt|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getProjectedColumnTypeInfos
parameter_list|()
block|{
return|return
name|projectedColumnTypeInfos
return|;
block|}
specifier|public
name|void
name|setProjectedColumnDataTypePhysicalVariations
parameter_list|(
name|DataTypePhysicalVariation
index|[]
name|projectedColumnDataTypePhysicalVariation
parameter_list|)
block|{
name|this
operator|.
name|projectedColumnDataTypePhysicalVariation
operator|=
name|projectedColumnDataTypePhysicalVariation
expr_stmt|;
block|}
specifier|public
name|DataTypePhysicalVariation
index|[]
name|getProjectedColumnDataTypePhysicalVariations
parameter_list|()
block|{
return|return
name|projectedColumnDataTypePhysicalVariation
return|;
block|}
block|}
end_class

end_unit


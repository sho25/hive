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
name|udf
operator|.
name|generic
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_comment
comment|/**  * A simple implementation of<tt>GenericUDAFParameterInfo</tt>.  *  */
end_comment

begin_class
specifier|public
class|class
name|SimpleGenericUDAFParameterInfo
implements|implements
name|GenericUDAFParameterInfo
block|{
specifier|private
specifier|final
name|ObjectInspector
index|[]
name|parameters
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isWindowing
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|distinct
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|allColumns
decl_stmt|;
specifier|public
name|SimpleGenericUDAFParameterInfo
parameter_list|(
name|ObjectInspector
index|[]
name|params
parameter_list|,
name|boolean
name|isWindowing
parameter_list|,
name|boolean
name|distinct
parameter_list|,
name|boolean
name|allColumns
parameter_list|)
block|{
name|this
operator|.
name|parameters
operator|=
name|params
expr_stmt|;
name|this
operator|.
name|isWindowing
operator|=
name|isWindowing
expr_stmt|;
name|this
operator|.
name|distinct
operator|=
name|distinct
expr_stmt|;
name|this
operator|.
name|allColumns
operator|=
name|allColumns
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
name|TypeInfo
index|[]
name|getParameters
parameter_list|()
block|{
name|TypeInfo
index|[]
name|result
init|=
operator|new
name|TypeInfo
index|[
name|parameters
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|parameters
operator|.
name|length
condition|;
operator|++
name|ii
control|)
block|{
name|result
index|[
name|ii
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|parameters
index|[
name|ii
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|ObjectInspector
index|[]
name|getParameterObjectInspectors
parameter_list|()
block|{
return|return
name|parameters
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDistinct
parameter_list|()
block|{
return|return
name|distinct
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAllColumns
parameter_list|()
block|{
return|return
name|allColumns
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isWindowing
parameter_list|()
block|{
return|return
name|isWindowing
return|;
block|}
block|}
end_class

end_unit


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
name|ql
operator|.
name|exec
operator|.
name|vector
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
comment|/**  * This class collects column information for copying a row from one VectorizedRowBatch to  * same/another batch.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorColumnMapping
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
name|int
index|[]
name|sourceColumns
decl_stmt|;
specifier|protected
name|int
index|[]
name|outputColumns
decl_stmt|;
specifier|protected
name|TypeInfo
index|[]
name|typeInfos
decl_stmt|;
specifier|protected
name|VectorColumnOrderedMap
name|vectorColumnMapping
decl_stmt|;
specifier|public
name|VectorColumnMapping
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|vectorColumnMapping
operator|=
operator|new
name|VectorColumnOrderedMap
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|add
parameter_list|(
name|int
name|sourceColumn
parameter_list|,
name|int
name|outputColumn
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|void
name|finalize
parameter_list|()
function_decl|;
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|sourceColumns
operator|.
name|length
return|;
block|}
specifier|public
name|int
index|[]
name|getInputColumns
parameter_list|()
block|{
return|return
name|sourceColumns
return|;
block|}
specifier|public
name|int
index|[]
name|getOutputColumns
parameter_list|()
block|{
return|return
name|outputColumns
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
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"source columns: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|sourceColumns
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"output columns: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|outputColumns
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"type infos: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|typeInfos
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit


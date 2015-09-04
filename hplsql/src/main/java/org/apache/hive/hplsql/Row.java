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
name|hive
operator|.
name|hplsql
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_comment
comment|/**  * Table row (all columns)  */
end_comment

begin_class
specifier|public
class|class
name|Row
block|{
name|ArrayList
argument_list|<
name|Column
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|Column
argument_list|>
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Column
argument_list|>
name|columnMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Column
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Constructors    */
name|Row
parameter_list|()
block|{}
name|Row
parameter_list|(
name|Row
name|row
parameter_list|)
block|{
for|for
control|(
name|Column
name|c
range|:
name|row
operator|.
name|columns
control|)
block|{
name|addColumn
argument_list|(
name|c
operator|.
name|name
argument_list|,
name|c
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Add a column with specified data type    */
name|void
name|addColumn
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|Column
name|column
init|=
operator|new
name|Column
argument_list|(
name|name
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|columnMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|column
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the data type by column name    */
name|String
name|getType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Column
name|column
init|=
name|columnMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|column
operator|!=
literal|null
condition|)
block|{
return|return
name|column
operator|.
name|getType
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get value by index    */
name|Var
name|getValue
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|columns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getValue
argument_list|()
return|;
block|}
comment|/**    * Get value by column name    */
name|Var
name|getValue
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Column
name|column
init|=
name|columnMap
operator|.
name|get
argument_list|(
name|name
operator|.
name|toUpperCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|column
operator|!=
literal|null
condition|)
block|{
return|return
name|column
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get columns    */
name|ArrayList
argument_list|<
name|Column
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
comment|/**    * Get the number of columns    */
name|int
name|size
parameter_list|()
block|{
return|return
name|columns
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit


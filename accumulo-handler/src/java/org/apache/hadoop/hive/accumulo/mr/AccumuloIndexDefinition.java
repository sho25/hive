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
name|accumulo
operator|.
name|mr
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Index table definition.  */
end_comment

begin_class
specifier|public
class|class
name|AccumuloIndexDefinition
block|{
specifier|private
specifier|final
name|String
name|baseTable
decl_stmt|;
specifier|private
specifier|final
name|String
name|indexTable
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|colMap
decl_stmt|;
specifier|public
name|AccumuloIndexDefinition
parameter_list|(
name|String
name|baseTable
parameter_list|,
name|String
name|indexTable
parameter_list|)
block|{
name|this
operator|.
name|colMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|baseTable
operator|=
name|baseTable
expr_stmt|;
name|this
operator|.
name|indexTable
operator|=
name|indexTable
expr_stmt|;
block|}
specifier|public
name|String
name|getBaseTable
parameter_list|()
block|{
return|return
name|baseTable
return|;
block|}
specifier|public
name|String
name|getIndexTable
parameter_list|()
block|{
return|return
name|indexTable
return|;
block|}
specifier|public
name|void
name|addIndexCol
parameter_list|(
name|String
name|cf
parameter_list|,
name|String
name|cq
parameter_list|,
name|String
name|colType
parameter_list|)
block|{
name|colMap
operator|.
name|put
argument_list|(
name|encode
argument_list|(
name|cf
argument_list|,
name|cq
argument_list|)
argument_list|,
name|colType
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getColumnMap
parameter_list|()
block|{
return|return
name|colMap
return|;
block|}
specifier|public
name|void
name|setColumnTuples
parameter_list|(
name|String
name|columns
parameter_list|)
block|{
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
name|String
name|cols
init|=
name|columns
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|cols
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
literal|"*"
operator|.
name|equals
argument_list|(
name|cols
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|col
range|:
name|cols
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|String
index|[]
name|cfcqtp
init|=
name|col
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|addIndexCol
argument_list|(
name|cfcqtp
index|[
literal|0
index|]
argument_list|,
name|cfcqtp
index|[
literal|1
index|]
argument_list|,
name|cfcqtp
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
name|boolean
name|contains
parameter_list|(
name|String
name|cf
parameter_list|,
name|String
name|cq
parameter_list|)
block|{
return|return
name|colMap
operator|.
name|containsKey
argument_list|(
name|encode
argument_list|(
name|cf
argument_list|,
name|cq
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getColType
parameter_list|(
name|String
name|cf
parameter_list|,
name|String
name|cq
parameter_list|)
block|{
return|return
name|colMap
operator|.
name|get
argument_list|(
name|encode
argument_list|(
name|cf
argument_list|,
name|cq
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|String
name|encode
parameter_list|(
name|String
name|cf
parameter_list|,
name|String
name|cq
parameter_list|)
block|{
return|return
name|cq
operator|+
literal|":"
operator|+
name|cq
return|;
block|}
block|}
end_class

end_unit


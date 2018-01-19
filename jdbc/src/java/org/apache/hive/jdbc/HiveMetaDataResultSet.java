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
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

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
name|Collections
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

begin_class
specifier|public
specifier|abstract
class|class
name|HiveMetaDataResultSet
parameter_list|<
name|M
parameter_list|>
extends|extends
name|HiveBaseResultSet
block|{
specifier|protected
name|List
argument_list|<
name|M
argument_list|>
name|data
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|public
name|HiveMetaDataResultSet
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
parameter_list|,
specifier|final
name|List
argument_list|<
name|M
argument_list|>
name|data
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|data
operator|=
operator|new
name|ArrayList
argument_list|<
name|M
argument_list|>
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnTypes
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|columnTypes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|columnTypes
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|columnNames
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|normalizedColumnNames
operator|=
name|normalizeColumnNames
argument_list|(
name|columnNames
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|columnNames
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
name|this
operator|.
name|normalizedColumnNames
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|normalizeColumnNames
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|columnNames
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|colName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|SQLException
block|{   }
block|}
end_class

end_unit


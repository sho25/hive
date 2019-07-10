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
name|common
operator|.
name|repl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * Class that stores the replication scope. Replication scope includes the details of database and  * tables included under the scope of replication.  */
end_comment

begin_class
specifier|public
class|class
name|ReplScope
implements|implements
name|Serializable
block|{
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|Pattern
name|dbNamePattern
decl_stmt|;
comment|// Include and exclude table names/patterns exist only for REPL DUMP.
specifier|private
name|String
name|includedTableNames
decl_stmt|;
specifier|private
name|String
name|excludedTableNames
decl_stmt|;
specifier|private
name|Pattern
name|includedTableNamePattern
decl_stmt|;
specifier|private
name|Pattern
name|excludedTableNamePattern
decl_stmt|;
specifier|public
name|ReplScope
parameter_list|()
block|{   }
specifier|public
name|ReplScope
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|dbNamePattern
operator|=
operator|(
operator|(
operator|(
name|dbName
operator|==
literal|null
operator|)
operator|||
literal|"*"
operator|.
name|equals
argument_list|(
name|dbName
argument_list|)
operator|)
condition|?
literal|null
else|:
name|Pattern
operator|.
name|compile
argument_list|(
name|dbName
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|)
operator|)
expr_stmt|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|void
name|setIncludedTablePatterns
parameter_list|(
name|String
name|includedTableNames
parameter_list|)
block|{
name|this
operator|.
name|includedTableNames
operator|=
name|includedTableNames
expr_stmt|;
name|this
operator|.
name|includedTableNamePattern
operator|=
name|compilePattern
argument_list|(
name|includedTableNames
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getIncludedTableNames
parameter_list|()
block|{
return|return
name|includedTableNames
return|;
block|}
specifier|public
name|void
name|setExcludedTablePatterns
parameter_list|(
name|String
name|excludedTableNames
parameter_list|)
block|{
name|this
operator|.
name|excludedTableNames
operator|=
name|excludedTableNames
expr_stmt|;
name|this
operator|.
name|excludedTableNamePattern
operator|=
name|compilePattern
argument_list|(
name|excludedTableNames
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getExcludedTableNames
parameter_list|()
block|{
return|return
name|excludedTableNames
return|;
block|}
specifier|public
name|boolean
name|includeAllTables
parameter_list|()
block|{
return|return
operator|(
operator|(
name|includedTableNamePattern
operator|==
literal|null
operator|)
operator|&&
operator|(
name|excludedTableNamePattern
operator|==
literal|null
operator|)
operator|)
return|;
block|}
specifier|public
name|boolean
name|includedInReplScope
parameter_list|(
specifier|final
name|String
name|dbName
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
block|{
return|return
name|dbIncludedInReplScope
argument_list|(
name|dbName
argument_list|)
operator|&&
name|tableIncludedInReplScope
argument_list|(
name|tableName
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|dbIncludedInReplScope
parameter_list|(
specifier|final
name|String
name|dbName
parameter_list|)
block|{
return|return
operator|(
name|dbNamePattern
operator|==
literal|null
operator|)
operator|||
name|dbNamePattern
operator|.
name|matcher
argument_list|(
name|dbName
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|tableIncludedInReplScope
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
comment|// If input tableName is empty, it means, DB level event. It should be always included as
comment|// this is DB level replication with list of included/excluded tables.
return|return
literal|true
return|;
block|}
return|return
operator|(
name|inTableIncludedList
argument_list|(
name|tableName
argument_list|)
operator|&&
operator|!
name|inTableExcludedList
argument_list|(
name|tableName
argument_list|)
operator|)
return|;
block|}
specifier|private
name|Pattern
name|compilePattern
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Convert the pattern to lower case because events/HMS will have table names in lower case.
return|return
name|Pattern
operator|.
name|compile
argument_list|(
name|pattern
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|inTableIncludedList
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|includedTableNamePattern
operator|==
literal|null
condition|)
block|{
comment|// If included list is empty, repl policy should be full db replication.
comment|// So, all tables must be included.
return|return
literal|true
return|;
block|}
return|return
name|includedTableNamePattern
operator|.
name|matcher
argument_list|(
name|tableName
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|inTableExcludedList
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|excludedTableNamePattern
operator|==
literal|null
condition|)
block|{
comment|// If excluded tables list is empty means, all tables in included list must be accepted.
return|return
literal|false
return|;
block|}
return|return
name|excludedTableNamePattern
operator|.
name|matcher
argument_list|(
name|tableName
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
block|}
end_class

end_unit


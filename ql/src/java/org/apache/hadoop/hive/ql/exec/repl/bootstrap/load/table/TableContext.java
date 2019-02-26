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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|table
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|repl
operator|.
name|util
operator|.
name|TaskTracker
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
name|parse
operator|.
name|SemanticException
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
name|plan
operator|.
name|ImportTableDesc
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
name|repl
operator|.
name|util
operator|.
name|ReplUtils
import|;
end_import

begin_class
specifier|public
class|class
name|TableContext
block|{
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|private
specifier|final
name|TaskTracker
name|parentTracker
decl_stmt|;
comment|// this will only be available when we are doing table load only in replication not otherwise
specifier|private
specifier|final
name|String
name|tableNameToLoadIn
decl_stmt|;
specifier|public
name|TableContext
parameter_list|(
name|TaskTracker
name|parentTracker
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|String
name|tableNameToLoadIn
parameter_list|)
block|{
name|this
operator|.
name|dbNameToLoadIn
operator|=
name|dbNameToLoadIn
expr_stmt|;
name|this
operator|.
name|parentTracker
operator|=
name|parentTracker
expr_stmt|;
name|this
operator|.
name|tableNameToLoadIn
operator|=
name|tableNameToLoadIn
expr_stmt|;
block|}
name|boolean
name|waitOnPrecursor
parameter_list|()
block|{
return|return
name|parentTracker
operator|.
name|hasTasks
argument_list|()
return|;
block|}
name|ImportTableDesc
name|overrideProperties
parameter_list|(
name|ImportTableDesc
name|importTableDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|tableNameToLoadIn
argument_list|)
condition|)
block|{
name|importTableDesc
operator|.
name|setTableName
argument_list|(
name|tableNameToLoadIn
argument_list|)
expr_stmt|;
comment|//For table level load, add this property to avoid duplicate copy.
comment|// This flag will be set to false after first incremental load is done. This flag is used by
comment|// repl copy task to check if duplicate file check is required or not. This flag is used by
comment|// compaction to check if compaction can be done for this database or not. If compaction is
comment|// done before first incremental then duplicate check will fail as compaction may change
comment|// the directory structure.
name|importTableDesc
operator|.
name|getTblProps
argument_list|()
operator|.
name|put
argument_list|(
name|ReplUtils
operator|.
name|REPL_FIRST_INC_PENDING_FLAG
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
return|return
name|importTableDesc
return|;
block|}
block|}
end_class

end_unit


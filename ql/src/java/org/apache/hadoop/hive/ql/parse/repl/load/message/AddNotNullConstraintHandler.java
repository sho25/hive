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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
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
name|metastore
operator|.
name|api
operator|.
name|SQLCheckConstraint
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
name|metastore
operator|.
name|api
operator|.
name|SQLDefaultConstraint
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
name|metastore
operator|.
name|api
operator|.
name|SQLForeignKey
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
name|metastore
operator|.
name|api
operator|.
name|SQLNotNullConstraint
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
name|metastore
operator|.
name|api
operator|.
name|SQLPrimaryKey
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
name|metastore
operator|.
name|api
operator|.
name|SQLUniqueConstraint
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
name|metastore
operator|.
name|messaging
operator|.
name|AddNotNullConstraintMessage
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
name|Task
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
name|TaskFactory
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
name|AlterTableDesc
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
name|DDLWork
import|;
end_import

begin_class
specifier|public
class|class
name|AddNotNullConstraintHandler
extends|extends
name|AbstractMessageHandler
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|handle
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
name|AddNotNullConstraintMessage
name|msg
init|=
name|deserializer
operator|.
name|getAddNotNullConstraintMessage
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getPayload
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SQLNotNullConstraint
argument_list|>
name|nns
init|=
literal|null
decl_stmt|;
try|try
block|{
name|nns
operator|=
name|msg
operator|.
name|getNotNullConstraints
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|e
operator|instanceof
name|SemanticException
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Error reading message members"
argument_list|,
name|e
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|(
name|SemanticException
operator|)
name|e
throw|;
block|}
block|}
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|nns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|tasks
return|;
block|}
name|String
name|actualDbName
init|=
name|context
operator|.
name|isDbNameEmpty
argument_list|()
condition|?
name|nns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_db
argument_list|()
else|:
name|context
operator|.
name|dbName
decl_stmt|;
name|String
name|actualTblName
init|=
name|context
operator|.
name|isTableNameEmpty
argument_list|()
condition|?
name|nns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_name
argument_list|()
else|:
name|context
operator|.
name|tableName
decl_stmt|;
for|for
control|(
name|SQLNotNullConstraint
name|nn
range|:
name|nns
control|)
block|{
name|nn
operator|.
name|setTable_db
argument_list|(
name|actualDbName
argument_list|)
expr_stmt|;
name|nn
operator|.
name|setTable_name
argument_list|(
name|actualTblName
argument_list|)
expr_stmt|;
block|}
name|AlterTableDesc
name|addConstraintsDesc
init|=
operator|new
name|AlterTableDesc
argument_list|(
name|actualDbName
operator|+
literal|"."
operator|+
name|actualTblName
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SQLPrimaryKey
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SQLForeignKey
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SQLUniqueConstraint
argument_list|>
argument_list|()
argument_list|,
name|nns
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SQLDefaultConstraint
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SQLCheckConstraint
argument_list|>
argument_list|()
argument_list|,
name|context
operator|.
name|eventOnlyReplicationSpec
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|addConstraintsTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|readEntitySet
argument_list|,
name|writeEntitySet
argument_list|,
name|addConstraintsDesc
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|tasks
operator|.
name|add
argument_list|(
name|addConstraintsTask
argument_list|)
expr_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Added add constrains task : {}:{}"
argument_list|,
name|addConstraintsTask
operator|.
name|getId
argument_list|()
argument_list|,
name|actualTblName
argument_list|)
expr_stmt|;
name|updatedMetadata
operator|.
name|set
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|actualDbName
argument_list|,
name|actualTblName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|addConstraintsTask
argument_list|)
return|;
block|}
block|}
end_class

end_unit


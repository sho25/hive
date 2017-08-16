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
name|FieldSchema
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
name|AlterPartitionMessage
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
name|DDLWork
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
name|TruncateTableDesc
import|;
end_import

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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|TruncatePartitionHandler
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
name|AlterPartitionMessage
name|msg
init|=
name|deserializer
operator|.
name|getAlterPartitionMessage
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getPayload
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|actualDbName
init|=
name|context
operator|.
name|isDbNameEmpty
argument_list|()
condition|?
name|msg
operator|.
name|getDB
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
name|msg
operator|.
name|getTable
argument_list|()
else|:
name|context
operator|.
name|tableName
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
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
name|Table
name|tblObj
init|=
name|msg
operator|.
name|getTableObj
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|afterIterator
init|=
name|msg
operator|.
name|getPtnObjAfter
argument_list|()
operator|.
name|getValuesIterator
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|tblObj
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
name|partSpec
operator|.
name|put
argument_list|(
name|fs
operator|.
name|getName
argument_list|()
argument_list|,
name|afterIterator
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|TruncateTableDesc
name|truncateTableDesc
init|=
operator|new
name|TruncateTableDesc
argument_list|(
name|actualDbName
operator|+
literal|"."
operator|+
name|actualTblName
argument_list|,
name|partSpec
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
name|truncatePtnTask
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
name|truncateTableDesc
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Added truncate ptn task : {}:{}"
argument_list|,
name|truncatePtnTask
operator|.
name|getId
argument_list|()
argument_list|,
name|truncateTableDesc
operator|.
name|getTableName
argument_list|()
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
name|partSpec
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|truncatePtnTask
argument_list|)
return|;
block|}
block|}
end_class

end_unit


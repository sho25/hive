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
name|messaging
operator|.
name|DropPartitionMessage
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
name|metadata
operator|.
name|Table
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
name|DDLSemanticAnalyzer
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
name|DropTableDesc
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
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|ExprNodeGenericFuncDesc
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
name|PrimitiveTypeInfo
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
name|TypeInfoFactory
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
name|HashMap
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
name|DropPartitionHandler
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
try|try
block|{
name|DropPartitionMessage
name|msg
init|=
name|deserializer
operator|.
name|getDropPartitionMessage
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
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|partSpecs
init|=
name|genPartSpecs
argument_list|(
operator|new
name|Table
argument_list|(
name|msg
operator|.
name|getTableObj
argument_list|()
argument_list|)
argument_list|,
name|msg
operator|.
name|getPartitions
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partSpecs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|DropTableDesc
name|dropPtnDesc
init|=
operator|new
name|DropTableDesc
argument_list|(
name|actualDbName
operator|+
literal|"."
operator|+
name|actualTblName
argument_list|,
name|partSpecs
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
name|eventOnlyReplicationSpec
argument_list|(
name|context
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|dropPtnTask
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
name|dropPtnDesc
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
literal|"Added drop ptn task : {}:{},{}"
argument_list|,
name|dropPtnTask
operator|.
name|getId
argument_list|()
argument_list|,
name|dropPtnDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|msg
operator|.
name|getPartitions
argument_list|()
argument_list|)
expr_stmt|;
name|databasesUpdated
operator|.
name|put
argument_list|(
name|actualDbName
argument_list|,
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
argument_list|)
expr_stmt|;
name|tablesUpdated
operator|.
name|put
argument_list|(
name|actualDbName
operator|+
literal|"."
operator|+
name|actualTblName
argument_list|,
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|dropPtnTask
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"DROP PARTITION EVENT does not return any part descs for event message :"
operator|+
name|context
operator|.
name|dmd
operator|.
name|getPayload
argument_list|()
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|e
operator|instanceof
name|SemanticException
operator|)
condition|?
operator|(
name|SemanticException
operator|)
name|e
else|:
operator|new
name|SemanticException
argument_list|(
literal|"Error reading message members"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|genPartSpecs
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|partSpecs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|partPrefixLength
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|partitions
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|partPrefixLength
operator|=
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// pick the length of the first ptn, we expect all ptns listed to have the same number of
comment|// key-vals.
block|}
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
name|partitionDesc
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ptn
range|:
name|partitions
control|)
block|{
comment|// convert each key-value-map to appropriate expression.
name|ExprNodeGenericFuncDesc
name|expr
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kvp
range|:
name|ptn
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|kvp
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|val
init|=
name|kvp
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|type
init|=
name|table
operator|.
name|getPartColByName
argument_list|(
name|key
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|PrimitiveTypeInfo
name|pti
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|column
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|pti
argument_list|,
name|key
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|op
init|=
name|DDLSemanticAnalyzer
operator|.
name|makeBinaryPredicate
argument_list|(
literal|"="
argument_list|,
name|column
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|pti
argument_list|,
name|val
argument_list|)
argument_list|)
decl_stmt|;
name|expr
operator|=
operator|(
name|expr
operator|==
literal|null
operator|)
condition|?
name|op
else|:
name|DDLSemanticAnalyzer
operator|.
name|makeBinaryPredicate
argument_list|(
literal|"and"
argument_list|,
name|expr
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|expr
operator|!=
literal|null
condition|)
block|{
name|partitionDesc
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|partitionDesc
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|partSpecs
operator|.
name|put
argument_list|(
name|partPrefixLength
argument_list|,
name|partitionDesc
argument_list|)
expr_stmt|;
block|}
return|return
name|partSpecs
return|;
block|}
block|}
end_class

end_unit

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
name|MessageDeserializer
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
name|MessageFactory
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|ReplicationSpec
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
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|abstract
class|class
name|AbstractMessageHandler
implements|implements
name|MessageHandler
block|{
specifier|final
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|readEntitySet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|writeEntitySet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tablesUpdated
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|,
name|databasesUpdated
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|MessageDeserializer
name|deserializer
init|=
name|MessageFactory
operator|.
name|getInstance
argument_list|()
operator|.
name|getDeserializer
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|readEntities
parameter_list|()
block|{
return|return
name|readEntitySet
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|writeEntities
parameter_list|()
block|{
return|return
name|writeEntitySet
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tablesUpdated
parameter_list|()
block|{
return|return
name|tablesUpdated
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|databasesUpdated
parameter_list|()
block|{
return|return
name|databasesUpdated
return|;
block|}
name|ReplicationSpec
name|eventOnlyReplicationSpec
parameter_list|(
name|Context
name|forContext
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|eventId
init|=
name|forContext
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|replicationSpec
argument_list|(
name|eventId
argument_list|,
name|eventId
argument_list|)
return|;
block|}
specifier|private
name|ReplicationSpec
name|replicationSpec
parameter_list|(
name|String
name|fromId
parameter_list|,
name|String
name|toId
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
operator|new
name|ReplicationSpec
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|fromId
argument_list|,
name|toId
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
return|;
block|}
block|}
end_class

end_unit

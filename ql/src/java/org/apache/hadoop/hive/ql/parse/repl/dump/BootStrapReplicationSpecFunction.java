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
name|dump
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|HiveException
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

begin_class
class|class
name|BootStrapReplicationSpecFunction
implements|implements
name|HiveWrapper
operator|.
name|Tuple
operator|.
name|Function
argument_list|<
name|ReplicationSpec
argument_list|>
block|{
specifier|private
specifier|final
name|Hive
name|db
decl_stmt|;
specifier|private
specifier|final
name|long
name|currentNotificationId
decl_stmt|;
name|BootStrapReplicationSpecFunction
parameter_list|(
name|Hive
name|db
parameter_list|,
name|long
name|currentNotificationId
parameter_list|)
block|{
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|currentNotificationId
operator|=
name|currentNotificationId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationSpec
name|fromMetaStore
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
block|{
name|long
name|currentReplicationState
init|=
operator|(
name|this
operator|.
name|currentNotificationId
operator|>
literal|0
operator|)
condition|?
name|this
operator|.
name|currentNotificationId
else|:
name|db
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
decl_stmt|;
name|ReplicationSpec
name|replicationSpec
init|=
operator|new
name|ReplicationSpec
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|"replv2"
argument_list|,
literal|"will-be-set"
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|replicationSpec
operator|.
name|setCurrentReplicationState
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|currentReplicationState
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|replicationSpec
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


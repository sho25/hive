begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|classification
operator|.
name|InterfaceAudience
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
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|rpc
operator|.
name|RpcServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
import|;
end_import

begin_comment
comment|/**  * Factory for SparkClient instances.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|SparkClientFactory
block|{
comment|/** Used to run the driver in-process, mostly for testing. */
specifier|static
specifier|final
name|String
name|CONF_KEY_IN_PROCESS
init|=
literal|"spark.client.do_not_use.run_driver_in_process"
decl_stmt|;
comment|/** Used by client and driver to share a client ID for establishing an RPC session. */
specifier|static
specifier|final
name|String
name|CONF_CLIENT_ID
init|=
literal|"spark.client.authentication.client_id"
decl_stmt|;
comment|/** Used by client and driver to share a secret for establishing an RPC session. */
specifier|static
specifier|final
name|String
name|CONF_KEY_SECRET
init|=
literal|"spark.client.authentication.secret"
decl_stmt|;
specifier|private
specifier|static
name|RpcServer
name|server
init|=
literal|null
decl_stmt|;
comment|/**    * Initializes the SparkClient library. Must be called before creating client instances.    *    * @param conf Map containing configuration parameters for the client library.    */
specifier|public
specifier|static
specifier|synchronized
name|void
name|initialize
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|server
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|server
operator|=
operator|new
name|RpcServer
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
name|Throwables
operator|.
name|propagate
argument_list|(
name|ie
argument_list|)
throw|;
block|}
block|}
block|}
comment|/** Stops the SparkClient library. */
specifier|public
specifier|static
specifier|synchronized
name|void
name|stop
parameter_list|()
block|{
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|server
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Instantiates a new Spark client.    *    * @param sparkConf Configuration for the remote Spark application, contains spark.* properties.    * @param hiveConf Configuration for Hive, contains hive.* properties.    */
specifier|public
specifier|static
specifier|synchronized
name|SparkClient
name|createClient
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sparkConf
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
throws|,
name|SparkException
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|server
operator|!=
literal|null
argument_list|,
literal|"initialize() not called."
argument_list|)
expr_stmt|;
return|return
operator|new
name|SparkClientImpl
argument_list|(
name|server
argument_list|,
name|sparkConf
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
block|}
end_class

end_unit


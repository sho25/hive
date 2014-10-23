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
name|akka
operator|.
name|actor
operator|.
name|ActorSystem
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
name|collect
operator|.
name|Maps
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

begin_comment
comment|/**  * Factory for SparkClient instances.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SparkClientFactory
block|{
specifier|static
name|ActorSystem
name|actorSystem
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|akkaUrl
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|secret
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
comment|/**    * Initializes the SparkClient library. Must be called before creating client instances.    *    * @param conf Map containing configuration parameters for the client.    */
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
name|secret
operator|=
name|akka
operator|.
name|util
operator|.
name|Crypt
operator|.
name|generateSecureCookie
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|akkaConf
init|=
name|Maps
operator|.
name|newHashMap
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|akkaConf
operator|.
name|put
argument_list|(
name|ClientUtils
operator|.
name|CONF_KEY_SECRET
argument_list|,
name|secret
argument_list|)
expr_stmt|;
name|ClientUtils
operator|.
name|ActorSystemInfo
name|info
init|=
name|ClientUtils
operator|.
name|createActorSystem
argument_list|(
name|akkaConf
argument_list|)
decl_stmt|;
name|actorSystem
operator|=
name|info
operator|.
name|system
expr_stmt|;
name|akkaUrl
operator|=
name|info
operator|.
name|url
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
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
name|initialized
condition|)
block|{
name|actorSystem
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|actorSystem
operator|=
literal|null
expr_stmt|;
name|akkaUrl
operator|=
literal|null
expr_stmt|;
name|secret
operator|=
literal|null
expr_stmt|;
name|initialized
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**    * Instantiates a new Spark client.    *    * @param conf Configuration for the remote Spark application.    */
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
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|SparkException
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Library is not initialized. Call initialize() first."
argument_list|)
throw|;
block|}
return|return
operator|new
name|SparkClientImpl
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit


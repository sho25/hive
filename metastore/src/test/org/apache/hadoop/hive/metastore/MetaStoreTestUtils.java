begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
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
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
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
name|conf
operator|.
name|Configuration
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|HiveMetaStore
operator|.
name|HMSHandler
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
name|conf
operator|.
name|MetastoreConf
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
name|events
operator|.
name|EventCleanerTask
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
name|security
operator|.
name|HadoopThriftAuthBridge
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|MetaStoreTestUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"hive.log"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|startMetaStore
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|MetaStoreTestUtils
operator|.
name|startMetaStore
argument_list|(
name|HadoopThriftAuthBridge
operator|.
name|getBridge
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|startMetaStore
parameter_list|(
specifier|final
name|HadoopThriftAuthBridge
name|bridge
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|port
init|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
decl_stmt|;
name|MetaStoreTestUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|bridge
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|port
return|;
block|}
specifier|public
specifier|static
name|int
name|startMetaStore
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|startMetaStore
argument_list|(
name|HadoopThriftAuthBridge
operator|.
name|getBridge
argument_list|()
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|startMetaStore
parameter_list|(
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|HadoopThriftAuthBridge
name|bridge
parameter_list|)
throws|throws
name|Exception
block|{
name|MetaStoreTestUtils
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|bridge
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|startMetaStore
parameter_list|(
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|HadoopThriftAuthBridge
name|bridge
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|hiveConf
operator|==
literal|null
condition|)
block|{
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|HMSHandler
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|final
name|HiveConf
name|finalHiveConf
init|=
name|hiveConf
decl_stmt|;
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|HiveMetaStore
operator|.
name|startMetaStore
argument_list|(
name|port
argument_list|,
name|bridge
argument_list|,
name|finalHiveConf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Metastore Thrift Server threw an exception..."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|thread
operator|.
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|loopUntilHMSReady
argument_list|(
name|port
argument_list|)
expr_stmt|;
block|}
comment|/**    * A simple connect test to make sure that the metastore is up    * @throws Exception    */
specifier|public
specifier|static
name|void
name|loopUntilHMSReady
parameter_list|(
name|int
name|port
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|retries
init|=
literal|0
decl_stmt|;
name|Exception
name|exc
init|=
literal|null
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|Socket
name|socket
init|=
operator|new
name|Socket
argument_list|()
decl_stmt|;
name|socket
operator|.
name|connect
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|port
argument_list|)
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|retries
operator|++
operator|>
literal|60
condition|)
block|{
comment|//give up
name|exc
operator|=
name|e
expr_stmt|;
break|break;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
comment|// something is preventing metastore from starting
comment|// print the stack from all threads for debugging purposes
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to connect to metastore server: "
operator|+
name|exc
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Printing all thread stack traces for debugging before throwing exception."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|MetaStoreTestUtils
operator|.
name|getAllThreadStacksAsString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|exc
throw|;
block|}
specifier|public
specifier|static
name|String
name|getAllThreadStacksAsString
parameter_list|()
block|{
name|Map
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|threadStacks
init|=
name|Thread
operator|.
name|getAllStackTraces
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Thread
argument_list|,
name|StackTraceElement
index|[]
argument_list|>
name|entry
range|:
name|threadStacks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Thread
name|t
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Name: "
argument_list|)
operator|.
name|append
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" State: "
argument_list|)
operator|.
name|append
argument_list|(
name|t
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|addStackString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|sb
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|addStackString
parameter_list|(
name|StackTraceElement
index|[]
name|stackElems
parameter_list|,
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|StackTraceElement
name|stackElem
range|:
name|stackElems
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|stackElem
argument_list|)
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Finds a free port on the machine.    *    * @return    * @throws IOException    */
specifier|public
specifier|static
name|int
name|findFreePort
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerSocket
name|socket
init|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|socket
operator|.
name|getLocalPort
argument_list|()
decl_stmt|;
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|port
return|;
block|}
comment|/**    * Finds a free port on the machine, but allow the    * ability to specify a port number to not use, no matter what.    */
specifier|public
specifier|static
name|int
name|findFreePortExcepting
parameter_list|(
name|int
name|portToExclude
parameter_list|)
throws|throws
name|IOException
block|{
name|ServerSocket
name|socket1
init|=
literal|null
decl_stmt|;
name|ServerSocket
name|socket2
init|=
literal|null
decl_stmt|;
try|try
block|{
name|socket1
operator|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|socket2
operator|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|socket1
operator|.
name|getLocalPort
argument_list|()
operator|!=
name|portToExclude
condition|)
block|{
return|return
name|socket1
operator|.
name|getLocalPort
argument_list|()
return|;
block|}
comment|// If we're here, then socket1.getLocalPort was the port to exclude
comment|// Since both sockets were open together at a point in time, we're
comment|// guaranteed that socket2.getLocalPort() is not the same.
return|return
name|socket2
operator|.
name|getLocalPort
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|socket1
operator|!=
literal|null
condition|)
block|{
name|socket1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|socket2
operator|!=
literal|null
condition|)
block|{
name|socket2
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


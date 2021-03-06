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
name|processors
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
import|;
end_import

begin_import
import|import static
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
name|MetadataTypedColumnsetSerDe
operator|.
name|defaultNullString
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|net
operator|.
name|SocketFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLineParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|GnuParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|HelpFormatter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|ParseException
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
name|VariableSubstitution
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
name|llap
operator|.
name|daemon
operator|.
name|rpc
operator|.
name|LlapDaemonProtocolProtos
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
name|llap
operator|.
name|impl
operator|.
name|LlapManagementProtocolClientImpl
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
name|llap
operator|.
name|registry
operator|.
name|LlapServiceInstance
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
name|llap
operator|.
name|registry
operator|.
name|impl
operator|.
name|LlapRegistryService
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
name|api
operator|.
name|Schema
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HiveOperationType
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
name|session
operator|.
name|SessionState
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
name|io
operator|.
name|retry
operator|.
name|RetryPolicies
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
name|io
operator|.
name|retry
operator|.
name|RetryPolicy
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
name|net
operator|.
name|NetUtils
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
name|Joiner
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
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|LlapCacheResourceProcessor
implements|implements
name|CommandProcessor
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
name|LlapCacheResourceProcessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Options
name|CACHE_OPTIONS
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
specifier|private
name|HelpFormatter
name|helpFormatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|LlapCacheResourceProcessor
parameter_list|()
block|{
name|CACHE_OPTIONS
operator|.
name|addOption
argument_list|(
literal|"purge"
argument_list|,
literal|"purge"
argument_list|,
literal|false
argument_list|,
literal|"Purge LLAP IO cache"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CommandProcessorResponse
name|run
parameter_list|(
name|String
name|command
parameter_list|)
throws|throws
name|CommandProcessorException
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|command
operator|=
operator|new
name|VariableSubstitution
argument_list|(
parameter_list|()
lambda|->
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
argument_list|)
operator|.
name|substitute
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|,
name|command
argument_list|)
expr_stmt|;
name|String
index|[]
name|tokens
init|=
name|command
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokens
operator|.
name|length
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|CommandProcessorException
argument_list|(
literal|"LLAP Cache Processor Helper Failed: Command arguments are empty."
argument_list|)
throw|;
block|}
name|String
name|params
index|[]
init|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|tokens
argument_list|,
literal|1
argument_list|,
name|tokens
operator|.
name|length
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|llapCacheCommandHandler
argument_list|(
name|ss
argument_list|,
name|params
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|CommandProcessorException
argument_list|(
literal|"LLAP Cache Processor Helper Failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|CommandProcessorResponse
name|llapCacheCommandHandler
parameter_list|(
name|SessionState
name|ss
parameter_list|,
name|String
index|[]
name|params
parameter_list|)
throws|throws
name|ParseException
throws|,
name|CommandProcessorException
block|{
name|CommandLine
name|args
init|=
name|parseCommandArgs
argument_list|(
name|CACHE_OPTIONS
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|boolean
name|purge
init|=
name|args
operator|.
name|hasOption
argument_list|(
literal|"purge"
argument_list|)
decl_stmt|;
name|String
name|hs2Host
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ss
operator|.
name|isHiveServerQuery
argument_list|()
condition|)
block|{
name|hs2Host
operator|=
name|ss
operator|.
name|getHiveServer2Host
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|purge
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fullCommand
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"llap"
argument_list|,
literal|"cache"
argument_list|)
decl_stmt|;
name|fullCommand
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|authErrResp
init|=
name|CommandUtil
operator|.
name|authorizeCommandAndServiceObject
argument_list|(
name|ss
argument_list|,
name|HiveOperationType
operator|.
name|LLAP_CACHE_PURGE
argument_list|,
name|fullCommand
argument_list|,
name|hs2Host
argument_list|)
decl_stmt|;
if|if
condition|(
name|authErrResp
operator|!=
literal|null
condition|)
block|{
comment|// there was an authorization issue
return|return
name|authErrResp
return|;
block|}
try|try
block|{
name|LlapRegistryService
name|llapRegistryService
init|=
name|LlapRegistryService
operator|.
name|getClient
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|llapCachePurge
argument_list|(
name|ss
argument_list|,
name|llapRegistryService
argument_list|)
expr_stmt|;
return|return
operator|new
name|CommandProcessorResponse
argument_list|(
name|getSchema
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while purging LLAP IO Cache. err: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|CommandProcessorException
argument_list|(
literal|"LLAP Cache Processor Helper Failed: Error while purging LLAP IO Cache. err: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|String
name|usage
init|=
name|getUsageAsString
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|CommandProcessorException
argument_list|(
literal|"LLAP Cache Processor Helper Failed: Unsupported sub-command option. "
operator|+
name|usage
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Schema
name|getSchema
parameter_list|()
block|{
name|Schema
name|sch
init|=
operator|new
name|Schema
argument_list|()
decl_stmt|;
name|sch
operator|.
name|addToFieldSchemas
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"hostName"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|sch
operator|.
name|addToFieldSchemas
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"purgedMemoryBytes"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|sch
operator|.
name|putToProperties
argument_list|(
name|SERIALIZATION_NULL_FORMAT
argument_list|,
name|defaultNullString
argument_list|)
expr_stmt|;
return|return
name|sch
return|;
block|}
specifier|private
name|void
name|llapCachePurge
parameter_list|(
specifier|final
name|SessionState
name|ss
parameter_list|,
specifier|final
name|LlapRegistryService
name|llapRegistryService
parameter_list|)
throws|throws
name|Exception
block|{
name|ExecutorService
name|executorService
init|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Future
argument_list|<
name|Long
argument_list|>
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|LlapServiceInstance
argument_list|>
name|instances
init|=
name|llapRegistryService
operator|.
name|getInstances
argument_list|()
operator|.
name|getAll
argument_list|()
decl_stmt|;
for|for
control|(
name|LlapServiceInstance
name|instance
range|:
name|instances
control|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|executorService
operator|.
name|submit
argument_list|(
operator|new
name|PurgeCallable
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|,
name|instance
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|LlapServiceInstance
name|instance
range|:
name|instances
control|)
block|{
name|Future
argument_list|<
name|Long
argument_list|>
name|future
init|=
name|futures
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ss
operator|.
name|out
operator|.
name|println
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
literal|"\t"
argument_list|)
operator|.
name|join
argument_list|(
name|instance
operator|.
name|getHost
argument_list|()
argument_list|,
name|future
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|PurgeCallable
implements|implements
name|Callable
argument_list|<
name|Long
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PurgeCallable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|LlapServiceInstance
name|instance
decl_stmt|;
specifier|private
name|SocketFactory
name|socketFactory
decl_stmt|;
specifier|private
name|RetryPolicy
name|retryPolicy
decl_stmt|;
name|PurgeCallable
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|LlapServiceInstance
name|llapServiceInstance
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|llapServiceInstance
expr_stmt|;
name|this
operator|.
name|socketFactory
operator|=
name|NetUtils
operator|.
name|getDefaultSocketFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|//not making this configurable, best effort
name|this
operator|.
name|retryPolicy
operator|=
name|RetryPolicies
operator|.
name|retryUpToMaximumTimeWithFixedSleep
argument_list|(
literal|10000
argument_list|,
literal|2000L
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|call
parameter_list|()
block|{
try|try
block|{
name|LlapManagementProtocolClientImpl
name|client
init|=
operator|new
name|LlapManagementProtocolClientImpl
argument_list|(
name|conf
argument_list|,
name|instance
operator|.
name|getHost
argument_list|()
argument_list|,
name|instance
operator|.
name|getManagementPort
argument_list|()
argument_list|,
name|retryPolicy
argument_list|,
name|socketFactory
argument_list|)
decl_stmt|;
name|LlapDaemonProtocolProtos
operator|.
name|PurgeCacheResponseProto
name|resp
init|=
name|client
operator|.
name|purgeCache
argument_list|(
literal|null
argument_list|,
name|LlapDaemonProtocolProtos
operator|.
name|PurgeCacheRequestProto
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|resp
operator|.
name|getPurgedMemoryBytes
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while purging cache."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|0L
return|;
block|}
block|}
block|}
specifier|private
name|String
name|getUsageAsString
parameter_list|()
block|{
name|StringWriter
name|out
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|pw
init|=
operator|new
name|PrintWriter
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|helpFormatter
operator|.
name|printUsage
argument_list|(
name|pw
argument_list|,
name|helpFormatter
operator|.
name|getWidth
argument_list|()
argument_list|,
literal|"llap cache"
argument_list|,
name|CACHE_OPTIONS
argument_list|)
expr_stmt|;
name|pw
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|out
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|CommandLine
name|parseCommandArgs
parameter_list|(
specifier|final
name|Options
name|opts
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|ParseException
block|{
name|CommandLineParser
name|parser
init|=
operator|new
name|GnuParser
argument_list|()
decl_stmt|;
return|return
name|parser
operator|.
name|parse
argument_list|(
name|opts
argument_list|,
name|args
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{   }
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|ConcurrentHashMap
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
name|LinkedBlockingQueue
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
name|io
operator|.
name|FileUtils
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|Function
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
name|MetaException
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
name|ResourceUri
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
name|AddToClassPathAction
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
name|FunctionRegistry
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
name|FunctionUtils
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
name|UDFClassLoader
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
name|FunctionInfo
operator|.
name|FunctionResource
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
name|session
operator|.
name|SessionState
operator|.
name|ResourceType
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBridge
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
name|util
operator|.
name|ResourceDownloader
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

begin_comment
comment|/**  * This class localizes and manages jars for the functions allowed inside LLAP.  */
end_comment

begin_class
specifier|public
class|class
name|FunctionLocalizer
implements|implements
name|GenericUDFBridge
operator|.
name|UdfWhitelistChecker
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DIR_NAME
init|=
literal|"fnresources"
decl_stmt|;
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
name|FunctionLocalizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ResourceDownloader
name|resourceDownloader
decl_stmt|;
specifier|private
specifier|final
name|LinkedBlockingQueue
argument_list|<
name|LocalizerWork
argument_list|>
name|workQueue
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|recentlyLocalizedJars
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|recentlyLocalizedClasses
init|=
operator|new
name|LinkedList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Thread
name|workThread
decl_stmt|;
specifier|private
specifier|final
name|File
name|localDir
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|UDFClassLoader
name|executorClassloader
decl_stmt|;
specifier|private
specifier|final
name|IdentityHashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Boolean
argument_list|>
name|allowedUdfClasses
init|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|FnResources
argument_list|>
name|resourcesByFn
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|URI
argument_list|,
name|RefCountedResource
argument_list|>
name|localFiles
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|FunctionLocalizer
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|localDir
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
name|localDir
operator|=
operator|new
name|File
argument_list|(
name|localDir
argument_list|,
name|DIR_NAME
argument_list|)
expr_stmt|;
name|AddToClassPathAction
name|addAction
init|=
operator|new
name|AddToClassPathAction
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|this
operator|.
name|executorClassloader
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
name|addAction
argument_list|)
expr_stmt|;
name|this
operator|.
name|workThread
operator|=
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
name|runWorkThread
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|localDir
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// TODO: We don't want some random jars of unknown provenance sitting around. Or do we care?
comment|//       Ideally, we should try to reuse jars and verify using some checksum.
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
name|localDir
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|resourceDownloader
operator|=
operator|new
name|ResourceDownloader
argument_list|(
name|conf
argument_list|,
name|localDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|workThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|isUdfAllowed
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
name|FunctionRegistry
operator|.
name|isBuiltInFuncClass
argument_list|(
name|clazz
argument_list|)
operator|||
name|allowedUdfClasses
operator|.
name|containsKey
argument_list|(
name|clazz
argument_list|)
return|;
block|}
specifier|public
name|ClassLoader
name|getClassLoader
parameter_list|()
block|{
return|return
name|executorClassloader
return|;
block|}
specifier|public
name|void
name|startLocalizeAllFunctions
parameter_list|()
throws|throws
name|HiveException
block|{
name|Hive
name|hive
init|=
name|Hive
operator|.
name|get
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// Do not allow embedded metastore in LLAP unless we are in test.
try|try
block|{
name|hive
operator|.
name|getMSC
argument_list|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Function
argument_list|>
name|fns
init|=
name|hive
operator|.
name|getAllFunctions
argument_list|()
decl_stmt|;
for|for
control|(
name|Function
name|fn
range|:
name|fns
control|)
block|{
name|String
name|fqfn
init|=
name|fn
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|fn
operator|.
name|getFunctionName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
init|=
name|fn
operator|.
name|getResourceUris
argument_list|()
decl_stmt|;
if|if
condition|(
name|resources
operator|==
literal|null
operator|||
name|resources
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
comment|// Nothing to localize.
name|FnResources
name|result
init|=
operator|new
name|FnResources
argument_list|()
decl_stmt|;
name|resourcesByFn
operator|.
name|put
argument_list|(
name|fqfn
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|workQueue
operator|.
name|add
argument_list|(
operator|new
name|LocalizeFn
argument_list|(
name|fqfn
argument_list|,
name|resources
argument_list|,
name|result
argument_list|,
name|fn
operator|.
name|getClassName
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|workQueue
operator|.
name|add
argument_list|(
operator|new
name|RefreshClassloader
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
name|isClosed
operator|=
literal|true
expr_stmt|;
name|workThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
try|try
block|{
name|workThread
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// Give it some time, then don't delay shutdown too much.
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Interrupted during close"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|runWorkThread
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|isClosed
condition|)
block|{
name|deleteAllLocalResources
argument_list|()
expr_stmt|;
return|return;
block|}
name|LocalizerWork
name|lw
init|=
literal|null
decl_stmt|;
try|try
block|{
name|lw
operator|=
name|workQueue
operator|.
name|take
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Localizer thread interrupted"
argument_list|)
expr_stmt|;
name|isClosed
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|isClosed
condition|)
block|{
name|deleteAllLocalResources
argument_list|()
expr_stmt|;
return|return;
block|}
try|try
block|{
name|lw
operator|.
name|run
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Localizer thread interrupted"
argument_list|)
expr_stmt|;
name|isClosed
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to run "
operator|+
name|lw
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
interface|interface
name|LocalizerWork
block|{
name|void
name|run
parameter_list|(
name|FunctionLocalizer
name|parent
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
throws|,
name|InterruptedException
function_decl|;
block|}
specifier|private
specifier|static
class|class
name|LocalizeFn
implements|implements
name|LocalizerWork
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
decl_stmt|;
specifier|private
specifier|final
name|FnResources
name|result
decl_stmt|;
specifier|private
specifier|final
name|String
name|fqfn
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|doRefreshClassloader
decl_stmt|;
specifier|private
specifier|final
name|String
name|className
decl_stmt|;
specifier|public
name|LocalizeFn
parameter_list|(
name|String
name|fqfn
parameter_list|,
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|,
name|FnResources
name|result
parameter_list|,
name|String
name|className
parameter_list|,
name|boolean
name|doRefreshClassloader
parameter_list|)
block|{
name|this
operator|.
name|resources
operator|=
name|resources
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
name|this
operator|.
name|fqfn
operator|=
name|fqfn
expr_stmt|;
name|this
operator|.
name|className
operator|=
name|className
expr_stmt|;
name|this
operator|.
name|doRefreshClassloader
operator|=
name|doRefreshClassloader
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|(
name|FunctionLocalizer
name|parent
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
block|{
name|parent
operator|.
name|localizeFunctionResources
argument_list|(
name|fqfn
argument_list|,
name|resources
argument_list|,
name|className
argument_list|,
name|result
argument_list|,
name|doRefreshClassloader
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"localize "
operator|+
name|resources
operator|.
name|size
argument_list|()
operator|+
literal|" resources for "
operator|+
name|fqfn
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|RefreshClassloader
implements|implements
name|LocalizerWork
block|{
specifier|public
name|void
name|run
parameter_list|(
name|FunctionLocalizer
name|parent
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
block|{
name|parent
operator|.
name|refreshClassloader
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"load the recently localized jars"
return|;
block|}
block|}
specifier|private
name|void
name|deleteAllLocalResources
parameter_list|()
block|{
try|try
block|{
name|executorClassloader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to close the classloader"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|resourcesByFn
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|RefCountedResource
name|rcr
range|:
name|localFiles
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|FunctionResource
name|fr
range|:
name|rcr
operator|.
name|resources
control|)
block|{
comment|// We ignore refcounts (and errors) for now.
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|fr
operator|.
name|getResourceURI
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|file
operator|.
name|delete
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to delete "
operator|+
name|file
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to delete "
operator|+
name|file
operator|+
literal|": "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
name|void
name|refreshClassloader
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|recentlyLocalizedJars
operator|.
name|isEmpty
argument_list|()
condition|)
return|return;
name|String
index|[]
name|jars
init|=
name|recentlyLocalizedJars
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|recentlyLocalizedJars
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ClassLoader
name|updatedCl
init|=
literal|null
decl_stmt|;
try|try
block|{
name|AddToClassPathAction
name|addAction
init|=
operator|new
name|AddToClassPathAction
argument_list|(
name|executorClassloader
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|jars
argument_list|)
argument_list|)
decl_stmt|;
name|updatedCl
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
name|addAction
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Added "
operator|+
name|jars
operator|.
name|length
operator|+
literal|" jars to classpath"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// TODO: we could fall back to trying one by one and only ignore the failed ones.
name|logRefreshError
argument_list|(
literal|"Unable to localize jars: "
argument_list|,
name|jars
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return;
comment|// logRefreshError always throws.
block|}
if|if
condition|(
name|updatedCl
operator|!=
name|executorClassloader
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Classloader was replaced despite using UDFClassLoader: new "
operator|+
name|updatedCl
operator|+
literal|", old "
operator|+
name|executorClassloader
argument_list|)
throw|;
block|}
name|String
index|[]
name|classNames
init|=
name|recentlyLocalizedClasses
operator|.
name|toArray
argument_list|(
name|jars
argument_list|)
decl_stmt|;
name|recentlyLocalizedClasses
operator|.
name|clear
argument_list|()
expr_stmt|;
try|try
block|{
for|for
control|(
name|String
name|className
range|:
name|classNames
control|)
block|{
name|allowedUdfClasses
operator|.
name|put
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|,
literal|false
argument_list|,
name|executorClassloader
argument_list|)
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// TODO: we could fall back to trying one by one and only ignore the failed ones.
name|logRefreshError
argument_list|(
literal|"Unable to instantiate localized classes: "
argument_list|,
name|classNames
argument_list|,
name|t
argument_list|)
expr_stmt|;
return|return;
comment|// logRefreshError always throws.
block|}
block|}
specifier|private
name|void
name|logRefreshError
parameter_list|(
name|String
name|what
parameter_list|,
name|String
index|[]
name|items
parameter_list|,
name|Throwable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|item
range|:
name|items
control|)
block|{
name|what
operator|+=
operator|(
name|item
operator|+
literal|", "
operator|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|what
argument_list|,
name|t
argument_list|)
throw|;
block|}
specifier|private
name|void
name|localizeFunctionResources
parameter_list|(
name|String
name|fqfn
parameter_list|,
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|,
name|String
name|className
parameter_list|,
name|FnResources
name|result
parameter_list|,
name|boolean
name|doRefreshClassloader
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
block|{
comment|// We will download into fn-scoped subdirectories to avoid name collisions (we assume there
comment|// are no collisions within the same fn). That doesn't mean we download for every fn.
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Localizing "
operator|+
name|resources
operator|.
name|size
argument_list|()
operator|+
literal|" resources for "
operator|+
name|fqfn
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ResourceUri
name|resource
range|:
name|resources
control|)
block|{
name|URI
name|srcUri
init|=
name|ResourceDownloader
operator|.
name|createURI
argument_list|(
name|resource
operator|.
name|getUri
argument_list|()
argument_list|)
decl_stmt|;
name|ResourceType
name|rt
init|=
name|FunctionUtils
operator|.
name|getResourceType
argument_list|(
name|resource
operator|.
name|getResourceType
argument_list|()
argument_list|)
decl_stmt|;
name|localizeOneResource
argument_list|(
name|fqfn
argument_list|,
name|srcUri
argument_list|,
name|rt
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
name|recentlyLocalizedClasses
operator|.
name|add
argument_list|(
name|className
argument_list|)
expr_stmt|;
if|if
condition|(
name|doRefreshClassloader
condition|)
block|{
name|refreshClassloader
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|localizeOneResource
parameter_list|(
name|String
name|fqfn
parameter_list|,
name|URI
name|srcUri
parameter_list|,
name|ResourceType
name|rt
parameter_list|,
name|FnResources
name|result
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
block|{
name|RefCountedResource
name|rcr
init|=
name|localFiles
operator|.
name|get
argument_list|(
name|srcUri
argument_list|)
decl_stmt|;
if|if
condition|(
name|rcr
operator|!=
literal|null
operator|&&
name|rcr
operator|.
name|refCount
operator|>
literal|0
condition|)
block|{
name|logFilesUsed
argument_list|(
literal|"Reusing"
argument_list|,
name|fqfn
argument_list|,
name|srcUri
argument_list|,
name|rcr
argument_list|)
expr_stmt|;
operator|++
name|rcr
operator|.
name|refCount
expr_stmt|;
name|result
operator|.
name|addResources
argument_list|(
name|rcr
argument_list|)
expr_stmt|;
return|return;
block|}
name|rcr
operator|=
operator|new
name|RefCountedResource
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|URI
argument_list|>
name|localUris
init|=
name|resourceDownloader
operator|.
name|downloadExternal
argument_list|(
name|srcUri
argument_list|,
name|fqfn
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|localUris
operator|==
literal|null
operator|||
name|localUris
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Cannot download "
operator|+
name|srcUri
operator|+
literal|" for "
operator|+
name|fqfn
argument_list|)
expr_stmt|;
return|return;
block|}
name|rcr
operator|.
name|resources
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|URI
name|uri
range|:
name|localUris
control|)
block|{
comment|// Reuse the same type for all. Only Ivy can return more than one, probably all jars.
name|String
name|path
init|=
name|uri
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|rcr
operator|.
name|resources
operator|.
name|add
argument_list|(
operator|new
name|FunctionResource
argument_list|(
name|rt
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|rt
operator|==
name|ResourceType
operator|.
name|JAR
condition|)
block|{
name|recentlyLocalizedJars
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
operator|++
name|rcr
operator|.
name|refCount
expr_stmt|;
name|logFilesUsed
argument_list|(
literal|"Using"
argument_list|,
name|fqfn
argument_list|,
name|srcUri
argument_list|,
name|rcr
argument_list|)
expr_stmt|;
name|localFiles
operator|.
name|put
argument_list|(
name|srcUri
argument_list|,
name|rcr
argument_list|)
expr_stmt|;
name|result
operator|.
name|addResources
argument_list|(
name|rcr
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|logFilesUsed
parameter_list|(
name|String
name|what
parameter_list|,
name|String
name|fqfn
parameter_list|,
name|URI
name|srcUri
parameter_list|,
name|RefCountedResource
name|rcr
parameter_list|)
block|{
if|if
condition|(
operator|!
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
return|return;
name|String
name|desc
init|=
operator|(
name|rcr
operator|.
name|resources
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|rcr
operator|.
name|resources
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
else|:
operator|(
name|rcr
operator|.
name|resources
operator|.
name|size
argument_list|()
operator|+
literal|" files"
operator|)
operator|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|what
operator|+
literal|" files ["
operator|+
name|desc
operator|+
literal|"] for ["
operator|+
name|srcUri
operator|+
literal|"] resource for "
operator|+
name|fqfn
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|RefCountedResource
block|{
name|List
argument_list|<
name|FunctionResource
argument_list|>
name|resources
decl_stmt|;
name|int
name|refCount
init|=
literal|0
decl_stmt|;
block|}
specifier|private
specifier|static
class|class
name|FnResources
block|{
specifier|final
name|List
argument_list|<
name|FunctionResource
argument_list|>
name|localResources
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RefCountedResource
argument_list|>
name|originals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|addResources
parameter_list|(
name|RefCountedResource
name|rcr
parameter_list|)
block|{
name|localResources
operator|.
name|addAll
argument_list|(
name|rcr
operator|.
name|resources
argument_list|)
expr_stmt|;
name|originals
operator|.
name|add
argument_list|(
name|rcr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


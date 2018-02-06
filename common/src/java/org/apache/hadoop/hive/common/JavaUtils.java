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
name|common
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|PathFilter
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
comment|/**  * Collection of Java class loading/reflection related utilities common across  * Hive.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|JavaUtils
block|{
specifier|public
specifier|static
specifier|final
name|String
name|BASE_PREFIX
init|=
literal|"base"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DELTA_PREFIX
init|=
literal|"delta"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DELTA_DIGITS
init|=
literal|"%07d"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DELTA_DIGITS_LEN
init|=
literal|7
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STATEMENT_DIGITS
init|=
literal|"%04d"
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
name|JavaUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Method
name|SUN_MISC_UTIL_RELEASE
decl_stmt|;
static|static
block|{
if|if
condition|(
name|Closeable
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|URLClassLoader
operator|.
name|class
argument_list|)
condition|)
block|{
name|SUN_MISC_UTIL_RELEASE
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|Method
name|release
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"sun.misc.ClassLoaderUtil"
argument_list|)
decl_stmt|;
name|release
operator|=
name|clazz
operator|.
name|getMethod
argument_list|(
literal|"releaseLoader"
argument_list|,
name|URLClassLoader
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|SUN_MISC_UTIL_RELEASE
operator|=
name|release
expr_stmt|;
block|}
block|}
comment|/**    * Standard way of getting classloader in Hive code (outside of Hadoop).    *    * Uses the context loader to get access to classpaths to auxiliary and jars    * added with 'add jar' command. Falls back to current classloader.    *    * In Hadoop-related code, we use Configuration.getClassLoader().    */
specifier|public
specifier|static
name|ClassLoader
name|getClassLoader
parameter_list|()
block|{
name|ClassLoader
name|classLoader
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|classLoader
operator|==
literal|null
condition|)
block|{
name|classLoader
operator|=
name|JavaUtils
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
expr_stmt|;
block|}
return|return
name|classLoader
return|;
block|}
specifier|public
specifier|static
name|Class
name|loadClass
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
name|loadClass
argument_list|(
name|className
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Class
name|loadClass
parameter_list|(
name|String
name|className
parameter_list|,
name|boolean
name|init
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|,
name|init
argument_list|,
name|getClassLoader
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|closeClassLoadersTo
parameter_list|(
name|ClassLoader
name|current
parameter_list|,
name|ClassLoader
name|stop
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isValidHierarchy
argument_list|(
name|current
argument_list|,
name|stop
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
init|;
name|current
operator|!=
literal|null
operator|&&
name|current
operator|!=
name|stop
condition|;
name|current
operator|=
name|current
operator|.
name|getParent
argument_list|()
control|)
block|{
try|try
block|{
name|closeClassLoader
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to close class loader "
operator|+
name|current
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|current
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|// check before closing loaders, not to close app-classloader, etc. by mistake
specifier|private
specifier|static
name|boolean
name|isValidHierarchy
parameter_list|(
name|ClassLoader
name|current
parameter_list|,
name|ClassLoader
name|stop
parameter_list|)
block|{
if|if
condition|(
name|current
operator|==
literal|null
operator|||
name|stop
operator|==
literal|null
operator|||
name|current
operator|==
name|stop
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
init|;
name|current
operator|!=
literal|null
operator|&&
name|current
operator|!=
name|stop
condition|;
name|current
operator|=
name|current
operator|.
name|getParent
argument_list|()
control|)
block|{     }
return|return
name|current
operator|==
name|stop
return|;
block|}
comment|// best effort to close
comment|// see https://issues.apache.org/jira/browse/HIVE-3969 for detail
specifier|public
specifier|static
name|void
name|closeClassLoader
parameter_list|(
name|ClassLoader
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|loader
operator|instanceof
name|Closeable
condition|)
block|{
operator|(
operator|(
name|Closeable
operator|)
name|loader
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|SUN_MISC_UTIL_RELEASE
operator|!=
literal|null
operator|&&
name|loader
operator|instanceof
name|URLClassLoader
condition|)
block|{
name|PrintStream
name|outputStream
init|=
name|System
operator|.
name|out
decl_stmt|;
name|ByteArrayOutputStream
name|byteArrayOutputStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|newOutputStream
init|=
operator|new
name|PrintStream
argument_list|(
name|byteArrayOutputStream
argument_list|)
decl_stmt|;
try|try
block|{
comment|// SUN_MISC_UTIL_RELEASE.invoke prints to System.out
comment|// So we're changing the outputstream for that call,
comment|// and setting it back to original System.out when we're done
name|System
operator|.
name|setOut
argument_list|(
name|newOutputStream
argument_list|)
expr_stmt|;
name|SUN_MISC_UTIL_RELEASE
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|loader
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|byteArrayOutputStream
operator|.
name|toString
argument_list|(
literal|"UTF8"
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getTargetException
argument_list|()
operator|instanceof
name|IOException
condition|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|e
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
name|e
operator|.
name|getTargetException
argument_list|()
argument_list|)
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|System
operator|.
name|setOut
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|newOutputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Utility method for ACID to normalize logging info.  Matches    * org.apache.hadoop.hive.metastore.api.LockRequest#toString    */
specifier|public
specifier|static
name|String
name|lockIdToString
parameter_list|(
name|long
name|extLockId
parameter_list|)
block|{
return|return
literal|"lockid:"
operator|+
name|extLockId
return|;
block|}
comment|/**    * Utility method for ACID to normalize logging info.  Matches    * org.apache.hadoop.hive.metastore.api.LockResponse#toString    */
specifier|public
specifier|static
name|String
name|txnIdToString
parameter_list|(
name|long
name|txnId
parameter_list|)
block|{
return|return
literal|"txnid:"
operator|+
name|txnId
return|;
block|}
specifier|public
specifier|static
name|String
name|txnIdsToString
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|txnIds
parameter_list|)
block|{
return|return
literal|"Transactions requested to be aborted: "
operator|+
name|txnIds
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|JavaUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|Long
name|extractTxnId
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|String
name|fileName
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|fileName
operator|.
name|split
argument_list|(
literal|"_"
argument_list|,
literal|4
argument_list|)
decl_stmt|;
comment|// e.g. delta_0000001_0000001_0000 or base_0000022
if|if
condition|(
name|parts
operator|.
name|length
operator|<
literal|2
operator|||
operator|!
operator|(
name|DELTA_PREFIX
operator|.
name|equals
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
operator|||
name|BASE_PREFIX
operator|.
name|equals
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cannot extract transaction ID for a MM table: "
operator|+
name|file
operator|+
literal|" ("
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|parts
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|long
name|writeId
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|writeId
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cannot extract transaction ID for a MM table: "
operator|+
name|file
operator|+
literal|"; parsing "
operator|+
name|parts
index|[
literal|1
index|]
operator|+
literal|" got "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|writeId
return|;
block|}
specifier|public
specifier|static
class|class
name|IdPathFilter
implements|implements
name|PathFilter
block|{
specifier|private
name|String
name|baseDirName
decl_stmt|,
name|deltaDirName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isMatch
decl_stmt|,
name|isIgnoreTemp
decl_stmt|,
name|isDeltaPrefix
decl_stmt|;
specifier|public
name|IdPathFilter
parameter_list|(
name|long
name|writeId
parameter_list|,
name|int
name|stmtId
parameter_list|,
name|boolean
name|isMatch
parameter_list|)
block|{
name|this
argument_list|(
name|writeId
argument_list|,
name|stmtId
argument_list|,
name|isMatch
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IdPathFilter
parameter_list|(
name|long
name|writeId
parameter_list|,
name|int
name|stmtId
parameter_list|,
name|boolean
name|isMatch
parameter_list|,
name|boolean
name|isIgnoreTemp
parameter_list|)
block|{
name|String
name|deltaDirName
init|=
literal|null
decl_stmt|;
name|deltaDirName
operator|=
name|DELTA_PREFIX
operator|+
literal|"_"
operator|+
name|String
operator|.
name|format
argument_list|(
name|DELTA_DIGITS
argument_list|,
name|writeId
argument_list|)
operator|+
literal|"_"
operator|+
name|String
operator|.
name|format
argument_list|(
name|DELTA_DIGITS
argument_list|,
name|writeId
argument_list|)
operator|+
literal|"_"
expr_stmt|;
name|isDeltaPrefix
operator|=
operator|(
name|stmtId
operator|<
literal|0
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|isDeltaPrefix
condition|)
block|{
name|deltaDirName
operator|+=
name|String
operator|.
name|format
argument_list|(
name|STATEMENT_DIGITS
argument_list|,
name|stmtId
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|baseDirName
operator|=
name|BASE_PREFIX
operator|+
literal|"_"
operator|+
name|String
operator|.
name|format
argument_list|(
name|DELTA_DIGITS
argument_list|,
name|writeId
argument_list|)
expr_stmt|;
name|this
operator|.
name|deltaDirName
operator|=
name|deltaDirName
expr_stmt|;
name|this
operator|.
name|isMatch
operator|=
name|isMatch
expr_stmt|;
name|this
operator|.
name|isIgnoreTemp
operator|=
name|isIgnoreTemp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|String
name|name
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|baseDirName
argument_list|)
operator|||
operator|(
name|isDeltaPrefix
operator|&&
name|name
operator|.
name|startsWith
argument_list|(
name|deltaDirName
argument_list|)
operator|)
operator|||
operator|(
operator|!
name|isDeltaPrefix
operator|&&
name|name
operator|.
name|equals
argument_list|(
name|deltaDirName
argument_list|)
operator|)
condition|)
block|{
return|return
name|isMatch
return|;
block|}
if|if
condition|(
name|isIgnoreTemp
operator|&&
name|name
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|char
name|c
init|=
name|name
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'.'
operator|||
name|c
operator|==
literal|'_'
condition|)
return|return
literal|false
return|;
comment|// Regardless of isMatch, ignore this.
block|}
return|return
operator|!
name|isMatch
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|AnyIdDirFilter
implements|implements
name|PathFilter
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|String
name|name
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|startsWith
argument_list|(
name|DELTA_PREFIX
operator|+
literal|"_"
argument_list|)
condition|)
return|return
literal|false
return|;
name|String
name|idStr
init|=
name|name
operator|.
name|substring
argument_list|(
name|DELTA_PREFIX
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|DELTA_PREFIX
operator|.
name|length
argument_list|()
operator|+
literal|1
operator|+
name|DELTA_DIGITS_LEN
argument_list|)
decl_stmt|;
try|try
block|{
name|Long
operator|.
name|parseLong
argument_list|(
name|idStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit


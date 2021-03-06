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
name|shims
package|;
end_package

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
name|util
operator|.
name|VersionInfo
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
comment|/**  * ShimLoader.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ShimLoader
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
name|ShimLoader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP23VERSIONNAME
init|=
literal|"0.23"
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|HadoopShims
name|hadoopShims
decl_stmt|;
comment|/**    * The names of the classes for shimming Hadoop for each major version.    */
specifier|private
specifier|static
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|HADOOP_SHIM_CLASSES
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|HADOOP_SHIM_CLASSES
operator|.
name|put
argument_list|(
name|HADOOP23VERSIONNAME
argument_list|,
literal|"org.apache.hadoop.hive.shims.Hadoop23Shims"
argument_list|)
expr_stmt|;
block|}
comment|/**    * The names of the classes for shimming Hadoop's event counter    */
specifier|private
specifier|static
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|EVENT_COUNTER_SHIM_CLASSES
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|EVENT_COUNTER_SHIM_CLASSES
operator|.
name|put
argument_list|(
name|HADOOP23VERSIONNAME
argument_list|,
literal|"org.apache.hadoop.log.metrics"
operator|+
literal|".EventCounter"
argument_list|)
expr_stmt|;
block|}
comment|/**    * The names of the classes for shimming HadoopThriftAuthBridge    */
specifier|private
specifier|static
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|HADOOP_THRIFT_AUTH_BRIDGE_CLASSES
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|HADOOP_THRIFT_AUTH_BRIDGE_CLASSES
operator|.
name|put
argument_list|(
name|HADOOP23VERSIONNAME
argument_list|,
literal|"org.apache.hadoop.hive.metastore.security.HadoopThriftAuthBridge23"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|SCHEDULER_SHIM_CLASSE
init|=
literal|"org.apache.hadoop.hive.schshim.FairSchedulerShim"
decl_stmt|;
comment|/**    * Factory method to get an instance of HadoopShims based on the    * version of Hadoop on the classpath.    */
specifier|public
specifier|static
name|HadoopShims
name|getHadoopShims
parameter_list|()
block|{
if|if
condition|(
name|hadoopShims
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|ShimLoader
operator|.
name|class
init|)
block|{
if|if
condition|(
name|hadoopShims
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|hadoopShims
operator|=
name|loadShims
argument_list|(
name|HADOOP_SHIM_CLASSES
argument_list|,
name|HadoopShims
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error loading shims"
argument_list|,
name|t
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
block|}
block|}
return|return
name|hadoopShims
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|loadShims
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|classMap
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|xface
parameter_list|)
block|{
name|String
name|vers
init|=
name|getMajorVersion
argument_list|()
decl_stmt|;
name|String
name|className
init|=
name|classMap
operator|.
name|get
argument_list|(
name|vers
argument_list|)
decl_stmt|;
return|return
name|createShim
argument_list|(
name|className
argument_list|,
name|xface
argument_list|)
return|;
block|}
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|createShim
parameter_list|(
name|String
name|className
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|xface
parameter_list|)
block|{
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
name|className
argument_list|)
decl_stmt|;
return|return
name|xface
operator|.
name|cast
argument_list|(
name|clazz
operator|.
name|newInstance
argument_list|()
argument_list|)
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
name|RuntimeException
argument_list|(
literal|"Could not load shims in class "
operator|+
name|className
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Return the "major" version of Hadoop currently on the classpath.    * Releases in the 1.x and 2.x series are mapped to the appropriate    * 0.x release series, e.g. 1.x is mapped to "0.20S" and 2.x    * is mapped to "0.23".    */
specifier|public
specifier|static
name|String
name|getMajorVersion
parameter_list|()
block|{
name|String
name|vers
init|=
name|VersionInfo
operator|.
name|getVersion
argument_list|()
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|vers
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Illegal Hadoop Version: "
operator|+
name|vers
operator|+
literal|" (expected A.B.* format)"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
case|case
literal|2
case|:
case|case
literal|3
case|:
return|return
name|HADOOP23VERSIONNAME
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unrecognized Hadoop major version number: "
operator|+
name|vers
argument_list|)
throw|;
block|}
block|}
specifier|private
name|ShimLoader
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit


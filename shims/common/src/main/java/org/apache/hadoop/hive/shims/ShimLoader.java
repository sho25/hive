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
name|hive
operator|.
name|thrift
operator|.
name|HadoopThriftAuthBridge
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
name|apache
operator|.
name|log4j
operator|.
name|AppenderSkeleton
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
name|HadoopShims
name|hadoopShims
decl_stmt|;
specifier|private
specifier|static
name|JettyShims
name|jettyShims
decl_stmt|;
specifier|private
specifier|static
name|AppenderSkeleton
name|eventCounter
decl_stmt|;
specifier|private
specifier|static
name|HadoopThriftAuthBridge
name|hadoopThriftAuthBridge
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
literal|"0.20"
argument_list|,
literal|"org.apache.hadoop.hive.shims.Hadoop20Shims"
argument_list|)
expr_stmt|;
name|HADOOP_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.20S"
argument_list|,
literal|"org.apache.hadoop.hive.shims.Hadoop20SShims"
argument_list|)
expr_stmt|;
name|HADOOP_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.23"
argument_list|,
literal|"org.apache.hadoop.hive.shims.Hadoop23Shims"
argument_list|)
expr_stmt|;
block|}
comment|/**    * The names of the classes for shimming Jetty for each major version of    * Hadoop.    */
specifier|private
specifier|static
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|JETTY_SHIM_CLASSES
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
name|JETTY_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.20"
argument_list|,
literal|"org.apache.hadoop.hive.shims.Jetty20Shims"
argument_list|)
expr_stmt|;
name|JETTY_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.20S"
argument_list|,
literal|"org.apache.hadoop.hive.shims.Jetty20SShims"
argument_list|)
expr_stmt|;
name|JETTY_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.23"
argument_list|,
literal|"org.apache.hadoop.hive.shims.Jetty23Shims"
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
literal|"0.20"
argument_list|,
literal|"org.apache.hadoop.metrics.jvm.EventCounter"
argument_list|)
expr_stmt|;
name|EVENT_COUNTER_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.20S"
argument_list|,
literal|"org.apache.hadoop.log.metrics.EventCounter"
argument_list|)
expr_stmt|;
name|EVENT_COUNTER_SHIM_CLASSES
operator|.
name|put
argument_list|(
literal|"0.23"
argument_list|,
literal|"org.apache.hadoop.log.metrics.EventCounter"
argument_list|)
expr_stmt|;
block|}
comment|/**    * The names of the classes for shimming {@link HadoopThriftAuthBridge}    */
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
literal|"0.20"
argument_list|,
literal|"org.apache.hadoop.hive.thrift.HadoopThriftAuthBridge"
argument_list|)
expr_stmt|;
name|HADOOP_THRIFT_AUTH_BRIDGE_CLASSES
operator|.
name|put
argument_list|(
literal|"0.20S"
argument_list|,
literal|"org.apache.hadoop.hive.thrift.HadoopThriftAuthBridge20S"
argument_list|)
expr_stmt|;
name|HADOOP_THRIFT_AUTH_BRIDGE_CLASSES
operator|.
name|put
argument_list|(
literal|"0.23"
argument_list|,
literal|"org.apache.hadoop.hive.thrift.HadoopThriftAuthBridge23"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Factory method to get an instance of HadoopShims based on the    * version of Hadoop on the classpath.    */
specifier|public
specifier|static
specifier|synchronized
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
return|return
name|hadoopShims
return|;
block|}
comment|/**    * Factory method to get an instance of JettyShims based on the version    * of Hadoop on the classpath.    */
specifier|public
specifier|static
specifier|synchronized
name|JettyShims
name|getJettyShims
parameter_list|()
block|{
if|if
condition|(
name|jettyShims
operator|==
literal|null
condition|)
block|{
name|jettyShims
operator|=
name|loadShims
argument_list|(
name|JETTY_SHIM_CLASSES
argument_list|,
name|JettyShims
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
return|return
name|jettyShims
return|;
block|}
specifier|public
specifier|static
specifier|synchronized
name|AppenderSkeleton
name|getEventCounter
parameter_list|()
block|{
if|if
condition|(
name|eventCounter
operator|==
literal|null
condition|)
block|{
name|eventCounter
operator|=
name|loadShims
argument_list|(
name|EVENT_COUNTER_SHIM_CLASSES
argument_list|,
name|AppenderSkeleton
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
return|return
name|eventCounter
return|;
block|}
specifier|public
specifier|static
specifier|synchronized
name|HadoopThriftAuthBridge
name|getHadoopThriftAuthBridge
parameter_list|()
block|{
if|if
condition|(
name|hadoopThriftAuthBridge
operator|==
literal|null
condition|)
block|{
name|hadoopThriftAuthBridge
operator|=
name|loadShims
argument_list|(
name|HADOOP_THRIFT_AUTH_BRIDGE_CLASSES
argument_list|,
name|HadoopThriftAuthBridge
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
return|return
name|hadoopThriftAuthBridge
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
comment|/**    * Return the "major" version of Hadoop currently on the classpath.    * For releases in the 0.x series this is simply the first two    * components of the version, e.g. "0.20" or "0.23". Releases in    * the 1.x and 2.x series are mapped to the appropriate    * 0.x release series, e.g. 1.x is mapped to "0.20S" and 2.x    * is mapped to "0.23".    */
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
comment|// Special handling for Hadoop 1.x and 2.x
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
literal|0
case|:
break|break;
case|case
literal|1
case|:
return|return
literal|"0.20S"
return|;
case|case
literal|2
case|:
return|return
literal|"0.23"
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
name|String
name|majorVersion
init|=
name|parts
index|[
literal|0
index|]
operator|+
literal|"."
operator|+
name|parts
index|[
literal|1
index|]
decl_stmt|;
comment|// If we are running a security release, we won't have UnixUserGroupInformation
comment|// (removed by HADOOP-6299 when switching to JAAS for Login)
try|try
block|{
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.security.UnixUserGroupInformation"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnf
parameter_list|)
block|{
if|if
condition|(
literal|"0.20"
operator|.
name|equals
argument_list|(
name|majorVersion
argument_list|)
condition|)
block|{
name|majorVersion
operator|+=
literal|"S"
expr_stmt|;
block|}
block|}
return|return
name|majorVersion
return|;
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


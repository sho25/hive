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
name|ql
operator|.
name|exec
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|fs
operator|.
name|FileSystem
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
name|ql
operator|.
name|Context
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
name|DriverContext
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
name|Utilities
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
name|plan
operator|.
name|BaseWork
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
name|plan
operator|.
name|SparkWork
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
name|mapred
operator|.
name|JobConf
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
name|SparkConf
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
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|SparkClient
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
specifier|static
specifier|transient
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SparkClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SPARK_DEFAULT_CONF_FILE
init|=
literal|"spark-defaults.conf"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SPARK_DEFAULT_MASTER
init|=
literal|"local"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SAPRK_DEFAULT_APP_NAME
init|=
literal|"Hive on Spark"
decl_stmt|;
specifier|private
specifier|static
name|SparkClient
name|client
decl_stmt|;
specifier|public
specifier|static
specifier|synchronized
name|SparkClient
name|getInstance
parameter_list|(
name|Configuration
name|hiveConf
parameter_list|)
block|{
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
name|client
operator|=
operator|new
name|SparkClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
specifier|private
name|JavaSparkContext
name|sc
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|localJars
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|localFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|SparkClient
parameter_list|(
name|Configuration
name|hiveConf
parameter_list|)
block|{
name|sc
operator|=
operator|new
name|JavaSparkContext
argument_list|(
name|initiateSparkConf
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|SparkConf
name|initiateSparkConf
parameter_list|(
name|Configuration
name|hiveConf
parameter_list|)
block|{
name|SparkConf
name|sparkConf
init|=
operator|new
name|SparkConf
argument_list|()
decl_stmt|;
comment|// set default spark configurations.
name|sparkConf
operator|.
name|set
argument_list|(
literal|"spark.master"
argument_list|,
name|SPARK_DEFAULT_MASTER
argument_list|)
expr_stmt|;
name|sparkConf
operator|.
name|set
argument_list|(
literal|"spark.app.name"
argument_list|,
name|SAPRK_DEFAULT_APP_NAME
argument_list|)
expr_stmt|;
name|sparkConf
operator|.
name|set
argument_list|(
literal|"spark.serializer"
argument_list|,
literal|"org.apache.spark.serializer.KryoSerializer"
argument_list|)
expr_stmt|;
name|sparkConf
operator|.
name|set
argument_list|(
literal|"spark.default.parallelism"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
comment|// load properties from spark-defaults.conf.
name|InputStream
name|inputStream
init|=
literal|null
decl_stmt|;
try|try
block|{
name|inputStream
operator|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|SPARK_DEFAULT_CONF_FILE
argument_list|)
expr_stmt|;
if|if
condition|(
name|inputStream
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"loading spark properties from:"
operator|+
name|SPARK_DEFAULT_CONF_FILE
argument_list|)
expr_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|properties
operator|.
name|load
argument_list|(
name|inputStream
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|propertyName
range|:
name|properties
operator|.
name|stringPropertyNames
argument_list|()
control|)
block|{
if|if
condition|(
name|propertyName
operator|.
name|startsWith
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
name|String
name|value
init|=
name|properties
operator|.
name|getProperty
argument_list|(
name|propertyName
argument_list|)
decl_stmt|;
name|sparkConf
operator|.
name|set
argument_list|(
name|propertyName
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
name|propertyName
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"load spark configuration from %s (%s -> %s)."
argument_list|,
name|SPARK_DEFAULT_CONF_FILE
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
literal|"Failed to open spark configuration file:"
operator|+
name|SPARK_DEFAULT_CONF_FILE
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|inputStream
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|inputStream
operator|.
name|close
argument_list|()
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
name|debug
argument_list|(
literal|"Failed to close inputstream."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// load properties from hive configurations.
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iterator
init|=
name|hiveConf
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|propertyName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|propertyName
operator|.
name|startsWith
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|sparkConf
operator|.
name|set
argument_list|(
name|propertyName
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"load spark configuration from hive configuration (%s -> %s)."
argument_list|,
name|propertyName
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sparkConf
return|;
block|}
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|Context
name|ctx
init|=
name|driverContext
operator|.
name|getCtx
argument_list|()
decl_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|(
name|HiveConf
operator|)
name|ctx
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|refreshLocalResources
argument_list|(
name|sparkWork
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|// Create temporary scratch dir
name|Path
name|emptyScratchDir
decl_stmt|;
try|try
block|{
name|emptyScratchDir
operator|=
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|emptyScratchDir
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|emptyScratchDir
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
name|error
argument_list|(
literal|"Error launching map-reduce job"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|5
return|;
block|}
comment|// Generate Spark plan
name|SparkPlanGenerator
name|gen
init|=
operator|new
name|SparkPlanGenerator
argument_list|(
name|sc
argument_list|,
name|ctx
argument_list|,
name|jobConf
argument_list|,
name|emptyScratchDir
argument_list|)
decl_stmt|;
name|SparkPlan
name|plan
decl_stmt|;
try|try
block|{
name|plan
operator|=
name|gen
operator|.
name|generate
argument_list|(
name|sparkWork
argument_list|)
expr_stmt|;
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
literal|"Error generating Spark Plan"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|2
return|;
block|}
comment|// Execute generated plan.
comment|// TODO: we should catch any exception and return more meaningful error code.
name|plan
operator|.
name|execute
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|refreshLocalResources
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
comment|// add hive-exec jar
name|String
name|hiveJar
init|=
name|conf
operator|.
name|getJar
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|localJars
operator|.
name|contains
argument_list|(
name|hiveJar
argument_list|)
condition|)
block|{
name|localJars
operator|.
name|add
argument_list|(
name|hiveJar
argument_list|)
expr_stmt|;
name|sc
operator|.
name|addJar
argument_list|(
name|hiveJar
argument_list|)
expr_stmt|;
block|}
comment|// add aux jars
name|String
name|auxJars
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEAUXJARS
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|auxJars
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|auxJars
argument_list|)
condition|)
block|{
name|addJars
argument_list|(
name|auxJars
argument_list|)
expr_stmt|;
block|}
comment|// add added jars
name|String
name|addedJars
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|JAR
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|addedJars
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|addedJars
argument_list|)
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDJARS
argument_list|,
name|addedJars
argument_list|)
expr_stmt|;
name|addJars
argument_list|(
name|addedJars
argument_list|)
expr_stmt|;
block|}
comment|// add plugin module jars on demand
specifier|final
name|String
name|MR_JAR_PROPERTY
init|=
literal|"tmpjars"
decl_stmt|;
comment|// jobConf will hold all the configuration for hadoop, tez, and hive
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|setStrings
argument_list|(
name|MR_JAR_PROPERTY
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|sparkWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|work
operator|.
name|configureJobConf
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|newTmpJars
init|=
name|jobConf
operator|.
name|getStrings
argument_list|(
name|MR_JAR_PROPERTY
argument_list|)
decl_stmt|;
if|if
condition|(
name|newTmpJars
operator|!=
literal|null
operator|&&
name|newTmpJars
operator|.
name|length
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|String
name|tmpJar
range|:
name|newTmpJars
control|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|tmpJar
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|tmpJar
argument_list|)
operator|&&
operator|!
name|localJars
operator|.
name|contains
argument_list|(
name|tmpJar
argument_list|)
condition|)
block|{
name|localJars
operator|.
name|add
argument_list|(
name|tmpJar
argument_list|)
expr_stmt|;
name|sc
operator|.
name|addJar
argument_list|(
name|tmpJar
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|//add added files
name|String
name|addedFiles
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|FILE
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|addedFiles
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|addedFiles
argument_list|)
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDFILES
argument_list|,
name|addedFiles
argument_list|)
expr_stmt|;
name|addResources
argument_list|(
name|addedFiles
argument_list|)
expr_stmt|;
block|}
comment|// add added archives
name|String
name|addedArchives
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|ARCHIVE
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|addedArchives
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|addedArchives
argument_list|)
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDARCHIVES
argument_list|,
name|addedArchives
argument_list|)
expr_stmt|;
name|addResources
argument_list|(
name|addedArchives
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|addResources
parameter_list|(
name|String
name|addedFiles
parameter_list|)
block|{
for|for
control|(
name|String
name|addedFile
range|:
name|addedFiles
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|addedFile
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|addedFile
argument_list|)
operator|&&
operator|!
name|localFiles
operator|.
name|contains
argument_list|(
name|addedFile
argument_list|)
condition|)
block|{
name|localFiles
operator|.
name|add
argument_list|(
name|addedFile
argument_list|)
expr_stmt|;
name|sc
operator|.
name|addFile
argument_list|(
name|addedFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|addJars
parameter_list|(
name|String
name|addedJars
parameter_list|)
block|{
for|for
control|(
name|String
name|addedJar
range|:
name|addedJars
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
if|if
condition|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|addedJar
argument_list|)
operator|&&
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|addedJar
argument_list|)
operator|&&
operator|!
name|localJars
operator|.
name|contains
argument_list|(
name|addedJar
argument_list|)
condition|)
block|{
name|localJars
operator|.
name|add
argument_list|(
name|addedJar
argument_list|)
expr_stmt|;
name|sc
operator|.
name|addJar
argument_list|(
name|addedJar
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


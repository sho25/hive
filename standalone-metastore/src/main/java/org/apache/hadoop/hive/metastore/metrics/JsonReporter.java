begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Counter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Gauge
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Histogram
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Meter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricFilter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|MetricRegistry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|ScheduledReporter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|json
operator|.
name|MetricsModule
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonProcessingException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectWriter
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
name|java
operator|.
name|io
operator|.
name|BufferedWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
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
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|StandardCopyOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|FileAttribute
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFilePermission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|PosixFilePermissions
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
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

begin_comment
comment|/**  * A metrics reporter for Metrics that dumps metrics periodically into  * a file in JSON format.  */
end_comment

begin_class
specifier|public
class|class
name|JsonReporter
extends|extends
name|ScheduledReporter
block|{
comment|//
comment|// Implementation notes.
comment|//
comment|// 1. Since only local file systems are supported, there is no need to use Hadoop
comment|//    version of Path class.
comment|// 2. java.nio package provides modern implementation of file and directory operations
comment|//    which is better then the traditional java.io, so we are using it here.
comment|//    In particular, it supports atomic creation of temporary files with specified
comment|//    permissions in the specified directory. This also avoids various attacks possible
comment|//    when temp file name is generated first, followed by file creation.
comment|//    See http://www.oracle.com/technetwork/articles/javase/nio-139333.html for
comment|//    the description of NIO API and
comment|//    http://docs.oracle.com/javase/tutorial/essential/io/legacy.html for the
comment|//    description of interoperability between legacy IO api vs NIO API.
comment|// 3. To avoid race conditions with readers of the metrics file, the implementation
comment|//    dumps metrics to a temporary file in the same directory as the actual metrics
comment|//    file and then renames it to the destination. Since both are located on the same
comment|//    filesystem, this rename is likely to be atomic (as long as the underlying OS
comment|//    support atomic renames.
comment|//
comment|// NOTE: This reporter is very similar to
comment|//       org.apache.hadoop.hive.common.metrics.metrics2.JsonFileMetricsReporter.
comment|//       org.apache.hadoop.hive.metastore.metrics.JsonReporter.
comment|//       It would be good to unify the two.
comment|//
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
name|JsonReporter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|FileAttribute
argument_list|<
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
argument_list|>
name|FILE_ATTRS
init|=
name|PosixFilePermissions
operator|.
name|asFileAttribute
argument_list|(
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"rw-r--r--"
argument_list|)
argument_list|)
decl_stmt|;
comment|// Permissions for metric directory
specifier|private
specifier|static
specifier|final
name|FileAttribute
argument_list|<
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
argument_list|>
name|DIR_ATTRS
init|=
name|PosixFilePermissions
operator|.
name|asFileAttribute
argument_list|(
name|PosixFilePermissions
operator|.
name|fromString
argument_list|(
literal|"rwxr-xr-x"
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MetricRegistry
name|registry
decl_stmt|;
specifier|private
name|ObjectWriter
name|jsonWriter
decl_stmt|;
comment|// Location of JSON file
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
comment|// Directory where path resides
specifier|private
specifier|final
name|Path
name|metricsDir
decl_stmt|;
specifier|private
name|JsonReporter
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|,
name|String
name|name
parameter_list|,
name|MetricFilter
name|filter
parameter_list|,
name|TimeUnit
name|rateUnit
parameter_list|,
name|TimeUnit
name|durationUnit
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|registry
argument_list|,
name|name
argument_list|,
name|filter
argument_list|,
name|rateUnit
argument_list|,
name|durationUnit
argument_list|)
expr_stmt|;
name|String
name|pathString
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METRICS_JSON_FILE_LOCATION
argument_list|)
decl_stmt|;
name|path
operator|=
name|Paths
operator|.
name|get
argument_list|(
name|pathString
argument_list|)
operator|.
name|toAbsolutePath
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Reporting metrics to {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
comment|// We want to use metricsDir in the same directory as the destination file to support atomic
comment|// move of temp file to the destination metrics file
name|metricsDir
operator|=
name|path
operator|.
name|getParent
argument_list|()
expr_stmt|;
name|this
operator|.
name|registry
operator|=
name|registry
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|long
name|period
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
comment|// Create metrics directory if it is not present
if|if
condition|(
operator|!
name|metricsDir
operator|.
name|toFile
argument_list|()
operator|.
name|exists
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Metrics directory {} does not exist, creating one"
argument_list|,
name|metricsDir
argument_list|)
expr_stmt|;
try|try
block|{
comment|// createDirectories creates all non-existent parent directories
name|Files
operator|.
name|createDirectories
argument_list|(
name|metricsDir
argument_list|,
name|DIR_ATTRS
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
name|warn
argument_list|(
literal|"Failed to initialize JSON reporter: failed to create directory {}: {}"
argument_list|,
name|metricsDir
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|jsonWriter
operator|=
operator|new
name|ObjectMapper
argument_list|()
operator|.
name|registerModule
argument_list|(
operator|new
name|MetricsModule
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|writerWithDefaultPrettyPrinter
argument_list|()
expr_stmt|;
name|super
operator|.
name|start
argument_list|(
name|period
argument_list|,
name|unit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|report
parameter_list|(
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Gauge
argument_list|>
name|sortedMap
parameter_list|,
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Counter
argument_list|>
name|sortedMap1
parameter_list|,
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Histogram
argument_list|>
name|sortedMap2
parameter_list|,
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Meter
argument_list|>
name|sortedMap3
parameter_list|,
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Timer
argument_list|>
name|sortedMap4
parameter_list|)
block|{
name|String
name|json
decl_stmt|;
try|try
block|{
name|json
operator|=
name|jsonWriter
operator|.
name|writeValueAsString
argument_list|(
name|registry
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|JsonProcessingException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to convert json to string "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Metrics are first dumped to a temp file which is then renamed to the destination
name|Path
name|tmpFile
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tmpFile
operator|=
name|Files
operator|.
name|createTempFile
argument_list|(
name|metricsDir
argument_list|,
literal|"hmsmetrics"
argument_list|,
literal|"json"
argument_list|,
name|FILE_ATTRS
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
literal|"failed to create temp file for JSON metrics"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
comment|// This shouldn't ever happen
name|LOG
operator|.
name|error
argument_list|(
literal|"failed to create temp file for JSON metrics: no permissions"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// This shouldn't ever happen
name|LOG
operator|.
name|error
argument_list|(
literal|"failed to create temp file for JSON metrics: operartion not supported"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Use try .. finally to cleanup temp file if something goes wrong
try|try
block|{
comment|// Write json to the temp file
try|try
init|(
name|BufferedWriter
name|bw
init|=
operator|new
name|BufferedWriter
argument_list|(
operator|new
name|FileWriter
argument_list|(
name|tmpFile
operator|.
name|toFile
argument_list|()
argument_list|)
argument_list|)
init|)
block|{
name|bw
operator|.
name|write
argument_list|(
name|json
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
literal|"Unable to write to temp file {}"
operator|+
name|tmpFile
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Atomically move temp file to the destination file
try|try
block|{
name|Files
operator|.
name|move
argument_list|(
name|tmpFile
argument_list|,
name|path
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
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
literal|"Unable to rename temp file {} to {}"
argument_list|,
name|tmpFile
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception during rename"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// If something happened and we were not able to rename the temp file, attempt to remove it
if|if
condition|(
name|tmpFile
operator|.
name|toFile
argument_list|()
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// Attempt to delete temp file, if this fails, not much can be done about it.
try|try
block|{
name|Files
operator|.
name|delete
argument_list|(
name|tmpFile
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
literal|"failed to delete temporary metrics file "
operator|+
name|tmpFile
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|Builder
name|forRegistry
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|registry
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
specifier|final
name|MetricRegistry
name|registry
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|TimeUnit
name|rate
init|=
name|TimeUnit
operator|.
name|SECONDS
decl_stmt|;
specifier|private
name|TimeUnit
name|duration
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
decl_stmt|;
specifier|private
name|MetricFilter
name|filter
init|=
name|MetricFilter
operator|.
name|ALL
decl_stmt|;
specifier|private
name|Builder
parameter_list|(
name|MetricRegistry
name|registry
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|registry
operator|=
name|registry
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|Builder
name|convertRatesTo
parameter_list|(
name|TimeUnit
name|rate
parameter_list|)
block|{
name|this
operator|.
name|rate
operator|=
name|rate
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|convertDurationsTo
parameter_list|(
name|TimeUnit
name|duration
parameter_list|)
block|{
name|this
operator|.
name|duration
operator|=
name|duration
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|filter
parameter_list|(
name|MetricFilter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|JsonReporter
name|build
parameter_list|()
block|{
return|return
operator|new
name|JsonReporter
argument_list|(
name|registry
argument_list|,
literal|"json-reporter"
argument_list|,
name|filter
argument_list|,
name|rate
argument_list|,
name|duration
argument_list|,
name|conf
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


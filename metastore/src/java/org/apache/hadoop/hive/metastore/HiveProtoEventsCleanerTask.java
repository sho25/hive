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
name|metastore
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
name|lang3
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
name|FileStatus
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
name|fs
operator|.
name|PathFilter
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
name|security
operator|.
name|UserGroupInformation
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
name|yarn
operator|.
name|util
operator|.
name|SystemClock
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|LocalDate
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|LocalDateTime
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|ZoneOffset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatter
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
name|TimeUnit
import|;
end_import

begin_class
specifier|public
class|class
name|HiveProtoEventsCleanerTask
implements|implements
name|MetastoreTaskThread
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
name|HiveProtoEventsCleanerTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|eventsSubDirs
init|=
operator|new
name|String
index|[]
block|{
literal|"query_data"
block|,
literal|"dag_meta"
block|,
literal|"dag_data"
block|,
literal|"app_data"
block|}
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|eventsBasePaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|long
name|ttl
decl_stmt|;
specifier|private
specifier|static
name|String
name|expiredDatePtn
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SystemClock
name|clock
init|=
name|SystemClock
operator|.
name|getInstance
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|String
name|hiveEventsDir
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PROTO_EVENTS_BASE_PATH
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|hiveEventsDir
argument_list|)
condition|)
block|{
return|return;
block|}
name|Path
name|hiveEventsBasePath
init|=
operator|new
name|Path
argument_list|(
name|hiveEventsDir
argument_list|)
decl_stmt|;
name|Path
name|baseDir
init|=
name|hiveEventsBasePath
operator|.
name|getParent
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|subDir
range|:
name|eventsSubDirs
control|)
block|{
name|eventsBasePaths
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|baseDir
argument_list|,
name|subDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
assert|assert
operator|(
name|eventsBasePaths
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|hiveEventsBasePath
argument_list|)
operator|)
assert|;
name|ttl
operator|=
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PROTO_EVENTS_TTL
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
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|runFrequency
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_PROTO_EVENTS_CLEAN_FREQ
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// If Hive proto logging is not enabled, then nothing to be cleaned-up.
if|if
condition|(
name|eventsBasePaths
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// Expired date should be computed each time we run cleaner thread.
name|computeExpiredDatePtn
argument_list|(
name|ttl
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|basePath
range|:
name|eventsBasePaths
control|)
block|{
name|cleanupDir
argument_list|(
name|basePath
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Compute the expired date partition, using the underlying clock in UTC time.    */
specifier|private
specifier|static
name|void
name|computeExpiredDatePtn
parameter_list|(
name|long
name|ttl
parameter_list|)
block|{
comment|// Use UTC date to ensure reader date is same on all timezones.
name|LocalDate
name|expiredDate
init|=
name|LocalDateTime
operator|.
name|ofEpochSecond
argument_list|(
operator|(
name|clock
operator|.
name|getTime
argument_list|()
operator|-
name|ttl
operator|)
operator|/
literal|1000
argument_list|,
literal|0
argument_list|,
name|ZoneOffset
operator|.
name|UTC
argument_list|)
operator|.
name|toLocalDate
argument_list|()
decl_stmt|;
name|expiredDatePtn
operator|=
literal|"date="
operator|+
name|DateTimeFormatter
operator|.
name|ISO_LOCAL_DATE
operator|.
name|format
argument_list|(
name|expiredDate
argument_list|)
expr_stmt|;
block|}
comment|/**    * Path filters to include only expired date partitions based on TTL.    */
specifier|private
specifier|static
specifier|final
name|PathFilter
name|expiredDatePartitionsFilter
init|=
operator|new
name|PathFilter
argument_list|()
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
name|dirName
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|(
operator|(
name|dirName
operator|.
name|startsWith
argument_list|(
literal|"date="
argument_list|)
operator|)
operator|&&
operator|(
name|dirName
operator|.
name|compareTo
argument_list|(
name|expiredDatePtn
argument_list|)
operator|<=
literal|0
operator|)
operator|)
return|;
block|}
block|}
decl_stmt|;
comment|/**    * Finds the expired date partitioned events directory based on TTL and delete them.    */
specifier|private
name|void
name|cleanupDir
parameter_list|(
name|Path
name|eventsBasePath
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to delete expired proto events from "
operator|+
name|eventsBasePath
argument_list|)
expr_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|eventsBasePath
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|eventsBasePath
argument_list|)
condition|)
block|{
return|return;
block|}
name|FileStatus
index|[]
name|statuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|eventsBasePath
argument_list|,
name|expiredDatePartitionsFilter
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|dir
range|:
name|statuses
control|)
block|{
try|try
block|{
name|deleteDirByOwner
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleted expired proto events dir: "
operator|+
name|dir
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// Log error and continue to delete other expired dirs.
name|LOG
operator|.
name|error
argument_list|(
literal|"Error deleting expired proto events dir "
operator|+
name|dir
operator|.
name|getPath
argument_list|()
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
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
name|error
argument_list|(
literal|"Error while trying to delete expired proto events from "
operator|+
name|eventsBasePath
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Delete the events dir with it's owner as proxy user.    */
specifier|private
name|void
name|deleteDirByOwner
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|FileStatus
name|eventsDir
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|owner
init|=
name|eventsDir
operator|.
name|getOwner
argument_list|()
decl_stmt|;
if|if
condition|(
name|owner
operator|.
name|equals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|eventsDir
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting "
operator|+
name|eventsDir
operator|.
name|getPath
argument_list|()
operator|+
literal|" as user "
operator|+
name|owner
argument_list|)
expr_stmt|;
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|createProxyUser
argument_list|(
name|owner
argument_list|,
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
throws|throws
name|Exception
block|{
comment|// New FileSystem object to be obtained in user context for doAs flow.
try|try
init|(
name|FileSystem
name|doAsFs
init|=
name|FileSystem
operator|.
name|newInstance
argument_list|(
name|eventsDir
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
init|)
block|{
name|doAsFs
operator|.
name|delete
argument_list|(
name|eventsDir
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not delete "
operator|+
name|eventsDir
operator|.
name|getPath
argument_list|()
operator|+
literal|" for UGI: "
operator|+
name|ugi
argument_list|,
name|ie
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


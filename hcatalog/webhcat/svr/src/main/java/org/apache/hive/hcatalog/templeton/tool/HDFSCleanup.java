begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
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
name|Date
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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonStorage
operator|.
name|Type
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
comment|/**  * This does periodic cleanup  */
end_comment

begin_class
specifier|public
class|class
name|HDFSCleanup
extends|extends
name|Thread
block|{
specifier|protected
name|Configuration
name|appConf
decl_stmt|;
comment|// The interval to wake up and check the queue
specifier|public
specifier|static
specifier|final
name|String
name|HDFS_CLEANUP_INTERVAL
init|=
literal|"templeton.hdfs.cleanup.interval"
decl_stmt|;
comment|// 12 hours
comment|// The max age of a task allowed
specifier|public
specifier|static
specifier|final
name|String
name|HDFS_CLEANUP_MAX_AGE
init|=
literal|"templeton.hdfs.cleanup.maxage"
decl_stmt|;
comment|// ~ 1 week
specifier|protected
specifier|static
name|long
name|interval
init|=
literal|1000L
operator|*
literal|60L
operator|*
literal|60L
operator|*
literal|12L
decl_stmt|;
specifier|protected
specifier|static
name|long
name|maxage
init|=
literal|1000L
operator|*
literal|60L
operator|*
literal|60L
operator|*
literal|24L
operator|*
literal|7L
decl_stmt|;
comment|// The logger
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
name|HDFSCleanup
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Handle to cancel loop
specifier|private
name|boolean
name|stop
init|=
literal|false
decl_stmt|;
comment|// The instance
specifier|private
specifier|static
name|HDFSCleanup
name|thisclass
init|=
literal|null
decl_stmt|;
comment|// Whether the cycle is running
specifier|private
specifier|static
name|boolean
name|isRunning
init|=
literal|false
decl_stmt|;
comment|// The storage root
specifier|private
name|String
name|storage_root
decl_stmt|;
comment|/**    * Create a cleanup object.     */
specifier|private
name|HDFSCleanup
parameter_list|(
name|Configuration
name|appConf
parameter_list|)
block|{
name|this
operator|.
name|appConf
operator|=
name|appConf
expr_stmt|;
name|interval
operator|=
name|appConf
operator|.
name|getLong
argument_list|(
name|HDFS_CLEANUP_INTERVAL
argument_list|,
name|interval
argument_list|)
expr_stmt|;
name|maxage
operator|=
name|appConf
operator|.
name|getLong
argument_list|(
name|HDFS_CLEANUP_MAX_AGE
argument_list|,
name|maxage
argument_list|)
expr_stmt|;
name|storage_root
operator|=
name|appConf
operator|.
name|get
argument_list|(
name|TempletonStorage
operator|.
name|STORAGE_ROOT
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|HDFSCleanup
name|getInstance
parameter_list|(
name|Configuration
name|appConf
parameter_list|)
block|{
if|if
condition|(
name|thisclass
operator|!=
literal|null
condition|)
block|{
return|return
name|thisclass
return|;
block|}
name|thisclass
operator|=
operator|new
name|HDFSCleanup
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
return|return
name|thisclass
return|;
block|}
specifier|public
specifier|static
name|void
name|startInstance
parameter_list|(
name|Configuration
name|appConf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isRunning
condition|)
block|{
name|getInstance
argument_list|(
name|appConf
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Run the cleanup loop.    *    */
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
operator|!
name|stop
condition|)
block|{
try|try
block|{
comment|// Put each check in a separate try/catch, so if that particular
comment|// cycle fails, it'll try again on the next cycle.
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fs
operator|=
operator|new
name|Path
argument_list|(
name|storage_root
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
name|checkFiles
argument_list|(
name|fs
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
literal|"Cleanup cycle failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|fs
operator|.
name|close
argument_list|()
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
literal|"Closing file system failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|long
name|sleepMillis
init|=
call|(
name|long
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
name|interval
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Next execution: "
operator|+
operator|new
name|Date
argument_list|(
operator|new
name|Date
argument_list|()
operator|.
name|getTime
argument_list|()
operator|+
name|sleepMillis
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepMillis
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// If sleep fails, we should exit now before things get worse.
name|isRunning
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Cleanup failed: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|isRunning
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Loop through all the files, deleting any that are older than    * maxage.    *     * @param fs    * @throws IOException    */
specifier|private
name|void
name|checkFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|now
init|=
operator|new
name|Date
argument_list|()
operator|.
name|getTime
argument_list|()
decl_stmt|;
for|for
control|(
name|Type
name|type
range|:
name|Type
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
for|for
control|(
name|FileStatus
name|status
range|:
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|HDFSStorage
operator|.
name|getPath
argument_list|(
name|type
argument_list|,
name|storage_root
argument_list|)
argument_list|)
argument_list|)
control|)
block|{
if|if
condition|(
name|now
operator|-
name|status
operator|.
name|getModificationTime
argument_list|()
operator|>
name|maxage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Deleting "
operator|+
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Nothing to find for this type.
block|}
block|}
block|}
comment|// Handle to stop this process from the outside if needed.
specifier|public
name|void
name|exit
parameter_list|()
block|{
name|stop
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit


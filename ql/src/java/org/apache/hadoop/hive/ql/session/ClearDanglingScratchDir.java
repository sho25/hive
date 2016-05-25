begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|session
package|;
end_package

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
name|OptionBuilder
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
name|hadoop
operator|.
name|fs
operator|.
name|FileAlreadyExistsException
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
name|hdfs
operator|.
name|protocol
operator|.
name|AlreadyBeingCreatedException
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
name|common
operator|.
name|LogUtils
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
name|common
operator|.
name|LogUtils
operator|.
name|LogInitializationException
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
name|io
operator|.
name|IOUtils
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * A tool to remove dangling scratch directory. A scratch directory could be left behind  * in some cases, such as when vm restarts and leave no chance for Hive to run shutdown hook.  * The tool will test a scratch directory is use, if not, remove it.  * We rely on HDFS write lock for to detect if a scratch directory is in use:  * 1. A HDFS client open HDFS file ($scratchdir/inuse.lck) for write and only close  *    it at the time the session is closed  * 2. cleardanglingscratchDir can try to open $scratchdir/inuse.lck for write. If the  *    corresponding HiveCli/HiveServer2 is still running, we will get exception.  *    Otherwise, we know the session is dead  * 3. If the HiveCli/HiveServer2 dies without closing the HDFS file, NN will reclaim the  *    lease after 10 min, ie, the HDFS file hold by the dead HiveCli/HiveServer2 is writable  *    again after 10 min. Once it become writable, cleardanglingscratchDir will be able to  *    remove it  */
end_comment

begin_class
specifier|public
class|class
name|ClearDanglingScratchDir
block|{
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|LogUtils
operator|.
name|initHiveLog4j
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LogInitializationException
name|e
parameter_list|)
block|{     }
name|Options
name|opts
init|=
name|createOptions
argument_list|()
decl_stmt|;
name|CommandLine
name|cli
init|=
operator|new
name|GnuParser
argument_list|()
operator|.
name|parse
argument_list|(
name|opts
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|cli
operator|.
name|hasOption
argument_list|(
literal|'h'
argument_list|)
condition|)
block|{
name|HelpFormatter
name|formatter
init|=
operator|new
name|HelpFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|printHelp
argument_list|(
literal|"cleardanglingscratchdir"
operator|+
literal|" (clear scratch dir left behind by dead HiveCli or HiveServer2)"
argument_list|,
name|opts
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|dryRun
init|=
literal|false
decl_stmt|;
name|boolean
name|verbose
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|cli
operator|.
name|hasOption
argument_list|(
literal|"r"
argument_list|)
condition|)
block|{
name|dryRun
operator|=
literal|true
expr_stmt|;
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"dry-run mode on"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cli
operator|.
name|hasOption
argument_list|(
literal|"v"
argument_list|)
condition|)
block|{
name|verbose
operator|=
literal|true
expr_stmt|;
block|}
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|Path
name|rootHDFSDirPath
decl_stmt|;
if|if
condition|(
name|cli
operator|.
name|hasOption
argument_list|(
literal|"s"
argument_list|)
condition|)
block|{
name|rootHDFSDirPath
operator|=
operator|new
name|Path
argument_list|(
name|cli
operator|.
name|getOptionValue
argument_list|(
literal|"s"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rootHDFSDirPath
operator|=
operator|new
name|Path
argument_list|(
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
name|SCRATCHDIR
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|rootHDFSDirPath
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|userHDFSDirList
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|rootHDFSDirPath
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|scratchDirToRemove
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|userHDFSDir
range|:
name|userHDFSDirList
control|)
block|{
name|FileStatus
index|[]
name|scratchDirList
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|userHDFSDir
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|scratchDir
range|:
name|scratchDirList
control|)
block|{
name|Path
name|lockFilePath
init|=
operator|new
name|Path
argument_list|(
name|scratchDir
operator|.
name|getPath
argument_list|()
argument_list|,
name|SessionState
operator|.
name|LOCK_FILE_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|lockFilePath
argument_list|)
condition|)
block|{
name|String
name|message
init|=
literal|"Skipping "
operator|+
name|scratchDir
operator|.
name|getPath
argument_list|()
operator|+
literal|" since it does not contain "
operator|+
name|SessionState
operator|.
name|LOCK_FILE_NAME
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|logInfo
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|boolean
name|removable
init|=
literal|false
decl_stmt|;
name|boolean
name|inuse
init|=
literal|false
decl_stmt|;
try|try
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|fs
operator|.
name|append
argument_list|(
name|lockFilePath
argument_list|)
argument_list|)
expr_stmt|;
name|removable
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|eAppend
parameter_list|)
block|{
comment|// RemoteException with AlreadyBeingCreatedException will be thrown
comment|// if the file is currently held by a writer
if|if
condition|(
name|AlreadyBeingCreatedException
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|eAppend
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
name|inuse
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|UnsupportedOperationException
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|eAppend
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Append is not supported in the cluster, try to use create
try|try
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|fs
operator|.
name|create
argument_list|(
name|lockFilePath
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RemoteException
name|eCreate
parameter_list|)
block|{
if|if
condition|(
name|AlreadyBeingCreatedException
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|eCreate
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
comment|// If the file is held by a writer, will throw AlreadyBeingCreatedException
name|inuse
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Unexpected error:"
operator|+
name|eCreate
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|eCreateNormal
parameter_list|)
block|{
comment|// Otherwise, throw FileAlreadyExistsException, which means the file owner is
comment|// dead
name|removable
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Unexpected error:"
operator|+
name|eAppend
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|inuse
condition|)
block|{
comment|// Cannot open the lock file for writing, must be held by a live process
name|String
name|message
init|=
name|scratchDir
operator|.
name|getPath
argument_list|()
operator|+
literal|" is being used by live process"
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|logInfo
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|removable
condition|)
block|{
name|scratchDirToRemove
operator|.
name|add
argument_list|(
name|scratchDir
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|scratchDirToRemove
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Cannot find any scratch directory to clear"
argument_list|)
expr_stmt|;
return|return;
block|}
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Removing "
operator|+
name|scratchDirToRemove
operator|.
name|size
argument_list|()
operator|+
literal|" scratch directories"
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|scratchDir
range|:
name|scratchDirToRemove
control|)
block|{
if|if
condition|(
name|dryRun
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|scratchDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|succ
init|=
name|fs
operator|.
name|delete
argument_list|(
name|scratchDir
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|succ
condition|)
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Cannot remove "
operator|+
name|scratchDir
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|message
init|=
name|scratchDir
operator|+
literal|" removed"
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|logInfo
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|static
name|Options
name|createOptions
parameter_list|()
block|{
name|Options
name|result
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
comment|// add -r and --dry-run to generate list only
name|result
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|withLongOpt
argument_list|(
literal|"dry-run"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Generate a list of dangling scratch dir, printed on console"
argument_list|)
operator|.
name|create
argument_list|(
literal|'r'
argument_list|)
argument_list|)
expr_stmt|;
comment|// add -s and --scratchdir to specify a non-default scratch dir
name|result
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|withLongOpt
argument_list|(
literal|"scratchdir"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Specify a non-default location of the scratch dir"
argument_list|)
operator|.
name|hasArg
argument_list|()
operator|.
name|create
argument_list|(
literal|'s'
argument_list|)
argument_list|)
expr_stmt|;
comment|// add -v and --verbose to print verbose message
name|result
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|withLongOpt
argument_list|(
literal|"verbose"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"Print verbose message"
argument_list|)
operator|.
name|create
argument_list|(
literal|'v'
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|addOption
argument_list|(
name|OptionBuilder
operator|.
name|withLongOpt
argument_list|(
literal|"help"
argument_list|)
operator|.
name|withDescription
argument_list|(
literal|"print help message"
argument_list|)
operator|.
name|create
argument_list|(
literal|'h'
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit


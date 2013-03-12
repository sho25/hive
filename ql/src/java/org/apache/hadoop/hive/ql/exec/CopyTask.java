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
package|;
end_package

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
name|FileUtil
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
name|parse
operator|.
name|LoadSemanticAnalyzer
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
name|CopyWork
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
name|api
operator|.
name|StageType
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * CopyTask implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|CopyTask
extends|extends
name|Task
argument_list|<
name|CopyWork
argument_list|>
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
specifier|private
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
name|CopyTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|CopyTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|FileSystem
name|dstFs
init|=
literal|null
decl_stmt|;
name|Path
name|toPath
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Path
name|fromPath
init|=
operator|new
name|Path
argument_list|(
name|work
operator|.
name|getFromPath
argument_list|()
argument_list|)
decl_stmt|;
name|toPath
operator|=
operator|new
name|Path
argument_list|(
name|work
operator|.
name|getToPath
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Copying data from "
operator|+
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|" to "
operator|+
name|toPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|FileSystem
name|srcFs
init|=
name|fromPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|dstFs
operator|=
name|toPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|srcs
init|=
name|LoadSemanticAnalyzer
operator|.
name|matchFilesOrDir
argument_list|(
name|srcFs
argument_list|,
name|fromPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcs
operator|==
literal|null
operator|||
name|srcs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|isErrorOnSrcEmpty
argument_list|()
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"No files matching path: "
operator|+
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|3
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
if|if
condition|(
operator|!
name|dstFs
operator|.
name|mkdirs
argument_list|(
name|toPath
argument_list|)
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Cannot make target directory: "
operator|+
name|toPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|2
return|;
block|}
for|for
control|(
name|FileStatus
name|oneSrc
range|:
name|srcs
control|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Copying file: "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Copying file: "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|FileUtil
operator|.
name|copy
argument_list|(
name|srcFs
argument_list|,
name|oneSrc
operator|.
name|getPath
argument_list|()
argument_list|,
name|dstFs
argument_list|,
name|toPath
argument_list|,
literal|false
argument_list|,
comment|// delete
comment|// source
literal|true
argument_list|,
comment|// overwrite destination
name|conf
argument_list|)
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed to copy: '"
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"to: '"
operator|+
name|toPath
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|(
literal|1
operator|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|COPY
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"COPY"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|localizeMRTmpFilesImpl
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
comment|// copy task is only used by the load command and
comment|// does not use any map-reduce tmp files
comment|// we don't expect to enter this code path at all
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected call"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit


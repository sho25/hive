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
name|ql
operator|.
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|util
operator|.
name|Iterator
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
name|parse
operator|.
name|EximUtil
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
name|ReplicationSemanticAnalyzer
import|;
end_import

begin_class
specifier|public
class|class
name|ConstraintEventsIterator
implements|implements
name|Iterator
argument_list|<
name|FSConstraintEvent
argument_list|>
block|{
specifier|private
name|FileStatus
index|[]
name|dbDirs
decl_stmt|;
specifier|private
name|int
name|currentDbIndex
decl_stmt|;
specifier|private
name|FileStatus
index|[]
name|constraintFiles
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|currentConstraintIndex
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|path
decl_stmt|;
specifier|public
name|ConstraintEventsIterator
parameter_list|(
name|String
name|dumpDirectory
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|dumpDirectory
argument_list|)
expr_stmt|;
name|fs
operator|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|FileStatus
index|[]
name|listConstraintFilesInDBDir
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dbDir
parameter_list|)
block|{
try|try
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|dbDir
argument_list|,
name|ReplicationSemanticAnalyzer
operator|.
name|CONSTRAINTS_ROOT_DIR_NAME
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
return|return
operator|new
name|FileStatus
index|[]
block|{}
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
if|if
condition|(
name|dbDirs
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|dbDirs
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|path
argument_list|,
name|EximUtil
operator|.
name|getDirectoryFilter
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|currentDbIndex
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|dbDirs
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
name|currentConstraintIndex
operator|=
literal|0
expr_stmt|;
name|constraintFiles
operator|=
name|listConstraintFilesInDBDir
argument_list|(
name|fs
argument_list|,
name|dbDirs
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|(
name|currentDbIndex
operator|<
name|dbDirs
operator|.
name|length
operator|)
operator|&&
operator|(
name|currentConstraintIndex
operator|<
name|constraintFiles
operator|.
name|length
operator|)
condition|)
block|{
return|return
literal|true
return|;
block|}
while|while
condition|(
operator|(
name|currentDbIndex
operator|<
name|dbDirs
operator|.
name|length
operator|)
operator|&&
operator|(
name|currentConstraintIndex
operator|==
name|constraintFiles
operator|.
name|length
operator|)
condition|)
block|{
name|currentDbIndex
operator|++
expr_stmt|;
if|if
condition|(
name|currentDbIndex
operator|<
name|dbDirs
operator|.
name|length
condition|)
block|{
name|currentConstraintIndex
operator|=
literal|0
expr_stmt|;
name|constraintFiles
operator|=
name|listConstraintFilesInDBDir
argument_list|(
name|fs
argument_list|,
name|dbDirs
index|[
name|currentDbIndex
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|constraintFiles
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|constraintFiles
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|FSConstraintEvent
name|next
parameter_list|()
block|{
name|int
name|thisIndex
init|=
name|currentConstraintIndex
decl_stmt|;
name|currentConstraintIndex
operator|++
expr_stmt|;
return|return
operator|new
name|FSConstraintEvent
argument_list|(
name|constraintFiles
index|[
name|thisIndex
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit


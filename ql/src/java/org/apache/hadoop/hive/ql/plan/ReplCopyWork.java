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
name|plan
package|;
end_package

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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * Marker work for Replication - behaves similar to CopyWork, but maps to ReplCopyTask,  * which will have mechanics to list the files in source to write to the destination,  * instead of copying them, if specified, falling back to copying if needed.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Repl Copy"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|ReplCopyWork
extends|extends
name|CopyWork
block|{
comment|/**    * TODO : Refactor    *    * There is an upcoming patch that refactors this bit of code. Currently, the idea is the following:    *    * By default, ReplCopyWork will behave similarly to CopyWork, and simply copy    * along data from the source to destination.    * If the flag readSrcAsFilesList is set, changes the source behaviour of this CopyTask, and    * instead of copying explicit files, this will then fall back to a behaviour wherein an _files is    * read from the source, and the files specified by the _files are then copied to the destination.    *    * This allows us a lazy-copy-on-source and a pull-from destination semantic that we want    * to use from replication.    */
comment|// Governs remote-fetch-input behaviour
comment|// If set to true, we'll assume that the input has a _files file present which lists
comment|//   the actual input files to copy, and we'll pull each of those on read.
comment|// If set to false, it'll behave as a traditional CopyTask.
specifier|protected
name|boolean
name|readSrcAsFilesList
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|distCpDoAsUser
init|=
literal|null
decl_stmt|;
specifier|public
name|ReplCopyWork
parameter_list|(
specifier|final
name|Path
name|srcPath
parameter_list|,
specifier|final
name|Path
name|destPath
parameter_list|,
name|boolean
name|errorOnSrcEmpty
parameter_list|)
block|{
name|super
argument_list|(
name|srcPath
argument_list|,
name|destPath
argument_list|,
name|errorOnSrcEmpty
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setReadSrcAsFilesList
parameter_list|(
name|boolean
name|readSrcAsFilesList
parameter_list|)
block|{
name|this
operator|.
name|readSrcAsFilesList
operator|=
name|readSrcAsFilesList
expr_stmt|;
block|}
specifier|public
name|boolean
name|readSrcAsFilesList
parameter_list|()
block|{
return|return
name|this
operator|.
name|readSrcAsFilesList
return|;
block|}
specifier|public
name|void
name|setDistCpDoAsUser
parameter_list|(
name|String
name|distCpDoAsUser
parameter_list|)
block|{
name|this
operator|.
name|distCpDoAsUser
operator|=
name|distCpDoAsUser
expr_stmt|;
block|}
specifier|public
name|String
name|distCpDoAsUser
parameter_list|()
block|{
return|return
name|distCpDoAsUser
return|;
block|}
block|}
end_class

end_unit


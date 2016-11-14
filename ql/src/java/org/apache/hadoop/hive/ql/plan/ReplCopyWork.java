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
literal|"Copy for Replication"
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
specifier|protected
name|boolean
name|copyFiles
init|=
literal|true
decl_stmt|;
comment|// governs copy-or-list-files behaviour.
comment|// If set to true, behaves identically to a CopyWork
comment|// If set to false, ReplCopyTask does a file-list of the things to be copied instead, and puts them in a file called _files.
comment|// Default is set to mimic CopyTask, with the intent that any Replication code will explicitly flip this.
comment|/**    * TODO : Refactor    *    * There is an upcoming patch that refactors this bit of code. Currently, the idea is the following:    *    * By default, ReplCopyWork will behave similarly to CopyWork, and simply copy    * along data from the source to destination. If, however, listFilesOnOutput is set,    * then, instead of copying the individual files to the destination, it simply creates    * a file called _files on destination that contains the list of the original files    * that were intended to be copied. Thus, we do not actually copy the files at CopyWork    * time.    *    * The flip side of this behaviour happens when, instead, readListFromInput is set. This    * flag, if set, changes the source behaviour of this CopyTask, and instead of copying    * explicit files, this will then fall back to a behaviour wherein an _files is read from    * the source, and the files specified by the _files are then copied to the destination.    *    * This allows us a lazy-copy-on-source and a pull-from destination semantic that we want    * to use from replication.    *    * ==    *    * The refactor intent, however, is to simplify this, so that we have only 1 flag that we set,    * called isLazy. If isLazy is set, then this is the equivalent of the current listFilesOnOutput,    * and will generate a _files file.    *    * As to the input, we simply decide on whether to use the lazy mode or not depending on the    * presence of a _files file on the input. If we see a _files on the input, we simply expand it    * to copy as needed. If we do not, we copy as normal.    *    */
specifier|protected
name|boolean
name|listFilesOnOutput
init|=
literal|false
decl_stmt|;
comment|// governs copy-or-list-files behaviour
comment|// If set to true, it'll iterate over input files, and for each file in the input,
comment|//   it'll write out an additional line in a _files file in the output.
comment|// If set to false, it'll behave as a traditional CopyTask.
specifier|protected
name|boolean
name|readListFromInput
init|=
literal|false
decl_stmt|;
comment|// governs remote-fetch-input behaviour
comment|// If set to true, we'll assume that the input has a _files file present which lists
comment|//   the actual input files to copy, and we'll pull each of those on read.
comment|// If set to false, it'll behave as a traditional CopyTask.
specifier|public
name|ReplCopyWork
parameter_list|()
block|{   }
specifier|public
name|ReplCopyWork
parameter_list|(
specifier|final
name|Path
name|fromPath
parameter_list|,
specifier|final
name|Path
name|toPath
parameter_list|)
block|{
name|super
argument_list|(
name|fromPath
argument_list|,
name|toPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplCopyWork
parameter_list|(
specifier|final
name|Path
name|fromPath
parameter_list|,
specifier|final
name|Path
name|toPath
parameter_list|,
name|boolean
name|errorOnSrcEmpty
parameter_list|)
block|{
name|super
argument_list|(
name|fromPath
argument_list|,
name|toPath
argument_list|,
name|errorOnSrcEmpty
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setListFilesOnOutputBehaviour
parameter_list|(
name|boolean
name|listFilesOnOutput
parameter_list|)
block|{
name|this
operator|.
name|listFilesOnOutput
operator|=
name|listFilesOnOutput
expr_stmt|;
block|}
specifier|public
name|boolean
name|getListFilesOnOutputBehaviour
parameter_list|()
block|{
return|return
name|this
operator|.
name|listFilesOnOutput
return|;
block|}
specifier|public
name|void
name|setReadListFromInput
parameter_list|(
name|boolean
name|readListFromInput
parameter_list|)
block|{
name|this
operator|.
name|readListFromInput
operator|=
name|readListFromInput
expr_stmt|;
block|}
specifier|public
name|boolean
name|getReadListFromInput
parameter_list|()
block|{
return|return
name|this
operator|.
name|readListFromInput
return|;
block|}
comment|// specialization of getListFilesOnOutputBehaviour, with a filestatus arg
comment|// we can default to the default getListFilesOnOutputBehaviour behaviour,
comment|// or, we can do additional pattern matching to decide that certain files
comment|// should not be listed, and copied instead, _metadata files, for instance.
comment|// Currently, we use this to skip _metadata files, but we might decide that
comment|// this is not the right place for it later on.
specifier|public
name|boolean
name|getListFilesOnOutputBehaviour
parameter_list|(
name|FileStatus
name|f
parameter_list|)
block|{
if|if
condition|(
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"_metadata"
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
comment|// always copy _metadata files
block|}
return|return
name|this
operator|.
name|listFilesOnOutput
return|;
block|}
block|}
end_class

end_unit


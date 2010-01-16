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
name|Serializable
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
name|exec
operator|.
name|Task
import|;
end_import

begin_comment
comment|/**  * Conditional task resolution interface. This is invoked at run time to get the task to invoke.   * Developers can plug in their own resolvers  */
end_comment

begin_class
specifier|public
class|class
name|ConditionalResolverMergeFiles
implements|implements
name|ConditionalResolver
implements|,
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
specifier|public
name|ConditionalResolverMergeFiles
parameter_list|()
block|{     }
specifier|public
specifier|static
class|class
name|ConditionalResolverMergeFilesCtx
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
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
decl_stmt|;
specifier|private
name|String
name|dir
decl_stmt|;
specifier|public
name|ConditionalResolverMergeFilesCtx
parameter_list|()
block|{           }
comment|/**      * @param dir      */
specifier|public
name|ConditionalResolverMergeFilesCtx
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
parameter_list|,
name|String
name|dir
parameter_list|)
block|{
name|this
operator|.
name|listTasks
operator|=
name|listTasks
expr_stmt|;
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
block|}
comment|/**      * @return the dir      */
specifier|public
name|String
name|getDir
parameter_list|()
block|{
return|return
name|dir
return|;
block|}
comment|/**      * @param dir the dir to set      */
specifier|public
name|void
name|setDir
parameter_list|(
name|String
name|dir
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
block|}
comment|/**      * @return the listTasks      */
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getListTasks
parameter_list|()
block|{
return|return
name|listTasks
return|;
block|}
comment|/**      * @param listTasks the listTasks to set      */
specifier|public
name|void
name|setListTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTasks
parameter_list|)
block|{
name|this
operator|.
name|listTasks
operator|=
name|listTasks
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getTasks
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Object
name|objCtx
parameter_list|)
block|{
name|ConditionalResolverMergeFilesCtx
name|ctx
init|=
operator|(
name|ConditionalResolverMergeFilesCtx
operator|)
name|objCtx
decl_stmt|;
name|String
name|dirName
init|=
name|ctx
operator|.
name|getDir
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|resTsks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// check if a map-reduce job is needed to merge the files
comment|// If the current size is smaller than the target, merge
name|long
name|trgtSize
init|=
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEMAPFILESSIZE
argument_list|)
decl_stmt|;
name|long
name|avgConditionSize
init|=
name|conf
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMERGEMAPFILESAVGSIZE
argument_list|)
decl_stmt|;
name|trgtSize
operator|=
name|trgtSize
operator|>
name|avgConditionSize
condition|?
name|trgtSize
else|:
name|avgConditionSize
expr_stmt|;
try|try
block|{
comment|// If the input file does not exist, replace it by a empty file
name|Path
name|dirPath
init|=
operator|new
name|Path
argument_list|(
name|dirName
argument_list|)
decl_stmt|;
name|FileSystem
name|inpFs
init|=
name|dirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|inpFs
operator|.
name|exists
argument_list|(
name|dirPath
argument_list|)
condition|)
block|{
name|FileStatus
index|[]
name|fStats
init|=
name|inpFs
operator|.
name|listStatus
argument_list|(
name|dirPath
argument_list|)
decl_stmt|;
name|long
name|totalSz
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|fStat
range|:
name|fStats
control|)
name|totalSz
operator|+=
name|fStat
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|long
name|currAvgSz
init|=
name|totalSz
operator|/
name|fStats
operator|.
name|length
decl_stmt|;
if|if
condition|(
operator|(
name|currAvgSz
operator|<
name|avgConditionSize
operator|)
operator|&&
operator|(
name|fStats
operator|.
name|length
operator|>
literal|1
operator|)
condition|)
block|{
comment|// also set the number of reducers
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
init|=
name|ctx
operator|.
name|getListTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|mapredWork
name|work
init|=
operator|(
name|mapredWork
operator|)
name|tsk
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|int
name|maxReducers
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
argument_list|)
decl_stmt|;
name|int
name|reducers
init|=
call|(
name|int
call|)
argument_list|(
operator|(
name|totalSz
operator|+
name|trgtSize
operator|-
literal|1
operator|)
operator|/
name|trgtSize
argument_list|)
decl_stmt|;
name|reducers
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|reducers
argument_list|)
expr_stmt|;
name|reducers
operator|=
name|Math
operator|.
name|min
argument_list|(
name|maxReducers
argument_list|,
name|reducers
argument_list|)
expr_stmt|;
name|work
operator|.
name|setNumReduceTasks
argument_list|(
name|reducers
argument_list|)
expr_stmt|;
name|resTsks
operator|.
name|add
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
return|return
name|resTsks
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|resTsks
operator|.
name|add
argument_list|(
name|ctx
operator|.
name|getListTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|resTsks
return|;
block|}
block|}
end_class

end_unit


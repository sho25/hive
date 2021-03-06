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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
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
name|common
operator|.
name|FileUtils
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

begin_comment
comment|/**  * ConditionalResolverSkewJoin.  *  */
end_comment

begin_class
specifier|public
class|class
name|ConditionalResolverCommonJoin
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
name|ConditionalResolverCommonJoin
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * ConditionalResolverSkewJoinCtx.    *    */
specifier|public
specifier|static
class|class
name|ConditionalResolverCommonJoinCtx
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
name|HashMap
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|taskToAliases
decl_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|aliasToKnownSize
decl_stmt|;
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|commonJoinTask
decl_stmt|;
specifier|private
name|Path
name|localTmpDir
decl_stmt|;
specifier|private
name|Path
name|hdfsTmpDir
decl_stmt|;
specifier|public
name|ConditionalResolverCommonJoinCtx
parameter_list|()
block|{     }
specifier|public
name|HashMap
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|getTaskToAliases
parameter_list|()
block|{
return|return
name|taskToAliases
return|;
block|}
specifier|public
name|void
name|setTaskToAliases
parameter_list|(
name|HashMap
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|taskToAliases
parameter_list|)
block|{
name|this
operator|.
name|taskToAliases
operator|=
name|taskToAliases
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
argument_list|>
name|getCommonJoinTask
parameter_list|()
block|{
return|return
name|commonJoinTask
return|;
block|}
specifier|public
name|void
name|setCommonJoinTask
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|commonJoinTask
parameter_list|)
block|{
name|this
operator|.
name|commonJoinTask
operator|=
name|commonJoinTask
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|getAliasToKnownSize
parameter_list|()
block|{
return|return
name|aliasToKnownSize
operator|==
literal|null
condition|?
name|aliasToKnownSize
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
else|:
name|aliasToKnownSize
return|;
block|}
specifier|public
name|void
name|setAliasToKnownSize
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|aliasToKnownSize
parameter_list|)
block|{
name|this
operator|.
name|aliasToKnownSize
operator|=
name|aliasToKnownSize
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getPathToAliases
parameter_list|()
block|{
return|return
name|pathToAliases
return|;
block|}
specifier|public
name|void
name|setPathToAliases
parameter_list|(
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|)
block|{
name|this
operator|.
name|pathToAliases
operator|=
name|pathToAliases
expr_stmt|;
block|}
specifier|public
name|Path
name|getLocalTmpDir
parameter_list|()
block|{
return|return
name|localTmpDir
return|;
block|}
specifier|public
name|void
name|setLocalTmpDir
parameter_list|(
name|Path
name|localTmpDir
parameter_list|)
block|{
name|this
operator|.
name|localTmpDir
operator|=
name|localTmpDir
expr_stmt|;
block|}
specifier|public
name|Path
name|getHdfsTmpDir
parameter_list|()
block|{
return|return
name|hdfsTmpDir
return|;
block|}
specifier|public
name|void
name|setHdfsTmpDir
parameter_list|(
name|Path
name|hdfsTmpDir
parameter_list|)
block|{
name|this
operator|.
name|hdfsTmpDir
operator|=
name|hdfsTmpDir
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ConditionalResolverCommonJoinCtx
name|clone
parameter_list|()
block|{
name|ConditionalResolverCommonJoinCtx
name|ctx
init|=
operator|new
name|ConditionalResolverCommonJoinCtx
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|setTaskToAliases
argument_list|(
name|taskToAliases
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCommonJoinTask
argument_list|(
name|commonJoinTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setPathToAliases
argument_list|(
name|pathToAliases
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setHdfsTmpDir
argument_list|(
name|hdfsTmpDir
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setLocalTmpDir
argument_list|(
name|localTmpDir
argument_list|)
expr_stmt|;
comment|// if any of join participants is from other MR, it has alias like '[pos:]$INTNAME'
comment|// which of size should be caculated for each resolver.
name|ctx
operator|.
name|setAliasToKnownSize
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|(
name|aliasToKnownSize
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ctx
return|;
block|}
block|}
specifier|public
name|ConditionalResolverCommonJoin
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
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
name|ConditionalResolverCommonJoinCtx
name|ctx
init|=
operator|(
operator|(
name|ConditionalResolverCommonJoinCtx
operator|)
name|objCtx
operator|)
operator|.
name|clone
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
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
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// get aliasToPath and pass it to the heuristic
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
name|resolveDriverAlias
argument_list|(
name|ctx
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|==
literal|null
condition|)
block|{
comment|// run common join task
name|resTsks
operator|.
name|add
argument_list|(
name|ctx
operator|.
name|getCommonJoinTask
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// run the map join task, set task tag
if|if
condition|(
name|task
operator|.
name|getBackupTask
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|task
operator|.
name|getBackupTask
argument_list|()
operator|.
name|setTaskTag
argument_list|(
name|Task
operator|.
name|BACKUP_COMMON_JOIN
argument_list|)
expr_stmt|;
block|}
name|resTsks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
return|return
name|resTsks
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|resolveDriverAlias
parameter_list|(
name|ConditionalResolverCommonJoinCtx
name|ctx
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
try|try
block|{
name|resolveUnknownSizes
argument_list|(
name|ctx
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|resolveMapJoinTask
argument_list|(
name|ctx
argument_list|,
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to resolve driver alias by exception.. Falling back to common join"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|protected
name|Task
argument_list|<
name|?
argument_list|>
name|resolveMapJoinTask
parameter_list|(
name|ConditionalResolverCommonJoinCtx
name|ctx
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|participants
init|=
name|getParticipants
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|aliasToKnownSize
init|=
name|ctx
operator|.
name|getAliasToKnownSize
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|taskToAliases
init|=
name|ctx
operator|.
name|getTaskToAliases
argument_list|()
decl_stmt|;
name|long
name|threshold
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESMALLTABLESFILESIZE
argument_list|)
decl_stmt|;
name|Long
name|bigTableSize
init|=
literal|null
decl_stmt|;
name|Long
name|smallTablesSize
init|=
literal|null
decl_stmt|;
name|Map
operator|.
name|Entry
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|nextTask
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|taskToAliases
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|long
name|sumOfOthers
init|=
name|Utilities
operator|.
name|sumOfExcept
argument_list|(
name|aliasToKnownSize
argument_list|,
name|participants
argument_list|,
name|aliases
argument_list|)
decl_stmt|;
if|if
condition|(
name|sumOfOthers
argument_list|<
literal|0
operator|||
name|sumOfOthers
argument_list|>
name|threshold
condition|)
block|{
continue|continue;
block|}
comment|// at most one alias is unknown. we can safely regard it as a big alias
name|long
name|aliasSize
init|=
name|Utilities
operator|.
name|sumOf
argument_list|(
name|aliasToKnownSize
argument_list|,
name|aliases
argument_list|)
decl_stmt|;
if|if
condition|(
name|bigTableSize
operator|==
literal|null
operator|||
name|aliasSize
operator|>
name|bigTableSize
condition|)
block|{
name|nextTask
operator|=
name|entry
expr_stmt|;
name|bigTableSize
operator|=
name|aliasSize
expr_stmt|;
name|smallTablesSize
operator|=
name|sumOfOthers
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nextTask
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Driver alias is "
operator|+
name|nextTask
operator|.
name|getValue
argument_list|()
operator|+
literal|" with size "
operator|+
name|bigTableSize
operator|+
literal|" (total size of others : "
operator|+
name|smallTablesSize
operator|+
literal|", threshold : "
operator|+
name|threshold
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
name|nextTask
operator|.
name|getKey
argument_list|()
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to resolve driver alias (threshold : "
operator|+
name|threshold
operator|+
literal|", length mapping : "
operator|+
name|aliasToKnownSize
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getParticipants
parameter_list|(
name|ConditionalResolverCommonJoinCtx
name|ctx
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|participants
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|aliases
range|:
name|ctx
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|participants
operator|.
name|addAll
argument_list|(
name|aliases
argument_list|)
expr_stmt|;
block|}
return|return
name|participants
return|;
block|}
specifier|protected
name|void
name|resolveUnknownSizes
parameter_list|(
name|ConditionalResolverCommonJoinCtx
name|ctx
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|getParticipants
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|aliasToKnownSize
init|=
name|ctx
operator|.
name|getAliasToKnownSize
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
init|=
name|ctx
operator|.
name|getPathToAliases
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Path
argument_list|>
name|unknownPaths
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|pathToAliases
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|alias
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
name|aliases
operator|.
name|contains
argument_list|(
name|alias
argument_list|)
operator|&&
operator|!
name|aliasToKnownSize
operator|.
name|containsKey
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|unknownPaths
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
name|Path
name|hdfsTmpDir
init|=
name|ctx
operator|.
name|getHdfsTmpDir
argument_list|()
decl_stmt|;
name|Path
name|localTmpDir
init|=
name|ctx
operator|.
name|getLocalTmpDir
argument_list|()
decl_stmt|;
comment|// need to compute the input size at runtime, and select the biggest as
comment|// the big table.
for|for
control|(
name|Path
name|path
range|:
name|unknownPaths
control|)
block|{
comment|// this path is intermediate data
if|if
condition|(
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|path
argument_list|,
name|hdfsTmpDir
argument_list|)
operator|||
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|path
argument_list|,
name|localTmpDir
argument_list|)
condition|)
block|{
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|long
name|fileSize
init|=
name|fs
operator|.
name|getContentSummary
argument_list|(
name|path
argument_list|)
operator|.
name|getLength
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|pathToAliases
operator|.
name|get
argument_list|(
name|path
argument_list|)
control|)
block|{
name|Long
name|length
init|=
name|aliasToKnownSize
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|length
operator|==
literal|null
condition|)
block|{
name|aliasToKnownSize
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|fileSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit


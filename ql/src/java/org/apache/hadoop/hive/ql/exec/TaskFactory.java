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
name|mr
operator|.
name|MapRedTask
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
name|mr
operator|.
name|MapredLocalTask
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
name|spark
operator|.
name|SparkTask
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
name|tez
operator|.
name|TezTask
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
name|index
operator|.
name|IndexMetadataChangeTask
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
name|index
operator|.
name|IndexMetadataChangeWork
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
name|io
operator|.
name|merge
operator|.
name|MergeFileTask
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
name|io
operator|.
name|merge
operator|.
name|MergeFileWork
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
name|io
operator|.
name|rcfile
operator|.
name|stats
operator|.
name|PartialScanTask
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
name|io
operator|.
name|rcfile
operator|.
name|stats
operator|.
name|PartialScanWork
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
name|ColumnStatsUpdateWork
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
name|ColumnStatsWork
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
name|ConditionalWork
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
name|DDLWork
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
name|DependencyCollectionWork
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
name|ExplainSQRewriteWork
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
name|ExplainWork
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
name|FetchWork
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
name|FunctionWork
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
name|MapredLocalWork
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
name|MapredWork
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
name|MoveWork
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
name|SparkWork
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
name|StatsNoJobWork
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
name|StatsWork
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
name|TezWork
import|;
end_import

begin_comment
comment|/**  * TaskFactory implementation.  **/
end_comment

begin_class
specifier|public
specifier|final
class|class
name|TaskFactory
block|{
comment|/**    * taskTuple.    *    * @param<T>    */
specifier|public
specifier|static
specifier|final
class|class
name|TaskTuple
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
block|{
specifier|public
name|Class
argument_list|<
name|T
argument_list|>
name|workClass
decl_stmt|;
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Task
argument_list|<
name|T
argument_list|>
argument_list|>
name|taskClass
decl_stmt|;
specifier|public
name|TaskTuple
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|workClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Task
argument_list|<
name|T
argument_list|>
argument_list|>
name|taskClass
parameter_list|)
block|{
name|this
operator|.
name|workClass
operator|=
name|workClass
expr_stmt|;
name|this
operator|.
name|taskClass
operator|=
name|taskClass
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|TaskTuple
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|taskvec
decl_stmt|;
static|static
block|{
name|taskvec
operator|=
operator|new
name|ArrayList
argument_list|<
name|TaskTuple
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|MoveWork
argument_list|>
argument_list|(
name|MoveWork
operator|.
name|class
argument_list|,
name|MoveTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|FetchWork
argument_list|>
argument_list|(
name|FetchWork
operator|.
name|class
argument_list|,
name|FetchTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|CopyWork
argument_list|>
argument_list|(
name|CopyWork
operator|.
name|class
argument_list|,
name|CopyTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|DDLWork
argument_list|>
argument_list|(
name|DDLWork
operator|.
name|class
argument_list|,
name|DDLTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|FunctionWork
argument_list|>
argument_list|(
name|FunctionWork
operator|.
name|class
argument_list|,
name|FunctionTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|ExplainWork
argument_list|>
argument_list|(
name|ExplainWork
operator|.
name|class
argument_list|,
name|ExplainTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|ExplainSQRewriteWork
argument_list|>
argument_list|(
name|ExplainSQRewriteWork
operator|.
name|class
argument_list|,
name|ExplainSQRewriteTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|ConditionalWork
argument_list|>
argument_list|(
name|ConditionalWork
operator|.
name|class
argument_list|,
name|ConditionalTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|MapredWork
argument_list|>
argument_list|(
name|MapredWork
operator|.
name|class
argument_list|,
name|MapRedTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|MapredLocalWork
argument_list|>
argument_list|(
name|MapredLocalWork
operator|.
name|class
argument_list|,
name|MapredLocalTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|StatsWork
argument_list|>
argument_list|(
name|StatsWork
operator|.
name|class
argument_list|,
name|StatsTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|StatsNoJobWork
argument_list|>
argument_list|(
name|StatsNoJobWork
operator|.
name|class
argument_list|,
name|StatsNoJobTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|ColumnStatsWork
argument_list|>
argument_list|(
name|ColumnStatsWork
operator|.
name|class
argument_list|,
name|ColumnStatsTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|ColumnStatsUpdateWork
argument_list|>
argument_list|(
name|ColumnStatsUpdateWork
operator|.
name|class
argument_list|,
name|ColumnStatsUpdateTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|MergeFileWork
argument_list|>
argument_list|(
name|MergeFileWork
operator|.
name|class
argument_list|,
name|MergeFileTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|DependencyCollectionWork
argument_list|>
argument_list|(
name|DependencyCollectionWork
operator|.
name|class
argument_list|,
name|DependencyCollectionTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|ImportCommitWork
argument_list|>
argument_list|(
name|ImportCommitWork
operator|.
name|class
argument_list|,
name|ImportCommitTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|PartialScanWork
argument_list|>
argument_list|(
name|PartialScanWork
operator|.
name|class
argument_list|,
name|PartialScanTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|IndexMetadataChangeWork
argument_list|>
argument_list|(
name|IndexMetadataChangeWork
operator|.
name|class
argument_list|,
name|IndexMetadataChangeTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|TezWork
argument_list|>
argument_list|(
name|TezWork
operator|.
name|class
argument_list|,
name|TezTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|taskvec
operator|.
name|add
argument_list|(
operator|new
name|TaskTuple
argument_list|<
name|SparkWork
argument_list|>
argument_list|(
name|SparkWork
operator|.
name|class
argument_list|,
name|SparkTask
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|Integer
argument_list|>
name|tid
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Integer
name|initialValue
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|int
name|getAndIncrementId
parameter_list|()
block|{
name|int
name|curValue
init|=
name|tid
operator|.
name|get
argument_list|()
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|tid
operator|.
name|set
argument_list|(
operator|new
name|Integer
argument_list|(
name|curValue
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|curValue
return|;
block|}
specifier|public
specifier|static
name|void
name|resetId
parameter_list|()
block|{
name|tid
operator|.
name|set
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Task
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|workClass
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
for|for
control|(
name|TaskTuple
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|t
range|:
name|taskvec
control|)
block|{
if|if
condition|(
name|t
operator|.
name|workClass
operator|==
name|workClass
condition|)
block|{
try|try
block|{
name|Task
argument_list|<
name|T
argument_list|>
name|ret
init|=
operator|(
name|Task
argument_list|<
name|T
argument_list|>
operator|)
name|t
operator|.
name|taskClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|ret
operator|.
name|setId
argument_list|(
literal|"Stage-"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|getAndIncrementId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"No task for work class "
operator|+
name|workClass
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|SafeVarargs
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Task
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|T
name|work
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
modifier|...
name|tasklist
parameter_list|)
block|{
name|Task
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|work
operator|.
name|getClass
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setWork
argument_list|(
name|work
argument_list|)
expr_stmt|;
if|if
condition|(
name|tasklist
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|(
name|ret
operator|)
return|;
block|}
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|clist
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
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|tasklist
control|)
block|{
name|clist
operator|.
name|add
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
name|ret
operator|.
name|setChildTasks
argument_list|(
name|clist
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Serializable
parameter_list|>
name|Task
argument_list|<
name|T
argument_list|>
name|getAndMakeChild
parameter_list|(
name|T
name|work
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
modifier|...
name|tasklist
parameter_list|)
block|{
name|Task
argument_list|<
name|T
argument_list|>
name|ret
init|=
name|get
argument_list|(
operator|(
name|Class
argument_list|<
name|T
argument_list|>
operator|)
name|work
operator|.
name|getClass
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|ret
operator|.
name|setWork
argument_list|(
name|work
argument_list|)
expr_stmt|;
if|if
condition|(
name|tasklist
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|(
name|ret
operator|)
return|;
block|}
name|makeChild
argument_list|(
name|ret
argument_list|,
name|tasklist
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|public
specifier|static
name|void
name|makeChild
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|ret
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
modifier|...
name|tasklist
parameter_list|)
block|{
comment|// Add the new task as child of each of the passed in tasks
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|tasklist
control|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|children
init|=
name|tsk
operator|.
name|getChildTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|==
literal|null
condition|)
block|{
name|children
operator|=
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
expr_stmt|;
block|}
name|children
operator|.
name|add
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|tsk
operator|.
name|setChildTasks
argument_list|(
name|children
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|TaskFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit


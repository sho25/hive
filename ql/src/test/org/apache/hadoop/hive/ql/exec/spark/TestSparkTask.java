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
name|spark
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|never
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|common
operator|.
name|metrics
operator|.
name|common
operator|.
name|Metrics
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
name|metrics
operator|.
name|common
operator|.
name|MetricsConstant
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
name|plan
operator|.
name|BaseWork
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
name|MapWork
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
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
specifier|public
class|class
name|TestSparkTask
block|{
annotation|@
name|Test
specifier|public
name|void
name|sparkTask_updates_Metrics
parameter_list|()
throws|throws
name|IOException
block|{
name|Metrics
name|mockMetrics
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Metrics
operator|.
name|class
argument_list|)
decl_stmt|;
name|SparkTask
name|sparkTask
init|=
operator|new
name|SparkTask
argument_list|()
decl_stmt|;
name|sparkTask
operator|.
name|updateTaskMetrics
argument_list|(
name|mockMetrics
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockMetrics
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_SPARK_TASKS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockMetrics
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_TEZ_TASKS
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockMetrics
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|incrementCounter
argument_list|(
name|MetricsConstant
operator|.
name|HIVE_MR_TASKS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|removeEmptySparkTask
parameter_list|()
block|{
name|SparkTask
name|grandpa
init|=
operator|new
name|SparkTask
argument_list|()
decl_stmt|;
name|SparkWork
name|grandpaWork
init|=
operator|new
name|SparkWork
argument_list|(
literal|"grandpa"
argument_list|)
decl_stmt|;
name|grandpaWork
operator|.
name|add
argument_list|(
operator|new
name|MapWork
argument_list|()
argument_list|)
expr_stmt|;
name|grandpa
operator|.
name|setWork
argument_list|(
name|grandpaWork
argument_list|)
expr_stmt|;
name|SparkTask
name|parent
init|=
operator|new
name|SparkTask
argument_list|()
decl_stmt|;
name|SparkWork
name|parentWork
init|=
operator|new
name|SparkWork
argument_list|(
literal|"parent"
argument_list|)
decl_stmt|;
name|parentWork
operator|.
name|add
argument_list|(
operator|new
name|MapWork
argument_list|()
argument_list|)
expr_stmt|;
name|parent
operator|.
name|setWork
argument_list|(
name|parentWork
argument_list|)
expr_stmt|;
name|SparkTask
name|child1
init|=
operator|new
name|SparkTask
argument_list|()
decl_stmt|;
name|SparkWork
name|childWork1
init|=
operator|new
name|SparkWork
argument_list|(
literal|"child1"
argument_list|)
decl_stmt|;
name|childWork1
operator|.
name|add
argument_list|(
operator|new
name|MapWork
argument_list|()
argument_list|)
expr_stmt|;
name|child1
operator|.
name|setWork
argument_list|(
name|childWork1
argument_list|)
expr_stmt|;
name|grandpa
operator|.
name|addDependentTask
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|parent
operator|.
name|addDependentTask
argument_list|(
name|child1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|grandpa
operator|.
name|getChildTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|child1
operator|.
name|getParentTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|isEmptySparkWork
argument_list|(
name|parent
operator|.
name|getWork
argument_list|()
argument_list|)
condition|)
block|{
name|SparkUtilities
operator|.
name|removeEmptySparkTask
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|grandpa
operator|.
name|getChildTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|child1
operator|.
name|getParentTasks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|isEmptySparkWork
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|allWorks
init|=
name|sparkWork
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
name|boolean
name|allWorksIsEmtpy
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|allWorks
control|)
block|{
if|if
condition|(
name|work
operator|.
name|getAllOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|allWorksIsEmtpy
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
return|return
name|allWorksIsEmtpy
return|;
block|}
block|}
end_class

end_unit


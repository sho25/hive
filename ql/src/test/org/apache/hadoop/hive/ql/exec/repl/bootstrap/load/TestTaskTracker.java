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
name|load
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|modules
operator|.
name|junit4
operator|.
name|PowerMockRunner
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|PowerMockRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestTaskTracker
block|{
annotation|@
name|Mock
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|taskTrackerCompositionInitializesTheMaxTasksCorrectly
parameter_list|()
block|{
name|TaskTracker
name|taskTracker
init|=
operator|new
name|TaskTracker
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|taskTracker
operator|.
name|canAddMoreTasks
argument_list|()
argument_list|)
expr_stmt|;
name|taskTracker
operator|.
name|addTask
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|taskTracker
operator|.
name|canAddMoreTasks
argument_list|()
argument_list|)
expr_stmt|;
name|TaskTracker
name|taskTracker2
init|=
operator|new
name|TaskTracker
argument_list|(
name|taskTracker
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|taskTracker2
operator|.
name|canAddMoreTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


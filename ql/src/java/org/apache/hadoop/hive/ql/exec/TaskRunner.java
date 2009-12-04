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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|QueryPlan
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|HiveException
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
name|session
operator|.
name|SessionState
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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

begin_comment
comment|/**  * TaskRunner implementation  **/
end_comment

begin_class
specifier|public
class|class
name|TaskRunner
extends|extends
name|Thread
block|{
specifier|protected
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
decl_stmt|;
specifier|protected
name|TaskResult
name|result
decl_stmt|;
specifier|protected
name|SessionState
name|ss
decl_stmt|;
specifier|public
name|TaskRunner
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
parameter_list|,
name|TaskResult
name|result
parameter_list|)
block|{
name|this
operator|.
name|tsk
operator|=
name|tsk
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
name|ss
operator|=
name|SessionState
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getTask
parameter_list|()
block|{
return|return
name|tsk
return|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|runSequential
argument_list|()
expr_stmt|;
block|}
comment|/**    * Launches a task, and sets its exit value in the result variable    */
specifier|public
name|void
name|runSequential
parameter_list|()
block|{
name|int
name|exitVal
init|=
name|tsk
operator|.
name|executeTask
argument_list|()
decl_stmt|;
name|result
operator|.
name|setExitVal
argument_list|(
name|exitVal
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|optimizer
operator|.
name|physical
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
name|parse
operator|.
name|ParseContext
import|;
end_import

begin_comment
comment|/**  * physical context used by physical resolvers.  */
end_comment

begin_class
specifier|public
class|class
name|PhysicalContext
block|{
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|ParseContext
name|parseContext
decl_stmt|;
specifier|private
name|Context
name|context
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
decl_stmt|;
specifier|protected
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fetchTask
decl_stmt|;
specifier|public
name|PhysicalContext
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|,
name|Context
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fetchTask
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|parseContext
operator|=
name|parseContext
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
expr_stmt|;
name|this
operator|.
name|fetchTask
operator|=
name|fetchTask
expr_stmt|;
block|}
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|void
name|setConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|ParseContext
name|getParseContext
parameter_list|()
block|{
return|return
name|parseContext
return|;
block|}
specifier|public
name|void
name|setParseContext
parameter_list|(
name|ParseContext
name|parseContext
parameter_list|)
block|{
name|this
operator|.
name|parseContext
operator|=
name|parseContext
expr_stmt|;
block|}
specifier|public
name|Context
name|getContext
parameter_list|()
block|{
return|return
name|context
return|;
block|}
specifier|public
name|void
name|setContext
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
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
name|getRootTasks
parameter_list|()
block|{
return|return
name|rootTasks
return|;
block|}
specifier|public
name|void
name|setRootTasks
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
name|rootTasks
parameter_list|)
block|{
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getFetchTask
parameter_list|()
block|{
return|return
name|fetchTask
return|;
block|}
specifier|public
name|void
name|setFetchTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fetchTask
parameter_list|)
block|{
name|this
operator|.
name|fetchTask
operator|=
name|fetchTask
expr_stmt|;
block|}
specifier|public
name|void
name|addToRootTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
parameter_list|)
block|{
name|rootTasks
operator|.
name|add
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|removeFromRootTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
parameter_list|)
block|{
name|rootTasks
operator|.
name|remove
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


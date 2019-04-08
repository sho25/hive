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
name|ddl
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|Map
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
name|CompilationOpContext
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
name|DriverContext
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
name|QueryState
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
name|ExplainConfiguration
operator|.
name|AnalyzeState
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
name|api
operator|.
name|StageType
import|;
end_import

begin_comment
comment|/**  * DDLTask implementation. **/
end_comment

begin_class
specifier|public
specifier|final
class|class
name|DDLTask2
extends|extends
name|Task
argument_list|<
name|DDLWork2
argument_list|>
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
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|DDLDesc
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|DDLOperation
argument_list|>
argument_list|>
name|DESC_TO_OPARATION
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|void
name|registerOperation
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|DDLDesc
argument_list|>
name|descClass
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|DDLOperation
argument_list|>
name|operationClass
parameter_list|)
block|{
name|DESC_TO_OPARATION
operator|.
name|put
argument_list|(
name|descClass
argument_list|,
name|operationClass
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requireLock
parameter_list|()
block|{
return|return
name|this
operator|.
name|work
operator|!=
literal|null
operator|&&
name|this
operator|.
name|work
operator|.
name|getNeedLock
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|ctx
parameter_list|,
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|ctx
argument_list|,
name|opContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
if|if
condition|(
name|driverContext
operator|.
name|getCtx
argument_list|()
operator|.
name|getExplainAnalyze
argument_list|()
operator|==
name|AnalyzeState
operator|.
name|RUNNING
condition|)
block|{
return|return
literal|0
return|;
block|}
try|try
block|{
name|DDLDesc
name|ddlDesc
init|=
name|work
operator|.
name|getDDLDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|DESC_TO_OPARATION
operator|.
name|containsKey
argument_list|(
name|ddlDesc
operator|.
name|getClass
argument_list|()
argument_list|)
condition|)
block|{
name|DDLOperationContext
name|context
init|=
operator|new
name|DDLOperationContext
argument_list|(
name|conf
argument_list|,
name|driverContext
argument_list|,
name|this
argument_list|,
operator|(
name|DDLWork2
operator|)
name|work
argument_list|,
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|console
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|DDLOperation
argument_list|>
name|ddlOpertaionClass
init|=
name|DESC_TO_OPARATION
operator|.
name|get
argument_list|(
name|ddlDesc
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
extends|extends
name|DDLOperation
argument_list|>
name|constructor
init|=
name|ddlOpertaionClass
operator|.
name|getConstructor
argument_list|(
name|DDLOperationContext
operator|.
name|class
argument_list|,
name|ddlDesc
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|DDLOperation
name|ddlOperation
init|=
name|constructor
operator|.
name|newInstance
argument_list|(
name|context
argument_list|,
name|ddlDesc
argument_list|)
decl_stmt|;
return|return
name|ddlOperation
operator|.
name|execute
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown DDL request: "
operator|+
name|ddlDesc
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|failed
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
name|void
name|failed
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
while|while
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getClass
argument_list|()
operator|==
name|RuntimeException
operator|.
name|class
condition|)
block|{
name|e
operator|=
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|DDL
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"DDL"
return|;
block|}
comment|/*   uses the authorizer from SessionState will need some more work to get this to run in parallel,   however this should not be a bottle neck so might not need to parallelize this.    */
annotation|@
name|Override
specifier|public
name|boolean
name|canExecuteInParallel
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit


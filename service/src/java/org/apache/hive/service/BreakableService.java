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
name|hive
operator|.
name|service
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
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_comment
comment|/**  * This is a service that can be configured to break on any of the lifecycle  * events, so test the failure handling of other parts of the service  * infrastructure.  *  * It retains a counter to the number of times each entry point is called -  * these counters are incremented before the exceptions are raised and  * before the superclass state methods are invoked.  *  */
end_comment

begin_class
specifier|public
class|class
name|BreakableService
extends|extends
name|AbstractService
block|{
specifier|private
name|boolean
name|failOnInit
decl_stmt|;
specifier|private
name|boolean
name|failOnStart
decl_stmt|;
specifier|private
name|boolean
name|failOnStop
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|counts
init|=
operator|new
name|int
index|[
literal|4
index|]
decl_stmt|;
specifier|public
name|BreakableService
parameter_list|()
block|{
name|this
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BreakableService
parameter_list|(
name|boolean
name|failOnInit
parameter_list|,
name|boolean
name|failOnStart
parameter_list|,
name|boolean
name|failOnStop
parameter_list|)
block|{
name|super
argument_list|(
literal|"BreakableService"
argument_list|)
expr_stmt|;
name|this
operator|.
name|failOnInit
operator|=
name|failOnInit
expr_stmt|;
name|this
operator|.
name|failOnStart
operator|=
name|failOnStart
expr_stmt|;
name|this
operator|.
name|failOnStop
operator|=
name|failOnStop
expr_stmt|;
name|inc
argument_list|(
name|STATE
operator|.
name|NOTINITED
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|convert
parameter_list|(
name|STATE
name|state
parameter_list|)
block|{
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|NOTINITED
case|:
return|return
literal|0
return|;
case|case
name|INITED
case|:
return|return
literal|1
return|;
case|case
name|STARTED
case|:
return|return
literal|2
return|;
case|case
name|STOPPED
case|:
return|return
literal|3
return|;
default|default:
return|return
literal|0
return|;
block|}
block|}
specifier|private
name|void
name|inc
parameter_list|(
name|STATE
name|state
parameter_list|)
block|{
name|int
name|index
init|=
name|convert
argument_list|(
name|state
argument_list|)
decl_stmt|;
name|counts
index|[
name|index
index|]
operator|++
expr_stmt|;
block|}
specifier|public
name|int
name|getCount
parameter_list|(
name|STATE
name|state
parameter_list|)
block|{
return|return
name|counts
index|[
name|convert
argument_list|(
name|state
argument_list|)
index|]
return|;
block|}
specifier|private
name|void
name|maybeFail
parameter_list|(
name|boolean
name|fail
parameter_list|,
name|String
name|action
parameter_list|)
block|{
if|if
condition|(
name|fail
condition|)
block|{
throw|throw
operator|new
name|BrokenLifecycleEvent
argument_list|(
name|action
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|inc
argument_list|(
name|STATE
operator|.
name|INITED
argument_list|)
expr_stmt|;
name|maybeFail
argument_list|(
name|failOnInit
argument_list|,
literal|"init"
argument_list|)
expr_stmt|;
name|super
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|inc
argument_list|(
name|STATE
operator|.
name|STARTED
argument_list|)
expr_stmt|;
name|maybeFail
argument_list|(
name|failOnStart
argument_list|,
literal|"start"
argument_list|)
expr_stmt|;
name|super
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|inc
argument_list|(
name|STATE
operator|.
name|STOPPED
argument_list|)
expr_stmt|;
name|maybeFail
argument_list|(
name|failOnStop
argument_list|,
literal|"stop"
argument_list|)
expr_stmt|;
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setFailOnInit
parameter_list|(
name|boolean
name|failOnInit
parameter_list|)
block|{
name|this
operator|.
name|failOnInit
operator|=
name|failOnInit
expr_stmt|;
block|}
specifier|public
name|void
name|setFailOnStart
parameter_list|(
name|boolean
name|failOnStart
parameter_list|)
block|{
name|this
operator|.
name|failOnStart
operator|=
name|failOnStart
expr_stmt|;
block|}
specifier|public
name|void
name|setFailOnStop
parameter_list|(
name|boolean
name|failOnStop
parameter_list|)
block|{
name|this
operator|.
name|failOnStop
operator|=
name|failOnStop
expr_stmt|;
block|}
comment|/**    * The exception explicitly raised on a failure    */
specifier|public
specifier|static
class|class
name|BrokenLifecycleEvent
extends|extends
name|RuntimeException
block|{
name|BrokenLifecycleEvent
parameter_list|(
name|String
name|action
parameter_list|)
block|{
name|super
argument_list|(
literal|"Lifecycle Failure during "
operator|+
name|action
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


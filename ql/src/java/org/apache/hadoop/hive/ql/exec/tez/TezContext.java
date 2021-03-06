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
name|tez
package|;
end_package

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
name|exec
operator|.
name|DummyStoreOperator
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
name|MapredContext
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|ProcessorContext
import|;
end_import

begin_comment
comment|/**  * TezContext contains additional context only available with Tez  */
end_comment

begin_class
specifier|public
class|class
name|TezContext
extends|extends
name|MapredContext
block|{
comment|// all the inputs for the tez processor
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputs
decl_stmt|;
specifier|private
name|ProcessorContext
name|processorContext
decl_stmt|;
specifier|private
name|RecordSource
index|[]
name|sources
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
name|dummyOpsMap
decl_stmt|;
specifier|public
name|TezContext
parameter_list|(
name|boolean
name|isMap
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
block|{
name|super
argument_list|(
name|isMap
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setInputs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
block|}
specifier|public
name|void
name|setOutputs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalOutput
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
block|}
specifier|public
name|LogicalInput
name|getInput
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|inputs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|inputs
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|LogicalOutput
name|getOutput
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|outputs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputs
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|void
name|setTezProcessorContext
parameter_list|(
name|ProcessorContext
name|processorContext
parameter_list|)
block|{
name|this
operator|.
name|processorContext
operator|=
name|processorContext
expr_stmt|;
block|}
specifier|public
name|ProcessorContext
name|getTezProcessorContext
parameter_list|()
block|{
return|return
name|processorContext
return|;
block|}
specifier|public
name|RecordSource
index|[]
name|getRecordSources
parameter_list|()
block|{
return|return
name|sources
return|;
block|}
specifier|public
name|void
name|setRecordSources
parameter_list|(
name|RecordSource
index|[]
name|sources
parameter_list|)
block|{
name|this
operator|.
name|sources
operator|=
name|sources
expr_stmt|;
block|}
specifier|public
name|void
name|setDummyOpsMap
parameter_list|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
name|dummyOpsMap
parameter_list|)
block|{
name|this
operator|.
name|dummyOpsMap
operator|=
name|dummyOpsMap
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
name|getDummyOpsMap
parameter_list|()
block|{
return|return
name|dummyOpsMap
return|;
block|}
block|}
end_class

end_unit


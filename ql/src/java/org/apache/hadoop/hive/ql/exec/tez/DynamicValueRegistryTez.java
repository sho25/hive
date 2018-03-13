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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|DynamicValueRegistry
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
name|ExprNodeEvaluator
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
name|ExprNodeEvaluatorFactory
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
name|RuntimeValuesInfo
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
name|DynamicValue
operator|.
name|NoDynamicValuesException
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
name|ExprNodeDesc
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
name|serde2
operator|.
name|Deserializer
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|io
operator|.
name|Writable
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
name|ReflectionUtils
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
name|Input
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
name|ProcessorContext
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
name|library
operator|.
name|api
operator|.
name|KeyValueReader
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
name|concurrent
operator|.
name|ConcurrentHashMap
import|;
end_import

begin_class
specifier|public
class|class
name|DynamicValueRegistryTez
implements|implements
name|DynamicValueRegistry
block|{
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
name|DynamicValueRegistryTez
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|RegistryConfTez
extends|extends
name|RegistryConf
block|{
specifier|public
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|BaseWork
name|baseWork
decl_stmt|;
specifier|public
name|ProcessorContext
name|processorContext
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
decl_stmt|;
specifier|public
name|RegistryConfTez
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|BaseWork
name|baseWork
parameter_list|,
name|ProcessorContext
name|processorContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalInput
argument_list|>
name|inputs
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
name|baseWork
operator|=
name|baseWork
expr_stmt|;
name|this
operator|.
name|processorContext
operator|=
name|processorContext
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
block|}
block|}
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|values
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|DynamicValueRegistryTez
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|Object
name|getValue
parameter_list|(
name|String
name|key
parameter_list|)
block|{
if|if
condition|(
operator|!
name|values
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|NoDynamicValuesException
argument_list|(
literal|"Value does not exist in registry: "
operator|+
name|key
argument_list|)
throw|;
block|}
return|return
name|values
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|protected
name|void
name|setValue
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|values
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|RegistryConf
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|RegistryConfTez
name|rct
init|=
operator|(
name|RegistryConfTez
operator|)
name|conf
decl_stmt|;
for|for
control|(
name|String
name|inputSourceName
range|:
name|rct
operator|.
name|baseWork
operator|.
name|getInputSourceToRuntimeValuesInfo
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Runtime value source: "
operator|+
name|inputSourceName
argument_list|)
expr_stmt|;
name|LogicalInput
name|runtimeValueInput
init|=
name|rct
operator|.
name|inputs
operator|.
name|get
argument_list|(
name|inputSourceName
argument_list|)
decl_stmt|;
name|RuntimeValuesInfo
name|runtimeValuesInfo
init|=
name|rct
operator|.
name|baseWork
operator|.
name|getInputSourceToRuntimeValuesInfo
argument_list|()
operator|.
name|get
argument_list|(
name|inputSourceName
argument_list|)
decl_stmt|;
comment|// Setup deserializer/obj inspectors for the incoming data source
name|Deserializer
name|deserializer
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|runtimeValuesInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|deserializer
operator|.
name|initialize
argument_list|(
name|rct
operator|.
name|conf
argument_list|,
name|runtimeValuesInfo
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|ObjectInspector
name|inspector
init|=
name|deserializer
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
comment|// Set up col expressions for the dynamic values using this input
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|colExprEvaluators
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|runtimeValuesInfo
operator|.
name|getColExprs
argument_list|()
control|)
block|{
name|ExprNodeEvaluator
name|exprEval
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|exprEval
operator|.
name|initialize
argument_list|(
name|inspector
argument_list|)
expr_stmt|;
name|colExprEvaluators
operator|.
name|add
argument_list|(
name|exprEval
argument_list|)
expr_stmt|;
block|}
name|runtimeValueInput
operator|.
name|start
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Input
argument_list|>
name|inputList
init|=
operator|new
name|ArrayList
argument_list|<
name|Input
argument_list|>
argument_list|()
decl_stmt|;
name|inputList
operator|.
name|add
argument_list|(
name|runtimeValueInput
argument_list|)
expr_stmt|;
name|rct
operator|.
name|processorContext
operator|.
name|waitForAllInputsReady
argument_list|(
name|inputList
argument_list|)
expr_stmt|;
name|KeyValueReader
name|kvReader
init|=
operator|(
name|KeyValueReader
operator|)
name|runtimeValueInput
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|long
name|rowCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|kvReader
operator|.
name|next
argument_list|()
condition|)
block|{
name|Object
name|row
init|=
name|deserializer
operator|.
name|deserialize
argument_list|(
operator|(
name|Writable
operator|)
name|kvReader
operator|.
name|getCurrentValue
argument_list|()
argument_list|)
decl_stmt|;
name|rowCount
operator|++
expr_stmt|;
for|for
control|(
name|int
name|colIdx
init|=
literal|0
init|;
name|colIdx
operator|<
name|colExprEvaluators
operator|.
name|size
argument_list|()
condition|;
operator|++
name|colIdx
control|)
block|{
comment|// Read each expression and save it to the value registry
name|ExprNodeEvaluator
name|eval
init|=
name|colExprEvaluators
operator|.
name|get
argument_list|(
name|colIdx
argument_list|)
decl_stmt|;
name|Object
name|val
init|=
name|eval
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|setValue
argument_list|(
name|runtimeValuesInfo
operator|.
name|getDynamicValueIDs
argument_list|()
operator|.
name|get
argument_list|(
name|colIdx
argument_list|)
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
comment|// For now, expecting a single row (min/max, aggregated bloom filter), or no rows
if|if
condition|(
name|rowCount
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"No input rows from "
operator|+
name|inputSourceName
operator|+
literal|", filling dynamic values with nulls"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|colIdx
init|=
literal|0
init|;
name|colIdx
operator|<
name|colExprEvaluators
operator|.
name|size
argument_list|()
condition|;
operator|++
name|colIdx
control|)
block|{
name|ExprNodeEvaluator
name|eval
init|=
name|colExprEvaluators
operator|.
name|get
argument_list|(
name|colIdx
argument_list|)
decl_stmt|;
name|setValue
argument_list|(
name|runtimeValuesInfo
operator|.
name|getDynamicValueIDs
argument_list|()
operator|.
name|get
argument_list|(
name|colIdx
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|rowCount
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Expected 0 or 1 rows from "
operator|+
name|inputSourceName
operator|+
literal|", got "
operator|+
name|rowCount
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit


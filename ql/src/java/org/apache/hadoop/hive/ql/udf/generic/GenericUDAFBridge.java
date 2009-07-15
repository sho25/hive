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
name|udf
operator|.
name|generic
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
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|FunctionRegistry
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
name|UDAF
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
name|UDAFEvaluator
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
name|parse
operator|.
name|SemanticException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
operator|.
name|Mode
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFUtils
operator|.
name|PrimitiveConversionHelper
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|typeinfo
operator|.
name|TypeInfo
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
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * This class is a bridge between GenericUDAF and UDAF.  * Old UDAF can be used with the GenericUDAF infrastructure through  * this bridge.  */
end_comment

begin_class
specifier|public
class|class
name|GenericUDAFBridge
implements|implements
name|GenericUDAFResolver
block|{
name|UDAF
name|udaf
decl_stmt|;
specifier|public
name|GenericUDAFBridge
parameter_list|(
name|UDAF
name|udaf
parameter_list|)
block|{
name|this
operator|.
name|udaf
operator|=
name|udaf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|udafEvaluatorClass
init|=
name|udaf
operator|.
name|getResolver
argument_list|()
operator|.
name|getEvaluatorClass
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|parameters
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|GenericUDAFBridgeEvaluator
argument_list|(
name|udafEvaluatorClass
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFBridgeEvaluator
extends|extends
name|GenericUDAFEvaluator
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
comment|// Used by serialization only
specifier|public
name|GenericUDAFBridgeEvaluator
parameter_list|()
block|{     }
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|getUdafEvaluator
parameter_list|()
block|{
return|return
name|udafEvaluator
return|;
block|}
specifier|public
name|void
name|setUdafEvaluator
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|udafEvaluator
parameter_list|)
block|{
name|this
operator|.
name|udafEvaluator
operator|=
name|udafEvaluator
expr_stmt|;
block|}
specifier|public
name|GenericUDAFBridgeEvaluator
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|udafEvaluator
parameter_list|)
block|{
name|this
operator|.
name|udafEvaluator
operator|=
name|udafEvaluator
expr_stmt|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|udafEvaluator
decl_stmt|;
specifier|transient
name|ObjectInspector
index|[]
name|parameterOIs
decl_stmt|;
specifier|transient
name|Object
name|result
decl_stmt|;
specifier|transient
name|Method
name|iterateMethod
decl_stmt|;
specifier|transient
name|Method
name|mergeMethod
decl_stmt|;
specifier|transient
name|Method
name|terminatePartialMethod
decl_stmt|;
specifier|transient
name|Method
name|terminateMethod
decl_stmt|;
specifier|transient
name|PrimitiveConversionHelper
name|conversionHelper
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|init
parameter_list|(
name|Mode
name|m
parameter_list|,
name|ObjectInspector
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
name|this
operator|.
name|parameterOIs
operator|=
name|parameters
expr_stmt|;
comment|// Get the reflection methods from ue
for|for
control|(
name|Method
name|method
range|:
name|udafEvaluator
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"iterate"
argument_list|)
condition|)
block|{
name|iterateMethod
operator|=
name|method
expr_stmt|;
block|}
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"merge"
argument_list|)
condition|)
block|{
name|mergeMethod
operator|=
name|method
expr_stmt|;
block|}
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"terminatePartial"
argument_list|)
condition|)
block|{
name|terminatePartialMethod
operator|=
name|method
expr_stmt|;
block|}
if|if
condition|(
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"terminate"
argument_list|)
condition|)
block|{
name|terminateMethod
operator|=
name|method
expr_stmt|;
block|}
block|}
comment|// Input: do Java/Writable conversion if needed
name|Method
name|aggregateMethod
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|aggregateMethod
operator|=
name|iterateMethod
expr_stmt|;
block|}
else|else
block|{
name|aggregateMethod
operator|=
name|mergeMethod
expr_stmt|;
block|}
name|conversionHelper
operator|=
operator|new
name|PrimitiveConversionHelper
argument_list|(
name|aggregateMethod
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
comment|// Output: get the evaluate method
name|Method
name|evaluateMethod
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL2
condition|)
block|{
name|evaluateMethod
operator|=
name|terminatePartialMethod
expr_stmt|;
block|}
else|else
block|{
name|evaluateMethod
operator|=
name|terminateMethod
expr_stmt|;
block|}
comment|// Get the output ObjectInspector from the return type.
name|Class
argument_list|<
name|?
argument_list|>
name|returnType
init|=
name|evaluateMethod
operator|.
name|getReturnType
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveObjectInspectorFromClass
argument_list|(
name|returnType
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot recognize return type "
operator|+
name|returnType
operator|+
literal|" from "
operator|+
name|evaluateMethod
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/** class for storing UDAFEvaluator value */
specifier|static
class|class
name|UDAFAgg
implements|implements
name|AggregationBuffer
block|{
name|UDAFEvaluator
name|ueObject
decl_stmt|;
name|UDAFAgg
parameter_list|(
name|UDAFEvaluator
name|ueObject
parameter_list|)
block|{
name|this
operator|.
name|ueObject
operator|=
name|ueObject
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
block|{
return|return
operator|new
name|UDAFAgg
argument_list|(
operator|(
name|UDAFEvaluator
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|udafEvaluator
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
operator|(
operator|(
name|UDAFAgg
operator|)
name|agg
operator|)
operator|.
name|ueObject
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|iterate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|FunctionRegistry
operator|.
name|invoke
argument_list|(
name|iterateMethod
argument_list|,
operator|(
operator|(
name|UDAFAgg
operator|)
name|agg
operator|)
operator|.
name|ueObject
argument_list|,
name|conversionHelper
operator|.
name|convertIfNecessary
argument_list|(
name|parameters
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
name|partial
parameter_list|)
throws|throws
name|HiveException
block|{
name|FunctionRegistry
operator|.
name|invoke
argument_list|(
name|mergeMethod
argument_list|,
operator|(
operator|(
name|UDAFAgg
operator|)
name|agg
operator|)
operator|.
name|ueObject
argument_list|,
name|conversionHelper
operator|.
name|convertIfNecessary
argument_list|(
name|partial
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|FunctionRegistry
operator|.
name|invoke
argument_list|(
name|terminateMethod
argument_list|,
operator|(
operator|(
name|UDAFAgg
operator|)
name|agg
operator|)
operator|.
name|ueObject
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminatePartial
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|FunctionRegistry
operator|.
name|invoke
argument_list|(
name|terminatePartialMethod
argument_list|,
operator|(
operator|(
name|UDAFAgg
operator|)
name|agg
operator|)
operator|.
name|ueObject
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


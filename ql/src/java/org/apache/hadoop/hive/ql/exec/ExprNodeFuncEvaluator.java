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
name|plan
operator|.
name|exprNodeFuncDesc
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
name|InspectableObject
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
name|ListObjectInspector
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
name|MapObjectInspector
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
name|ObjectInspectorFactory
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
operator|.
name|Category
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

begin_class
specifier|public
class|class
name|ExprNodeFuncEvaluator
extends|extends
name|ExprNodeEvaluator
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ExprNodeFuncEvaluator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|exprNodeFuncDesc
name|expr
decl_stmt|;
specifier|transient
name|ExprNodeEvaluator
index|[]
name|paramEvaluators
decl_stmt|;
specifier|transient
name|InspectableObject
index|[]
name|paramInspectableObjects
decl_stmt|;
specifier|transient
name|Object
index|[]
name|paramValues
decl_stmt|;
specifier|transient
name|UDF
name|udf
decl_stmt|;
specifier|transient
name|Method
name|udfMethod
decl_stmt|;
specifier|transient
name|ObjectInspector
name|outputObjectInspector
decl_stmt|;
specifier|public
name|ExprNodeFuncEvaluator
parameter_list|(
name|exprNodeFuncDesc
name|expr
parameter_list|)
block|{
name|this
operator|.
name|expr
operator|=
name|expr
expr_stmt|;
assert|assert
operator|(
name|expr
operator|!=
literal|null
operator|)
assert|;
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|expr
operator|.
name|getUDFClass
argument_list|()
decl_stmt|;
name|udfMethod
operator|=
name|expr
operator|.
name|getUDFMethod
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|c
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|udfMethod
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|udf
operator|=
operator|(
name|UDF
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|expr
operator|.
name|getUDFClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|int
name|paramNumber
init|=
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|paramEvaluators
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|paramNumber
index|]
expr_stmt|;
name|paramInspectableObjects
operator|=
operator|new
name|InspectableObject
index|[
name|paramNumber
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|paramNumber
condition|;
name|i
operator|++
control|)
block|{
name|paramEvaluators
index|[
name|i
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|paramInspectableObjects
index|[
name|i
index|]
operator|=
operator|new
name|InspectableObject
argument_list|()
expr_stmt|;
block|}
name|paramValues
operator|=
operator|new
name|Object
index|[
name|expr
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|outputObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardPrimitiveObjectInspector
argument_list|(
name|udfMethod
operator|.
name|getReturnType
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|evaluate
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|,
name|InspectableObject
name|result
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"result cannot be null."
argument_list|)
throw|;
block|}
comment|// Evaluate all children first
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|paramEvaluators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|paramEvaluators
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|,
name|paramInspectableObjects
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|Category
name|c
init|=
name|paramInspectableObjects
index|[
name|i
index|]
operator|.
name|oi
operator|.
name|getCategory
argument_list|()
decl_stmt|;
comment|// TODO: Both getList and getMap are not very efficient.
comment|// We should convert them to UDFTemplate - UDFs that accepts Object with
comment|// ObjectInspectors when needed.
if|if
condition|(
name|c
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|LIST
argument_list|)
condition|)
block|{
comment|// Need to pass a Java List for List type
name|paramValues
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|ListObjectInspector
operator|)
name|paramInspectableObjects
index|[
name|i
index|]
operator|.
name|oi
operator|)
operator|.
name|getList
argument_list|(
name|paramInspectableObjects
index|[
name|i
index|]
operator|.
name|o
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|MAP
argument_list|)
condition|)
block|{
comment|// Need to pass a Java Map for Map type
name|paramValues
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|MapObjectInspector
operator|)
name|paramInspectableObjects
index|[
name|i
index|]
operator|.
name|oi
operator|)
operator|.
name|getMap
argument_list|(
name|paramInspectableObjects
index|[
name|i
index|]
operator|.
name|o
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|paramValues
index|[
name|i
index|]
operator|=
name|paramInspectableObjects
index|[
name|i
index|]
operator|.
name|o
expr_stmt|;
block|}
block|}
name|result
operator|.
name|o
operator|=
name|FunctionRegistry
operator|.
name|invoke
argument_list|(
name|udfMethod
argument_list|,
name|udf
argument_list|,
name|paramValues
argument_list|)
expr_stmt|;
name|result
operator|.
name|oi
operator|=
name|outputObjectInspector
expr_stmt|;
block|}
specifier|public
name|ObjectInspector
name|evaluateInspector
parameter_list|(
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|outputObjectInspector
return|;
block|}
block|}
end_class

end_unit


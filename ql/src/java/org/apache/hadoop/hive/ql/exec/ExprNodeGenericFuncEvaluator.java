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
name|ql
operator|.
name|plan
operator|.
name|ExprNodeGenericFuncDesc
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
name|GenericUDF
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
name|GenericUDFBaseCompare
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
name|GenericUDFCase
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
name|GenericUDFWhen
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
name|ConstantObjectInspector
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
name|ObjectInspectorUtils
import|;
end_import

begin_comment
comment|/**  * ExprNodeGenericFuncEvaluator.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeGenericFuncEvaluator
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
name|ExprNodeGenericFuncEvaluator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|ExprNodeGenericFuncDesc
name|expr
decl_stmt|;
specifier|transient
name|GenericUDF
name|genericUDF
decl_stmt|;
specifier|transient
name|Object
name|rowObject
decl_stmt|;
specifier|transient
name|ObjectInspector
name|outputOI
decl_stmt|;
specifier|transient
name|ExprNodeEvaluator
index|[]
name|children
decl_stmt|;
specifier|transient
name|GenericUDF
operator|.
name|DeferredObject
index|[]
name|deferredChildren
decl_stmt|;
specifier|transient
name|boolean
name|isEager
decl_stmt|;
comment|/**    * Class to allow deferred evaluation for GenericUDF.    */
class|class
name|DeferredExprObject
implements|implements
name|GenericUDF
operator|.
name|DeferredObject
block|{
name|ExprNodeEvaluator
name|eval
decl_stmt|;
name|DeferredExprObject
parameter_list|(
name|ExprNodeEvaluator
name|eval
parameter_list|)
block|{
name|this
operator|.
name|eval
operator|=
name|eval
expr_stmt|;
block|}
specifier|public
name|Object
name|get
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
name|eval
operator|.
name|evaluate
argument_list|(
name|rowObject
argument_list|)
return|;
block|}
block|}
comment|/**    * Class to force eager evaluation for GenericUDF in cases where    * it is warranted.    */
class|class
name|EagerExprObject
implements|implements
name|GenericUDF
operator|.
name|DeferredObject
block|{
name|ExprNodeEvaluator
name|eval
decl_stmt|;
specifier|transient
name|Object
name|obj
decl_stmt|;
name|EagerExprObject
parameter_list|(
name|ExprNodeEvaluator
name|eval
parameter_list|)
block|{
name|this
operator|.
name|eval
operator|=
name|eval
expr_stmt|;
block|}
name|void
name|evaluate
parameter_list|()
throws|throws
name|HiveException
block|{
name|obj
operator|=
name|eval
operator|.
name|evaluate
argument_list|(
name|rowObject
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|get
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
name|obj
return|;
block|}
block|}
specifier|public
name|ExprNodeGenericFuncEvaluator
parameter_list|(
name|ExprNodeGenericFuncDesc
name|expr
parameter_list|)
block|{
name|this
operator|.
name|expr
operator|=
name|expr
expr_stmt|;
name|children
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|expr
operator|.
name|getChildExprs
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|isEager
operator|=
literal|false
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
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeDesc
name|child
init|=
name|expr
operator|.
name|getChildExprs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|nodeEvaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|child
argument_list|)
decl_stmt|;
name|children
index|[
name|i
index|]
operator|=
name|nodeEvaluator
expr_stmt|;
comment|// If we have eager evaluators anywhere below us, then we are eager too.
if|if
condition|(
name|nodeEvaluator
operator|instanceof
name|ExprNodeGenericFuncEvaluator
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|ExprNodeGenericFuncEvaluator
operator|)
name|nodeEvaluator
operator|)
operator|.
name|isEager
condition|)
block|{
name|isEager
operator|=
literal|true
expr_stmt|;
block|}
comment|// Base case:  we are eager if a child is stateful
name|GenericUDF
name|childUDF
init|=
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|child
operator|)
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
if|if
condition|(
name|FunctionRegistry
operator|.
name|isStateful
argument_list|(
name|childUDF
argument_list|)
condition|)
block|{
name|isEager
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
name|deferredChildren
operator|=
operator|new
name|GenericUDF
operator|.
name|DeferredObject
index|[
name|expr
operator|.
name|getChildExprs
argument_list|()
operator|.
name|size
argument_list|()
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
name|deferredChildren
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|isEager
condition|)
block|{
name|deferredChildren
index|[
name|i
index|]
operator|=
operator|new
name|EagerExprObject
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|deferredChildren
index|[
name|i
index|]
operator|=
operator|new
name|DeferredExprObject
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Initialize all children first
name|ObjectInspector
index|[]
name|childrenOIs
init|=
operator|new
name|ObjectInspector
index|[
name|children
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|childrenOIs
index|[
name|i
index|]
operator|=
name|children
index|[
name|i
index|]
operator|.
name|initialize
argument_list|(
name|rowInspector
argument_list|)
expr_stmt|;
block|}
name|genericUDF
operator|=
name|expr
operator|.
name|getGenericUDF
argument_list|()
expr_stmt|;
if|if
condition|(
name|isEager
operator|&&
operator|(
operator|(
name|genericUDF
operator|instanceof
name|GenericUDFCase
operator|)
operator|||
operator|(
name|genericUDF
operator|instanceof
name|GenericUDFWhen
operator|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Stateful expressions cannot be used inside of CASE"
argument_list|)
throw|;
block|}
name|this
operator|.
name|outputOI
operator|=
name|genericUDF
operator|.
name|initializeAndFoldConstants
argument_list|(
name|childrenOIs
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|outputOI
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDeterministic
parameter_list|()
block|{
name|boolean
name|result
init|=
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|genericUDF
argument_list|)
decl_stmt|;
for|for
control|(
name|ExprNodeEvaluator
name|child
range|:
name|children
control|)
block|{
name|result
operator|=
name|result
operator|&&
name|child
operator|.
name|isDeterministic
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
name|rowObject
operator|=
name|row
expr_stmt|;
if|if
condition|(
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|outputOI
argument_list|)
operator|&&
name|isDeterministic
argument_list|()
condition|)
block|{
comment|// The output of this UDF is constant, so don't even bother evaluating.
return|return
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|outputOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
return|;
block|}
if|if
condition|(
name|isEager
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|deferredChildren
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|EagerExprObject
operator|)
name|deferredChildren
index|[
name|i
index|]
operator|)
operator|.
name|evaluate
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|genericUDF
operator|.
name|evaluate
argument_list|(
name|deferredChildren
argument_list|)
return|;
block|}
comment|/**    * If the genericUDF is a base comparison, it returns an integer based on the result of comparing    * the two sides of the UDF, like the compareTo method in Comparable.    *    * If the genericUDF is not a base comparison, or there is an error executing the comparison, it    * returns null.    * @param row    * @return the compare results    * @throws HiveException    */
specifier|public
name|Integer
name|compare
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|expr
operator|.
name|isSortedExpr
argument_list|()
operator|||
operator|!
operator|(
name|genericUDF
operator|instanceof
name|GenericUDFBaseCompare
operator|)
condition|)
block|{
for|for
control|(
name|ExprNodeEvaluator
name|evaluator
range|:
name|children
control|)
block|{
if|if
condition|(
name|evaluator
operator|instanceof
name|ExprNodeGenericFuncEvaluator
condition|)
block|{
name|Integer
name|comparison
init|=
operator|(
operator|(
name|ExprNodeGenericFuncEvaluator
operator|)
name|evaluator
operator|)
operator|.
name|compare
argument_list|(
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparison
operator|!=
literal|null
condition|)
block|{
return|return
name|comparison
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
name|rowObject
operator|=
name|row
expr_stmt|;
if|if
condition|(
name|isEager
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|deferredChildren
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
operator|(
operator|(
name|EagerExprObject
operator|)
name|deferredChildren
index|[
name|i
index|]
operator|)
operator|.
name|evaluate
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|(
operator|(
name|GenericUDFBaseCompare
operator|)
name|genericUDF
operator|)
operator|.
name|compare
argument_list|(
name|deferredChildren
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|exprNodeIndexDesc
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
name|ObjectInspector
operator|.
name|Category
import|;
end_import

begin_class
specifier|public
class|class
name|ExprNodeIndexEvaluator
extends|extends
name|ExprNodeEvaluator
block|{
specifier|protected
name|exprNodeIndexDesc
name|expr
decl_stmt|;
specifier|transient
name|ExprNodeEvaluator
name|mainEvaluator
decl_stmt|;
specifier|transient
name|InspectableObject
name|mainInspectableObject
init|=
operator|new
name|InspectableObject
argument_list|()
decl_stmt|;
specifier|transient
name|ExprNodeEvaluator
name|indexEvaluator
decl_stmt|;
specifier|transient
name|InspectableObject
name|indexInspectableObject
init|=
operator|new
name|InspectableObject
argument_list|()
decl_stmt|;
specifier|public
name|ExprNodeIndexEvaluator
parameter_list|(
name|exprNodeIndexDesc
name|expr
parameter_list|)
block|{
name|this
operator|.
name|expr
operator|=
name|expr
expr_stmt|;
name|mainEvaluator
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
operator|.
name|getDesc
argument_list|()
argument_list|)
expr_stmt|;
name|indexEvaluator
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|expr
operator|.
name|getIndex
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
assert|assert
operator|(
name|result
operator|!=
literal|null
operator|)
assert|;
name|mainEvaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|,
name|mainInspectableObject
argument_list|)
expr_stmt|;
name|indexEvaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|,
name|indexInspectableObject
argument_list|)
expr_stmt|;
if|if
condition|(
name|mainInspectableObject
operator|.
name|oi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|LIST
condition|)
block|{
name|int
name|index
init|=
operator|(
operator|(
name|Number
operator|)
name|indexInspectableObject
operator|.
name|o
operator|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|mainInspectableObject
operator|.
name|oi
decl_stmt|;
name|result
operator|.
name|oi
operator|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|result
operator|.
name|o
operator|=
name|loi
operator|.
name|getListElement
argument_list|(
name|mainInspectableObject
operator|.
name|o
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mainInspectableObject
operator|.
name|oi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|MAP
condition|)
block|{
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|mainInspectableObject
operator|.
name|oi
decl_stmt|;
name|result
operator|.
name|oi
operator|=
name|moi
operator|.
name|getMapValueObjectInspector
argument_list|()
expr_stmt|;
name|result
operator|.
name|o
operator|=
name|moi
operator|.
name|getMapValueElement
argument_list|(
name|mainInspectableObject
operator|.
name|o
argument_list|,
name|indexInspectableObject
operator|.
name|o
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Should never happen because we checked this in SemanticAnalyzer.getXpathOrFuncExprNodeDesc
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive 2 Internal error: cannot evaluate index expression on "
operator|+
name|mainInspectableObject
operator|.
name|oi
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
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
name|ObjectInspector
name|mainInspector
init|=
name|mainEvaluator
operator|.
name|evaluateInspector
argument_list|(
name|rowInspector
argument_list|)
decl_stmt|;
if|if
condition|(
name|mainInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|LIST
condition|)
block|{
return|return
operator|(
operator|(
name|ListObjectInspector
operator|)
name|mainInspector
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|mainInspector
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|MAP
condition|)
block|{
return|return
operator|(
operator|(
name|MapObjectInspector
operator|)
name|mainInspector
operator|)
operator|.
name|getMapValueObjectInspector
argument_list|()
return|;
block|}
else|else
block|{
comment|// Should never happen because we checked this in SemanticAnalyzer.getXpathOrFuncExprNodeDesc
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive 2 Internal error: cannot evaluate index expression on "
operator|+
name|mainInspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


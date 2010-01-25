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
name|Serializable
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
name|SelectDesc
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
name|OperatorType
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
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/**  * Select operator implementation  **/
end_comment

begin_class
specifier|public
class|class
name|SelectOperator
extends|extends
name|Operator
argument_list|<
name|SelectDesc
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
specifier|transient
specifier|protected
name|ExprNodeEvaluator
index|[]
name|eval
decl_stmt|;
specifier|transient
name|Object
index|[]
name|output
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Just forward the row as is
if|if
condition|(
name|conf
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
return|return;
block|}
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colList
init|=
name|conf
operator|.
name|getColList
argument_list|()
decl_stmt|;
name|eval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|colList
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
name|colList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
assert|assert
operator|(
name|colList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|!=
literal|null
operator|)
assert|;
name|eval
index|[
name|i
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|colList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|output
operator|=
operator|new
name|Object
index|[
name|eval
operator|.
name|length
index|]
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"SELECT "
operator|+
operator|(
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
operator|)
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|outputObjInspector
operator|=
name|initEvaluatorsAndReturnStruct
argument_list|(
name|eval
argument_list|,
name|conf
operator|.
name|getOutputColumnNames
argument_list|()
argument_list|,
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Just forward the row as is
if|if
condition|(
name|conf
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
name|forward
argument_list|(
name|row
argument_list|,
name|inputObjInspectors
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
return|return;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|eval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|output
index|[
name|i
index|]
operator|=
name|eval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
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
literal|"Error evaluating "
operator|+
name|conf
operator|.
name|getColList
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getExprString
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|forward
argument_list|(
name|output
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|new
name|String
argument_list|(
literal|"SEL"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|SELECT
return|;
block|}
block|}
end_class

end_unit


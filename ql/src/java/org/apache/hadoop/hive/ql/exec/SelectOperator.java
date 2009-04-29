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
name|exprNodeDesc
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
name|selectDesc
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
name|mapred
operator|.
name|Reporter
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
name|selectDesc
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
specifier|transient
name|ObjectInspector
name|outputObjectInspector
decl_stmt|;
name|boolean
name|firstRow
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|exprNodeDesc
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
name|firstRow
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|firstRow
condition|)
block|{
name|firstRow
operator|=
literal|false
expr_stmt|;
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
name|outputObjectInspector
operator|=
name|initEvaluatorsAndReturnStruct
argument_list|(
name|eval
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
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
name|forward
argument_list|(
name|output
argument_list|,
name|outputObjectInspector
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
block|}
end_class

end_unit


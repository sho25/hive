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
name|*
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
name|conf
operator|.
name|Configuration
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
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
try|try
block|{
name|eval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|conf
operator|.
name|getColList
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|exprNodeDesc
name|e
range|:
name|conf
operator|.
name|getColList
argument_list|()
control|)
block|{
name|eval
index|[
name|i
operator|++
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|process
parameter_list|(
name|HiveObject
name|r
parameter_list|)
throws|throws
name|HiveException
block|{
name|CompositeHiveObject
name|nr
init|=
operator|new
name|CompositeHiveObject
argument_list|(
name|eval
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|ExprNodeEvaluator
name|e
range|:
name|eval
control|)
block|{
name|HiveObject
name|ho
init|=
name|e
operator|.
name|evaluate
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|nr
operator|.
name|addHiveObject
argument_list|(
name|ho
argument_list|)
expr_stmt|;
block|}
name|forward
argument_list|(
name|nr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


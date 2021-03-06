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
name|vector
operator|.
name|util
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
name|java
operator|.
name|util
operator|.
name|List
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
name|CompilationOpContext
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
name|Operator
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
name|OperatorDesc
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

begin_comment
comment|/**  * Operator that captures output emitted by parent.  * Used in unit test only.  */
end_comment

begin_class
specifier|public
class|class
name|FakeCaptureOutputOperator
extends|extends
name|Operator
argument_list|<
name|FakeCaptureOutputDesc
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
specifier|public
interface|interface
name|OutputInspector
block|{
specifier|public
name|void
name|inspectRow
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
specifier|private
name|OutputInspector
name|outputInspector
decl_stmt|;
specifier|public
name|void
name|setOutputInspector
parameter_list|(
name|OutputInspector
name|outputInspector
parameter_list|)
block|{
name|this
operator|.
name|outputInspector
operator|=
name|outputInspector
expr_stmt|;
block|}
specifier|public
name|OutputInspector
name|getOutputInspector
parameter_list|()
block|{
return|return
name|outputInspector
return|;
block|}
specifier|private
specifier|transient
name|List
argument_list|<
name|Object
argument_list|>
name|rows
decl_stmt|;
specifier|public
specifier|static
name|FakeCaptureOutputOperator
name|addCaptureOutputChild
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
name|FakeCaptureOutputOperator
name|out
init|=
operator|new
name|FakeCaptureOutputOperator
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|listParents
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|listParents
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
name|out
operator|.
name|setParentOperators
argument_list|(
name|listParents
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|listChildren
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|listChildren
operator|.
name|add
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|op
operator|.
name|setChildOperators
argument_list|(
name|listChildren
argument_list|)
expr_stmt|;
return|return
name|out
return|;
block|}
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getCapturedRows
parameter_list|()
block|{
return|return
name|rows
return|;
block|}
comment|/** Kryo ctor. */
specifier|protected
name|FakeCaptureOutputOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|FakeCaptureOutputOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rows
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
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
name|rows
operator|.
name|add
argument_list|(
name|row
argument_list|)
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|outputInspector
condition|)
block|{
name|outputInspector
operator|.
name|inspectRow
argument_list|(
name|row
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|FakeCaptureOutputOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"FAKE_CAPTURE"
return|;
block|}
block|}
end_class

end_unit


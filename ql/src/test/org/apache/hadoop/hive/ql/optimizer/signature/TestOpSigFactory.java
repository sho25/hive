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
name|optimizer
operator|.
name|signature
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
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
name|AbstractOperatorDesc
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
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Spy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|junit
operator|.
name|MockitoJUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|junit
operator|.
name|MockitoRule
import|;
end_import

begin_class
specifier|public
class|class
name|TestOpSigFactory
block|{
name|CompilationOpContext
name|cCtx
init|=
operator|new
name|CompilationOpContext
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|MockitoRule
name|a
init|=
name|MockitoJUnit
operator|.
name|rule
argument_list|()
decl_stmt|;
annotation|@
name|Spy
name|OpTreeSignatureFactory
name|f
init|=
name|OpTreeSignatureFactory
operator|.
name|newCache
argument_list|()
decl_stmt|;
specifier|public
specifier|static
class|class
name|SampleDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|int
name|desc_invocations
decl_stmt|;
annotation|@
name|Signature
specifier|public
name|int
name|asd
parameter_list|()
block|{
name|desc_invocations
operator|++
expr_stmt|;
return|return
literal|8
return|;
block|}
specifier|public
name|int
name|getDesc_invocations
parameter_list|()
block|{
return|return
name|desc_invocations
return|;
block|}
block|}
specifier|static
class|class
name|SampleOperator
extends|extends
name|Operator
argument_list|<
name|SampleDesc
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
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
block|{     }
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"A1"
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|FILTER
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|checkExplicit
parameter_list|()
block|{
name|SampleOperator
name|so
init|=
operator|new
name|SampleOperator
argument_list|()
decl_stmt|;
name|SampleDesc
name|sd
init|=
operator|new
name|SampleDesc
argument_list|()
decl_stmt|;
name|so
operator|.
name|setConf
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|f
operator|.
name|getSignature
argument_list|(
name|so
argument_list|)
expr_stmt|;
name|f
operator|.
name|getSignature
argument_list|(
name|so
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|f
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|getSignature
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd
operator|.
name|getDesc_invocations
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|checkImplicit
parameter_list|()
block|{
name|SampleOperator
name|so
init|=
operator|new
name|SampleOperator
argument_list|()
decl_stmt|;
name|SampleDesc
name|sd
init|=
operator|new
name|SampleDesc
argument_list|()
decl_stmt|;
name|so
operator|.
name|setConf
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|SampleOperator
name|so2
init|=
operator|new
name|SampleOperator
argument_list|()
decl_stmt|;
name|SampleDesc
name|sd2
init|=
operator|new
name|SampleDesc
argument_list|()
decl_stmt|;
name|so2
operator|.
name|setConf
argument_list|(
name|sd2
argument_list|)
expr_stmt|;
name|so
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|so2
argument_list|)
expr_stmt|;
name|so2
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|so
argument_list|)
expr_stmt|;
name|f
operator|.
name|getSignature
argument_list|(
name|so
argument_list|)
expr_stmt|;
comment|// computes the sig of every object
name|verify
argument_list|(
name|f
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|getSignature
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd
operator|.
name|getDesc_invocations
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd2
operator|.
name|getDesc_invocations
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|.
name|getSignature
argument_list|(
name|so
argument_list|)
expr_stmt|;
name|f
operator|.
name|getSignature
argument_list|(
name|so2
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|f
argument_list|,
name|times
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|getSignature
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd
operator|.
name|getDesc_invocations
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sd2
operator|.
name|getDesc_invocations
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


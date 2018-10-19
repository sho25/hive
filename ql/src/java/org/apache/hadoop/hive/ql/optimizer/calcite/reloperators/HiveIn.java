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
name|calcite
operator|.
name|reloperators
package|;
end_package

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
name|calcite
operator|.
name|sql
operator|.
name|SqlCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlKind
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlSpecialOperator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlWriter
operator|.
name|Frame
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlWriter
operator|.
name|FrameTypeEnum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|InferTypes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|ReturnTypes
import|;
end_import

begin_class
specifier|public
class|class
name|HiveIn
extends|extends
name|SqlSpecialOperator
block|{
specifier|public
specifier|static
specifier|final
name|SqlSpecialOperator
name|INSTANCE
init|=
operator|new
name|HiveIn
argument_list|()
decl_stmt|;
specifier|private
name|HiveIn
parameter_list|()
block|{
name|super
argument_list|(
literal|"IN"
argument_list|,
name|SqlKind
operator|.
name|IN
argument_list|,
literal|30
argument_list|,
literal|true
argument_list|,
name|ReturnTypes
operator|.
name|BOOLEAN_NULLABLE
argument_list|,
name|InferTypes
operator|.
name|FIRST_KNOWN
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unparse
parameter_list|(
name|SqlWriter
name|writer
parameter_list|,
name|SqlCall
name|call
parameter_list|,
name|int
name|leftPrec
parameter_list|,
name|int
name|rightPrec
parameter_list|)
block|{
name|List
argument_list|<
name|SqlNode
argument_list|>
name|opList
init|=
name|call
operator|.
name|getOperandList
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|opList
operator|.
name|size
argument_list|()
operator|>=
literal|1
operator|)
assert|;
name|SqlNode
name|sqlNode
init|=
name|opList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sqlNode
operator|.
name|unparse
argument_list|(
name|writer
argument_list|,
name|leftPrec
argument_list|,
name|getLeftPrec
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|sep
argument_list|(
literal|"IN"
argument_list|)
expr_stmt|;
name|Frame
name|frame
init|=
name|writer
operator|.
name|startList
argument_list|(
name|FrameTypeEnum
operator|.
name|SETOP
argument_list|,
literal|"("
argument_list|,
literal|")"
argument_list|)
decl_stmt|;
for|for
control|(
name|SqlNode
name|op
range|:
name|opList
operator|.
name|subList
argument_list|(
literal|1
argument_list|,
name|opList
operator|.
name|size
argument_list|()
argument_list|)
control|)
block|{
name|writer
operator|.
name|sep
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|op
operator|.
name|unparse
argument_list|(
name|writer
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|endList
argument_list|(
name|frame
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|jdbc
operator|.
name|JavaTypeFactoryImpl
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
name|rel
operator|.
name|type
operator|.
name|RelDataTypeFactory
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
name|rex
operator|.
name|RexBuilder
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
name|rex
operator|.
name|RexNode
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
name|rex
operator|.
name|RexUtil
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
name|fun
operator|.
name|SqlStdOperatorTable
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
name|SqlTypeName
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

begin_class
specifier|public
class|class
name|TestCBOMaxNumToCNF
block|{
specifier|final
name|int
name|maxNumNodesCNF
init|=
literal|8
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCBOMaxNumToCNF1
parameter_list|()
block|{
comment|// OR(=($0, 1), AND(=($0, 0), =($1, 8)))
comment|// transformation creates 7 nodes AND(OR(=($0, 1), =($0, 0)), OR(=($0, 1), =($1, 8)))
comment|// thus, it is triggered
specifier|final
name|RelDataTypeFactory
name|typeFactory
init|=
operator|new
name|JavaTypeFactoryImpl
argument_list|()
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
operator|new
name|RexBuilder
argument_list|(
name|typeFactory
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|cond
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|OR
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|1
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|AND
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|0
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|8
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|newCond
init|=
name|RexUtil
operator|.
name|toCnf
argument_list|(
name|rexBuilder
argument_list|,
name|maxNumNodesCNF
argument_list|,
name|cond
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|newCond
operator|.
name|toString
argument_list|()
argument_list|,
literal|"AND(OR(=($0, 1), =($0, 0)), OR(=($0, 1), =($1, 8)))"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCBOMaxNumToCNF2
parameter_list|()
block|{
comment|// OR(=($0, 1), =($0, 2), AND(=($0, 0), =($1, 8)))
comment|// transformation creates 9 nodes AND(OR(=($0, 1), =($0, 2), =($0, 0)), OR(=($0, 1), =($0, 2), =($1, 8)))
comment|// thus, it is NOT triggered
specifier|final
name|RelDataTypeFactory
name|typeFactory
init|=
operator|new
name|JavaTypeFactoryImpl
argument_list|()
decl_stmt|;
specifier|final
name|RexBuilder
name|rexBuilder
init|=
operator|new
name|RexBuilder
argument_list|(
name|typeFactory
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|cond
init|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|OR
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|1
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|2
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|AND
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|0
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|,
name|rexBuilder
operator|.
name|makeLiteral
argument_list|(
literal|8
argument_list|,
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|INTEGER
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|RexNode
name|newCond
init|=
name|RexUtil
operator|.
name|toCnf
argument_list|(
name|rexBuilder
argument_list|,
name|maxNumNodesCNF
argument_list|,
name|cond
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|newCond
operator|.
name|toString
argument_list|()
argument_list|,
literal|"OR(=($0, 1), =($0, 2), AND(=($0, 0), =($1, 8)))"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|plan
package|;
end_package

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
name|junit
operator|.
name|Assert
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
name|ddl
operator|.
name|function
operator|.
name|macro
operator|.
name|create
operator|.
name|CreateMacroDesc
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|TestCreateMacroDesc
block|{
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|colTypes
decl_stmt|;
specifier|private
name|ExprNodeConstantDesc
name|bodyDesc
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|name
operator|=
literal|"fixed_number"
expr_stmt|;
name|colNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|colTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
name|colNames
operator|.
name|add
argument_list|(
literal|"x"
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
expr_stmt|;
name|colNames
operator|.
name|add
argument_list|(
literal|"y"
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
expr_stmt|;
name|bodyDesc
operator|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreateMacroDesc
parameter_list|()
throws|throws
name|Exception
block|{
name|CreateMacroDesc
name|desc
init|=
operator|new
name|CreateMacroDesc
argument_list|(
name|name
argument_list|,
name|colNames
argument_list|,
name|colTypes
argument_list|,
name|bodyDesc
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|name
argument_list|,
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|bodyDesc
argument_list|,
name|desc
operator|.
name|getBody
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|colNames
argument_list|,
name|desc
operator|.
name|getColumnNames
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|colTypes
argument_list|,
name|desc
operator|.
name|getColumnTypes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


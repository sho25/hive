begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2015 The Apache Software Foundation.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ppr
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|GenericUDFOPAnd
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
name|GenericUDFOPNull
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
name|GenericUDFOPOr
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
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
specifier|final
class|class
name|TestPositivePartitionPrunerCompactExpr
block|{
specifier|private
specifier|final
name|ExprNodeDesc
name|expression
decl_stmt|;
specifier|private
specifier|final
name|String
name|expected
decl_stmt|;
specifier|public
name|TestPositivePartitionPrunerCompactExpr
parameter_list|(
name|ExprNodeDesc
name|expression
parameter_list|,
name|String
name|expected
parameter_list|)
block|{
name|this
operator|.
name|expression
operator|=
name|expression
expr_stmt|;
name|this
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
block|}
annotation|@
name|Parameterized
operator|.
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: {0} => {1}"
argument_list|)
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|ExprNodeDesc
name|trueExpr
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|falseExpr
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|col1Expr
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
literal|"col1"
argument_list|,
literal|"t1"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|col2Expr
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
literal|"col2"
argument_list|,
literal|"t1"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|udf1Expr
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
operator|new
name|GenericUDFOPNull
argument_list|()
argument_list|,
name|Arrays
operator|.
expr|<
name|ExprNodeDesc
operator|>
name|asList
argument_list|(
name|col1Expr
argument_list|)
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|udf2Expr
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
operator|new
name|GenericUDFOPNull
argument_list|()
argument_list|,
name|Arrays
operator|.
expr|<
name|ExprNodeDesc
operator|>
name|asList
argument_list|(
name|col2Expr
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
literal|null
block|,
literal|null
block|}
block|,
block|{
name|and
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|and
argument_list|(
name|falseExpr
argument_list|,
literal|null
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
literal|null
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
name|trueExpr
argument_list|,
literal|null
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|and
argument_list|(
literal|null
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|and
argument_list|(
name|udf1Expr
argument_list|,
literal|null
argument_list|)
block|,
literal|"col1 is null"
block|}
block|,
block|{
name|and
argument_list|(
literal|null
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"col2 is null"
block|}
block|,
block|{
name|and
argument_list|(
name|udf1Expr
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"(col1 is null and col2 is null)"
block|}
block|,
block|{
name|and
argument_list|(
name|falseExpr
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
name|trueExpr
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
name|falseExpr
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
name|udf1Expr
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
name|falseExpr
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|and
argument_list|(
name|trueExpr
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|and
argument_list|(
name|udf1Expr
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"col1 is null"
block|}
block|,
block|{
name|and
argument_list|(
name|trueExpr
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"col2 is null"
block|}
block|,
block|{
name|or
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|or
argument_list|(
name|falseExpr
argument_list|,
literal|null
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|or
argument_list|(
literal|null
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|or
argument_list|(
name|trueExpr
argument_list|,
literal|null
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
literal|null
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
name|udf1Expr
argument_list|,
literal|null
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|or
argument_list|(
literal|null
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|null
block|}
block|,
block|{
name|or
argument_list|(
name|udf1Expr
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"(col1 is null or col2 is null)"
block|}
block|,
block|{
name|or
argument_list|(
name|falseExpr
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"false"
block|}
block|,
block|{
name|or
argument_list|(
name|trueExpr
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
name|falseExpr
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
name|udf1Expr
argument_list|,
name|falseExpr
argument_list|)
block|,
literal|"col1 is null"
block|}
block|,
block|{
name|or
argument_list|(
name|falseExpr
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"col2 is null"
block|}
block|,
block|{
name|or
argument_list|(
name|trueExpr
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
name|udf1Expr
argument_list|,
name|trueExpr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
name|trueExpr
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"true"
block|}
block|,
block|{
name|or
argument_list|(
name|and
argument_list|(
name|udf1Expr
argument_list|,
name|udf2Expr
argument_list|)
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"((col1 is null and col2 is null) or col2 is null)"
block|}
block|,
block|{
name|and
argument_list|(
name|or
argument_list|(
name|udf1Expr
argument_list|,
name|udf2Expr
argument_list|)
argument_list|,
name|udf2Expr
argument_list|)
block|,
literal|"((col1 is null or col2 is null) and col2 is null)"
block|}
block|,     }
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompactExpr
parameter_list|()
block|{
name|ExprNodeDesc
name|actual
init|=
name|PartitionPruner
operator|.
name|compactExpr
argument_list|(
name|expression
argument_list|)
decl_stmt|;
if|if
condition|(
name|expected
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|actual
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
literal|"Expected not NULL expression"
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Expected not NULL expression string"
argument_list|,
name|actual
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|ExprNodeDesc
name|or
parameter_list|(
name|ExprNodeDesc
name|left
parameter_list|,
name|ExprNodeDesc
name|right
parameter_list|)
block|{
return|return
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
operator|new
name|GenericUDFOPOr
argument_list|()
argument_list|,
name|Arrays
operator|.
expr|<
name|ExprNodeDesc
operator|>
name|asList
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ExprNodeDesc
name|and
parameter_list|(
name|ExprNodeDesc
name|left
parameter_list|,
name|ExprNodeDesc
name|right
parameter_list|)
block|{
return|return
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
operator|new
name|GenericUDFOPAnd
argument_list|()
argument_list|,
name|Arrays
operator|.
expr|<
name|ExprNodeDesc
operator|>
name|asList
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit


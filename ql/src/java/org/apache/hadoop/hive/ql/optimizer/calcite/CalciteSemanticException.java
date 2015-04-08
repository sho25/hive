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
name|optimizer
operator|.
name|calcite
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
name|ErrorMsg
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
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Exception from SemanticAnalyzer.  */
end_comment

begin_class
specifier|public
class|class
name|CalciteSemanticException
extends|extends
name|SemanticException
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
enum|enum
name|UnsupportedFeature
block|{
name|Distinct_without_an_aggreggation
block|,
name|Duplicates_in_RR
block|,
name|Filter_expression_with_non_boolean_return_type
block|,
name|Having_clause_without_any_groupby
block|,
name|Hint
block|,
name|Invalid_column_reference
block|,
name|Invalid_decimal
block|,
name|Less_than_equal_greater_than
block|,
name|Multi_insert
block|,
name|Others
block|,
name|Same_name_in_multiple_expressions
block|,
name|Schema_less_table
block|,
name|Select_alias_in_having_clause
block|,
name|Select_transform
block|,
name|Subquery
block|,
name|Table_sample_clauses
block|,
name|UDTF
block|,
name|Union_type
block|,
name|Unique_join
block|}
empty_stmt|;
specifier|private
name|UnsupportedFeature
name|unsupportedFeature
decl_stmt|;
specifier|public
name|CalciteSemanticException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CalciteSemanticException
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CalciteSemanticException
parameter_list|(
name|String
name|message
parameter_list|,
name|UnsupportedFeature
name|feature
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|this
operator|.
name|setUnsupportedFeature
argument_list|(
name|feature
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CalciteSemanticException
parameter_list|(
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CalciteSemanticException
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CalciteSemanticException
parameter_list|(
name|ErrorMsg
name|errorMsg
parameter_list|,
name|String
modifier|...
name|msgArgs
parameter_list|)
block|{
name|super
argument_list|(
name|errorMsg
argument_list|,
name|msgArgs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|UnsupportedFeature
name|getUnsupportedFeature
parameter_list|()
block|{
return|return
name|unsupportedFeature
return|;
block|}
specifier|public
name|void
name|setUnsupportedFeature
parameter_list|(
name|UnsupportedFeature
name|unsupportedFeature
parameter_list|)
block|{
name|this
operator|.
name|unsupportedFeature
operator|=
name|unsupportedFeature
expr_stmt|;
block|}
block|}
end_class

end_unit


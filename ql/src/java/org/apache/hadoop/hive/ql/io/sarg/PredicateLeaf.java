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
name|io
operator|.
name|sarg
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

begin_comment
comment|/**  * The primitive predicates that form a SearchArgument.  */
end_comment

begin_interface
specifier|public
interface|interface
name|PredicateLeaf
block|{
comment|/**    * The possible operators for predicates. To get the opposites, construct    * an expression with a not operator.    */
specifier|public
specifier|static
enum|enum
name|Operator
block|{
name|EQUALS
block|,
name|NULL_SAFE_EQUALS
block|,
name|LESS_THAN
block|,
name|LESS_THAN_EQUALS
block|,
name|IN
block|,
name|BETWEEN
block|,
name|IS_NULL
block|}
comment|/**    * The possible types for sargs.    */
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|INTEGER
block|,
comment|// all of the integer types
name|FLOAT
block|,
comment|// float and double
name|STRING
block|}
comment|/**    * Get the operator for the leaf.    */
specifier|public
name|Operator
name|getOperator
parameter_list|()
function_decl|;
comment|/**    * Get the type of the column and literal.    */
specifier|public
name|Type
name|getType
parameter_list|()
function_decl|;
comment|/**    * Get the simple column name.    * @return the column name    */
specifier|public
name|String
name|getColumnName
parameter_list|()
function_decl|;
comment|/**    * Get the literal half of the predicate leaf.    * @return a Long, Double, or String    */
specifier|public
name|Object
name|getLiteral
parameter_list|()
function_decl|;
comment|/**    * For operators with multiple literals (IN and BETWEEN), get the literals.    * @return the list of literals (Longs, Doubles, or Strings)    */
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getLiteralList
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


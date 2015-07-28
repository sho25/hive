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
name|io
operator|.
name|HiveDecimalWritable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
parameter_list|(
name|Integer
operator|.
name|class
parameter_list|)
operator|,
comment|// all of the integer types except long
constructor|LONG(Long.class
block|)
enum|,
name|FLOAT
parameter_list|(
name|Double
operator|.
name|class
parameter_list|)
operator|,
comment|// float and double
constructor|STRING(String.class
block|)
operator|,
comment|// string, char, varchar
name|DATE
argument_list|(
name|Date
operator|.
name|class
argument_list|)
operator|,
name|DECIMAL
argument_list|(
name|HiveDecimalWritable
operator|.
name|class
argument_list|)
operator|,
name|TIMESTAMP
argument_list|(
name|Timestamp
operator|.
name|class
argument_list|)
operator|,
name|BOOLEAN
argument_list|(
name|Boolean
operator|.
name|class
argument_list|)
expr_stmt|;
end_interface

begin_decl_stmt
specifier|private
specifier|final
name|Class
name|cls
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|Type
argument_list|(
name|Class
name|cls
argument_list|)
block|{
name|this
operator|.
name|cls
operator|=
name|cls
block|;     }
comment|/**      * For all SARG leaves, the values must be the matching class.      * @return the value class      */
specifier|public
name|Class
name|getValueClass
argument_list|()
block|{
return|return
name|cls
return|;
block|}
end_expr_stmt

begin_comment
unit|}
comment|/**    * Get the operator for the leaf.    */
end_comment

begin_function_decl
unit|public
name|Operator
name|getOperator
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**    * Get the type of the column and literal by the file format.    */
end_comment

begin_function_decl
specifier|public
name|Type
name|getType
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**    * Get the simple column name.    * @return the column name    */
end_comment

begin_function_decl
specifier|public
name|String
name|getColumnName
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**    * Get the literal half of the predicate leaf. Adapt the original type for what orc needs    *    * @return an Integer, Long, Double, or String    */
end_comment

begin_function_decl
specifier|public
name|Object
name|getLiteral
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**    * For operators with multiple literals (IN and BETWEEN), get the literals.    *    * @return the list of literals (Integer, Longs, Doubles, or Strings)    *    */
end_comment

begin_function_decl
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getLiteralList
parameter_list|()
function_decl|;
end_function_decl

unit|}
end_unit


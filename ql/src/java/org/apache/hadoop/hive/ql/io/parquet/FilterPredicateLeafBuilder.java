begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|parquet
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|sarg
operator|.
name|PredicateLeaf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterApi
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterPredicate
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterApi
operator|.
name|not
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterApi
operator|.
name|or
import|;
end_import

begin_comment
comment|/**  * The base class for building parquet supported filter predicate in primary types.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|FilterPredicateLeafBuilder
block|{
comment|/**    * Build filter predicate with multiple constants    *    * @param op         IN or BETWEEN    * @param literals    * @param columnName    * @return    */
specifier|public
name|FilterPredicate
name|buildPredicate
parameter_list|(
name|PredicateLeaf
operator|.
name|Operator
name|op
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|literals
parameter_list|,
name|String
name|columnName
parameter_list|)
throws|throws
name|Exception
block|{
name|FilterPredicate
name|result
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|op
condition|)
block|{
case|case
name|IN
case|:
for|for
control|(
name|Object
name|literal
range|:
name|literals
control|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
name|buildPredict
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|literal
argument_list|,
name|columnName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|or
argument_list|(
name|result
argument_list|,
name|buildPredict
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|literal
argument_list|,
name|columnName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
case|case
name|BETWEEN
case|:
if|if
condition|(
name|literals
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not able to build 'between' operation filter with "
operator|+
name|literals
operator|+
literal|" which needs two literals"
argument_list|)
throw|;
block|}
name|Object
name|min
init|=
name|literals
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Object
name|max
init|=
name|literals
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|FilterPredicate
name|lt
init|=
name|not
argument_list|(
name|buildPredict
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN_EQUALS
argument_list|,
name|min
argument_list|,
name|columnName
argument_list|)
argument_list|)
decl_stmt|;
name|FilterPredicate
name|gt
init|=
name|buildPredict
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|max
argument_list|,
name|columnName
argument_list|)
decl_stmt|;
name|result
operator|=
name|FilterApi
operator|.
name|and
argument_list|(
name|gt
argument_list|,
name|lt
argument_list|)
expr_stmt|;
return|return
name|result
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown PredicateLeaf Operator type: "
operator|+
name|op
argument_list|)
throw|;
block|}
block|}
comment|/**    * Build predicate with a single constant    *    * @param op         EQUALS, NULL_SAFE_EQUALS, LESS_THAN, LESS_THAN_EQUALS, IS_NULL    * @param constant    * @param columnName    * @return null or a FilterPredicate, null means no filter will be executed    */
specifier|public
specifier|abstract
name|FilterPredicate
name|buildPredict
parameter_list|(
name|PredicateLeaf
operator|.
name|Operator
name|op
parameter_list|,
name|Object
name|constant
parameter_list|,
name|String
name|columnName
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
end_class

end_unit


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
name|parse
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
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|CommonTree
import|;
end_import

begin_comment
comment|/**  *   * This class stores all the information specified in the TABLESAMPLE clause. e.g.   * for the clause "FROM t TABLESAMPLE(1 OUT OF 2 ON c1) it will store the numerator  * 1, the denominator 2 and the list of expressions(in this case c1) in the appropriate  * fields. The afore-mentioned sampling clause causes the 1st bucket to be picked out of  * the 2 buckets created by hashing on c1.  *  */
end_comment

begin_class
specifier|public
class|class
name|TableSample
block|{
comment|/**    * The numerator of the TABLESAMPLE clause    */
specifier|private
name|int
name|numerator
decl_stmt|;
comment|/**    * The denominator of the TABLESAMPLE clause    */
specifier|private
name|int
name|denominator
decl_stmt|;
comment|/**    * The list of expressions following ON part of the TABLESAMPLE clause. This list is    * empty in case there are no expressions such as in the clause    * "FROM t TABLESAMPLE(1 OUT OF 2)". For this expression the sampling is done    * on the tables clustering column(as specified when the table was created). In case    * the table does not have any clustering column, the usage of a table sample clause    * without an ON part is disallowed by the compiler    */
specifier|private
name|ArrayList
argument_list|<
name|CommonTree
argument_list|>
name|exprs
decl_stmt|;
comment|/**    * Flag to indicate that input files can be pruned    */
specifier|private
name|boolean
name|inputPruning
decl_stmt|;
comment|/**    * Constructs the TableSample given the numerator, denominator and the list of    * ON clause expressions    *     * @param num The numerator    * @param den The denominator    * @param exprs The list of expressions in the ON part of the TABLESAMPLE clause    */
specifier|public
name|TableSample
parameter_list|(
name|String
name|num
parameter_list|,
name|String
name|den
parameter_list|,
name|ArrayList
argument_list|<
name|CommonTree
argument_list|>
name|exprs
parameter_list|)
block|{
name|this
operator|.
name|numerator
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|num
argument_list|)
operator|.
name|intValue
argument_list|()
expr_stmt|;
name|this
operator|.
name|denominator
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|den
argument_list|)
operator|.
name|intValue
argument_list|()
expr_stmt|;
name|this
operator|.
name|exprs
operator|=
name|exprs
expr_stmt|;
block|}
comment|/**    * Gets the numerator    *     * @return int    */
specifier|public
name|int
name|getNumerator
parameter_list|()
block|{
return|return
name|this
operator|.
name|numerator
return|;
block|}
comment|/**    * Sets the numerator    *     * @param num The numerator    */
specifier|public
name|void
name|setNumerator
parameter_list|(
name|int
name|num
parameter_list|)
block|{
name|this
operator|.
name|numerator
operator|=
name|num
expr_stmt|;
block|}
comment|/**    * Gets the denominator    *     * @return int    */
specifier|public
name|int
name|getDenominator
parameter_list|()
block|{
return|return
name|this
operator|.
name|denominator
return|;
block|}
comment|/**    * Sets the denominator    *     * @param den The denominator    */
specifier|public
name|void
name|setDenominator
parameter_list|(
name|int
name|den
parameter_list|)
block|{
name|this
operator|.
name|denominator
operator|=
name|den
expr_stmt|;
block|}
comment|/**    * Gets the ON part's expression list    *     * @return ArrayList<CommonTree>    */
specifier|public
name|ArrayList
argument_list|<
name|CommonTree
argument_list|>
name|getExprs
parameter_list|()
block|{
return|return
name|this
operator|.
name|exprs
return|;
block|}
comment|/**    * Sets the expression list    *     * @param exprs The expression list    */
specifier|public
name|void
name|setExprs
parameter_list|(
name|ArrayList
argument_list|<
name|CommonTree
argument_list|>
name|exprs
parameter_list|)
block|{
name|this
operator|.
name|exprs
operator|=
name|exprs
expr_stmt|;
block|}
comment|/**    * Gets the flag that indicates whether input pruning is possible    *     * @return boolean    */
specifier|public
name|boolean
name|getInputPruning
parameter_list|()
block|{
return|return
name|this
operator|.
name|inputPruning
return|;
block|}
comment|/**    * Sets the flag that indicates whether input pruning is possible or not    *     * @param inputPruning true if input pruning is possible    */
specifier|public
name|void
name|setInputPruning
parameter_list|(
name|boolean
name|inputPruning
parameter_list|)
block|{
name|this
operator|.
name|inputPruning
operator|=
name|inputPruning
expr_stmt|;
block|}
block|}
end_class

end_unit


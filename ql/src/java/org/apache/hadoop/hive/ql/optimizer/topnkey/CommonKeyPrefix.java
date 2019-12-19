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
name|topnkey
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
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
name|GroupByDesc
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
name|ReduceSinkDesc
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
name|TopNKeyDesc
import|;
end_import

begin_comment
comment|/**  * Holds result of a common key prefix of two operators.  * Provides factory methods for mapping TopNKey operator keys to GroupBy and ReduceSink operator keys.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|CommonKeyPrefix
block|{
comment|/**    * Factory method to map a {@link org.apache.hadoop.hive.ql.exec.TopNKeyOperator}'s and a    * {@link org.apache.hadoop.hive.ql.exec.GroupByOperator}'s keys.    * This method calls the {@link #map(List, String, String, List, Map, String, String)} method to do the mapping.    * Since the {@link GroupByDesc} does not contains any ordering information {@link TopNKeyDesc} ordering is passed    * for both operators.    * @param topNKeyDesc {@link TopNKeyDesc} contains {@link org.apache.hadoop.hive.ql.exec.TopNKeyOperator} keys.    * @param groupByDesc {@link GroupByDesc} contains {@link org.apache.hadoop.hive.ql.exec.GroupByOperator} keys.    * @return {@link CommonKeyPrefix} object containing common key prefix of the mapped operators.    */
specifier|public
specifier|static
name|CommonKeyPrefix
name|map
parameter_list|(
name|TopNKeyDesc
name|topNKeyDesc
parameter_list|,
name|GroupByDesc
name|groupByDesc
parameter_list|)
block|{
return|return
name|map
argument_list|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|,
name|groupByDesc
operator|.
name|getKeys
argument_list|()
argument_list|,
name|groupByDesc
operator|.
name|getColumnExprMap
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Factory method to map a {@link org.apache.hadoop.hive.ql.exec.TopNKeyOperator}'s and    * a {@link org.apache.hadoop.hive.ql.exec.ReduceSinkOperator}'s keys.    * This method calls the {@link #map(List, String, String, List, Map, String, String)} method to do the mapping.    * @param topNKeyDesc {@link TopNKeyDesc} contains {@link org.apache.hadoop.hive.ql.exec.TopNKeyOperator} keys.    * @param reduceSinkDesc {@link ReduceSinkDesc} contains    *   {@link org.apache.hadoop.hive.ql.exec.ReduceSinkOperator} keys.    * @return {@link CommonKeyPrefix} object containing common key prefix of the mapped operators.    */
specifier|public
specifier|static
name|CommonKeyPrefix
name|map
parameter_list|(
name|TopNKeyDesc
name|topNKeyDesc
parameter_list|,
name|ReduceSinkDesc
name|reduceSinkDesc
parameter_list|)
block|{
return|return
name|map
argument_list|(
name|topNKeyDesc
operator|.
name|getKeyColumns
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getColumnSortOrder
argument_list|()
argument_list|,
name|topNKeyDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getKeyCols
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getColumnExprMap
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getOrder
argument_list|()
argument_list|,
name|reduceSinkDesc
operator|.
name|getNullOrder
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * General factory method to map two operator keys.    * Two keys are considered to be equal    * - if parent operator's<code>parentColExprMap</code> has an entry with the operator key column name    * - and that entry value has the same index as the operator key column index.    * - and both key columns has the same ordering    * - and both key columns has the same null ordering    *    * Ex.: op1: a, b, c, d    *      op2: a, b, e    *      result: a, b    *    *      opKeys: Column[_col0], Column[_col1], Column[_col2], Column[_col3]    *      parentKeys: Column[KEY._col0], Column[KEY._col1], Column[KEY._col4]    *      parentColExprMap: {_col0 -> Column[KEY._col0]}, {_col1 -> Column[KEY._col1]}, {_col4 -> Column[KEY._col4]}    *    * Column ordering and null ordering is given by a string where each character represents a column order/null order.    * Ex.: a ASC NULLS FIRST, b DESC NULLS LAST, c ASC NULLS LAST -> order="+-+", null order="azz"    *    * When<code>parentColExprMap</code> is null this method falls back to    * {@link #map(List, String, String, List, String, String)}.    *    * @param opKeys {@link List} of {@link ExprNodeDesc}. contains the operator's key columns    * @param opOrder operator's key column ordering in {@link String} format    * @param opNullOrder operator's key column null ordering in {@link String} format    * @param parentKeys {@link List} of {@link ExprNodeDesc}. contains the parent operator's key columns    * @param parentColExprMap {@link Map} of {@link String} -> {@link ExprNodeDesc}.    *                                    contains parent operator's key column name {@link ExprNodeDesc} mapping    * @param parentOrder parent operator's key column ordering in {@link String} format    * @param parentNullOrder parent operator's key column null ordering in {@link String} format    * @return {@link CommonKeyPrefix} object containing the common key prefix of the mapped operators.    */
specifier|public
specifier|static
name|CommonKeyPrefix
name|map
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|opKeys
parameter_list|,
name|String
name|opOrder
parameter_list|,
name|String
name|opNullOrder
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parentKeys
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|parentColExprMap
parameter_list|,
name|String
name|parentOrder
parameter_list|,
name|String
name|parentNullOrder
parameter_list|)
block|{
if|if
condition|(
name|parentColExprMap
operator|==
literal|null
condition|)
block|{
return|return
name|map
argument_list|(
name|opKeys
argument_list|,
name|opOrder
argument_list|,
name|opNullOrder
argument_list|,
name|parentKeys
argument_list|,
name|parentOrder
argument_list|,
name|parentNullOrder
argument_list|)
return|;
block|}
name|CommonKeyPrefix
name|commonPrefix
init|=
operator|new
name|CommonKeyPrefix
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|Stream
operator|.
name|of
argument_list|(
name|opKeys
operator|.
name|size
argument_list|()
argument_list|,
name|opOrder
operator|.
name|length
argument_list|()
argument_list|,
name|opNullOrder
operator|.
name|length
argument_list|()
argument_list|,
name|parentKeys
operator|.
name|size
argument_list|()
argument_list|,
name|parentColExprMap
operator|.
name|size
argument_list|()
argument_list|,
name|parentOrder
operator|.
name|length
argument_list|()
argument_list|,
name|parentNullOrder
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|min
argument_list|(
name|Integer
operator|::
name|compareTo
argument_list|)
operator|.
name|orElse
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
operator|++
name|i
control|)
block|{
name|ExprNodeDesc
name|column
init|=
name|opKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|columnName
init|=
name|column
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|parentKey
init|=
name|parentKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentKey
operator|!=
literal|null
operator|&&
name|parentKey
operator|.
name|isSame
argument_list|(
name|parentColExprMap
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
argument_list|)
operator|&&
name|opOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
name|parentOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|&&
name|opNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
name|parentNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|commonPrefix
operator|.
name|add
argument_list|(
name|parentKey
argument_list|,
name|opOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|,
name|opNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|commonPrefix
return|;
block|}
block|}
return|return
name|commonPrefix
return|;
block|}
comment|// General factory method to map two operator keys. Operator's and parent operator's {@link ExprNodeDesc}s are
comment|// compared using the
comment|// {@link ExprNodeDesc.isSame} method.
specifier|public
specifier|static
name|CommonKeyPrefix
name|map
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|opKeys
parameter_list|,
name|String
name|opOrder
parameter_list|,
name|String
name|opNullOrder
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|parentKeys
parameter_list|,
name|String
name|parentOrder
parameter_list|,
name|String
name|parentNullOrder
parameter_list|)
block|{
name|CommonKeyPrefix
name|commonPrefix
init|=
operator|new
name|CommonKeyPrefix
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|Stream
operator|.
name|of
argument_list|(
name|opKeys
operator|.
name|size
argument_list|()
argument_list|,
name|opOrder
operator|.
name|length
argument_list|()
argument_list|,
name|opNullOrder
operator|.
name|length
argument_list|()
argument_list|,
name|parentKeys
operator|.
name|size
argument_list|()
argument_list|,
name|parentOrder
operator|.
name|length
argument_list|()
argument_list|,
name|parentNullOrder
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|min
argument_list|(
name|Integer
operator|::
name|compareTo
argument_list|)
operator|.
name|orElse
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
operator|++
name|i
control|)
block|{
name|ExprNodeDesc
name|opKey
init|=
name|opKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|parentKey
init|=
name|parentKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|opKey
operator|!=
literal|null
operator|&&
name|opKey
operator|.
name|isSame
argument_list|(
name|parentKey
argument_list|)
operator|&&
name|opOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
name|parentOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|&&
name|opNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
name|parentNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|commonPrefix
operator|.
name|add
argument_list|(
name|parentKey
argument_list|,
name|opOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|,
name|opNullOrder
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|commonPrefix
return|;
block|}
block|}
return|return
name|commonPrefix
return|;
block|}
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|mappedColumns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|StringBuilder
name|mappedOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|private
name|StringBuilder
name|mappedNullOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
specifier|private
name|CommonKeyPrefix
parameter_list|()
block|{   }
specifier|public
name|void
name|add
parameter_list|(
name|ExprNodeDesc
name|column
parameter_list|,
name|char
name|order
parameter_list|,
name|char
name|nullOrder
parameter_list|)
block|{
name|mappedColumns
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|mappedOrder
operator|.
name|append
argument_list|(
name|order
argument_list|)
expr_stmt|;
name|mappedNullOrder
operator|.
name|append
argument_list|(
name|nullOrder
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|mappedColumns
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getMappedColumns
parameter_list|()
block|{
return|return
name|mappedColumns
return|;
block|}
specifier|public
name|String
name|getMappedOrder
parameter_list|()
block|{
return|return
name|mappedOrder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getMappedNullOrder
parameter_list|()
block|{
return|return
name|mappedNullOrder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|mappedColumns
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit


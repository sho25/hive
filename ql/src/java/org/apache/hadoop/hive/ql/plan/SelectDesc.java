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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * SelectDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Select Operator"
argument_list|)
specifier|public
class|class
name|SelectDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
argument_list|>
name|colList
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
decl_stmt|;
specifier|private
name|boolean
name|selectStar
decl_stmt|;
specifier|private
name|boolean
name|selStarNoCompute
decl_stmt|;
specifier|public
name|SelectDesc
parameter_list|()
block|{   }
specifier|public
name|SelectDesc
parameter_list|(
specifier|final
name|boolean
name|selStarNoCompute
parameter_list|)
block|{
name|this
operator|.
name|selStarNoCompute
operator|=
name|selStarNoCompute
expr_stmt|;
block|}
specifier|public
name|SelectDesc
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
argument_list|>
name|colList
parameter_list|,
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
parameter_list|)
block|{
name|this
argument_list|(
name|colList
argument_list|,
name|outputColumnNames
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SelectDesc
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
argument_list|>
name|colList
parameter_list|,
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|boolean
name|selectStar
parameter_list|)
block|{
name|this
operator|.
name|colList
operator|=
name|colList
expr_stmt|;
name|this
operator|.
name|selectStar
operator|=
name|selectStar
expr_stmt|;
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
block|}
specifier|public
name|SelectDesc
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
argument_list|>
name|colList
parameter_list|,
specifier|final
name|boolean
name|selectStar
parameter_list|,
specifier|final
name|boolean
name|selStarNoCompute
parameter_list|)
block|{
name|this
operator|.
name|colList
operator|=
name|colList
expr_stmt|;
name|this
operator|.
name|selectStar
operator|=
name|selectStar
expr_stmt|;
name|this
operator|.
name|selStarNoCompute
operator|=
name|selStarNoCompute
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"expressions"
argument_list|)
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
argument_list|>
name|getColList
parameter_list|()
block|{
return|return
name|colList
return|;
block|}
specifier|public
name|void
name|setColList
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
argument_list|>
name|colList
parameter_list|)
block|{
name|this
operator|.
name|colList
operator|=
name|colList
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"outputColumnNames"
argument_list|)
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|getOutputColumnNames
parameter_list|()
block|{
return|return
name|outputColumnNames
return|;
block|}
specifier|public
name|void
name|setOutputColumnNames
parameter_list|(
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"SELECT * "
argument_list|)
specifier|public
name|String
name|explainNoCompute
parameter_list|()
block|{
if|if
condition|(
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
return|return
literal|"(no compute)"
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * @return the selectStar    */
specifier|public
name|boolean
name|isSelectStar
parameter_list|()
block|{
return|return
name|selectStar
return|;
block|}
comment|/**    * @param selectStar    *          the selectStar to set    */
specifier|public
name|void
name|setSelectStar
parameter_list|(
name|boolean
name|selectStar
parameter_list|)
block|{
name|this
operator|.
name|selectStar
operator|=
name|selectStar
expr_stmt|;
block|}
comment|/**    * @return the selStarNoCompute    */
specifier|public
name|boolean
name|isSelStarNoCompute
parameter_list|()
block|{
return|return
name|selStarNoCompute
return|;
block|}
comment|/**    * @param selStarNoCompute    *          the selStarNoCompute to set    */
specifier|public
name|void
name|setSelStarNoCompute
parameter_list|(
name|boolean
name|selStarNoCompute
parameter_list|)
block|{
name|this
operator|.
name|selStarNoCompute
operator|=
name|selStarNoCompute
expr_stmt|;
block|}
block|}
end_class

end_unit


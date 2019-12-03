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
name|Explain
operator|.
name|Level
import|;
end_import

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
name|Objects
import|;
end_import

begin_comment
comment|/**  * TopNKeyDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Top N Key Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|TopNKeyDesc
extends|extends
name|AbstractOperatorDesc
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
name|int
name|topN
decl_stmt|;
specifier|private
name|String
name|columnSortOrder
decl_stmt|;
specifier|private
name|String
name|nullOrder
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyColumns
decl_stmt|;
specifier|public
name|TopNKeyDesc
parameter_list|()
block|{   }
specifier|public
name|TopNKeyDesc
parameter_list|(
specifier|final
name|int
name|topN
parameter_list|,
specifier|final
name|String
name|columnSortOrder
parameter_list|,
specifier|final
name|String
name|nullOrder
parameter_list|,
specifier|final
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyColumns
parameter_list|)
block|{
name|this
operator|.
name|topN
operator|=
name|topN
expr_stmt|;
name|this
operator|.
name|columnSortOrder
operator|=
name|columnSortOrder
expr_stmt|;
name|this
operator|.
name|nullOrder
operator|=
name|nullOrder
expr_stmt|;
name|this
operator|.
name|keyColumns
operator|=
name|keyColumns
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"top n"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|,
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|int
name|getTopN
parameter_list|()
block|{
return|return
name|topN
return|;
block|}
specifier|public
name|void
name|setTopN
parameter_list|(
name|int
name|topN
parameter_list|)
block|{
name|this
operator|.
name|topN
operator|=
name|topN
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"sort order"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getColumnSortOrder
parameter_list|()
block|{
return|return
name|columnSortOrder
return|;
block|}
specifier|public
name|void
name|setColumnSortOrder
parameter_list|(
name|String
name|columnSortOrder
parameter_list|)
block|{
name|this
operator|.
name|columnSortOrder
operator|=
name|columnSortOrder
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"null sort order"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getNullOrder
parameter_list|()
block|{
return|return
name|nullOrder
return|;
block|}
specifier|public
name|void
name|setNullOrder
parameter_list|(
name|String
name|nullOrder
parameter_list|)
block|{
name|this
operator|.
name|nullOrder
operator|=
name|nullOrder
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"keys"
argument_list|)
specifier|public
name|String
name|getKeyString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|keyColumns
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"keys"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|String
name|getUserLevelExplainKeyString
parameter_list|()
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|keyColumns
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getKeyColumns
parameter_list|()
block|{
return|return
name|keyColumns
return|;
block|}
specifier|public
name|void
name|setKeyColumns
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyColumns
parameter_list|)
block|{
name|this
operator|.
name|keyColumns
operator|=
name|keyColumns
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getKeyColumnNames
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|keyColumn
range|:
name|keyColumns
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|keyColumn
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|OperatorDesc
name|other
parameter_list|)
block|{
if|if
condition|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|TopNKeyDesc
name|otherDesc
init|=
operator|(
name|TopNKeyDesc
operator|)
name|other
decl_stmt|;
return|return
name|getTopN
argument_list|()
operator|==
name|otherDesc
operator|.
name|getTopN
argument_list|()
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|columnSortOrder
argument_list|,
name|otherDesc
operator|.
name|columnSortOrder
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|nullOrder
argument_list|,
name|otherDesc
operator|.
name|nullOrder
argument_list|)
operator|&&
name|ExprNodeDescUtils
operator|.
name|isSame
argument_list|(
name|keyColumns
argument_list|,
name|otherDesc
operator|.
name|keyColumns
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|TopNKeyDesc
name|ret
init|=
operator|new
name|TopNKeyDesc
argument_list|()
decl_stmt|;
name|ret
operator|.
name|setTopN
argument_list|(
name|topN
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setColumnSortOrder
argument_list|(
name|columnSortOrder
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setNullOrder
argument_list|(
name|nullOrder
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setKeyColumns
argument_list|(
name|getKeyColumns
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|getKeyColumns
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
specifier|public
class|class
name|TopNKeyDescExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|TopNKeyDesc
name|topNKeyDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorTopNKeyDesc
name|vectorTopNKeyDesc
decl_stmt|;
specifier|public
name|TopNKeyDescExplainVectorization
parameter_list|(
name|TopNKeyDesc
name|topNKeyDesc
parameter_list|,
name|VectorTopNKeyDesc
name|vectorTopNKeyDesc
parameter_list|)
block|{
name|super
argument_list|(
name|vectorTopNKeyDesc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|topNKeyDesc
operator|=
name|topNKeyDesc
expr_stmt|;
name|this
operator|.
name|vectorTopNKeyDesc
operator|=
name|vectorTopNKeyDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Explain
operator|.
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"keyExpressions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getKeyExpressions
parameter_list|()
block|{
return|return
name|vectorExpressionsToStringList
argument_list|(
name|vectorTopNKeyDesc
operator|.
name|getKeyExpressions
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Explain
operator|.
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"Top N Key Vectorization"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|TopNKeyDescExplainVectorization
name|getTopNKeyVectorization
parameter_list|()
block|{
name|VectorTopNKeyDesc
name|vectorTopNKeyDesc
init|=
operator|(
name|VectorTopNKeyDesc
operator|)
name|getVectorDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|vectorTopNKeyDesc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|TopNKeyDescExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorTopNKeyDesc
argument_list|)
return|;
block|}
block|}
end_class

end_unit


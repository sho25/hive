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
name|exec
operator|.
name|vector
operator|.
name|VectorColumnOutputMapping
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
name|exec
operator|.
name|vector
operator|.
name|VectorColumnSourceMapping
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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

begin_comment
comment|/**  * VectorMapJoinInfo.  *  * A convenience data structure that has information needed to vectorize map join.  *  * It is created by the Vectorizer when it is determining whether it can specialize so the  * information doesn't have to be recreated again and again by the VectorMapJoinOperator's  * constructors and later during execution.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinInfo
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
index|[]
name|bigTableKeyColumnMap
decl_stmt|;
specifier|private
name|String
index|[]
name|bigTableKeyColumnNames
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|bigTableKeyTypeInfos
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|slimmedBigTableKeyExpressions
decl_stmt|;
specifier|private
name|int
index|[]
name|bigTableValueColumnMap
decl_stmt|;
specifier|private
name|String
index|[]
name|bigTableValueColumnNames
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|bigTableValueTypeInfos
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|slimmedBigTableValueExpressions
decl_stmt|;
specifier|private
name|VectorExpression
index|[]
name|bigTableFilterExpressions
decl_stmt|;
specifier|private
name|int
index|[]
name|bigTableRetainColumnMap
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|bigTableRetainTypeInfos
decl_stmt|;
specifier|private
name|int
index|[]
name|nonOuterSmallTableKeyColumnMap
decl_stmt|;
specifier|private
name|TypeInfo
index|[]
name|nonOuterSmallTableKeyTypeInfos
decl_stmt|;
specifier|private
name|VectorColumnOutputMapping
name|outerSmallTableKeyMapping
decl_stmt|;
specifier|private
name|VectorColumnSourceMapping
name|fullOuterSmallTableKeyMapping
decl_stmt|;
specifier|private
name|VectorColumnSourceMapping
name|smallTableValueMapping
decl_stmt|;
specifier|private
name|VectorColumnSourceMapping
name|projectionMapping
decl_stmt|;
specifier|public
name|VectorMapJoinInfo
parameter_list|()
block|{
name|bigTableKeyColumnMap
operator|=
literal|null
expr_stmt|;
name|bigTableKeyColumnNames
operator|=
literal|null
expr_stmt|;
name|bigTableKeyTypeInfos
operator|=
literal|null
expr_stmt|;
name|slimmedBigTableKeyExpressions
operator|=
literal|null
expr_stmt|;
name|bigTableValueColumnMap
operator|=
literal|null
expr_stmt|;
name|bigTableValueColumnNames
operator|=
literal|null
expr_stmt|;
name|bigTableValueTypeInfos
operator|=
literal|null
expr_stmt|;
name|slimmedBigTableValueExpressions
operator|=
literal|null
expr_stmt|;
name|bigTableFilterExpressions
operator|=
literal|null
expr_stmt|;
name|bigTableRetainColumnMap
operator|=
literal|null
expr_stmt|;
name|bigTableRetainTypeInfos
operator|=
literal|null
expr_stmt|;
name|nonOuterSmallTableKeyColumnMap
operator|=
literal|null
expr_stmt|;
name|nonOuterSmallTableKeyTypeInfos
operator|=
literal|null
expr_stmt|;
name|outerSmallTableKeyMapping
operator|=
literal|null
expr_stmt|;
name|fullOuterSmallTableKeyMapping
operator|=
literal|null
expr_stmt|;
name|smallTableValueMapping
operator|=
literal|null
expr_stmt|;
name|projectionMapping
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|int
index|[]
name|getBigTableKeyColumnMap
parameter_list|()
block|{
return|return
name|bigTableKeyColumnMap
return|;
block|}
specifier|public
name|void
name|setBigTableKeyColumnMap
parameter_list|(
name|int
index|[]
name|bigTableKeyColumnMap
parameter_list|)
block|{
name|this
operator|.
name|bigTableKeyColumnMap
operator|=
name|bigTableKeyColumnMap
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getBigTableKeyColumnNames
parameter_list|()
block|{
return|return
name|bigTableKeyColumnNames
return|;
block|}
specifier|public
name|void
name|setBigTableKeyColumnNames
parameter_list|(
name|String
index|[]
name|bigTableKeyColumnNames
parameter_list|)
block|{
name|this
operator|.
name|bigTableKeyColumnNames
operator|=
name|bigTableKeyColumnNames
expr_stmt|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getBigTableKeyTypeInfos
parameter_list|()
block|{
return|return
name|bigTableKeyTypeInfos
return|;
block|}
specifier|public
name|void
name|setBigTableKeyTypeInfos
parameter_list|(
name|TypeInfo
index|[]
name|bigTableKeyTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|bigTableKeyTypeInfos
operator|=
name|bigTableKeyTypeInfos
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getSlimmedBigTableKeyExpressions
parameter_list|()
block|{
return|return
name|slimmedBigTableKeyExpressions
return|;
block|}
specifier|public
name|void
name|setSlimmedBigTableKeyExpressions
parameter_list|(
name|VectorExpression
index|[]
name|slimmedBigTableKeyExpressions
parameter_list|)
block|{
name|this
operator|.
name|slimmedBigTableKeyExpressions
operator|=
name|slimmedBigTableKeyExpressions
expr_stmt|;
block|}
specifier|public
name|int
index|[]
name|getBigTableValueColumnMap
parameter_list|()
block|{
return|return
name|bigTableValueColumnMap
return|;
block|}
specifier|public
name|void
name|setBigTableValueColumnMap
parameter_list|(
name|int
index|[]
name|bigTableValueColumnMap
parameter_list|)
block|{
name|this
operator|.
name|bigTableValueColumnMap
operator|=
name|bigTableValueColumnMap
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getBigTableValueColumnNames
parameter_list|()
block|{
return|return
name|bigTableValueColumnNames
return|;
block|}
specifier|public
name|void
name|setBigTableValueColumnNames
parameter_list|(
name|String
index|[]
name|bigTableValueColumnNames
parameter_list|)
block|{
name|this
operator|.
name|bigTableValueColumnNames
operator|=
name|bigTableValueColumnNames
expr_stmt|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getBigTableValueTypeInfos
parameter_list|()
block|{
return|return
name|bigTableValueTypeInfos
return|;
block|}
specifier|public
name|void
name|setBigTableValueTypeInfos
parameter_list|(
name|TypeInfo
index|[]
name|bigTableValueTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|bigTableValueTypeInfos
operator|=
name|bigTableValueTypeInfos
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getSlimmedBigTableValueExpressions
parameter_list|()
block|{
return|return
name|slimmedBigTableValueExpressions
return|;
block|}
specifier|public
name|void
name|setSlimmedBigTableValueExpressions
parameter_list|(
name|VectorExpression
index|[]
name|slimmedBigTableValueExpressions
parameter_list|)
block|{
name|this
operator|.
name|slimmedBigTableValueExpressions
operator|=
name|slimmedBigTableValueExpressions
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getBigTableFilterExpressions
parameter_list|()
block|{
return|return
name|bigTableFilterExpressions
return|;
block|}
specifier|public
name|void
name|setBigTableFilterExpressions
parameter_list|(
name|VectorExpression
index|[]
name|bigTableFilterExpressions
parameter_list|)
block|{
name|this
operator|.
name|bigTableFilterExpressions
operator|=
name|bigTableFilterExpressions
expr_stmt|;
block|}
specifier|public
name|void
name|setBigTableRetainColumnMap
parameter_list|(
name|int
index|[]
name|bigTableRetainColumnMap
parameter_list|)
block|{
name|this
operator|.
name|bigTableRetainColumnMap
operator|=
name|bigTableRetainColumnMap
expr_stmt|;
block|}
specifier|public
name|int
index|[]
name|getBigTableRetainColumnMap
parameter_list|()
block|{
return|return
name|bigTableRetainColumnMap
return|;
block|}
specifier|public
name|void
name|setBigTableRetainTypeInfos
parameter_list|(
name|TypeInfo
index|[]
name|bigTableRetainTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|bigTableRetainTypeInfos
operator|=
name|bigTableRetainTypeInfos
expr_stmt|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getBigTableRetainTypeInfos
parameter_list|()
block|{
return|return
name|bigTableRetainTypeInfos
return|;
block|}
specifier|public
name|void
name|setNonOuterSmallTableKeyColumnMap
parameter_list|(
name|int
index|[]
name|nonOuterSmallTableKeyColumnMap
parameter_list|)
block|{
name|this
operator|.
name|nonOuterSmallTableKeyColumnMap
operator|=
name|nonOuterSmallTableKeyColumnMap
expr_stmt|;
block|}
specifier|public
name|int
index|[]
name|getNonOuterSmallTableKeyColumnMap
parameter_list|()
block|{
return|return
name|nonOuterSmallTableKeyColumnMap
return|;
block|}
specifier|public
name|void
name|setNonOuterSmallTableKeyTypeInfos
parameter_list|(
name|TypeInfo
index|[]
name|nonOuterSmallTableKeyTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|nonOuterSmallTableKeyTypeInfos
operator|=
name|nonOuterSmallTableKeyTypeInfos
expr_stmt|;
block|}
specifier|public
name|TypeInfo
index|[]
name|getNonOuterSmallTableKeyTypeInfos
parameter_list|()
block|{
return|return
name|nonOuterSmallTableKeyTypeInfos
return|;
block|}
specifier|public
name|void
name|setOuterSmallTableKeyMapping
parameter_list|(
name|VectorColumnOutputMapping
name|outerSmallTableKeyMapping
parameter_list|)
block|{
name|this
operator|.
name|outerSmallTableKeyMapping
operator|=
name|outerSmallTableKeyMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnOutputMapping
name|getOuterSmallTableKeyMapping
parameter_list|()
block|{
return|return
name|outerSmallTableKeyMapping
return|;
block|}
specifier|public
name|void
name|setFullOuterSmallTableKeyMapping
parameter_list|(
name|VectorColumnSourceMapping
name|fullOuterSmallTableKeyMapping
parameter_list|)
block|{
name|this
operator|.
name|fullOuterSmallTableKeyMapping
operator|=
name|fullOuterSmallTableKeyMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnSourceMapping
name|getFullOuterSmallTableKeyMapping
parameter_list|()
block|{
return|return
name|fullOuterSmallTableKeyMapping
return|;
block|}
specifier|public
name|void
name|setSmallTableValueMapping
parameter_list|(
name|VectorColumnSourceMapping
name|smallTableValueMapping
parameter_list|)
block|{
name|this
operator|.
name|smallTableValueMapping
operator|=
name|smallTableValueMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnSourceMapping
name|getSmallTableValueMapping
parameter_list|()
block|{
return|return
name|smallTableValueMapping
return|;
block|}
specifier|public
name|void
name|setProjectionMapping
parameter_list|(
name|VectorColumnSourceMapping
name|projectionMapping
parameter_list|)
block|{
name|this
operator|.
name|projectionMapping
operator|=
name|projectionMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnSourceMapping
name|getProjectionMapping
parameter_list|()
block|{
return|return
name|projectionMapping
return|;
block|}
block|}
end_class

end_unit


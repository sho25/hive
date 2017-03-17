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
name|bigTableKeyExpressions
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
name|bigTableValueExpressions
decl_stmt|;
specifier|private
name|VectorColumnOutputMapping
name|bigTableRetainedMapping
decl_stmt|;
specifier|private
name|VectorColumnOutputMapping
name|bigTableOuterKeyMapping
decl_stmt|;
specifier|private
name|VectorColumnSourceMapping
name|smallTableMapping
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
name|bigTableKeyExpressions
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
name|bigTableValueExpressions
operator|=
literal|null
expr_stmt|;
name|bigTableRetainedMapping
operator|=
literal|null
expr_stmt|;
name|bigTableOuterKeyMapping
operator|=
literal|null
expr_stmt|;
name|smallTableMapping
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
name|getBigTableKeyExpressions
parameter_list|()
block|{
return|return
name|bigTableKeyExpressions
return|;
block|}
specifier|public
name|void
name|setBigTableKeyExpressions
parameter_list|(
name|VectorExpression
index|[]
name|bigTableKeyExpressions
parameter_list|)
block|{
name|this
operator|.
name|bigTableKeyExpressions
operator|=
name|bigTableKeyExpressions
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
name|getBigTableValueExpressions
parameter_list|()
block|{
return|return
name|bigTableValueExpressions
return|;
block|}
specifier|public
name|void
name|setBigTableValueExpressions
parameter_list|(
name|VectorExpression
index|[]
name|bigTableValueExpressions
parameter_list|)
block|{
name|this
operator|.
name|bigTableValueExpressions
operator|=
name|bigTableValueExpressions
expr_stmt|;
block|}
specifier|public
name|void
name|setBigTableRetainedMapping
parameter_list|(
name|VectorColumnOutputMapping
name|bigTableRetainedMapping
parameter_list|)
block|{
name|this
operator|.
name|bigTableRetainedMapping
operator|=
name|bigTableRetainedMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnOutputMapping
name|getBigTableRetainedMapping
parameter_list|()
block|{
return|return
name|bigTableRetainedMapping
return|;
block|}
specifier|public
name|void
name|setBigTableOuterKeyMapping
parameter_list|(
name|VectorColumnOutputMapping
name|bigTableOuterKeyMapping
parameter_list|)
block|{
name|this
operator|.
name|bigTableOuterKeyMapping
operator|=
name|bigTableOuterKeyMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnOutputMapping
name|getBigTableOuterKeyMapping
parameter_list|()
block|{
return|return
name|bigTableOuterKeyMapping
return|;
block|}
specifier|public
name|void
name|setSmallTableMapping
parameter_list|(
name|VectorColumnSourceMapping
name|smallTableMapping
parameter_list|)
block|{
name|this
operator|.
name|smallTableMapping
operator|=
name|smallTableMapping
expr_stmt|;
block|}
specifier|public
name|VectorColumnSourceMapping
name|getSmallTableMapping
parameter_list|()
block|{
return|return
name|smallTableMapping
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


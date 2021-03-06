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
name|ArrayList
import|;
end_import

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
name|exec
operator|.
name|vector
operator|.
name|VectorColumnMapping
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
name|Vectorization
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

begin_class
specifier|public
class|class
name|OperatorExplainVectorization
block|{
specifier|protected
specifier|final
name|VectorDesc
name|vectorDesc
decl_stmt|;
specifier|protected
specifier|final
name|boolean
name|isNative
decl_stmt|;
specifier|public
name|OperatorExplainVectorization
parameter_list|(
name|VectorDesc
name|vectorDesc
parameter_list|,
name|boolean
name|isNative
parameter_list|)
block|{
name|this
operator|.
name|vectorDesc
operator|=
name|vectorDesc
expr_stmt|;
name|this
operator|.
name|isNative
operator|=
name|isNative
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|vectorExpressionsToStringList
parameter_list|(
name|VectorExpression
index|[]
name|vectorExpressions
parameter_list|)
block|{
if|if
condition|(
name|vectorExpressions
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|vecExprList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|vectorExpressions
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|VectorExpression
name|vecExpr
range|:
name|vectorExpressions
control|)
block|{
name|vecExprList
operator|.
name|add
argument_list|(
name|vecExpr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|vecExprList
return|;
block|}
specifier|public
name|String
name|outputColumnsToStringList
parameter_list|(
name|VectorColumnMapping
name|vectorColumnMapping
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|vectorColumnMapping
operator|.
name|getCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
index|[]
name|outputColumns
init|=
name|vectorColumnMapping
operator|.
name|getOutputColumns
argument_list|()
decl_stmt|;
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|outputColumns
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnsAndTypesToStringList
parameter_list|(
name|int
index|[]
name|outputColumns
parameter_list|,
name|TypeInfo
index|[]
name|typeInfos
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|outputColumns
operator|.
name|length
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|size
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
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|outputColumns
index|[
name|i
index|]
operator|+
literal|":"
operator|+
name|typeInfos
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnsAndTypesToStringList
parameter_list|(
name|VectorColumnMapping
name|vectorColumnMapping
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|vectorColumnMapping
operator|.
name|getCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
index|[]
name|outputColumns
init|=
name|vectorColumnMapping
operator|.
name|getOutputColumns
argument_list|()
decl_stmt|;
name|TypeInfo
index|[]
name|typeInfos
init|=
name|vectorColumnMapping
operator|.
name|getTypeInfos
argument_list|()
decl_stmt|;
return|return
name|outputColumnsAndTypesToStringList
argument_list|(
name|outputColumns
argument_list|,
name|typeInfos
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|columnMappingToStringList
parameter_list|(
name|VectorColumnMapping
name|vectorColumnMapping
parameter_list|)
block|{
specifier|final
name|int
name|size
init|=
name|vectorColumnMapping
operator|.
name|getCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
index|[]
name|inputColumns
init|=
name|vectorColumnMapping
operator|.
name|getInputColumns
argument_list|()
decl_stmt|;
name|int
index|[]
name|outputColumns
init|=
name|vectorColumnMapping
operator|.
name|getOutputColumns
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|size
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
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|inputColumns
index|[
name|i
index|]
operator|+
literal|" -> "
operator|+
name|outputColumns
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"className"
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
name|getClassName
parameter_list|()
block|{
return|return
name|vectorDesc
operator|.
name|getVectorOpClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"native"
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
name|boolean
name|getNative
parameter_list|()
block|{
return|return
name|isNative
return|;
block|}
block|}
end_class

end_unit


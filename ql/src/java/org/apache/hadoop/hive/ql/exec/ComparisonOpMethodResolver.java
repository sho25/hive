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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|TypeInfoFactory
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
name|TypeInfoUtils
import|;
end_import

begin_comment
comment|/**  * The class implements the method resolution for operators like (&gt;&lt;&lt;=&gt;= =  *&lt;&gt;). The resolution logic is as follows: 1. If one of the parameters is null,  * then it resolves to evaluate(Double, Double) 2. If both of the parameters are  * of type T, then it resolves to evaluate(T, T) 3. If 1 and 2 fails then it  * resolves to evaluate(Double, Double).  */
end_comment

begin_class
specifier|public
class|class
name|ComparisonOpMethodResolver
implements|implements
name|UDFMethodResolver
block|{
comment|/**    * The udfclass for which resolution is needed.    */
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|udfClass
decl_stmt|;
comment|/**    * Constuctor.    */
specifier|public
name|ComparisonOpMethodResolver
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|udfClass
parameter_list|)
block|{
name|this
operator|.
name|udfClass
operator|=
name|udfClass
expr_stmt|;
block|}
comment|/*    * (non-Javadoc)    *    * @see    * org.apache.hadoop.hive.ql.exec.UDFMethodResolver#getEvalMethod(java.util    * .List)    */
annotation|@
name|Override
specifier|public
name|Method
name|getEvalMethod
parameter_list|(
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|argTypeInfos
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
assert|assert
operator|(
name|argTypeInfos
operator|.
name|size
argument_list|()
operator|==
literal|2
operator|)
assert|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|pTypeInfos
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|argTypeInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|voidTypeInfo
argument_list|)
operator|||
name|argTypeInfos
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|voidTypeInfo
argument_list|)
condition|)
block|{
name|pTypeInfos
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
name|pTypeInfos
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
expr_stmt|;
name|pTypeInfos
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|argTypeInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
operator|&&
name|argTypeInfos
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
condition|)
block|{
name|pTypeInfos
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
name|pTypeInfos
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
expr_stmt|;
name|pTypeInfos
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|argTypeInfos
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|==
name|argTypeInfos
operator|.
name|get
argument_list|(
literal|1
argument_list|)
condition|)
block|{
name|pTypeInfos
operator|=
name|argTypeInfos
expr_stmt|;
block|}
else|else
block|{
name|pTypeInfos
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
name|pTypeInfos
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
expr_stmt|;
name|pTypeInfos
operator|.
name|add
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
expr_stmt|;
block|}
name|Method
name|udfMethod
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Method
argument_list|>
name|evaluateMethods
init|=
operator|new
name|ArrayList
argument_list|<
name|Method
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|m
range|:
name|Arrays
operator|.
name|asList
argument_list|(
name|udfClass
operator|.
name|getMethods
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"evaluate"
argument_list|)
condition|)
block|{
name|evaluateMethods
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|acceptedTypeInfos
init|=
name|TypeInfoUtils
operator|.
name|getParameterTypeInfos
argument_list|(
name|m
argument_list|,
name|pTypeInfos
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|acceptedTypeInfos
operator|==
literal|null
condition|)
block|{
comment|// null means the method does not accept number of arguments passed.
continue|continue;
block|}
name|boolean
name|match
init|=
operator|(
name|acceptedTypeInfos
operator|.
name|size
argument_list|()
operator|==
name|pTypeInfos
operator|.
name|size
argument_list|()
operator|)
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
name|pTypeInfos
operator|.
name|size
argument_list|()
operator|&&
name|match
condition|;
name|i
operator|++
control|)
block|{
name|TypeInfo
name|accepted
init|=
name|acceptedTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|accepted
operator|!=
name|pTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|match
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|match
condition|)
block|{
if|if
condition|(
name|udfMethod
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|AmbiguousMethodException
argument_list|(
name|udfClass
argument_list|,
name|argTypeInfos
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Method
index|[]
block|{
name|udfMethod
block|,
name|m
block|}
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
name|udfMethod
operator|=
name|m
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|udfMethod
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoMatchingMethodException
argument_list|(
name|udfClass
argument_list|,
name|argTypeInfos
argument_list|,
name|evaluateMethods
argument_list|)
throw|;
block|}
return|return
name|udfMethod
return|;
block|}
block|}
end_class

end_unit


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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|PrimitiveTypeInfo
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
name|VarcharTypeInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TCLIServiceConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeQualifierValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeQualifiers
import|;
end_import

begin_comment
comment|/**  * This class holds type qualifier information for a primitive type,  * such as char/varchar length or decimal precision/scale.  */
end_comment

begin_class
specifier|public
class|class
name|TypeQualifiers
block|{
specifier|private
name|Integer
name|characterMaximumLength
decl_stmt|;
specifier|public
name|TypeQualifiers
parameter_list|()
block|{}
specifier|public
name|Integer
name|getCharacterMaximumLength
parameter_list|()
block|{
return|return
name|characterMaximumLength
return|;
block|}
specifier|public
name|void
name|setCharacterMaximumLength
parameter_list|(
name|int
name|characterMaximumLength
parameter_list|)
block|{
name|this
operator|.
name|characterMaximumLength
operator|=
name|characterMaximumLength
expr_stmt|;
block|}
specifier|public
name|TTypeQualifiers
name|toTTypeQualifiers
parameter_list|()
block|{
name|TTypeQualifiers
name|ret
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TTypeQualifierValue
argument_list|>
name|qMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TTypeQualifierValue
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|getCharacterMaximumLength
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|TTypeQualifierValue
name|val
init|=
operator|new
name|TTypeQualifierValue
argument_list|()
decl_stmt|;
name|val
operator|.
name|setI32Value
argument_list|(
name|getCharacterMaximumLength
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|qMap
operator|.
name|put
argument_list|(
name|TCLIServiceConstants
operator|.
name|CHARACTER_MAXIMUM_LENGTH
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|qMap
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|ret
operator|=
operator|new
name|TTypeQualifiers
argument_list|(
name|qMap
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
name|TypeQualifiers
name|fromTTypeQualifiers
parameter_list|(
name|TTypeQualifiers
name|ttq
parameter_list|)
block|{
name|TypeQualifiers
name|ret
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ttq
operator|!=
literal|null
condition|)
block|{
name|ret
operator|=
operator|new
name|TypeQualifiers
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TTypeQualifierValue
argument_list|>
name|tqMap
init|=
name|ttq
operator|.
name|getQualifiers
argument_list|()
decl_stmt|;
if|if
condition|(
name|tqMap
operator|.
name|containsKey
argument_list|(
name|TCLIServiceConstants
operator|.
name|CHARACTER_MAXIMUM_LENGTH
argument_list|)
condition|)
block|{
name|ret
operator|.
name|setCharacterMaximumLength
argument_list|(
name|tqMap
operator|.
name|get
argument_list|(
name|TCLIServiceConstants
operator|.
name|CHARACTER_MAXIMUM_LENGTH
argument_list|)
operator|.
name|getI32Value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
specifier|public
specifier|static
name|TypeQualifiers
name|fromTypeInfo
parameter_list|(
name|PrimitiveTypeInfo
name|pti
parameter_list|)
block|{
if|if
condition|(
name|pti
operator|instanceof
name|VarcharTypeInfo
condition|)
block|{
name|TypeQualifiers
name|ret
init|=
operator|new
name|TypeQualifiers
argument_list|()
decl_stmt|;
name|ret
operator|.
name|setCharacterMaximumLength
argument_list|(
operator|(
operator|(
name|VarcharTypeInfo
operator|)
name|pti
operator|)
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit


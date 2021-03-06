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
name|metastore
package|;
end_package

begin_comment
comment|/**  * Base class which provides context to implementations of MetaStoreEndFunctionListener  */
end_comment

begin_class
specifier|public
class|class
name|MetaStoreEndFunctionContext
block|{
comment|/**    * whether method was successful or not.    */
specifier|private
specifier|final
name|boolean
name|success
decl_stmt|;
specifier|private
specifier|final
name|Exception
name|e
decl_stmt|;
specifier|private
specifier|final
name|String
name|inputTableName
decl_stmt|;
specifier|public
name|MetaStoreEndFunctionContext
parameter_list|(
name|boolean
name|success
parameter_list|,
name|Exception
name|e
parameter_list|,
name|String
name|inputTableName
parameter_list|)
block|{
name|this
operator|.
name|success
operator|=
name|success
expr_stmt|;
name|this
operator|.
name|e
operator|=
name|e
expr_stmt|;
name|this
operator|.
name|inputTableName
operator|=
name|inputTableName
expr_stmt|;
block|}
specifier|public
name|MetaStoreEndFunctionContext
parameter_list|(
name|boolean
name|success
parameter_list|)
block|{
name|this
argument_list|(
name|success
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return whether or not the method succeeded.    */
specifier|public
name|boolean
name|isSuccess
parameter_list|()
block|{
return|return
name|success
return|;
block|}
specifier|public
name|Exception
name|getException
parameter_list|()
block|{
return|return
name|e
return|;
block|}
specifier|public
name|String
name|getInputTableName
parameter_list|()
block|{
return|return
name|inputTableName
return|;
block|}
block|}
end_class

end_unit


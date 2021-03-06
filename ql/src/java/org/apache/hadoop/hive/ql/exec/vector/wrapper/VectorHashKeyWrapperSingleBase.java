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
operator|.
name|vector
operator|.
name|wrapper
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HashCodeUtil
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|VectorHashKeyWrapperSingleBase
extends|extends
name|VectorHashKeyWrapperBase
block|{
specifier|protected
name|boolean
name|isNull0
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|nullHashcode
init|=
name|HashCodeUtil
operator|.
name|calculateLongHashCode
argument_list|(
literal|238322L
argument_list|)
decl_stmt|;
specifier|protected
name|VectorHashKeyWrapperSingleBase
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|isNull0
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clearIsNull
parameter_list|()
block|{
name|isNull0
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNull
parameter_list|()
block|{
name|isNull0
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNull
parameter_list|(
name|int
name|keyIndex
parameter_list|)
block|{
if|if
condition|(
name|keyIndex
operator|==
literal|0
condition|)
block|{
return|return
name|isNull0
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ArrayIndexOutOfBoundsException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit


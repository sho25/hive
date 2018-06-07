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
name|llap
operator|.
name|security
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|io
operator|.
name|Text
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|security
operator|.
name|token
operator|.
name|TokenIdentifier
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
name|security
operator|.
name|token
operator|.
name|TokenSelector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|LlapTokenSelector
implements|implements
name|TokenSelector
argument_list|<
name|LlapTokenIdentifier
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LlapTokenSelector
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|selectToken
parameter_list|(
name|Text
name|service
parameter_list|,
name|Collection
argument_list|<
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
argument_list|>
name|tokens
parameter_list|)
block|{
if|if
condition|(
name|service
operator|==
literal|null
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking for a token with service "
operator|+
name|service
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
name|token
range|:
name|tokens
control|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Token = "
operator|+
name|token
operator|.
name|getKind
argument_list|()
operator|+
literal|"; service = "
operator|+
name|token
operator|.
name|getService
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LlapTokenIdentifier
operator|.
name|KIND_NAME
operator|.
name|equals
argument_list|(
name|token
operator|.
name|getKind
argument_list|()
argument_list|)
operator|&&
name|service
operator|.
name|equals
argument_list|(
name|token
operator|.
name|getService
argument_list|()
argument_list|)
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|result
init|=
operator|(
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
operator|)
name|token
decl_stmt|;
return|return
name|result
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


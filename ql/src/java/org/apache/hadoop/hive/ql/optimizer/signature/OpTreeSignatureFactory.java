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
name|optimizer
operator|.
name|signature
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
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
name|ql
operator|.
name|exec
operator|.
name|Operator
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * A simple cache backend to prevent repeated signature computations.  */
end_comment

begin_interface
specifier|public
interface|interface
name|OpTreeSignatureFactory
block|{
name|OpTreeSignature
name|getSignature
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
function_decl|;
name|void
name|clear
parameter_list|()
function_decl|;
name|OpTreeSignatureFactory
name|DIRECT
init|=
operator|new
name|Direct
argument_list|()
decl_stmt|;
specifier|static
name|OpTreeSignatureFactory
name|direct
parameter_list|()
block|{
return|return
name|DIRECT
return|;
block|}
specifier|static
name|OpTreeSignatureFactory
name|newCache
parameter_list|()
block|{
return|return
operator|new
name|CachedFactory
argument_list|()
return|;
block|}
comment|// FIXME: possible alternative: move both OpSignature/OpTreeSignature into
comment|// under some class as nested ones; and that way this factory level caching can be made "transparent"
class|class
name|Direct
implements|implements
name|OpTreeSignatureFactory
block|{
annotation|@
name|Override
specifier|public
name|OpTreeSignature
name|getSignature
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
return|return
name|OpTreeSignature
operator|.
name|of
argument_list|(
name|op
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{     }
block|}
class|class
name|CachedFactory
implements|implements
name|OpTreeSignatureFactory
block|{
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|OpTreeSignature
argument_list|>
name|cache
init|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|OpTreeSignature
name|getSignature
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
return|return
name|cache
operator|.
name|computeIfAbsent
argument_list|(
name|op
argument_list|,
name|k
lambda|->
name|OpTreeSignature
operator|.
name|of
argument_list|(
name|op
argument_list|,
name|this
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_interface

end_unit


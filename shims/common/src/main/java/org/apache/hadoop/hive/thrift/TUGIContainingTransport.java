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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
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
name|UserGroupInformation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|MapMaker
import|;
end_import

begin_comment
comment|/** TUGIContainingTransport associates ugi information with connection (transport).  *  Wraps underlying<code>TSocket</code> transport and annotates it with ugi. */
end_comment

begin_class
specifier|public
class|class
name|TUGIContainingTransport
extends|extends
name|TFilterTransport
block|{
specifier|private
name|UserGroupInformation
name|ugi
decl_stmt|;
specifier|public
name|TUGIContainingTransport
parameter_list|(
name|TTransport
name|wrapped
parameter_list|)
block|{
name|super
argument_list|(
name|wrapped
argument_list|)
expr_stmt|;
block|}
specifier|public
name|UserGroupInformation
name|getClientUGI
parameter_list|()
block|{
return|return
name|ugi
return|;
block|}
specifier|public
name|void
name|setClientUGI
parameter_list|(
name|UserGroupInformation
name|ugi
parameter_list|)
block|{
name|this
operator|.
name|ugi
operator|=
name|ugi
expr_stmt|;
block|}
comment|/**    * If the underlying TTransport is an instance of TSocket, it returns the Socket object    * which it contains.  Otherwise it returns null.    */
specifier|public
name|Socket
name|getSocket
parameter_list|()
block|{
if|if
condition|(
name|wrapped
operator|instanceof
name|TSocket
condition|)
block|{
return|return
operator|(
operator|(
operator|(
name|TSocket
operator|)
name|wrapped
operator|)
operator|.
name|getSocket
argument_list|()
operator|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/** Factory to create TUGIContainingTransport.    */
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|TTransportFactory
block|{
comment|// Need a concurrent weakhashmap. WeakKeys() so that when underlying transport gets out of
comment|// scope, it still can be GC'ed. Since value of map has a ref to key, need weekValues as well.
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|TTransport
argument_list|,
name|TUGIContainingTransport
argument_list|>
name|transMap
init|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|weakKeys
argument_list|()
operator|.
name|weakValues
argument_list|()
operator|.
name|makeMap
argument_list|()
decl_stmt|;
comment|/**      * Get a new<code>TUGIContainingTransport</code> instance, or reuse the      * existing one if a<code>TUGIContainingTransport</code> has already been      * created before using the given<code>TTransport</code> as an underlying      * transport. This ensures that a given underlying transport instance      * receives the same<code>TUGIContainingTransport</code>.      */
annotation|@
name|Override
specifier|public
name|TUGIContainingTransport
name|getTransport
parameter_list|(
name|TTransport
name|trans
parameter_list|)
block|{
comment|// UGI information is not available at connection setup time, it will be set later
comment|// via set_ugi() rpc.
name|TUGIContainingTransport
name|tugiTrans
init|=
name|transMap
operator|.
name|get
argument_list|(
name|trans
argument_list|)
decl_stmt|;
if|if
condition|(
name|tugiTrans
operator|==
literal|null
condition|)
block|{
name|tugiTrans
operator|=
operator|new
name|TUGIContainingTransport
argument_list|(
name|trans
argument_list|)
expr_stmt|;
name|TUGIContainingTransport
name|prev
init|=
name|transMap
operator|.
name|putIfAbsent
argument_list|(
name|trans
argument_list|,
name|tugiTrans
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
return|return
name|prev
return|;
block|}
block|}
return|return
name|tugiTrans
return|;
block|}
block|}
block|}
end_class

end_unit


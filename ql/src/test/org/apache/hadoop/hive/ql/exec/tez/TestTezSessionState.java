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
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
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
name|conf
operator|.
name|HiveConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
import|;
end_import

begin_comment
comment|/**  * This class is needed for writing junit tests. For testing the multi-session  * use case from hive server 2, we need a session simulation.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestTezSessionState
extends|extends
name|TezSessionState
block|{
specifier|private
name|boolean
name|open
decl_stmt|;
specifier|private
name|String
name|sessionId
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|public
name|TestTezSessionState
parameter_list|(
name|String
name|sessionId
parameter_list|)
block|{
name|super
argument_list|(
name|sessionId
argument_list|)
expr_stmt|;
name|this
operator|.
name|sessionId
operator|=
name|sessionId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|open
return|;
block|}
specifier|public
name|void
name|setOpen
parameter_list|(
name|boolean
name|open
parameter_list|)
block|{
name|this
operator|.
name|open
operator|=
name|open
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|keepTmpDir
parameter_list|)
throws|throws
name|TezException
throws|,
name|IOException
block|{
name|open
operator|=
name|keepTmpDir
expr_stmt|;
block|}
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|hiveConf
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getSessionId
parameter_list|()
block|{
return|return
name|sessionId
return|;
block|}
block|}
end_class

end_unit


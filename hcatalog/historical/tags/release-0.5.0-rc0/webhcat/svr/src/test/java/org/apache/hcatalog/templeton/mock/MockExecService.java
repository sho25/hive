begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|mock
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
name|util
operator|.
name|List
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
name|commons
operator|.
name|exec
operator|.
name|ExecuteException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|ExecBean
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|ExecService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|NotAuthorizedException
import|;
end_import

begin_class
specifier|public
class|class
name|MockExecService
implements|implements
name|ExecService
block|{
specifier|public
name|ExecBean
name|run
parameter_list|(
name|String
name|program
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
block|{
name|ExecBean
name|bean
init|=
operator|new
name|ExecBean
argument_list|()
decl_stmt|;
name|bean
operator|.
name|stdout
operator|=
name|program
expr_stmt|;
name|bean
operator|.
name|stderr
operator|=
name|args
operator|.
name|toString
argument_list|()
expr_stmt|;
return|return
name|bean
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecBean
name|runUnlimited
parameter_list|(
name|String
name|program
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|args
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|ExecuteException
throws|,
name|IOException
block|{
name|ExecBean
name|bean
init|=
operator|new
name|ExecBean
argument_list|()
decl_stmt|;
name|bean
operator|.
name|stdout
operator|=
name|program
expr_stmt|;
name|bean
operator|.
name|stderr
operator|=
name|args
operator|.
name|toString
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


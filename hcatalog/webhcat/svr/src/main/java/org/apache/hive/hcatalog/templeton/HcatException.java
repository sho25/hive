begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|http
operator|.
name|HttpStatus
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_comment
comment|/**  * Unable to run hcat on the job.  */
end_comment

begin_class
specifier|public
class|class
name|HcatException
extends|extends
name|SimpleWebException
block|{
specifier|public
name|ExecBean
name|execBean
decl_stmt|;
specifier|public
name|String
name|statement
decl_stmt|;
specifier|public
name|HcatException
parameter_list|(
name|String
name|msg
parameter_list|,
specifier|final
name|ExecBean
name|bean
parameter_list|,
specifier|final
name|String
name|statement
parameter_list|)
block|{
name|super
argument_list|(
name|HttpStatus
operator|.
name|INTERNAL_SERVER_ERROR_500
argument_list|,
name|msg
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"exec"
argument_list|,
name|bean
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"statement"
argument_list|,
name|statement
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|execBean
operator|=
name|bean
expr_stmt|;
name|this
operator|.
name|statement
operator|=
name|statement
expr_stmt|;
block|}
block|}
end_class

end_unit


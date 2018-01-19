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

begin_comment
comment|/**  * Raise this exception if web service is busy with existing requests and not able  * service new requests.  */
end_comment

begin_class
specifier|public
class|class
name|TooManyRequestsException
extends|extends
name|SimpleWebException
block|{
comment|/*    * The current version of jetty server doesn't have the status    * HttpStatus.TOO_MANY_REQUESTS_429. Hence, passing this as constant.    */
specifier|public
specifier|static
name|int
name|TOO_MANY_REQUESTS_429
init|=
literal|429
decl_stmt|;
specifier|public
name|TooManyRequestsException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|TOO_MANY_REQUESTS_429
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


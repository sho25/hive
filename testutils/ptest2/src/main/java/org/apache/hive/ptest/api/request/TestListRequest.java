begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|api
operator|.
name|request
package|;
end_package

begin_class
specifier|public
class|class
name|TestListRequest
block|{
comment|/**    * Do not use. Required by Jackson.    */
specifier|private
name|String
name|dummy
decl_stmt|;
specifier|public
name|TestListRequest
parameter_list|()
block|{    }
specifier|public
name|String
name|getDummy
parameter_list|()
block|{
return|return
name|dummy
return|;
block|}
specifier|public
name|void
name|setDummy
parameter_list|(
name|String
name|dummy
parameter_list|)
block|{
name|this
operator|.
name|dummy
operator|=
name|dummy
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|execution
operator|.
name|context
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|Context
import|;
end_import

begin_interface
specifier|public
interface|interface
name|ExecutionContextProvider
block|{
specifier|static
specifier|final
name|String
name|PRIVATE_KEY
init|=
literal|"privateKey"
decl_stmt|;
specifier|public
name|ExecutionContext
name|createExecutionContext
parameter_list|()
throws|throws
name|CreateHostsFailedException
throws|,
name|ServiceNotAvailableException
function_decl|;
specifier|public
name|void
name|replaceBadHosts
parameter_list|(
name|ExecutionContext
name|executionContext
parameter_list|)
throws|throws
name|CreateHostsFailedException
function_decl|;
specifier|public
name|void
name|terminate
parameter_list|(
name|ExecutionContext
name|executionContext
parameter_list|)
function_decl|;
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
specifier|public
interface|interface
name|Builder
block|{
specifier|public
name|ExecutionContextProvider
name|build
parameter_list|(
name|Context
name|context
parameter_list|,
name|String
name|workingDirectory
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
block|}
end_interface

end_unit


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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_class
specifier|public
class|class
name|LocalCommandFactory
block|{
specifier|private
specifier|final
name|Logger
name|mLogger
decl_stmt|;
specifier|public
name|LocalCommandFactory
parameter_list|(
name|Logger
name|logger
parameter_list|)
block|{
name|mLogger
operator|=
name|logger
expr_stmt|;
block|}
specifier|public
name|LocalCommand
name|create
parameter_list|(
name|LocalCommand
operator|.
name|CollectPolicy
name|policy
parameter_list|,
name|String
name|command
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|LocalCommand
argument_list|(
name|mLogger
argument_list|,
name|policy
argument_list|,
name|command
argument_list|)
return|;
block|}
block|}
end_class

end_unit


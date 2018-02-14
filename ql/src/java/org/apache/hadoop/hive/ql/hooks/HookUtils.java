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
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|Utilities
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Strings
import|;
end_import

begin_class
specifier|public
class|class
name|HookUtils
block|{
specifier|public
specifier|static
name|String
name|redactLogString
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|logString
parameter_list|)
throws|throws
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|ClassNotFoundException
block|{
name|String
name|redactedString
init|=
name|logString
decl_stmt|;
if|if
condition|(
name|conf
operator|!=
literal|null
operator|&&
name|logString
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Redactor
argument_list|>
name|queryRedactors
init|=
name|readHooksFromConf
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|QUERYREDACTORHOOKS
argument_list|)
decl_stmt|;
for|for
control|(
name|Redactor
name|redactor
range|:
name|queryRedactors
control|)
block|{
name|redactor
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|redactedString
operator|=
name|redactor
operator|.
name|redactQuery
argument_list|(
name|redactedString
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|redactedString
return|;
block|}
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Hook
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|readHooksFromConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|HiveConf
operator|.
name|ConfVars
name|hookConfVar
parameter_list|)
throws|throws
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|ClassNotFoundException
block|{
name|String
name|csHooks
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|hookConfVar
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|T
argument_list|>
name|hooks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isBlank
argument_list|(
name|csHooks
argument_list|)
condition|)
block|{
return|return
name|hooks
return|;
block|}
name|String
index|[]
name|hookClasses
init|=
name|csHooks
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|hookClass
range|:
name|hookClasses
control|)
block|{
name|T
name|hook
init|=
operator|(
name|T
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|hookClass
operator|.
name|trim
argument_list|()
argument_list|,
literal|true
argument_list|,
name|Utilities
operator|.
name|getSessionSpecifiedClassLoader
argument_list|()
argument_list|)
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|hooks
operator|.
name|add
argument_list|(
name|hook
argument_list|)
expr_stmt|;
block|}
return|return
name|hooks
return|;
block|}
block|}
end_class

end_unit


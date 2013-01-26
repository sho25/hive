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
package|;
end_package

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
name|ArrayList
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|common
operator|.
name|JavaUtils
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

begin_class
specifier|public
class|class
name|OperatorHookUtils
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"OperatorHookUtils"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|List
argument_list|<
name|OperatorHook
argument_list|>
name|getOperatorHooks
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|OperatorHook
argument_list|>
name|opHooks
init|=
operator|new
name|ArrayList
argument_list|<
name|OperatorHook
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|csOpHooks
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|OPERATORHOOKS
argument_list|)
decl_stmt|;
if|if
condition|(
name|csOpHooks
operator|==
literal|null
condition|)
block|{
return|return
name|opHooks
return|;
block|}
name|String
index|[]
name|opHookClasses
init|=
name|csOpHooks
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|opHookClass
range|:
name|opHookClasses
control|)
block|{
try|try
block|{
name|OperatorHook
name|opHook
init|=
operator|(
name|OperatorHook
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|opHookClass
operator|.
name|trim
argument_list|()
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|opHooks
operator|.
name|add
argument_list|(
name|opHook
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|opHookClass
operator|+
literal|" Class not found: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|opHooks
return|;
block|}
block|}
end_class

end_unit


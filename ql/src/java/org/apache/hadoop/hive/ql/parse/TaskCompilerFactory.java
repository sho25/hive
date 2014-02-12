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
name|parse
package|;
end_package

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

begin_comment
comment|/**  * TaskCompilerFactory is a factory class to choose the appropriate  * TaskCompiler.  */
end_comment

begin_class
specifier|public
class|class
name|TaskCompilerFactory
block|{
specifier|private
name|TaskCompilerFactory
parameter_list|()
block|{
comment|// avoid instantiation
block|}
comment|/**    * Returns the appropriate compiler to translate the operator tree    * into executable units.    */
specifier|public
specifier|static
name|TaskCompiler
name|getCompiler
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TezCompiler
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|MapReduceCompiler
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit


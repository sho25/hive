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
name|net
operator|.
name|URLClassLoader
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
name|ql
operator|.
name|plan
operator|.
name|FunctionWork
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
name|plan
operator|.
name|createFunctionDesc
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
name|plan
operator|.
name|dropFunctionDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFResolver
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|util
operator|.
name|ReflectionUtils
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
name|util
operator|.
name|StringUtils
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
name|FunctionInfo
operator|.
name|OperatorType
import|;
end_import

begin_class
specifier|public
class|class
name|FunctionTask
extends|extends
name|Task
argument_list|<
name|FunctionWork
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.ql.exec.FunctionTask"
argument_list|)
decl_stmt|;
specifier|transient
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
block|{
name|createFunctionDesc
name|createFunctionDesc
init|=
name|work
operator|.
name|getCreateFunctionDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|createFunctionDesc
operator|!=
literal|null
condition|)
block|{
return|return
name|createFunction
argument_list|(
name|createFunctionDesc
argument_list|)
return|;
block|}
name|dropFunctionDesc
name|dropFunctionDesc
init|=
name|work
operator|.
name|getDropFunctionDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|dropFunctionDesc
operator|!=
literal|null
condition|)
block|{
return|return
name|dropFunction
argument_list|(
name|dropFunctionDesc
argument_list|)
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|int
name|createFunction
parameter_list|(
name|createFunctionDesc
name|createFunctionDesc
parameter_list|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|udfClass
init|=
name|getUdfClass
argument_list|(
name|createFunctionDesc
argument_list|)
decl_stmt|;
if|if
condition|(
name|UDF
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|udfClass
argument_list|)
condition|)
block|{
name|FunctionRegistry
operator|.
name|registerUDF
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
operator|)
name|udfClass
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|GenericUDF
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|udfClass
argument_list|)
condition|)
block|{
name|FunctionRegistry
operator|.
name|registerGenericUDF
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|GenericUDF
argument_list|>
operator|)
name|udfClass
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|UDAF
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|udfClass
argument_list|)
condition|)
block|{
name|FunctionRegistry
operator|.
name|registerUDAF
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
operator|)
name|udfClass
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|GenericUDAFResolver
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|udfClass
argument_list|)
condition|)
block|{
name|FunctionRegistry
operator|.
name|registerGenericUDAF
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|,
operator|(
name|GenericUDAFResolver
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|udfClass
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"create function: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
name|int
name|dropFunction
parameter_list|(
name|dropFunctionDesc
name|dropFunctionDesc
parameter_list|)
block|{
name|FunctionRegistry
operator|.
name|unregisterUDF
argument_list|(
name|dropFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|getUdfClass
parameter_list|(
name|createFunctionDesc
name|desc
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
name|desc
operator|.
name|getClassName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit


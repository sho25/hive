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
name|io
operator|.
name|Closeable
import|;
end_import

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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|tez
operator|.
name|TezContext
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
name|GenericUDAFEvaluator
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
name|hive
operator|.
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDTF
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * Runtime context of MapredTask providing additional information to GenericUDF  */
end_comment

begin_class
specifier|public
class|class
name|MapredContext
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"MapredContext"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|MapredContext
argument_list|>
name|contexts
init|=
operator|new
name|ThreadLocal
argument_list|<
name|MapredContext
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|MapredContext
name|get
parameter_list|()
block|{
return|return
name|contexts
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|MapredContext
name|init
parameter_list|(
name|boolean
name|isMap
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
block|{
name|MapredContext
name|context
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|jobConf
argument_list|,
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|?
operator|new
name|TezContext
argument_list|(
name|isMap
argument_list|,
name|jobConf
argument_list|)
else|:
operator|new
name|MapredContext
argument_list|(
name|isMap
argument_list|,
name|jobConf
argument_list|)
decl_stmt|;
name|contexts
operator|.
name|set
argument_list|(
name|context
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"MapredContext initialized."
argument_list|)
expr_stmt|;
block|}
return|return
name|context
return|;
block|}
specifier|public
specifier|static
name|void
name|close
parameter_list|()
block|{
name|MapredContext
name|context
init|=
name|contexts
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|context
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|closeAll
argument_list|()
expr_stmt|;
block|}
name|contexts
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|final
name|boolean
name|isMap
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Closeable
argument_list|>
name|udfs
decl_stmt|;
specifier|private
name|Reporter
name|reporter
decl_stmt|;
specifier|protected
name|MapredContext
parameter_list|(
name|boolean
name|isMap
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
block|{
name|this
operator|.
name|isMap
operator|=
name|isMap
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
name|this
operator|.
name|udfs
operator|=
operator|new
name|ArrayList
argument_list|<
name|Closeable
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns whether the UDF is called from Map or Reduce task.    */
specifier|public
name|boolean
name|isMap
parameter_list|()
block|{
return|return
name|isMap
return|;
block|}
comment|/**    * Returns Reporter, which is set right before reading the first row.    */
specifier|public
name|Reporter
name|getReporter
parameter_list|()
block|{
return|return
name|reporter
return|;
block|}
comment|/**    * Returns JobConf.    */
specifier|public
name|JobConf
name|getJobConf
parameter_list|()
block|{
return|return
name|jobConf
return|;
block|}
specifier|public
name|void
name|setReporter
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
block|{
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
block|}
specifier|private
name|void
name|registerCloseable
parameter_list|(
name|Closeable
name|closeable
parameter_list|)
block|{
name|udfs
operator|.
name|add
argument_list|(
name|closeable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|closeAll
parameter_list|()
block|{
for|for
control|(
name|Closeable
name|eval
range|:
name|udfs
control|)
block|{
try|try
block|{
name|eval
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Hit error while closing udf "
operator|+
name|eval
argument_list|)
expr_stmt|;
block|}
block|}
name|udfs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setup
parameter_list|(
name|GenericUDF
name|genericUDF
parameter_list|)
block|{
if|if
condition|(
name|needConfigure
argument_list|(
name|genericUDF
argument_list|)
condition|)
block|{
name|genericUDF
operator|.
name|configure
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|needClose
argument_list|(
name|genericUDF
argument_list|)
condition|)
block|{
name|registerCloseable
argument_list|(
name|genericUDF
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|setup
parameter_list|(
name|GenericUDAFEvaluator
name|genericUDAF
parameter_list|)
block|{
if|if
condition|(
name|needConfigure
argument_list|(
name|genericUDAF
argument_list|)
condition|)
block|{
name|genericUDAF
operator|.
name|configure
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|needClose
argument_list|(
name|genericUDAF
argument_list|)
condition|)
block|{
name|registerCloseable
argument_list|(
name|genericUDAF
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|setup
parameter_list|(
name|GenericUDTF
name|genericUDTF
parameter_list|)
block|{
if|if
condition|(
name|needConfigure
argument_list|(
name|genericUDTF
argument_list|)
condition|)
block|{
name|genericUDTF
operator|.
name|configure
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|// close is called by UDTFOperator
block|}
specifier|private
name|boolean
name|needConfigure
parameter_list|(
name|Object
name|func
parameter_list|)
block|{
try|try
block|{
name|Method
name|initMethod
init|=
name|func
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"configure"
argument_list|,
name|MapredContext
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|initMethod
operator|.
name|getDeclaringClass
argument_list|()
operator|!=
name|GenericUDF
operator|.
name|class
operator|&&
name|initMethod
operator|.
name|getDeclaringClass
argument_list|()
operator|!=
name|GenericUDAFEvaluator
operator|.
name|class
operator|&&
name|initMethod
operator|.
name|getDeclaringClass
argument_list|()
operator|!=
name|GenericUDTF
operator|.
name|class
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|private
name|boolean
name|needClose
parameter_list|(
name|Closeable
name|func
parameter_list|)
block|{
try|try
block|{
name|Method
name|closeMethod
init|=
name|func
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"close"
argument_list|)
decl_stmt|;
return|return
name|closeMethod
operator|.
name|getDeclaringClass
argument_list|()
operator|!=
name|GenericUDF
operator|.
name|class
operator|&&
name|closeMethod
operator|.
name|getDeclaringClass
argument_list|()
operator|!=
name|GenericUDAFEvaluator
operator|.
name|class
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit


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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|ql
operator|.
name|plan
operator|.
name|fetchWork
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
name|mapredLocalWork
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
name|mapredWork
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
name|serde2
operator|.
name|objectinspector
operator|.
name|InspectableObject
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|io
operator|.
name|Writable
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
name|MapReduceBase
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
name|Mapper
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
name|OutputCollector
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

begin_class
specifier|public
class|class
name|ExecMapper
extends|extends
name|MapReduceBase
implements|implements
name|Mapper
block|{
specifier|private
name|MapOperator
name|mo
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|fetchOperators
decl_stmt|;
specifier|private
name|OutputCollector
name|oc
decl_stmt|;
specifier|private
name|JobConf
name|jc
decl_stmt|;
specifier|private
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
specifier|private
name|Reporter
name|rp
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"ExecMapper"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|done
decl_stmt|;
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
try|try
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"conf classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|job
operator|.
name|getClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"thread classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"cannot get classpath: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|jc
operator|=
name|job
expr_stmt|;
comment|// create map and fetch operators
name|mapredWork
name|mrwork
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|mo
operator|=
operator|new
name|MapOperator
argument_list|()
expr_stmt|;
name|mo
operator|.
name|setConf
argument_list|(
name|mrwork
argument_list|)
expr_stmt|;
comment|// initialize map operator
name|mo
operator|.
name|setChildren
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
name|mo
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|mo
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// initialize map local work
name|mapredLocalWork
name|localWork
init|=
name|mrwork
operator|.
name|getMapLocalWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|localWork
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|fetchOperators
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
argument_list|()
expr_stmt|;
comment|// create map local operators
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|fetchWork
argument_list|>
name|entry
range|:
name|localWork
operator|.
name|getAliasToFetchWork
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|fetchOperators
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|FetchOperator
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|job
argument_list|)
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"fetchoperator for "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" created"
argument_list|)
expr_stmt|;
block|}
comment|// initialize map local operators
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|entry
range|:
name|fetchOperators
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|forwardOp
init|=
name|localWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
comment|// All the operators need to be initialized before process
name|forwardOp
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getOutputObjectInspector
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"fetchoperator for "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" initialized"
argument_list|)
expr_stmt|;
block|}
comment|// defer processing of map local operators to first row if in case there is no input (??)
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// will this be true here?
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Map operator initialization failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|void
name|map
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|,
name|OutputCollector
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|oc
operator|==
literal|null
condition|)
block|{
name|oc
operator|=
name|output
expr_stmt|;
name|rp
operator|=
name|reporter
expr_stmt|;
name|mo
operator|.
name|setOutputCollector
argument_list|(
name|oc
argument_list|)
expr_stmt|;
name|mo
operator|.
name|setReporter
argument_list|(
name|rp
argument_list|)
expr_stmt|;
comment|// process map local operators
if|if
condition|(
name|fetchOperators
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|mapredLocalWork
name|localWork
init|=
name|mo
operator|.
name|getConf
argument_list|()
operator|.
name|getMapLocalWork
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|entry
range|:
name|fetchOperators
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|FetchOperator
name|fetchOp
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|forwardOp
init|=
name|localWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|InspectableObject
name|row
init|=
name|fetchOp
operator|.
name|getNextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|forwardOp
operator|.
name|process
argument_list|(
name|row
operator|.
name|o
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Map local work failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
try|try
block|{
if|if
condition|(
name|mo
operator|.
name|getDone
argument_list|()
condition|)
name|done
operator|=
literal|true
expr_stmt|;
else|else
comment|// Since there is no concept of a group, we don't invoke startGroup/endGroup for a mapper
name|mo
operator|.
name|process
argument_list|(
operator|(
name|Writable
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// No row was processed
if|if
condition|(
name|oc
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|trace
argument_list|(
literal|"Close called. no row processed by map."
argument_list|)
expr_stmt|;
block|}
comment|// detecting failed executions by exceptions thrown by the operator tree
comment|// ideally hadoop should let us know whether map execution failed or not
try|try
block|{
name|mo
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
if|if
condition|(
name|fetchOperators
operator|!=
literal|null
condition|)
block|{
name|mapredLocalWork
name|localWork
init|=
name|mo
operator|.
name|getConf
argument_list|()
operator|.
name|getMapLocalWork
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FetchOperator
argument_list|>
name|entry
range|:
name|fetchOperators
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|forwardOp
init|=
name|localWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|forwardOp
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
block|}
name|reportStats
name|rps
init|=
operator|new
name|reportStats
argument_list|(
name|rp
argument_list|)
decl_stmt|;
name|mo
operator|.
name|preorderMap
argument_list|(
name|rps
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
comment|// signal new failure to map-reduce
name|l4j
operator|.
name|error
argument_list|(
literal|"Hit error while closing operators - failing tree"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error while closing operators"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|getDone
parameter_list|()
block|{
return|return
name|done
return|;
block|}
specifier|public
specifier|static
class|class
name|reportStats
implements|implements
name|Operator
operator|.
name|OperatorFunc
block|{
name|Reporter
name|rp
decl_stmt|;
specifier|public
name|reportStats
parameter_list|(
name|Reporter
name|rp
parameter_list|)
block|{
name|this
operator|.
name|rp
operator|=
name|rp
expr_stmt|;
block|}
specifier|public
name|void
name|func
parameter_list|(
name|Operator
name|op
parameter_list|)
block|{
name|Map
argument_list|<
name|Enum
argument_list|,
name|Long
argument_list|>
name|opStats
init|=
name|op
operator|.
name|getStats
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Enum
argument_list|,
name|Long
argument_list|>
name|e
range|:
name|opStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|this
operator|.
name|rp
operator|!=
literal|null
condition|)
block|{
name|rp
operator|.
name|incrCounter
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit


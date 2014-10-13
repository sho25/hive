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
operator|.
name|tez
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
name|exec
operator|.
name|MapOperator
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
name|mr
operator|.
name|ExecMapperContext
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
name|metadata
operator|.
name|HiveException
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
name|tez
operator|.
name|mapreduce
operator|.
name|input
operator|.
name|MRInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|library
operator|.
name|api
operator|.
name|KeyValueReader
import|;
end_import

begin_comment
comment|/**  * Process input from tez LogicalInput and write output - for a map plan Just pump the records  * through the query plan.  */
end_comment

begin_class
specifier|public
class|class
name|MapRecordSource
implements|implements
name|RecordSource
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
name|MapRecordSource
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ExecMapperContext
name|execContext
init|=
literal|null
decl_stmt|;
specifier|private
name|MapOperator
name|mapOp
init|=
literal|null
decl_stmt|;
specifier|private
name|KeyValueReader
name|reader
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|grouped
init|=
literal|false
decl_stmt|;
name|void
name|init
parameter_list|(
name|JobConf
name|jconf
parameter_list|,
name|MapOperator
name|mapOp
parameter_list|,
name|KeyValueReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|execContext
operator|=
name|mapOp
operator|.
name|getExecContext
argument_list|()
expr_stmt|;
name|this
operator|.
name|mapOp
operator|=
name|mapOp
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|boolean
name|isGrouped
parameter_list|()
block|{
return|return
name|grouped
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|pushRecord
parameter_list|()
throws|throws
name|HiveException
block|{
name|execContext
operator|.
name|resetRow
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|reader
operator|.
name|next
argument_list|()
condition|)
block|{
name|Object
name|value
decl_stmt|;
try|try
block|{
name|value
operator|=
name|reader
operator|.
name|getCurrentValue
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|processRow
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|processRow
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|mapOp
operator|.
name|getDone
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
comment|// done
block|}
else|else
block|{
comment|// Since there is no concept of a group, we don't invoke
comment|// startGroup/endGroup for a mapper
name|mapOp
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
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
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
name|LOG
operator|.
name|fatal
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
comment|// give me more
block|}
block|}
end_class

end_unit


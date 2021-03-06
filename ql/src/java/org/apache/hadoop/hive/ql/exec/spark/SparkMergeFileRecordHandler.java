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
name|exec
operator|.
name|spark
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
name|util
operator|.
name|Iterator
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
name|ql
operator|.
name|exec
operator|.
name|AbstractFileMergeOperator
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
name|Operator
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|merge
operator|.
name|MergeFileWork
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
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|FileMergeDesc
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
name|MapWork
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
name|OperatorDesc
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Copied from MergeFileMapper.  *  * As MergeFileMapper is very similar to ExecMapper, this class is  * very similar to SparkMapRecordHandler  */
end_comment

begin_class
specifier|public
class|class
name|SparkMergeFileRecordHandler
extends|extends
name|SparkRecordHandler
block|{
specifier|private
specifier|static
specifier|final
name|String
name|PLAN_KEY
init|=
literal|"__MAP_PLAN__"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkMergeFileRecordHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
decl_stmt|;
specifier|private
name|AbstractFileMergeOperator
argument_list|<
name|?
extends|extends
name|FileMergeDesc
argument_list|>
name|mergeOp
decl_stmt|;
specifier|private
name|Object
index|[]
name|row
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|void
name|init
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|OutputCollector
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|init
argument_list|(
name|job
argument_list|,
name|output
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
try|try
block|{
name|jc
operator|=
name|job
expr_stmt|;
name|MapWork
name|mapWork
init|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapWork
operator|instanceof
name|MergeFileWork
condition|)
block|{
name|MergeFileWork
name|mergeFileWork
init|=
operator|(
name|MergeFileWork
operator|)
name|mapWork
decl_stmt|;
name|String
name|alias
init|=
name|mergeFileWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|op
operator|=
name|mergeFileWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|AbstractFileMergeOperator
condition|)
block|{
name|mergeOp
operator|=
operator|(
name|AbstractFileMergeOperator
argument_list|<
name|?
extends|extends
name|FileMergeDesc
argument_list|>
operator|)
name|op
expr_stmt|;
name|mergeOp
operator|.
name|initializeOp
argument_list|(
name|jc
argument_list|)
expr_stmt|;
name|row
operator|=
operator|new
name|Object
index|[
literal|2
index|]
expr_stmt|;
name|abort
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|abort
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Merge file work's top operator should be an"
operator|+
literal|" instance of AbstractFileMergeOperator"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|abort
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Map work should be a merge file work."
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|mergeOp
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
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
annotation|@
name|Override
specifier|public
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|row
index|[
literal|0
index|]
operator|=
name|key
expr_stmt|;
name|row
index|[
literal|1
index|]
operator|=
name|value
expr_stmt|;
name|incrementRowNumber
argument_list|()
expr_stmt|;
try|try
block|{
name|mergeOp
operator|.
name|process
argument_list|(
name|row
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|E
parameter_list|>
name|void
name|processRow
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterator
argument_list|<
name|E
argument_list|>
name|values
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Do not support this method in "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing Merge Operator "
operator|+
name|mergeOp
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|mergeOp
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getDone
parameter_list|()
block|{
return|return
name|mergeOp
operator|.
name|getDone
argument_list|()
return|;
block|}
block|}
end_class

end_unit


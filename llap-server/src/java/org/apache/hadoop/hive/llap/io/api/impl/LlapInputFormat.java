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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|impl
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
name|ql
operator|.
name|io
operator|.
name|BatchToRowInputFormat
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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
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
name|fs
operator|.
name|Path
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|ColumnVectorProducer
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
name|ColumnInfo
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
name|RowSchema
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
name|TableScanOperator
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedInputFormatInterface
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|CombineHiveInputFormat
operator|.
name|AvoidSplitCombination
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
name|LlapAwareSplit
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
name|SelfDescribingInputFormatInterface
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
name|metadata
operator|.
name|VirtualColumn
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
name|PartitionDesc
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
name|ColumnProjectionUtils
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
name|Deserializer
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|NullWritable
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
name|FileSplit
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
name|InputFormat
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
name|InputSplit
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
name|RecordReader
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_class
specifier|public
class|class
name|LlapInputFormat
implements|implements
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
implements|,
name|VectorizedInputFormatInterface
implements|,
name|SelfDescribingInputFormatInterface
implements|,
name|AvoidSplitCombination
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NONVECTOR_SETTING_MESSAGE
init|=
literal|"disable "
operator|+
name|ConfVars
operator|.
name|LLAP_IO_NONVECTOR_WRAPPER_ENABLED
operator|.
name|varname
operator|+
literal|" to work around this error"
decl_stmt|;
specifier|private
specifier|final
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|sourceInputFormat
decl_stmt|;
specifier|private
specifier|final
name|AvoidSplitCombination
name|sourceASC
decl_stmt|;
specifier|private
specifier|final
name|Deserializer
name|sourceSerDe
decl_stmt|;
specifier|final
name|ColumnVectorProducer
name|cvp
decl_stmt|;
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
specifier|private
specifier|final
name|String
name|hostName
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|daemonConf
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
name|LlapInputFormat
parameter_list|(
name|InputFormat
name|sourceInputFormat
parameter_list|,
name|Deserializer
name|sourceSerDe
parameter_list|,
name|ColumnVectorProducer
name|cvp
parameter_list|,
name|ExecutorService
name|executor
parameter_list|,
name|Configuration
name|daemonConf
parameter_list|)
block|{
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
name|this
operator|.
name|cvp
operator|=
name|cvp
expr_stmt|;
name|this
operator|.
name|daemonConf
operator|=
name|daemonConf
expr_stmt|;
name|this
operator|.
name|sourceInputFormat
operator|=
name|sourceInputFormat
expr_stmt|;
name|this
operator|.
name|sourceASC
operator|=
operator|(
name|sourceInputFormat
operator|instanceof
name|AvoidSplitCombination
operator|)
condition|?
operator|(
name|AvoidSplitCombination
operator|)
name|sourceInputFormat
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|sourceSerDe
operator|=
name|sourceSerDe
expr_stmt|;
name|this
operator|.
name|hostName
operator|=
name|HiveStringUtils
operator|.
name|getHostname
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Check LLAP-aware split (e.g. OrcSplit) to make sure it's compatible.
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|noLlap
init|=
name|checkLlapSplit
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
if|if
condition|(
name|noLlap
operator|!=
literal|null
condition|)
return|return
name|noLlap
return|;
name|FileSplit
name|fileSplit
init|=
operator|(
name|FileSplit
operator|)
name|split
decl_stmt|;
name|reporter
operator|.
name|setStatus
argument_list|(
name|fileSplit
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// At this entry point, we are going to assume that these are logical table columns.
comment|// Perhaps we should go thru the code and clean this up to be more explicit; for now, we
comment|// will start with this single assumption and maintain clear semantics from here.
name|List
argument_list|<
name|Integer
argument_list|>
name|tableIncludedCols
init|=
name|ColumnProjectionUtils
operator|.
name|isReadAllColumns
argument_list|(
name|job
argument_list|)
condition|?
literal|null
else|:
name|ColumnProjectionUtils
operator|.
name|getReadColumnIDs
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|LlapRecordReader
name|rr
init|=
name|LlapRecordReader
operator|.
name|create
argument_list|(
name|job
argument_list|,
name|fileSplit
argument_list|,
name|tableIncludedCols
argument_list|,
name|hostName
argument_list|,
name|cvp
argument_list|,
name|executor
argument_list|,
name|sourceInputFormat
argument_list|,
name|sourceSerDe
argument_list|,
name|reporter
argument_list|,
name|daemonConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|rr
operator|==
literal|null
condition|)
block|{
comment|// Reader-specific incompatibility like SMB or schema evolution.
return|return
name|sourceInputFormat
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
return|;
block|}
comment|// For non-vectorized operator case, wrap the reader if possible.
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|result
init|=
name|rr
decl_stmt|;
if|if
condition|(
operator|!
name|Utilities
operator|.
name|getIsVectorized
argument_list|(
name|job
argument_list|)
condition|)
block|{
name|result
operator|=
name|wrapLlapReader
argument_list|(
name|tableIncludedCols
argument_list|,
name|rr
argument_list|,
name|split
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
comment|// Cannot wrap a reader for non-vectorized pipeline.
return|return
name|sourceInputFormat
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
return|;
block|}
block|}
comment|// This starts the reader in the background.
name|rr
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|private
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|wrapLlapReader
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|includedCols
parameter_list|,
name|LlapRecordReader
name|rr
parameter_list|,
name|InputSplit
name|split
parameter_list|)
throws|throws
name|IOException
block|{
comment|// vectorized row batch reader
if|if
condition|(
name|sourceInputFormat
operator|instanceof
name|BatchToRowInputFormat
condition|)
block|{
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Using batch-to-row converter for split: "
operator|+
name|split
argument_list|)
expr_stmt|;
return|return
name|bogusCast
argument_list|(
operator|(
operator|(
name|BatchToRowInputFormat
operator|)
name|sourceInputFormat
operator|)
operator|.
name|getWrapper
argument_list|(
name|rr
argument_list|,
name|rr
operator|.
name|getVectorizedRowBatchCtx
argument_list|()
argument_list|,
name|includedCols
argument_list|)
argument_list|)
return|;
block|}
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using LLAP IO for an unsupported split: "
operator|+
name|split
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|checkLlapSplit
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|useLlapIo
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|split
operator|instanceof
name|LlapAwareSplit
condition|)
block|{
name|useLlapIo
operator|=
operator|(
operator|(
name|LlapAwareSplit
operator|)
name|split
operator|)
operator|.
name|canUseLlapIo
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|useLlapIo
condition|)
return|return
literal|null
return|;
name|LlapIoImpl
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using LLAP IO for an unsupported split: "
operator|+
name|split
argument_list|)
expr_stmt|;
return|return
name|sourceInputFormat
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
return|;
block|}
comment|// Returning either a vectorized or non-vectorized reader from the same call requires breaking
comment|// generics... this is how vectorization currently works.
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
parameter_list|<
name|A
parameter_list|,
name|B
parameter_list|,
name|C
parameter_list|,
name|D
parameter_list|>
name|RecordReader
argument_list|<
name|A
argument_list|,
name|B
argument_list|>
name|bogusCast
parameter_list|(
name|RecordReader
argument_list|<
name|C
argument_list|,
name|D
argument_list|>
name|rr
parameter_list|)
block|{
return|return
operator|(
name|RecordReader
argument_list|<
name|A
argument_list|,
name|B
argument_list|>
operator|)
name|rr
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|sourceInputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
name|numSplits
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldSkipCombine
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|sourceASC
operator|==
literal|null
condition|?
literal|false
else|:
name|sourceASC
operator|.
name|shouldSkipCombine
argument_list|(
name|path
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|static
name|VectorizedRowBatchCtx
name|createFakeVrbCtx
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// This is based on Vectorizer code, minus the validation.
comment|// Add all non-virtual columns from the TableScan operator.
name|RowSchema
name|rowSchema
init|=
name|findTsOp
argument_list|(
name|mapWork
argument_list|)
operator|.
name|getSchema
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|rowSchema
operator|.
name|getSignature
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|colTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
name|rowSchema
operator|.
name|getSignature
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|hasRowId
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|c
range|:
name|rowSchema
operator|.
name|getSignature
argument_list|()
control|)
block|{
name|String
name|columnName
init|=
name|c
operator|.
name|getInternalName
argument_list|()
decl_stmt|;
if|if
condition|(
name|VirtualColumn
operator|.
name|ROWID
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
name|hasRowId
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|VirtualColumn
operator|.
name|VIRTUAL_COLUMN_NAMES
operator|.
name|contains
argument_list|(
name|columnName
argument_list|)
condition|)
continue|continue;
block|}
name|colNames
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|colTypes
operator|.
name|add
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|c
operator|.
name|getTypeName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Determine the partition columns using the first partition descriptor.
comment|// Note - like vectorizer, this assumes partition columns go after data columns.
name|int
name|partitionColumnCount
init|=
literal|0
decl_stmt|;
name|Iterator
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|mapWork
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|paths
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|PartitionDesc
name|partDesc
init|=
name|mapWork
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|paths
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partDesc
operator|!=
literal|null
condition|)
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|partDesc
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|partSpec
operator|!=
literal|null
operator|&&
operator|!
name|partSpec
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|partitionColumnCount
operator|=
name|partSpec
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|final
name|VirtualColumn
index|[]
name|virtualColumns
decl_stmt|;
if|if
condition|(
name|hasRowId
condition|)
block|{
name|virtualColumns
operator|=
operator|new
name|VirtualColumn
index|[]
block|{
name|VirtualColumn
operator|.
name|ROWID
block|}
expr_stmt|;
block|}
else|else
block|{
name|virtualColumns
operator|=
operator|new
name|VirtualColumn
index|[
literal|0
index|]
expr_stmt|;
block|}
return|return
operator|new
name|VectorizedRowBatchCtx
argument_list|(
name|colNames
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|colNames
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|colTypes
operator|.
name|toArray
argument_list|(
operator|new
name|TypeInfo
index|[
name|colTypes
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|partitionColumnCount
argument_list|,
name|virtualColumns
operator|.
name|length
argument_list|,
name|virtualColumns
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|static
name|TableScanOperator
name|findTsOp
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unexpected - aliasToWork is missing; "
operator|+
name|NONVECTOR_SETTING_MESSAGE
argument_list|)
throw|;
block|}
name|Iterator
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|TableScanOperator
name|tableScanOperator
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|ops
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|ops
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
if|if
condition|(
name|tableScanOperator
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unexpected - more than one TSOP; "
operator|+
name|NONVECTOR_SETTING_MESSAGE
argument_list|)
throw|;
block|}
name|tableScanOperator
operator|=
operator|(
name|TableScanOperator
operator|)
name|op
expr_stmt|;
block|}
block|}
return|return
name|tableScanOperator
return|;
block|}
block|}
end_class

end_unit


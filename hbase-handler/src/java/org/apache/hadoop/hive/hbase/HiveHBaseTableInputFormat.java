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
name|hbase
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
name|ArrayList
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
name|List
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|hbase
operator|.
name|client
operator|.
name|HTable
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
name|hbase
operator|.
name|client
operator|.
name|Result
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
name|hbase
operator|.
name|client
operator|.
name|Scan
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
name|hbase
operator|.
name|filter
operator|.
name|BinaryComparator
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
name|hbase
operator|.
name|filter
operator|.
name|CompareFilter
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
name|hbase
operator|.
name|filter
operator|.
name|RowFilter
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
name|hbase
operator|.
name|filter
operator|.
name|WhileMatchFilter
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
name|hbase
operator|.
name|io
operator|.
name|ImmutableBytesWritable
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
name|hbase
operator|.
name|mapreduce
operator|.
name|TableInputFormatBase
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
name|hbase
operator|.
name|mapreduce
operator|.
name|TableSplit
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|hbase
operator|.
name|util
operator|.
name|Writables
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
name|ExprNodeConstantEvaluator
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
name|index
operator|.
name|IndexPredicateAnalyzer
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
name|index
operator|.
name|IndexSearchCondition
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
name|HiveStoragePredicateHandler
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
name|ExprNodeDesc
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
name|TableScanDesc
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
name|serde
operator|.
name|Constants
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
name|ByteStream
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
name|SerDeException
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
name|lazy
operator|.
name|LazyUtils
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|PrimitiveObjectInspector
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
name|hadoop
operator|.
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|TaskAttemptID
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
name|mapreduce
operator|.
name|lib
operator|.
name|input
operator|.
name|FileInputFormat
import|;
end_import

begin_comment
comment|/**  * HiveHBaseTableInputFormat implements InputFormat for HBase storage handler  * tables, decorating an underlying HBase TableInputFormat with extra Hive logic  * such as column pruning and filter pushdown.  */
end_comment

begin_class
specifier|public
class|class
name|HiveHBaseTableInputFormat
extends|extends
name|TableInputFormatBase
implements|implements
name|InputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveHBaseTableInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|jobConf
parameter_list|,
specifier|final
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseSplit
name|hbaseSplit
init|=
operator|(
name|HBaseSplit
operator|)
name|split
decl_stmt|;
name|TableSplit
name|tableSplit
init|=
name|hbaseSplit
operator|.
name|getSplit
argument_list|()
decl_stmt|;
name|String
name|hbaseTableName
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|)
decl_stmt|;
name|setHTable
argument_list|(
operator|new
name|HTable
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|(
name|jobConf
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|hbaseColumnsMapping
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnFamilies
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnQualifiers
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|hbaseColumnFamiliesBytes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|hbaseColumnQualifiersBytes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|iKey
decl_stmt|;
try|try
block|{
name|iKey
operator|=
name|HBaseSerDe
operator|.
name|parseColumnMapping
argument_list|(
name|hbaseColumnsMapping
argument_list|,
name|hbaseColumnFamilies
argument_list|,
name|hbaseColumnFamiliesBytes
argument_list|,
name|hbaseColumnQualifiers
argument_list|,
name|hbaseColumnQualifiersBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|se
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|se
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|readColIDs
init|=
name|ColumnProjectionUtils
operator|.
name|getReadColumnIDs
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|hbaseColumnFamilies
operator|.
name|size
argument_list|()
operator|<
name|readColIDs
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot read more columns than the given table contains."
argument_list|)
throw|;
block|}
name|boolean
name|addAll
init|=
operator|(
name|readColIDs
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|boolean
name|empty
init|=
literal|true
decl_stmt|;
if|if
condition|(
operator|!
name|addAll
condition|)
block|{
for|for
control|(
name|int
name|i
range|:
name|readColIDs
control|)
block|{
if|if
condition|(
name|i
operator|==
name|iKey
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|hbaseColumnQualifiers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|hbaseColumnQualifiersBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|empty
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|// The HBase table's row key maps to a Hive table column. In the corner case when only the
comment|// row key column is selected in Hive, the HBase Scan will be empty i.e. no column family/
comment|// column qualifier will have been added to the scan. We arbitrarily add at least one column
comment|// to the HBase scan so that we can retrieve all of the row keys and return them as the Hive
comment|// tables column projection.
if|if
condition|(
name|empty
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hbaseColumnFamilies
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|iKey
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|hbaseColumnQualifiers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|hbaseColumnQualifiersBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|addAll
condition|)
block|{
break|break;
block|}
block|}
block|}
comment|// If Hive's optimizer gave us a filter to process, convert it to the
comment|// HBase scan form now.
name|tableSplit
operator|=
name|convertFilter
argument_list|(
name|jobConf
argument_list|,
name|scan
argument_list|,
name|tableSplit
argument_list|,
name|iKey
argument_list|)
expr_stmt|;
name|setScan
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|TaskAttemptContext
name|tac
init|=
operator|new
name|TaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|progress
parameter_list|()
block|{
name|reporter
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|recordReader
init|=
name|createRecordReader
argument_list|(
name|tableSplit
argument_list|,
name|tac
argument_list|)
decl_stmt|;
return|return
operator|new
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ImmutableBytesWritable
name|createKey
parameter_list|()
block|{
return|return
operator|new
name|ImmutableBytesWritable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|Result
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
name|float
name|progress
init|=
literal|0.0F
decl_stmt|;
try|try
block|{
name|progress
operator|=
name|recordReader
operator|.
name|getProgress
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|progress
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|ImmutableBytesWritable
name|rowKey
parameter_list|,
name|Result
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|next
init|=
literal|false
decl_stmt|;
try|try
block|{
name|next
operator|=
name|recordReader
operator|.
name|nextKeyValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
condition|)
block|{
name|rowKey
operator|.
name|set
argument_list|(
name|recordReader
operator|.
name|getCurrentValue
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|Writables
operator|.
name|copyWritable
argument_list|(
name|recordReader
operator|.
name|getCurrentValue
argument_list|()
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|next
return|;
block|}
block|}
return|;
block|}
comment|/**    * Converts a filter (which has been pushed down from Hive's optimizer)    * into corresponding restrictions on the HBase scan.  The    * filter should already be in a form which can be fully converted.    *    * @param jobConf configuration for the scan    *    * @param scan the HBase scan object to restrict    *    * @param tableSplit the HBase table split to restrict, or null    * if calculating splits    *    * @param iKey 0-based offset of key column within Hive table    *    * @return converted table split if any    */
specifier|private
name|TableSplit
name|convertFilter
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|TableSplit
name|tableSplit
parameter_list|,
name|int
name|iKey
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|filterExprSerialized
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_EXPR_CONF_STR
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterExprSerialized
operator|==
literal|null
condition|)
block|{
return|return
name|tableSplit
return|;
block|}
name|ExprNodeDesc
name|filterExpr
init|=
name|Utilities
operator|.
name|deserializeExpression
argument_list|(
name|filterExprSerialized
argument_list|,
name|jobConf
argument_list|)
decl_stmt|;
name|String
name|columnNameProperty
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
decl_stmt|;
name|IndexPredicateAnalyzer
name|analyzer
init|=
name|newIndexPredicateAnalyzer
argument_list|(
name|columnNames
operator|.
name|get
argument_list|(
name|iKey
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndexSearchCondition
argument_list|>
name|searchConditions
init|=
operator|new
name|ArrayList
argument_list|<
name|IndexSearchCondition
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|residualPredicate
init|=
name|analyzer
operator|.
name|analyzePredicate
argument_list|(
name|filterExpr
argument_list|,
name|searchConditions
argument_list|)
decl_stmt|;
comment|// There should be no residual since we already negotiated
comment|// that earlier in HBaseStorageHandler.decomposePredicate.
if|if
condition|(
name|residualPredicate
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected residual predicate "
operator|+
name|residualPredicate
operator|.
name|getExprString
argument_list|()
argument_list|)
throw|;
block|}
comment|// There should be exactly one predicate since we already
comment|// negotiated that also.
if|if
condition|(
name|searchConditions
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exactly one search condition expected in push down"
argument_list|)
throw|;
block|}
comment|// Convert the search condition into a restriction on the HBase scan
name|IndexSearchCondition
name|sc
init|=
name|searchConditions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeConstantEvaluator
name|eval
init|=
operator|new
name|ExprNodeConstantEvaluator
argument_list|(
name|sc
operator|.
name|getConstantDesc
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|startRow
decl_stmt|;
try|try
block|{
name|ObjectInspector
name|objInspector
init|=
name|eval
operator|.
name|initialize
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Object
name|writable
init|=
name|eval
operator|.
name|evaluate
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|ByteStream
operator|.
name|Output
name|serializeStream
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
name|LazyUtils
operator|.
name|writePrimitiveUTF8
argument_list|(
name|serializeStream
argument_list|,
name|writable
argument_list|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|objInspector
argument_list|,
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|startRow
operator|=
operator|new
name|byte
index|[
name|serializeStream
operator|.
name|getCount
argument_list|()
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|serializeStream
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|startRow
argument_list|,
literal|0
argument_list|,
name|serializeStream
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
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
comment|// stopRow is exclusive, so pad it with a trailing 0 byte to
comment|// make it compare as the very next value after startRow
name|byte
index|[]
name|stopRow
init|=
operator|new
name|byte
index|[
name|startRow
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|startRow
argument_list|,
literal|0
argument_list|,
name|stopRow
argument_list|,
literal|0
argument_list|,
name|startRow
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableSplit
operator|!=
literal|null
condition|)
block|{
name|tableSplit
operator|=
operator|new
name|TableSplit
argument_list|(
name|tableSplit
operator|.
name|getTableName
argument_list|()
argument_list|,
name|startRow
argument_list|,
name|stopRow
argument_list|,
name|tableSplit
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|scan
operator|.
name|setStartRow
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setStopRow
argument_list|(
name|stopRow
argument_list|)
expr_stmt|;
comment|// Add a WhileMatchFilter to make the scan terminate as soon
comment|// as we see a non-matching key.  This is probably redundant
comment|// since the stopRow above should already take care of it for us.
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|WhileMatchFilter
argument_list|(
operator|new
name|RowFilter
argument_list|(
name|CompareFilter
operator|.
name|CompareOp
operator|.
name|EQUAL
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|startRow
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|tableSplit
return|;
block|}
comment|/**    * Instantiates a new predicate analyzer suitable for    * determining how to push a filter down into the HBase scan,    * based on the rules for what kinds of pushdown we currently support.    *    * @param keyColumnName name of the Hive column mapped to the HBase row key    *    * @return preconfigured predicate analyzer    */
specifier|static
name|IndexPredicateAnalyzer
name|newIndexPredicateAnalyzer
parameter_list|(
name|String
name|keyColumnName
parameter_list|)
block|{
name|IndexPredicateAnalyzer
name|analyzer
init|=
operator|new
name|IndexPredicateAnalyzer
argument_list|()
decl_stmt|;
comment|// for now, we only support equality comparisons
name|analyzer
operator|.
name|addComparisonOp
argument_list|(
literal|"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual"
argument_list|)
expr_stmt|;
comment|// and only on the key column
name|analyzer
operator|.
name|clearAllowedColumnNames
argument_list|()
expr_stmt|;
name|analyzer
operator|.
name|allowColumnName
argument_list|(
name|keyColumnName
argument_list|)
expr_stmt|;
return|return
name|analyzer
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
name|jobConf
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|hbaseTableName
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|)
decl_stmt|;
name|setHTable
argument_list|(
operator|new
name|HTable
argument_list|(
operator|new
name|HBaseConfiguration
argument_list|(
name|jobConf
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|hbaseColumnsMapping
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_COLUMNS_MAPPING
argument_list|)
decl_stmt|;
if|if
condition|(
name|hbaseColumnsMapping
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"hbase.columns.mapping required for HBase Table."
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnFamilies
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnQualifiers
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|hbaseColumnFamiliesBytes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|hbaseColumnQualifiersBytes
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|iKey
decl_stmt|;
try|try
block|{
name|iKey
operator|=
name|HBaseSerDe
operator|.
name|parseColumnMapping
argument_list|(
name|hbaseColumnsMapping
argument_list|,
name|hbaseColumnFamilies
argument_list|,
name|hbaseColumnFamiliesBytes
argument_list|,
name|hbaseColumnQualifiers
argument_list|,
name|hbaseColumnQualifiersBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|se
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|se
argument_list|)
throw|;
block|}
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Take filter pushdown into account while calculating splits; this
comment|// allows us to prune off regions immediately.  Note that although
comment|// the Javadoc for the superclass getSplits says that it returns one
comment|// split per region, the implementation actually takes the scan
comment|// definition into account and excludes regions which don't satisfy
comment|// the start/stop row conditions (HBASE-1829).
name|convertFilter
argument_list|(
name|jobConf
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|,
name|iKey
argument_list|)
expr_stmt|;
comment|// REVIEW:  are we supposed to be applying the getReadColumnIDs
comment|// same as in getRecordReader?
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hbaseColumnFamilies
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|iKey
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|hbaseColumnQualifiers
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|hbaseColumnFamiliesBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|hbaseColumnQualifiersBytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|setScan
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|JobContext
name|jobContext
init|=
operator|new
name|JobContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|job
operator|.
name|getJobID
argument_list|()
argument_list|)
decl_stmt|;
name|Path
index|[]
name|tablePaths
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|InputSplit
argument_list|>
name|splits
init|=
name|super
operator|.
name|getSplits
argument_list|(
name|jobContext
argument_list|)
decl_stmt|;
name|InputSplit
index|[]
name|results
init|=
operator|new
name|InputSplit
index|[
name|splits
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|splits
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|results
index|[
name|i
index|]
operator|=
operator|new
name|HBaseSplit
argument_list|(
operator|(
name|TableSplit
operator|)
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|tablePaths
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
block|}
end_class

end_unit


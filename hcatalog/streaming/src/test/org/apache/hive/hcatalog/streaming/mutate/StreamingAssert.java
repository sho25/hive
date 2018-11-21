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
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|Collections
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
name|common
operator|.
name|TableName
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
name|ValidTxnList
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
name|ValidWriteIdList
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
name|metastore
operator|.
name|IMetaStoreClient
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|NoSuchObjectException
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
name|metastore
operator|.
name|api
operator|.
name|Partition
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|metastore
operator|.
name|api
operator|.
name|TableValidWriteIds
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
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
name|metastore
operator|.
name|txn
operator|.
name|TxnCommonUtils
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
name|AcidInputFormat
operator|.
name|AcidRecordReader
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
name|AcidUtils
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
name|IOConstants
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
name|AcidUtils
operator|.
name|Directory
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
name|RecordIdentifier
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
name|orc
operator|.
name|OrcInputFormat
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
name|orc
operator|.
name|OrcStruct
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
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_class
specifier|public
class|class
name|StreamingAssert
block|{
specifier|public
specifier|static
class|class
name|Factory
block|{
specifier|private
name|IMetaStoreClient
name|metaStoreClient
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|Factory
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|metaStoreClient
operator|=
name|metaStoreClient
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|StreamingAssert
name|newStreamingAssert
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|newStreamingAssert
argument_list|(
name|table
argument_list|,
name|Collections
operator|.
expr|<
name|String
operator|>
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|StreamingAssert
name|newStreamingAssert
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partition
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|StreamingAssert
argument_list|(
name|metaStoreClient
argument_list|,
name|conf
argument_list|,
name|table
argument_list|,
name|partition
argument_list|)
return|;
block|}
block|}
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|partition
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|metaStoreClient
decl_stmt|;
specifier|private
name|Directory
name|dir
decl_stmt|;
specifier|private
name|ValidWriteIdList
name|writeIds
decl_stmt|;
specifier|private
name|ValidTxnList
name|validTxnList
decl_stmt|;
specifier|private
name|List
argument_list|<
name|AcidUtils
operator|.
name|ParsedDelta
argument_list|>
name|currentDeltas
decl_stmt|;
specifier|private
name|long
name|min
decl_stmt|;
specifier|private
name|long
name|max
decl_stmt|;
specifier|private
name|Path
name|partitionLocation
decl_stmt|;
name|StreamingAssert
parameter_list|(
name|IMetaStoreClient
name|metaStoreClient
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partition
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|metaStoreClient
operator|=
name|metaStoreClient
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
name|validTxnList
operator|=
name|metaStoreClient
operator|.
name|getValidTxns
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|,
name|validTxnList
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableValidWriteIds
argument_list|>
name|v
init|=
name|metaStoreClient
operator|.
name|getValidWriteIds
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|TableName
operator|.
name|getDbTable
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|validTxnList
operator|.
name|writeToString
argument_list|()
argument_list|)
decl_stmt|;
name|writeIds
operator|=
name|TxnCommonUtils
operator|.
name|createValidReaderWriteIdList
argument_list|(
name|v
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|partitionLocation
operator|=
name|getPartitionLocation
argument_list|()
expr_stmt|;
name|dir
operator|=
name|AcidUtils
operator|.
name|getAcidState
argument_list|(
name|partitionLocation
argument_list|,
name|conf
argument_list|,
name|writeIds
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|dir
operator|.
name|getObsolete
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|dir
operator|.
name|getOriginalFiles
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|currentDeltas
operator|=
name|dir
operator|.
name|getCurrentDirectories
argument_list|()
expr_stmt|;
name|min
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|max
operator|=
name|Long
operator|.
name|MIN_VALUE
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Files found: "
argument_list|)
expr_stmt|;
for|for
control|(
name|AcidUtils
operator|.
name|ParsedDelta
name|parsedDelta
range|:
name|currentDeltas
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|parsedDelta
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|parsedDelta
operator|.
name|getMaxWriteId
argument_list|()
argument_list|,
name|max
argument_list|)
expr_stmt|;
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|parsedDelta
operator|.
name|getMinWriteId
argument_list|()
argument_list|,
name|min
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|assertExpectedFileCount
parameter_list|(
name|int
name|expectedFileCount
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedFileCount
argument_list|,
name|currentDeltas
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|assertNothingWritten
parameter_list|()
block|{
name|assertExpectedFileCount
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|assertMinWriteId
parameter_list|(
name|long
name|expectedMinWriteId
parameter_list|)
block|{
if|if
condition|(
name|currentDeltas
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"No data"
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
name|expectedMinWriteId
argument_list|,
name|min
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|assertMaxWriteId
parameter_list|(
name|long
name|expectedMaxWriteId
parameter_list|)
block|{
if|if
condition|(
name|currentDeltas
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"No data"
argument_list|)
throw|;
block|}
name|assertEquals
argument_list|(
name|expectedMaxWriteId
argument_list|,
name|max
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Record
argument_list|>
name|readRecords
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|readRecords
argument_list|(
literal|1
argument_list|)
return|;
block|}
comment|/**    * TODO: this would be more flexible doing a SQL select statement rather than using InputFormat directly    * see {@link org.apache.hive.hcatalog.streaming.TestStreaming#checkDataWritten2(Path, long, long, int, String, String...)}    * @param numSplitsExpected    * @return    * @throws Exception    */
name|List
argument_list|<
name|Record
argument_list|>
name|readRecords
parameter_list|(
name|int
name|numSplitsExpected
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|currentDeltas
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"No data"
argument_list|)
throw|;
block|}
name|InputFormat
argument_list|<
name|NullWritable
argument_list|,
name|OrcStruct
argument_list|>
name|inputFormat
init|=
operator|new
name|OrcInputFormat
argument_list|()
decl_stmt|;
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|job
operator|.
name|set
argument_list|(
literal|"mapred.input.dir"
argument_list|,
name|partitionLocation
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|hive_metastoreConstants
operator|.
name|BUCKET_COUNT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|IOConstants
operator|.
name|SCHEMA_EVOLUTION_COLUMNS
argument_list|,
literal|"id,msg"
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|IOConstants
operator|.
name|SCHEMA_EVOLUTION_COLUMNS_TYPES
argument_list|,
literal|"bigint:string"
argument_list|)
expr_stmt|;
name|AcidUtils
operator|.
name|setAcidOperationalProperties
argument_list|(
name|job
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|job
operator|.
name|setBoolean
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|ValidWriteIdList
operator|.
name|VALID_WRITEIDS_KEY
argument_list|,
name|writeIds
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|set
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|,
name|validTxnList
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|numSplitsExpected
argument_list|,
name|splits
operator|.
name|length
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Record
argument_list|>
name|records
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|InputSplit
name|is
range|:
name|splits
control|)
block|{
specifier|final
name|AcidRecordReader
argument_list|<
name|NullWritable
argument_list|,
name|OrcStruct
argument_list|>
name|recordReader
init|=
operator|(
name|AcidRecordReader
argument_list|<
name|NullWritable
argument_list|,
name|OrcStruct
argument_list|>
operator|)
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|is
argument_list|,
name|job
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
decl_stmt|;
name|NullWritable
name|key
init|=
name|recordReader
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|OrcStruct
name|value
init|=
name|recordReader
operator|.
name|createValue
argument_list|()
decl_stmt|;
while|while
condition|(
name|recordReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|RecordIdentifier
name|recordIdentifier
init|=
name|recordReader
operator|.
name|getRecordIdentifier
argument_list|()
decl_stmt|;
name|Record
name|record
init|=
operator|new
name|Record
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
name|recordIdentifier
operator|.
name|getWriteId
argument_list|()
argument_list|,
name|recordIdentifier
operator|.
name|getBucketProperty
argument_list|()
argument_list|,
name|recordIdentifier
operator|.
name|getRowId
argument_list|()
argument_list|)
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|record
argument_list|)
expr_stmt|;
name|records
operator|.
name|add
argument_list|(
name|record
argument_list|)
expr_stmt|;
block|}
name|recordReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|records
return|;
block|}
specifier|private
name|Path
name|getPartitionLocation
parameter_list|()
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
block|{
name|Path
name|partitionLocacation
decl_stmt|;
if|if
condition|(
name|partition
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|partitionLocacation
operator|=
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: calculate this instead. Just because we're writing to the location doesn't mean that it'll
comment|// always be wanted in the meta store right away.
name|List
argument_list|<
name|Partition
argument_list|>
name|partitionEntries
init|=
name|metaStoreClient
operator|.
name|listPartitions
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partition
argument_list|,
operator|(
name|short
operator|)
literal|1
argument_list|)
decl_stmt|;
name|partitionLocacation
operator|=
operator|new
name|Path
argument_list|(
name|partitionEntries
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|partitionLocacation
return|;
block|}
specifier|public
specifier|static
class|class
name|Record
block|{
specifier|private
name|RecordIdentifier
name|recordIdentifier
decl_stmt|;
specifier|private
name|String
name|row
decl_stmt|;
name|Record
parameter_list|(
name|RecordIdentifier
name|recordIdentifier
parameter_list|,
name|String
name|row
parameter_list|)
block|{
name|this
operator|.
name|recordIdentifier
operator|=
name|recordIdentifier
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
specifier|public
name|RecordIdentifier
name|getRecordIdentifier
parameter_list|()
block|{
return|return
name|recordIdentifier
return|;
block|}
specifier|public
name|String
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Record [recordIdentifier="
operator|+
name|recordIdentifier
operator|+
literal|", row="
operator|+
name|row
operator|+
literal|"]"
return|;
block|}
block|}
block|}
end_class

end_unit


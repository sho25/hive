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
name|io
operator|.
name|orc
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|FileSystem
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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
name|io
operator|.
name|AcidOutputFormat
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
name|BucketCodec
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
name|RecordUpdater
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
name|VectorizedOrcAcidRowBatchReader
operator|.
name|ColumnizedDeleteEventRegistry
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
name|VectorizedOrcAcidRowBatchReader
operator|.
name|SortMergedDeleteEventRegistry
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
name|ObjectInspectorFactory
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
name|LongWritable
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
name|orc
operator|.
name|TypeDescription
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * This class tests the VectorizedOrcAcidRowBatchReader by creating an actual split and a set  * of delete delta files. The split is on an insert delta and there are multiple delete deltas  * with interleaving list of record ids that get deleted. Correctness is tested by validating  * that the correct set of record ids are returned in sorted order for valid transactions only.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorizedOrcAcidRowBatchReader
block|{
specifier|private
specifier|static
specifier|final
name|long
name|NUM_ROWID_PER_OTID
init|=
literal|15000L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|NUM_OTID
init|=
literal|10L
decl_stmt|;
specifier|private
name|JobConf
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|root
decl_stmt|;
specifier|static
class|class
name|DummyRow
block|{
name|LongWritable
name|field
decl_stmt|;
name|RecordIdentifier
name|ROW__ID
decl_stmt|;
name|DummyRow
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|field
operator|=
operator|new
name|LongWritable
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|ROW__ID
operator|=
literal|null
expr_stmt|;
block|}
name|DummyRow
parameter_list|(
name|long
name|val
parameter_list|,
name|long
name|rowId
parameter_list|,
name|long
name|origTxn
parameter_list|,
name|int
name|bucket
parameter_list|)
block|{
name|field
operator|=
operator|new
name|LongWritable
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|bucket
operator|=
name|BucketCodec
operator|.
name|V1
operator|.
name|encode
argument_list|(
operator|new
name|AcidOutputFormat
operator|.
name|Options
argument_list|(
literal|null
argument_list|)
operator|.
name|bucket
argument_list|(
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
name|ROW__ID
operator|=
operator|new
name|RecordIdentifier
argument_list|(
name|origTxn
argument_list|,
name|bucket
argument_list|,
name|rowId
argument_list|)
expr_stmt|;
block|}
specifier|static
name|String
name|getColumnNamesProperty
parameter_list|()
block|{
return|return
literal|"field"
return|;
block|}
specifier|static
name|String
name|getColumnTypesProperty
parameter_list|()
block|{
return|return
literal|"bigint"
return|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|JobConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"bucket_count"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TRANSACTIONAL_TABLE_SCAN
operator|.
name|varname
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_OPERATIONAL_PROPERTIES
operator|.
name|varname
argument_list|,
name|AcidUtils
operator|.
name|AcidOperationalProperties
operator|.
name|getDefault
argument_list|()
operator|.
name|toInt
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|IOConstants
operator|.
name|SCHEMA_EVOLUTION_COLUMNS
argument_list|,
name|DummyRow
operator|.
name|getColumnNamesProperty
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|IOConstants
operator|.
name|SCHEMA_EVOLUTION_COLUMNS_TYPES
argument_list|,
name|DummyRow
operator|.
name|getColumnTypesProperty
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
operator|.
name|varname
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_SPLIT_STRATEGY
operator|.
name|varname
argument_list|,
literal|"BI"
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|root
operator|=
operator|new
name|Path
argument_list|(
name|workDir
argument_list|,
literal|"TestVectorizedOrcAcidRowBatch.testDump"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|root
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ObjectInspector
name|inspector
decl_stmt|;
synchronized|synchronized
init|(
name|TestOrcFile
operator|.
name|class
init|)
block|{
name|inspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|DummyRow
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
name|int
name|bucket
init|=
literal|0
decl_stmt|;
name|AcidOutputFormat
operator|.
name|Options
name|options
init|=
operator|new
name|AcidOutputFormat
operator|.
name|Options
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
operator|.
name|bucket
argument_list|(
name|bucket
argument_list|)
operator|.
name|writingBase
argument_list|(
literal|false
argument_list|)
operator|.
name|minimumTransactionId
argument_list|(
literal|1
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
name|NUM_OTID
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|reporter
argument_list|(
name|Reporter
operator|.
name|NULL
argument_list|)
operator|.
name|recordIdColumn
argument_list|(
literal|1
argument_list|)
operator|.
name|finalDestination
argument_list|(
name|root
argument_list|)
decl_stmt|;
name|RecordUpdater
name|updater
init|=
operator|new
name|OrcRecordUpdater
argument_list|(
name|root
argument_list|,
name|options
argument_list|)
decl_stmt|;
comment|// Create a single insert delta with 150,000 rows, with 15000 rowIds per original transaction id.
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_OTID
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|long
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_ROWID_PER_OTID
condition|;
operator|++
name|j
control|)
block|{
name|long
name|payload
init|=
operator|(
name|i
operator|-
literal|1
operator|)
operator|*
name|NUM_ROWID_PER_OTID
operator|+
name|j
decl_stmt|;
name|updater
operator|.
name|insert
argument_list|(
name|i
argument_list|,
operator|new
name|DummyRow
argument_list|(
name|payload
argument_list|,
name|j
argument_list|,
name|i
argument_list|,
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Now create three types of delete deltas- first has rowIds divisible by 2 but not by 3,
comment|// second has rowIds divisible by 3 but not by 2, and the third has rowIds divisible by
comment|// both 2 and 3. This should produce delete deltas that will thoroughly test the sort-merge
comment|// logic when the delete events in the delete delta files interleave in the sort order.
comment|// Create a delete delta that has rowIds divisible by 2 but not by 3. This will produce
comment|// a delete delta file with 50,000 delete events.
name|long
name|currTxnId
init|=
name|NUM_OTID
operator|+
literal|1
decl_stmt|;
name|options
operator|.
name|minimumTransactionId
argument_list|(
name|currTxnId
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
name|currTxnId
argument_list|)
expr_stmt|;
name|updater
operator|=
operator|new
name|OrcRecordUpdater
argument_list|(
name|root
argument_list|,
name|options
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_OTID
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|long
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_ROWID_PER_OTID
condition|;
name|j
operator|+=
literal|1
control|)
block|{
if|if
condition|(
name|j
operator|%
literal|2
operator|==
literal|0
operator|&&
name|j
operator|%
literal|3
operator|!=
literal|0
condition|)
block|{
name|updater
operator|.
name|delete
argument_list|(
name|currTxnId
argument_list|,
operator|new
name|DummyRow
argument_list|(
operator|-
literal|1
argument_list|,
name|j
argument_list|,
name|i
argument_list|,
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Now, create a delete delta that has rowIds divisible by 3 but not by 2. This will produce
comment|// a delete delta file with 25,000 delete events.
name|currTxnId
operator|=
name|NUM_OTID
operator|+
literal|2
expr_stmt|;
name|options
operator|.
name|minimumTransactionId
argument_list|(
name|currTxnId
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
name|currTxnId
argument_list|)
expr_stmt|;
name|updater
operator|=
operator|new
name|OrcRecordUpdater
argument_list|(
name|root
argument_list|,
name|options
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_OTID
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|long
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_ROWID_PER_OTID
condition|;
name|j
operator|+=
literal|1
control|)
block|{
if|if
condition|(
name|j
operator|%
literal|2
operator|!=
literal|0
operator|&&
name|j
operator|%
literal|3
operator|==
literal|0
condition|)
block|{
name|updater
operator|.
name|delete
argument_list|(
name|currTxnId
argument_list|,
operator|new
name|DummyRow
argument_list|(
operator|-
literal|1
argument_list|,
name|j
argument_list|,
name|i
argument_list|,
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Now, create a delete delta that has rowIds divisible by both 3 and 2. This will produce
comment|// a delete delta file with 25,000 delete events.
name|currTxnId
operator|=
name|NUM_OTID
operator|+
literal|3
expr_stmt|;
name|options
operator|.
name|minimumTransactionId
argument_list|(
name|currTxnId
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
name|currTxnId
argument_list|)
expr_stmt|;
name|updater
operator|=
operator|new
name|OrcRecordUpdater
argument_list|(
name|root
argument_list|,
name|options
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|NUM_OTID
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|long
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUM_ROWID_PER_OTID
condition|;
name|j
operator|+=
literal|1
control|)
block|{
if|if
condition|(
name|j
operator|%
literal|2
operator|==
literal|0
operator|&&
name|j
operator|%
literal|3
operator|==
literal|0
condition|)
block|{
name|updater
operator|.
name|delete
argument_list|(
name|currTxnId
argument_list|,
operator|new
name|DummyRow
argument_list|(
operator|-
literal|1
argument_list|,
name|j
argument_list|,
name|i
argument_list|,
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|OrcSplit
argument_list|>
name|getSplits
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_OPERATIONAL_PROPERTIES
operator|.
name|varname
argument_list|,
name|AcidUtils
operator|.
name|AcidOperationalProperties
operator|.
name|getDefault
argument_list|()
operator|.
name|toInt
argument_list|()
argument_list|)
expr_stmt|;
name|OrcInputFormat
operator|.
name|Context
name|context
init|=
operator|new
name|OrcInputFormat
operator|.
name|Context
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|OrcInputFormat
operator|.
name|FileGenerator
name|gen
init|=
operator|new
name|OrcInputFormat
operator|.
name|FileGenerator
argument_list|(
name|context
argument_list|,
name|fs
argument_list|,
name|root
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|OrcInputFormat
operator|.
name|AcidDirInfo
name|adi
init|=
name|gen
operator|.
name|call
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|OrcInputFormat
operator|.
name|SplitStrategy
argument_list|<
name|?
argument_list|>
argument_list|>
name|splitStrategies
init|=
name|OrcInputFormat
operator|.
name|determineSplitStrategies
argument_list|(
literal|null
argument_list|,
name|context
argument_list|,
name|adi
operator|.
name|fs
argument_list|,
name|adi
operator|.
name|splitPath
argument_list|,
name|adi
operator|.
name|baseFiles
argument_list|,
name|adi
operator|.
name|parsedDeltas
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|splitStrategies
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|OrcSplit
argument_list|>
name|splits
init|=
operator|(
operator|(
name|OrcInputFormat
operator|.
name|ACIDSplitStrategy
operator|)
name|splitStrategies
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getSplits
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"file://"
operator|+
name|root
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"delta_0000001_0000010_0000/bucket_00000"
argument_list|,
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|isOriginal
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|splits
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVectorizedOrcAcidRowBatchReader
parameter_list|()
throws|throws
name|Exception
block|{
name|testVectorizedOrcAcidRowBatchReader
argument_list|(
name|ColumnizedDeleteEventRegistry
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// To test the SortMergedDeleteEventRegistry, we need to explicitly set the
comment|// HIVE_TRANSACTIONAL_NUM_EVENTS_IN_MEMORY constant to a smaller value.
name|int
name|oldValue
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TRANSACTIONAL_NUM_EVENTS_IN_MEMORY
operator|.
name|varname
argument_list|,
literal|1000000
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TRANSACTIONAL_NUM_EVENTS_IN_MEMORY
operator|.
name|varname
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|testVectorizedOrcAcidRowBatchReader
argument_list|(
name|SortMergedDeleteEventRegistry
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Restore the old value.
name|conf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TRANSACTIONAL_NUM_EVENTS_IN_MEMORY
operator|.
name|varname
argument_list|,
name|oldValue
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testVectorizedOrcAcidRowBatchReader
parameter_list|(
name|String
name|deleteEventRegistry
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|OrcSplit
argument_list|>
name|splits
init|=
name|getSplits
argument_list|()
decl_stmt|;
comment|// Mark one of the transactions as an exception to test that invalid transactions
comment|// are being handled properly.
name|conf
operator|.
name|set
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|,
literal|"14:1:1:5"
argument_list|)
expr_stmt|;
comment|// Exclude transaction 5
name|VectorizedOrcAcidRowBatchReader
name|vectorizedReader
init|=
operator|new
name|VectorizedOrcAcidRowBatchReader
argument_list|(
name|splits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|conf
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleteEventRegistry
operator|.
name|equals
argument_list|(
name|ColumnizedDeleteEventRegistry
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|vectorizedReader
operator|.
name|getDeleteEventRegistry
argument_list|()
operator|instanceof
name|ColumnizedDeleteEventRegistry
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|deleteEventRegistry
operator|.
name|equals
argument_list|(
name|SortMergedDeleteEventRegistry
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|vectorizedReader
operator|.
name|getDeleteEventRegistry
argument_list|()
operator|instanceof
name|SortMergedDeleteEventRegistry
argument_list|)
expr_stmt|;
block|}
name|TypeDescription
name|schema
init|=
name|OrcInputFormat
operator|.
name|getDesiredRowTypeDescr
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|vectorizedRowBatch
init|=
name|schema
operator|.
name|createRowBatch
argument_list|()
decl_stmt|;
name|vectorizedRowBatch
operator|.
name|setPartitionInfo
argument_list|(
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// set data column count as 1.
name|long
name|previousPayload
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
while|while
condition|(
name|vectorizedReader
operator|.
name|next
argument_list|(
literal|null
argument_list|,
name|vectorizedRowBatch
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|vectorizedRowBatch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
name|LongColumnVector
name|col
init|=
operator|(
name|LongColumnVector
operator|)
name|vectorizedRowBatch
operator|.
name|cols
index|[
literal|0
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
name|vectorizedRowBatch
operator|.
name|size
condition|;
operator|++
name|i
control|)
block|{
name|int
name|idx
init|=
name|vectorizedRowBatch
operator|.
name|selected
index|[
name|i
index|]
decl_stmt|;
name|long
name|payload
init|=
name|col
operator|.
name|vector
index|[
name|idx
index|]
decl_stmt|;
name|long
name|otid
init|=
operator|(
name|payload
operator|/
name|NUM_ROWID_PER_OTID
operator|)
operator|+
literal|1
decl_stmt|;
name|long
name|rowId
init|=
name|payload
operator|%
name|NUM_ROWID_PER_OTID
decl_stmt|;
name|assertFalse
argument_list|(
name|rowId
operator|%
literal|2
operator|==
literal|0
operator|||
name|rowId
operator|%
literal|3
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|otid
operator|!=
literal|5
argument_list|)
expr_stmt|;
comment|// Check that txn#5 has been excluded.
name|assertTrue
argument_list|(
name|payload
operator|>
name|previousPayload
argument_list|)
expr_stmt|;
comment|// Check that the data is in sorted order.
name|previousPayload
operator|=
name|payload
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCanCreateVectorizedAcidRowBatchReaderOnSplit
parameter_list|()
throws|throws
name|Exception
block|{
name|OrcSplit
name|mockSplit
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|OrcSplit
operator|.
name|class
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TXN_OPERATIONAL_PROPERTIES
operator|.
name|varname
argument_list|,
name|AcidUtils
operator|.
name|AcidOperationalProperties
operator|.
name|getDefault
argument_list|()
operator|.
name|toInt
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockSplit
operator|.
name|isOriginal
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Test false when trying to create a vectorized ACID row batch reader when reading originals.
name|assertFalse
argument_list|(
name|VectorizedOrcAcidRowBatchReader
operator|.
name|canCreateVectorizedAcidRowBatchReaderOnSplit
argument_list|(
name|conf
argument_list|,
name|mockSplit
argument_list|)
argument_list|)
expr_stmt|;
comment|// A positive test case.
name|Mockito
operator|.
name|when
argument_list|(
name|mockSplit
operator|.
name|isOriginal
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|VectorizedOrcAcidRowBatchReader
operator|.
name|canCreateVectorizedAcidRowBatchReaderOnSplit
argument_list|(
name|conf
argument_list|,
name|mockSplit
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


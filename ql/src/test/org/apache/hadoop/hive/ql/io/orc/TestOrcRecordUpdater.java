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
name|assertNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|IntWritable
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
name|io
operator|.
name|Text
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
name|impl
operator|.
name|OrcAcidUtils
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
name|tools
operator|.
name|FileDump
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

begin_class
specifier|public
class|class
name|TestOrcRecordUpdater
block|{
annotation|@
name|Test
specifier|public
name|void
name|testAccessors
parameter_list|()
throws|throws
name|Exception
block|{
name|OrcStruct
name|event
init|=
operator|new
name|OrcStruct
argument_list|(
name|OrcRecordUpdater
operator|.
name|FIELDS
argument_list|)
decl_stmt|;
name|event
operator|.
name|setFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|OPERATION
argument_list|,
operator|new
name|IntWritable
argument_list|(
name|OrcRecordUpdater
operator|.
name|INSERT_OPERATION
argument_list|)
argument_list|)
expr_stmt|;
name|event
operator|.
name|setFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|CURRENT_TRANSACTION
argument_list|,
operator|new
name|LongWritable
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|event
operator|.
name|setFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|ORIGINAL_TRANSACTION
argument_list|,
operator|new
name|LongWritable
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|event
operator|.
name|setFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|BUCKET
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|event
operator|.
name|setFieldValue
argument_list|(
name|OrcRecordUpdater
operator|.
name|ROW_ID
argument_list|,
operator|new
name|LongWritable
argument_list|(
literal|300
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|OrcRecordUpdater
operator|.
name|INSERT_OPERATION
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOperation
argument_list|(
name|event
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOriginalTransaction
argument_list|(
name|event
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|OrcRecordUpdater
operator|.
name|getCurrentTransaction
argument_list|(
name|event
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|OrcRecordUpdater
operator|.
name|getBucket
argument_list|(
name|event
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|300
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|event
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
specifier|static
class|class
name|MyRow
block|{
name|Text
name|field
decl_stmt|;
name|RecordIdentifier
name|ROW__ID
decl_stmt|;
name|MyRow
parameter_list|(
name|String
name|val
parameter_list|)
block|{
name|field
operator|=
operator|new
name|Text
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|ROW__ID
operator|=
literal|null
expr_stmt|;
block|}
name|MyRow
parameter_list|(
name|String
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
name|Text
argument_list|(
name|val
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriter
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|root
init|=
operator|new
name|Path
argument_list|(
name|workDir
argument_list|,
literal|"testWriter"
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
comment|// Must use raw local because the checksummer doesn't honor flushes.
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
operator|.
name|getRaw
argument_list|()
decl_stmt|;
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
name|MyRow
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
literal|10
argument_list|)
operator|.
name|writingBase
argument_list|(
literal|false
argument_list|)
operator|.
name|minimumTransactionId
argument_list|(
literal|10
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
literal|19
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
name|updater
operator|.
name|insert
argument_list|(
literal|11
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"first"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|11
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"second"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|11
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"third"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|flush
argument_list|()
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|12
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"fourth"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|12
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"fifth"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// Check the stats
name|assertEquals
argument_list|(
literal|5L
argument_list|,
name|updater
operator|.
name|getStats
argument_list|()
operator|.
name|getRowCount
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|bucketPath
init|=
name|AcidUtils
operator|.
name|createFilename
argument_list|(
name|root
argument_list|,
name|options
argument_list|)
decl_stmt|;
name|Path
name|sidePath
init|=
name|OrcAcidUtils
operator|.
name|getSideFile
argument_list|(
name|bucketPath
argument_list|)
decl_stmt|;
name|DataInputStream
name|side
init|=
name|fs
operator|.
name|open
argument_list|(
name|sidePath
argument_list|)
decl_stmt|;
comment|// read the stopping point for the first flush and make sure we only see
comment|// 3 rows
name|long
name|len
init|=
name|side
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|bucketPath
argument_list|,
operator|new
name|OrcFile
operator|.
name|ReaderOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
operator|.
name|maxLength
argument_list|(
name|len
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|reader
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// read the second flush and make sure we see all 5 rows
name|len
operator|=
name|side
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|side
operator|.
name|close
argument_list|()
expr_stmt|;
name|reader
operator|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|bucketPath
argument_list|,
operator|new
name|OrcFile
operator|.
name|ReaderOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
operator|.
name|maxLength
argument_list|(
name|len
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|reader
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
expr_stmt|;
name|RecordReader
name|rows
init|=
name|reader
operator|.
name|rows
argument_list|()
decl_stmt|;
comment|// check the contents of the file
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|OrcStruct
name|row
init|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|OrcRecordUpdater
operator|.
name|INSERT_OPERATION
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOperation
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|OrcRecordUpdater
operator|.
name|getCurrentTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOriginalTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|getBucketId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first"
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
operator|.
name|getFieldValue
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|getBucketId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"second"
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
operator|.
name|getFieldValue
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|getBucketId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"third"
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
operator|.
name|getFieldValue
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|OrcRecordUpdater
operator|.
name|getCurrentTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOriginalTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|getBucketId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fourth"
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
operator|.
name|getFieldValue
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fifth"
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
operator|.
name|getFieldValue
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
comment|// add one more record and close
name|updater
operator|.
name|insert
argument_list|(
literal|20
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"sixth"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|reader
operator|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|bucketPath
argument_list|,
operator|new
name|OrcFile
operator|.
name|ReaderOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|reader
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6L
argument_list|,
name|updater
operator|.
name|getStats
argument_list|()
operator|.
name|getRowCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|sidePath
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|int
name|getBucketId
parameter_list|(
name|OrcStruct
name|row
parameter_list|)
block|{
name|int
name|bucketValue
init|=
name|OrcRecordUpdater
operator|.
name|getBucket
argument_list|(
name|row
argument_list|)
decl_stmt|;
return|return
name|BucketCodec
operator|.
name|determineVersion
argument_list|(
name|bucketValue
argument_list|)
operator|.
name|decodeWriterId
argument_list|(
name|bucketValue
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriterTblProperties
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|root
init|=
operator|new
name|Path
argument_list|(
name|workDir
argument_list|,
literal|"testWriterTblProperties"
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
comment|// Must use raw local because the checksummer doesn't honor flushes.
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
operator|.
name|getRaw
argument_list|()
decl_stmt|;
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
name|MyRow
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
name|Properties
name|tblProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|tblProps
operator|.
name|setProperty
argument_list|(
literal|"orc.compress"
argument_list|,
literal|"SNAPPY"
argument_list|)
expr_stmt|;
name|tblProps
operator|.
name|setProperty
argument_list|(
literal|"orc.compress.size"
argument_list|,
literal|"8192"
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_BASE_DELTA_RATIO
argument_list|,
literal|4
argument_list|)
expr_stmt|;
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
literal|10
argument_list|)
operator|.
name|writingBase
argument_list|(
literal|false
argument_list|)
operator|.
name|minimumTransactionId
argument_list|(
literal|10
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
literal|19
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
name|finalDestination
argument_list|(
name|root
argument_list|)
operator|.
name|tableProperties
argument_list|(
name|tblProps
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
name|updater
operator|.
name|insert
argument_list|(
literal|11
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"first"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|11
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"second"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|11
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"third"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|flush
argument_list|()
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|12
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"fourth"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|insert
argument_list|(
literal|12
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"fifth"
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|flush
argument_list|()
expr_stmt|;
name|PrintStream
name|origOut
init|=
name|System
operator|.
name|out
decl_stmt|;
name|ByteArrayOutputStream
name|myOut
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|myOut
argument_list|)
argument_list|)
expr_stmt|;
name|FileDump
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
name|root
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|String
name|outDump
init|=
operator|new
name|String
argument_list|(
name|myOut
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Compression: SNAPPY"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Compression size: 2048"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|origOut
argument_list|)
expr_stmt|;
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdates
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|root
init|=
operator|new
name|Path
argument_list|(
name|workDir
argument_list|,
literal|"testUpdates"
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|root
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
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
name|MyRow
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
literal|20
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
literal|100
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
literal|100
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
name|updater
operator|.
name|update
argument_list|(
literal|100
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|"update"
argument_list|,
literal|30
argument_list|,
literal|10
argument_list|,
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
name|updater
operator|.
name|delete
argument_list|(
literal|100
argument_list|,
operator|new
name|MyRow
argument_list|(
literal|""
argument_list|,
literal|60
argument_list|,
literal|40
argument_list|,
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1L
argument_list|,
name|updater
operator|.
name|getStats
argument_list|()
operator|.
name|getRowCount
argument_list|()
argument_list|)
expr_stmt|;
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Path
name|bucketPath
init|=
name|AcidUtils
operator|.
name|createFilename
argument_list|(
name|root
argument_list|,
name|options
argument_list|)
decl_stmt|;
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|bucketPath
argument_list|,
operator|new
name|OrcFile
operator|.
name|ReaderOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|filesystem
argument_list|(
name|fs
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|reader
operator|.
name|getNumberOfRows
argument_list|()
argument_list|)
expr_stmt|;
name|RecordReader
name|rows
init|=
name|reader
operator|.
name|rows
argument_list|()
decl_stmt|;
comment|// check the contents of the file
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|OrcStruct
name|row
init|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|OrcRecordUpdater
operator|.
name|UPDATE_OPERATION
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOperation
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|OrcRecordUpdater
operator|.
name|getCurrentTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOriginalTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
argument_list|,
name|OrcRecordUpdater
operator|.
name|getBucket
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|30
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"update"
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
operator|.
name|getFieldValue
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
operator|(
name|OrcStruct
operator|)
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|OrcRecordUpdater
operator|.
name|getCurrentTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|40
argument_list|,
name|OrcRecordUpdater
operator|.
name|getOriginalTransaction
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
argument_list|,
name|OrcRecordUpdater
operator|.
name|getBucket
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|OrcRecordUpdater
operator|.
name|getRowId
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|OrcRecordUpdater
operator|.
name|getRow
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|rows
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


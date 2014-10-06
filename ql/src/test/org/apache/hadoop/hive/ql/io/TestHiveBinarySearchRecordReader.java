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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyObject
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|inOrder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|IOException
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
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|HiveInputFormat
operator|.
name|HiveInputSplit
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
name|MapredWork
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
name|ql
operator|.
name|plan
operator|.
name|TableDesc
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
name|GenericUDFOPEqual
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
name|GenericUDFOPEqualOrGreaterThan
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
name|GenericUDFOPEqualOrLessThan
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
name|GenericUDFOPGreaterThan
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
name|GenericUDFOPLessThan
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
name|columnar
operator|.
name|BytesRefArrayWritable
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
name|io
operator|.
name|WritableComparable
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
name|mockito
operator|.
name|InOrder
import|;
end_import

begin_comment
comment|/**  * TestHiveBinarySearchRecordReader.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveBinarySearchRecordReader
extends|extends
name|TestCase
block|{
specifier|private
name|RCFileRecordReader
name|rcfReader
decl_stmt|;
specifier|private
name|JobConf
name|conf
decl_stmt|;
specifier|private
name|TestHiveInputSplit
name|hiveSplit
decl_stmt|;
specifier|private
name|HiveContextAwareRecordReader
name|hbsReader
decl_stmt|;
specifier|private
name|IOContext
name|ioContext
decl_stmt|;
specifier|private
specifier|static
class|class
name|TestHiveInputSplit
extends|extends
name|HiveInputSplit
block|{
annotation|@
name|Override
specifier|public
name|long
name|getStart
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
literal|100
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestHiveRecordReader
parameter_list|<
name|K
extends|extends
name|WritableComparable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
extends|extends
name|HiveContextAwareRecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|public
name|TestHiveRecordReader
parameter_list|(
name|RecordReader
name|recordReader
parameter_list|,
name|JobConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|recordReader
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|K
name|createKey
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|createValue
parameter_list|()
block|{
return|return
literal|null
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
name|boolean
name|doNext
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|super
operator|.
name|doNext
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doClose
parameter_list|()
throws|throws
name|IOException
block|{      }
block|}
specifier|private
name|void
name|resetIOContext
parameter_list|()
block|{
name|conf
operator|.
name|set
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|,
literal|"TestHiveBinarySearchRecordReader"
argument_list|)
expr_stmt|;
name|ioContext
operator|=
name|IOContext
operator|.
name|get
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setUseSorted
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setIsBinarySearching
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setEndBinarySearch
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
operator|new
name|JobConf
argument_list|()
expr_stmt|;
name|resetIOContext
argument_list|()
expr_stmt|;
name|rcfReader
operator|=
name|mock
argument_list|(
name|RCFileRecordReader
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rcfReader
operator|.
name|next
argument_list|(
operator|(
name|LongWritable
operator|)
name|anyObject
argument_list|()
argument_list|,
operator|(
name|BytesRefArrayWritable
operator|)
name|anyObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Since the start is 0, and the length is 100, the first call to sync should be with the value
comment|// 50 so return that for getPos()
name|when
argument_list|(
name|rcfReader
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|50L
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hive.input.format.sorted"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TableDesc
name|tblDesc
init|=
name|Utilities
operator|.
name|defaultTd
decl_stmt|;
name|PartitionDesc
name|partDesc
init|=
operator|new
name|PartitionDesc
argument_list|(
name|tblDesc
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pt
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
decl_stmt|;
name|pt
operator|.
name|put
argument_list|(
literal|"/tmp/testfolder"
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
name|MapredWork
name|mrwork
init|=
operator|new
name|MapredWork
argument_list|()
decl_stmt|;
name|mrwork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setPathToPartitionInfo
argument_list|(
name|pt
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|conf
argument_list|,
name|mrwork
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
literal|"hive"
argument_list|)
argument_list|)
expr_stmt|;
name|hiveSplit
operator|=
operator|new
name|TestHiveInputSplit
argument_list|()
expr_stmt|;
name|hbsReader
operator|=
operator|new
name|TestHiveRecordReader
argument_list|(
name|rcfReader
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|hbsReader
operator|.
name|initIOContext
argument_list|(
name|hiveSplit
argument_list|,
name|conf
argument_list|,
name|Class
operator|.
name|class
argument_list|,
name|rcfReader
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|executeDoNext
parameter_list|(
name|HiveContextAwareRecordReader
name|hbsReader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|hbsReader
operator|.
name|next
argument_list|(
name|hbsReader
operator|.
name|createKey
argument_list|()
argument_list|,
name|hbsReader
operator|.
name|createValue
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|testNonLinearGreaterThan
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rcfReader
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|25L
argument_list|)
expr_stmt|;
comment|// By setting the comparison to greater, the search should use the block [0, 50]
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|25
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testNonLinearLessThan
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rcfReader
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|75L
argument_list|)
expr_stmt|;
comment|// By setting the comparison to less, the search should use the block [50, 100]
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|75
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testNonLinearEqualTo
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rcfReader
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|25L
argument_list|)
expr_stmt|;
comment|// By setting the comparison to equal, the search should use the block [0, 50]
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|25
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHitLastBlock
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rcfReader
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|100L
argument_list|)
expr_stmt|;
comment|// When sync is called it will return 100, the value signaling the end of the file, this should
comment|// result in a call to sync to the beginning of the block it was searching [50, 100], and it
comment|// should continue normally
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|rcfReader
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|75
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testHitSamePositionTwice
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// When getPos is called it should return the same value, signaling the end of the search, so
comment|// the search should continue linearly and it should sync to the beginning of the block [0, 50]
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|rcfReader
argument_list|)
decl_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|25
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testResetRange
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|InOrder
name|inOrder
init|=
name|inOrder
argument_list|(
name|rcfReader
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rcfReader
operator|.
name|getPos
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|75L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|75
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setEndBinarySearch
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// This should make the search linear, sync to the beginning of the block being searched
comment|// [50, 100], set the comparison to be null, and the flag to reset the range should be unset
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|inOrder
operator|.
name|verify
argument_list|(
name|rcfReader
argument_list|)
operator|.
name|sync
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ioContext
operator|.
name|shouldEndBinarySearch
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testEqualOpClass
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
name|GenericUDFOPEqual
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setIsBinarySearching
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testLessThanOpClass
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
name|GenericUDFOPLessThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testLessThanOrEqualOpClass
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
name|GenericUDFOPEqualOrLessThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGreaterThanOpClass
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
name|GenericUDFOPGreaterThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setIsBinarySearching
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGreaterThanOrEqualOpClass
parameter_list|()
throws|throws
name|Exception
block|{
name|init
argument_list|()
expr_stmt|;
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
name|GenericUDFOPEqualOrGreaterThan
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setIsBinarySearching
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setComparison
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|executeDoNext
argument_list|(
name|hbsReader
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testNonLinearGreaterThan
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testNonLinearLessThan
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testNonLinearEqualTo
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testHitLastBlock
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testHitSamePositionTwice
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testResetRange
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testEqualOpClass
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testLessThanOpClass
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testLessThanOrEqualOpClass
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testGreaterThanOpClass
argument_list|()
expr_stmt|;
operator|new
name|TestHiveBinarySearchRecordReader
argument_list|()
operator|.
name|testGreaterThanOrEqualOpClass
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


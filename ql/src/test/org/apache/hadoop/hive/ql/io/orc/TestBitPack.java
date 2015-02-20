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
name|assertArrayEquals
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
name|assertEquals
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|java
operator|.
name|util
operator|.
name|Random
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|collect
operator|.
name|Lists
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
name|primitives
operator|.
name|Longs
import|;
end_import

begin_class
specifier|public
class|class
name|TestBitPack
block|{
specifier|private
specifier|static
specifier|final
name|int
name|SIZE
init|=
literal|100
decl_stmt|;
specifier|private
specifier|static
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|100
argument_list|)
decl_stmt|;
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
name|Configuration
name|conf
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|Path
name|testFilePath
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|testCaseName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|openFileSystem
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
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
name|testFilePath
operator|=
operator|new
name|Path
argument_list|(
name|workDir
argument_list|,
literal|"TestOrcFile."
operator|+
name|testCaseName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|".orc"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|testFilePath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
index|[]
name|deltaEncode
parameter_list|(
name|long
index|[]
name|inp
parameter_list|)
block|{
name|long
index|[]
name|output
init|=
operator|new
name|long
index|[
name|inp
operator|.
name|length
index|]
decl_stmt|;
name|SerializationUtils
name|utils
init|=
operator|new
name|SerializationUtils
argument_list|()
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
name|inp
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|output
index|[
name|i
index|]
operator|=
name|utils
operator|.
name|zigzagEncode
argument_list|(
name|inp
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|output
return|;
block|}
specifier|private
name|long
name|nextLong
parameter_list|(
name|Random
name|rng
parameter_list|,
name|long
name|n
parameter_list|)
block|{
name|long
name|bits
decl_stmt|,
name|val
decl_stmt|;
do|do
block|{
name|bits
operator|=
operator|(
name|rng
operator|.
name|nextLong
argument_list|()
operator|<<
literal|1
operator|)
operator|>>>
literal|1
expr_stmt|;
name|val
operator|=
name|bits
operator|%
name|n
expr_stmt|;
block|}
do|while
condition|(
name|bits
operator|-
name|val
operator|+
operator|(
name|n
operator|-
literal|1
operator|)
operator|<
literal|0L
condition|)
do|;
return|return
name|val
return|;
block|}
specifier|private
name|void
name|runTest
parameter_list|(
name|int
name|numBits
parameter_list|)
throws|throws
name|IOException
block|{
name|long
index|[]
name|inp
init|=
operator|new
name|long
index|[
name|SIZE
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
name|SIZE
condition|;
name|i
operator|++
control|)
block|{
name|long
name|val
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|numBits
operator|<=
literal|32
condition|)
block|{
if|if
condition|(
name|numBits
operator|==
literal|1
condition|)
block|{
name|val
operator|=
operator|-
literal|1
operator|*
name|rand
operator|.
name|nextInt
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|val
operator|=
name|rand
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
name|numBits
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|val
operator|=
name|nextLong
argument_list|(
name|rand
argument_list|,
operator|(
name|long
operator|)
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
name|numBits
operator|-
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|val
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|val
operator|=
operator|-
name|val
expr_stmt|;
block|}
name|inp
index|[
name|i
index|]
operator|=
name|val
expr_stmt|;
block|}
name|long
index|[]
name|deltaEncoded
init|=
name|deltaEncode
argument_list|(
name|inp
argument_list|)
decl_stmt|;
name|long
name|minInput
init|=
name|Collections
operator|.
name|min
argument_list|(
name|Longs
operator|.
name|asList
argument_list|(
name|deltaEncoded
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|maxInput
init|=
name|Collections
operator|.
name|max
argument_list|(
name|Longs
operator|.
name|asList
argument_list|(
name|deltaEncoded
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|rangeInput
init|=
name|maxInput
operator|-
name|minInput
decl_stmt|;
name|SerializationUtils
name|utils
init|=
operator|new
name|SerializationUtils
argument_list|()
decl_stmt|;
name|int
name|fixedWidth
init|=
name|utils
operator|.
name|findClosestNumBits
argument_list|(
name|rangeInput
argument_list|)
decl_stmt|;
name|TestInStream
operator|.
name|OutputCollector
name|collect
init|=
operator|new
name|TestInStream
operator|.
name|OutputCollector
argument_list|()
decl_stmt|;
name|OutStream
name|output
init|=
operator|new
name|OutStream
argument_list|(
literal|"test"
argument_list|,
name|SIZE
argument_list|,
literal|null
argument_list|,
name|collect
argument_list|)
decl_stmt|;
name|utils
operator|.
name|writeInts
argument_list|(
name|deltaEncoded
argument_list|,
literal|0
argument_list|,
name|deltaEncoded
operator|.
name|length
argument_list|,
name|fixedWidth
argument_list|,
name|output
argument_list|)
expr_stmt|;
name|output
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ByteBuffer
name|inBuf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|collect
operator|.
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|collect
operator|.
name|buffer
operator|.
name|setByteBuffer
argument_list|(
name|inBuf
argument_list|,
literal|0
argument_list|,
name|collect
operator|.
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|inBuf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|long
index|[]
name|buff
init|=
operator|new
name|long
index|[
name|SIZE
index|]
decl_stmt|;
name|utils
operator|.
name|readInts
argument_list|(
name|buff
argument_list|,
literal|0
argument_list|,
name|SIZE
argument_list|,
name|fixedWidth
argument_list|,
name|InStream
operator|.
name|create
argument_list|(
literal|null
argument_list|,
literal|"test"
argument_list|,
operator|new
name|ByteBuffer
index|[]
block|{
name|inBuf
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
literal|0
block|}
argument_list|,
name|inBuf
operator|.
name|remaining
argument_list|()
argument_list|,
literal|null
argument_list|,
name|SIZE
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|SIZE
condition|;
name|i
operator|++
control|)
block|{
name|buff
index|[
name|i
index|]
operator|=
name|utils
operator|.
name|zigzagDecode
argument_list|(
name|buff
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|numBits
argument_list|,
name|fixedWidth
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|inp
argument_list|,
name|buff
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test01BitPacking1Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test02BitPacking2Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test03BitPacking3Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test04BitPacking4Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test05BitPacking5Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test06BitPacking6Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|6
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test07BitPacking7Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|7
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test08BitPacking8Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|8
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test09BitPacking9Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|9
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test10BitPacking10Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test11BitPacking11Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|11
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test12BitPacking12Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|12
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test13BitPacking13Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|13
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test14BitPacking14Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|14
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test15BitPacking15Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|15
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test16BitPacking16Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|16
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test17BitPacking17Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|17
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test18BitPacking18Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|18
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test19BitPacking19Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|19
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test20BitPacking20Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|20
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test21BitPacking21Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|21
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test22BitPacking22Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|22
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test23BitPacking23Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|23
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test24BitPacking24Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|24
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test26BitPacking26Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|26
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test28BitPacking28Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|28
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test30BitPacking30Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|30
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test32BitPacking32Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|32
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test40BitPacking40Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|40
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test48BitPacking48Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|48
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test56BitPacking56Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|56
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test64BitPacking64Bit
parameter_list|()
throws|throws
name|IOException
block|{
name|runTest
argument_list|(
literal|64
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBitPack64Large
parameter_list|()
throws|throws
name|Exception
block|{
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
name|Long
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
name|size
init|=
literal|1080832
decl_stmt|;
name|long
index|[]
name|inp
init|=
operator|new
name|long
index|[
name|size
index|]
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|1234
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|inp
index|[
name|i
index|]
operator|=
name|rand
operator|.
name|nextLong
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Long
argument_list|>
name|input
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Longs
operator|.
name|asList
argument_list|(
name|inp
argument_list|)
argument_list|)
decl_stmt|;
name|Writer
name|writer
init|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|testFilePath
argument_list|,
name|OrcFile
operator|.
name|writerOptions
argument_list|(
name|conf
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|compress
argument_list|(
name|CompressionKind
operator|.
name|ZLIB
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Long
name|l
range|:
name|input
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
name|l
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|testFilePath
argument_list|,
name|OrcFile
operator|.
name|readerOptions
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
name|RecordReader
name|rows
init|=
name|reader
operator|.
name|rows
argument_list|()
decl_stmt|;
name|int
name|idx
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|rows
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Object
name|row
init|=
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|input
operator|.
name|get
argument_list|(
name|idx
operator|++
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|,
operator|(
operator|(
name|LongWritable
operator|)
name|row
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


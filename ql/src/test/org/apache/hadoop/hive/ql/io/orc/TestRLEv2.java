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

begin_class
specifier|public
class|class
name|TestRLEv2
block|{
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
name|Path
name|testFilePath
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
name|FileSystem
name|fs
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
literal|"TestRLEv2."
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
annotation|@
name|Test
specifier|public
name|void
name|testFixedDeltaZero
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
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
literal|5120
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
literal|123
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// 10 runs of 512 elements. Each run has 2 bytes header, 2 bytes base (base = 123,
comment|// zigzag encoded varint) and 1 byte delta (delta = 0). In total, 5 bytes per run.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 50"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFixedDeltaOne
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
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
literal|5120
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
name|i
operator|%
literal|512
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// 10 runs of 512 elements. Each run has 2 bytes header, 1 byte base (base = 0)
comment|// and 1 byte delta (delta = 1). In total, 4 bytes per run.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 40"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFixedDeltaOneDescending
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
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
literal|5120
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
literal|512
operator|-
operator|(
name|i
operator|%
literal|512
operator|)
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// 10 runs of 512 elements. Each run has 2 bytes header, 2 byte base (base = 512, zigzag + varint)
comment|// and 1 byte delta (delta = 1). In total, 5 bytes per run.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 50"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFixedDeltaLarge
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
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
literal|5120
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
name|i
operator|%
literal|512
operator|+
operator|(
operator|(
name|i
operator|%
literal|512
operator|)
operator|*
literal|100
operator|)
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// 10 runs of 512 elements. Each run has 2 bytes header, 1 byte base (base = 0)
comment|// and 2 bytes delta (delta = 100, zigzag encoded varint). In total, 5 bytes per run.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 50"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFixedDeltaLargeDescending
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
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
literal|5120
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
operator|(
literal|512
operator|-
name|i
operator|%
literal|512
operator|)
operator|+
operator|(
operator|(
name|i
operator|%
literal|512
operator|)
operator|*
literal|100
operator|)
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// 10 runs of 512 elements. Each run has 2 bytes header, 2 byte base (base = 512, zigzag + varint)
comment|// and 2 bytes delta (delta = 100, zigzag encoded varint). In total, 6 bytes per run.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 60"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testShortRepeat
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
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
literal|5
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// 1 byte header + 1 byte value
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 2"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeltaUnknownSign
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
argument_list|)
decl_stmt|;
name|w
operator|.
name|addRow
argument_list|(
literal|0
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
literal|511
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// monotonicity will be undetermined for this sequence 0,0,1,2,3,...510. Hence DIRECT encoding
comment|// will be used. 2 bytes for header and 640 bytes for data (512 values with fixed bit of 10 bits
comment|// each, 5120/8 = 640). Total bytes 642
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 642"
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
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPatchedBase
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectInspector
name|inspector
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
decl_stmt|;
name|Writer
name|w
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
name|compress
argument_list|(
name|CompressionKind
operator|.
name|NONE
argument_list|)
operator|.
name|inspector
argument_list|(
name|inspector
argument_list|)
operator|.
name|rowIndexStride
argument_list|(
literal|0
argument_list|)
operator|.
name|encodingStrategy
argument_list|(
name|OrcFile
operator|.
name|EncodingStrategy
operator|.
name|COMPRESSION
argument_list|)
operator|.
name|version
argument_list|(
name|OrcFile
operator|.
name|Version
operator|.
name|V_0_12
argument_list|)
argument_list|)
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|123
argument_list|)
decl_stmt|;
name|w
operator|.
name|addRow
argument_list|(
literal|10000000
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
literal|511
condition|;
operator|++
name|i
control|)
block|{
name|w
operator|.
name|addRow
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|w
operator|.
name|close
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
name|testFilePath
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
comment|// use PATCHED_BASE encoding
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|outDump
operator|.
name|contains
argument_list|(
literal|"Stream: column 0 section DATA start: 3 length 583"
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
block|}
block|}
end_class

end_unit


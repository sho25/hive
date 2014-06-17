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
name|BufferedReader
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
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileReader
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveTestUtils
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

begin_class
specifier|public
class|class
name|TestFileDump
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
name|fs
operator|.
name|setWorkingDirectory
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
name|testFilePath
operator|=
operator|new
name|Path
argument_list|(
literal|"TestFileDump.testDump.orc"
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
specifier|static
class|class
name|MyRecord
block|{
name|int
name|i
decl_stmt|;
name|long
name|l
decl_stmt|;
name|String
name|s
decl_stmt|;
name|MyRecord
parameter_list|(
name|int
name|i
parameter_list|,
name|long
name|l
parameter_list|,
name|String
name|s
parameter_list|)
block|{
name|this
operator|.
name|i
operator|=
name|i
expr_stmt|;
name|this
operator|.
name|l
operator|=
name|l
expr_stmt|;
name|this
operator|.
name|s
operator|=
name|s
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|checkOutput
parameter_list|(
name|String
name|expected
parameter_list|,
name|String
name|actual
parameter_list|)
throws|throws
name|Exception
block|{
name|BufferedReader
name|eStream
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
name|expected
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|BufferedReader
name|aStream
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|FileReader
argument_list|(
name|actual
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|expectedLine
init|=
name|eStream
operator|.
name|readLine
argument_list|()
decl_stmt|;
while|while
condition|(
name|expectedLine
operator|!=
literal|null
condition|)
block|{
name|String
name|actualLine
init|=
name|aStream
operator|.
name|readLine
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"actual:   "
operator|+
name|actualLine
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected: "
operator|+
name|expectedLine
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedLine
argument_list|,
name|actualLine
argument_list|)
expr_stmt|;
name|expectedLine
operator|=
name|eStream
operator|.
name|readLine
argument_list|()
expr_stmt|;
block|}
name|assertNull
argument_list|(
name|eStream
operator|.
name|readLine
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|aStream
operator|.
name|readLine
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDump
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
name|MyRecord
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
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_ENCODING_STRATEGY
operator|.
name|varname
argument_list|,
literal|"COMPRESSION"
argument_list|)
expr_stmt|;
name|Writer
name|writer
init|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|testFilePath
argument_list|,
name|conf
argument_list|,
name|inspector
argument_list|,
literal|100000
argument_list|,
name|CompressionKind
operator|.
name|ZLIB
argument_list|,
literal|10000
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|Random
name|r1
init|=
operator|new
name|Random
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
index|[]
name|words
init|=
operator|new
name|String
index|[]
block|{
literal|"It"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"best"
block|,
literal|"of"
block|,
literal|"times,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"worst"
block|,
literal|"of"
block|,
literal|"times,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"age"
block|,
literal|"of"
block|,
literal|"wisdom,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"age"
block|,
literal|"of"
block|,
literal|"foolishness,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"epoch"
block|,
literal|"of"
block|,
literal|"belief,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"epoch"
block|,
literal|"of"
block|,
literal|"incredulity,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"season"
block|,
literal|"of"
block|,
literal|"Light,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"season"
block|,
literal|"of"
block|,
literal|"Darkness,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"spring"
block|,
literal|"of"
block|,
literal|"hope,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"winter"
block|,
literal|"of"
block|,
literal|"despair,"
block|,
literal|"we"
block|,
literal|"had"
block|,
literal|"everything"
block|,
literal|"before"
block|,
literal|"us,"
block|,
literal|"we"
block|,
literal|"had"
block|,
literal|"nothing"
block|,
literal|"before"
block|,
literal|"us,"
block|,
literal|"we"
block|,
literal|"were"
block|,
literal|"all"
block|,
literal|"going"
block|,
literal|"direct"
block|,
literal|"to"
block|,
literal|"Heaven,"
block|,
literal|"we"
block|,
literal|"were"
block|,
literal|"all"
block|,
literal|"going"
block|,
literal|"direct"
block|,
literal|"the"
block|,
literal|"other"
block|,
literal|"way"
block|}
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
literal|21000
condition|;
operator|++
name|i
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|MyRecord
argument_list|(
name|r1
operator|.
name|nextInt
argument_list|()
argument_list|,
name|r1
operator|.
name|nextLong
argument_list|()
argument_list|,
name|words
index|[
name|r1
operator|.
name|nextInt
argument_list|(
name|words
operator|.
name|length
argument_list|)
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
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
name|String
name|outputFilename
init|=
literal|"orc-file-dump.out"
decl_stmt|;
name|FileOutputStream
name|myOut
init|=
operator|new
name|FileOutputStream
argument_list|(
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|outputFilename
argument_list|)
decl_stmt|;
comment|// replace stdout and run command
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
name|System
operator|.
name|setOut
argument_list|(
name|origOut
argument_list|)
expr_stmt|;
name|checkOutput
argument_list|(
name|outputFilename
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|outputFilename
argument_list|)
expr_stmt|;
block|}
comment|// Test that if the fraction of rows that have distinct strings is greater than the configured
comment|// threshold dictionary encoding is turned off.  If dictionary encoding is turned off the length
comment|// of the dictionary stream for the column will be 0 in the ORC file dump.
annotation|@
name|Test
specifier|public
name|void
name|testDictionaryThreshold
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
name|MyRecord
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
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_ENCODING_STRATEGY
operator|.
name|varname
argument_list|,
literal|"COMPRESSION"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_ORC_DICTIONARY_KEY_SIZE_THRESHOLD
operator|.
name|varname
argument_list|,
literal|0.49f
argument_list|)
expr_stmt|;
name|Writer
name|writer
init|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|testFilePath
argument_list|,
name|conf
argument_list|,
name|inspector
argument_list|,
literal|100000
argument_list|,
name|CompressionKind
operator|.
name|ZLIB
argument_list|,
literal|10000
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|Random
name|r1
init|=
operator|new
name|Random
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
index|[]
name|words
init|=
operator|new
name|String
index|[]
block|{
literal|"It"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"best"
block|,
literal|"of"
block|,
literal|"times,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"worst"
block|,
literal|"of"
block|,
literal|"times,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"age"
block|,
literal|"of"
block|,
literal|"wisdom,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"age"
block|,
literal|"of"
block|,
literal|"foolishness,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"epoch"
block|,
literal|"of"
block|,
literal|"belief,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"epoch"
block|,
literal|"of"
block|,
literal|"incredulity,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"season"
block|,
literal|"of"
block|,
literal|"Light,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"season"
block|,
literal|"of"
block|,
literal|"Darkness,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"spring"
block|,
literal|"of"
block|,
literal|"hope,"
block|,
literal|"it"
block|,
literal|"was"
block|,
literal|"the"
block|,
literal|"winter"
block|,
literal|"of"
block|,
literal|"despair,"
block|,
literal|"we"
block|,
literal|"had"
block|,
literal|"everything"
block|,
literal|"before"
block|,
literal|"us,"
block|,
literal|"we"
block|,
literal|"had"
block|,
literal|"nothing"
block|,
literal|"before"
block|,
literal|"us,"
block|,
literal|"we"
block|,
literal|"were"
block|,
literal|"all"
block|,
literal|"going"
block|,
literal|"direct"
block|,
literal|"to"
block|,
literal|"Heaven,"
block|,
literal|"we"
block|,
literal|"were"
block|,
literal|"all"
block|,
literal|"going"
block|,
literal|"direct"
block|,
literal|"the"
block|,
literal|"other"
block|,
literal|"way"
block|}
decl_stmt|;
name|int
name|nextInt
init|=
literal|0
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
literal|21000
condition|;
operator|++
name|i
control|)
block|{
comment|// Write out the same string twice, this guarantees the fraction of rows with
comment|// distinct strings is 0.5
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|nextInt
operator|=
name|r1
operator|.
name|nextInt
argument_list|(
name|words
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Append the value of i to the word, this guarantees when an index or word is repeated
comment|// the actual string is unique.
name|words
index|[
name|nextInt
index|]
operator|+=
literal|"-"
operator|+
name|i
expr_stmt|;
block|}
name|writer
operator|.
name|addRow
argument_list|(
operator|new
name|MyRecord
argument_list|(
name|r1
operator|.
name|nextInt
argument_list|()
argument_list|,
name|r1
operator|.
name|nextLong
argument_list|()
argument_list|,
name|words
index|[
name|nextInt
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
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
name|String
name|outputFilename
init|=
literal|"orc-file-dump-dictionary-threshold.out"
decl_stmt|;
name|FileOutputStream
name|myOut
init|=
operator|new
name|FileOutputStream
argument_list|(
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|outputFilename
argument_list|)
decl_stmt|;
comment|// replace stdout and run command
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
name|System
operator|.
name|setOut
argument_list|(
name|origOut
argument_list|)
expr_stmt|;
name|checkOutput
argument_list|(
name|outputFilename
argument_list|,
name|workDir
operator|+
name|File
operator|.
name|separator
operator|+
name|outputFilename
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


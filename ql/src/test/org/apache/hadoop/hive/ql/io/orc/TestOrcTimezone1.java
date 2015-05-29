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
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertNotNull
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
name|sql
operator|.
name|Timestamp
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
name|Collection
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
name|TimeZone
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
name|io
operator|.
name|TimestampWritable
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
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
name|StructObjectInspector
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
name|primitive
operator|.
name|TimestampObjectInspector
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
name|After
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
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestOrcTimezone1
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
name|Configuration
name|conf
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|Path
name|testFilePath
decl_stmt|;
name|String
name|writerTimeZone
decl_stmt|;
name|String
name|readerTimeZone
decl_stmt|;
specifier|static
name|TimeZone
name|defaultTimeZone
init|=
name|TimeZone
operator|.
name|getDefault
argument_list|()
decl_stmt|;
specifier|public
name|TestOrcTimezone1
parameter_list|(
name|String
name|writerTZ
parameter_list|,
name|String
name|readerTZ
parameter_list|)
block|{
name|this
operator|.
name|writerTimeZone
operator|=
name|writerTZ
expr_stmt|;
name|this
operator|.
name|readerTimeZone
operator|=
name|readerTZ
expr_stmt|;
block|}
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|result
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
comment|/* Extreme timezones */
block|{
literal|"GMT-12:00"
block|,
literal|"GMT+14:00"
block|}
block|,
comment|/* No difference in DST */
block|{
literal|"America/Los_Angeles"
block|,
literal|"America/Los_Angeles"
block|}
block|,
comment|/* same timezone both with DST */
block|{
literal|"Europe/Berlin"
block|,
literal|"Europe/Berlin"
block|}
block|,
comment|/* same as above but europe */
block|{
literal|"America/Phoenix"
block|,
literal|"Asia/Kolkata"
block|}
comment|/* Writer no DST, Reader no DST */
block|,
block|{
literal|"Europe/Berlin"
block|,
literal|"America/Los_Angeles"
block|}
comment|/* Writer DST, Reader DST */
block|,
block|{
literal|"Europe/Berlin"
block|,
literal|"America/Chicago"
block|}
comment|/* Writer DST, Reader DST */
block|,
comment|/* With DST difference */
block|{
literal|"Europe/Berlin"
block|,
literal|"UTC"
block|}
block|,
block|{
literal|"UTC"
block|,
literal|"Europe/Berlin"
block|}
comment|/* Writer no DST, Reader DST */
block|,
block|{
literal|"America/Los_Angeles"
block|,
literal|"Asia/Kolkata"
block|}
comment|/* Writer DST, Reader no DST */
block|,
block|{
literal|"Europe/Berlin"
block|,
literal|"Asia/Kolkata"
block|}
comment|/* Writer DST, Reader no DST */
block|,
comment|/* Timezone offsets for the reader has changed historically */
block|{
literal|"Asia/Saigon"
block|,
literal|"Pacific/Enderbury"
block|}
block|,
block|{
literal|"UTC"
block|,
literal|"Asia/Jerusalem"
block|}
block|,
comment|// NOTE:
comment|// "1995-01-01 03:00:00.688888888" this is not a valid time in Pacific/Enderbury timezone.
comment|// On 1995-01-01 00:00:00 GMT offset moved from -11:00 hr to +13:00 which makes all values
comment|// on 1995-01-01 invalid. Try this with joda time
comment|// new MutableDateTime("1995-01-01", DateTimeZone.forTimeZone(readerTimeZone));
block|}
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
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
annotation|@
name|After
specifier|public
name|void
name|restoreTimeZone
parameter_list|()
block|{
name|TimeZone
operator|.
name|setDefault
argument_list|(
name|defaultTimeZone
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTimestampWriter
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
name|Timestamp
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
name|TimeZone
operator|.
name|setDefault
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
name|writerTimeZone
argument_list|)
argument_list|)
expr_stmt|;
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
name|stripeSize
argument_list|(
literal|100000
argument_list|)
operator|.
name|bufferSize
argument_list|(
literal|10000
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|writerTimeZone
argument_list|,
name|TimeZone
operator|.
name|getDefault
argument_list|()
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ts
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2003-01-01 01:00:00.000000222"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"1996-08-02 09:00:00.723100809"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"1999-01-01 02:00:00.999999999"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"1995-01-02 03:00:00.688888888"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2002-01-01 04:00:00.1"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2010-03-02 05:00:00.000009001"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2005-01-01 06:00:00.000002229"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2006-01-01 07:00:00.900203003"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2003-01-01 08:00:00.800000007"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"1998-11-02 10:00:00.857340643"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2008-10-02 11:00:00.0"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2037-01-01 00:00:00.000999"
argument_list|)
expr_stmt|;
name|ts
operator|.
name|add
argument_list|(
literal|"2014-03-28 00:00:00.0"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|t
range|:
name|ts
control|)
block|{
name|writer
operator|.
name|addRow
argument_list|(
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|TimeZone
operator|.
name|setDefault
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
name|readerTimeZone
argument_list|)
argument_list|)
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
name|assertEquals
argument_list|(
name|readerTimeZone
argument_list|,
name|TimeZone
operator|.
name|getDefault
argument_list|()
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
name|RecordReader
name|rows
init|=
name|reader
operator|.
name|rows
argument_list|(
literal|null
argument_list|)
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
name|Timestamp
name|got
init|=
operator|(
operator|(
name|TimestampWritable
operator|)
name|row
operator|)
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|ts
operator|.
name|get
argument_list|(
name|idx
operator|++
argument_list|)
argument_list|,
name|got
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rows
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadTimestampFormat_0_11
parameter_list|()
throws|throws
name|Exception
block|{
name|TimeZone
operator|.
name|setDefault
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
name|readerTimeZone
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|oldFilePath
init|=
operator|new
name|Path
argument_list|(
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
literal|"orc-file-11-format.orc"
argument_list|)
argument_list|)
decl_stmt|;
name|Reader
name|reader
init|=
name|OrcFile
operator|.
name|createReader
argument_list|(
name|oldFilePath
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
name|StructObjectInspector
name|readerInspector
init|=
operator|(
name|StructObjectInspector
operator|)
name|reader
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|readerInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|TimestampObjectInspector
name|tso
init|=
operator|(
name|TimestampObjectInspector
operator|)
name|readerInspector
operator|.
name|getStructFieldRef
argument_list|(
literal|"ts"
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|RecordReader
name|rows
init|=
name|reader
operator|.
name|rows
argument_list|()
decl_stmt|;
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
name|assertNotNull
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2000-03-12 15:00:00"
argument_list|)
argument_list|,
name|tso
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|readerInspector
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|12
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// check the contents of second row
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
name|rows
operator|.
name|seekToRow
argument_list|(
literal|7499
argument_list|)
expr_stmt|;
name|row
operator|=
name|rows
operator|.
name|next
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2000-03-12 15:00:01"
argument_list|)
argument_list|,
name|tso
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|readerInspector
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|12
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// handle the close up
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
name|rows
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


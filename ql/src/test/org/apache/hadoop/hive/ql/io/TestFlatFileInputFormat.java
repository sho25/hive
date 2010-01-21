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
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|FSDataOutputStream
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
name|serializer
operator|.
name|JavaSerialization
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
name|serializer
operator|.
name|Serializer
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
name|serializer
operator|.
name|WritableSerialization
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
name|FileInputFormat
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

begin_comment
comment|//import org.apache.hadoop.contrib.serialization.thrift.*;
end_comment

begin_class
specifier|public
class|class
name|TestFlatFileInputFormat
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testFlatFileInputJava
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
decl_stmt|;
name|JobConf
name|job
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|Path
name|dir
decl_stmt|;
name|Path
name|file
decl_stmt|;
name|Reporter
name|reporter
decl_stmt|;
name|FSDataOutputStream
name|ds
decl_stmt|;
try|try
block|{
comment|//
comment|// create job and filesystem and reporter and such.
comment|//
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
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
name|dir
operator|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.dir"
argument_list|,
literal|"."
argument_list|)
operator|+
literal|"/mapred"
argument_list|)
expr_stmt|;
name|file
operator|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"test.txt"
argument_list|)
expr_stmt|;
name|reporter
operator|=
name|Reporter
operator|.
name|NULL
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|job
operator|.
name|setClass
argument_list|(
name|FlatFileInputFormat
operator|.
name|SerializationImplKey
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|serializer
operator|.
name|JavaSerialization
operator|.
name|class
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|serializer
operator|.
name|Serialization
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setClass
argument_list|(
name|FlatFileInputFormat
operator|.
name|SerializationContextFromConf
operator|.
name|SerializationSubclassKey
argument_list|,
name|JavaTestObjFlatFileInputFormat
operator|.
name|class
argument_list|,
name|java
operator|.
name|io
operator|.
name|Serializable
operator|.
name|class
argument_list|)
expr_stmt|;
comment|//
comment|// Write some data out to a flat file
comment|//
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|ds
operator|=
name|fs
operator|.
name|create
argument_list|(
name|file
argument_list|)
expr_stmt|;
name|Serializer
name|serializer
init|=
operator|new
name|JavaSerialization
argument_list|()
operator|.
name|getSerializer
argument_list|(
literal|null
argument_list|)
decl_stmt|;
comment|// construct some data and write it
name|serializer
operator|.
name|open
argument_list|(
name|ds
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|serializer
operator|.
name|serialize
argument_list|(
operator|new
name|JavaTestObjFlatFileInputFormat
argument_list|(
literal|"Hello World! "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//
comment|// Construct the reader
comment|//
name|FileInputFormat
argument_list|<
name|Void
argument_list|,
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|Serializable
argument_list|>
argument_list|>
name|format
init|=
operator|new
name|FlatFileInputFormat
argument_list|<
name|Serializable
argument_list|>
argument_list|()
decl_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|format
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|// construct the record reader
name|RecordReader
argument_list|<
name|Void
argument_list|,
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|Serializable
argument_list|>
argument_list|>
name|reader
init|=
name|format
operator|.
name|getRecordReader
argument_list|(
name|splits
index|[
literal|0
index|]
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
comment|// create key/value
name|Void
name|key
init|=
name|reader
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|Serializable
argument_list|>
name|value
init|=
name|reader
operator|.
name|createValue
argument_list|()
decl_stmt|;
comment|//
comment|// read back the data using the FlatFileRecordReader
comment|//
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|key
operator|==
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|JavaTestObjFlatFileInputFormat
operator|)
name|value
operator|.
name|row
operator|)
operator|.
name|s
operator|.
name|equals
argument_list|(
literal|"Hello World! "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|count
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|JavaTestObjFlatFileInputFormat
operator|)
name|value
operator|.
name|row
operator|)
operator|.
name|num
operator|==
name|count
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"caught: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{     }
block|}
specifier|public
name|void
name|testFlatFileInputRecord
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
decl_stmt|;
name|JobConf
name|job
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
name|Path
name|dir
decl_stmt|;
name|Path
name|file
decl_stmt|;
name|Reporter
name|reporter
decl_stmt|;
name|FSDataOutputStream
name|ds
decl_stmt|;
try|try
block|{
comment|//
comment|// create job and filesystem and reporter and such.
comment|//
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
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
name|dir
operator|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.dir"
argument_list|,
literal|"."
argument_list|)
operator|+
literal|"/mapred"
argument_list|)
expr_stmt|;
name|file
operator|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"test.txt"
argument_list|)
expr_stmt|;
name|reporter
operator|=
name|Reporter
operator|.
name|NULL
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|job
operator|.
name|setClass
argument_list|(
name|FlatFileInputFormat
operator|.
name|SerializationImplKey
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|serializer
operator|.
name|WritableSerialization
operator|.
name|class
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|serializer
operator|.
name|Serialization
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setClass
argument_list|(
name|FlatFileInputFormat
operator|.
name|SerializationContextFromConf
operator|.
name|SerializationSubclassKey
argument_list|,
name|RecordTestObj
operator|.
name|class
argument_list|,
name|Writable
operator|.
name|class
argument_list|)
expr_stmt|;
comment|//
comment|// Write some data out to a flat file
comment|//
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|ds
operator|=
name|fs
operator|.
name|create
argument_list|(
name|file
argument_list|)
expr_stmt|;
name|Serializer
name|serializer
init|=
operator|new
name|WritableSerialization
argument_list|()
operator|.
name|getSerializer
argument_list|(
name|Writable
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// construct some data and write it
name|serializer
operator|.
name|open
argument_list|(
name|ds
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|serializer
operator|.
name|serialize
argument_list|(
operator|new
name|RecordTestObj
argument_list|(
literal|"Hello World! "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serializer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//
comment|// Construct the reader
comment|//
name|FileInputFormat
argument_list|<
name|Void
argument_list|,
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|Writable
argument_list|>
argument_list|>
name|format
init|=
operator|new
name|FlatFileInputFormat
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|format
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|1
argument_list|)
decl_stmt|;
comment|// construct the record reader
name|RecordReader
argument_list|<
name|Void
argument_list|,
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|Writable
argument_list|>
argument_list|>
name|reader
init|=
name|format
operator|.
name|getRecordReader
argument_list|(
name|splits
index|[
literal|0
index|]
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
comment|// create key/value
name|Void
name|key
init|=
name|reader
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|FlatFileInputFormat
operator|.
name|RowContainer
argument_list|<
name|Writable
argument_list|>
name|value
init|=
name|reader
operator|.
name|createValue
argument_list|()
decl_stmt|;
comment|//
comment|// read back the data using the FlatFileRecordReader
comment|//
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|key
operator|==
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|RecordTestObj
operator|)
name|value
operator|.
name|row
operator|)
operator|.
name|getS
argument_list|()
operator|.
name|equals
argument_list|(
literal|"Hello World! "
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|count
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
operator|(
name|RecordTestObj
operator|)
name|value
operator|.
name|row
operator|)
operator|.
name|getNum
argument_list|()
operator|==
name|count
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"caught: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{     }
block|}
comment|/*    * public void testFlatFileInputThrift() throws Exception { Configuration    * conf; JobConf job ; FileSystem fs; Path dir ; Path file; Reporter reporter;    * FSDataOutputStream ds;    *     * try { // // create job and filesystem and reporter and such. // conf = new    * Configuration(); job = new JobConf(conf); fs = FileSystem.getLocal(conf);    * dir = new Path(System.getProperty("test.data.dir",".") + "/mapred"); file =    * new Path(dir, "test.txt"); reporter = Reporter.NULL; fs.delete(dir, true);    *     * job.setClass(FlatFileInputFormat.SerializationContextFromConf.    * SerializationImplKey,    * org.apache.hadoop.contrib.serialization.thrift.ThriftSerialization.class,    * org.apache.hadoop.io.serializer.Serialization.class);    *     * job.setClass(FlatFileInputFormat.SerializationContextFromConf.    * SerializationSubclassKey, FlatFileThriftTestObj.class, TBase.class);    *     * // // Write some data out to a flat file //    * FileInputFormat.setInputPaths(job, dir); ds = fs.create(file); Serializer    * serializer = new ThriftSerialization().getSerializer(TBase.class);    *     * // construct some data and write it serializer.open(ds); for (int i = 0; i    *< 10; i++) { serializer.serialize(new FlatFileThriftTestObj("Hello World! "    * + String.valueOf(i), i)); } serializer.close();    *     * // // Construct the reader // FileInputFormat<Void,    * FlatFileInputFormat.RowContainer<TBase>> format = new    * FlatFileInputFormat<TBase>(); InputSplit[] splits = format.getSplits(job,    * 1);    *     * // construct the record reader RecordReader<Void,    * FlatFileInputFormat.RowContainer<TBase>> reader =    * format.getRecordReader(splits[0], job, reporter);    *     * // create key/value Void key = reader.createKey();    * FlatFileInputFormat.RowContainer<TBase> value = reader.createValue();    *     * // // read back the data using the FlatFileRecordReader // int count = 0;    * while (reader.next(key, value)) { assertTrue(key == null);    * assertTrue(((FlatFileThriftTestObj)value.row).s.equals("Hello World! "    * +String.valueOf(count))); assertTrue(((FlatFileThriftTestObj)value.row).num    * == count); count++; } reader.close();    *     * } catch(Exception e) { System.err.println("caught: " + e);    * e.printStackTrace(); } finally { }    *     * }    */
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
name|TestFlatFileInputFormat
argument_list|()
operator|.
name|testFlatFileInputJava
argument_list|()
expr_stmt|;
operator|new
name|TestFlatFileInputFormat
argument_list|()
operator|.
name|testFlatFileInputRecord
argument_list|()
expr_stmt|;
comment|// new TestFlatFileInputFormat().testFlatFileInputThrift();
block|}
block|}
end_class

end_unit


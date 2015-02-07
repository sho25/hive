begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|mapreduce
package|;
end_package

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
name|FileWriter
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
name|net
operator|.
name|URI
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
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
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
name|filecache
operator|.
name|DistributedCache
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
name|FSDataInputStream
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
name|FileUtil
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
name|SequenceFile
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
name|MiniMRCluster
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
name|Mapper
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
name|OutputCommitter
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
name|Reducer
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
name|lib
operator|.
name|input
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
name|mapreduce
operator|.
name|lib
operator|.
name|input
operator|.
name|TextInputFormat
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
name|output
operator|.
name|FileOutputFormat
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
name|output
operator|.
name|SequenceFileOutputFormat
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
name|output
operator|.
name|TextOutputFormat
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
name|hcatalog
operator|.
name|mapreduce
operator|.
name|MultiOutputFormat
operator|.
name|JobConfigurer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestMultiOutputFormat
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestMultiOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|File
name|workDir
decl_stmt|;
specifier|private
specifier|static
name|JobConf
name|mrConf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|MiniMRCluster
name|mrCluster
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|createWorkDir
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.queues"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"yarn.scheduler.capacity.root.default.capacity"
argument_list|,
literal|"100"
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hadoop.log.dir"
argument_list|,
operator|new
name|File
argument_list|(
name|workDir
argument_list|,
literal|"/logs"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
comment|// LocalJobRunner does not work with mapreduce OutputCommitter. So need
comment|// to use MiniMRCluster. MAPREDUCE-2350
name|mrConf
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|mrCluster
operator|=
operator|new
name|MiniMRCluster
argument_list|(
literal|1
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|mrConf
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|createWorkDir
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|testDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"./"
argument_list|)
decl_stmt|;
name|testDir
operator|=
name|testDir
operator|+
literal|"/test_multiout_"
operator|+
name|Math
operator|.
name|abs
argument_list|(
operator|new
name|Random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|+
literal|"/"
expr_stmt|;
name|workDir
operator|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|testDir
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
expr_stmt|;
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
name|workDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|mrCluster
operator|!=
literal|null
condition|)
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
block|}
comment|/**    * A test job that reads a input file and outputs each word and the index of    * the word encountered to a text file and sequence file with different key    * values.    */
annotation|@
name|Test
specifier|public
name|void
name|testMultiOutputFormatWithoutReduce
parameter_list|()
throws|throws
name|Throwable
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|mrConf
argument_list|,
literal|"MultiOutNoReduce"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|MultiOutWordIndexMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|MultiOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|JobConfigurer
name|configurer
init|=
name|MultiOutputFormat
operator|.
name|createConfigurer
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|configurer
operator|.
name|addOutputFormat
argument_list|(
literal|"out1"
argument_list|,
name|TextOutputFormat
operator|.
name|class
argument_list|,
name|IntWritable
operator|.
name|class
argument_list|,
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|configurer
operator|.
name|addOutputFormat
argument_list|(
literal|"out2"
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Text
operator|.
name|class
argument_list|,
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|outDir
init|=
operator|new
name|Path
argument_list|(
name|workDir
operator|.
name|getPath
argument_list|()
argument_list|,
name|job
operator|.
name|getJobName
argument_list|()
argument_list|)
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|configurer
operator|.
name|getJob
argument_list|(
literal|"out1"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out1"
argument_list|)
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|configurer
operator|.
name|getJob
argument_list|(
literal|"out2"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out2"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|fileContent
init|=
literal|"Hello World"
decl_stmt|;
name|String
name|inputFile
init|=
name|createInputFile
argument_list|(
name|fileContent
argument_list|)
decl_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
argument_list|)
expr_stmt|;
comment|//Test for merging of configs
name|DistributedCache
operator|.
name|addFileToClassPath
argument_list|(
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
argument_list|,
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|String
name|dummyFile
init|=
name|createInputFile
argument_list|(
literal|"dummy file"
argument_list|)
decl_stmt|;
name|DistributedCache
operator|.
name|addFileToClassPath
argument_list|(
operator|new
name|Path
argument_list|(
name|dummyFile
argument_list|)
argument_list|,
name|configurer
operator|.
name|getJob
argument_list|(
literal|"out1"
argument_list|)
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|)
expr_stmt|;
comment|// duplicate of the value. Merging should remove duplicates
name|DistributedCache
operator|.
name|addFileToClassPath
argument_list|(
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
argument_list|,
name|configurer
operator|.
name|getJob
argument_list|(
literal|"out2"
argument_list|)
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|configurer
operator|.
name|configure
argument_list|()
expr_stmt|;
comment|// Verify if the configs are merged
name|Path
index|[]
name|fileClassPaths
init|=
name|DistributedCache
operator|.
name|getFileClassPaths
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|fileClassPathsList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|fileClassPaths
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Cannot find "
operator|+
operator|(
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
operator|)
operator|+
literal|" in "
operator|+
name|fileClassPathsList
argument_list|,
name|fileClassPathsList
operator|.
name|contains
argument_list|(
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Cannot find "
operator|+
operator|(
operator|new
name|Path
argument_list|(
name|dummyFile
argument_list|)
operator|)
operator|+
literal|" in "
operator|+
name|fileClassPathsList
argument_list|,
name|fileClassPathsList
operator|.
name|contains
argument_list|(
operator|new
name|Path
argument_list|(
name|dummyFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|URI
index|[]
name|cacheFiles
init|=
name|DistributedCache
operator|.
name|getCacheFiles
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|URI
argument_list|>
name|cacheFilesList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|cacheFiles
argument_list|)
decl_stmt|;
name|URI
name|inputFileURI
init|=
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Cannot find "
operator|+
name|inputFileURI
operator|+
literal|" in "
operator|+
name|cacheFilesList
argument_list|,
name|cacheFilesList
operator|.
name|contains
argument_list|(
name|inputFileURI
argument_list|)
argument_list|)
expr_stmt|;
name|URI
name|dummyFileURI
init|=
operator|new
name|Path
argument_list|(
name|dummyFile
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Cannot find "
operator|+
name|dummyFileURI
operator|+
literal|" in "
operator|+
name|cacheFilesList
argument_list|,
name|cacheFilesList
operator|.
name|contains
argument_list|(
name|dummyFileURI
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|textOutPath
init|=
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out1/part-m-00000"
argument_list|)
decl_stmt|;
name|String
index|[]
name|textOutput
init|=
name|readFully
argument_list|(
name|textOutPath
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
name|Path
name|seqOutPath
init|=
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out2/part-m-00000"
argument_list|)
decl_stmt|;
name|SequenceFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|seqOutPath
argument_list|,
name|mrConf
argument_list|)
decl_stmt|;
name|Text
name|key
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|IntWritable
name|value
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
name|String
index|[]
name|words
init|=
name|fileContent
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|words
operator|.
name|length
argument_list|,
name|textOutput
operator|.
name|length
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Verifying file contents"
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
name|words
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"\t"
operator|+
name|words
index|[
name|i
index|]
argument_list|,
name|textOutput
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|words
index|[
name|i
index|]
argument_list|,
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|value
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * A word count test job that reads a input file and outputs the count of    * words to a text file and sequence file with different key values.    */
annotation|@
name|Test
specifier|public
name|void
name|testMultiOutputFormatWithReduce
parameter_list|()
throws|throws
name|Throwable
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|mrConf
argument_list|,
literal|"MultiOutWithReduce"
argument_list|)
decl_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|WordCountMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|MultiOutWordCountReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|MultiOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|JobConfigurer
name|configurer
init|=
name|MultiOutputFormat
operator|.
name|createConfigurer
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|configurer
operator|.
name|addOutputFormat
argument_list|(
literal|"out1"
argument_list|,
name|TextOutputFormat
operator|.
name|class
argument_list|,
name|IntWritable
operator|.
name|class
argument_list|,
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|configurer
operator|.
name|addOutputFormat
argument_list|(
literal|"out2"
argument_list|,
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|Text
operator|.
name|class
argument_list|,
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|configurer
operator|.
name|addOutputFormat
argument_list|(
literal|"out3"
argument_list|,
name|NullOutputFormat
operator|.
name|class
argument_list|,
name|Text
operator|.
name|class
argument_list|,
name|IntWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|outDir
init|=
operator|new
name|Path
argument_list|(
name|workDir
operator|.
name|getPath
argument_list|()
argument_list|,
name|job
operator|.
name|getJobName
argument_list|()
argument_list|)
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|configurer
operator|.
name|getJob
argument_list|(
literal|"out1"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out1"
argument_list|)
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|configurer
operator|.
name|getJob
argument_list|(
literal|"out2"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out2"
argument_list|)
argument_list|)
expr_stmt|;
name|configurer
operator|.
name|configure
argument_list|()
expr_stmt|;
name|String
name|fileContent
init|=
literal|"Hello World Hello World World"
decl_stmt|;
name|String
name|inputFile
init|=
name|createInputFile
argument_list|(
name|fileContent
argument_list|)
decl_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
operator|new
name|Path
argument_list|(
name|inputFile
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|textOutPath
init|=
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out1/part-r-00000"
argument_list|)
decl_stmt|;
name|String
index|[]
name|textOutput
init|=
name|readFully
argument_list|(
name|textOutPath
argument_list|)
operator|.
name|split
argument_list|(
literal|"\n"
argument_list|)
decl_stmt|;
name|Path
name|seqOutPath
init|=
operator|new
name|Path
argument_list|(
name|outDir
argument_list|,
literal|"out2/part-r-00000"
argument_list|)
decl_stmt|;
name|SequenceFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|seqOutPath
argument_list|,
name|mrConf
argument_list|)
decl_stmt|;
name|Text
name|key
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|IntWritable
name|value
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
name|String
index|[]
name|words
init|=
literal|"Hello World"
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|words
operator|.
name|length
argument_list|,
name|textOutput
operator|.
name|length
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
name|words
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|2
operator|)
operator|+
literal|"\t"
operator|+
name|words
index|[
name|i
index|]
argument_list|,
name|textOutput
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|words
index|[
name|i
index|]
argument_list|,
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|(
name|i
operator|+
literal|2
operator|)
argument_list|,
name|value
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a file for map input    *    * @return absolute path of the file.    * @throws IOException if any error encountered    */
specifier|private
name|String
name|createInputFile
parameter_list|(
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|f
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"input"
argument_list|,
literal|"txt"
argument_list|)
decl_stmt|;
name|FileWriter
name|writer
init|=
operator|new
name|FileWriter
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|content
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|f
operator|.
name|getAbsolutePath
argument_list|()
return|;
block|}
specifier|private
name|String
name|readFully
parameter_list|(
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[
name|in
operator|.
name|available
argument_list|()
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|b
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|MultiOutWordIndexMapper
extends|extends
name|Mapper
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|,
name|Writable
argument_list|,
name|Writable
argument_list|>
block|{
specifier|private
name|IntWritable
name|index
init|=
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|Text
name|word
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|map
parameter_list|(
name|LongWritable
name|key
parameter_list|,
name|Text
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|StringTokenizer
name|itr
init|=
operator|new
name|StringTokenizer
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasMoreTokens
argument_list|()
condition|)
block|{
name|word
operator|.
name|set
argument_list|(
name|itr
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|MultiOutputFormat
operator|.
name|write
argument_list|(
literal|"out1"
argument_list|,
name|index
argument_list|,
name|word
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|MultiOutputFormat
operator|.
name|write
argument_list|(
literal|"out2"
argument_list|,
name|word
argument_list|,
name|index
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|index
operator|.
name|set
argument_list|(
name|index
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|WordCountMapper
extends|extends
name|Mapper
argument_list|<
name|LongWritable
argument_list|,
name|Text
argument_list|,
name|Text
argument_list|,
name|IntWritable
argument_list|>
block|{
specifier|private
specifier|final
specifier|static
name|IntWritable
name|one
init|=
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|Text
name|word
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|map
parameter_list|(
name|LongWritable
name|key
parameter_list|,
name|Text
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|StringTokenizer
name|itr
init|=
operator|new
name|StringTokenizer
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasMoreTokens
argument_list|()
condition|)
block|{
name|word
operator|.
name|set
argument_list|(
name|itr
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|word
argument_list|,
name|one
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|MultiOutWordCountReducer
extends|extends
name|Reducer
argument_list|<
name|Text
argument_list|,
name|IntWritable
argument_list|,
name|Writable
argument_list|,
name|Writable
argument_list|>
block|{
specifier|private
name|IntWritable
name|count
init|=
operator|new
name|IntWritable
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|reduce
parameter_list|(
name|Text
name|word
parameter_list|,
name|Iterable
argument_list|<
name|IntWritable
argument_list|>
name|values
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|int
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|IntWritable
name|val
range|:
name|values
control|)
block|{
name|sum
operator|+=
name|val
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|count
operator|.
name|set
argument_list|(
name|sum
argument_list|)
expr_stmt|;
name|MultiOutputFormat
operator|.
name|write
argument_list|(
literal|"out1"
argument_list|,
name|count
argument_list|,
name|word
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|MultiOutputFormat
operator|.
name|write
argument_list|(
literal|"out2"
argument_list|,
name|word
argument_list|,
name|count
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|MultiOutputFormat
operator|.
name|write
argument_list|(
literal|"out3"
argument_list|,
name|word
argument_list|,
name|count
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|NullOutputFormat
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
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
name|output
operator|.
name|NullOutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|OutputCommitter
name|getOutputCommitter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|OutputCommitter
argument_list|()
block|{
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{         }
specifier|public
name|void
name|cleanupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
block|{         }
specifier|public
name|void
name|commitJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
block|{         }
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"needsTaskCommit is false but commitTask was called"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|needsTaskCommit
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|setupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
block|{         }
specifier|public
name|void
name|setupTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
block|{         }
block|}
return|;
block|}
block|}
block|}
end_class

end_unit


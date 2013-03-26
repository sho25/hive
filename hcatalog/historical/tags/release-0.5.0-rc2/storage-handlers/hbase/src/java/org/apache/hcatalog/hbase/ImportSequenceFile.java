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
name|hcatalog
operator|.
name|hbase
package|;
end_package

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
name|permission
operator|.
name|FsPermission
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
name|hbase
operator|.
name|mapreduce
operator|.
name|HFileOutputFormat
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
name|hbase
operator|.
name|mapreduce
operator|.
name|LoadIncrementalHFiles
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
name|hbase
operator|.
name|mapreduce
operator|.
name|PutSortReducer
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
name|hbase
operator|.
name|mapreduce
operator|.
name|hadoopbackport
operator|.
name|TotalOrderPartitioner
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
name|Map
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
name|hbase
operator|.
name|client
operator|.
name|HTable
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
name|hbase
operator|.
name|client
operator|.
name|Put
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
name|hbase
operator|.
name|io
operator|.
name|ImmutableBytesWritable
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
name|JobStatus
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
name|TaskAttemptID
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
name|SequenceFileInputFormat
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

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|mapreduce
operator|.
name|hadoopbackport
operator|.
name|TotalOrderPartitioner
operator|.
name|DEFAULT_PATH
import|;
end_import

begin_comment
comment|/**  * MapReduce job which reads a series of Puts stored in a sequence file  * and imports the data into HBase. It needs to create the necessary HBase  * regions using HFileOutputFormat and then notify the correct region servers  * to doBulkLoad(). This will be used After an MR job has written the SequenceFile  * and data needs to be bulk loaded onto HBase.  */
end_comment

begin_class
class|class
name|ImportSequenceFile
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ImportSequenceFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"HCatImportSequenceFile"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|String
name|IMPORTER_WORK_DIR
init|=
literal|"_IMPORTER_MR_WORK_DIR"
decl_stmt|;
specifier|private
specifier|static
class|class
name|SequenceFileImporter
extends|extends
name|Mapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|rowKey
parameter_list|,
name|Put
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|context
operator|.
name|write
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|value
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|ImporterOutputFormat
extends|extends
name|HFileOutputFormat
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
throws|throws
name|IOException
block|{
specifier|final
name|OutputCommitter
name|baseOutputCommitter
init|=
name|super
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
operator|new
name|OutputCommitter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|setupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|setupJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|setupTask
argument_list|(
name|taskContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsTaskCommit
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|baseOutputCommitter
operator|.
name|needsTaskCommit
argument_list|(
name|taskContext
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|commitTask
argument_list|(
name|taskContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|abortTask
argument_list|(
name|taskContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|JobStatus
operator|.
name|State
name|state
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|baseOutputCommitter
operator|.
name|abortJob
argument_list|(
name|jobContext
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupScratch
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|baseOutputCommitter
operator|.
name|commitJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
try|try
block|{
comment|//import hfiles
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|conf
argument_list|)
operator|.
name|doBulkLoad
argument_list|(
name|HFileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|jobContext
argument_list|)
argument_list|,
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HBaseConstants
operator|.
name|PROPERTY_OUTPUT_TABLE_NAME_KEY
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"BulkLoad failed."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|cleanupScratch
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanupJob
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|baseOutputCommitter
operator|.
name|cleanupJob
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanupScratch
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanupScratch
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|HFileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|context
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
specifier|private
specifier|static
name|Job
name|createSubmittableJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Path
name|inputDir
parameter_list|,
name|Path
name|scratchDir
parameter_list|,
name|boolean
name|localMode
parameter_list|)
throws|throws
name|IOException
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|SequenceFileImporter
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|inputDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|SequenceFileImporter
operator|.
name|class
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|PutSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|FileOutputFormat
operator|.
name|setOutputPath
argument_list|(
name|job
argument_list|,
name|scratchDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|Put
operator|.
name|class
argument_list|)
expr_stmt|;
name|HFileOutputFormat
operator|.
name|configureIncrementalLoad
argument_list|(
name|job
argument_list|,
name|table
argument_list|)
expr_stmt|;
comment|//override OutputFormatClass with our own so we can include cleanup in the committer
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|ImporterOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
comment|//local mode doesn't support symbolic links so we have to manually set the actual path
if|if
condition|(
name|localMode
condition|)
block|{
name|String
name|partitionFile
init|=
literal|null
decl_stmt|;
for|for
control|(
name|URI
name|uri
range|:
name|DistributedCache
operator|.
name|getCacheFiles
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|DEFAULT_PATH
operator|.
name|equals
argument_list|(
name|uri
operator|.
name|getFragment
argument_list|()
argument_list|)
condition|)
block|{
name|partitionFile
operator|=
name|uri
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|partitionFile
operator|=
name|partitionFile
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|partitionFile
operator|.
name|lastIndexOf
argument_list|(
literal|"#"
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|TotalOrderPartitioner
operator|.
name|PARTITIONER_PATH
argument_list|,
name|partitionFile
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|job
return|;
block|}
comment|/**      * Method to run the Importer MapReduce Job. Normally will be called by another MR job      * during OutputCommitter.commitJob().      * @param parentContext JobContext of the parent job      * @param tableName name of table to bulk load data into      * @param InputDir path of SequenceFile formatted data to read      * @param scratchDir temporary path for the Importer MR job to build the HFiles which will be imported      * @return      */
specifier|static
name|boolean
name|runJob
parameter_list|(
name|JobContext
name|parentContext
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Path
name|InputDir
parameter_list|,
name|Path
name|scratchDir
parameter_list|)
block|{
name|Configuration
name|parentConf
init|=
name|parentContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|el
range|:
name|parentConf
control|)
block|{
if|if
condition|(
name|el
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"hbase."
argument_list|)
condition|)
name|conf
operator|.
name|set
argument_list|(
name|el
operator|.
name|getKey
argument_list|()
argument_list|,
name|el
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|el
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"mapred.cache.archives"
argument_list|)
condition|)
name|conf
operator|.
name|set
argument_list|(
name|el
operator|.
name|getKey
argument_list|()
argument_list|,
name|el
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//Inherit jar dependencies added to distributed cache loaded by parent job
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.job.classpath.archives"
argument_list|,
name|parentConf
operator|.
name|get
argument_list|(
literal|"mapred.job.classpath.archives"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapreduce.job.cache.archives.visibilities"
argument_list|,
name|parentConf
operator|.
name|get
argument_list|(
literal|"mapreduce.job.cache.archives.visibilities"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
comment|//Temporary fix until hbase security is ready
comment|//We need the written HFile to be world readable so
comment|//hbase regionserver user has the privileges to perform a hdfs move
if|if
condition|(
name|parentConf
operator|.
name|getBoolean
argument_list|(
literal|"hadoop.security.authorization"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|FsPermission
operator|.
name|setUMask
argument_list|(
name|conf
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"----------"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|HBaseConstants
operator|.
name|PROPERTY_OUTPUT_TABLE_NAME_KEY
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|JobContext
operator|.
name|JOB_CANCEL_DELEGATION_TOKEN
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|boolean
name|localMode
init|=
literal|"local"
operator|.
name|equals
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|parentConf
argument_list|)
decl_stmt|;
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Job
argument_list|(
name|parentConf
argument_list|)
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
name|IMPORTER_WORK_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|workDir
argument_list|)
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Importer work directory already exists: "
operator|+
name|workDir
argument_list|)
throw|;
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|InputDir
argument_list|,
name|scratchDir
argument_list|,
name|localMode
argument_list|)
decl_stmt|;
name|job
operator|.
name|setWorkingDirectory
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addAll
argument_list|(
name|parentContext
operator|.
name|getCredentials
argument_list|()
argument_list|)
expr_stmt|;
name|success
operator|=
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|workDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|//We only cleanup on success because failure might've been caused by existence of target directory
if|if
condition|(
name|localMode
operator|&&
name|success
condition|)
block|{
operator|new
name|ImporterOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|commitJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ImportSequenceFile Failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ImportSequenceFile Failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"ImportSequenceFile Failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|success
return|;
block|}
block|}
end_class

end_unit


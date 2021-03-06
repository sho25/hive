begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
package|;
end_package

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
name|hbase
operator|.
name|TableName
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
name|BufferedMutator
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
name|Connection
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
name|ConnectionFactory
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
name|FileSinkOperator
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
name|HiveOutputFormat
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|Durability
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
name|hbase
operator|.
name|mapred
operator|.
name|TableMapReduceUtil
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
name|TableOutputCommitter
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
name|TableOutputFormat
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
name|shims
operator|.
name|ShimLoader
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
name|util
operator|.
name|Progressable
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * HiveHBaseTableOutputFormat implements HiveOutputFormat for HBase tables.  */
end_comment

begin_class
specifier|public
class|class
name|HiveHBaseTableOutputFormat
extends|extends
name|TableOutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|>
implements|implements
name|HiveOutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Object
argument_list|>
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveHBaseTableOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|JobConf
name|jc
parameter_list|)
throws|throws
name|IOException
block|{
comment|//obtain delegation tokens for the job
if|if
condition|(
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|hasKerberosCredentials
argument_list|()
condition|)
block|{
name|TableMapReduceUtil
operator|.
name|initCredentials
argument_list|(
name|jc
argument_list|)
expr_stmt|;
block|}
name|String
name|hbaseTableName
init|=
name|jc
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|)
decl_stmt|;
name|jc
operator|.
name|set
argument_list|(
name|TableOutputFormat
operator|.
name|OUTPUT_TABLE
argument_list|,
name|hbaseTableName
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|JobContext
name|jobContext
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|newJobContext
argument_list|(
name|job
argument_list|)
decl_stmt|;
try|try
block|{
name|checkOutputSpecs
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Object
argument_list|>
name|getRecordWriter
parameter_list|(
name|FileSystem
name|fileSystem
parameter_list|,
name|JobConf
name|jobConf
parameter_list|,
name|String
name|name
parameter_list|,
name|Progressable
name|progressable
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getMyRecordWriter
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
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
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|TableOutputCommitter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileSinkOperator
operator|.
name|RecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|finalOutPath
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|valueClass
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Properties
name|tableProperties
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getMyRecordWriter
argument_list|(
name|jobConf
argument_list|)
return|;
block|}
specifier|private
name|MyRecordWriter
name|getMyRecordWriter
parameter_list|(
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|hbaseTableName
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|HBaseSerDe
operator|.
name|HBASE_TABLE_NAME
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|TableOutputFormat
operator|.
name|OUTPUT_TABLE
argument_list|,
name|hbaseTableName
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|walEnabled
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|jobConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_HBASE_WAL_ENABLED
argument_list|)
decl_stmt|;
specifier|final
name|Connection
name|conn
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|jobConf
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|BufferedMutator
name|table
init|=
name|conn
operator|.
name|getBufferedMutator
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|hbaseTableName
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|MyRecordWriter
argument_list|(
name|table
argument_list|,
name|conn
argument_list|,
name|walEnabled
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|MyRecordWriter
implements|implements
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Object
argument_list|>
implements|,
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
name|FileSinkOperator
operator|.
name|RecordWriter
block|{
specifier|private
specifier|final
name|BufferedMutator
name|m_table
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|m_walEnabled
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|m_connection
decl_stmt|;
specifier|public
name|MyRecordWriter
parameter_list|(
name|BufferedMutator
name|table
parameter_list|,
name|Connection
name|connection
parameter_list|,
name|boolean
name|walEnabled
parameter_list|)
block|{
name|m_table
operator|=
name|table
expr_stmt|;
name|m_walEnabled
operator|=
name|walEnabled
expr_stmt|;
name|m_connection
operator|=
name|connection
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|m_table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Put
condition|)
block|{
name|put
operator|=
operator|(
name|Put
operator|)
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|PutWritable
condition|)
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
operator|(
operator|(
name|PutWritable
operator|)
name|value
operator|)
operator|.
name|getPut
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal Argument "
operator|+
operator|(
name|value
operator|==
literal|null
condition|?
literal|"null"
else|:
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|m_walEnabled
condition|)
block|{
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SYNC_WAL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
block|}
name|m_table
operator|.
name|mutate
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|finalize
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|m_table
operator|.
name|close
argument_list|()
expr_stmt|;
name|m_connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|super
operator|.
name|finalize
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
name|write
argument_list|(
literal|null
argument_list|,
name|w
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
name|close
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


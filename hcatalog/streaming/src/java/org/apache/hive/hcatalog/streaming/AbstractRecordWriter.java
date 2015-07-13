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
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|common
operator|.
name|JavaUtils
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
name|metastore
operator|.
name|IMetaStoreClient
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|NoSuchObjectException
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|SerDe
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
name|SerDeException
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
name|ReflectionUtils
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
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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
name|Random
import|;
end_import

begin_class
specifier|abstract
class|class
name|AbstractRecordWriter
implements|implements
name|RecordWriter
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AbstractRecordWriter
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|final
name|HiveEndPoint
name|endPoint
decl_stmt|;
specifier|final
name|Table
name|tbl
decl_stmt|;
specifier|final
name|IMetaStoreClient
name|msClient
decl_stmt|;
name|RecordUpdater
name|updater
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|int
name|totalBuckets
decl_stmt|;
specifier|private
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
name|int
name|currentBucketId
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|Path
name|partitionPath
decl_stmt|;
specifier|final
name|AcidOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|outf
decl_stmt|;
specifier|protected
name|AbstractRecordWriter
parameter_list|(
name|HiveEndPoint
name|endPoint
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|ConnectionError
throws|,
name|StreamingException
block|{
name|this
operator|.
name|endPoint
operator|=
name|endPoint
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
operator|!=
literal|null
condition|?
name|conf
else|:
name|HiveEndPoint
operator|.
name|createHiveConf
argument_list|(
name|DelimitedInputWriter
operator|.
name|class
argument_list|,
name|endPoint
operator|.
name|metaStoreUri
argument_list|)
expr_stmt|;
try|try
block|{
name|msClient
operator|=
name|HCatUtil
operator|.
name|getHiveMetastoreClient
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|tbl
operator|=
name|msClient
operator|.
name|getTable
argument_list|(
name|endPoint
operator|.
name|database
argument_list|,
name|endPoint
operator|.
name|table
argument_list|)
expr_stmt|;
name|this
operator|.
name|partitionPath
operator|=
name|getPathForEndPoint
argument_list|(
name|msClient
argument_list|,
name|endPoint
argument_list|)
expr_stmt|;
name|this
operator|.
name|totalBuckets
operator|=
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
if|if
condition|(
name|totalBuckets
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|StreamingException
argument_list|(
literal|"Cannot stream to table that has not been bucketed : "
operator|+
name|endPoint
argument_list|)
throw|;
block|}
name|String
name|outFormatName
init|=
name|this
operator|.
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
decl_stmt|;
name|outf
operator|=
operator|(
name|AcidOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|outFormatName
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ConnectionError
argument_list|(
name|endPoint
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ConnectionError
argument_list|(
name|endPoint
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|StreamingException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|StreamingException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|StreamingException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|AbstractRecordWriter
parameter_list|(
name|HiveEndPoint
name|endPoint
parameter_list|)
throws|throws
name|ConnectionError
throws|,
name|StreamingException
block|{
name|this
argument_list|(
name|endPoint
argument_list|,
name|HiveEndPoint
operator|.
name|createHiveConf
argument_list|(
name|AbstractRecordWriter
operator|.
name|class
argument_list|,
name|endPoint
operator|.
name|metaStoreUri
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|abstract
name|SerDe
name|getSerde
parameter_list|()
throws|throws
name|SerializationError
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|StreamingIOFailure
block|{
try|try
block|{
name|updater
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|StreamingIOFailure
argument_list|(
literal|"Unable to flush recordUpdater"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
throws|throws
name|StreamingIOFailure
block|{   }
comment|/**    * Creates a new record updater for the new batch    * @param minTxnId smallest Txnid in the batch    * @param maxTxnID largest Txnid in the batch    * @throws StreamingIOFailure if failed to create record updater    */
annotation|@
name|Override
specifier|public
name|void
name|newBatch
parameter_list|(
name|Long
name|minTxnId
parameter_list|,
name|Long
name|maxTxnID
parameter_list|)
throws|throws
name|StreamingIOFailure
throws|,
name|SerializationError
block|{
try|try
block|{
name|this
operator|.
name|currentBucketId
operator|=
name|rand
operator|.
name|nextInt
argument_list|(
name|totalBuckets
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating Record updater"
argument_list|)
expr_stmt|;
name|updater
operator|=
name|createRecordUpdater
argument_list|(
name|currentBucketId
argument_list|,
name|minTxnId
argument_list|,
name|maxTxnID
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
literal|"Failed creating record updater"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|StreamingIOFailure
argument_list|(
literal|"Unable to get new record Updater"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeBatch
parameter_list|()
throws|throws
name|StreamingIOFailure
block|{
try|try
block|{
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|updater
operator|=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|StreamingIOFailure
argument_list|(
literal|"Unable to close recordUpdater"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|RecordUpdater
name|createRecordUpdater
parameter_list|(
name|int
name|bucketId
parameter_list|,
name|Long
name|minTxnId
parameter_list|,
name|Long
name|maxTxnID
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerializationError
block|{
try|try
block|{
return|return
name|outf
operator|.
name|getRecordUpdater
argument_list|(
name|partitionPath
argument_list|,
operator|new
name|AcidOutputFormat
operator|.
name|Options
argument_list|(
name|conf
argument_list|)
operator|.
name|inspector
argument_list|(
name|getSerde
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
operator|.
name|bucket
argument_list|(
name|bucketId
argument_list|)
operator|.
name|minimumTransactionId
argument_list|(
name|minTxnId
argument_list|)
operator|.
name|maximumTransactionId
argument_list|(
name|maxTxnID
argument_list|)
operator|.
name|statementId
argument_list|(
operator|-
literal|1
argument_list|)
operator|.
name|finalDestination
argument_list|(
name|partitionPath
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerializationError
argument_list|(
literal|"Failed to get object inspector from Serde "
operator|+
name|getSerde
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Path
name|getPathForEndPoint
parameter_list|(
name|IMetaStoreClient
name|msClient
parameter_list|,
name|HiveEndPoint
name|endPoint
parameter_list|)
throws|throws
name|StreamingException
block|{
try|try
block|{
name|String
name|location
decl_stmt|;
if|if
condition|(
name|endPoint
operator|.
name|partitionVals
operator|==
literal|null
operator|||
name|endPoint
operator|.
name|partitionVals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|location
operator|=
name|msClient
operator|.
name|getTable
argument_list|(
name|endPoint
operator|.
name|database
argument_list|,
name|endPoint
operator|.
name|table
argument_list|)
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|location
operator|=
name|msClient
operator|.
name|getPartition
argument_list|(
name|endPoint
operator|.
name|database
argument_list|,
name|endPoint
operator|.
name|table
argument_list|,
name|endPoint
operator|.
name|partitionVals
argument_list|)
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|location
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|StreamingException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|". Unable to get path for end point: "
operator|+
name|endPoint
operator|.
name|partitionVals
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


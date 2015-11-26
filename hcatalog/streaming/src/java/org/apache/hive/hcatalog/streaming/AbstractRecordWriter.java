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
name|FieldSchema
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
name|ObjectInspectorUtils
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractRecordWriter
implements|implements
name|RecordWriter
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
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
specifier|protected
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|bucketIds
decl_stmt|;
name|ArrayList
argument_list|<
name|RecordUpdater
argument_list|>
name|updaters
init|=
literal|null
decl_stmt|;
specifier|public
specifier|final
name|int
name|totalBuckets
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
specifier|private
name|Object
index|[]
name|bucketFieldData
decl_stmt|;
comment|// Pre-allocated in constructor. Updated on each write.
specifier|private
name|Long
name|curBatchMinTxnId
decl_stmt|;
specifier|private
name|Long
name|curBatchMaxTxnId
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
name|this
operator|.
name|bucketIds
operator|=
name|getBucketColIDs
argument_list|(
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getBucketCols
argument_list|()
argument_list|,
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|bucketFieldData
operator|=
operator|new
name|Object
index|[
name|bucketIds
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
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
name|bucketFieldData
operator|=
operator|new
name|Object
index|[
name|bucketIds
operator|.
name|size
argument_list|()
index|]
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
comment|/**    * used to tag error msgs to provied some breadcrumbs    */
name|String
name|getWatermark
parameter_list|()
block|{
return|return
name|partitionPath
operator|+
literal|" txnIds["
operator|+
name|curBatchMinTxnId
operator|+
literal|","
operator|+
name|curBatchMaxTxnId
operator|+
literal|"]"
return|;
block|}
comment|// return the column numbers of the bucketed columns
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|getBucketColIDs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|bucketCols
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|HashSet
argument_list|<
name|String
argument_list|>
name|bucketSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|bucketCols
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
name|cols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|bucketSet
operator|.
name|contains
argument_list|(
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Get the SerDe for the Objects created by {@link #encode}.  This is public so that test    * frameworks can use it.    * @return serde    * @throws SerializationError    */
specifier|public
specifier|abstract
name|SerDe
name|getSerde
parameter_list|()
throws|throws
name|SerializationError
function_decl|;
comment|/**    * Encode a record as an Object that Hive can read with the ObjectInspector associated with the    * serde returned by {@link #getSerde}.  This is public so that test frameworks can use it.    * @param record record to be deserialized    * @return deserialized record as an Object    * @throws SerializationError    */
specifier|public
specifier|abstract
name|Object
name|encode
parameter_list|(
name|byte
index|[]
name|record
parameter_list|)
throws|throws
name|SerializationError
function_decl|;
specifier|protected
specifier|abstract
name|ObjectInspector
index|[]
name|getBucketObjectInspectors
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|StructObjectInspector
name|getRecordObjectInspector
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|StructField
index|[]
name|getBucketStructFields
parameter_list|()
function_decl|;
comment|// returns the bucket number to which the record belongs to
specifier|protected
name|int
name|getBucket
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|SerializationError
block|{
name|ObjectInspector
index|[]
name|inspectors
init|=
name|getBucketObjectInspectors
argument_list|()
decl_stmt|;
name|Object
index|[]
name|bucketFields
init|=
name|getBucketFields
argument_list|(
name|row
argument_list|)
decl_stmt|;
return|return
name|ObjectInspectorUtils
operator|.
name|getBucketNumber
argument_list|(
name|bucketFields
argument_list|,
name|inspectors
argument_list|,
name|totalBuckets
argument_list|)
return|;
block|}
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
for|for
control|(
name|RecordUpdater
name|updater
range|:
name|updaters
control|)
block|{
name|updater
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating Record updater"
argument_list|)
expr_stmt|;
name|curBatchMinTxnId
operator|=
name|minTxnId
expr_stmt|;
name|curBatchMaxTxnId
operator|=
name|maxTxnID
expr_stmt|;
name|updaters
operator|=
name|createRecordUpdaters
argument_list|(
name|totalBuckets
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
name|String
name|errMsg
init|=
literal|"Failed creating RecordUpdaterS for "
operator|+
name|getWatermark
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|errMsg
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|StreamingIOFailure
argument_list|(
name|errMsg
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
name|boolean
name|haveError
init|=
literal|false
decl_stmt|;
for|for
control|(
name|RecordUpdater
name|updater
range|:
name|updaters
control|)
block|{
try|try
block|{
comment|//try not to leave any files open
name|updater
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|haveError
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to close "
operator|+
name|updater
operator|+
literal|" due to: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
name|updaters
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|haveError
condition|)
block|{
throw|throw
operator|new
name|StreamingIOFailure
argument_list|(
literal|"Encountered errors while closing (see logs) "
operator|+
name|getWatermark
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|static
name|ObjectInspector
index|[]
name|getObjectInspectorsForBucketedCols
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|bucketIds
parameter_list|,
name|StructObjectInspector
name|recordObjInspector
parameter_list|)
throws|throws
name|SerializationError
block|{
name|ObjectInspector
index|[]
name|result
init|=
operator|new
name|ObjectInspector
index|[
name|bucketIds
operator|.
name|size
argument_list|()
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
name|bucketIds
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|bucketId
init|=
name|bucketIds
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|result
index|[
name|i
index|]
operator|=
name|recordObjInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|get
argument_list|(
name|bucketId
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Object
index|[]
name|getBucketFields
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|SerializationError
block|{
name|StructObjectInspector
name|recordObjInspector
init|=
name|getRecordObjectInspector
argument_list|()
decl_stmt|;
name|StructField
index|[]
name|bucketStructFields
init|=
name|getBucketStructFields
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
name|bucketIds
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|bucketFieldData
index|[
name|i
index|]
operator|=
name|recordObjInspector
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|bucketStructFields
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|bucketFieldData
return|;
block|}
specifier|private
name|ArrayList
argument_list|<
name|RecordUpdater
argument_list|>
name|createRecordUpdaters
parameter_list|(
name|int
name|bucketCount
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
name|ArrayList
argument_list|<
name|RecordUpdater
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|RecordUpdater
argument_list|>
argument_list|(
name|bucketCount
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|bucket
init|=
literal|0
init|;
name|bucket
operator|<
name|bucketCount
condition|;
name|bucket
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|createRecordUpdater
argument_list|(
name|bucket
argument_list|,
name|minTxnId
argument_list|,
name|maxTxnID
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
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


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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Iterator
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|hbase
operator|.
name|KeyValue
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|util
operator|.
name|Bytes
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
name|DataInputBuffer
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
name|DataOutputBuffer
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
name|hive
operator|.
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|FamilyRevision
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
name|hbase
operator|.
name|snapshot
operator|.
name|RevisionManager
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
name|hbase
operator|.
name|snapshot
operator|.
name|TableSnapshot
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
name|InputJobInfo
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

begin_comment
comment|/**  * The Class HbaseSnapshotRecordReader implements logic for filtering records  * based on snapshot.  */
end_comment

begin_class
class|class
name|HbaseSnapshotRecordReader
implements|implements
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
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
name|HbaseSnapshotRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|InputJobInfo
name|inpJobInfo
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxRevisions
init|=
literal|1
decl_stmt|;
specifier|private
name|ResultScanner
name|scanner
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|HTable
name|htable
decl_stmt|;
specifier|private
name|TableSnapshot
name|snapshot
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Result
argument_list|>
name|resultItr
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Long
argument_list|>
name|allAbortedTransactions
decl_stmt|;
specifier|private
name|DataOutputBuffer
name|valueOut
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
specifier|private
name|DataInputBuffer
name|valueIn
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|HbaseSnapshotRecordReader
parameter_list|(
name|InputJobInfo
name|inputJobInfo
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|inpJobInfo
operator|=
name|inputJobInfo
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|String
name|snapshotString
init|=
name|conf
operator|.
name|get
argument_list|(
name|HBaseConstants
operator|.
name|PROPERTY_TABLE_SNAPSHOT_KEY
argument_list|)
decl_stmt|;
name|HCatTableSnapshot
name|hcatSnapshot
init|=
operator|(
name|HCatTableSnapshot
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|snapshotString
argument_list|)
decl_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|HBaseRevisionManagerUtil
operator|.
name|convertSnapshot
argument_list|(
name|hcatSnapshot
argument_list|,
name|inpJobInfo
operator|.
name|getTableInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|restart
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|restart
parameter_list|(
name|byte
index|[]
name|firstRow
parameter_list|)
throws|throws
name|IOException
block|{
name|allAbortedTransactions
operator|=
name|getAbortedTransactions
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|htable
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|scan
argument_list|)
expr_stmt|;
name|long
name|maxValidRevision
init|=
name|getMaximumRevision
argument_list|(
name|scan
argument_list|,
name|snapshot
argument_list|)
decl_stmt|;
while|while
condition|(
name|allAbortedTransactions
operator|.
name|contains
argument_list|(
name|maxValidRevision
argument_list|)
condition|)
block|{
name|maxValidRevision
operator|--
expr_stmt|;
block|}
name|Scan
name|newScan
init|=
operator|new
name|Scan
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|newScan
operator|.
name|setStartRow
argument_list|(
name|firstRow
argument_list|)
expr_stmt|;
comment|//TODO: See if filters in 0.92 can be used to optimize the scan
comment|//TODO: Consider create a custom snapshot filter
comment|//TODO: Make min revision a constant in RM
name|newScan
operator|.
name|setTimeRange
argument_list|(
literal|0
argument_list|,
name|maxValidRevision
operator|+
literal|1
argument_list|)
expr_stmt|;
name|newScan
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|this
operator|.
name|scanner
operator|=
name|this
operator|.
name|htable
operator|.
name|getScanner
argument_list|(
name|newScan
argument_list|)
expr_stmt|;
name|resultItr
operator|=
name|this
operator|.
name|scanner
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|Long
argument_list|>
name|getAbortedTransactions
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|Long
argument_list|>
name|abortedTransactions
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|RevisionManager
name|rm
init|=
literal|null
decl_stmt|;
try|try
block|{
name|rm
operator|=
name|HBaseRevisionManagerUtil
operator|.
name|getOpenedRevisionManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|familyKey
range|:
name|families
control|)
block|{
name|String
name|family
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|familyKey
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FamilyRevision
argument_list|>
name|abortedWriteTransactions
init|=
name|rm
operator|.
name|getAbortedWriteTransactions
argument_list|(
name|tableName
argument_list|,
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|abortedWriteTransactions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FamilyRevision
name|revision
range|:
name|abortedWriteTransactions
control|)
block|{
name|abortedTransactions
operator|.
name|add
argument_list|(
name|revision
operator|.
name|getRevision
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|abortedTransactions
return|;
block|}
finally|finally
block|{
name|HBaseRevisionManagerUtil
operator|.
name|closeRevisionManagerQuietly
argument_list|(
name|rm
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|long
name|getMaximumRevision
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|TableSnapshot
name|snapshot
parameter_list|)
block|{
name|long
name|maxRevision
init|=
literal|0
decl_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|familyKey
range|:
name|families
control|)
block|{
name|String
name|family
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|familyKey
argument_list|)
decl_stmt|;
name|long
name|revision
init|=
name|snapshot
operator|.
name|getRevision
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|revision
operator|>
name|maxRevision
condition|)
name|maxRevision
operator|=
name|revision
expr_stmt|;
block|}
return|return
name|maxRevision
return|;
block|}
comment|/*      * @param htable The HTable ( of HBase) to use for the record reader.      *      */
specifier|public
name|void
name|setHTable
parameter_list|(
name|HTable
name|htable
parameter_list|)
block|{
name|this
operator|.
name|htable
operator|=
name|htable
expr_stmt|;
block|}
comment|/*      * @param scan The scan to be used for reading records.      *      */
specifier|public
name|void
name|setScan
parameter_list|(
name|Scan
name|scan
parameter_list|)
block|{
name|this
operator|.
name|scan
operator|=
name|scan
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ImmutableBytesWritable
name|createKey
parameter_list|()
block|{
return|return
operator|new
name|ImmutableBytesWritable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|Result
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
block|{
comment|// This should be the ordinal tuple in the range;
comment|// not clear how to calculate...
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Depends on the total number of tuples
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|resultItr
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The HBase result iterator is found null. It is possible"
operator|+
literal|" that the record reader has already been closed."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
while|while
condition|(
name|resultItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Result
name|temp
init|=
name|resultItr
operator|.
name|next
argument_list|()
decl_stmt|;
name|Result
name|hbaseRow
init|=
name|prepareResult
argument_list|(
name|temp
operator|.
name|list
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hbaseRow
operator|!=
literal|null
condition|)
block|{
comment|// Update key and value. Currently no way to avoid serialization/de-serialization
comment|// as no setters are available.
name|key
operator|.
name|set
argument_list|(
name|hbaseRow
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|valueOut
operator|.
name|reset
argument_list|()
expr_stmt|;
name|hbaseRow
operator|.
name|write
argument_list|(
name|valueOut
argument_list|)
expr_stmt|;
name|valueIn
operator|.
name|reset
argument_list|(
name|valueOut
operator|.
name|getData
argument_list|()
argument_list|,
name|valueOut
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|value
operator|.
name|readFields
argument_list|(
name|valueIn
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|Result
name|prepareResult
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keyvalues
parameter_list|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|finalKeyVals
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|qualValMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|keyvalues
control|)
block|{
name|byte
index|[]
name|cf
init|=
name|kv
operator|.
name|getFamily
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|kv
operator|.
name|getQualifier
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|cf
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
decl_stmt|;
if|if
condition|(
name|qualValMap
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|kvs
operator|=
name|qualValMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|kvs
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|String
name|family
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|kv
operator|.
name|getFamily
argument_list|()
argument_list|)
decl_stmt|;
comment|//Ignore aborted transactions
if|if
condition|(
name|allAbortedTransactions
operator|.
name|contains
argument_list|(
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|long
name|desiredTS
init|=
name|snapshot
operator|.
name|getRevision
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|kv
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|desiredTS
condition|)
block|{
name|kvs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
name|qualValMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|kvs
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
name|qualValMap
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|cf
range|:
name|keys
control|)
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|qualValMap
operator|.
name|get
argument_list|(
name|cf
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxRevisions
operator|<=
name|kvs
operator|.
name|size
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxRevisions
condition|;
name|i
operator|++
control|)
block|{
name|finalKeyVals
operator|.
name|add
argument_list|(
name|kvs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|finalKeyVals
operator|.
name|addAll
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|finalKeyVals
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
name|KeyValue
index|[]
name|kvArray
init|=
operator|new
name|KeyValue
index|[
name|finalKeyVals
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|finalKeyVals
operator|.
name|toArray
argument_list|(
name|kvArray
argument_list|)
expr_stmt|;
return|return
operator|new
name|Result
argument_list|(
name|kvArray
argument_list|)
return|;
block|}
block|}
comment|/*      * @see org.apache.hadoop.hbase.mapred.TableRecordReader#close()      */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|this
operator|.
name|resultItr
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


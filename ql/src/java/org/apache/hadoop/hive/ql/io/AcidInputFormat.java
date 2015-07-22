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
name|hive
operator|.
name|common
operator|.
name|ValidTxnList
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
name|WritableComparable
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
name|InputFormat
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Collections
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

begin_comment
comment|/**  * The interface required for input formats that what to support ACID  * transactions.  *<p>  * The goal is to provide ACID transactions to Hive. There are  * several primary use cases:  *<ul>  *<li>Streaming ingest- Allow Flume or Storm to stream data into Hive  *   tables with relatively low latency (~30 seconds).</li>  *<li>Dimension table update- Allow updates of dimension tables without  *   overwriting the entire partition (or table) using standard SQL syntax.</li>  *<li>Fact table inserts- Insert data into fact tables at granularity other  *   than entire partitions using standard SQL syntax.</li>  *<li>Fact table update- Update large fact tables to correct data that  *   was previously loaded.</li>  *</ul>  * It is important to support batch updates and maintain read consistency within  * a query. A non-goal is to support many simultaneous updates or to replace  * online transactions systems.  *<p>  * The design changes the layout of data within a partition from being in files  * at the top level to having base and delta directories. Each write operation  * will be assigned a sequential global transaction id and each read operation  * will request the list of valid transaction ids.  *<ul>  *<li>Old format -  *<pre>  *        $partition/$bucket  *</pre></li>  *<li>New format -  *<pre>  *        $partition/base_$tid/$bucket  *                   delta_$tid_$tid_$stid/$bucket  *</pre></li>  *</ul>  *<p>  * With each new write operation a new delta directory is created with events  * that correspond to inserted, updated, or deleted rows. Each of the files is  * stored sorted by the original transaction id (ascending), bucket (ascending),  * row id (ascending), and current transaction id (descending). Thus the files  * can be merged by advancing through the files in parallel.  * The stid is unique id (within the transaction) of the statement that created  * this delta file.  *<p>  * The base files include all transactions from the beginning of time  * (transaction id 0) to the transaction in the directory name. Delta  * directories include transactions (inclusive) between the two transaction ids.  *<p>  * Because read operations get the list of valid transactions when they start,  * all reads are performed on that snapshot, regardless of any transactions that  * are committed afterwards.  *<p>  * The base and the delta directories have the transaction ids so that major  * (merge all deltas into the base) and minor (merge several deltas together)  * compactions can happen while readers continue their processing.  *<p>  * To support transitions between non-ACID layouts to ACID layouts, the input  * formats are expected to support both layouts and detect the correct one.  *<p>  *   A note on the KEY of this InputFormat.    *   For row-at-a-time processing, KEY can conveniently pass RowId into the operator  *   pipeline.  For vectorized execution the KEY could perhaps represent a range in the batch.  *   Since {@link org.apache.hadoop.hive.ql.io.orc.OrcInputFormat} is declared to return  *   {@code NullWritable} key, {@link org.apache.hadoop.hive.ql.io.AcidInputFormat.AcidRecordReader} is defined  *   to provide access to the RowId.  Other implementations of AcidInputFormat can use either  *   mechanism.  *</p>  *   * @param<VALUE> The row type  */
end_comment

begin_interface
specifier|public
interface|interface
name|AcidInputFormat
parameter_list|<
name|KEY
extends|extends
name|WritableComparable
parameter_list|,
name|VALUE
parameter_list|>
extends|extends
name|InputFormat
argument_list|<
name|KEY
argument_list|,
name|VALUE
argument_list|>
extends|,
name|InputFormatChecker
block|{
specifier|static
specifier|final
class|class
name|DeltaMetaData
implements|implements
name|Writable
block|{
specifier|private
name|long
name|minTxnId
decl_stmt|;
specifier|private
name|long
name|maxTxnId
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|stmtIds
decl_stmt|;
specifier|public
name|DeltaMetaData
parameter_list|()
block|{
name|this
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|DeltaMetaData
parameter_list|(
name|long
name|minTxnId
parameter_list|,
name|long
name|maxTxnId
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|stmtIds
parameter_list|)
block|{
name|this
operator|.
name|minTxnId
operator|=
name|minTxnId
expr_stmt|;
name|this
operator|.
name|maxTxnId
operator|=
name|maxTxnId
expr_stmt|;
name|this
operator|.
name|stmtIds
operator|=
name|stmtIds
expr_stmt|;
block|}
name|long
name|getMinTxnId
parameter_list|()
block|{
return|return
name|minTxnId
return|;
block|}
name|long
name|getMaxTxnId
parameter_list|()
block|{
return|return
name|maxTxnId
return|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|getStmtIds
parameter_list|()
block|{
return|return
name|stmtIds
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|minTxnId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|maxTxnId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|stmtIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|stmtIds
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Integer
name|id
range|:
name|stmtIds
control|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|minTxnId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|maxTxnId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|int
name|numStatements
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numStatements
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
name|stmtIds
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|numStatements
condition|;
name|i
operator|++
control|)
block|{
name|stmtIds
operator|.
name|add
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Options for controlling the record readers.    */
specifier|public
specifier|static
class|class
name|Options
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|Reporter
name|reporter
decl_stmt|;
comment|/**      * Supply the configuration to use when reading.      * @param conf      */
specifier|public
name|Options
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**      * Supply the reporter.      * @param reporter the MapReduce reporter object      * @return this      */
specifier|public
name|Options
name|reporter
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
block|{
name|this
operator|.
name|reporter
operator|=
name|reporter
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|Reporter
name|getReporter
parameter_list|()
block|{
return|return
name|reporter
return|;
block|}
block|}
specifier|public
specifier|static
interface|interface
name|RowReader
parameter_list|<
name|V
parameter_list|>
extends|extends
name|RecordReader
argument_list|<
name|RecordIdentifier
argument_list|,
name|V
argument_list|>
block|{
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
function_decl|;
block|}
comment|/**    * Get a record reader that provides the user-facing view of the data after    * it has been merged together. The key provides information about the    * record's identifier (transaction, bucket, record id).    * @param split the split to read    * @param options the options to read with    * @return a record reader    * @throws IOException    */
specifier|public
name|RowReader
argument_list|<
name|VALUE
argument_list|>
name|getReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|Options
name|options
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
specifier|static
interface|interface
name|RawReader
parameter_list|<
name|V
parameter_list|>
extends|extends
name|RecordReader
argument_list|<
name|RecordIdentifier
argument_list|,
name|V
argument_list|>
block|{
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
function_decl|;
specifier|public
name|boolean
name|isDelete
parameter_list|(
name|V
name|value
parameter_list|)
function_decl|;
block|}
comment|/**    * Get a reader that returns the raw ACID events (insert, update, delete).    * Should only be used by the compactor.    * @param conf the configuration    * @param collapseEvents should the ACID events be collapsed so that only    *                       the last version of the row is kept.    * @param bucket the bucket to read    * @param validTxnList the list of valid transactions to use    * @param baseDirectory the base directory to read or the root directory for    *                      old style files    * @param deltaDirectory a list of delta files to include in the merge    * @return a record reader    * @throws IOException    */
name|RawReader
argument_list|<
name|VALUE
argument_list|>
name|getRawReader
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|collapseEvents
parameter_list|,
name|int
name|bucket
parameter_list|,
name|ValidTxnList
name|validTxnList
parameter_list|,
name|Path
name|baseDirectory
parameter_list|,
name|Path
index|[]
name|deltaDirectory
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * RecordReader returned by AcidInputFormat working in row-at-a-time mode should AcidRecordReader.    */
specifier|public
interface|interface
name|AcidRecordReader
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|RecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
name|RecordIdentifier
name|getRecordIdentifier
parameter_list|()
function_decl|;
block|}
block|}
end_interface

end_unit


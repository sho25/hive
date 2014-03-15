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
name|List
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
name|mapred
operator|.
name|TableSplit
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
name|TableInputFormat
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
name|hbase
operator|.
name|ResultWritable
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
name|HCatMapRedUtil
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|hcatalog
operator|.
name|mapreduce
operator|.
name|InputJobInfo
import|;
end_import

begin_comment
comment|/**  * This class HBaseInputFormat is a wrapper class of TableInputFormat in HBase.  */
end_comment

begin_class
class|class
name|HBaseInputFormat
implements|implements
name|InputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ResultWritable
argument_list|>
block|{
specifier|private
specifier|final
name|TableInputFormat
name|inputFormat
decl_stmt|;
specifier|public
name|HBaseInputFormat
parameter_list|()
block|{
name|inputFormat
operator|=
operator|new
name|TableInputFormat
argument_list|()
expr_stmt|;
block|}
comment|/*    * @param instance of InputSplit    *    * @param instance of TaskAttemptContext    *    * @return RecordReader    *    * @throws IOException    *    * @throws InterruptedException    *    * @see    * org.apache.hadoop.mapreduce.InputFormat#createRecordReader(org.apache    * .hadoop.mapreduce.InputSplit,    * org.apache.hadoop.mapreduce.TaskAttemptContext)    */
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|ResultWritable
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|jobString
init|=
name|job
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_JOB_INFO
argument_list|)
decl_stmt|;
name|InputJobInfo
name|inputJobInfo
init|=
operator|(
name|InputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|jobString
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|job
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|INPUT_TABLE
argument_list|)
decl_stmt|;
name|TableSplit
name|tSplit
init|=
operator|(
name|TableSplit
operator|)
name|split
decl_stmt|;
name|HbaseSnapshotRecordReader
name|recordReader
init|=
operator|new
name|HbaseSnapshotRecordReader
argument_list|(
name|inputJobInfo
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|inputFormat
operator|.
name|setConf
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|Scan
name|inputScan
init|=
name|inputFormat
operator|.
name|getScan
argument_list|()
decl_stmt|;
comment|// TODO: Make the caching configurable by the user
name|inputScan
operator|.
name|setCaching
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|inputScan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Scan
name|sc
init|=
operator|new
name|Scan
argument_list|(
name|inputScan
argument_list|)
decl_stmt|;
name|sc
operator|.
name|setStartRow
argument_list|(
name|tSplit
operator|.
name|getStartRow
argument_list|()
argument_list|)
expr_stmt|;
name|sc
operator|.
name|setStopRow
argument_list|(
name|tSplit
operator|.
name|getEndRow
argument_list|()
argument_list|)
expr_stmt|;
name|recordReader
operator|.
name|setScan
argument_list|(
name|sc
argument_list|)
expr_stmt|;
name|recordReader
operator|.
name|setHTable
argument_list|(
operator|new
name|HTable
argument_list|(
name|job
argument_list|,
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|recordReader
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|recordReader
return|;
block|}
comment|/*    * @param jobContext    *    * @return List of InputSplit    *    * @throws IOException    *    * @throws InterruptedException    *    * @see    * org.apache.hadoop.mapreduce.InputFormat#getSplits(org.apache.hadoop.mapreduce    * .JobContext)    */
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
name|InputSplit
index|[]
name|getSplits
parameter_list|(
name|JobConf
name|job
parameter_list|,
name|int
name|numSplits
parameter_list|)
throws|throws
name|IOException
block|{
name|inputFormat
operator|.
name|setConf
argument_list|(
name|job
argument_list|)
expr_stmt|;
return|return
name|convertSplits
argument_list|(
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|HCatMapRedUtil
operator|.
name|createJobContext
argument_list|(
name|job
argument_list|,
literal|null
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|InputSplit
index|[]
name|convertSplits
parameter_list|(
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|InputSplit
argument_list|>
name|splits
parameter_list|)
block|{
name|InputSplit
index|[]
name|converted
init|=
operator|new
name|InputSplit
index|[
name|splits
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
name|splits
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
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
name|TableSplit
name|tableSplit
init|=
operator|(
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
name|TableSplit
operator|)
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|TableSplit
name|newTableSplit
init|=
operator|new
name|TableSplit
argument_list|(
name|tableSplit
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableSplit
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|tableSplit
operator|.
name|getEndRow
argument_list|()
argument_list|,
name|tableSplit
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
decl_stmt|;
name|converted
index|[
name|i
index|]
operator|=
name|newTableSplit
expr_stmt|;
block|}
return|return
name|converted
return|;
block|}
block|}
end_class

end_unit


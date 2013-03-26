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
name|HashMap
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
name|RecordWriter
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
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
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
name|HCatException
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
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_class
specifier|public
class|class
name|HCatRecordWriter
extends|extends
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
block|{
specifier|private
specifier|final
name|HCatOutputStorageDriver
name|storageDriver
decl_stmt|;
specifier|private
name|boolean
name|dynamicPartitioningUsed
init|=
literal|false
decl_stmt|;
comment|//    static final private Log LOG = LogFactory.getLog(HCatRecordWriter.class);
specifier|private
specifier|final
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|baseWriter
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
argument_list|>
name|baseDynamicWriters
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|HCatOutputStorageDriver
argument_list|>
name|baseDynamicStorageDrivers
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|partColsToDel
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|dynamicPartCols
decl_stmt|;
specifier|private
name|int
name|maxDynamicPartitions
decl_stmt|;
specifier|private
name|OutputJobInfo
name|jobInfo
decl_stmt|;
specifier|private
name|TaskAttemptContext
name|context
decl_stmt|;
specifier|public
name|HCatRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|jobInfo
operator|=
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
comment|// If partition columns occur in data, we want to remove them.
name|partColsToDel
operator|=
name|jobInfo
operator|.
name|getPosOfPartCols
argument_list|()
expr_stmt|;
name|dynamicPartitioningUsed
operator|=
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|isDynamicPartitioningUsed
argument_list|()
expr_stmt|;
name|dynamicPartCols
operator|=
name|jobInfo
operator|.
name|getPosOfDynPartCols
argument_list|()
expr_stmt|;
name|maxDynamicPartitions
operator|=
name|jobInfo
operator|.
name|getMaxDynamicPartitions
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|partColsToDel
operator|==
literal|null
operator|)
operator|||
operator|(
name|dynamicPartitioningUsed
operator|&&
operator|(
name|dynamicPartCols
operator|==
literal|null
operator|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"It seems that setSchema() is not called on "
operator|+
literal|"HCatOutputFormat. Please make sure that method is called."
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|dynamicPartitioningUsed
condition|)
block|{
name|this
operator|.
name|storageDriver
operator|=
name|HCatOutputFormat
operator|.
name|getOutputDriverInstance
argument_list|(
name|context
argument_list|,
name|jobInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|baseWriter
operator|=
name|storageDriver
operator|.
name|getOutputFormat
argument_list|()
operator|.
name|getRecordWriter
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|baseDynamicStorageDrivers
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|baseDynamicWriters
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|baseDynamicStorageDrivers
operator|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|HCatOutputStorageDriver
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|baseDynamicWriters
operator|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|storageDriver
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|baseWriter
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * @return the storageDriver      */
specifier|public
name|HCatOutputStorageDriver
name|getStorageDriver
parameter_list|()
block|{
return|return
name|storageDriver
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|dynamicPartitioningUsed
condition|)
block|{
for|for
control|(
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|bwriter
range|:
name|baseDynamicWriters
operator|.
name|values
argument_list|()
control|)
block|{
name|bwriter
operator|.
name|close
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HCatOutputStorageDriver
name|osd
range|:
name|baseDynamicStorageDrivers
operator|.
name|values
argument_list|()
control|)
block|{
name|OutputCommitter
name|baseOutputCommitter
init|=
name|osd
operator|.
name|getOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|baseOutputCommitter
operator|.
name|needsTaskCommit
argument_list|(
name|context
argument_list|)
condition|)
block|{
name|baseOutputCommitter
operator|.
name|commitTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|baseWriter
operator|.
name|close
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|WritableComparable
argument_list|<
name|?
argument_list|>
name|key
parameter_list|,
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|localWriter
decl_stmt|;
name|HCatOutputStorageDriver
name|localDriver
decl_stmt|;
comment|//      HCatUtil.logList(LOG, "HCatRecord to write", value.getAll());
if|if
condition|(
name|dynamicPartitioningUsed
condition|)
block|{
comment|// calculate which writer to use from the remaining values - this needs to be done before we delete cols
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|colToAppend
range|:
name|dynamicPartCols
control|)
block|{
name|dynamicPartValues
operator|.
name|add
argument_list|(
name|value
operator|.
name|get
argument_list|(
name|colToAppend
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|dynHashCode
init|=
name|dynamicPartValues
operator|.
name|hashCode
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|baseDynamicWriters
operator|.
name|containsKey
argument_list|(
name|dynHashCode
argument_list|)
condition|)
block|{
comment|//          LOG.info("Creating new storage driver["+baseDynamicStorageDrivers.size()
comment|//              +"/"+maxDynamicPartitions+ "] for "+dynamicPartValues.toString());
if|if
condition|(
operator|(
name|maxDynamicPartitions
operator|!=
operator|-
literal|1
operator|)
operator|&&
operator|(
name|baseDynamicStorageDrivers
operator|.
name|size
argument_list|()
operator|>
name|maxDynamicPartitions
operator|)
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_TOO_MANY_DYNAMIC_PTNS
argument_list|,
literal|"Number of dynamic partitions being created "
operator|+
literal|"exceeds configured max allowable partitions["
operator|+
name|maxDynamicPartitions
operator|+
literal|"], increase parameter ["
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONMAXPARTS
operator|.
name|varname
operator|+
literal|"] if needed."
argument_list|)
throw|;
block|}
comment|//          HCatUtil.logList(LOG, "dynamicpartvals", dynamicPartValues);
comment|//          HCatUtil.logList(LOG, "dynamicpartCols", dynamicPartCols);
name|HCatOutputStorageDriver
name|localOsd
init|=
name|createDynamicStorageDriver
argument_list|(
name|dynamicPartValues
argument_list|)
decl_stmt|;
name|RecordWriter
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|baseRecordWriter
init|=
name|localOsd
operator|.
name|getOutputFormat
argument_list|()
operator|.
name|getRecordWriter
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|localOsd
operator|.
name|setupOutputCommitterJob
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|OutputCommitter
name|baseOutputCommitter
init|=
name|localOsd
operator|.
name|getOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|baseOutputCommitter
operator|.
name|setupTask
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|prepareForStorageDriverOutput
argument_list|(
name|localOsd
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|baseDynamicWriters
operator|.
name|put
argument_list|(
name|dynHashCode
argument_list|,
name|baseRecordWriter
argument_list|)
expr_stmt|;
name|baseDynamicStorageDrivers
operator|.
name|put
argument_list|(
name|dynHashCode
argument_list|,
name|localOsd
argument_list|)
expr_stmt|;
block|}
name|localWriter
operator|=
name|baseDynamicWriters
operator|.
name|get
argument_list|(
name|dynHashCode
argument_list|)
expr_stmt|;
name|localDriver
operator|=
name|baseDynamicStorageDrivers
operator|.
name|get
argument_list|(
name|dynHashCode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|localWriter
operator|=
name|baseWriter
expr_stmt|;
name|localDriver
operator|=
name|storageDriver
expr_stmt|;
block|}
for|for
control|(
name|Integer
name|colToDel
range|:
name|partColsToDel
control|)
block|{
name|value
operator|.
name|remove
argument_list|(
name|colToDel
argument_list|)
expr_stmt|;
block|}
comment|//The key given by user is ignored
name|WritableComparable
argument_list|<
name|?
argument_list|>
name|generatedKey
init|=
name|localDriver
operator|.
name|generateKey
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|Writable
name|convertedValue
init|=
name|localDriver
operator|.
name|convertValue
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|localWriter
operator|.
name|write
argument_list|(
name|generatedKey
argument_list|,
name|convertedValue
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HCatOutputStorageDriver
name|createDynamicStorageDriver
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartVals
parameter_list|)
throws|throws
name|IOException
block|{
name|HCatOutputStorageDriver
name|localOsd
init|=
name|HCatOutputFormat
operator|.
name|getOutputDriverInstance
argument_list|(
name|context
argument_list|,
name|jobInfo
argument_list|,
name|dynamicPartVals
argument_list|)
decl_stmt|;
return|return
name|localOsd
return|;
block|}
specifier|public
name|void
name|prepareForStorageDriverOutput
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Set permissions and group on freshly created files.
if|if
condition|(
operator|!
name|dynamicPartitioningUsed
condition|)
block|{
name|HCatOutputStorageDriver
name|localOsd
init|=
name|this
operator|.
name|getStorageDriver
argument_list|()
decl_stmt|;
name|prepareForStorageDriverOutput
argument_list|(
name|localOsd
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|prepareForStorageDriverOutput
parameter_list|(
name|HCatOutputStorageDriver
name|localOsd
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|HCatOutputFormat
operator|.
name|prepareOutputLocation
argument_list|(
name|localOsd
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


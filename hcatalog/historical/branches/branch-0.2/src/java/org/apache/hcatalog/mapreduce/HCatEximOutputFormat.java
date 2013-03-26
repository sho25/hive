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
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|hive
operator|.
name|metastore
operator|.
name|Warehouse
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
name|SerDeInfo
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
name|StorageDescriptor
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
name|RCFileInputFormat
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
name|RCFileOutputFormat
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
name|columnar
operator|.
name|ColumnarSerDe
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
name|OutputFormat
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
name|schema
operator|.
name|HCatFieldSchema
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
name|schema
operator|.
name|HCatSchema
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
name|schema
operator|.
name|HCatSchemaUtils
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
name|rcfile
operator|.
name|RCFileInputDriver
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
name|rcfile
operator|.
name|RCFileOutputDriver
import|;
end_import

begin_comment
comment|/**  * The OutputFormat to use to write data to HCat without a hcat server. This can then  * be imported into a hcat instance, or used with a HCatEximInputFormat. As in  * HCatOutputFormat, the key value is ignored and  * and should be given as null. The value is the HCatRecord to write.  */
end_comment

begin_class
specifier|public
class|class
name|HCatEximOutputFormat
extends|extends
name|HCatBaseOutputFormat
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HCatEximOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Get the record writer for the job. Uses the Table's default OutputStorageDriver    * to get the record writer.    *    * @param context    *          the information about the current task.    * @return a RecordWriter to write the output for the job.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HCatRecordWriter
name|rw
init|=
operator|new
name|HCatRecordWriter
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
name|rw
return|;
block|}
comment|/**    * Get the output committer for this output format. This is responsible    * for ensuring the output is committed correctly.    * @param context the task context    * @return an output committer    * @throws IOException    * @throws InterruptedException    */
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
name|OutputFormat
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
name|outputFormat
init|=
name|getOutputFormat
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatEximOutputCommitter
argument_list|(
name|context
argument_list|,
name|outputFormat
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|setOutput
parameter_list|(
name|Job
name|job
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|tablename
parameter_list|,
name|String
name|location
parameter_list|,
name|HCatSchema
name|partitionSchema
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|HCatSchema
name|columnSchema
parameter_list|)
throws|throws
name|HCatException
block|{
name|setOutput
argument_list|(
name|job
argument_list|,
name|dbname
argument_list|,
name|tablename
argument_list|,
name|location
argument_list|,
name|partitionSchema
argument_list|,
name|partitionValues
argument_list|,
name|columnSchema
argument_list|,
name|RCFileInputDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|RCFileOutputDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|RCFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|ColumnarSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|void
name|setOutput
parameter_list|(
name|Job
name|job
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|tablename
parameter_list|,
name|String
name|location
parameter_list|,
name|HCatSchema
name|partitionSchema
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|HCatSchema
name|columnSchema
parameter_list|,
name|String
name|isdname
parameter_list|,
name|String
name|osdname
parameter_list|,
name|String
name|ifname
parameter_list|,
name|String
name|ofname
parameter_list|,
name|String
name|serializationLib
parameter_list|)
throws|throws
name|HCatException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|partKeys
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|partitionSchema
operator|!=
literal|null
condition|)
block|{
name|partKeys
operator|=
name|partitionSchema
operator|.
name|getFields
argument_list|()
expr_stmt|;
if|if
condition|(
name|partKeys
operator|.
name|size
argument_list|()
operator|!=
name|partitionValues
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Partition key size differs from partition value size"
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partKeys
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|HCatFieldSchema
name|partKey
init|=
name|partKeys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|partKey
operator|.
name|getType
argument_list|()
operator|!=
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Partition key type string is only supported"
argument_list|)
throw|;
block|}
name|partSpec
operator|.
name|put
argument_list|(
name|partKey
operator|.
name|getName
argument_list|()
argument_list|,
name|partitionValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|StorerInfo
name|storerInfo
init|=
operator|new
name|StorerInfo
argument_list|(
name|isdname
argument_list|,
name|osdname
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
decl_stmt|;
name|HCatTableInfo
name|outputInfo
init|=
name|HCatTableInfo
operator|.
name|getOutputTableInfo
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|dbname
argument_list|,
name|tablename
argument_list|,
name|partSpec
argument_list|)
decl_stmt|;
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
name|metadata
operator|.
name|Table
name|tbl
init|=
operator|new
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
name|metadata
operator|.
name|Table
argument_list|(
name|dbname
argument_list|,
name|tablename
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|tbl
operator|.
name|getTTable
argument_list|()
decl_stmt|;
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ISD_CLASS
argument_list|,
name|isdname
argument_list|)
expr_stmt|;
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_OSD_CLASS
argument_list|,
name|osdname
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|partname
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|(
name|partKeys
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|partKeys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partSchema
init|=
name|HCatSchemaUtils
operator|.
name|getFieldSchemas
argument_list|(
name|partKeys
argument_list|)
decl_stmt|;
name|table
operator|.
name|setPartitionKeys
argument_list|(
name|partSchema
argument_list|)
expr_stmt|;
name|partname
operator|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|partSchema
argument_list|,
name|partitionValues
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|partname
operator|=
literal|"data"
expr_stmt|;
block|}
name|StorageDescriptor
name|sd
init|=
name|table
operator|.
name|getSd
argument_list|()
decl_stmt|;
name|sd
operator|.
name|setLocation
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|String
name|dataLocation
init|=
name|location
operator|+
literal|"/"
operator|+
name|partname
decl_stmt|;
name|OutputJobInfo
name|jobInfo
init|=
operator|new
name|OutputJobInfo
argument_list|(
name|outputInfo
argument_list|,
name|columnSchema
argument_list|,
name|columnSchema
argument_list|,
name|storerInfo
argument_list|,
name|dataLocation
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|setPartDetails
argument_list|(
name|jobInfo
argument_list|,
name|columnSchema
argument_list|,
name|partSpec
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|HCatUtil
operator|.
name|getFieldSchemaList
argument_list|(
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
operator|.
name|getFields
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|ifname
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|ofname
argument_list|)
expr_stmt|;
name|SerDeInfo
name|serdeInfo
init|=
name|sd
operator|.
name|getSerdeInfo
argument_list|()
decl_stmt|;
name|serdeInfo
operator|.
name|setSerializationLib
argument_list|(
name|serializationLib
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_OUTPUT_INFO
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|jobInfo
argument_list|)
argument_list|)
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
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_SET_OUTPUT
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_SET_OUTPUT
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


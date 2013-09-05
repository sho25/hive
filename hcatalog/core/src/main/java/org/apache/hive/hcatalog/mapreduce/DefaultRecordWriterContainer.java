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
name|IOException
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
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_comment
comment|/**  * Part of the DefaultOutput*Container classes  * See {@link DefaultOutputFormatContainer} for more information  */
end_comment

begin_class
class|class
name|DefaultRecordWriterContainer
extends|extends
name|RecordWriterContainer
block|{
specifier|private
specifier|final
name|HCatStorageHandler
name|storageHandler
decl_stmt|;
specifier|private
specifier|final
name|SerDe
name|serDe
decl_stmt|;
specifier|private
specifier|final
name|OutputJobInfo
name|jobInfo
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|hcatRecordOI
decl_stmt|;
comment|/**      * @param context current JobContext      * @param baseRecordWriter RecordWriter to contain      * @throws IOException      * @throws InterruptedException      */
specifier|public
name|DefaultRecordWriterContainer
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|,
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
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|context
argument_list|,
name|baseRecordWriter
argument_list|)
expr_stmt|;
name|jobInfo
operator|=
name|HCatOutputFormat
operator|.
name|getJobInfo
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|storageHandler
operator|=
name|HCatUtil
operator|.
name|getStorageHandler
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|jobInfo
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getStorerInfo
argument_list|()
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|configureOutputStorageHandler
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|serDe
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|storageHandler
operator|.
name|getSerDeClass
argument_list|()
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|hcatRecordOI
operator|=
name|InternalUtil
operator|.
name|createStructObjectInspector
argument_list|(
name|jobInfo
operator|.
name|getOutputSchema
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|InternalUtil
operator|.
name|initializeOutputSerDe
argument_list|(
name|serDe
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|jobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize SerDe"
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
name|getBaseRecordWriter
argument_list|()
operator|.
name|close
argument_list|(
name|InternalUtil
operator|.
name|createReporter
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
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
try|try
block|{
name|getBaseRecordWriter
argument_list|()
operator|.
name|write
argument_list|(
literal|null
argument_list|,
name|serDe
operator|.
name|serialize
argument_list|(
name|value
operator|.
name|getAll
argument_list|()
argument_list|,
name|hcatRecordOI
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to serialize object"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


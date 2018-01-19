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
name|ql
operator|.
name|io
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
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|BytesRefArrayWritable
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
name|io
operator|.
name|compress
operator|.
name|CompressionCodec
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
name|compress
operator|.
name|DefaultCodec
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
name|FileOutputFormat
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * RCFileOutputFormat.  *  */
end_comment

begin_class
specifier|public
class|class
name|RCFileOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
implements|implements
name|HiveOutputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
block|{
comment|/**    * set number of columns into the given configuration.    *    * @param conf    *          configuration instance which need to set the column number    * @param columnNum    *          column number for RCFile's Writer    *    */
specifier|public
specifier|static
name|void
name|setColumnNumber
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|columnNum
parameter_list|)
block|{
assert|assert
name|columnNum
operator|>
literal|0
assert|;
name|conf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_RCFILE_COLUMN_NUMBER_CONF
operator|.
name|varname
argument_list|,
name|columnNum
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the number of columns set in the conf for writers.    *    * @param conf    * @return number of columns for RCFile's writer    */
specifier|public
specifier|static
name|int
name|getColumnNumber
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_RCFILE_COLUMN_NUMBER_CONF
argument_list|)
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
name|getRecordWriter
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|String
name|name
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|outputPath
init|=
name|getWorkOutputPath
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|outputPath
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|outputPath
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|CompressionCodec
name|codec
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|getCompressOutput
argument_list|(
name|job
argument_list|)
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|codecClass
init|=
name|getOutputCompressorClass
argument_list|(
name|job
argument_list|,
name|DefaultCodec
operator|.
name|class
argument_list|)
decl_stmt|;
name|codec
operator|=
operator|(
name|CompressionCodec
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|codecClass
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
specifier|final
name|RCFile
operator|.
name|Writer
name|out
init|=
operator|new
name|RCFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|job
argument_list|,
name|file
argument_list|,
name|progress
argument_list|,
name|codec
argument_list|)
decl_stmt|;
return|return
operator|new
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
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
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|WritableComparable
name|key
parameter_list|,
name|BytesRefArrayWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/**    * create the final out file.    *    * @param jc    *          the job configuration file    * @param finalOutPath    *          the final output file to be created    * @param valueClass    *          the value class used for create    * @param isCompressed    *          whether the content is compressed or not    * @param tableProperties    *          the tableInfo of this file's corresponding table    * @param progress    *          progress used for status report    * @throws IOException    */
annotation|@
name|Override
specifier|public
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
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jc
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
name|String
index|[]
name|cols
init|=
literal|null
decl_stmt|;
name|String
name|columns
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
literal|"columns"
argument_list|)
decl_stmt|;
if|if
condition|(
name|columns
operator|==
literal|null
operator|||
name|columns
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|cols
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|cols
operator|=
name|StringUtils
operator|.
name|split
argument_list|(
name|columns
argument_list|,
literal|","
argument_list|)
expr_stmt|;
block|}
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|jc
argument_list|,
name|cols
operator|.
name|length
argument_list|)
expr_stmt|;
specifier|final
name|RCFile
operator|.
name|Writer
name|outWriter
init|=
name|Utilities
operator|.
name|createRCFileWriter
argument_list|(
name|jc
argument_list|,
name|finalOutPath
operator|.
name|getFileSystem
argument_list|(
name|jc
argument_list|)
argument_list|,
name|finalOutPath
argument_list|,
name|isCompressed
argument_list|,
name|progress
argument_list|)
decl_stmt|;
return|return
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
name|exec
operator|.
name|FileSinkOperator
operator|.
name|RecordWriter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Writable
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|outWriter
operator|.
name|append
argument_list|(
name|r
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
name|outWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit


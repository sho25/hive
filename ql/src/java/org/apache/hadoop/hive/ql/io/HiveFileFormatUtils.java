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
name|io
operator|.
name|Serializable
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
name|Map
operator|.
name|Entry
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
name|fs
operator|.
name|FileStatus
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
name|FileSinkOperator
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Operator
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
name|ql
operator|.
name|io
operator|.
name|HivePassThroughOutputFormat
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
name|metadata
operator|.
name|HiveException
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
name|plan
operator|.
name|FileSinkDesc
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
name|plan
operator|.
name|OperatorDesc
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
name|plan
operator|.
name|PartitionDesc
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
name|plan
operator|.
name|TableDesc
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
name|SequenceFile
operator|.
name|CompressionType
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
name|mapred
operator|.
name|SequenceFileInputFormat
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
name|SequenceFileOutputFormat
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
name|TextInputFormat
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
name|Shell
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
comment|/**  * An util class for various Hive file format tasks.  * registerOutputFormatSubstitute(Class, Class) getOutputFormatSubstitute(Class)  * are added for backward compatibility. They return the newly added  * HiveOutputFormat for the older ones.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HiveFileFormatUtils
block|{
static|static
block|{
name|outputFormatSubstituteMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerOutputFormatSubstitute
argument_list|(
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
name|HiveIgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerOutputFormatSubstitute
argument_list|(
name|SequenceFileOutputFormat
operator|.
name|class
argument_list|,
name|HiveSequenceFileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|static
name|String
name|realoutputFormat
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
argument_list|>
name|outputFormatSubstituteMap
decl_stmt|;
comment|/**    * register a substitute.    *    * @param origin    *          the class that need to be substituted    * @param substitute    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
specifier|synchronized
name|void
name|registerOutputFormatSubstitute
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|origin
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|substitute
parameter_list|)
block|{
name|outputFormatSubstituteMap
operator|.
name|put
argument_list|(
name|origin
argument_list|,
name|substitute
argument_list|)
expr_stmt|;
block|}
comment|/**    * get a OutputFormat's substitute HiveOutputFormat.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
specifier|synchronized
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|getOutputFormatSubstitute
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|origin
parameter_list|,
name|boolean
name|storagehandlerflag
parameter_list|)
block|{
if|if
condition|(
name|HiveOutputFormat
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|origin
argument_list|)
condition|)
block|{
return|return
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
operator|)
name|origin
return|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|HiveOutputFormat
argument_list|>
name|result
init|=
name|outputFormatSubstituteMap
operator|.
name|get
argument_list|(
name|origin
argument_list|)
decl_stmt|;
comment|//register this output format into the map for the first time
if|if
condition|(
operator|(
name|storagehandlerflag
operator|==
literal|true
operator|)
operator|&&
operator|(
name|result
operator|==
literal|null
operator|)
condition|)
block|{
name|HiveFileFormatUtils
operator|.
name|setRealOutputFormatClassName
argument_list|(
name|origin
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|=
name|HivePassThroughOutputFormat
operator|.
name|class
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerOutputFormatSubstitute
argument_list|(
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
operator|)
name|origin
argument_list|,
name|HivePassThroughOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * get a RealOutputFormatClassName corresponding to the HivePassThroughOutputFormat    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|String
name|getRealOutputFormatClassName
parameter_list|()
block|{
return|return
name|realoutputFormat
return|;
block|}
comment|/**    * set a RealOutputFormatClassName corresponding to the HivePassThroughOutputFormat    */
specifier|public
specifier|static
name|void
name|setRealOutputFormatClassName
parameter_list|(
name|String
name|destination
parameter_list|)
block|{
if|if
condition|(
name|destination
operator|!=
literal|null
condition|)
block|{
name|realoutputFormat
operator|=
name|destination
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
block|}
comment|/**    * get the final output path of a given FileOutputFormat.    *    * @param parent    *          parent dir of the expected final output path    * @param jc    *          job configuration    * @deprecated    */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|Path
name|getOutputFormatFinalPath
parameter_list|(
name|Path
name|parent
parameter_list|,
name|String
name|taskId
parameter_list|,
name|JobConf
name|jc
parameter_list|,
name|HiveOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hiveOutputFormat
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Path
name|defaultFinalPath
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|hiveOutputFormat
operator|instanceof
name|HiveIgnoreKeyTextOutputFormat
condition|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|parent
argument_list|,
name|taskId
operator|+
name|Utilities
operator|.
name|getFileExtension
argument_list|(
name|jc
argument_list|,
name|isCompressed
argument_list|)
argument_list|)
return|;
block|}
return|return
name|defaultFinalPath
return|;
block|}
static|static
block|{
name|inputFormatCheckerMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerInputFormatChecker
argument_list|(
name|SequenceFileInputFormat
operator|.
name|class
argument_list|,
name|SequenceFileInputFormatChecker
operator|.
name|class
argument_list|)
expr_stmt|;
name|HiveFileFormatUtils
operator|.
name|registerInputFormatChecker
argument_list|(
name|RCFileInputFormat
operator|.
name|class
argument_list|,
name|RCFileInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|inputFormatCheckerInstanceCache
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|,
name|InputFormatChecker
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|>
name|inputFormatCheckerMap
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
argument_list|,
name|InputFormatChecker
argument_list|>
name|inputFormatCheckerInstanceCache
decl_stmt|;
comment|/**    * register an InputFormatChecker for a given InputFormat.    *    * @param format    *          the class that need to be substituted    * @param checker    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
specifier|synchronized
name|void
name|registerInputFormatChecker
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|format
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|checker
parameter_list|)
block|{
name|inputFormatCheckerMap
operator|.
name|put
argument_list|(
name|format
argument_list|,
name|checker
argument_list|)
expr_stmt|;
block|}
comment|/**    * get an InputFormatChecker for a file format.    */
specifier|public
specifier|static
specifier|synchronized
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|getInputFormatChecker
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|inputFormat
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|result
init|=
name|inputFormatCheckerMap
operator|.
name|get
argument_list|(
name|inputFormat
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * checks if files are in same format as the given input format.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
specifier|static
name|boolean
name|checkInputFormat
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormatCls
parameter_list|,
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|InputFormatChecker
argument_list|>
name|checkerCls
init|=
name|getInputFormatChecker
argument_list|(
name|inputFormatCls
argument_list|)
decl_stmt|;
if|if
condition|(
name|checkerCls
operator|==
literal|null
operator|&&
name|inputFormatCls
operator|.
name|isAssignableFrom
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
condition|)
block|{
comment|// we get a text input format here, we can not determine a file is text
comment|// according to its content, so we can do is to test if other file
comment|// format can accept it. If one other file format can accept this file,
comment|// we treat this file as text file, although it maybe not.
return|return
name|checkTextInputFormat
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|files
argument_list|)
return|;
block|}
if|if
condition|(
name|checkerCls
operator|!=
literal|null
condition|)
block|{
name|InputFormatChecker
name|checkerInstance
init|=
name|inputFormatCheckerInstanceCache
operator|.
name|get
argument_list|(
name|checkerCls
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|checkerInstance
operator|==
literal|null
condition|)
block|{
name|checkerInstance
operator|=
name|checkerCls
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|inputFormatCheckerInstanceCache
operator|.
name|put
argument_list|(
name|checkerCls
argument_list|,
name|checkerInstance
argument_list|)
expr_stmt|;
block|}
return|return
name|checkerInstance
operator|.
name|validateInput
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|files
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|boolean
name|checkTextInputFormat
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|HiveException
block|{
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
argument_list|>
name|inputFormatter
init|=
name|inputFormatCheckerMap
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|reg
range|:
name|inputFormatter
control|)
block|{
name|boolean
name|result
init|=
name|checkInputFormat
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|reg
argument_list|,
name|files
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|RecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|TableDesc
name|tableInfo
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|outputClass
parameter_list|,
name|FileSinkDesc
name|conf
parameter_list|,
name|Path
name|outPath
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
block|{
name|boolean
name|storagehandlerofhivepassthru
init|=
literal|false
decl_stmt|;
name|HiveOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hiveOutputFormat
decl_stmt|;
try|try
block|{
if|if
condition|(
name|tableInfo
operator|.
name|getJobProperties
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|tableInfo
operator|.
name|getJobProperties
argument_list|()
operator|.
name|get
argument_list|(
name|HivePassThroughOutputFormat
operator|.
name|HIVE_PASSTHROUGH_STORAGEHANDLER_OF_JOBCONFKEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|jc
operator|.
name|set
argument_list|(
name|HivePassThroughOutputFormat
operator|.
name|HIVE_PASSTHROUGH_STORAGEHANDLER_OF_JOBCONFKEY
argument_list|,
name|tableInfo
operator|.
name|getJobProperties
argument_list|()
operator|.
name|get
argument_list|(
name|HivePassThroughOutputFormat
operator|.
name|HIVE_PASSTHROUGH_STORAGEHANDLER_OF_JOBCONFKEY
argument_list|)
argument_list|)
expr_stmt|;
name|storagehandlerofhivepassthru
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|storagehandlerofhivepassthru
condition|)
block|{
name|hiveOutputFormat
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|tableInfo
operator|.
name|getOutputFileFormatClass
argument_list|()
argument_list|,
name|jc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hiveOutputFormat
operator|=
name|tableInfo
operator|.
name|getOutputFileFormatClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
name|boolean
name|isCompressed
init|=
name|conf
operator|.
name|getCompressed
argument_list|()
decl_stmt|;
name|JobConf
name|jc_output
init|=
name|jc
decl_stmt|;
if|if
condition|(
name|isCompressed
condition|)
block|{
name|jc_output
operator|=
operator|new
name|JobConf
argument_list|(
name|jc
argument_list|)
expr_stmt|;
name|String
name|codecStr
init|=
name|conf
operator|.
name|getCompressCodec
argument_list|()
decl_stmt|;
if|if
condition|(
name|codecStr
operator|!=
literal|null
operator|&&
operator|!
name|codecStr
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
name|Class
argument_list|<
name|?
extends|extends
name|CompressionCodec
argument_list|>
name|codec
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|CompressionCodec
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|codecStr
argument_list|)
decl_stmt|;
name|FileOutputFormat
operator|.
name|setOutputCompressorClass
argument_list|(
name|jc_output
argument_list|,
name|codec
argument_list|)
expr_stmt|;
block|}
name|String
name|type
init|=
name|conf
operator|.
name|getCompressType
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
operator|&&
operator|!
name|type
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
name|CompressionType
name|style
init|=
name|CompressionType
operator|.
name|valueOf
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|SequenceFileOutputFormat
operator|.
name|setOutputCompressionType
argument_list|(
name|jc
argument_list|,
name|style
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|getRecordWriter
argument_list|(
name|jc_output
argument_list|,
name|hiveOutputFormat
argument_list|,
name|outputClass
argument_list|,
name|isCompressed
argument_list|,
name|tableInfo
operator|.
name|getProperties
argument_list|()
argument_list|,
name|outPath
argument_list|,
name|reporter
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|RecordWriter
name|getRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|HiveOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hiveOutputFormat
parameter_list|,
specifier|final
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
name|tableProp
parameter_list|,
name|Path
name|outPath
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
name|hiveOutputFormat
operator|!=
literal|null
condition|)
block|{
return|return
name|hiveOutputFormat
operator|.
name|getHiveRecordWriter
argument_list|(
name|jc
argument_list|,
name|outPath
argument_list|,
name|valueClass
argument_list|,
name|isCompressed
argument_list|,
name|tableProp
argument_list|,
name|reporter
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|PartitionDesc
name|getPartitionDescFromPathRecursively
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
name|Path
name|dir
parameter_list|,
name|Map
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|>
name|cacheMap
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getPartitionDescFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|dir
argument_list|,
name|cacheMap
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|PartitionDesc
name|getPartitionDescFromPathRecursively
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
name|Path
name|dir
parameter_list|,
name|Map
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|>
name|cacheMap
parameter_list|,
name|boolean
name|ignoreSchema
parameter_list|)
throws|throws
name|IOException
block|{
name|PartitionDesc
name|part
init|=
name|doGetPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|part
operator|==
literal|null
operator|&&
operator|(
name|ignoreSchema
operator|||
operator|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
operator|||
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|trim
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
operator|)
operator|||
name|pathsContainNoScheme
argument_list|(
name|pathToPartitionInfo
argument_list|)
operator|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|newPathToPartitionInfo
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|cacheMap
operator|!=
literal|null
condition|)
block|{
name|newPathToPartitionInfo
operator|=
name|cacheMap
operator|.
name|get
argument_list|(
name|pathToPartitionInfo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|newPathToPartitionInfo
operator|==
literal|null
condition|)
block|{
comment|// still null
name|newPathToPartitionInfo
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
argument_list|()
expr_stmt|;
name|populateNewPartitionDesc
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|newPathToPartitionInfo
argument_list|)
expr_stmt|;
if|if
condition|(
name|cacheMap
operator|!=
literal|null
condition|)
block|{
name|cacheMap
operator|.
name|put
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|newPathToPartitionInfo
argument_list|)
expr_stmt|;
block|}
block|}
name|part
operator|=
name|doGetPartitionDescFromPath
argument_list|(
name|newPathToPartitionInfo
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
return|return
name|part
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"cannot find dir = "
operator|+
name|dir
operator|.
name|toString
argument_list|()
operator|+
literal|" in pathToPartitionInfo: "
operator|+
name|pathToPartitionInfo
operator|.
name|keySet
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|pathsContainNoScheme
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pe
range|:
name|pathToPartitionInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|new
name|Path
argument_list|(
name|pe
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|void
name|populateNewPartitionDesc
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|newPathToPartitionInfo
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|entry
range|:
name|pathToPartitionInfo
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|entryKey
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partDesc
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Path
name|newP
init|=
operator|new
name|Path
argument_list|(
name|entryKey
argument_list|)
decl_stmt|;
name|String
name|pathOnly
init|=
name|newP
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|newPathToPartitionInfo
operator|.
name|put
argument_list|(
name|pathOnly
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|PartitionDesc
name|doGetPartitionDescFromPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
parameter_list|,
name|Path
name|dir
parameter_list|)
block|{
comment|// We first do exact match, and then do prefix matching. The latter is due to input dir
comment|// could be /dir/ds='2001-02-21'/part-03 where part-03 is not part of partition
name|String
name|dirPath
init|=
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|PartitionDesc
name|part
init|=
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|dir
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
comment|//      LOG.warn("exact match not found, try ripping input path's theme and authority");
name|part
operator|=
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|dirPath
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
name|Path
name|curPath
init|=
operator|new
name|Path
argument_list|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|dir
operator|=
name|dir
operator|.
name|getParent
argument_list|()
expr_stmt|;
while|while
condition|(
name|dir
operator|!=
literal|null
condition|)
block|{
comment|// first try full match
name|part
operator|=
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|dir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
comment|// exact match not found, try ripping input path's scheme and authority
name|part
operator|=
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|curPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
break|break;
block|}
name|dir
operator|=
name|dir
operator|.
name|getParent
argument_list|()
expr_stmt|;
name|curPath
operator|=
name|curPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|part
return|;
block|}
specifier|private
specifier|static
name|boolean
name|foundAlias
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|pathToAliases
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|aliases
operator|==
literal|null
operator|)
operator|||
operator|(
name|aliases
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|String
name|getMatchingPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|,
name|Path
name|dir
parameter_list|)
block|{
comment|// First find the path to be searched
name|String
name|path
init|=
name|dir
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|foundAlias
argument_list|(
name|pathToAliases
argument_list|,
name|path
argument_list|)
condition|)
block|{
return|return
name|path
return|;
block|}
name|String
name|dirPath
init|=
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|Shell
operator|.
name|WINDOWS
condition|)
block|{
comment|//temp hack
comment|//do this to get rid of "/" before the drive letter in windows
name|dirPath
operator|=
operator|new
name|Path
argument_list|(
name|dirPath
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|foundAlias
argument_list|(
name|pathToAliases
argument_list|,
name|dirPath
argument_list|)
condition|)
block|{
return|return
name|dirPath
return|;
block|}
name|path
operator|=
name|dirPath
expr_stmt|;
name|String
name|dirStr
init|=
name|dir
operator|.
name|toString
argument_list|()
decl_stmt|;
name|int
name|dirPathIndex
init|=
name|dirPath
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|int
name|dirStrIndex
init|=
name|dirStr
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
while|while
condition|(
name|dirPathIndex
operator|>=
literal|0
operator|&&
name|dirStrIndex
operator|>=
literal|0
condition|)
block|{
name|dirStr
operator|=
name|dirStr
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dirStrIndex
argument_list|)
expr_stmt|;
name|dirPath
operator|=
name|dirPath
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|dirPathIndex
argument_list|)
expr_stmt|;
comment|//first try full match
if|if
condition|(
name|foundAlias
argument_list|(
name|pathToAliases
argument_list|,
name|dirStr
argument_list|)
condition|)
block|{
return|return
name|dirStr
return|;
block|}
if|if
condition|(
name|foundAlias
argument_list|(
name|pathToAliases
argument_list|,
name|dirPath
argument_list|)
condition|)
block|{
return|return
name|dirPath
return|;
block|}
name|dirPathIndex
operator|=
name|dirPath
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
name|dirStrIndex
operator|=
name|dirStr
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get the list of operators from the operator tree that are needed for the path    * @param pathToAliases  mapping from path to aliases    * @param aliasToWork    The operator tree to be invoked for a given alias    * @param dir            The path to look for    **/
specifier|public
specifier|static
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|doGetWorksFromPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|,
name|Path
name|dir
parameter_list|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|opList
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|doGetAliasesFromPath
argument_list|(
name|pathToAliases
argument_list|,
name|dir
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|aliases
control|)
block|{
name|opList
operator|.
name|add
argument_list|(
name|aliasToWork
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|opList
return|;
block|}
comment|/**    * Get the list of aliases from the opeerator tree that are needed for the path    * @param pathToAliases  mapping from path to aliases    * @param dir            The path to look for    **/
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|doGetAliasesFromPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
parameter_list|,
name|Path
name|dir
parameter_list|)
block|{
if|if
condition|(
name|pathToAliases
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
block|}
name|String
name|path
init|=
name|getMatchingPath
argument_list|(
name|pathToAliases
argument_list|,
name|dir
argument_list|)
decl_stmt|;
return|return
name|pathToAliases
operator|.
name|get
argument_list|(
name|path
argument_list|)
return|;
block|}
specifier|private
name|HiveFileFormatUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit


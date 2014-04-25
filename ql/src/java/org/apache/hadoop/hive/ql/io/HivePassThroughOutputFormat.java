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
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
comment|/**  *  This pass through class is used to wrap OutputFormat implementations such that new OutputFormats not derived from  *  HiveOutputFormat gets through the checker  */
end_comment

begin_class
specifier|public
class|class
name|HivePassThroughOutputFormat
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Configurable
implements|,
name|HiveOutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
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
name|actualOutputFormat
decl_stmt|;
specifier|private
name|String
name|actualOutputFormatClass
init|=
literal|""
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|boolean
name|initialized
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_PASSTHROUGH_OF_CLASSNAME
init|=
literal|"org.apache.hadoop.hive.ql.io.HivePassThroughOutputFormat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_PASSTHROUGH_STORAGEHANDLER_OF_JOBCONFKEY
init|=
literal|"hive.passthrough.storagehandler.of"
decl_stmt|;
specifier|public
name|HivePassThroughOutputFormat
parameter_list|()
block|{
comment|//construct this class through ReflectionUtils from FileSinkOperator
name|this
operator|.
name|actualOutputFormat
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|initialized
operator|=
literal|false
expr_stmt|;
block|}
specifier|private
name|void
name|createActualOF
parameter_list|()
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|cls
decl_stmt|;
try|try
block|{
name|int
name|e
decl_stmt|;
if|if
condition|(
name|actualOutputFormatClass
operator|!=
literal|null
condition|)
block|{
name|cls
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|actualOutputFormatClass
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Null pointer detected in actualOutputFormatClass"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|actualOF
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|cls
argument_list|,
name|this
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|actualOutputFormat
operator|=
name|actualOF
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|initialized
operator|==
literal|false
condition|)
block|{
name|createActualOF
argument_list|()
expr_stmt|;
name|this
operator|.
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
name|this
operator|.
name|actualOutputFormat
operator|.
name|checkOutputSpecs
argument_list|(
name|ignored
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
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
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|V
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
if|if
condition|(
name|this
operator|.
name|initialized
operator|==
literal|false
condition|)
block|{
name|createActualOF
argument_list|()
expr_stmt|;
name|this
operator|.
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
return|return
operator|(
name|RecordWriter
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
operator|)
name|this
operator|.
name|actualOutputFormat
operator|.
name|getRecordWriter
argument_list|(
name|ignored
argument_list|,
name|job
argument_list|,
name|name
argument_list|,
name|progress
argument_list|)
return|;
block|}
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
if|if
condition|(
name|this
operator|.
name|initialized
operator|==
literal|false
condition|)
block|{
name|createActualOF
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|actualOutputFormat
operator|instanceof
name|HiveOutputFormat
condition|)
block|{
return|return
operator|(
operator|(
name|HiveOutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
operator|)
name|this
operator|.
name|actualOutputFormat
operator|)
operator|.
name|getHiveRecordWriter
argument_list|(
name|jc
argument_list|,
name|finalOutPath
argument_list|,
name|valueClass
argument_list|,
name|isCompressed
argument_list|,
name|tableProperties
argument_list|,
name|progress
argument_list|)
return|;
block|}
else|else
block|{
name|FileSystem
name|fs
init|=
name|finalOutPath
operator|.
name|getFileSystem
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|HivePassThroughRecordWriter
name|hivepassthroughrecordwriter
init|=
operator|new
name|HivePassThroughRecordWriter
argument_list|(
name|this
operator|.
name|actualOutputFormat
operator|.
name|getRecordWriter
argument_list|(
name|fs
argument_list|,
name|jc
argument_list|,
literal|null
argument_list|,
name|progress
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|hivepassthroughrecordwriter
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
if|if
condition|(
name|config
operator|.
name|get
argument_list|(
name|HIVE_PASSTHROUGH_STORAGEHANDLER_OF_JOBCONFKEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|actualOutputFormatClass
operator|=
name|config
operator|.
name|get
argument_list|(
name|HIVE_PASSTHROUGH_STORAGEHANDLER_OF_JOBCONFKEY
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|conf
operator|=
name|config
expr_stmt|;
block|}
block|}
end_class

end_unit


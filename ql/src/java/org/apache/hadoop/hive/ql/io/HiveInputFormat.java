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
name|HashMap
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|io
operator|.
name|HiveIOExceptionHandlerUtil
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
name|TableScanOperator
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
name|log
operator|.
name|PerfLogger
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
name|ExprNodeGenericFuncDesc
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
name|MapWork
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
name|TableScanDesc
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
name|ColumnProjectionUtils
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
name|shims
operator|.
name|ShimLoader
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
name|FileInputFormat
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
name|FileSplit
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
name|JobConfigurable
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
name|hadoop
operator|.
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * HiveInputFormat is a parameterized InputFormat which looks at the path name  * and determine the correct InputFormat for that path name from  * mapredPlan.pathToPartitionInfo(). It can be used to read files with different  * input format in the same map-reduce job.  */
end_comment

begin_class
specifier|public
class|class
name|HiveInputFormat
parameter_list|<
name|K
extends|extends
name|WritableComparable
parameter_list|,
name|V
extends|extends
name|Writable
parameter_list|>
implements|implements
name|InputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
implements|,
name|JobConfigurable
block|{
specifier|public
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
comment|/**    * HiveInputSplit encapsulates an InputSplit with its corresponding    * inputFormatClass. The reason that it derives from FileSplit is to make sure    * "map.input.file" in MapTask.    */
specifier|public
specifier|static
class|class
name|HiveInputSplit
extends|extends
name|FileSplit
implements|implements
name|InputSplit
implements|,
name|Configurable
block|{
name|InputSplit
name|inputSplit
decl_stmt|;
name|String
name|inputFormatClassName
decl_stmt|;
specifier|public
name|HiveInputSplit
parameter_list|()
block|{
comment|// This is the only public constructor of FileSplit
name|super
argument_list|(
operator|(
name|Path
operator|)
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveInputSplit
parameter_list|(
name|InputSplit
name|inputSplit
parameter_list|,
name|String
name|inputFormatClassName
parameter_list|)
block|{
comment|// This is the only public constructor of FileSplit
name|super
argument_list|(
operator|(
name|Path
operator|)
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|inputSplit
operator|=
name|inputSplit
expr_stmt|;
name|this
operator|.
name|inputFormatClassName
operator|=
name|inputFormatClassName
expr_stmt|;
block|}
specifier|public
name|InputSplit
name|getInputSplit
parameter_list|()
block|{
return|return
name|inputSplit
return|;
block|}
specifier|public
name|String
name|inputFormatClassName
parameter_list|()
block|{
return|return
name|inputFormatClassName
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
if|if
condition|(
name|inputSplit
operator|instanceof
name|FileSplit
condition|)
block|{
return|return
operator|(
operator|(
name|FileSplit
operator|)
name|inputSplit
operator|)
operator|.
name|getPath
argument_list|()
return|;
block|}
return|return
operator|new
name|Path
argument_list|(
literal|""
argument_list|)
return|;
block|}
comment|/** The position of the first byte in the file to process. */
annotation|@
name|Override
specifier|public
name|long
name|getStart
parameter_list|()
block|{
if|if
condition|(
name|inputSplit
operator|instanceof
name|FileSplit
condition|)
block|{
return|return
operator|(
operator|(
name|FileSplit
operator|)
name|inputSplit
operator|)
operator|.
name|getStart
argument_list|()
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|inputFormatClassName
operator|+
literal|":"
operator|+
name|inputSplit
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
name|long
name|r
init|=
literal|0
decl_stmt|;
try|try
block|{
name|r
operator|=
name|inputSplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|r
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|inputSplit
operator|.
name|getLocations
argument_list|()
return|;
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
name|String
name|inputSplitClassName
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
try|try
block|{
name|inputSplit
operator|=
operator|(
name|InputSplit
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|conf
operator|.
name|getClassByName
argument_list|(
name|inputSplitClassName
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create an instance of InputSplit class = "
operator|+
name|inputSplitClassName
operator|+
literal|":"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|inputSplit
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|inputFormatClassName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
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
name|writeUTF
argument_list|(
name|inputSplit
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|inputSplit
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|inputFormatClassName
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|conf
decl_stmt|;
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
block|}
name|JobConf
name|job
decl_stmt|;
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|this
operator|.
name|job
operator|=
name|job
expr_stmt|;
block|}
comment|/**    * A cache of InputFormat instances.    */
specifier|protected
specifier|static
name|Map
argument_list|<
name|Class
argument_list|,
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
argument_list|>
name|inputFormats
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|Class
argument_list|,
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|getInputFormatFromCache
parameter_list|(
name|Class
name|inputFormatClass
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|inputFormats
operator|.
name|containsKey
argument_list|(
name|inputFormatClass
argument_list|)
condition|)
block|{
try|try
block|{
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|newInstance
init|=
operator|(
name|InputFormat
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|inputFormatClass
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|inputFormats
operator|.
name|put
argument_list|(
name|inputFormatClass
argument_list|,
name|newInstance
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cannot create an instance of InputFormat class "
operator|+
name|inputFormatClass
operator|.
name|getName
argument_list|()
operator|+
literal|" as specified in mapredWork!"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|inputFormats
operator|.
name|get
argument_list|(
name|inputFormatClass
argument_list|)
return|;
block|}
specifier|public
name|RecordReader
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
name|HiveInputSplit
name|hsplit
init|=
operator|(
name|HiveInputSplit
operator|)
name|split
decl_stmt|;
name|InputSplit
name|inputSplit
init|=
name|hsplit
operator|.
name|getInputSplit
argument_list|()
decl_stmt|;
name|String
name|inputFormatClassName
init|=
literal|null
decl_stmt|;
name|Class
name|inputFormatClass
init|=
literal|null
decl_stmt|;
try|try
block|{
name|inputFormatClassName
operator|=
name|hsplit
operator|.
name|inputFormatClassName
argument_list|()
expr_stmt|;
name|inputFormatClass
operator|=
name|job
operator|.
name|getClassByName
argument_list|(
name|inputFormatClassName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"cannot find class "
operator|+
name|inputFormatClassName
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|mrwork
operator|==
literal|null
condition|)
block|{
name|init
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
name|boolean
name|nonNative
init|=
literal|false
decl_stmt|;
name|PartitionDesc
name|part
init|=
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|hsplit
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|part
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|part
operator|.
name|getTableDesc
argument_list|()
operator|!=
literal|null
operator|)
condition|)
block|{
name|Utilities
operator|.
name|copyTableJobPropertiesToConf
argument_list|(
name|part
operator|.
name|getTableDesc
argument_list|()
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|nonNative
operator|=
name|part
operator|.
name|getTableDesc
argument_list|()
operator|.
name|isNonNative
argument_list|()
expr_stmt|;
block|}
name|pushProjectionsAndFilters
argument_list|(
name|job
argument_list|,
name|inputFormatClass
argument_list|,
name|hsplit
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|hsplit
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|nonNative
argument_list|)
expr_stmt|;
name|InputFormat
name|inputFormat
init|=
name|getInputFormatFromCache
argument_list|(
name|inputFormatClass
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|RecordReader
name|innerReader
init|=
literal|null
decl_stmt|;
try|try
block|{
name|innerReader
operator|=
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|inputSplit
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|innerReader
operator|=
name|HiveIOExceptionHandlerUtil
operator|.
name|handleRecordReaderCreationException
argument_list|(
name|e
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
name|HiveRecordReader
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|rr
init|=
operator|new
name|HiveRecordReader
argument_list|(
name|innerReader
argument_list|,
name|job
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initIOContext
argument_list|(
name|hsplit
argument_list|,
name|job
argument_list|,
name|inputFormatClass
argument_list|,
name|innerReader
argument_list|)
expr_stmt|;
return|return
name|rr
return|;
block|}
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
decl_stmt|;
name|MapWork
name|mrwork
init|=
literal|null
decl_stmt|;
specifier|protected
name|void
name|init
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|mrwork
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|=
name|mrwork
operator|.
name|getPathToPartitionInfo
argument_list|()
expr_stmt|;
block|}
comment|/*    * AddSplitsForGroup collects separate calls to setInputPaths into one where possible.    * The reason for this is that this is faster on some InputFormats. E.g.: Orc will start    * a threadpool to do the work and calling it multiple times unnecessarily will create a lot    * of unnecessary thread pools.    */
specifier|private
name|void
name|addSplitsForGroup
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|dirs
parameter_list|,
name|TableScanOperator
name|tableScan
parameter_list|,
name|JobConf
name|conf
parameter_list|,
name|InputFormat
name|inputFormat
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormatClass
parameter_list|,
name|int
name|splits
parameter_list|,
name|TableDesc
name|table
parameter_list|,
name|List
argument_list|<
name|InputSplit
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|Utilities
operator|.
name|copyTableJobPropertiesToConf
argument_list|(
name|table
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableScan
operator|!=
literal|null
condition|)
block|{
name|pushFilters
argument_list|(
name|conf
argument_list|,
name|tableScan
argument_list|)
expr_stmt|;
block|}
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|conf
argument_list|,
name|dirs
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
name|dirs
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInputFormat
argument_list|(
name|inputFormat
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|headerCount
init|=
literal|0
decl_stmt|;
name|int
name|footerCount
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|headerCount
operator|=
name|Utilities
operator|.
name|getHeaderCount
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|footerCount
operator|=
name|Utilities
operator|.
name|getFooterCount
argument_list|(
name|table
argument_list|,
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|headerCount
operator|!=
literal|0
operator|||
name|footerCount
operator|!=
literal|0
condition|)
block|{
comment|// Input file has header or footer, cannot be splitted.
name|conf
operator|.
name|setLong
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHadoopConfNames
argument_list|()
operator|.
name|get
argument_list|(
literal|"MAPREDMINSPLITSIZE"
argument_list|)
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
block|}
name|InputSplit
index|[]
name|iss
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|conf
argument_list|,
name|splits
argument_list|)
decl_stmt|;
for|for
control|(
name|InputSplit
name|is
range|:
name|iss
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|HiveInputSplit
argument_list|(
name|is
argument_list|,
name|inputFormatClass
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
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
name|PerfLogger
name|perfLogger
init|=
name|PerfLogger
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|GET_SPLITS
argument_list|)
expr_stmt|;
name|init
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|Path
index|[]
name|dirs
init|=
name|FileInputFormat
operator|.
name|getInputPaths
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|dirs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// on tez we're avoiding to duplicate the file info in FileInputFormat.
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
try|try
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|Utilities
operator|.
name|getInputPathsTez
argument_list|(
name|job
argument_list|,
name|mrwork
argument_list|)
decl_stmt|;
name|dirs
operator|=
name|paths
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
name|paths
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not create input files"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"No input paths specified in job"
argument_list|)
throw|;
block|}
block|}
name|JobConf
name|newjob
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|currentDirs
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|currentInputFormatClass
init|=
literal|null
decl_stmt|;
name|TableDesc
name|currentTable
init|=
literal|null
decl_stmt|;
name|TableScanOperator
name|currentTableScan
init|=
literal|null
decl_stmt|;
comment|// for each dir, get the InputFormat, and do getSplits.
for|for
control|(
name|Path
name|dir
range|:
name|dirs
control|)
block|{
name|PartitionDesc
name|part
init|=
name|getPartitionDescFromPath
argument_list|(
name|pathToPartitionInfo
argument_list|,
name|dir
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|inputFormatClass
init|=
name|part
operator|.
name|getInputFileFormatClass
argument_list|()
decl_stmt|;
name|TableDesc
name|table
init|=
name|part
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|TableScanOperator
name|tableScan
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|mrwork
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// Make filter pushdown information available to getSplits.
if|if
condition|(
operator|(
name|aliases
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|aliases
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
condition|)
block|{
name|Operator
name|op
init|=
name|mrwork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|aliases
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|op
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|op
operator|instanceof
name|TableScanOperator
operator|)
condition|)
block|{
name|tableScan
operator|=
operator|(
name|TableScanOperator
operator|)
name|op
expr_stmt|;
comment|// push down projections.
name|ColumnProjectionUtils
operator|.
name|appendReadColumns
argument_list|(
name|newjob
argument_list|,
name|tableScan
operator|.
name|getNeededColumnIDs
argument_list|()
argument_list|,
name|tableScan
operator|.
name|getNeededColumns
argument_list|()
argument_list|)
expr_stmt|;
comment|// push down filters
name|pushFilters
argument_list|(
name|newjob
argument_list|,
name|tableScan
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|currentDirs
operator|.
name|isEmpty
argument_list|()
operator|&&
name|inputFormatClass
operator|.
name|equals
argument_list|(
name|currentInputFormatClass
argument_list|)
operator|&&
name|table
operator|.
name|equals
argument_list|(
name|currentTable
argument_list|)
operator|&&
name|tableScan
operator|==
name|currentTableScan
condition|)
block|{
name|currentDirs
operator|.
name|add
argument_list|(
name|dir
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|!
name|currentDirs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Generating splits"
argument_list|)
expr_stmt|;
name|addSplitsForGroup
argument_list|(
name|currentDirs
argument_list|,
name|currentTableScan
argument_list|,
name|newjob
argument_list|,
name|getInputFormatFromCache
argument_list|(
name|currentInputFormatClass
argument_list|,
name|job
argument_list|)
argument_list|,
name|currentInputFormatClass
argument_list|,
name|currentDirs
operator|.
name|size
argument_list|()
operator|*
operator|(
name|numSplits
operator|/
name|dirs
operator|.
name|length
operator|)
argument_list|,
name|currentTable
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
name|currentDirs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|currentDirs
operator|.
name|add
argument_list|(
name|dir
argument_list|)
expr_stmt|;
name|currentTableScan
operator|=
name|tableScan
expr_stmt|;
name|currentTable
operator|=
name|table
expr_stmt|;
name|currentInputFormatClass
operator|=
name|inputFormatClass
expr_stmt|;
block|}
if|if
condition|(
name|dirs
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Generating splits"
argument_list|)
expr_stmt|;
name|addSplitsForGroup
argument_list|(
name|currentDirs
argument_list|,
name|currentTableScan
argument_list|,
name|newjob
argument_list|,
name|getInputFormatFromCache
argument_list|(
name|currentInputFormatClass
argument_list|,
name|job
argument_list|)
argument_list|,
name|currentInputFormatClass
argument_list|,
name|currentDirs
operator|.
name|size
argument_list|()
operator|*
operator|(
name|numSplits
operator|/
name|dirs
operator|.
name|length
operator|)
argument_list|,
name|currentTable
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
name|Utilities
operator|.
name|clearWorkMap
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"number of splits "
operator|+
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogEnd
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|GET_SPLITS
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toArray
argument_list|(
operator|new
name|HiveInputSplit
index|[
name|result
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|PartitionDesc
name|getPartitionDescFromPath
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
throws|throws
name|IOException
block|{
name|PartitionDesc
name|partDesc
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
name|partDesc
operator|==
literal|null
condition|)
block|{
name|partDesc
operator|=
name|pathToPartitionInfo
operator|.
name|get
argument_list|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partDesc
operator|==
literal|null
condition|)
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
literal|" in "
operator|+
name|pathToPartitionInfo
argument_list|)
throw|;
block|}
return|return
name|partDesc
return|;
block|}
specifier|public
specifier|static
name|void
name|pushFilters
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|TableScanOperator
name|tableScan
parameter_list|)
block|{
name|TableScanDesc
name|scanDesc
init|=
name|tableScan
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|scanDesc
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// construct column name list and types for reference by filter push down
name|Utilities
operator|.
name|setColumnNameList
argument_list|(
name|jobConf
argument_list|,
name|tableScan
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setColumnTypeList
argument_list|(
name|jobConf
argument_list|,
name|tableScan
argument_list|)
expr_stmt|;
comment|// push down filters
name|ExprNodeGenericFuncDesc
name|filterExpr
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|scanDesc
operator|.
name|getFilterExpr
argument_list|()
decl_stmt|;
if|if
condition|(
name|filterExpr
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
name|filterText
init|=
name|filterExpr
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|String
name|filterExprSerialized
init|=
name|Utilities
operator|.
name|serializeExpression
argument_list|(
name|filterExpr
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter text = "
operator|+
name|filterText
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Filter expression = "
operator|+
name|filterExprSerialized
argument_list|)
expr_stmt|;
block|}
name|jobConf
operator|.
name|set
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_TEXT_CONF_STR
argument_list|,
name|filterText
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_EXPR_CONF_STR
argument_list|,
name|filterExprSerialized
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|pushProjectionsAndFilters
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Class
name|inputFormatClass
parameter_list|,
name|String
name|splitPath
parameter_list|,
name|String
name|splitPathWithNoSchema
parameter_list|)
block|{
name|pushProjectionsAndFilters
argument_list|(
name|jobConf
argument_list|,
name|inputFormatClass
argument_list|,
name|splitPath
argument_list|,
name|splitPathWithNoSchema
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|pushProjectionsAndFilters
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Class
name|inputFormatClass
parameter_list|,
name|String
name|splitPath
parameter_list|,
name|String
name|splitPathWithNoSchema
parameter_list|,
name|boolean
name|nonNative
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|mrwork
operator|==
literal|null
condition|)
block|{
name|init
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|mrwork
operator|.
name|getPathToAliases
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|iterator
init|=
name|this
operator|.
name|mrwork
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|boolean
name|match
decl_stmt|;
if|if
condition|(
name|nonNative
condition|)
block|{
comment|// For non-native tables, we need to do an exact match to avoid
comment|// HIVE-1903.  (The table location contains no files, and the string
comment|// representation of its path does not have a trailing slash.)
name|match
operator|=
name|splitPath
operator|.
name|equals
argument_list|(
name|key
argument_list|)
operator|||
name|splitPathWithNoSchema
operator|.
name|equals
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// But for native tables, we need to do a prefix match for
comment|// subdirectories.  (Unlike non-native tables, prefix mixups don't seem
comment|// to be a potential problem here since we are always dealing with the
comment|// path to something deeper than the table location.)
name|match
operator|=
name|splitPath
operator|.
name|startsWith
argument_list|(
name|key
argument_list|)
operator|||
name|splitPathWithNoSchema
operator|.
name|startsWith
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|match
condition|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|list
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|val
range|:
name|list
control|)
block|{
name|aliases
operator|.
name|add
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|alias
range|:
name|aliases
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
init|=
name|this
operator|.
name|mrwork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|TableScanOperator
name|ts
init|=
operator|(
name|TableScanOperator
operator|)
name|op
decl_stmt|;
comment|// push down projections.
name|ColumnProjectionUtils
operator|.
name|appendReadColumns
argument_list|(
name|jobConf
argument_list|,
name|ts
operator|.
name|getNeededColumnIDs
argument_list|()
argument_list|,
name|ts
operator|.
name|getNeededColumns
argument_list|()
argument_list|)
expr_stmt|;
comment|// push down filters
name|pushFilters
argument_list|(
name|jobConf
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


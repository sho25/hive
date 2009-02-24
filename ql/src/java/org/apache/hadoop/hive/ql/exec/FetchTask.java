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
name|exec
package|;
end_package

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
name|Arrays
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
name|LinkedHashMap
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
name|Vector
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
name|plan
operator|.
name|fetchWork
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
name|partitionDesc
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
name|tableDesc
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
name|Deserializer
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
name|MetadataTypedColumnsetSerDe
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
name|lazy
operator|.
name|LazySimpleSerDe
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
name|hadoop
operator|.
name|util
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
name|io
operator|.
name|Text
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
name|serde
operator|.
name|Constants
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/**  * FetchTask implementation  **/
end_comment

begin_class
specifier|public
class|class
name|FetchTask
extends|extends
name|Task
argument_list|<
name|fetchWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|int
name|maxRows
init|=
literal|100
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|currRecReader
operator|=
literal|null
expr_stmt|;
try|try
block|{
comment|// Create a file system handle
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|ExecDriver
operator|.
name|class
argument_list|)
expr_stmt|;
name|mSerde
operator|=
operator|new
name|LazySimpleSerDe
argument_list|()
expr_stmt|;
name|Properties
name|mSerdeProp
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|mSerdeProp
operator|.
name|put
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|""
operator|+
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|mSerdeProp
operator|.
name|put
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"NULL"
argument_list|)
expr_stmt|;
name|mSerde
operator|.
name|initialize
argument_list|(
name|job
argument_list|,
name|mSerdeProp
argument_list|)
expr_stmt|;
name|currPath
operator|=
literal|null
expr_stmt|;
name|currTbl
operator|=
literal|null
expr_stmt|;
name|currPart
operator|=
literal|null
expr_stmt|;
name|iterPath
operator|=
literal|null
expr_stmt|;
name|iterPartDesc
operator|=
literal|null
expr_stmt|;
name|totalRows
operator|=
literal|0
expr_stmt|;
name|tblDataDone
operator|=
literal|false
expr_stmt|;
name|rowWithPart
operator|=
operator|new
name|Object
index|[
literal|2
index|]
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Bail out ungracefully - we should never hit
comment|// this here - but would have hit it in SemanticAnalyzer
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|int
name|execute
parameter_list|()
block|{
assert|assert
literal|false
assert|;
return|return
literal|0
return|;
block|}
comment|/**    * Return the tableDesc of the fetchWork    */
specifier|public
name|tableDesc
name|getTblDesc
parameter_list|()
block|{
return|return
name|work
operator|.
name|getTblDesc
argument_list|()
return|;
block|}
comment|/**    * A cache of InputFormat instances.    */
specifier|private
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
name|HashMap
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
name|Configuration
name|conf
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
name|conf
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
specifier|private
name|int
name|splitNum
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|currRecReader
decl_stmt|;
specifier|private
name|InputSplit
index|[]
name|inputSplits
decl_stmt|;
specifier|private
name|InputFormat
name|inputFormat
decl_stmt|;
specifier|private
name|JobConf
name|job
decl_stmt|;
specifier|private
name|WritableComparable
name|key
decl_stmt|;
specifier|private
name|Writable
name|value
decl_stmt|;
specifier|private
name|Deserializer
name|serde
decl_stmt|;
specifier|private
name|LazySimpleSerDe
name|mSerde
decl_stmt|;
specifier|private
name|int
name|totalRows
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|Path
argument_list|>
name|iterPath
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
name|partitionDesc
argument_list|>
name|iterPartDesc
decl_stmt|;
specifier|private
name|Path
name|currPath
decl_stmt|;
specifier|private
name|partitionDesc
name|currPart
decl_stmt|;
specifier|private
name|tableDesc
name|currTbl
decl_stmt|;
specifier|private
name|boolean
name|tblDataDone
decl_stmt|;
specifier|private
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
specifier|private
name|Object
index|[]
name|rowWithPart
decl_stmt|;
specifier|private
name|void
name|setPrtnDesc
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|pcols
init|=
name|currPart
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
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
name|Constants
operator|.
name|META_TABLE_PARTITION_COLUMNS
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|currPart
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|partObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|partKeys
init|=
name|pcols
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|partKeys
control|)
block|{
name|partNames
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|partValues
operator|.
name|add
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardPrimitiveObjectInspector
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|partObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|partNames
argument_list|,
name|partObjectInspectors
argument_list|)
decl_stmt|;
name|rowObjectInspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|rowWithPart
index|[
literal|1
index|]
operator|=
name|partValues
expr_stmt|;
name|rowObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getUnionStructObjectInspector
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StructObjectInspector
index|[]
block|{
name|rowObjectInspector
block|,
name|partObjectInspector
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getNextPath
parameter_list|()
throws|throws
name|Exception
block|{
comment|// first time
if|if
condition|(
name|iterPath
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|getTblDir
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|tblDataDone
condition|)
block|{
name|currPath
operator|=
name|work
operator|.
name|getTblDir
argument_list|()
expr_stmt|;
name|currTbl
operator|=
name|work
operator|.
name|getTblDesc
argument_list|()
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|currPath
argument_list|)
condition|)
block|{
name|FileStatus
index|[]
name|fStats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|currPath
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fStat
range|:
name|fStats
control|)
block|{
if|if
condition|(
name|fStat
operator|.
name|getLen
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tblDataDone
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|tblDataDone
condition|)
name|currPath
operator|=
literal|null
expr_stmt|;
return|return;
block|}
else|else
block|{
name|currTbl
operator|=
literal|null
expr_stmt|;
name|currPath
operator|=
literal|null
expr_stmt|;
block|}
return|return;
block|}
else|else
block|{
name|iterPath
operator|=
name|work
operator|.
name|getPartDir
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|iterPartDesc
operator|=
name|work
operator|.
name|getPartDesc
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
block|}
while|while
condition|(
name|iterPath
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Path
name|nxt
init|=
name|iterPath
operator|.
name|next
argument_list|()
decl_stmt|;
name|partitionDesc
name|prt
init|=
name|iterPartDesc
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|nxt
argument_list|)
condition|)
block|{
name|FileStatus
index|[]
name|fStats
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|nxt
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fStat
range|:
name|fStats
control|)
block|{
if|if
condition|(
name|fStat
operator|.
name|getLen
argument_list|()
operator|>
literal|0
condition|)
block|{
name|currPath
operator|=
name|nxt
expr_stmt|;
name|currPart
operator|=
name|prt
expr_stmt|;
return|return;
block|}
block|}
block|}
block|}
block|}
specifier|private
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|getRecordReader
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|currPath
operator|==
literal|null
condition|)
block|{
name|getNextPath
argument_list|()
expr_stmt|;
if|if
condition|(
name|currPath
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|FileInputFormat
operator|.
name|setInputPaths
argument_list|(
name|job
argument_list|,
name|currPath
argument_list|)
expr_stmt|;
name|tableDesc
name|tmp
init|=
name|currTbl
decl_stmt|;
if|if
condition|(
name|tmp
operator|==
literal|null
condition|)
name|tmp
operator|=
name|currPart
operator|.
name|getTableDesc
argument_list|()
expr_stmt|;
name|inputFormat
operator|=
name|getInputFormatFromCache
argument_list|(
name|tmp
operator|.
name|getInputFileFormatClass
argument_list|()
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|inputSplits
operator|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|splitNum
operator|=
literal|0
expr_stmt|;
name|serde
operator|=
name|tmp
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
name|job
argument_list|,
name|tmp
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating fetchTask with deserializer typeinfo: "
operator|+
name|serde
operator|.
name|getObjectInspector
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"deserializer properties: "
operator|+
name|tmp
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|tblDataDone
condition|)
name|setPrtnDesc
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|splitNum
operator|>=
name|inputSplits
operator|.
name|length
condition|)
block|{
if|if
condition|(
name|currRecReader
operator|!=
literal|null
condition|)
block|{
name|currRecReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|currRecReader
operator|=
literal|null
expr_stmt|;
block|}
name|currPath
operator|=
literal|null
expr_stmt|;
return|return
name|getRecordReader
argument_list|()
return|;
block|}
name|currRecReader
operator|=
name|inputFormat
operator|.
name|getRecordReader
argument_list|(
name|inputSplits
index|[
name|splitNum
operator|++
index|]
argument_list|,
name|job
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
expr_stmt|;
name|key
operator|=
name|currRecReader
operator|.
name|createKey
argument_list|()
expr_stmt|;
name|value
operator|=
name|currRecReader
operator|.
name|createValue
argument_list|()
expr_stmt|;
return|return
name|currRecReader
return|;
block|}
comment|/**    * Return the maximum number of rows returned by fetch    */
specifier|public
name|int
name|getMaxRows
parameter_list|()
block|{
return|return
name|maxRows
return|;
block|}
comment|/**    * Set the maximum number of rows returned by fetch    */
specifier|public
name|void
name|setMaxRows
parameter_list|(
name|int
name|maxRows
parameter_list|)
block|{
name|this
operator|.
name|maxRows
operator|=
name|maxRows
expr_stmt|;
block|}
specifier|public
name|boolean
name|fetch
parameter_list|(
name|Vector
argument_list|<
name|String
argument_list|>
name|res
parameter_list|)
block|{
try|try
block|{
name|int
name|numRows
init|=
literal|0
decl_stmt|;
name|int
name|rowsRet
init|=
name|maxRows
decl_stmt|;
if|if
condition|(
operator|(
name|work
operator|.
name|getLimit
argument_list|()
operator|>=
literal|0
operator|)
operator|&&
operator|(
operator|(
name|work
operator|.
name|getLimit
argument_list|()
operator|-
name|totalRows
operator|)
operator|<
name|rowsRet
operator|)
condition|)
name|rowsRet
operator|=
name|work
operator|.
name|getLimit
argument_list|()
operator|-
name|totalRows
expr_stmt|;
if|if
condition|(
name|rowsRet
operator|<=
literal|0
condition|)
block|{
if|if
condition|(
name|currRecReader
operator|!=
literal|null
condition|)
block|{
name|currRecReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|currRecReader
operator|=
literal|null
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
while|while
condition|(
name|numRows
operator|<
name|rowsRet
condition|)
block|{
if|if
condition|(
name|currRecReader
operator|==
literal|null
condition|)
block|{
name|currRecReader
operator|=
name|getRecordReader
argument_list|()
expr_stmt|;
if|if
condition|(
name|currRecReader
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|numRows
operator|==
literal|0
condition|)
return|return
literal|false
return|;
name|totalRows
operator|+=
name|numRows
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
name|boolean
name|ret
init|=
name|currRecReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
condition|)
block|{
if|if
condition|(
name|tblDataDone
condition|)
block|{
name|Object
name|obj
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|res
operator|.
name|add
argument_list|(
operator|(
operator|(
name|Text
operator|)
name|mSerde
operator|.
name|serialize
argument_list|(
name|obj
argument_list|,
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
operator|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowWithPart
index|[
literal|0
index|]
operator|=
name|serde
operator|.
name|deserialize
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|res
operator|.
name|add
argument_list|(
operator|(
operator|(
name|Text
operator|)
name|mSerde
operator|.
name|serialize
argument_list|(
name|rowWithPart
argument_list|,
name|rowObjectInspector
argument_list|)
operator|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|numRows
operator|++
expr_stmt|;
block|}
else|else
block|{
name|currRecReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|currRecReader
operator|=
literal|null
expr_stmt|;
name|currRecReader
operator|=
name|getRecordReader
argument_list|()
expr_stmt|;
if|if
condition|(
name|currRecReader
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|numRows
operator|==
literal|0
condition|)
return|return
literal|false
return|;
name|totalRows
operator|+=
name|numRows
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
name|key
operator|=
name|currRecReader
operator|.
name|createKey
argument_list|()
expr_stmt|;
name|value
operator|=
name|currRecReader
operator|.
name|createValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|totalRows
operator|+=
name|numRows
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit


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
name|common
operator|.
name|FileUtils
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
name|FetchWork
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
name|hive
operator|.
name|ql
operator|.
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|objectinspector
operator|.
name|InspectableObject
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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

begin_comment
comment|/**  * FetchTask implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|FetchOperator
implements|implements
name|Serializable
block|{
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FetchOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
name|LogHelper
name|console
init|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|isEmptyTable
decl_stmt|;
specifier|private
name|boolean
name|isNativeTable
decl_stmt|;
specifier|private
name|FetchWork
name|work
decl_stmt|;
specifier|private
name|int
name|splitNum
decl_stmt|;
specifier|private
name|PartitionDesc
name|currPart
decl_stmt|;
specifier|private
name|TableDesc
name|currTbl
decl_stmt|;
specifier|private
name|boolean
name|tblDataDone
decl_stmt|;
specifier|private
specifier|transient
name|RecordReader
argument_list|<
name|WritableComparable
argument_list|,
name|Writable
argument_list|>
name|currRecReader
decl_stmt|;
specifier|private
specifier|transient
name|InputSplit
index|[]
name|inputSplits
decl_stmt|;
specifier|private
specifier|transient
name|InputFormat
name|inputFormat
decl_stmt|;
specifier|private
specifier|transient
name|JobConf
name|job
decl_stmt|;
specifier|private
specifier|transient
name|WritableComparable
name|key
decl_stmt|;
specifier|private
specifier|transient
name|Writable
name|value
decl_stmt|;
specifier|private
specifier|transient
name|Deserializer
name|serde
decl_stmt|;
specifier|private
specifier|transient
name|Iterator
argument_list|<
name|Path
argument_list|>
name|iterPath
decl_stmt|;
specifier|private
specifier|transient
name|Iterator
argument_list|<
name|PartitionDesc
argument_list|>
name|iterPartDesc
decl_stmt|;
specifier|private
specifier|transient
name|Path
name|currPath
decl_stmt|;
specifier|private
specifier|transient
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
specifier|private
specifier|transient
name|Object
index|[]
name|rowWithPart
decl_stmt|;
specifier|public
name|FetchOperator
parameter_list|()
block|{   }
specifier|public
name|FetchOperator
parameter_list|(
name|FetchWork
name|work
parameter_list|,
name|JobConf
name|job
parameter_list|)
block|{
name|this
operator|.
name|work
operator|=
name|work
expr_stmt|;
name|initialize
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
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
if|if
condition|(
name|work
operator|.
name|getTblDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|isNativeTable
operator|=
operator|!
name|work
operator|.
name|getTblDesc
argument_list|()
operator|.
name|isNonNative
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|isNativeTable
operator|=
literal|true
expr_stmt|;
block|}
block|}
specifier|public
name|FetchWork
name|getWork
parameter_list|()
block|{
return|return
name|work
return|;
block|}
specifier|public
name|void
name|setWork
parameter_list|(
name|FetchWork
name|work
parameter_list|)
block|{
name|this
operator|.
name|work
operator|=
name|work
expr_stmt|;
block|}
specifier|public
name|int
name|getSplitNum
parameter_list|()
block|{
return|return
name|splitNum
return|;
block|}
specifier|public
name|void
name|setSplitNum
parameter_list|(
name|int
name|splitNum
parameter_list|)
block|{
name|this
operator|.
name|splitNum
operator|=
name|splitNum
expr_stmt|;
block|}
specifier|public
name|PartitionDesc
name|getCurrPart
parameter_list|()
block|{
return|return
name|currPart
return|;
block|}
specifier|public
name|void
name|setCurrPart
parameter_list|(
name|PartitionDesc
name|currPart
parameter_list|)
block|{
name|this
operator|.
name|currPart
operator|=
name|currPart
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getCurrTbl
parameter_list|()
block|{
return|return
name|currTbl
return|;
block|}
specifier|public
name|void
name|setCurrTbl
parameter_list|(
name|TableDesc
name|currTbl
parameter_list|)
block|{
name|this
operator|.
name|currTbl
operator|=
name|currTbl
expr_stmt|;
block|}
specifier|public
name|boolean
name|isTblDataDone
parameter_list|()
block|{
return|return
name|tblDataDone
return|;
block|}
specifier|public
name|void
name|setTblDataDone
parameter_list|(
name|boolean
name|tblDataDone
parameter_list|)
block|{
name|this
operator|.
name|tblDataDone
operator|=
name|tblDataDone
expr_stmt|;
block|}
specifier|public
name|boolean
name|isEmptyTable
parameter_list|()
block|{
return|return
name|isEmptyTable
return|;
block|}
specifier|public
name|void
name|setEmptyTable
parameter_list|(
name|boolean
name|isEmptyTable
parameter_list|)
block|{
name|this
operator|.
name|isEmptyTable
operator|=
name|isEmptyTable
expr_stmt|;
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
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
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
name|getTblDirPath
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
name|isNativeTable
condition|)
block|{
name|FileSystem
name|fs
init|=
name|currPath
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
decl_stmt|;
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
name|listStatusUnderPath
argument_list|(
name|fs
argument_list|,
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
block|}
else|else
block|{
name|tblDataDone
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|tblDataDone
condition|)
block|{
name|currPath
operator|=
literal|null
expr_stmt|;
block|}
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
name|FetchWork
operator|.
name|convertStringToPathArray
argument_list|(
name|work
operator|.
name|getPartDir
argument_list|()
argument_list|)
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
name|PartitionDesc
name|prt
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|iterPartDesc
operator|!=
literal|null
condition|)
block|{
name|prt
operator|=
name|iterPartDesc
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
name|nxt
operator|.
name|getFileSystem
argument_list|(
name|job
argument_list|)
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
name|listStatusUnderPath
argument_list|(
name|fs
argument_list|,
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
if|if
condition|(
name|iterPartDesc
operator|!=
literal|null
condition|)
block|{
name|currPart
operator|=
name|prt
expr_stmt|;
block|}
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
block|{
return|return
literal|null
return|;
block|}
comment|// not using FileInputFormat.setInputPaths() here because it forces a
comment|// connection
comment|// to the default file system - which may or may not be online during pure
comment|// metadata
comment|// operations
name|job
operator|.
name|set
argument_list|(
literal|"mapred.input.dir"
argument_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|escapeString
argument_list|(
name|currPath
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|PartitionDesc
name|tmp
decl_stmt|;
if|if
condition|(
name|currTbl
operator|==
literal|null
condition|)
block|{
name|tmp
operator|=
name|currPart
expr_stmt|;
block|}
else|else
block|{
name|tmp
operator|=
operator|new
name|PartitionDesc
argument_list|(
name|currTbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
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
name|Utilities
operator|.
name|copyTableJobPropertiesToConf
argument_list|(
name|tmp
operator|.
name|getTableDesc
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
block|}
if|if
condition|(
name|currPart
operator|!=
literal|null
condition|)
block|{
name|setPrtnDesc
argument_list|()
expr_stmt|;
block|}
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
comment|/**    * Get the next row. The fetch context is modified appropriately.    *    **/
specifier|public
name|InspectableObject
name|getNextRow
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
while|while
condition|(
literal|true
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
return|return
literal|null
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
name|this
operator|.
name|currPart
operator|==
literal|null
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
return|return
operator|new
name|InspectableObject
argument_list|(
name|obj
argument_list|,
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
return|;
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
return|return
operator|new
name|InspectableObject
argument_list|(
name|rowWithPart
argument_list|,
name|rowObjectInspector
argument_list|)
return|;
block|}
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
block|}
block|}
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
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Clear the context, if anything needs to be done.    *    **/
specifier|public
name|void
name|clearFetchContext
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
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
name|this
operator|.
name|currPath
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|iterPath
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|iterPartDesc
operator|=
literal|null
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
name|HiveException
argument_list|(
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * used for bucket map join. there is a hack for getting partitionDesc. bucket map join right now    * only allow one partition present in bucket map join.    */
specifier|public
name|void
name|setupContext
parameter_list|(
name|Iterator
argument_list|<
name|Path
argument_list|>
name|iterPath
parameter_list|,
name|Iterator
argument_list|<
name|PartitionDesc
argument_list|>
name|iterPartDesc
parameter_list|)
block|{
name|this
operator|.
name|iterPath
operator|=
name|iterPath
expr_stmt|;
name|this
operator|.
name|iterPartDesc
operator|=
name|iterPartDesc
expr_stmt|;
if|if
condition|(
name|iterPartDesc
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
name|this
operator|.
name|currTbl
operator|=
name|work
operator|.
name|getTblDesc
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// hack, get the first.
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|listParts
init|=
name|work
operator|.
name|getPartDesc
argument_list|()
decl_stmt|;
name|currPart
operator|=
name|listParts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|ObjectInspector
name|getOutputObjectInspector
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
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
name|TableDesc
name|tbl
init|=
name|work
operator|.
name|getTblDesc
argument_list|()
decl_stmt|;
name|Deserializer
name|serde
init|=
name|tbl
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
name|job
argument_list|,
name|tbl
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|serde
operator|.
name|getObjectInspector
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|work
operator|.
name|getPartDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|listParts
init|=
name|work
operator|.
name|getPartDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|listParts
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|currPart
operator|=
name|listParts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|serde
operator|=
name|currPart
operator|.
name|getTableDesc
argument_list|()
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
name|currPart
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|setPrtnDesc
argument_list|()
expr_stmt|;
name|currPart
operator|=
literal|null
expr_stmt|;
return|return
name|rowObjectInspector
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
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
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|/**    * Lists status for all files under a given path. Whether or not this is recursive depends on the    * setting of job configuration parameter mapred.input.dir.recursive.    *    * @param fs    *          file system    *    * @param p    *          path in file system    *    * @return list of file status entries    */
specifier|private
name|FileStatus
index|[]
name|listStatusUnderPath
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|job
argument_list|,
name|FetchOperator
operator|.
name|class
argument_list|)
decl_stmt|;
name|boolean
name|recursive
init|=
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPMAPREDINPUTDIRRECURSIVE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|recursive
condition|)
block|{
return|return
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
return|;
block|}
name|List
argument_list|<
name|FileStatus
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|FileStatus
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|stat
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|p
argument_list|)
control|)
block|{
name|FileUtils
operator|.
name|listStatusRecursively
argument_list|(
name|fs
argument_list|,
name|stat
argument_list|,
name|results
argument_list|)
expr_stmt|;
block|}
return|return
name|results
operator|.
name|toArray
argument_list|(
operator|new
name|FileStatus
index|[
name|results
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit


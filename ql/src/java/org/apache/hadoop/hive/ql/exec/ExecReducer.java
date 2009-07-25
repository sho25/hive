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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|MemoryUsage
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|*
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
name|ql
operator|.
name|plan
operator|.
name|mapredWork
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
name|exec
operator|.
name|ExecMapper
operator|.
name|reportStats
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
name|io
operator|.
name|ByteWritable
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
name|BytesWritable
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

begin_class
specifier|public
class|class
name|ExecReducer
extends|extends
name|MapReduceBase
implements|implements
name|Reducer
block|{
specifier|private
name|JobConf
name|jc
decl_stmt|;
specifier|private
name|OutputCollector
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|oc
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
specifier|private
name|Reporter
name|rp
decl_stmt|;
specifier|private
name|boolean
name|abort
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isTagged
init|=
literal|false
decl_stmt|;
specifier|private
name|long
name|cntr
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|nextCntr
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|String
index|[]
name|fieldNames
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"ExecReducer"
argument_list|)
decl_stmt|;
comment|// used to log memory usage periodically
specifier|private
name|MemoryMXBean
name|memoryMXBean
decl_stmt|;
comment|// TODO: move to DynamicSerDe when it's ready
specifier|private
name|Deserializer
name|inputKeyDeserializer
decl_stmt|;
comment|// Input value serde needs to be an array to support different SerDe
comment|// for different tags
specifier|private
name|SerDe
index|[]
name|inputValueDeserializer
init|=
operator|new
name|SerDe
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
static|static
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fieldNameArray
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
name|Utilities
operator|.
name|ReduceField
name|r
range|:
name|Utilities
operator|.
name|ReduceField
operator|.
name|values
argument_list|()
control|)
block|{
name|fieldNameArray
operator|.
name|add
argument_list|(
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fieldNames
operator|=
name|fieldNameArray
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|ObjectInspector
index|[]
name|rowObjectInspector
init|=
operator|new
name|ObjectInspector
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
name|ObjectInspector
index|[]
name|valueObjectInspector
init|=
operator|new
name|ObjectInspector
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
comment|// Allocate the bean at the beginning -
name|memoryMXBean
operator|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"maximum memory = "
operator|+
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"conf classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|job
operator|.
name|getClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"thread classpath = "
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"cannot get classpath: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|jc
operator|=
name|job
expr_stmt|;
name|mapredWork
name|gWork
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|reducer
operator|=
name|gWork
operator|.
name|getReducer
argument_list|()
expr_stmt|;
name|reducer
operator|.
name|setParentOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// clear out any parents as reducer is the root
name|isTagged
operator|=
name|gWork
operator|.
name|getNeedsTagging
argument_list|()
expr_stmt|;
try|try
block|{
name|tableDesc
name|keyTableDesc
init|=
name|gWork
operator|.
name|getKeyDesc
argument_list|()
decl_stmt|;
name|inputKeyDeserializer
operator|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|inputKeyDeserializer
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|keyObjectInspector
operator|=
name|inputKeyDeserializer
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|tag
init|=
literal|0
init|;
name|tag
operator|<
name|gWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|tag
operator|++
control|)
block|{
comment|// We should initialize the SerDe with the TypeInfo when available.
name|tableDesc
name|valueTableDesc
init|=
name|gWork
operator|.
name|getTagToValueDesc
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
decl_stmt|;
name|inputValueDeserializer
index|[
name|tag
index|]
operator|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|valueTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|inputValueDeserializer
index|[
name|tag
index|]
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|valueTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|valueObjectInspector
index|[
name|tag
index|]
operator|=
name|inputValueDeserializer
index|[
name|tag
index|]
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|keyObjectInspector
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|valueObjectInspector
index|[
name|tag
index|]
argument_list|)
expr_stmt|;
name|ois
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableByteObjectInspector
argument_list|)
expr_stmt|;
name|rowObjectInspector
index|[
name|tag
index|]
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|fieldNames
argument_list|)
argument_list|,
name|ois
argument_list|)
expr_stmt|;
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|//initialize reduce operator tree
try|try
block|{
name|l4j
operator|.
name|info
argument_list|(
name|reducer
operator|.
name|dump
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Reduce operator initialization failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|Object
name|keyObject
decl_stmt|;
specifier|private
name|Object
index|[]
name|valueObject
init|=
operator|new
name|Object
index|[
name|Byte
operator|.
name|MAX_VALUE
index|]
decl_stmt|;
specifier|private
name|BytesWritable
name|groupKey
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|row
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|ByteWritable
name|tag
init|=
operator|new
name|ByteWritable
argument_list|()
decl_stmt|;
specifier|public
name|void
name|reduce
parameter_list|(
name|Object
name|key
parameter_list|,
name|Iterator
name|values
parameter_list|,
name|OutputCollector
name|output
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|oc
operator|==
literal|null
condition|)
block|{
comment|// propagete reporter and output collector to all operators
name|oc
operator|=
name|output
expr_stmt|;
name|rp
operator|=
name|reporter
expr_stmt|;
name|reducer
operator|.
name|setOutputCollector
argument_list|(
name|oc
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|setReporter
argument_list|(
name|rp
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|BytesWritable
name|keyWritable
init|=
operator|(
name|BytesWritable
operator|)
name|key
decl_stmt|;
name|tag
operator|.
name|set
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|isTagged
condition|)
block|{
comment|// remove the tag
name|int
name|size
init|=
name|keyWritable
operator|.
name|getSize
argument_list|()
operator|-
literal|1
decl_stmt|;
name|tag
operator|.
name|set
argument_list|(
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|size
index|]
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|keyWritable
operator|.
name|equals
argument_list|(
name|groupKey
argument_list|)
condition|)
block|{
comment|// If a operator wants to do some work at the beginning of a group
if|if
condition|(
name|groupKey
operator|==
literal|null
condition|)
block|{
name|groupKey
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// If a operator wants to do some work at the end of a group
name|l4j
operator|.
name|trace
argument_list|(
literal|"End Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
name|groupKey
operator|.
name|set
argument_list|(
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|trace
argument_list|(
literal|"Start Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|startGroup
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|keyObject
operator|=
name|inputKeyDeserializer
operator|.
name|deserialize
argument_list|(
name|keyWritable
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// System.err.print(keyObject.toString());
while|while
condition|(
name|values
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Writable
name|valueWritable
init|=
operator|(
name|Writable
operator|)
name|values
operator|.
name|next
argument_list|()
decl_stmt|;
comment|//System.err.print(who.getHo().toString());
try|try
block|{
name|valueObject
index|[
name|tag
operator|.
name|get
argument_list|()
index|]
operator|=
name|inputValueDeserializer
index|[
name|tag
operator|.
name|get
argument_list|()
index|]
operator|.
name|deserialize
argument_list|(
name|valueWritable
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|row
operator|.
name|clear
argument_list|()
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
name|keyObject
argument_list|)
expr_stmt|;
name|row
operator|.
name|add
argument_list|(
name|valueObject
index|[
name|tag
operator|.
name|get
argument_list|()
index|]
argument_list|)
expr_stmt|;
comment|// The tag is not used any more, we should remove it.
name|row
operator|.
name|add
argument_list|(
name|tag
argument_list|)
expr_stmt|;
if|if
condition|(
name|l4j
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|cntr
operator|++
expr_stmt|;
if|if
condition|(
name|cntr
operator|==
name|nextCntr
condition|)
block|{
name|long
name|used_memory
init|=
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
decl_stmt|;
name|l4j
operator|.
name|info
argument_list|(
literal|"ExecReducer: processing "
operator|+
name|cntr
operator|+
literal|" rows: used memory = "
operator|+
name|used_memory
argument_list|)
expr_stmt|;
name|nextCntr
operator|=
name|getNextCntr
argument_list|(
name|cntr
argument_list|)
expr_stmt|;
block|}
block|}
name|reducer
operator|.
name|process
argument_list|(
name|row
argument_list|,
name|tag
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|abort
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|OutOfMemoryError
condition|)
block|{
comment|// Don't create a new object if we are already out of memory
throw|throw
operator|(
name|OutOfMemoryError
operator|)
name|e
throw|;
block|}
else|else
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
block|}
specifier|private
name|long
name|getNextCntr
parameter_list|(
name|long
name|cntr
parameter_list|)
block|{
comment|// A very simple counter to keep track of number of rows processed by the reducer. It dumps
comment|// every 1 million times, and quickly before that
if|if
condition|(
name|cntr
operator|>=
literal|1000000
condition|)
return|return
name|cntr
operator|+
literal|1000000
return|;
return|return
literal|10
operator|*
name|cntr
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// No row was processed
if|if
condition|(
name|oc
operator|==
literal|null
condition|)
block|{
name|l4j
operator|.
name|trace
argument_list|(
literal|"Close called no row"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|groupKey
operator|!=
literal|null
condition|)
block|{
comment|// If a operator wants to do some work at the end of a group
name|l4j
operator|.
name|trace
argument_list|(
literal|"End Group"
argument_list|)
expr_stmt|;
name|reducer
operator|.
name|endGroup
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|l4j
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|l4j
operator|.
name|info
argument_list|(
literal|"ExecReducer: processed "
operator|+
name|cntr
operator|+
literal|" rows: used memory = "
operator|+
name|memoryMXBean
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getUsed
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reducer
operator|.
name|close
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|reportStats
name|rps
init|=
operator|new
name|reportStats
argument_list|(
name|rp
argument_list|)
decl_stmt|;
name|reducer
operator|.
name|preorderMap
argument_list|(
name|rps
argument_list|)
expr_stmt|;
return|return;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
comment|// signal new failure to map-reduce
name|l4j
operator|.
name|error
argument_list|(
literal|"Hit error while closing operators - failing tree"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error while closing operators: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit


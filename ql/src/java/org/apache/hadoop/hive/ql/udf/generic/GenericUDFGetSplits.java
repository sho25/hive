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
name|udf
operator|.
name|generic
package|;
end_package

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
name|List
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
name|UUID
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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|udf
operator|.
name|UDFType
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|Driver
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
name|QueryPlan
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
name|Task
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
name|tez
operator|.
name|TezTask
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
name|TezWork
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
name|CommandNeedRetryException
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|Description
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
name|UDFArgumentException
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
name|UDFArgumentLengthException
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
name|UDFArgumentTypeException
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
name|exec
operator|.
name|MapredContext
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
name|metadata
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
name|metadata
operator|.
name|Hive
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
name|llap
operator|.
name|LlapInputFormat
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
name|llap
operator|.
name|LlapOutputFormat
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|StringObjectInspector
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
name|IntObjectInspector
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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Schema
import|;
end_import

begin_comment
comment|/**  * GenericUDFGetSplits.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"get_splits"
argument_list|,
name|value
operator|=
literal|"_FUNC_(string,int) - "
operator|+
literal|"Returns an array of length int serialized splits for the referenced tables string."
argument_list|)
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|false
argument_list|)
specifier|public
class|class
name|GenericUDFGetSplits
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenericUDFGetSplits
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|transient
name|StringObjectInspector
name|stringOI
decl_stmt|;
specifier|private
specifier|transient
name|IntObjectInspector
name|intOI
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|retArray
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|JobConf
name|jc
decl_stmt|;
specifier|private
specifier|transient
name|Hive
name|db
decl_stmt|;
specifier|private
name|ByteArrayOutputStream
name|bos
decl_stmt|;
specifier|private
name|DataOutput
name|dos
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"initializing GenericUDFGetSplits"
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
operator|&&
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HiveConf
name|conf
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|jc
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|jc
operator|=
name|MapredContext
operator|.
name|get
argument_list|()
operator|.
name|getJobConf
argument_list|()
expr_stmt|;
name|db
operator|=
name|Hive
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to initialize: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initialized conf, jc and metastore connection"
argument_list|)
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function GET_SPLITS accepts 2 arguments."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|StringObjectInspector
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got "
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" instead of string."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"\""
operator|+
literal|"string\" is expected at function GET_SPLITS, "
operator|+
literal|"but \""
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|arguments
index|[
literal|1
index|]
operator|instanceof
name|IntObjectInspector
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got "
operator|+
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" instead of int."
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"\""
operator|+
literal|"int\" is expected at function GET_SPLITS, "
operator|+
literal|"but \""
operator|+
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
name|stringOI
operator|=
operator|(
name|StringObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|intOI
operator|=
operator|(
name|IntObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"if_class"
argument_list|,
literal|"split_class"
argument_list|,
literal|"split"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
name|Arrays
operator|.
expr|<
name|ObjectInspector
operator|>
name|asList
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaByteArrayObjectInspector
argument_list|)
decl_stmt|;
name|ObjectInspector
name|outputOI
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|names
argument_list|,
name|fieldOIs
argument_list|)
decl_stmt|;
name|ObjectInspector
name|listOI
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|outputOI
argument_list|)
decl_stmt|;
name|bos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|1024
argument_list|)
expr_stmt|;
name|dos
operator|=
operator|new
name|DataOutputStream
argument_list|(
name|bos
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"done initializing GenericUDFGetSplits"
argument_list|)
expr_stmt|;
return|return
name|listOI
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|retArray
operator|.
name|clear
argument_list|()
expr_stmt|;
name|String
name|query
init|=
name|stringOI
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|num
init|=
name|intOI
operator|.
name|get
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|()
decl_stmt|;
name|CommandProcessorResponse
name|cpr
decl_stmt|;
name|HiveConf
name|conf
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Need configuration"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"setting fetch.task.conversion to none and query file format to \""
operator|+
name|LlapOutputFormat
operator|.
name|class
operator|.
name|toString
argument_list|()
operator|+
literal|"\""
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEFETCHTASKCONVERSION
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYRESULTFILEFORMAT
argument_list|,
name|LlapOutputFormat
operator|.
name|class
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|cpr
operator|=
name|driver
operator|.
name|compileAndRespond
argument_list|(
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to compile query: "
operator|+
name|cpr
operator|.
name|getException
argument_list|()
argument_list|)
throw|;
block|}
name|QueryPlan
name|plan
init|=
name|driver
operator|.
name|getPlan
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|roots
init|=
name|plan
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
name|Schema
name|schema
init|=
name|plan
operator|.
name|getResultSchema
argument_list|()
decl_stmt|;
if|if
condition|(
name|roots
operator|==
literal|null
operator|||
name|roots
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|||
operator|!
operator|(
name|roots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|TezTask
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Was expecting a single TezTask."
argument_list|)
throw|;
block|}
name|Path
name|data
init|=
literal|null
decl_stmt|;
name|InputFormat
name|inp
init|=
literal|null
decl_stmt|;
name|String
name|ifc
init|=
literal|null
decl_stmt|;
name|TezWork
name|tezWork
init|=
operator|(
operator|(
name|TezTask
operator|)
name|roots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|tezWork
operator|.
name|getAllWork
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|String
name|tableName
init|=
literal|"table_"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"[^A-Za-z0-9 ]"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|String
name|ctas
init|=
literal|"create temporary table "
operator|+
name|tableName
operator|+
literal|" as "
operator|+
name|query
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"CTAS: "
operator|+
name|ctas
argument_list|)
expr_stmt|;
try|try
block|{
name|cpr
operator|=
name|driver
operator|.
name|run
argument_list|(
name|ctas
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
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
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to create temp table: "
operator|+
name|cpr
operator|.
name|getException
argument_list|()
argument_list|)
throw|;
block|}
name|query
operator|=
literal|"select * from "
operator|+
name|tableName
expr_stmt|;
name|cpr
operator|=
name|driver
operator|.
name|compileAndRespond
argument_list|(
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpr
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Failed to create temp table: "
operator|+
name|cpr
operator|.
name|getException
argument_list|()
argument_list|)
throw|;
block|}
name|plan
operator|=
name|driver
operator|.
name|getPlan
argument_list|()
expr_stmt|;
name|roots
operator|=
name|plan
operator|.
name|getRootTasks
argument_list|()
expr_stmt|;
name|schema
operator|=
name|plan
operator|.
name|getResultSchema
argument_list|()
expr_stmt|;
if|if
condition|(
name|roots
operator|==
literal|null
operator|||
name|roots
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|||
operator|!
operator|(
name|roots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|TezTask
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Was expecting a single TezTask."
argument_list|)
throw|;
block|}
name|tezWork
operator|=
operator|(
operator|(
name|TezTask
operator|)
name|roots
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getWork
argument_list|()
expr_stmt|;
comment|// Table table = db.getTable(tableName);
comment|// if (table.isPartitioned()) {
comment|//   throw new UDFArgumentException("Table " + tableName + " is partitioned.");
comment|// }
comment|// data = table.getDataLocation();
comment|// LOG.info("looking at: "+data);
comment|// ifc = table.getInputFormatClass().toString();
comment|// inp = ReflectionUtils.newInstance(table.getInputFormatClass(), jc);
block|}
name|MapWork
name|w
init|=
operator|(
name|MapWork
operator|)
name|tezWork
operator|.
name|getAllWork
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|inp
operator|=
operator|new
name|LlapInputFormat
argument_list|(
name|tezWork
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|ifc
operator|=
name|LlapInputFormat
operator|.
name|class
operator|.
name|toString
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|inp
operator|instanceof
name|JobConfigurable
condition|)
block|{
operator|(
operator|(
name|JobConfigurable
operator|)
name|inp
operator|)
operator|.
name|configure
argument_list|(
name|jc
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|inp
operator|instanceof
name|FileInputFormat
condition|)
block|{
operator|(
operator|(
name|FileInputFormat
operator|)
name|inp
operator|)
operator|.
name|addInputPath
argument_list|(
name|jc
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|InputSplit
name|s
range|:
name|inp
operator|.
name|getSplits
argument_list|(
name|jc
argument_list|,
name|num
argument_list|)
control|)
block|{
name|Object
index|[]
name|os
init|=
operator|new
name|Object
index|[
literal|3
index|]
decl_stmt|;
name|os
index|[
literal|0
index|]
operator|=
name|ifc
expr_stmt|;
name|os
index|[
literal|1
index|]
operator|=
name|s
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|bos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|s
operator|.
name|write
argument_list|(
name|dos
argument_list|)
expr_stmt|;
name|byte
index|[]
name|frozen
init|=
name|bos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|os
index|[
literal|2
index|]
operator|=
name|frozen
expr_stmt|;
name|retArray
operator|.
name|add
argument_list|(
name|os
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|retArray
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
assert|assert
name|children
operator|.
name|length
operator|==
literal|2
assert|;
return|return
name|getStandardDisplayString
argument_list|(
literal|"get_splits"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit


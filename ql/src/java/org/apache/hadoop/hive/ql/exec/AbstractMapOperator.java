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
name|TreeMap
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
name|ql
operator|.
name|CompilationOpContext
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
name|io
operator|.
name|LongWritable
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

begin_comment
comment|/**  * Abstract Map operator. Common code of MapOperator and VectorMapOperator.  **/
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
specifier|abstract
class|class
name|AbstractMapOperator
extends|extends
name|Operator
argument_list|<
name|MapWork
argument_list|>
implements|implements
name|Serializable
implements|,
name|Cloneable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * Initialization call sequence:    *    *   (Operator)                     Operator.setConf(MapWork conf);    *   (Operator)                     Operator.initialize(    *                                      Configuration hconf, ObjectInspector[] inputOIs);    *    *   ([Vector]MapOperator)          @Override setChildren(Configuration hconf)    *    *   (Operator)                     Operator.passExecContext(ExecMapperContext execContext)    *   (Operator)                     Operator.initializeLocalWork(Configuration hconf)    *    *   (AbstractMapOperator)          initializeMapOperator(Configuration hconf)    *    * [ (AbstractMapOperator)          initializeContexts() ]   // exec.tez.MapRecordProcessor only.    *    *   (Operator)                     Operator.setReporter(Reporter rep)    *    */
comment|/**    * Counter.    *    */
specifier|public
specifier|static
enum|enum
name|Counter
block|{
name|DESERIALIZE_ERRORS
block|,
name|RECORDS_IN
block|}
specifier|protected
specifier|final
specifier|transient
name|LongWritable
name|deserialize_error_count
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
specifier|transient
name|LongWritable
name|recordCounter
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|long
name|numRows
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
name|connectedOperators
init|=
operator|new
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Map
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
name|normalizedPaths
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|normalizePath
parameter_list|(
name|Path
name|onefile
parameter_list|,
name|boolean
name|schemaless
parameter_list|)
block|{
comment|//creating Path is expensive, so cache the corresponding
comment|//Path object in normalizedPaths
name|Path
name|path
init|=
name|normalizedPaths
operator|.
name|get
argument_list|(
name|onefile
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
name|path
operator|=
name|onefile
expr_stmt|;
if|if
condition|(
name|schemaless
operator|&&
name|path
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
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|normalizedPaths
operator|.
name|put
argument_list|(
name|onefile
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
return|return
name|path
return|;
block|}
specifier|protected
name|Path
name|getNominalPath
parameter_list|(
name|Path
name|fpath
parameter_list|)
block|{
name|Path
name|nominal
init|=
literal|null
decl_stmt|;
name|boolean
name|schemaless
init|=
name|fpath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
decl_stmt|;
for|for
control|(
name|Path
name|onefile
range|:
name|conf
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Path
name|onepath
init|=
name|normalizePath
argument_list|(
name|onefile
argument_list|,
name|schemaless
argument_list|)
decl_stmt|;
name|Path
name|curfpath
init|=
name|fpath
decl_stmt|;
if|if
condition|(
operator|!
name|schemaless
operator|&&
name|onepath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|==
literal|null
condition|)
block|{
name|curfpath
operator|=
operator|new
name|Path
argument_list|(
name|fpath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// check for the operators who will process rows coming to this Map Operator
if|if
condition|(
name|onepath
operator|.
name|toUri
argument_list|()
operator|.
name|relativize
argument_list|(
name|curfpath
operator|.
name|toUri
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|curfpath
operator|.
name|toUri
argument_list|()
argument_list|)
condition|)
block|{
comment|// not from this
continue|continue;
block|}
if|if
condition|(
name|nominal
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Ambiguous input path "
operator|+
name|fpath
argument_list|)
throw|;
block|}
name|nominal
operator|=
name|onefile
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|nominal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Invalid input path "
operator|+
name|fpath
argument_list|)
throw|;
block|}
return|return
name|nominal
return|;
block|}
specifier|public
specifier|abstract
name|void
name|initEmptyInputChildren
parameter_list|(
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|children
parameter_list|,
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|Exception
function_decl|;
comment|/** Kryo ctor. */
specifier|protected
name|AbstractMapOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AbstractMapOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|setChildren
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|Exception
function_decl|;
specifier|public
name|void
name|initializeMapOperator
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// set that parent initialization is done and call initialize on children
name|state
operator|=
name|State
operator|.
name|INIT
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|DESERIALIZE_ERRORS
operator|.
name|toString
argument_list|()
argument_list|,
name|deserialize_error_count
argument_list|)
expr_stmt|;
name|numRows
operator|=
literal|0
expr_stmt|;
name|String
name|context
init|=
name|hconf
operator|.
name|get
argument_list|(
name|Operator
operator|.
name|CONTEXT_NAME_KEY
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|!=
literal|null
operator|&&
operator|!
name|context
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|context
operator|=
literal|"_"
operator|+
name|context
operator|.
name|replace
argument_list|(
literal|" "
argument_list|,
literal|"_"
argument_list|)
expr_stmt|;
block|}
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|RECORDS_IN
operator|+
name|context
argument_list|,
name|recordCounter
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|initializeContexts
parameter_list|()
throws|throws
name|HiveException
function_decl|;
specifier|public
specifier|abstract
name|Deserializer
name|getCurrentDeserializer
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|void
name|process
parameter_list|(
name|Writable
name|value
parameter_list|)
throws|throws
name|HiveException
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
name|recordCounter
operator|.
name|set
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clearConnectedOperators
parameter_list|()
block|{
name|connectedOperators
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setConnectedOperators
parameter_list|(
name|int
name|tag
parameter_list|,
name|DummyStoreOperator
name|dummyOp
parameter_list|)
block|{
name|connectedOperators
operator|.
name|put
argument_list|(
name|tag
argument_list|,
name|dummyOp
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|DummyStoreOperator
argument_list|>
name|getConnectedOperators
parameter_list|()
block|{
return|return
name|connectedOperators
return|;
block|}
block|}
end_class

end_unit


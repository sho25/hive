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
name|ListSinkDesc
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
name|api
operator|.
name|OperatorType
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
name|serdeConstants
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
name|DefaultFetchFormatter
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
name|FetchFormatter
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
name|SerDeUtils
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
comment|/**  * For fetch task with operator tree, row read from FetchOperator is processed via operator tree  * and finally arrives to this operator.  */
end_comment

begin_class
specifier|public
class|class
name|ListSinkOperator
extends|extends
name|Operator
argument_list|<
name|ListSinkDesc
argument_list|>
block|{
specifier|private
specifier|transient
name|List
name|res
decl_stmt|;
specifier|private
specifier|transient
name|FetchFormatter
name|fetcher
decl_stmt|;
specifier|private
specifier|transient
name|int
name|numRows
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|ListSinkOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ListSinkOperator
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
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
try|try
block|{
name|fetcher
operator|=
name|initializeFetcher
argument_list|(
name|hconf
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
block|}
specifier|private
name|FetchFormatter
name|initializeFetcher
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|formatterName
init|=
name|conf
operator|.
name|get
argument_list|(
name|SerDeUtils
operator|.
name|LIST_SINK_OUTPUT_FORMATTER
argument_list|)
decl_stmt|;
name|FetchFormatter
name|fetcher
decl_stmt|;
if|if
condition|(
name|formatterName
operator|!=
literal|null
operator|&&
operator|!
name|formatterName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|FetchFormatter
argument_list|>
name|fetcherClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|formatterName
argument_list|,
literal|true
argument_list|,
name|Utilities
operator|.
name|getSessionSpecifiedClassLoader
argument_list|()
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|FetchFormatter
operator|.
name|class
argument_list|)
decl_stmt|;
name|fetcher
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|fetcherClass
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fetcher
operator|=
operator|new
name|DefaultFetchFormatter
argument_list|()
expr_stmt|;
block|}
comment|// selectively used by fetch formatter
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
name|serdeConstants
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
name|props
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
name|getConf
argument_list|()
operator|.
name|getSerializationNullFormat
argument_list|()
argument_list|)
expr_stmt|;
name|fetcher
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|props
argument_list|)
expr_stmt|;
return|return
name|fetcher
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|(
name|List
name|res
parameter_list|)
block|{
name|this
operator|.
name|res
operator|=
name|res
expr_stmt|;
name|this
operator|.
name|numRows
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|int
name|getNumRows
parameter_list|()
block|{
return|return
name|numRows
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|res
operator|.
name|add
argument_list|(
name|fetcher
operator|.
name|convert
argument_list|(
name|row
argument_list|,
name|inputObjInspectors
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|numRows
operator|++
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
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|FORWARD
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|ListSinkOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"LIST_SINK"
return|;
block|}
block|}
end_class

end_unit


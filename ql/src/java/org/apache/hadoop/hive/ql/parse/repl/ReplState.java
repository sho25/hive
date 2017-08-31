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
name|parse
operator|.
name|repl
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonIgnoreProperties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|SerializationConfig
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

begin_class
specifier|public
specifier|abstract
class|class
name|ReplState
block|{
annotation|@
name|JsonIgnoreProperties
specifier|private
specifier|static
specifier|final
name|Logger
name|REPL_LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"ReplState"
argument_list|)
decl_stmt|;
annotation|@
name|JsonIgnoreProperties
specifier|private
specifier|static
specifier|final
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
comment|// Thread-safe.
static|static
block|{
name|mapper
operator|.
name|configure
argument_list|(
name|SerializationConfig
operator|.
name|Feature
operator|.
name|AUTO_DETECT_GETTERS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|SerializationConfig
operator|.
name|Feature
operator|.
name|AUTO_DETECT_IS_GETTERS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|SerializationConfig
operator|.
name|Feature
operator|.
name|AUTO_DETECT_FIELDS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
enum|enum
name|LogTag
block|{
name|START
block|,
name|TABLE_DUMP
block|,
name|FUNCTION_DUMP
block|,
name|EVENT_DUMP
block|,
name|TABLE_LOAD
block|,
name|FUNCTION_LOAD
block|,
name|EVENT_LOAD
block|,
name|END
block|}
specifier|public
name|void
name|log
parameter_list|(
name|LogTag
name|tag
parameter_list|)
block|{
try|try
block|{
name|REPL_LOG
operator|.
name|info
argument_list|(
literal|"REPL::{}: {}"
argument_list|,
name|tag
operator|.
name|name
argument_list|()
argument_list|,
name|mapper
operator|.
name|writeValueAsString
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
name|REPL_LOG
operator|.
name|error
argument_list|(
literal|"Could not serialize REPL log: {}"
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


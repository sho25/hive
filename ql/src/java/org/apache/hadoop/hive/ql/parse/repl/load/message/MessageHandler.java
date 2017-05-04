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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|ql
operator|.
name|parse
operator|.
name|SemanticException
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
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|DumpMetaData
import|;
end_import

begin_interface
specifier|public
interface|interface
name|MessageHandler
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|handle
parameter_list|(
name|Context
name|withinContext
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|readEntities
parameter_list|()
function_decl|;
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|writeEntities
parameter_list|()
function_decl|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|tablesUpdated
parameter_list|()
function_decl|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|databasesUpdated
parameter_list|()
function_decl|;
class|class
name|Context
block|{
specifier|final
name|String
name|dbName
decl_stmt|,
name|tableName
decl_stmt|,
name|location
decl_stmt|;
specifier|final
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|precursor
decl_stmt|;
name|DumpMetaData
name|dmd
decl_stmt|;
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|final
name|Hive
name|db
decl_stmt|;
specifier|final
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
name|Context
name|nestedContext
decl_stmt|;
specifier|final
name|Logger
name|log
decl_stmt|;
specifier|public
name|Context
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|location
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|precursor
parameter_list|,
name|DumpMetaData
name|dmd
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|Hive
name|db
parameter_list|,
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
name|Context
name|nestedContext
parameter_list|,
name|Logger
name|log
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|precursor
operator|=
name|precursor
expr_stmt|;
name|this
operator|.
name|dmd
operator|=
name|dmd
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|nestedContext
operator|=
name|nestedContext
expr_stmt|;
name|this
operator|.
name|log
operator|=
name|log
expr_stmt|;
block|}
specifier|public
name|Context
parameter_list|(
name|Context
name|other
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|other
operator|.
name|location
expr_stmt|;
name|this
operator|.
name|precursor
operator|=
name|other
operator|.
name|precursor
expr_stmt|;
name|this
operator|.
name|dmd
operator|=
name|other
operator|.
name|dmd
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|other
operator|.
name|hiveConf
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|other
operator|.
name|db
expr_stmt|;
name|this
operator|.
name|nestedContext
operator|=
name|other
operator|.
name|nestedContext
expr_stmt|;
name|this
operator|.
name|log
operator|=
name|other
operator|.
name|log
expr_stmt|;
block|}
name|boolean
name|isTableNameEmpty
parameter_list|()
block|{
return|return
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|tableName
argument_list|)
return|;
block|}
name|boolean
name|isDbNameEmpty
parameter_list|()
block|{
return|return
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|dbName
argument_list|)
return|;
block|}
block|}
block|}
end_interface

end_unit


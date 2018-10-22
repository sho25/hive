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
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|load
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
name|metastore
operator|.
name|api
operator|.
name|NoSuchObjectException
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
name|SQLForeignKey
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
name|SQLNotNullConstraint
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
name|SQLPrimaryKey
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
name|SQLUniqueConstraint
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
name|messaging
operator|.
name|AddForeignKeyMessage
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
name|messaging
operator|.
name|AddNotNullConstraintMessage
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
name|messaging
operator|.
name|AddPrimaryKeyMessage
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
name|messaging
operator|.
name|AddUniqueConstraintMessage
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
name|messaging
operator|.
name|MessageDeserializer
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
name|messaging
operator|.
name|json
operator|.
name|JSONMessageEncoder
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
name|ErrorMsg
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
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|ConstraintEvent
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
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|util
operator|.
name|Context
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
name|repl
operator|.
name|util
operator|.
name|TaskTracker
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
name|EximUtil
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
name|DumpType
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
name|message
operator|.
name|AddForeignKeyHandler
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
name|message
operator|.
name|AddNotNullConstraintHandler
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
name|message
operator|.
name|AddPrimaryKeyHandler
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
name|message
operator|.
name|AddUniqueConstraintHandler
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
name|message
operator|.
name|MessageHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
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
name|net
operator|.
name|URI
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
name|List
import|;
end_import

begin_import
import|import static
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
name|BaseSemanticAnalyzer
operator|.
name|stripQuotes
import|;
end_import

begin_class
specifier|public
class|class
name|LoadConstraint
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
name|LoadFunction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Context
name|context
decl_stmt|;
specifier|private
specifier|final
name|ConstraintEvent
name|event
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|private
specifier|final
name|TaskTracker
name|tracker
decl_stmt|;
specifier|private
specifier|final
name|MessageDeserializer
name|deserializer
init|=
name|JSONMessageEncoder
operator|.
name|getInstance
argument_list|()
operator|.
name|getDeserializer
argument_list|()
decl_stmt|;
specifier|public
name|LoadConstraint
parameter_list|(
name|Context
name|context
parameter_list|,
name|ConstraintEvent
name|event
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|TaskTracker
name|existingTracker
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
name|this
operator|.
name|dbNameToLoadIn
operator|=
name|dbNameToLoadIn
expr_stmt|;
name|this
operator|.
name|tracker
operator|=
operator|new
name|TaskTracker
argument_list|(
name|existingTracker
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
name|URI
name|fromURI
init|=
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|,
name|stripQuotes
argument_list|(
name|event
operator|.
name|rootDir
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|fromPath
init|=
operator|new
name|Path
argument_list|(
name|fromURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|fromPath
operator|.
name|toUri
argument_list|()
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|JSONObject
name|json
init|=
operator|new
name|JSONObject
argument_list|(
name|EximUtil
operator|.
name|readAsString
argument_list|(
name|fs
argument_list|,
name|fromPath
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|pksString
init|=
name|json
operator|.
name|getString
argument_list|(
literal|"pks"
argument_list|)
decl_stmt|;
name|String
name|fksString
init|=
name|json
operator|.
name|getString
argument_list|(
literal|"fks"
argument_list|)
decl_stmt|;
name|String
name|uksString
init|=
name|json
operator|.
name|getString
argument_list|(
literal|"uks"
argument_list|)
decl_stmt|;
name|String
name|nnsString
init|=
name|json
operator|.
name|getString
argument_list|(
literal|"nns"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|pksString
operator|!=
literal|null
operator|&&
operator|!
name|pksString
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|isPrimaryKeysAlreadyLoaded
argument_list|(
name|pksString
argument_list|)
condition|)
block|{
name|AddPrimaryKeyHandler
name|pkHandler
init|=
operator|new
name|AddPrimaryKeyHandler
argument_list|()
decl_stmt|;
name|DumpMetaData
name|pkDumpMetaData
init|=
operator|new
name|DumpMetaData
argument_list|(
name|fromPath
argument_list|,
name|DumpType
operator|.
name|EVENT_ADD_PRIMARYKEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|pkDumpMetaData
operator|.
name|setPayload
argument_list|(
name|pksString
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|pkHandler
operator|.
name|handle
argument_list|(
operator|new
name|MessageHandler
operator|.
name|Context
argument_list|(
name|dbNameToLoadIn
argument_list|,
literal|null
argument_list|,
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
name|pkDumpMetaData
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|,
name|context
operator|.
name|nestedContext
argument_list|,
name|LOG
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|uksString
operator|!=
literal|null
operator|&&
operator|!
name|uksString
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|isUniqueConstraintsAlreadyLoaded
argument_list|(
name|uksString
argument_list|)
condition|)
block|{
name|AddUniqueConstraintHandler
name|ukHandler
init|=
operator|new
name|AddUniqueConstraintHandler
argument_list|()
decl_stmt|;
name|DumpMetaData
name|ukDumpMetaData
init|=
operator|new
name|DumpMetaData
argument_list|(
name|fromPath
argument_list|,
name|DumpType
operator|.
name|EVENT_ADD_UNIQUECONSTRAINT
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|ukDumpMetaData
operator|.
name|setPayload
argument_list|(
name|uksString
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|ukHandler
operator|.
name|handle
argument_list|(
operator|new
name|MessageHandler
operator|.
name|Context
argument_list|(
name|dbNameToLoadIn
argument_list|,
literal|null
argument_list|,
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
name|ukDumpMetaData
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|,
name|context
operator|.
name|nestedContext
argument_list|,
name|LOG
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nnsString
operator|!=
literal|null
operator|&&
operator|!
name|nnsString
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|isNotNullConstraintsAlreadyLoaded
argument_list|(
name|nnsString
argument_list|)
condition|)
block|{
name|AddNotNullConstraintHandler
name|nnHandler
init|=
operator|new
name|AddNotNullConstraintHandler
argument_list|()
decl_stmt|;
name|DumpMetaData
name|nnDumpMetaData
init|=
operator|new
name|DumpMetaData
argument_list|(
name|fromPath
argument_list|,
name|DumpType
operator|.
name|EVENT_ADD_NOTNULLCONSTRAINT
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|nnDumpMetaData
operator|.
name|setPayload
argument_list|(
name|nnsString
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|nnHandler
operator|.
name|handle
argument_list|(
operator|new
name|MessageHandler
operator|.
name|Context
argument_list|(
name|dbNameToLoadIn
argument_list|,
literal|null
argument_list|,
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
name|nnDumpMetaData
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|,
name|context
operator|.
name|nestedContext
argument_list|,
name|LOG
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fksString
operator|!=
literal|null
operator|&&
operator|!
name|fksString
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|isForeignKeysAlreadyLoaded
argument_list|(
name|fksString
argument_list|)
condition|)
block|{
name|AddForeignKeyHandler
name|fkHandler
init|=
operator|new
name|AddForeignKeyHandler
argument_list|()
decl_stmt|;
name|DumpMetaData
name|fkDumpMetaData
init|=
operator|new
name|DumpMetaData
argument_list|(
name|fromPath
argument_list|,
name|DumpType
operator|.
name|EVENT_ADD_FOREIGNKEY
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|fkDumpMetaData
operator|.
name|setPayload
argument_list|(
name|fksString
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|fkHandler
operator|.
name|handle
argument_list|(
operator|new
name|MessageHandler
operator|.
name|Context
argument_list|(
name|dbNameToLoadIn
argument_list|,
literal|null
argument_list|,
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
name|fkDumpMetaData
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|,
name|context
operator|.
name|nestedContext
argument_list|,
name|LOG
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tasks
operator|.
name|forEach
argument_list|(
name|tracker
operator|::
name|addTask
argument_list|)
expr_stmt|;
return|return
name|tracker
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|isPrimaryKeysAlreadyLoaded
parameter_list|(
name|String
name|pksMsgString
parameter_list|)
throws|throws
name|Exception
block|{
name|AddPrimaryKeyMessage
name|msg
init|=
name|deserializer
operator|.
name|getAddPrimaryKeyMessage
argument_list|(
name|pksMsgString
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SQLPrimaryKey
argument_list|>
name|pksInMsg
init|=
name|msg
operator|.
name|getPrimaryKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|pksInMsg
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
name|dbName
init|=
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|?
name|pksInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_db
argument_list|()
else|:
name|dbNameToLoadIn
decl_stmt|;
name|List
argument_list|<
name|SQLPrimaryKey
argument_list|>
name|pks
decl_stmt|;
try|try
block|{
name|pks
operator|=
name|context
operator|.
name|hiveDb
operator|.
name|getPrimaryKeyList
argument_list|(
name|dbName
argument_list|,
name|pksInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_name
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
operator|(
name|pks
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|pks
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|private
name|boolean
name|isForeignKeysAlreadyLoaded
parameter_list|(
name|String
name|fksMsgString
parameter_list|)
throws|throws
name|Exception
block|{
name|AddForeignKeyMessage
name|msg
init|=
name|deserializer
operator|.
name|getAddForeignKeyMessage
argument_list|(
name|fksMsgString
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SQLForeignKey
argument_list|>
name|fksInMsg
init|=
name|msg
operator|.
name|getForeignKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|fksInMsg
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
name|dbName
init|=
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|?
name|fksInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFktable_db
argument_list|()
else|:
name|dbNameToLoadIn
decl_stmt|;
name|List
argument_list|<
name|SQLForeignKey
argument_list|>
name|fks
decl_stmt|;
try|try
block|{
name|fks
operator|=
name|context
operator|.
name|hiveDb
operator|.
name|getForeignKeyList
argument_list|(
name|dbName
argument_list|,
name|fksInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFktable_name
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
operator|(
name|fks
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|fks
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|private
name|boolean
name|isUniqueConstraintsAlreadyLoaded
parameter_list|(
name|String
name|uksMsgString
parameter_list|)
throws|throws
name|Exception
block|{
name|AddUniqueConstraintMessage
name|msg
init|=
name|deserializer
operator|.
name|getAddUniqueConstraintMessage
argument_list|(
name|uksMsgString
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SQLUniqueConstraint
argument_list|>
name|uksInMsg
init|=
name|msg
operator|.
name|getUniqueConstraints
argument_list|()
decl_stmt|;
if|if
condition|(
name|uksInMsg
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
name|dbName
init|=
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|?
name|uksInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_db
argument_list|()
else|:
name|dbNameToLoadIn
decl_stmt|;
name|List
argument_list|<
name|SQLUniqueConstraint
argument_list|>
name|uks
decl_stmt|;
try|try
block|{
name|uks
operator|=
name|context
operator|.
name|hiveDb
operator|.
name|getUniqueConstraintList
argument_list|(
name|dbName
argument_list|,
name|uksInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_name
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
operator|(
name|uks
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|uks
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|private
name|boolean
name|isNotNullConstraintsAlreadyLoaded
parameter_list|(
name|String
name|nnsMsgString
parameter_list|)
throws|throws
name|Exception
block|{
name|AddNotNullConstraintMessage
name|msg
init|=
name|deserializer
operator|.
name|getAddNotNullConstraintMessage
argument_list|(
name|nnsMsgString
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SQLNotNullConstraint
argument_list|>
name|nnsInMsg
init|=
name|msg
operator|.
name|getNotNullConstraints
argument_list|()
decl_stmt|;
if|if
condition|(
name|nnsInMsg
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
name|dbName
init|=
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|?
name|nnsInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_db
argument_list|()
else|:
name|dbNameToLoadIn
decl_stmt|;
name|List
argument_list|<
name|SQLNotNullConstraint
argument_list|>
name|nns
decl_stmt|;
try|try
block|{
name|nns
operator|=
name|context
operator|.
name|hiveDb
operator|.
name|getNotNullConstraintList
argument_list|(
name|dbName
argument_list|,
name|nnsInMsg
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTable_name
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
operator|(
name|nns
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|nns
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
block|}
end_class

end_unit


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
name|dump
package|;
end_package

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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|NotificationEvent
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
name|io
operator|.
name|AcidUtils
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
name|parse
operator|.
name|ReplicationSpec
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
name|SemanticAnalyzer
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
name|io
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Collections2
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
name|DataOutputStream
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
name|Collection
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
name|UUID
import|;
end_import

begin_class
specifier|public
class|class
name|Utils
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|Utils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BOOTSTRAP_DUMP_STATE_KEY_PREFIX
init|=
literal|"bootstrap.dump.state."
decl_stmt|;
specifier|public
enum|enum
name|ReplDumpState
block|{
name|IDLE
block|,
name|ACTIVE
block|}
specifier|public
specifier|static
name|void
name|writeOutput
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|,
name|Path
name|outputFile
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|DataOutputStream
name|outStream
init|=
literal|null
decl_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|outputFile
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|outStream
operator|=
name|fs
operator|.
name|create
argument_list|(
name|outputFile
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|writeBytes
argument_list|(
operator|(
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|==
literal|null
condition|?
name|Utilities
operator|.
name|nullStringOutput
else|:
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|values
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|outStream
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|writeBytes
argument_list|(
operator|(
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|?
name|Utilities
operator|.
name|nullStringOutput
else|:
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
argument_list|)
expr_stmt|;
block|}
name|outStream
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|outStream
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|Iterable
argument_list|<
name|?
extends|extends
name|String
argument_list|>
name|matchesDb
parameter_list|(
name|Hive
name|db
parameter_list|,
name|String
name|dbPattern
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|dbPattern
operator|==
literal|null
condition|)
block|{
return|return
name|db
operator|.
name|getAllDatabases
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|db
operator|.
name|getDatabasesByPattern
argument_list|(
name|dbPattern
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|Iterable
argument_list|<
name|?
extends|extends
name|String
argument_list|>
name|matchesTbl
parameter_list|(
name|Hive
name|db
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblPattern
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|tblPattern
operator|==
literal|null
condition|)
block|{
return|return
name|getAllTables
argument_list|(
name|db
argument_list|,
name|dbName
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|db
operator|.
name|getTablesByPattern
argument_list|(
name|dbName
argument_list|,
name|tblPattern
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|Collection
argument_list|<
name|String
argument_list|>
name|getAllTables
parameter_list|(
name|Hive
name|db
parameter_list|,
name|String
name|dbName
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|Collections2
operator|.
name|filter
argument_list|(
name|db
operator|.
name|getAllTables
argument_list|(
name|dbName
argument_list|)
argument_list|,
name|tableName
lambda|->
block|{
assert|assert
name|tableName
operator|!=
literal|null
assert|;
return|return
operator|!
name|tableName
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
name|SemanticAnalyzer
operator|.
name|VALUES_TMP_TABLE_NAME_PREFIX
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|setDbBootstrapDumpState
parameter_list|(
name|Hive
name|hiveDb
parameter_list|,
name|String
name|dbName
parameter_list|)
throws|throws
name|HiveException
block|{
name|Database
name|database
init|=
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|database
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newParams
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|uniqueKey
init|=
name|BOOTSTRAP_DUMP_STATE_KEY_PREFIX
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|newParams
operator|.
name|put
argument_list|(
name|uniqueKey
argument_list|,
name|ReplDumpState
operator|.
name|ACTIVE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|database
operator|.
name|getParameters
argument_list|()
decl_stmt|;
comment|// if both old params are not null, merge them
if|if
condition|(
name|params
operator|!=
literal|null
condition|)
block|{
name|params
operator|.
name|putAll
argument_list|(
name|newParams
argument_list|)
expr_stmt|;
name|database
operator|.
name|setParameters
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// if one of them is null, replace the old params with the new one
name|database
operator|.
name|setParameters
argument_list|(
name|newParams
argument_list|)
expr_stmt|;
block|}
name|hiveDb
operator|.
name|alterDatabase
argument_list|(
name|dbName
argument_list|,
name|database
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"REPL DUMP:: Set property for Database: {}, Property: {}, Value: {}"
argument_list|,
name|dbName
argument_list|,
name|uniqueKey
argument_list|,
name|Utils
operator|.
name|ReplDumpState
operator|.
name|ACTIVE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|uniqueKey
return|;
block|}
specifier|public
specifier|static
name|void
name|resetDbBootstrapDumpState
parameter_list|(
name|Hive
name|hiveDb
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|uniqueKey
parameter_list|)
throws|throws
name|HiveException
block|{
name|Database
name|database
init|=
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|database
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|database
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|params
operator|!=
literal|null
operator|)
operator|&&
name|params
operator|.
name|containsKey
argument_list|(
name|uniqueKey
argument_list|)
condition|)
block|{
name|params
operator|.
name|remove
argument_list|(
name|uniqueKey
argument_list|)
expr_stmt|;
name|database
operator|.
name|setParameters
argument_list|(
name|params
argument_list|)
expr_stmt|;
name|hiveDb
operator|.
name|alterDatabase
argument_list|(
name|dbName
argument_list|,
name|database
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"REPL DUMP:: Reset property for Database: {}, Property: {}"
argument_list|,
name|dbName
argument_list|,
name|uniqueKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isBootstrapDumpInProgress
parameter_list|(
name|Hive
name|hiveDb
parameter_list|,
name|String
name|dbName
parameter_list|)
throws|throws
name|HiveException
block|{
name|Database
name|database
init|=
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|database
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|database
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|String
name|key
range|:
name|params
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|BOOTSTRAP_DUMP_STATE_KEY_PREFIX
argument_list|)
operator|&&
name|params
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|equals
argument_list|(
name|ReplDumpState
operator|.
name|ACTIVE
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * validates if a table can be exported, similar to EximUtil.shouldExport with few replication    * specific checks.    */
specifier|public
specifier|static
name|Boolean
name|shouldReplicate
parameter_list|(
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Table
name|tableHandle
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
if|if
condition|(
name|replicationSpec
operator|==
literal|null
condition|)
block|{
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|replicationSpec
operator|.
name|isNoop
argument_list|()
operator|||
name|tableHandle
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|tableHandle
operator|.
name|isNonNative
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|boolean
name|isAcidTable
init|=
name|AcidUtils
operator|.
name|isAcidTable
argument_list|(
name|tableHandle
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAcidTable
condition|)
block|{
return|return
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_DUMP_INCLUDE_ACID_TABLES
argument_list|)
return|;
block|}
return|return
operator|!
name|tableHandle
operator|.
name|isTemporary
argument_list|()
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|boolean
name|shouldReplicate
parameter_list|(
name|NotificationEvent
name|tableForEvent
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|Table
name|table
decl_stmt|;
try|try
block|{
name|table
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tableForEvent
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tableForEvent
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"error while getting table info for"
operator|+
name|tableForEvent
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tableForEvent
operator|.
name|getTableName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
name|shouldReplicate
argument_list|(
name|replicationSpec
argument_list|,
name|table
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
block|}
end_class

end_unit


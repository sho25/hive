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
name|ddl
operator|.
name|table
operator|.
name|creation
package|;
end_package

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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
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
name|HashSet
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
name|StatsSetupConst
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
name|PartitionManagementTask
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
name|TableType
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
name|utils
operator|.
name|MetaStoreUtils
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
name|ddl
operator|.
name|DDLOperation
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
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|SerDeSpec
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|AnnotationUtils
import|;
end_import

begin_comment
comment|/**  * Operation process of creating a table like an existing one.  */
end_comment

begin_class
specifier|public
class|class
name|CreateTableLikeOperation
extends|extends
name|DDLOperation
argument_list|<
name|CreateTableLikeDesc
argument_list|>
block|{
specifier|public
name|CreateTableLikeOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|CreateTableLikeDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// Get the existing table
name|Table
name|oldTable
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getLikeTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|tbl
decl_stmt|;
if|if
condition|(
name|oldTable
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|||
name|oldTable
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|MATERIALIZED_VIEW
condition|)
block|{
name|tbl
operator|=
name|createViewLikeTable
argument_list|(
name|oldTable
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tbl
operator|=
name|createTableLikeTable
argument_list|(
name|oldTable
argument_list|)
expr_stmt|;
block|}
comment|// If location is specified - ensure that it is a full qualified name
if|if
condition|(
name|CreateTableOperation
operator|.
name|doesTableNeedLocation
argument_list|(
name|tbl
argument_list|)
condition|)
block|{
name|CreateTableOperation
operator|.
name|makeLocationQualified
argument_list|(
name|tbl
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|desc
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
operator|&&
operator|!
name|tbl
operator|.
name|isPartitioned
argument_list|()
operator|&&
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTATSAUTOGATHER
argument_list|)
condition|)
block|{
name|StatsSetupConst
operator|.
name|setStatsStateForCreateTable
argument_list|(
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|,
name|MetaStoreUtils
operator|.
name|getColumnNames
argument_list|(
name|tbl
operator|.
name|getCols
argument_list|()
argument_list|)
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
comment|// create the table
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|,
name|desc
operator|.
name|getIfNotExists
argument_list|()
argument_list|)
expr_stmt|;
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|tbl
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|Table
name|createViewLikeTable
parameter_list|(
name|Table
name|oldTable
parameter_list|)
throws|throws
name|HiveException
block|{
name|Table
name|table
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|newTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getTblProps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|putAll
argument_list|(
name|desc
operator|.
name|getTblProps
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|isExternal
argument_list|()
condition|)
block|{
name|setExternalProperties
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|setFields
argument_list|(
name|oldTable
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setPartCols
argument_list|(
name|oldTable
operator|.
name|getPartCols
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getDefaultSerdeProps
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|desc
operator|.
name|getDefaultSerdeProps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|table
operator|.
name|setSerdeParam
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|setStorage
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
specifier|private
name|Table
name|createTableLikeTable
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|HiveException
block|{
name|String
index|[]
name|names
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|names
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|table
operator|.
name|setOwner
argument_list|(
name|SessionState
operator|.
name|getUserFromAuthenticator
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|setDataLocation
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|table
operator|.
name|unsetDataLocation
argument_list|()
expr_stmt|;
block|}
name|setTableParameters
argument_list|(
name|table
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|isUserStorageFormat
argument_list|()
condition|)
block|{
name|setStorage
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|setTemporary
argument_list|(
name|desc
operator|.
name|isTemporary
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|unsetId
argument_list|()
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|isExternal
argument_list|()
condition|)
block|{
name|setExternalProperties
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|remove
argument_list|(
literal|"EXTERNAL"
argument_list|)
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
specifier|private
name|void
name|setTableParameters
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|HiveException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|retainer
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|Deserializer
argument_list|>
name|serdeClass
decl_stmt|;
try|try
block|{
name|serdeClass
operator|=
name|tbl
operator|.
name|getDeserializerClass
argument_list|()
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
comment|// We should copy only those table parameters that are specified in the config.
name|SerDeSpec
name|spec
init|=
name|AnnotationUtils
operator|.
name|getAnnotation
argument_list|(
name|serdeClass
argument_list|,
name|SerDeSpec
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// for non-native table, property storage_handler should be retained
name|retainer
operator|.
name|add
argument_list|(
name|META_TABLE_STORAGE
argument_list|)
expr_stmt|;
if|if
condition|(
name|spec
operator|!=
literal|null
operator|&&
name|spec
operator|.
name|schemaProps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|retainer
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|spec
operator|.
name|schemaProps
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|paramsStr
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DDL_CTL_PARAMETERS_WHITELIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|paramsStr
operator|!=
literal|null
condition|)
block|{
name|retainer
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|paramsStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
name|tbl
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|retainer
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|params
operator|.
name|keySet
argument_list|()
operator|.
name|retainAll
argument_list|(
name|retainer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|params
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|desc
operator|.
name|getTblProps
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|params
operator|.
name|putAll
argument_list|(
name|desc
operator|.
name|getTblProps
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setStorage
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|HiveException
block|{
name|table
operator|.
name|setInputFormatClass
argument_list|(
name|desc
operator|.
name|getDefaultInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setOutputFormatClass
argument_list|(
name|desc
operator|.
name|getDefaultOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|setInputFormat
argument_list|(
name|table
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|setOutputFormat
argument_list|(
name|table
operator|.
name|getOutputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getDefaultSerName
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Default to LazySimpleSerDe for table {}"
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setSerializationLib
argument_list|(
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// let's validate that the serde exists
name|DDLUtils
operator|.
name|validateSerDe
argument_list|(
name|desc
operator|.
name|getDefaultSerName
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|table
operator|.
name|setSerializationLib
argument_list|(
name|desc
operator|.
name|getDefaultSerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setExternalProperties
parameter_list|(
name|Table
name|tbl
parameter_list|)
block|{
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"TRUE"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
argument_list|)
expr_stmt|;
comment|// if the partition discovery table property is already defined don't change it
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
operator|&&
name|tbl
operator|.
name|getProperty
argument_list|(
name|PartitionManagementTask
operator|.
name|DISCOVER_PARTITIONS_TBLPROPERTY
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// partition discovery is on by default if it already doesn't exist
name|tbl
operator|.
name|setProperty
argument_list|(
name|PartitionManagementTask
operator|.
name|DISCOVER_PARTITIONS_TBLPROPERTY
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


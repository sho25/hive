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
name|metastore
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
name|Iterator
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
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
name|AlreadyExistsException
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
name|FieldSchema
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
name|InvalidObjectException
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
name|InvalidOperationException
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
name|MetaException
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
name|Partition
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
import|;
end_import

begin_comment
comment|/**  * Hive specific implementation of alter  */
end_comment

begin_class
specifier|public
class|class
name|HiveAlterHandler
implements|implements
name|AlterHandler
block|{
specifier|protected
name|Configuration
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveAlterHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|hiveConf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|void
name|alterTable
parameter_list|(
name|RawStore
name|msdb
parameter_list|,
name|Warehouse
name|wh
parameter_list|,
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|,
name|Table
name|newt
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
if|if
condition|(
name|newt
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"New table is invalid: "
operator|+
name|newt
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|MetaStoreUtils
operator|.
name|validateName
argument_list|(
name|newt
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|||
operator|!
name|MetaStoreUtils
operator|.
name|validateTblColumns
argument_list|(
name|newt
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
name|newt
operator|.
name|getTableName
argument_list|()
operator|+
literal|" is not a valid object name"
argument_list|)
throw|;
block|}
name|Path
name|srcPath
init|=
literal|null
decl_stmt|;
name|FileSystem
name|srcFs
init|=
literal|null
decl_stmt|;
name|Path
name|destPath
init|=
literal|null
decl_stmt|;
name|FileSystem
name|destFs
init|=
literal|null
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|String
name|oldTblLoc
init|=
literal|null
decl_stmt|;
name|String
name|newTblLoc
init|=
literal|null
decl_stmt|;
name|boolean
name|moveData
init|=
literal|false
decl_stmt|;
name|boolean
name|rename
init|=
literal|false
decl_stmt|;
name|Table
name|oldt
init|=
literal|null
decl_stmt|;
try|try
block|{
name|msdb
operator|.
name|openTransaction
argument_list|()
expr_stmt|;
name|name
operator|=
name|name
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|dbname
operator|=
name|dbname
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
comment|// check if table with the new name already exists
if|if
condition|(
operator|!
name|newt
operator|.
name|getTableName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
operator|||
operator|!
name|newt
operator|.
name|getDbName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|dbname
argument_list|)
condition|)
block|{
if|if
condition|(
name|msdb
operator|.
name|getTable
argument_list|(
name|newt
operator|.
name|getDbName
argument_list|()
argument_list|,
name|newt
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"new table "
operator|+
name|newt
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|newt
operator|.
name|getTableName
argument_list|()
operator|+
literal|" already exists"
argument_list|)
throw|;
block|}
name|rename
operator|=
literal|true
expr_stmt|;
block|}
comment|// get old table
name|oldt
operator|=
name|msdb
operator|.
name|getTable
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|oldt
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"table "
operator|+
name|newt
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|newt
operator|.
name|getTableName
argument_list|()
operator|+
literal|" doesn't exist"
argument_list|)
throw|;
block|}
comment|//check that partition keys have not changed, except for virtual views
comment|//however, allow the partition comments to change
name|boolean
name|partKeysPartiallyEqual
init|=
name|checkPartialPartKeysEqual
argument_list|(
name|oldt
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|newt
operator|.
name|getPartitionKeys
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|oldt
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|oldt
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
name|newt
operator|.
name|getPartitionKeys
argument_list|()
operator|.
name|size
argument_list|()
operator|||
operator|!
name|partKeysPartiallyEqual
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"partition keys can not be changed."
argument_list|)
throw|;
block|}
block|}
comment|// if this alter is a rename, the table is not a virtual view, the user
comment|// didn't change the default location (or new location is empty), and
comment|// table is not an external table, that means useris asking metastore to
comment|// move data to the new location corresponding to the new name
if|if
condition|(
name|rename
operator|&&
operator|!
name|oldt
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|.
name|toString
argument_list|()
argument_list|)
operator|&&
operator|(
name|oldt
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|.
name|compareTo
argument_list|(
name|newt
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
operator|==
literal|0
operator|||
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|newt
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
operator|)
operator|&&
operator|!
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|oldt
argument_list|)
condition|)
block|{
comment|// that means user is asking metastore to move data to new location
comment|// corresponding to the new name
comment|// get new location
name|newTblLoc
operator|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|msdb
operator|.
name|getDatabase
argument_list|(
name|newt
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|newt
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|Path
name|newTblPath
init|=
name|constructRenamedPath
argument_list|(
operator|new
name|Path
argument_list|(
name|newTblLoc
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|newt
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|newTblLoc
operator|=
name|newTblPath
operator|.
name|toString
argument_list|()
expr_stmt|;
name|newt
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|newTblLoc
argument_list|)
expr_stmt|;
name|oldTblLoc
operator|=
name|oldt
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
expr_stmt|;
name|moveData
operator|=
literal|true
expr_stmt|;
comment|// check that destination does not exist otherwise we will be
comment|// overwriting data
name|srcPath
operator|=
operator|new
name|Path
argument_list|(
name|oldTblLoc
argument_list|)
expr_stmt|;
name|srcFs
operator|=
name|wh
operator|.
name|getFs
argument_list|(
name|srcPath
argument_list|)
expr_stmt|;
name|destPath
operator|=
operator|new
name|Path
argument_list|(
name|newTblLoc
argument_list|)
expr_stmt|;
name|destFs
operator|=
name|wh
operator|.
name|getFs
argument_list|(
name|destPath
argument_list|)
expr_stmt|;
comment|// check that src and dest are on the same file system
if|if
condition|(
name|srcFs
operator|!=
name|destFs
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"table new location "
operator|+
name|destPath
operator|+
literal|" is on a different file system than the old location "
operator|+
name|srcPath
operator|+
literal|". This operation is not supported"
argument_list|)
throw|;
block|}
try|try
block|{
name|srcFs
operator|.
name|exists
argument_list|(
name|srcPath
argument_list|)
expr_stmt|;
comment|// check that src exists and also checks
comment|// permissions necessary
if|if
condition|(
name|destFs
operator|.
name|exists
argument_list|(
name|destPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"New location for this table "
operator|+
name|newt
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|newt
operator|.
name|getTableName
argument_list|()
operator|+
literal|" already exists : "
operator|+
name|destPath
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Warehouse
operator|.
name|closeFs
argument_list|(
name|srcFs
argument_list|)
expr_stmt|;
name|Warehouse
operator|.
name|closeFs
argument_list|(
name|destFs
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to access new location "
operator|+
name|destPath
operator|+
literal|" for table "
operator|+
name|newt
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|newt
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
comment|// also the location field in partition
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|msdb
operator|.
name|getPartitions
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|part
range|:
name|parts
control|)
block|{
name|String
name|oldPartLoc
init|=
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
name|Path
name|oldPartLocPath
init|=
operator|new
name|Path
argument_list|(
name|oldPartLoc
argument_list|)
decl_stmt|;
name|String
name|oldTblLocPath
init|=
operator|new
name|Path
argument_list|(
name|oldTblLoc
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|newTblLocPath
init|=
operator|new
name|Path
argument_list|(
name|newTblLoc
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldPartLoc
operator|.
name|contains
argument_list|(
name|oldTblLocPath
argument_list|)
condition|)
block|{
name|Path
name|newPartLocPath
init|=
literal|null
decl_stmt|;
name|URI
name|oldUri
init|=
name|oldPartLocPath
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|String
name|newPath
init|=
name|oldUri
operator|.
name|getPath
argument_list|()
operator|.
name|replace
argument_list|(
name|oldTblLocPath
argument_list|,
name|newTblLocPath
argument_list|)
decl_stmt|;
name|newPartLocPath
operator|=
operator|new
name|Path
argument_list|(
name|oldUri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|oldUri
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|newPath
argument_list|)
expr_stmt|;
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|newPartLocPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|msdb
operator|.
name|alterPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|part
operator|.
name|getValues
argument_list|()
argument_list|,
name|part
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// now finally call alter table
name|msdb
operator|.
name|alterTable
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|newt
argument_list|)
expr_stmt|;
comment|// commit the changes
name|success
operator|=
name|msdb
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidObjectException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to change partition or table."
operator|+
literal|" Check metastore logs for detailed stack."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to change partition or table. Database "
operator|+
name|dbname
operator|+
literal|" does not exist"
operator|+
literal|" Check metastore logs for detailed stack."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|msdb
operator|.
name|rollbackTransaction
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|success
operator|&&
name|moveData
condition|)
block|{
comment|// change the file name in hdfs
comment|// check that src exists otherwise there is no need to copy the data
try|try
block|{
if|if
condition|(
name|srcFs
operator|.
name|exists
argument_list|(
name|srcPath
argument_list|)
condition|)
block|{
comment|// rename the src to destination
name|srcFs
operator|.
name|rename
argument_list|(
name|srcPath
argument_list|,
name|destPath
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|boolean
name|revertMetaDataTransaction
init|=
literal|false
decl_stmt|;
try|try
block|{
name|msdb
operator|.
name|openTransaction
argument_list|()
expr_stmt|;
name|msdb
operator|.
name|alterTable
argument_list|(
name|dbname
argument_list|,
name|newt
operator|.
name|getTableName
argument_list|()
argument_list|,
name|oldt
argument_list|)
expr_stmt|;
name|revertMetaDataTransaction
operator|=
name|msdb
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Reverting metadata opeation failed During HDFS operation failed"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|revertMetaDataTransaction
condition|)
block|{
name|msdb
operator|.
name|rollbackTransaction
argument_list|()
expr_stmt|;
block|}
block|}
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to access old location "
operator|+
name|srcPath
operator|+
literal|" for table "
operator|+
name|dbname
operator|+
literal|"."
operator|+
name|name
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Committing the alter table transaction was not successful."
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Partition
name|alterPartition
parameter_list|(
specifier|final
name|RawStore
name|msdb
parameter_list|,
name|Warehouse
name|wh
parameter_list|,
specifier|final
name|String
name|dbname
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
specifier|final
name|Partition
name|new_part
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|Path
name|srcPath
init|=
literal|null
decl_stmt|;
name|Path
name|destPath
init|=
literal|null
decl_stmt|;
name|FileSystem
name|srcFs
init|=
literal|null
decl_stmt|;
name|FileSystem
name|destFs
init|=
literal|null
decl_stmt|;
name|Table
name|tbl
init|=
literal|null
decl_stmt|;
name|Partition
name|oldPart
init|=
literal|null
decl_stmt|;
name|String
name|oldPartLoc
init|=
literal|null
decl_stmt|;
name|String
name|newPartLoc
init|=
literal|null
decl_stmt|;
comment|// Set DDL time to now if not specified
if|if
condition|(
name|new_part
operator|.
name|getParameters
argument_list|()
operator|==
literal|null
operator|||
name|new_part
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|DDL_TIME
argument_list|)
operator|==
literal|null
operator|||
name|Integer
operator|.
name|parseInt
argument_list|(
name|new_part
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|DDL_TIME
argument_list|)
argument_list|)
operator|==
literal|0
condition|)
block|{
name|new_part
operator|.
name|putToParameters
argument_list|(
name|hive_metastoreConstants
operator|.
name|DDL_TIME
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//alter partition
if|if
condition|(
name|part_vals
operator|==
literal|null
operator|||
name|part_vals
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
try|try
block|{
name|oldPart
operator|=
name|msdb
operator|.
name|getPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|new_part
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|msdb
operator|.
name|alterPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|new_part
operator|.
name|getValues
argument_list|()
argument_list|,
name|new_part
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidObjectException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"alter is not possible"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|//old partition does not exist
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"alter is not possible"
argument_list|)
throw|;
block|}
return|return
name|oldPart
return|;
block|}
comment|//rename partition
try|try
block|{
name|msdb
operator|.
name|openTransaction
argument_list|()
expr_stmt|;
try|try
block|{
name|oldPart
operator|=
name|msdb
operator|.
name|getPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|part_vals
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|// this means there is no existing partition
throw|throw
operator|new
name|InvalidObjectException
argument_list|(
literal|"Unable to rename partition because old partition does not exist"
argument_list|)
throw|;
block|}
name|Partition
name|check_part
init|=
literal|null
decl_stmt|;
try|try
block|{
name|check_part
operator|=
name|msdb
operator|.
name|getPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|new_part
operator|.
name|getValues
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
comment|// this means there is no existing partition
name|check_part
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|check_part
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|AlreadyExistsException
argument_list|(
literal|"Partition already exists:"
operator|+
name|dbname
operator|+
literal|"."
operator|+
name|name
operator|+
literal|"."
operator|+
name|new_part
operator|.
name|getValues
argument_list|()
argument_list|)
throw|;
block|}
name|tbl
operator|=
name|msdb
operator|.
name|getTable
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|tbl
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidObjectException
argument_list|(
literal|"Unable to rename partition because table or database do not exist"
argument_list|)
throw|;
block|}
comment|// if the external partition is renamed, the file should not change
if|if
condition|(
name|tbl
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
name|new_part
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|oldPart
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|msdb
operator|.
name|alterPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|part_vals
argument_list|,
name|new_part
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|destPath
operator|=
operator|new
name|Path
argument_list|(
name|wh
operator|.
name|getTablePath
argument_list|(
name|msdb
operator|.
name|getDatabase
argument_list|(
name|dbname
argument_list|)
argument_list|,
name|name
argument_list|)
argument_list|,
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
argument_list|,
name|new_part
operator|.
name|getValues
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|destPath
operator|=
name|constructRenamedPath
argument_list|(
name|destPath
argument_list|,
operator|new
name|Path
argument_list|(
name|new_part
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to change partition or table. Database "
operator|+
name|dbname
operator|+
literal|" does not exist"
operator|+
literal|" Check metastore logs for detailed stack."
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|destPath
operator|!=
literal|null
condition|)
block|{
name|newPartLoc
operator|=
name|destPath
operator|.
name|toString
argument_list|()
expr_stmt|;
name|oldPartLoc
operator|=
name|oldPart
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
expr_stmt|;
name|srcPath
operator|=
operator|new
name|Path
argument_list|(
name|oldPartLoc
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"srcPath:"
operator|+
name|oldPartLoc
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"descPath:"
operator|+
name|newPartLoc
argument_list|)
expr_stmt|;
name|srcFs
operator|=
name|wh
operator|.
name|getFs
argument_list|(
name|srcPath
argument_list|)
expr_stmt|;
name|destFs
operator|=
name|wh
operator|.
name|getFs
argument_list|(
name|destPath
argument_list|)
expr_stmt|;
comment|// check that src and dest are on the same file system
if|if
condition|(
name|srcFs
operator|!=
name|destFs
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"table new location "
operator|+
name|destPath
operator|+
literal|" is on a different file system than the old location "
operator|+
name|srcPath
operator|+
literal|". This operation is not supported"
argument_list|)
throw|;
block|}
try|try
block|{
name|srcFs
operator|.
name|exists
argument_list|(
name|srcPath
argument_list|)
expr_stmt|;
comment|// check that src exists and also checks
if|if
condition|(
name|newPartLoc
operator|.
name|compareTo
argument_list|(
name|oldPartLoc
argument_list|)
operator|!=
literal|0
operator|&&
name|destFs
operator|.
name|exists
argument_list|(
name|destPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"New location for this table "
operator|+
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
operator|+
literal|" already exists : "
operator|+
name|destPath
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Warehouse
operator|.
name|closeFs
argument_list|(
name|srcFs
argument_list|)
expr_stmt|;
name|Warehouse
operator|.
name|closeFs
argument_list|(
name|destFs
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to access new location "
operator|+
name|destPath
operator|+
literal|" for partition "
operator|+
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
operator|+
literal|" "
operator|+
name|new_part
operator|.
name|getValues
argument_list|()
argument_list|)
throw|;
block|}
name|new_part
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|newPartLoc
argument_list|)
expr_stmt|;
name|msdb
operator|.
name|alterPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|part_vals
argument_list|,
name|new_part
argument_list|)
expr_stmt|;
block|}
block|}
name|success
operator|=
name|msdb
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|msdb
operator|.
name|rollbackTransaction
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|success
operator|&&
name|newPartLoc
operator|!=
literal|null
operator|&&
name|newPartLoc
operator|.
name|compareTo
argument_list|(
name|oldPartLoc
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|//rename the data directory
try|try
block|{
if|if
condition|(
name|srcFs
operator|.
name|exists
argument_list|(
name|srcPath
argument_list|)
condition|)
block|{
comment|//if destPath's parent path doesn't exist, we should mkdir it
name|Path
name|destParentPath
init|=
name|destPath
operator|.
name|getParent
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|wh
operator|.
name|mkdirs
argument_list|(
name|destParentPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to create path "
operator|+
name|destParentPath
argument_list|)
throw|;
block|}
name|srcFs
operator|.
name|rename
argument_list|(
name|srcPath
argument_list|,
name|destPath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"rename done!"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|boolean
name|revertMetaDataTransaction
init|=
literal|false
decl_stmt|;
try|try
block|{
name|msdb
operator|.
name|openTransaction
argument_list|()
expr_stmt|;
name|msdb
operator|.
name|alterPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|new_part
operator|.
name|getValues
argument_list|()
argument_list|,
name|oldPart
argument_list|)
expr_stmt|;
name|revertMetaDataTransaction
operator|=
name|msdb
operator|.
name|commitTransaction
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Reverting metadata opeation failed During HDFS operation failed"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|revertMetaDataTransaction
condition|)
block|{
name|msdb
operator|.
name|rollbackTransaction
argument_list|()
expr_stmt|;
block|}
block|}
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Unable to access old location "
operator|+
name|srcPath
operator|+
literal|" for partition "
operator|+
name|tbl
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|tbl
operator|.
name|getTableName
argument_list|()
operator|+
literal|" "
operator|+
name|part_vals
argument_list|)
throw|;
block|}
block|}
block|}
return|return
name|oldPart
return|;
block|}
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|alterPartitions
parameter_list|(
specifier|final
name|RawStore
name|msdb
parameter_list|,
name|Warehouse
name|wh
parameter_list|,
specifier|final
name|String
name|dbname
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|List
argument_list|<
name|Partition
argument_list|>
name|new_parts
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|oldParts
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|partValsList
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|Partition
name|tmpPart
range|:
name|new_parts
control|)
block|{
comment|// Set DDL time to now if not specified
if|if
condition|(
name|tmpPart
operator|.
name|getParameters
argument_list|()
operator|==
literal|null
operator|||
name|tmpPart
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|DDL_TIME
argument_list|)
operator|==
literal|null
operator|||
name|Integer
operator|.
name|parseInt
argument_list|(
name|tmpPart
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|DDL_TIME
argument_list|)
argument_list|)
operator|==
literal|0
condition|)
block|{
name|tmpPart
operator|.
name|putToParameters
argument_list|(
name|hive_metastoreConstants
operator|.
name|DDL_TIME
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Partition
name|oldTmpPart
init|=
name|msdb
operator|.
name|getPartition
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|tmpPart
operator|.
name|getValues
argument_list|()
argument_list|)
decl_stmt|;
name|oldParts
operator|.
name|add
argument_list|(
name|oldTmpPart
argument_list|)
expr_stmt|;
name|partValsList
operator|.
name|add
argument_list|(
name|tmpPart
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|msdb
operator|.
name|alterPartitions
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|,
name|partValsList
argument_list|,
name|new_parts
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidObjectException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"alter is not possible"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|//old partition does not exist
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"alter is not possible"
argument_list|)
throw|;
block|}
return|return
name|oldParts
return|;
block|}
specifier|private
name|boolean
name|checkPartialPartKeysEqual
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|oldPartKeys
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|newPartKeys
parameter_list|)
block|{
comment|//return true if both are null, or false if one is null and the other isn't
if|if
condition|(
name|newPartKeys
operator|==
literal|null
operator|||
name|oldPartKeys
operator|==
literal|null
condition|)
block|{
return|return
name|oldPartKeys
operator|==
name|newPartKeys
return|;
block|}
if|if
condition|(
name|oldPartKeys
operator|.
name|size
argument_list|()
operator|!=
name|newPartKeys
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Iterator
argument_list|<
name|FieldSchema
argument_list|>
name|oldPartKeysIter
init|=
name|oldPartKeys
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|FieldSchema
argument_list|>
name|newPartKeysIter
init|=
name|newPartKeys
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|FieldSchema
name|oldFs
decl_stmt|;
name|FieldSchema
name|newFs
decl_stmt|;
while|while
condition|(
name|oldPartKeysIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|oldFs
operator|=
name|oldPartKeysIter
operator|.
name|next
argument_list|()
expr_stmt|;
name|newFs
operator|=
name|newPartKeysIter
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|oldFs
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|newFs
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
operator|!
name|oldFs
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|newFs
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Uses the scheme and authority of the object's current location and the path constructed    * using the object's new name to construct a path for the object's new location.    */
specifier|private
name|Path
name|constructRenamedPath
parameter_list|(
name|Path
name|defaultNewPath
parameter_list|,
name|Path
name|currentPath
parameter_list|)
block|{
name|URI
name|currentUri
init|=
name|currentPath
operator|.
name|toUri
argument_list|()
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|currentUri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|currentUri
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|defaultNewPath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit


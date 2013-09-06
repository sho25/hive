begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|api
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
name|Map
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
name|common
operator|.
name|JavaUtils
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
name|PartitionEventType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
import|;
end_import

begin_comment
comment|/**  * The abstract class HCatClient containing APIs for HCatalog DDL commands.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatClient
block|{
specifier|public
enum|enum
name|DropDBMode
block|{
name|RESTRICT
block|,
name|CASCADE
block|}
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_CLIENT_IMPL_CLASS
init|=
literal|"hcat.client.impl.class"
decl_stmt|;
comment|/**      * Creates an instance of HCatClient.      *      * @param conf An instance of configuration.      * @return An instance of HCatClient.      * @throws HCatException      */
specifier|public
specifier|static
name|HCatClient
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HCatException
block|{
name|HCatClient
name|client
init|=
literal|null
decl_stmt|;
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|HCAT_CLIENT_IMPL_CLASS
argument_list|,
name|HCatClientHMSImpl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|HCatClient
argument_list|>
name|clientClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|HCatClient
operator|.
name|class
argument_list|)
decl_stmt|;
name|client
operator|=
operator|(
name|HCatClient
operator|)
name|clientClass
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"ClassNotFoundException while creating client class."
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"InstantiationException while creating client class."
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"IllegalAccessException while creating client class."
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
specifier|abstract
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Get all existing databases that match the given      * pattern. The matching occurs as per Java regular expressions      *      * @param pattern  java re pattern      * @return list of database names      * @throws HCatException      */
specifier|public
specifier|abstract
name|List
argument_list|<
name|String
argument_list|>
name|listDatabaseNamesByPattern
parameter_list|(
name|String
name|pattern
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Gets the database.      *      * @param dbName The name of the database.      * @return An instance of HCatDatabaseInfo.      * @throws HCatException      */
specifier|public
specifier|abstract
name|HCatDatabase
name|getDatabase
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Creates the database.      *      * @param dbInfo An instance of HCatCreateDBDesc.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|createDatabase
parameter_list|(
name|HCatCreateDBDesc
name|dbInfo
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Drops a database.      *      * @param dbName The name of the database to delete.      * @param ifExists Hive returns an error if the database specified does not exist,      *                 unless ifExists is set to true.      * @param mode This is set to either "restrict" or "cascade". Restrict will      *             remove the schema if all the tables are empty. Cascade removes      *             everything including data and definitions.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|dropDatabase
parameter_list|(
name|String
name|dbName
parameter_list|,
name|boolean
name|ifExists
parameter_list|,
name|DropDBMode
name|mode
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Returns all existing tables from the specified database which match the given      * pattern. The matching occurs as per Java regular expressions.      * @param dbName The name of the DB (to be searched)      * @param tablePattern The regex for the table-name      * @return list of table names      * @throws HCatException      */
specifier|public
specifier|abstract
name|List
argument_list|<
name|String
argument_list|>
name|listTableNamesByPattern
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tablePattern
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Gets the table.      *      * @param dbName The name of the database.      * @param tableName The name of the table.      * @return An instance of HCatTableInfo.      * @throws HCatException      */
specifier|public
specifier|abstract
name|HCatTable
name|getTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Creates the table.      *      * @param createTableDesc An instance of HCatCreateTableDesc class.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|createTable
parameter_list|(
name|HCatCreateTableDesc
name|createTableDesc
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Updates the Table's column schema to the specified definition.      *      * @param dbName The name of the database.      * @param tableName The name of the table.      * @param columnSchema The (new) definition of the column schema (i.e. list of fields).      *      */
specifier|public
specifier|abstract
name|void
name|updateTableSchema
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|columnSchema
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Creates the table like an existing table.      *      * @param dbName The name of the database.      * @param existingTblName The name of the existing table.      * @param newTableName The name of the new table.      * @param ifNotExists If true, then error related to already table existing is skipped.      * @param isExternal Set to "true", if table has be created at a different      *                   location other than default.      * @param location The location for the table.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|createTableLike
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|existingTblName
parameter_list|,
name|String
name|newTableName
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Drop table.      *      * @param dbName The name of the database.      * @param tableName The name of the table.      * @param ifExists Hive returns an error if the database specified does not exist,      *                 unless ifExists is set to true.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|dropTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|boolean
name|ifExists
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Renames a table.      *      * @param dbName The name of the database.      * @param oldName The name of the table to be renamed.      * @param newName The new name of the table.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|renameTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|oldName
parameter_list|,
name|String
name|newName
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Gets all the partitions.      *      * @param dbName The name of the database.      * @param tblName The name of the table.      * @return A list of partitions.      * @throws HCatException      */
specifier|public
specifier|abstract
name|List
argument_list|<
name|HCatPartition
argument_list|>
name|getPartitions
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Gets all the partitions that match the specified (and possibly partial) partition specification.      * A partial partition-specification is one where not all partition-keys have associated values. For example,      * for a table ('myDb.myTable') with 2 partition keys (dt string, region string),      * if for each dt ('20120101', '20120102', etc.) there can exist 3 regions ('us', 'uk', 'in'), then,      *  1. Complete partition spec: getPartitions('myDb', 'myTable', {dt='20120101', region='us'}) would return 1 partition.      *  2. Partial  partition spec: getPartitions('myDb', 'myTable', {dt='20120101'}) would return all 3 partitions,      *                              with dt='20120101' (i.e. region = 'us', 'uk' and 'in').      * @param dbName The name of the database.      * @param tblName The name of the table.      * @param partitionSpec The partition specification. (Need not include all partition keys.)      * @return A list of partitions.      * @throws HCatException      */
specifier|public
specifier|abstract
name|List
argument_list|<
name|HCatPartition
argument_list|>
name|getPartitions
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Gets the partition.      *      * @param dbName The database name.      * @param tableName The table name.      * @param partitionSpec The partition specification, {[col_name,value],[col_name2,value2]}. All partition-key-values      *                      must be specified.      * @return An instance of HCatPartitionInfo.      * @throws HCatException      */
specifier|public
specifier|abstract
name|HCatPartition
name|getPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Adds the partition.      *      * @param partInfo An instance of HCatAddPartitionDesc.      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|addPartition
parameter_list|(
name|HCatAddPartitionDesc
name|partInfo
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Adds a list of partitions.      *      * @param partInfoList A list of HCatAddPartitionDesc.      * @return The number of partitions added.      * @throws HCatException      */
specifier|public
specifier|abstract
name|int
name|addPartitions
parameter_list|(
name|List
argument_list|<
name|HCatAddPartitionDesc
argument_list|>
name|partInfoList
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Drops partition(s) that match the specified (and possibly partial) partition specification.      * A partial partition-specification is one where not all partition-keys have associated values. For example,      * for a table ('myDb.myTable') with 2 partition keys (dt string, region string),      * if for each dt ('20120101', '20120102', etc.) there can exist 3 regions ('us', 'uk', 'in'), then,      *  1. Complete partition spec: dropPartitions('myDb', 'myTable', {dt='20120101', region='us'}) would drop 1 partition.      *  2. Partial  partition spec: dropPartitions('myDb', 'myTable', {dt='20120101'}) would drop all 3 partitions,      *                              with dt='20120101' (i.e. region = 'us', 'uk' and 'in').      * @param dbName The database name.      * @param tableName The table name.      * @param partitionSpec The partition specification, {[col_name,value],[col_name2,value2]}.      * @param ifExists Hive returns an error if the partition specified does not exist, unless ifExists is set to true.      * @throws HCatException,ConnectionFailureException      */
specifier|public
specifier|abstract
name|void
name|dropPartitions
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|boolean
name|ifExists
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * List partitions by filter.      *      * @param dbName The database name.      * @param tblName The table name.      * @param filter The filter string,      *    for example "part1 = \"p1_abc\" and part2<= "\p2_test\"". Filtering can      *    be done only on string partition keys.      * @return list of partitions      * @throws HCatException      */
specifier|public
specifier|abstract
name|List
argument_list|<
name|HCatPartition
argument_list|>
name|listPartitionsByFilter
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|filter
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Mark partition for event.      *      * @param dbName The database name.      * @param tblName The table name.      * @param partKVs the key-values associated with the partition.      * @param eventType the event type      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|markPartitionForEvent
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partKVs
parameter_list|,
name|PartitionEventType
name|eventType
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Checks if a partition is marked for event.      *      * @param dbName the db name      * @param tblName the table name      * @param partKVs the key-values associated with the partition.      * @param eventType the event type      * @return true, if is partition marked for event      * @throws HCatException      */
specifier|public
specifier|abstract
name|boolean
name|isPartitionMarkedForEvent
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partKVs
parameter_list|,
name|PartitionEventType
name|eventType
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Gets the delegation token.      *      * @param owner the owner      * @param renewerKerberosPrincipalName the renewer kerberos principal name      * @return the delegation token      * @throws HCatException,ConnectionFailureException      */
specifier|public
specifier|abstract
name|String
name|getDelegationToken
parameter_list|(
name|String
name|owner
parameter_list|,
name|String
name|renewerKerberosPrincipalName
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Renew delegation token.      *      * @param tokenStrForm the token string      * @return the new expiration time      * @throws HCatException      */
specifier|public
specifier|abstract
name|long
name|renewDelegationToken
parameter_list|(
name|String
name|tokenStrForm
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Cancel delegation token.      *      * @param tokenStrForm the token string      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|cancelDelegationToken
parameter_list|(
name|String
name|tokenStrForm
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Retrieve Message-bus topic for a table.      *      * @param dbName The name of the DB.      * @param tableName The name of the table.      * @return Topic-name for the message-bus on which messages will be sent for the specified table.      * By default, this is set to<db-name>.<table-name>. Returns null when not set.      */
specifier|public
specifier|abstract
name|String
name|getMessageBusTopicName
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/**      * Close the hcatalog client.      *      * @throws HCatException      */
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
throws|throws
name|HCatException
function_decl|;
block|}
end_class

end_unit


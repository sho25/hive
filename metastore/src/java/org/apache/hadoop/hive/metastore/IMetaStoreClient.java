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
name|UnknownDBException
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
name|UnknownTableException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * TODO Unnecessary when the server sides for both dbstore and filestore are merged  */
end_comment

begin_interface
specifier|public
interface|interface
name|IMetaStoreClient
block|{
specifier|public
name|void
name|close
parameter_list|()
function_decl|;
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tablePattern
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|TException
throws|,
name|UnknownDBException
function_decl|;
comment|/**    * Drop the table.    * @param tableName The table to drop    * @param deleteData Should we delete the underlying data    * @throws MetaException Could not drop table properly.    * @throws UnknownTableException The table wasn't found.    * @throws TException A thrift communication error occurred    * @throws NoSuchObjectException The table wasn't found.    */
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|TException
throws|,
name|NoSuchObjectException
function_decl|;
comment|/**    * Drop the table.    * @param dbname The database for this table    * @param tableName The table to drop    * @throws MetaException Could not drop table properly.    * @throws NoSuchObjectException The table wasn't found.    * @throws TException A thrift communication error occurred    * @throws ExistingDependentsException    */
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|tableName
parameter_list|,
name|boolean
name|deleteData
parameter_list|,
name|boolean
name|ignoreUknownTab
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
function_decl|;
comment|//public void createTable(String tableName, Properties schema) throws MetaException, UnknownTableException,
comment|//  TException;
specifier|public
name|boolean
name|tableExists
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownDBException
function_decl|;
comment|/**    * Get a table object.     * @param tableName Name of the table to fetch.    * @return An object representing the table.    * @throws MetaException Could not fetch the table    * @throws TException A thrift communication error occurred     * @throws NoSuchObjectException In case the table wasn't found.    */
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
function_decl|;
comment|/**    * Get a table object.     * @param dbName The database the table is located in.    * @param tableName Name of the table to fetch.    * @return An object representing the table.    * @throws MetaException Could not fetch the table    * @throws TException A thrift communication error occurred     * @throws NoSuchObjectException In case the table wasn't found.    */
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|NoSuchObjectException
function_decl|;
comment|/**    * @param tableName    * @param dbName    * @param partVals    * @return the partition object    * @throws InvalidObjectException    * @throws AlreadyExistsException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#append_partition(java.lang.String, java.lang.String, java.util.List)    */
specifier|public
name|Partition
name|appendPartition
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
comment|/**    * Add a partition to the table.    * @param partition The partition to add    * @return The partition added    * @throws InvalidObjectException Could not find table to add to    * @throws AlreadyExistsException Partition already exists    * @throws MetaException Could not add partition    * @throws TException Thrift exception    */
specifier|public
name|Partition
name|add_partition
parameter_list|(
name|Partition
name|partition
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
comment|/**    * @param tblName    * @param dbName    * @param partVals    * @return the partition object    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#get_partition(java.lang.String, java.lang.String, java.util.List)    */
specifier|public
name|Partition
name|getPartition
parameter_list|(
name|String
name|tblName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
function_decl|;
comment|/**    * @param tbl_name    * @param db_name    * @param max_parts    * @return the list of partitions    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    */
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|listPartitions
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNames
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
function_decl|;
comment|/**    * @param tbl    * @throws AlreadyExistsException    * @throws InvalidObjectException    * @throws MetaException    * @throws NoSuchObjectException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#create_table(org.apache.hadoop.hive.metastore.api.Table)    */
specifier|public
name|void
name|createTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|AlreadyExistsException
throws|,
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|TException
function_decl|;
specifier|public
name|void
name|alter_table
parameter_list|(
name|String
name|defaultDatabaseName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
specifier|public
name|boolean
name|createDatabase
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|location_uri
parameter_list|)
throws|throws
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
specifier|public
name|boolean
name|dropDatabase
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
function_decl|;
comment|/**    * @param db_name    * @param tbl_name    * @param part_vals    * @param deleteData delete the underlying data or just delete the table in metadata    * @return true or false    * @throws NoSuchObjectException    * @throws MetaException    * @throws TException    * @see org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore.Iface#drop_partition(java.lang.String, java.lang.String, java.util.List, boolean)    */
specifier|public
name|boolean
name|dropPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|boolean
name|deleteData
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
comment|/**    * updates a partition to new partition    * @param dbName database of the old partition    * @param tblName table name of the old partition    * @param newPart new partition    * @throws InvalidOperationException if the old partition does not exist    * @throws MetaException if error in updating metadata    * @throws TException if error in communicating with metastore server    */
specifier|public
name|void
name|alter_partition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Partition
name|newPart
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
throws|,
name|TException
function_decl|;
block|}
end_interface

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|messaging
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
name|metastore
operator|.
name|api
operator|.
name|Catalog
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
name|Function
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
name|conf
operator|.
name|MetastoreConf
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
name|conf
operator|.
name|MetastoreConf
operator|.
name|ConfVars
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
name|JavaUtils
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

begin_comment
comment|/**  * Abstract Factory for the construction of HCatalog message instances.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MessageFactory
block|{
comment|// Common name constants for event messages
specifier|public
specifier|static
specifier|final
name|String
name|ADD_PARTITION_EVENT
init|=
literal|"ADD_PARTITION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALTER_PARTITION_EVENT
init|=
literal|"ALTER_PARTITION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_PARTITION_EVENT
init|=
literal|"DROP_PARTITION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_TABLE_EVENT
init|=
literal|"CREATE_TABLE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALTER_TABLE_EVENT
init|=
literal|"ALTER_TABLE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_TABLE_EVENT
init|=
literal|"DROP_TABLE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_DATABASE_EVENT
init|=
literal|"CREATE_DATABASE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALTER_DATABASE_EVENT
init|=
literal|"ALTER_DATABASE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_DATABASE_EVENT
init|=
literal|"DROP_DATABASE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INSERT_EVENT
init|=
literal|"INSERT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_FUNCTION_EVENT
init|=
literal|"CREATE_FUNCTION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_FUNCTION_EVENT
init|=
literal|"DROP_FUNCTION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ADD_PRIMARYKEY_EVENT
init|=
literal|"ADD_PRIMARYKEY"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ADD_FOREIGNKEY_EVENT
init|=
literal|"ADD_FOREIGNKEY"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ADD_UNIQUECONSTRAINT_EVENT
init|=
literal|"ADD_UNIQUECONSTRAINT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ADD_NOTNULLCONSTRAINT_EVENT
init|=
literal|"ADD_NOTNULLCONSTRAINT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_CONSTRAINT_EVENT
init|=
literal|"DROP_CONSTRAINT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_ISCHEMA_EVENT
init|=
literal|"CREATE_ISCHEMA"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALTER_ISCHEMA_EVENT
init|=
literal|"ALTER_ISCHEMA"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_ISCHEMA_EVENT
init|=
literal|"DROP_ISCHEMA"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ADD_SCHEMA_VERSION_EVENT
init|=
literal|"ADD_SCHEMA_VERSION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALTER_SCHEMA_VERSION_EVENT
init|=
literal|"ALTER_SCHEMA_VERSION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_SCHEMA_VERSION_EVENT
init|=
literal|"DROP_SCHEMA_VERSION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_CATALOG_EVENT
init|=
literal|"CREATE_CATALOG"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_CATALOG_EVENT
init|=
literal|"DROP_CATALOG"
decl_stmt|;
specifier|private
specifier|static
name|MessageFactory
name|instance
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
comment|/*   // TODO MS-SPLIT I'm 99% certain we don't need this, as MetastoreConf.newMetastoreConf already   adds this resource.   static {     conf.addResource("hive-site.xml");   }   */
specifier|protected
specifier|static
specifier|final
name|String
name|MS_SERVER_URL
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|THRIFT_URIS
argument_list|,
literal|""
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|MS_SERVICE_PRINCIPAL
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|KERBEROS_PRINCIPAL
argument_list|,
literal|""
argument_list|)
decl_stmt|;
comment|/**    * Getter for MessageFactory instance.    */
specifier|public
specifier|static
name|MessageFactory
name|getInstance
parameter_list|()
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
name|instance
operator|=
name|getInstance
argument_list|(
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|EVENT_MESSAGE_FACTORY
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|instance
return|;
block|}
specifier|private
specifier|static
name|MessageFactory
name|getInstance
parameter_list|(
name|String
name|className
parameter_list|)
block|{
try|try
block|{
return|return
name|JavaUtils
operator|.
name|newInstance
argument_list|(
name|JavaUtils
operator|.
name|getClass
argument_list|(
name|className
argument_list|,
name|MessageFactory
operator|.
name|class
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Could not construct MessageFactory implementation: "
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Getter for MessageDeserializer, corresponding to the specified format and version.    * @param format Serialization format for notifications.    * @param version Version of serialization format (currently ignored.)    * @return MessageDeserializer.    */
specifier|public
specifier|static
name|MessageDeserializer
name|getDeserializer
parameter_list|(
name|String
name|format
parameter_list|,
name|String
name|version
parameter_list|)
block|{
return|return
name|getInstance
argument_list|(
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|EVENT_MESSAGE_FACTORY
argument_list|)
argument_list|)
operator|.
name|getDeserializer
argument_list|()
return|;
comment|// Note : The reason this method exists outside the no-arg getDeserializer method is in
comment|// case there is a user-implemented MessageFactory that's used, and some the messages
comment|// are in an older format and the rest in another. Then, what MessageFactory is default
comment|// is irrelevant, we should always use the one that was used to create it to deserialize.
comment|//
comment|// There exist only 2 implementations of this - json and jms
comment|//
comment|// Additional note : rather than as a config parameter, does it make sense to have
comment|// this use jdbc-like semantics that each MessageFactory made available register
comment|// itself for discoverability? Might be worth pursuing.
block|}
specifier|public
specifier|abstract
name|MessageDeserializer
name|getDeserializer
parameter_list|()
function_decl|;
comment|/**    * Getter for message-format.    */
specifier|public
specifier|abstract
name|String
name|getMessageFormat
parameter_list|()
function_decl|;
comment|/**    * Factory method for CreateDatabaseMessage.    * @param db The Database being added.    * @return CreateDatabaseMessage instance.    */
specifier|public
specifier|abstract
name|CreateDatabaseMessage
name|buildCreateDatabaseMessage
parameter_list|(
name|Database
name|db
parameter_list|)
function_decl|;
comment|/**    * Factory method for AlterDatabaseMessage.    * @param beforeDb The Database before alter.    * @param afterDb The Database after alter.    * @return AlterDatabaseMessage instance.    */
specifier|public
specifier|abstract
name|AlterDatabaseMessage
name|buildAlterDatabaseMessage
parameter_list|(
name|Database
name|beforeDb
parameter_list|,
name|Database
name|afterDb
parameter_list|)
function_decl|;
comment|/**    * Factory method for DropDatabaseMessage.    * @param db The Database being dropped.    * @return DropDatabaseMessage instance.    */
specifier|public
specifier|abstract
name|DropDatabaseMessage
name|buildDropDatabaseMessage
parameter_list|(
name|Database
name|db
parameter_list|)
function_decl|;
comment|/**    * Factory method for CreateTableMessage.    * @param table The Table being created.    * @param files Iterator of files    * @return CreateTableMessage instance.    */
specifier|public
specifier|abstract
name|CreateTableMessage
name|buildCreateTableMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
function_decl|;
comment|/**    * Factory method for AlterTableMessage.  Unlike most of these calls, this one can return null,    * which means no message should be sent.  This is because there are many flavors of alter    * table (add column, add partition, etc.).  Some are covered elsewhere (like add partition)    * and some are not yet supported.    * @param before The table before the alter    * @param after The table after the alter    * @param isTruncateOp Flag to denote truncate table    * @return    */
specifier|public
specifier|abstract
name|AlterTableMessage
name|buildAlterTableMessage
parameter_list|(
name|Table
name|before
parameter_list|,
name|Table
name|after
parameter_list|,
name|boolean
name|isTruncateOp
parameter_list|)
function_decl|;
comment|/**    * Factory method for DropTableMessage.    * @param table The Table being dropped.    * @return DropTableMessage instance.    */
specifier|public
specifier|abstract
name|DropTableMessage
name|buildDropTableMessage
parameter_list|(
name|Table
name|table
parameter_list|)
function_decl|;
comment|/**      * Factory method for AddPartitionMessage.      * @param table The Table to which the partitions are added.      * @param partitions The iterator to set of Partitions being added.      * @param partitionFiles The iterator of partition files      * @return AddPartitionMessage instance.      */
specifier|public
specifier|abstract
name|AddPartitionMessage
name|buildAddPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|Iterator
argument_list|<
name|PartitionFiles
argument_list|>
name|partitionFiles
parameter_list|)
function_decl|;
comment|/**    * Factory method for building AlterPartitionMessage    * @param table The table in which the partition is being altered    * @param before The partition before it was altered    * @param after The partition after it was altered    * @param isTruncateOp Flag to denote truncate partition    * @return a new AlterPartitionMessage    */
specifier|public
specifier|abstract
name|AlterPartitionMessage
name|buildAlterPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|before
parameter_list|,
name|Partition
name|after
parameter_list|,
name|boolean
name|isTruncateOp
parameter_list|)
function_decl|;
comment|/**    * Factory method for DropPartitionMessage.    * @param table The Table from which the partition is dropped.    * @param partitions The set of partitions being dropped.    * @return DropPartitionMessage instance.    */
specifier|public
specifier|abstract
name|DropPartitionMessage
name|buildDropPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Iterator
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
function_decl|;
comment|/**    * Factory method for CreateFunctionMessage.    * @param fn The Function being added.    * @return CreateFunctionMessage instance.    */
specifier|public
specifier|abstract
name|CreateFunctionMessage
name|buildCreateFunctionMessage
parameter_list|(
name|Function
name|fn
parameter_list|)
function_decl|;
comment|/**    * Factory method for DropFunctionMessage.    * @param fn The Function being dropped.    * @return DropFunctionMessage instance.    */
specifier|public
specifier|abstract
name|DropFunctionMessage
name|buildDropFunctionMessage
parameter_list|(
name|Function
name|fn
parameter_list|)
function_decl|;
comment|/**    * Factory method for building insert message    *    * @param tableObj Table object where the insert occurred in    * @param ptnObj Partition object where the insert occurred in, may be null if    *          the insert was done into a non-partitioned table    * @param replace Flag to represent if INSERT OVERWRITE or INSERT INTO    * @param files Iterator of file created    * @return instance of InsertMessage    */
specifier|public
specifier|abstract
name|InsertMessage
name|buildInsertMessage
parameter_list|(
name|Table
name|tableObj
parameter_list|,
name|Partition
name|ptnObj
parameter_list|,
name|boolean
name|replace
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
function_decl|;
comment|/***    * Factory method for building add primary key message    *    * @param pks list of primary keys    * @return instance of AddPrimaryKeyMessage    */
specifier|public
specifier|abstract
name|AddPrimaryKeyMessage
name|buildAddPrimaryKeyMessage
parameter_list|(
name|List
argument_list|<
name|SQLPrimaryKey
argument_list|>
name|pks
parameter_list|)
function_decl|;
comment|/***    * Factory method for building add foreign key message    *    * @param fks list of foreign keys    * @return instance of AddForeignKeyMessage    */
specifier|public
specifier|abstract
name|AddForeignKeyMessage
name|buildAddForeignKeyMessage
parameter_list|(
name|List
argument_list|<
name|SQLForeignKey
argument_list|>
name|fks
parameter_list|)
function_decl|;
comment|/***    * Factory method for building add unique constraint message    *    * @param uks list of unique constraints    * @return instance of SQLUniqueConstraint    */
specifier|public
specifier|abstract
name|AddUniqueConstraintMessage
name|buildAddUniqueConstraintMessage
parameter_list|(
name|List
argument_list|<
name|SQLUniqueConstraint
argument_list|>
name|uks
parameter_list|)
function_decl|;
comment|/***    * Factory method for building add not null constraint message    *    * @param nns list of not null constraints    * @return instance of SQLNotNullConstraint    */
specifier|public
specifier|abstract
name|AddNotNullConstraintMessage
name|buildAddNotNullConstraintMessage
parameter_list|(
name|List
argument_list|<
name|SQLNotNullConstraint
argument_list|>
name|nns
parameter_list|)
function_decl|;
comment|/***    * Factory method for building drop constraint message    * @param dbName    * @param tableName    * @param constraintName    * @return    */
specifier|public
specifier|abstract
name|DropConstraintMessage
name|buildDropConstraintMessage
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|constraintName
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|CreateCatalogMessage
name|buildCreateCatalogMessage
parameter_list|(
name|Catalog
name|catalog
parameter_list|)
function_decl|;
specifier|public
specifier|abstract
name|DropCatalogMessage
name|buildDropCatalogMessage
parameter_list|(
name|Catalog
name|catalog
parameter_list|)
function_decl|;
block|}
end_class

end_unit


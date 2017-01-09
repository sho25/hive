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
name|Index
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
name|util
operator|.
name|ReflectionUtils
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|CREATE_INDEX_EVENT
init|=
literal|"CREATE_INDEX"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DROP_INDEX_EVENT
init|=
literal|"DROP_INDEX"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ALTER_INDEX_EVENT
init|=
literal|"ALTER_INDEX"
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
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
static|static
block|{
name|hiveConf
operator|.
name|addResource
argument_list|(
literal|"hive-site.xml"
argument_list|)
expr_stmt|;
block|}
comment|// This parameter is retained for legacy reasons, in case someone implemented custom
comment|// factories. This, however, should not be the case, since this api was intended to
comment|// be internal-only, and we should manage the jms and json implementations without
comment|// needing this parameter. Marking as deprecated, for removal by 2.4 - see corresponding
comment|// note on the getDeserializer(String,String) method
annotation|@
name|Deprecated
specifier|private
specifier|static
specifier|final
name|String
name|CONF_LABEL_HCAT_MESSAGE_FACTORY_IMPL_PREFIX
init|=
literal|"hcatalog.message.factory.impl."
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|MS_SERVER_URL
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
operator|.
name|name
argument_list|()
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
name|hiveConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_KERBEROS_PRINCIPAL
operator|.
name|name
argument_list|()
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
name|hiveConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_MESSAGE_FACTORY
operator|.
name|varname
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
operator|(
name|MessageFactory
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|className
argument_list|)
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|classNotFound
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Could not construct MessageFactory implementation: "
argument_list|,
name|classNotFound
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
name|hiveConf
operator|.
name|get
argument_list|(
name|CONF_LABEL_HCAT_MESSAGE_FACTORY_IMPL_PREFIX
operator|+
name|format
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_EVENT_MESSAGE_FACTORY
operator|.
name|varname
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
comment|/**    * Getter for version-string, corresponding to all constructed messages.    */
specifier|public
specifier|abstract
name|String
name|getVersion
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
comment|/**    * Factory method for CreateTableMessage.    * @param table The Table being created.    * @return CreateTableMessage instance.    */
specifier|public
specifier|abstract
name|CreateTableMessage
name|buildCreateTableMessage
parameter_list|(
name|Table
name|table
parameter_list|)
function_decl|;
comment|/**    * Factory method for AlterTableMessage.  Unlike most of these calls, this one can return null,    * which means no message should be sent.  This is because there are many flavors of alter    * table (add column, add partition, etc.).  Some are covered elsewhere (like add partition)    * and some are not yet supported.    * @param before The table before the alter    * @param after The table after the alter    * @return    */
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
comment|/**      * Factory method for AddPartitionMessage.      * @param table The Table to which the partitions are added.      * @param partitions The iterator to set of Partitions being added.      * @return AddPartitionMessage instance.      */
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
parameter_list|)
function_decl|;
comment|/**    * Factory method for building AlterPartitionMessage    * @param table The table in which the partition is being altered    * @param before The partition before it was altered    * @param after The partition after it was altered    * @return a new AlterPartitionMessage    */
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
comment|/**    * Factory method for CreateIndexMessage.    * @param idx The Index being added.    * @return CreateIndexMessage instance.    */
specifier|public
specifier|abstract
name|CreateIndexMessage
name|buildCreateIndexMessage
parameter_list|(
name|Index
name|idx
parameter_list|)
function_decl|;
comment|/**    * Factory method for DropIndexMessage.    * @param idx The Index being dropped.    * @return DropIndexMessage instance.    */
specifier|public
specifier|abstract
name|DropIndexMessage
name|buildDropIndexMessage
parameter_list|(
name|Index
name|idx
parameter_list|)
function_decl|;
comment|/**    * Factory method for AlterIndexMessage.    * @param before The index before the alter    * @param after The index after the alter    * @return AlterIndexMessage    */
specifier|public
specifier|abstract
name|AlterIndexMessage
name|buildAlterIndexMessage
parameter_list|(
name|Index
name|before
parameter_list|,
name|Index
name|after
parameter_list|)
function_decl|;
comment|/**    * Factory method for building insert message    *    * @param db Name of the database the insert occurred in    * @param table Name of the table the insert occurred in    * @param partVals Partition values for the partition that the insert occurred in, may be null if    *          the insert was done into a non-partitioned table    * @param files List of files created as a result of the insert, may be null.    * @return instance of InsertMessage    */
specifier|public
specifier|abstract
name|InsertMessage
name|buildInsertMessage
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partVals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|)
function_decl|;
comment|/**    * Factory method for building insert message    *    * @param db Name of the database the insert occurred in    * @param table Name of the table the insert occurred in    * @param partVals Partition values for the partition that the insert occurred in, may be null if    *          the insert was done into a non-partitioned table    * @param files List of files created as a result of the insert, may be null    * @param fileChecksums List of checksums corresponding to the files added during insert    * @return instance of InsertMessage    */
specifier|public
specifier|abstract
name|InsertMessage
name|buildInsertMessage
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partVals
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|files
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fileChecksums
parameter_list|)
function_decl|;
block|}
end_class

end_unit


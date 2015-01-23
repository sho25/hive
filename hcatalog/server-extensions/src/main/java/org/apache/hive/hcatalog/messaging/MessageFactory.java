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
name|hive
operator|.
name|hcatalog
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|partition
operator|.
name|spec
operator|.
name|PartitionSpecProxy
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
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|json
operator|.
name|JSONMessageFactory
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
specifier|private
specifier|static
name|MessageFactory
name|instance
init|=
operator|new
name|JSONMessageFactory
argument_list|()
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
specifier|private
specifier|static
specifier|final
name|String
name|CONF_LABEL_HCAT_MESSAGE_FACTORY_IMPL_PREFIX
init|=
literal|"hcatalog.message.factory.impl."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONF_LABEL_HCAT_MESSAGE_FORMAT
init|=
literal|"hcatalog.message.format"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HCAT_MESSAGE_FORMAT
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|CONF_LABEL_HCAT_MESSAGE_FORMAT
argument_list|,
literal|"json"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_MESSAGE_FACTORY_IMPL
init|=
literal|"org.apache.hive.hcatalog.messaging.json.JSONMessageFactory"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HCAT_MESSAGE_FACTORY_IMPL
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|CONF_LABEL_HCAT_MESSAGE_FACTORY_IMPL_PREFIX
operator|+
name|HCAT_MESSAGE_FORMAT
argument_list|,
name|DEFAULT_MESSAGE_FACTORY_IMPL
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|HCAT_SERVER_URL
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
name|HCAT_SERVICE_PRINCIPAL
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
name|HCAT_MESSAGE_FACTORY_IMPL
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
name|Class
operator|.
name|forName
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
name|DEFAULT_MESSAGE_FACTORY_IMPL
argument_list|)
argument_list|)
operator|.
name|getDeserializer
argument_list|()
return|;
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
comment|/**      * Factory method for AddPartitionMessage.      * @param table The Table to which the partitions are added.      * @param partitions The set of Partitions being added.      * @return AddPartitionMessage instance.      */
specifier|public
specifier|abstract
name|AddPartitionMessage
name|buildAddPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|)
function_decl|;
comment|/**    * Factory method for AddPartitionMessage.    * @param table The Table to which the partitions are added.    * @param partitionSpec The set of Partitions being added.    * @return AddPartitionMessage instance.    */
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"Hive"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|abstract
name|AddPartitionMessage
name|buildAddPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|PartitionSpecProxy
name|partitionSpec
parameter_list|)
function_decl|;
comment|/**    * Factory method for building AlterPartitionMessage    * @param before The partition before it was altered    * @param after The partition after it was altered    * @return a new AlterPartitionMessage    */
specifier|public
specifier|abstract
name|AlterPartitionMessage
name|buildAlterPartitionMessage
parameter_list|(
name|Partition
name|before
parameter_list|,
name|Partition
name|after
parameter_list|)
function_decl|;
comment|/**    * Factory method for DropPartitionMessage.    * @param table The Table from which the partition is dropped.    * @param partition The Partition being dropped.    * @return DropPartitionMessage instance.    */
specifier|public
specifier|abstract
name|DropPartitionMessage
name|buildDropPartitionMessage
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
function_decl|;
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|streaming
operator|.
name|mutate
operator|.
name|client
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
name|IMetaStoreClient
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
name|security
operator|.
name|UserGroupInformation
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
name|common
operator|.
name|HCatUtil
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
name|streaming
operator|.
name|mutate
operator|.
name|HiveConfFactory
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
name|streaming
operator|.
name|mutate
operator|.
name|UgiMetaStoreClientFactory
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
name|streaming
operator|.
name|mutate
operator|.
name|client
operator|.
name|lock
operator|.
name|Lock
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
name|streaming
operator|.
name|mutate
operator|.
name|client
operator|.
name|lock
operator|.
name|LockFailureListener
import|;
end_import

begin_comment
comment|/** Convenience class for building {@link MutatorClient} instances.  * @deprecated as of Hive 3.0.0  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|MutatorClientBuilder
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AcidTable
argument_list|>
name|tables
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|configuration
decl_stmt|;
specifier|private
name|UserGroupInformation
name|authenticatedUser
decl_stmt|;
specifier|private
name|String
name|metaStoreUri
decl_stmt|;
specifier|public
name|LockFailureListener
name|lockFailureListener
decl_stmt|;
specifier|public
name|MutatorClientBuilder
name|configuration
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|conf
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MutatorClientBuilder
name|authenticatedUser
parameter_list|(
name|UserGroupInformation
name|authenticatedUser
parameter_list|)
block|{
name|this
operator|.
name|authenticatedUser
operator|=
name|authenticatedUser
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|MutatorClientBuilder
name|metaStoreUri
parameter_list|(
name|String
name|metaStoreUri
parameter_list|)
block|{
name|this
operator|.
name|metaStoreUri
operator|=
name|metaStoreUri
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Set a listener to handle {@link Lock} failure events - highly recommended. */
specifier|public
name|MutatorClientBuilder
name|lockFailureListener
parameter_list|(
name|LockFailureListener
name|lockFailureListener
parameter_list|)
block|{
name|this
operator|.
name|lockFailureListener
operator|=
name|lockFailureListener
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Adds a mutation event destination (an ACID table) to be managed by this client, which is either unpartitioned or    * will is not to have partitions created automatically.    */
specifier|public
name|MutatorClientBuilder
name|addSourceTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|addTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|,
name|TableType
operator|.
name|SOURCE
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Adds a mutation event destination (an ACID table) to be managed by this client, which is either unpartitioned or    * will is not to have partitions created automatically.    */
specifier|public
name|MutatorClientBuilder
name|addSinkTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
name|addSinkTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Adds a partitioned mutation event destination (an ACID table) to be managed by this client, where new partitions    * will be created as needed.    */
specifier|public
name|MutatorClientBuilder
name|addSinkTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|boolean
name|createPartitions
parameter_list|)
block|{
name|addTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
name|createPartitions
argument_list|,
name|TableType
operator|.
name|SINK
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|private
name|void
name|addTable
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|boolean
name|createPartitions
parameter_list|,
name|TableType
name|tableType
parameter_list|)
block|{
if|if
condition|(
name|databaseName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Database cannot be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|tableName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table cannot be null"
argument_list|)
throw|;
block|}
name|String
name|key
init|=
operator|(
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
operator|)
operator|.
name|toUpperCase
argument_list|()
decl_stmt|;
name|AcidTable
name|previous
init|=
name|tables
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|tableType
operator|==
name|TableType
operator|.
name|SINK
operator|&&
name|previous
operator|.
name|getTableType
argument_list|()
operator|!=
name|TableType
operator|.
name|SINK
condition|)
block|{
name|tables
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Table has already been added: "
operator|+
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
argument_list|)
throw|;
block|}
block|}
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|databaseName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tables
operator|.
name|put
argument_list|(
name|key
argument_list|,
operator|new
name|AcidTable
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
name|createPartitions
argument_list|,
name|tableType
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Builds the client. */
specifier|public
name|MutatorClient
name|build
parameter_list|()
throws|throws
name|ClientException
throws|,
name|MetaException
block|{
name|String
name|user
init|=
name|authenticatedUser
operator|==
literal|null
condition|?
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
else|:
name|authenticatedUser
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
name|boolean
name|secureMode
init|=
name|authenticatedUser
operator|==
literal|null
condition|?
literal|false
else|:
name|authenticatedUser
operator|.
name|hasKerberosCredentials
argument_list|()
decl_stmt|;
name|configuration
operator|=
name|HiveConfFactory
operator|.
name|newInstance
argument_list|(
name|configuration
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
name|metaStoreUri
argument_list|)
expr_stmt|;
name|IMetaStoreClient
name|metaStoreClient
decl_stmt|;
try|try
block|{
name|metaStoreClient
operator|=
operator|new
name|UgiMetaStoreClientFactory
argument_list|(
name|metaStoreUri
argument_list|,
name|configuration
argument_list|,
name|authenticatedUser
argument_list|,
name|user
argument_list|,
name|secureMode
argument_list|)
operator|.
name|newInstance
argument_list|(
name|HCatUtil
operator|.
name|getHiveMetastoreClient
argument_list|(
name|configuration
argument_list|)
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
name|ClientException
argument_list|(
literal|"Could not create meta store client."
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|MutatorClient
argument_list|(
name|metaStoreClient
argument_list|,
name|configuration
argument_list|,
name|lockFailureListener
argument_list|,
name|user
argument_list|,
name|tables
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit


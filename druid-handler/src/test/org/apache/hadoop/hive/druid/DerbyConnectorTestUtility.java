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
name|druid
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Suppliers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|metadata
operator|.
name|MetadataStorageConnectorConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|metadata
operator|.
name|MetadataStorageTablesConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|metadata
operator|.
name|storage
operator|.
name|derby
operator|.
name|DerbyConnector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|druid
operator|.
name|metadata
operator|.
name|storage
operator|.
name|derby
operator|.
name|DerbyMetadataStorage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExternalResource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|skife
operator|.
name|jdbi
operator|.
name|v2
operator|.
name|DBI
import|;
end_import

begin_import
import|import
name|org
operator|.
name|skife
operator|.
name|jdbi
operator|.
name|v2
operator|.
name|exceptions
operator|.
name|UnableToObtainConnectionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_comment
comment|/**  * Derby test class.  */
end_comment

begin_class
specifier|public
class|class
name|DerbyConnectorTestUtility
extends|extends
name|DerbyConnector
block|{
specifier|private
specifier|final
name|String
name|jdbcUri
decl_stmt|;
specifier|public
name|DerbyConnectorTestUtility
parameter_list|(
name|Supplier
argument_list|<
name|MetadataStorageConnectorConfig
argument_list|>
name|config
parameter_list|,
name|Supplier
argument_list|<
name|MetadataStorageTablesConfig
argument_list|>
name|dbTables
parameter_list|)
block|{
name|this
argument_list|(
name|config
argument_list|,
name|dbTables
argument_list|,
literal|"jdbc:derby:memory:druidTest"
operator|+
name|dbSafeUUID
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|DerbyConnectorTestUtility
parameter_list|(
name|Supplier
argument_list|<
name|MetadataStorageConnectorConfig
argument_list|>
name|config
parameter_list|,
name|Supplier
argument_list|<
name|MetadataStorageTablesConfig
argument_list|>
name|dbTables
parameter_list|,
name|String
name|jdbcUri
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|DerbyMetadataStorage
argument_list|(
name|config
operator|.
name|get
argument_list|()
argument_list|)
argument_list|,
name|config
operator|::
name|get
argument_list|,
name|dbTables
operator|::
name|get
argument_list|,
operator|new
name|DBI
argument_list|(
name|jdbcUri
operator|+
literal|";create=true"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|jdbcUri
operator|=
name|jdbcUri
expr_stmt|;
block|}
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
try|try
block|{
operator|new
name|DBI
argument_list|(
name|jdbcUri
operator|+
literal|";drop=true"
argument_list|)
operator|.
name|open
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnableToObtainConnectionException
name|e
parameter_list|)
block|{
name|SQLException
name|cause
init|=
operator|(
name|SQLException
operator|)
name|e
operator|.
name|getCause
argument_list|()
decl_stmt|;
comment|// error code "08006" indicates proper shutdown
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Derby not shutdown: [%s]"
argument_list|,
name|cause
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|"08006"
argument_list|,
name|cause
operator|.
name|getSQLState
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|String
name|dbSafeUUID
parameter_list|()
block|{
return|return
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|"-"
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|public
name|String
name|getJdbcUri
parameter_list|()
block|{
return|return
name|jdbcUri
return|;
block|}
comment|/**    * Derby connector rule.    */
specifier|public
specifier|static
class|class
name|DerbyConnectorRule
extends|extends
name|ExternalResource
block|{
specifier|private
name|DerbyConnectorTestUtility
name|connector
decl_stmt|;
specifier|private
specifier|final
name|Supplier
argument_list|<
name|MetadataStorageTablesConfig
argument_list|>
name|dbTables
decl_stmt|;
specifier|private
specifier|final
name|MetadataStorageConnectorConfig
name|connectorConfig
decl_stmt|;
specifier|public
name|DerbyConnectorRule
parameter_list|()
block|{
name|this
argument_list|(
literal|"druidTest"
operator|+
name|dbSafeUUID
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|DerbyConnectorRule
parameter_list|(
specifier|final
name|String
name|defaultBase
parameter_list|)
block|{
name|this
argument_list|(
name|Suppliers
operator|.
name|ofInstance
argument_list|(
name|MetadataStorageTablesConfig
operator|.
name|fromBase
argument_list|(
name|defaultBase
argument_list|)
argument_list|)
operator|::
name|get
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DerbyConnectorRule
parameter_list|(
name|Supplier
argument_list|<
name|MetadataStorageTablesConfig
argument_list|>
name|dbTables
parameter_list|)
block|{
name|this
operator|.
name|dbTables
operator|=
name|dbTables
expr_stmt|;
name|this
operator|.
name|connectorConfig
operator|=
operator|new
name|MetadataStorageConnectorConfig
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|getConnectURI
parameter_list|()
block|{
return|return
name|connector
operator|.
name|getJdbcUri
argument_list|()
return|;
block|}
block|}
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|before
parameter_list|()
throws|throws
name|Throwable
block|{
name|connector
operator|=
operator|new
name|DerbyConnectorTestUtility
argument_list|(
name|Suppliers
operator|.
name|ofInstance
argument_list|(
name|connectorConfig
argument_list|)
operator|::
name|get
argument_list|,
name|dbTables
argument_list|)
expr_stmt|;
name|connector
operator|.
name|getDBI
argument_list|()
operator|.
name|open
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// create db
block|}
annotation|@
name|Override
specifier|protected
name|void
name|after
parameter_list|()
block|{
name|connector
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|DerbyConnectorTestUtility
name|getConnector
parameter_list|()
block|{
return|return
name|connector
return|;
block|}
specifier|public
name|Supplier
argument_list|<
name|MetadataStorageTablesConfig
argument_list|>
name|metadataTablesConfigSupplier
parameter_list|()
block|{
return|return
name|dbTables
return|;
block|}
block|}
block|}
end_class

end_unit


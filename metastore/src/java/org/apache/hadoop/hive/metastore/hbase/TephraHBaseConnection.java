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
name|hbase
package|;
end_package

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|TransactionAware
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|TransactionContext
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|TransactionFailureException
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|TransactionManager
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|TransactionSystemClient
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|distributed
operator|.
name|ThreadLocalClientProvider
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|distributed
operator|.
name|TransactionServiceClient
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|hbase10
operator|.
name|TransactionAwareHTable
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|hbase10
operator|.
name|coprocessor
operator|.
name|TransactionProcessor
import|;
end_import

begin_import
import|import
name|co
operator|.
name|cask
operator|.
name|tephra
operator|.
name|inmemory
operator|.
name|InMemoryTxSystemClient
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HTableDescriptor
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
name|hbase
operator|.
name|client
operator|.
name|HTableInterface
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
name|twill
operator|.
name|discovery
operator|.
name|InMemoryDiscoveryService
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

begin_comment
comment|/**  * A class that uses Tephra for transaction management.  */
end_comment

begin_class
specifier|public
class|class
name|TephraHBaseConnection
extends|extends
name|VanillaHBaseConnection
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TephraHBaseConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|TransactionAware
argument_list|>
name|txnTables
decl_stmt|;
specifier|private
name|TransactionContext
name|txn
decl_stmt|;
specifier|private
name|TransactionSystemClient
name|txnClient
decl_stmt|;
name|TephraHBaseConnection
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|txnTables
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TransactionAware
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connect
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|connect
argument_list|()
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using an in memory client transaction system for testing"
argument_list|)
expr_stmt|;
name|TransactionManager
name|txnMgr
init|=
operator|new
name|TransactionManager
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|txnMgr
operator|.
name|startAndWait
argument_list|()
expr_stmt|;
name|txnClient
operator|=
operator|new
name|InMemoryTxSystemClient
argument_list|(
name|txnMgr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO should enable use of ZKDiscoveryService if users want it
name|LOG
operator|.
name|debug
argument_list|(
literal|"Using real client transaction system for production"
argument_list|)
expr_stmt|;
name|txnClient
operator|=
operator|new
name|TransactionServiceClient
argument_list|(
name|conf
argument_list|,
operator|new
name|ThreadLocalClientProvider
argument_list|(
name|conf
argument_list|,
operator|new
name|InMemoryDiscoveryService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|tableName
range|:
name|HBaseReadWrite
operator|.
name|tableNames
control|)
block|{
name|txnTables
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
operator|new
name|TransactionAwareHTable
argument_list|(
name|super
operator|.
name|getHBaseTable
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|txn
operator|=
operator|new
name|TransactionContext
argument_list|(
name|txnClient
argument_list|,
name|txnTables
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beginTransaction
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|txn
operator|.
name|start
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Started txn in tephra"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TransactionFailureException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTransaction
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|txn
operator|.
name|finish
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Finished txn in tephra"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TransactionFailureException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollbackTransaction
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|txn
operator|.
name|abort
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Aborted txn in tephra"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TransactionFailureException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|(
name|HTableInterface
name|htab
parameter_list|)
throws|throws
name|IOException
block|{
comment|// NO-OP as we want to flush at commit time
block|}
annotation|@
name|Override
specifier|protected
name|HTableDescriptor
name|buildDescriptor
parameter_list|(
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|tableDesc
init|=
name|super
operator|.
name|buildDescriptor
argument_list|(
name|tableName
argument_list|,
name|columnFamilies
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|addCoprocessor
argument_list|(
name|TransactionProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|tableDesc
return|;
block|}
annotation|@
name|Override
specifier|public
name|HTableInterface
name|getHBaseTable
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Ignore force, it will mess up our previous creation of the tables.
return|return
operator|(
name|TransactionAwareHTable
operator|)
name|txnTables
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|hbase
operator|.
name|CoprocessorEnvironment
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
name|coprocessor
operator|.
name|BaseEndpointCoprocessor
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

begin_comment
comment|/**  * Implementation of RevisionManager as HBase RPC endpoint. This class will control the lifecycle of  * and delegate to the actual RevisionManager implementation and make it available as a service  * hosted in the HBase region server (instead of running it in the client (storage handler).  * In the case of {@link ZKBasedRevisionManager} now only the region servers need write access to  * manage revision data.  */
end_comment

begin_class
specifier|public
class|class
name|RevisionManagerEndpoint
extends|extends
name|BaseEndpointCoprocessor
implements|implements
name|RevisionManagerProtocol
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RevisionManagerEndpoint
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|RevisionManager
name|rmImpl
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
block|{
name|super
operator|.
name|start
argument_list|(
name|env
argument_list|)
expr_stmt|;
try|try
block|{
name|Configuration
name|conf
init|=
name|RevisionManagerConfiguration
operator|.
name|create
argument_list|(
name|env
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|RMConstants
operator|.
name|REVISION_MGR_ENDPOINT_IMPL_CLASS
argument_list|,
name|ZKBasedRevisionManager
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Using Revision Manager implementation: {}"
argument_list|,
name|className
argument_list|)
expr_stmt|;
name|rmImpl
operator|=
name|RevisionManagerFactory
operator|.
name|getOpenedRevisionManager
argument_list|(
name|className
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"Failed to initialize revision manager"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
block|{
if|if
condition|(
name|rmImpl
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|rmImpl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Error closing revision manager."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|stop
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// do nothing, HBase controls life cycle
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|IOException
block|{
comment|// do nothing, HBase controls life cycle
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// do nothing, HBase controls life cycle
block|}
annotation|@
name|Override
specifier|public
name|void
name|createTable
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnFamilies
parameter_list|)
throws|throws
name|IOException
block|{
name|rmImpl
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|columnFamilies
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|rmImpl
operator|.
name|dropTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Transaction
name|beginWriteTransaction
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|families
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|rmImpl
operator|.
name|beginWriteTransaction
argument_list|(
name|table
argument_list|,
name|families
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Transaction
name|beginWriteTransaction
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|families
parameter_list|,
name|long
name|keepAlive
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|rmImpl
operator|.
name|beginWriteTransaction
argument_list|(
name|table
argument_list|,
name|families
argument_list|,
name|keepAlive
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitWriteTransaction
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
block|{
name|rmImpl
operator|.
name|commitWriteTransaction
argument_list|(
name|transaction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortWriteTransaction
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
block|{
name|rmImpl
operator|.
name|abortWriteTransaction
argument_list|(
name|transaction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TableSnapshot
name|createSnapshot
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|rmImpl
operator|.
name|createSnapshot
argument_list|(
name|tableName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableSnapshot
name|createSnapshot
parameter_list|(
name|String
name|tableName
parameter_list|,
name|long
name|revision
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|rmImpl
operator|.
name|createSnapshot
argument_list|(
name|tableName
argument_list|,
name|revision
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|keepAlive
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
block|{
name|rmImpl
operator|.
name|keepAlive
argument_list|(
name|transaction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|FamilyRevision
argument_list|>
name|getAbortedWriteTransactions
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|rmImpl
operator|.
name|getAbortedWriteTransactions
argument_list|(
name|table
argument_list|,
name|columnFamily
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|ArrayList
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
name|events
operator|.
name|AddPartitionEvent
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
name|events
operator|.
name|AlterPartitionEvent
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
name|events
operator|.
name|AlterTableEvent
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
name|events
operator|.
name|CreateDatabaseEvent
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
name|events
operator|.
name|CreateTableEvent
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
name|events
operator|.
name|DropDatabaseEvent
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
name|events
operator|.
name|DropPartitionEvent
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
name|events
operator|.
name|DropTableEvent
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
name|events
operator|.
name|ListenerEvent
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
name|events
operator|.
name|LoadPartitionDoneEvent
import|;
end_import

begin_comment
comment|/** A dummy implementation for  * {@link org.apache.hadoop.hive.metastore.hadooorg.apache.hadoop.hive.metastore.MetaStoreEventListener}  * for testing purposes.  */
end_comment

begin_class
specifier|public
class|class
name|DummyListener
extends|extends
name|MetaStoreEventListener
block|{
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|ListenerEvent
argument_list|>
name|notifyList
init|=
operator|new
name|ArrayList
argument_list|<
name|ListenerEvent
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|DummyListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAddPartition
parameter_list|(
name|AddPartitionEvent
name|partition
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onCreateDatabase
parameter_list|(
name|CreateDatabaseEvent
name|db
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|db
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onCreateTable
parameter_list|(
name|CreateTableEvent
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onDropDatabase
parameter_list|(
name|DropDatabaseEvent
name|db
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|db
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onDropPartition
parameter_list|(
name|DropPartitionEvent
name|partition
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onDropTable
parameter_list|(
name|DropTableEvent
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAlterTable
parameter_list|(
name|AlterTableEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAlterPartition
parameter_list|(
name|AlterPartitionEvent
name|event
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onLoadPartitionDone
parameter_list|(
name|LoadPartitionDoneEvent
name|partEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|notifyList
operator|.
name|add
argument_list|(
name|partEvent
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


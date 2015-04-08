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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
name|AddIndexEvent
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
name|AlterIndexEvent
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
name|ConfigChangeEvent
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
name|DropIndexEvent
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
name|InsertEvent
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
comment|/**  * This abstract class needs to be extended to  provide implementation of actions that needs  * to be performed when a particular event occurs on a metastore. These methods  * are called whenever an event occurs on metastore. Status of the event whether  * it was successful or not is contained in container event object.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MetaStoreEventListener
implements|implements
name|Configurable
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|MetaStoreEventListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|config
expr_stmt|;
block|}
comment|/**    * @param tableEvent table event.    * @throws MetaException    */
specifier|public
name|void
name|onConfigChange
parameter_list|(
name|ConfigChangeEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param tableEvent table event.    * @throws MetaException    */
specifier|public
name|void
name|onCreateTable
parameter_list|(
name|CreateTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param tableEvent table event.    * @throws MetaException    */
specifier|public
name|void
name|onDropTable
parameter_list|(
name|DropTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param add partition event    * @throws MetaException    */
comment|/**    * @param tableEvent alter table event    * @throws MetaException    */
specifier|public
name|void
name|onAlterTable
parameter_list|(
name|AlterTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
specifier|public
name|void
name|onAddPartition
parameter_list|(
name|AddPartitionEvent
name|partitionEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param partitionEvent partition event    * @throws MetaException    */
specifier|public
name|void
name|onDropPartition
parameter_list|(
name|DropPartitionEvent
name|partitionEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param partitionEvent partition event    * @throws MetaException    */
specifier|public
name|void
name|onAlterPartition
parameter_list|(
name|AlterPartitionEvent
name|partitionEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param dbEvent database event    * @throws MetaException    */
specifier|public
name|void
name|onCreateDatabase
parameter_list|(
name|CreateDatabaseEvent
name|dbEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param dbEvent database event    * @throws MetaException    */
specifier|public
name|void
name|onDropDatabase
parameter_list|(
name|DropDatabaseEvent
name|dbEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param partSetDoneEvent    * @throws MetaException    */
specifier|public
name|void
name|onLoadPartitionDone
parameter_list|(
name|LoadPartitionDoneEvent
name|partSetDoneEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param indexEvent index event    * @throws MetaException    */
specifier|public
name|void
name|onAddIndex
parameter_list|(
name|AddIndexEvent
name|indexEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param indexEvent index event    * @throws MetaException    */
specifier|public
name|void
name|onDropIndex
parameter_list|(
name|DropIndexEvent
name|indexEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * @param indexEvent index event    * @throws MetaException    */
specifier|public
name|void
name|onAlterIndex
parameter_list|(
name|AlterIndexEvent
name|indexEvent
parameter_list|)
throws|throws
name|MetaException
block|{   }
comment|/**    * This will be called when an insert is executed that does not cause a partition to be added.    * If an insert causes a partition to be added it will cause {@link #onAddPartition} to be    * called instead.    * @param insertEvent    * @throws MetaException    */
specifier|public
name|void
name|onInsert
parameter_list|(
name|InsertEvent
name|insertEvent
parameter_list|)
throws|throws
name|MetaException
block|{    }
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|config
expr_stmt|;
block|}
block|}
end_class

end_unit


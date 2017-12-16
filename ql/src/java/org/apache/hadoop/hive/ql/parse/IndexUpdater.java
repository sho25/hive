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
name|ql
operator|.
name|parse
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
name|ql
operator|.
name|Driver
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
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|ql
operator|.
name|hooks
operator|.
name|ReadEntity
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|metadata
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
name|ql
operator|.
name|metadata
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
name|ql
operator|.
name|optimizer
operator|.
name|IndexUtils
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
name|ql
operator|.
name|plan
operator|.
name|LoadTableDesc
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
name|ql
operator|.
name|plan
operator|.
name|TableDesc
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
name|ql
operator|.
name|session
operator|.
name|LineageState
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|IndexUpdater
block|{
specifier|private
name|List
argument_list|<
name|LoadTableDesc
argument_list|>
name|loadTableWork
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
comment|// Assumes one instance of this + single-threaded compilation for each query.
specifier|private
name|Hive
name|hive
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|private
name|LineageState
name|lineageState
decl_stmt|;
specifier|public
name|IndexUpdater
parameter_list|(
name|List
argument_list|<
name|LoadTableDesc
argument_list|>
name|loadTableWork
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|LineageState
name|lineageState
parameter_list|)
block|{
name|this
operator|.
name|loadTableWork
operator|=
name|loadTableWork
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|,
name|IndexUpdater
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|lineageState
operator|=
name|lineageState
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
operator|new
name|LinkedList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|IndexUpdater
parameter_list|(
name|LoadTableDesc
name|loadTableWork
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|loadTableWork
operator|=
operator|new
name|LinkedList
argument_list|<
name|LoadTableDesc
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|loadTableWork
operator|.
name|add
argument_list|(
name|loadTableWork
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|,
name|IndexUpdater
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
operator|new
name|LinkedList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|generateUpdateTasks
parameter_list|()
throws|throws
name|HiveException
block|{
name|hive
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
for|for
control|(
name|LoadTableDesc
name|ltd
range|:
name|loadTableWork
control|)
block|{
name|TableDesc
name|td
init|=
name|ltd
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|Table
name|srcTable
init|=
name|hive
operator|.
name|getTable
argument_list|(
name|td
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Index
argument_list|>
name|tblIndexes
init|=
name|IndexUtils
operator|.
name|getAllIndexes
argument_list|(
name|srcTable
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|ltd
operator|.
name|getPartitionSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
operator|||
name|partSpec
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|//unpartitioned table, update whole index
name|doIndexUpdate
argument_list|(
name|tblIndexes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|doIndexUpdate
argument_list|(
name|tblIndexes
argument_list|,
name|partSpec
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tasks
return|;
block|}
specifier|private
name|void
name|doIndexUpdate
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|tblIndexes
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|Index
name|idx
range|:
name|tblIndexes
control|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"ALTER INDEX "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|idx
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ON "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|idx
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|idx
operator|.
name|getOrigTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" REBUILD"
argument_list|)
expr_stmt|;
name|compileRebuild
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doIndexUpdate
parameter_list|(
name|List
argument_list|<
name|Index
argument_list|>
name|tblIndexes
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|Index
name|index
range|:
name|tblIndexes
control|)
block|{
if|if
condition|(
name|containsPartition
argument_list|(
name|index
argument_list|,
name|partSpec
argument_list|)
condition|)
block|{
name|doIndexUpdate
argument_list|(
name|index
argument_list|,
name|partSpec
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|doIndexUpdate
parameter_list|(
name|Index
name|index
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|StringBuilder
name|ps
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|ps
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|key
range|:
name|partSpec
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|ps
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|ps
operator|.
name|append
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|ps
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
name|ps
operator|.
name|append
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ps
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"ALTER INDEX "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|index
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ON "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|index
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|index
operator|.
name|getOrigTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" PARTITION "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|ps
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" REBUILD"
argument_list|)
expr_stmt|;
name|compileRebuild
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|compileRebuild
parameter_list|(
name|String
name|query
parameter_list|)
block|{
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|lineageState
argument_list|)
decl_stmt|;
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|addAll
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|addAll
argument_list|(
name|driver
operator|.
name|getPlan
argument_list|()
operator|.
name|getInputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|containsPartition
parameter_list|(
name|Index
name|index
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
index|[]
name|qualified
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|index
operator|.
name|getDbName
argument_list|()
argument_list|,
name|index
operator|.
name|getIndexTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|indexTable
init|=
name|hive
operator|.
name|getTable
argument_list|(
name|qualified
index|[
literal|0
index|]
argument_list|,
name|qualified
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|hive
operator|.
name|getPartitions
argument_list|(
name|indexTable
argument_list|,
name|partSpec
argument_list|)
decl_stmt|;
return|return
operator|(
name|parts
operator|==
literal|null
operator|||
name|parts
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
return|;
block|}
block|}
end_class

end_unit


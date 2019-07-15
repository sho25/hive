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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|creation
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|ddl
operator|.
name|DDLOperation
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
name|WriteEntity
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
name|InvalidTableException
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
name|PartitionIterable
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
name|parse
operator|.
name|ReplicationSpec
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_comment
comment|/**  * Operation process of dropping a table.  */
end_comment

begin_class
specifier|public
class|class
name|DropTableOperation
extends|extends
name|DDLOperation
argument_list|<
name|DropTableDesc
argument_list|>
block|{
specifier|public
name|DropTableOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|DropTableDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|Table
name|table
init|=
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
comment|// dropping not existing table is handled by DDLSemanticAnalyzer
block|}
if|if
condition|(
name|desc
operator|.
name|getValidationRequired
argument_list|()
condition|)
block|{
if|if
condition|(
name|table
operator|.
name|isView
argument_list|()
operator|||
name|table
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
if|if
condition|(
name|desc
operator|.
name|isIfExists
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|table
operator|.
name|isView
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot drop a view with DROP TABLE"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot drop a materialized view with DROP TABLE"
argument_list|)
throw|;
block|}
block|}
block|}
name|ReplicationSpec
name|replicationSpec
init|=
name|desc
operator|.
name|getReplicationSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
comment|/**        * DROP TABLE FOR REPLICATION behaves differently from DROP TABLE IF EXISTS - it more closely        * matches a DROP TABLE IF OLDER THAN(x) semantic.        *        * Ideally, commands executed under the scope of replication need to be idempotent and resilient        * to repeats. What can happen, sometimes, is that a drone processing a replication task can        * have been abandoned for not returning in time, but still execute its task after a while,        * which should not result in it mucking up data that has been impressed later on. So, for eg.,        * if we create partition P1, followed by droppping it, followed by creating it yet again,        * the replication of that drop should not drop the newer partition if it runs after the destination        * object is already in the newer state.        *        * Thus, we check the replicationSpec.allowEventReplacementInto to determine whether or not we can        * drop the object in question(will return false if object is newer than the event, true if not)        *        * In addition, since DROP TABLE FOR REPLICATION can result in a table not being dropped, while DROP        * TABLE will always drop the table, and the included partitions, DROP TABLE FOR REPLICATION must        * do one more thing - if it does not drop the table because the table is in a newer state, it must        * drop the partitions inside it that are older than this event. To wit, DROP TABLE FOR REPL        * acts like a recursive DROP TABLE IF OLDER.        */
if|if
condition|(
operator|!
name|replicationSpec
operator|.
name|allowEventReplacementInto
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
comment|// Drop occured as part of replicating a drop, but the destination
comment|// table was newer than the event being replicated. Ignore, but drop
comment|// any partitions inside that are older.
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|PartitionIterable
name|partitions
init|=
operator|new
name|PartitionIterable
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
argument_list|,
name|table
argument_list|,
literal|null
argument_list|,
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|BATCH_RETRIEVE_MAX
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Partition
name|p
range|:
name|Iterables
operator|.
name|filter
argument_list|(
name|partitions
argument_list|,
name|replicationSpec
operator|.
name|allowEventReplacementInto
argument_list|()
argument_list|)
control|)
block|{
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|dropPartition
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|p
operator|.
name|getValues
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: Drop Table is skipped as table {} is newer than update"
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
comment|// table is newer, leave it be.
block|}
block|}
comment|// TODO: API w/catalog name
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|dropTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|desc
operator|.
name|isPurge
argument_list|()
argument_list|)
expr_stmt|;
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|table
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|Table
name|getTable
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
block|{
return|return
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit


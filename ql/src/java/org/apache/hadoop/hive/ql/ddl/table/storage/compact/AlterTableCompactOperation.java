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
name|storage
operator|.
name|compact
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
name|io
operator|.
name|AcidUtils
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
name|CompactionResponse
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
name|ShowCompactResponse
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
name|ShowCompactResponseElement
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
name|txn
operator|.
name|TxnStore
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
name|ErrorMsg
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

begin_comment
comment|/**  * Operation process of compacting a table.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableCompactOperation
extends|extends
name|DDLOperation
argument_list|<
name|AlterTableCompactDesc
argument_list|>
block|{
specifier|public
name|AlterTableCompactOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableCompactDesc
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
decl_stmt|;
if|if
condition|(
operator|!
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|NONACID_COMPACTION_NOT_SUPPORTED
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
name|String
name|partitionName
init|=
name|getPartitionName
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|CompactionResponse
name|resp
init|=
name|compact
argument_list|(
name|table
argument_list|,
name|partitionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|isBlocking
argument_list|()
operator|&&
name|resp
operator|.
name|isAccepted
argument_list|()
condition|)
block|{
name|waitForCompactionToFinish
argument_list|(
name|resp
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|String
name|getPartitionName
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|partitionName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getPartitionSpec
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
comment|// Compaction can only be done on the whole table if the table is non-partitioned.
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|NO_COMPACTION_PARTITION
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
init|=
name|desc
operator|.
name|getPartitionSpec
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getPartitions
argument_list|(
name|table
argument_list|,
name|partitionSpec
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitions
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|TOO_MANY_COMPACTION_PARTITIONS
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|partitions
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PARTITION_SPEC
argument_list|)
throw|;
block|}
name|partitionName
operator|=
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
return|return
name|partitionName
return|;
block|}
specifier|private
name|CompactionResponse
name|compact
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|partitionName
parameter_list|)
throws|throws
name|HiveException
block|{
name|CompactionResponse
name|resp
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|compact2
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
name|partitionName
argument_list|,
name|desc
operator|.
name|getCompactionType
argument_list|()
argument_list|,
name|desc
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|.
name|isAccepted
argument_list|()
condition|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Compaction enqueued with id "
operator|+
name|resp
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Compaction already enqueued with id "
operator|+
name|resp
operator|.
name|getId
argument_list|()
operator|+
literal|"; State is "
operator|+
name|resp
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|resp
return|;
block|}
specifier|private
name|void
name|waitForCompactionToFinish
parameter_list|(
name|CompactionResponse
name|resp
parameter_list|)
throws|throws
name|HiveException
block|{
name|StringBuilder
name|progressDots
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|long
name|waitTimeMs
init|=
literal|1000
decl_stmt|;
name|long
name|waitTimeOut
init|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_WAIT_TIMEOUT
argument_list|)
decl_stmt|;
name|wait
label|:
while|while
condition|(
literal|true
condition|)
block|{
comment|//double wait time until 5min
name|waitTimeMs
operator|=
name|waitTimeMs
operator|*
literal|2
expr_stmt|;
name|waitTimeMs
operator|=
name|Math
operator|.
name|max
argument_list|(
name|waitTimeMs
argument_list|,
name|waitTimeOut
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTimeMs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Interrupted while waiting for compaction with id="
operator|+
name|resp
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
comment|//this could be expensive when there are a lot of compactions....
comment|//todo: update to search by ID once HIVE-13353 is done
name|ShowCompactResponse
name|allCompactions
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|showCompactions
argument_list|()
decl_stmt|;
for|for
control|(
name|ShowCompactResponseElement
name|compaction
range|:
name|allCompactions
operator|.
name|getCompacts
argument_list|()
control|)
block|{
if|if
condition|(
name|resp
operator|.
name|getId
argument_list|()
operator|!=
name|compaction
operator|.
name|getId
argument_list|()
condition|)
block|{
continue|continue;
block|}
switch|switch
condition|(
name|compaction
operator|.
name|getState
argument_list|()
condition|)
block|{
case|case
name|TxnStore
operator|.
name|WORKING_RESPONSE
case|:
case|case
name|TxnStore
operator|.
name|INITIATED_RESPONSE
case|:
comment|//still working
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
name|progressDots
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|progressDots
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
continue|continue
name|wait
continue|;
default|default:
comment|//done
name|context
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Compaction with id "
operator|+
name|resp
operator|.
name|getId
argument_list|()
operator|+
literal|" finished with status: "
operator|+
name|compaction
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
break|break
name|wait
break|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit


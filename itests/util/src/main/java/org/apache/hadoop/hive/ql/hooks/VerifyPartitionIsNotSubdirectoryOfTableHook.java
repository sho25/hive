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
name|hooks
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
comment|// This hook verifies that the location of every partition in the inputs and outputs does not
end_comment

begin_comment
comment|// start with the location of the table.  It is a very simple check to make sure the location is
end_comment

begin_comment
comment|// not a subdirectory.
end_comment

begin_class
specifier|public
class|class
name|VerifyPartitionIsNotSubdirectoryOfTableHook
implements|implements
name|ExecuteWithHookContext
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
for|for
control|(
name|WriteEntity
name|output
range|:
name|hookContext
operator|.
name|getOutputs
argument_list|()
control|)
block|{
if|if
condition|(
name|output
operator|.
name|getType
argument_list|()
operator|==
name|WriteEntity
operator|.
name|Type
operator|.
name|PARTITION
condition|)
block|{
name|verify
argument_list|(
name|output
operator|.
name|getPartition
argument_list|()
argument_list|,
name|output
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|ReadEntity
name|input
range|:
name|hookContext
operator|.
name|getInputs
argument_list|()
control|)
block|{
if|if
condition|(
name|input
operator|.
name|getType
argument_list|()
operator|==
name|ReadEntity
operator|.
name|Type
operator|.
name|PARTITION
condition|)
block|{
name|verify
argument_list|(
name|input
operator|.
name|getPartition
argument_list|()
argument_list|,
name|input
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|verify
parameter_list|(
name|Partition
name|partition
parameter_list|,
name|Table
name|table
parameter_list|)
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"The location of the partition: "
operator|+
name|partition
operator|.
name|getName
argument_list|()
operator|+
literal|" was a "
operator|+
literal|"subdirectory of the location of the table: "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partition
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|table
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


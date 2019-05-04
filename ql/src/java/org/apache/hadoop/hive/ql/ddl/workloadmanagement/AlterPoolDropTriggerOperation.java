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
name|workloadmanagement
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|WMTrigger
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

begin_comment
comment|/**  * Operation process of dropping a trigger to pool mapping.  */
end_comment

begin_class
specifier|public
class|class
name|AlterPoolDropTriggerOperation
extends|extends
name|DDLOperation
block|{
specifier|private
specifier|final
name|AlterPoolDropTriggerDesc
name|desc
decl_stmt|;
specifier|public
name|AlterPoolDropTriggerOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterPoolDropTriggerDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
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
throws|,
name|IOException
block|{
if|if
condition|(
operator|!
name|desc
operator|.
name|isUnmanagedPool
argument_list|()
condition|)
block|{
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|createOrDropTriggerToPoolMapping
argument_list|(
name|desc
operator|.
name|getPlanName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTriggerName
argument_list|()
argument_list|,
name|desc
operator|.
name|getPoolPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|desc
operator|.
name|getPoolPath
argument_list|()
operator|==
literal|null
assert|;
name|WMTrigger
name|trigger
init|=
operator|new
name|WMTrigger
argument_list|(
name|desc
operator|.
name|getPlanName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTriggerName
argument_list|()
argument_list|)
decl_stmt|;
comment|// If we are dropping from unmanaged, unset the flag; and vice versa
name|trigger
operator|.
name|setIsInUnmanaged
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|alterWMTrigger
argument_list|(
name|trigger
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit


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
name|lockmgr
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

begin_class
specifier|public
class|class
name|HiveLockObject
block|{
name|String
index|[]
name|pathNames
init|=
literal|null
decl_stmt|;
comment|/* user supplied data for that object */
specifier|private
name|String
name|data
decl_stmt|;
specifier|public
name|HiveLockObject
parameter_list|()
block|{
name|this
operator|.
name|data
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|String
name|lockData
parameter_list|)
block|{
name|this
operator|.
name|pathNames
operator|=
name|paths
expr_stmt|;
name|this
operator|.
name|data
operator|=
name|lockData
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|String
name|lockData
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|String
index|[]
block|{
name|tbl
operator|.
name|getDbName
argument_list|()
block|,
name|tbl
operator|.
name|getTableName
argument_list|()
block|}
argument_list|,
name|lockData
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|Partition
name|par
parameter_list|,
name|String
name|lockData
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|String
index|[]
block|{
name|par
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
block|,
name|par
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
block|,
name|par
operator|.
name|getName
argument_list|()
block|}
argument_list|,
name|lockData
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|pathNames
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|ret
init|=
literal|""
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pathNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|ret
operator|=
name|ret
operator|+
literal|"@"
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|ret
operator|=
name|ret
operator|+
name|pathNames
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|String
name|getData
parameter_list|()
block|{
return|return
name|data
return|;
block|}
specifier|public
name|void
name|setData
parameter_list|(
name|String
name|data
parameter_list|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
block|}
block|}
end_class

end_unit


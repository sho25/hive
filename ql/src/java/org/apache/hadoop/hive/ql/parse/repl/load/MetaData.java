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
name|parse
operator|.
name|repl
operator|.
name|load
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
name|api
operator|.
name|Database
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
name|Function
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
name|metastore
operator|.
name|api
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

begin_comment
comment|/**  * Utility class to help return complex value from readMetaData function  */
end_comment

begin_class
specifier|public
class|class
name|MetaData
block|{
specifier|private
specifier|final
name|Database
name|db
decl_stmt|;
specifier|private
specifier|final
name|Table
name|table
decl_stmt|;
specifier|private
specifier|final
name|Iterable
argument_list|<
name|Partition
argument_list|>
name|partitions
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|public
specifier|final
name|Function
name|function
decl_stmt|;
specifier|public
name|MetaData
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|ReplicationSpec
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetaData
parameter_list|(
name|Database
name|db
parameter_list|,
name|Table
name|table
parameter_list|,
name|Iterable
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Function
name|function
parameter_list|)
block|{
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partitions
operator|=
name|partitions
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|function
operator|=
name|function
expr_stmt|;
block|}
specifier|public
name|Database
name|getDatabase
parameter_list|()
block|{
return|return
name|db
return|;
block|}
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|Iterable
argument_list|<
name|Partition
argument_list|>
name|getPartitions
parameter_list|()
block|{
return|return
name|partitions
return|;
block|}
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
return|return
name|replicationSpec
return|;
block|}
block|}
end_class

end_unit


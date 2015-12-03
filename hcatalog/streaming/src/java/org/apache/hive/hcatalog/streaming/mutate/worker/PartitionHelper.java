begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|fs
operator|.
name|Path
import|;
end_import

begin_comment
comment|/** Implementations are responsible for creating and obtaining path information about partitions. */
end_comment

begin_interface
interface|interface
name|PartitionHelper
extends|extends
name|Closeable
block|{
comment|/** Return the location of the partition described by the provided values. */
name|Path
name|getPathForPartition
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|newPartitionValues
parameter_list|)
throws|throws
name|WorkerException
function_decl|;
comment|/** Create the partition described by the provided values if it does not exist already. */
name|void
name|createPartitionIfNotExists
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|newPartitionValues
parameter_list|)
throws|throws
name|WorkerException
function_decl|;
block|}
end_interface

end_unit


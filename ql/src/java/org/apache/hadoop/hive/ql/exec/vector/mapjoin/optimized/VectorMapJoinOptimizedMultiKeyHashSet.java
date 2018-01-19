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
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|optimized
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
name|exec
operator|.
name|persistence
operator|.
name|MapJoinTableContainer
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
name|persistence
operator|.
name|MapJoinTableContainer
operator|.
name|ReusableGetAdaptor
import|;
end_import

begin_comment
comment|/*  * An multi-key hash map based on the BytesBytesMultiHashSet.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedMultiKeyHashSet
extends|extends
name|VectorMapJoinOptimizedHashSet
block|{
comment|// UNDONE: How to look for all NULLs in a multi-key?????  Let nulls through for now.
specifier|public
name|VectorMapJoinOptimizedMultiKeyHashSet
parameter_list|(
name|boolean
name|isOuterJoin
parameter_list|,
name|MapJoinTableContainer
name|originalTableContainer
parameter_list|,
name|ReusableGetAdaptor
name|hashMapRowGetter
parameter_list|)
block|{
name|super
argument_list|(
name|originalTableContainer
argument_list|,
name|hashMapRowGetter
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


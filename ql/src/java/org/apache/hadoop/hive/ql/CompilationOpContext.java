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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|ColumnStatsList
import|;
end_import

begin_comment
comment|/**  * A subset of compilation context that is passed to operators to get rid of some globals.  * Perhaps this should be rolled into main Context; however, some code necessitates storing the  * context in the operators for now, so this may not be advisable given how much stuff the main  * Context class contains.  * For now, only the operator sequence ID lives here.  */
end_comment

begin_class
specifier|public
class|class
name|CompilationOpContext
block|{
specifier|private
specifier|final
name|AtomicInteger
name|opSeqId
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ColumnStatsList
argument_list|>
name|colStatsCache
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|int
name|nextOperatorId
parameter_list|()
block|{
return|return
name|opSeqId
operator|.
name|getAndIncrement
argument_list|()
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ColumnStatsList
argument_list|>
name|getColStatsCache
parameter_list|()
block|{
return|return
name|colStatsCache
return|;
block|}
block|}
end_class

end_unit


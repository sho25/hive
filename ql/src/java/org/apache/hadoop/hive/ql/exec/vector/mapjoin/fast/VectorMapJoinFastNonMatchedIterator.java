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
name|fast
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
name|MatchTracker
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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinNonMatchedIterator
import|;
end_import

begin_comment
comment|/**  * The abstract class for vectorized non-match Small Table key iteration.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinFastNonMatchedIterator
extends|extends
name|VectorMapJoinNonMatchedIterator
block|{
specifier|protected
name|int
name|nonMatchedLogicalSlotNum
decl_stmt|;
specifier|public
name|VectorMapJoinFastNonMatchedIterator
parameter_list|(
name|MatchTracker
name|matchTracker
parameter_list|)
block|{
name|super
argument_list|(
name|matchTracker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|()
block|{
name|nonMatchedLogicalSlotNum
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
end_class

end_unit


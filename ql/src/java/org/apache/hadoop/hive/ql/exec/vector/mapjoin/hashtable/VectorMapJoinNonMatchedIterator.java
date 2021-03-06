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
name|hashtable
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * The abstract class for vectorized non-match Small Table key iteration.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinNonMatchedIterator
block|{
specifier|protected
specifier|final
name|MatchTracker
name|matchTracker
decl_stmt|;
specifier|protected
name|int
name|nonMatchedLogicalSlotNum
decl_stmt|;
specifier|public
name|VectorMapJoinNonMatchedIterator
parameter_list|(
name|MatchTracker
name|matchTracker
parameter_list|)
block|{
name|this
operator|.
name|matchTracker
operator|=
name|matchTracker
expr_stmt|;
block|}
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
specifier|public
name|boolean
name|findNextNonMatched
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|readNonMatchedLongKey
parameter_list|()
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|long
name|getNonMatchedLongKey
parameter_list|()
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|readNonMatchedBytesKey
parameter_list|()
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|byte
index|[]
name|getNonMatchedBytes
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|int
name|getNonMatchedBytesOffset
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|int
name|getNonMatchedBytesLength
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
specifier|public
name|VectorMapJoinHashMapResult
name|getNonMatchedHashMapResult
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not implemented"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit


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
name|hive
operator|.
name|streaming
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|HeartBeatFailure
extends|extends
name|StreamingException
block|{
specifier|private
name|Collection
argument_list|<
name|Long
argument_list|>
name|abortedTxns
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|Long
argument_list|>
name|nosuchTxns
decl_stmt|;
specifier|public
name|HeartBeatFailure
parameter_list|(
name|Collection
argument_list|<
name|Long
argument_list|>
name|abortedTxns
parameter_list|,
name|Set
argument_list|<
name|Long
argument_list|>
name|nosuchTxns
parameter_list|)
block|{
name|super
argument_list|(
literal|"Heart beat error. InvalidTxns: "
operator|+
name|nosuchTxns
operator|+
literal|". AbortedTxns: "
operator|+
name|abortedTxns
argument_list|)
expr_stmt|;
name|this
operator|.
name|abortedTxns
operator|=
name|abortedTxns
expr_stmt|;
name|this
operator|.
name|nosuchTxns
operator|=
name|nosuchTxns
expr_stmt|;
block|}
block|}
end_class

end_unit


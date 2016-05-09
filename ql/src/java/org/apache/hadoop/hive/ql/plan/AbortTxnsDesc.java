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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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

begin_comment
comment|/**  * Descriptor for aborting transactions.  */
end_comment

begin_class
specifier|public
class|class
name|AbortTxnsDesc
extends|extends
name|DDLDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|txnids
decl_stmt|;
comment|/**    *  No arg constructor for serialization.    */
specifier|public
name|AbortTxnsDesc
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|txnids
parameter_list|)
block|{
name|this
operator|.
name|txnids
operator|=
name|txnids
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Long
argument_list|>
name|getTxnids
parameter_list|()
block|{
return|return
name|txnids
return|;
block|}
block|}
end_class

end_unit


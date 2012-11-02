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
name|optimizer
operator|.
name|listbucketingpruner
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|Partition
import|;
end_import

begin_comment
comment|/**  * The processor context for list bucketing pruner. This contains the table alias  * that is being currently processed.  */
end_comment

begin_class
specifier|public
class|class
name|LBExprProcCtx
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * The table alias that is being currently processed.    */
specifier|private
name|String
name|tabAlias
decl_stmt|;
comment|// partition walker working on
specifier|private
specifier|final
name|Partition
name|part
decl_stmt|;
specifier|public
name|LBExprProcCtx
parameter_list|(
name|String
name|tabAlias
parameter_list|,
name|Partition
name|part
parameter_list|)
block|{
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|part
operator|=
name|part
expr_stmt|;
block|}
comment|/**    *    * @return    */
specifier|public
name|String
name|getTabAlias
parameter_list|()
block|{
return|return
name|tabAlias
return|;
block|}
comment|/**    *    * @param tabAlias    */
specifier|public
name|void
name|setTabAlias
parameter_list|(
name|String
name|tabAlias
parameter_list|)
block|{
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
block|}
comment|/**    * @return the part    */
specifier|public
name|Partition
name|getPart
parameter_list|()
block|{
return|return
name|part
return|;
block|}
block|}
end_class

end_unit


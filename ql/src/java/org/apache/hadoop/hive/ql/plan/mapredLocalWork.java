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
name|LinkedHashMap
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
name|Operator
import|;
end_import

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Map Reduce Local Work"
argument_list|)
specifier|public
class|class
name|mapredLocalWork
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
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|fetchWork
argument_list|>
name|aliasToFetchWork
decl_stmt|;
specifier|public
name|mapredLocalWork
parameter_list|()
block|{   }
specifier|public
name|mapredLocalWork
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|,
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|fetchWork
argument_list|>
name|aliasToFetchWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
name|this
operator|.
name|aliasToFetchWork
operator|=
name|aliasToFetchWork
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Alias -> Map Local Operator Tree"
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getAliasToWork
parameter_list|()
block|{
return|return
name|aliasToWork
return|;
block|}
specifier|public
name|void
name|setAliasToWork
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToWork
operator|=
name|aliasToWork
expr_stmt|;
block|}
comment|/**    * @return the aliasToFetchWork    */
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Alias -> Map Local Tables"
argument_list|)
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|fetchWork
argument_list|>
name|getAliasToFetchWork
parameter_list|()
block|{
return|return
name|aliasToFetchWork
return|;
block|}
comment|/**    * @param aliasToFetchWork    *          the aliasToFetchWork to set    */
specifier|public
name|void
name|setAliasToFetchWork
parameter_list|(
specifier|final
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|fetchWork
argument_list|>
name|aliasToFetchWork
parameter_list|)
block|{
name|this
operator|.
name|aliasToFetchWork
operator|=
name|aliasToFetchWork
expr_stmt|;
block|}
block|}
end_class

end_unit


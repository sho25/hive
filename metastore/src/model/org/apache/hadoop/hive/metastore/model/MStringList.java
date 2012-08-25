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
name|metastore
operator|.
name|model
package|;
end_package

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
comment|/**  *  * It represents data structure of string list.  *  * workaround JDO limitation: no support for collection of collection.  *  */
end_comment

begin_class
specifier|public
class|class
name|MStringList
block|{
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|internalList
decl_stmt|;
comment|/**    *    * @param list    */
specifier|public
name|MStringList
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|list
parameter_list|)
block|{
name|this
operator|.
name|internalList
operator|=
name|list
expr_stmt|;
block|}
comment|/**    * @return the internalList    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getInternalList
parameter_list|()
block|{
return|return
name|internalList
return|;
block|}
comment|/**    * @param internalList the internalList to set    */
specifier|public
name|void
name|setInternalList
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|internalList
parameter_list|)
block|{
name|this
operator|.
name|internalList
operator|=
name|internalList
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|internalList
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit


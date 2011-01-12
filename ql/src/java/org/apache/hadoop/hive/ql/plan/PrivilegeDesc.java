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
name|security
operator|.
name|authorization
operator|.
name|Privilege
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Privilege"
argument_list|)
specifier|public
class|class
name|PrivilegeDesc
implements|implements
name|Serializable
implements|,
name|Cloneable
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
name|Privilege
name|privilege
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columns
decl_stmt|;
specifier|public
name|PrivilegeDesc
parameter_list|(
name|Privilege
name|privilege
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|privilege
operator|=
name|privilege
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
block|}
specifier|public
name|PrivilegeDesc
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return privilege definition    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"privilege"
argument_list|)
specifier|public
name|Privilege
name|getPrivilege
parameter_list|()
block|{
return|return
name|privilege
return|;
block|}
comment|/**    * @param privilege    */
specifier|public
name|void
name|setPrivilege
parameter_list|(
name|Privilege
name|privilege
parameter_list|)
block|{
name|this
operator|.
name|privilege
operator|=
name|privilege
expr_stmt|;
block|}
comment|/**    * @return columns on which the given privilege take affect.    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
comment|/**    * @param columns    */
specifier|public
name|void
name|setColumns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
block|}
block|}
end_class

end_unit


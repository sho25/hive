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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * DropTableDesc.  * TODO: this is currently used for both drop table and drop partitions.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Drop Table"
argument_list|)
specifier|public
class|class
name|DropTableDesc
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
specifier|public
specifier|static
class|class
name|PartSpec
block|{
specifier|public
name|PartSpec
parameter_list|(
name|ExprNodeGenericFuncDesc
name|partSpec
parameter_list|,
name|int
name|prefixLength
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
name|this
operator|.
name|prefixLength
operator|=
name|prefixLength
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
specifier|public
name|int
name|getPrefixLength
parameter_list|()
block|{
return|return
name|prefixLength
return|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|ExprNodeGenericFuncDesc
name|partSpec
decl_stmt|;
comment|// TODO: see if we can get rid of this... used in one place to distinguish archived parts
specifier|private
name|int
name|prefixLength
decl_stmt|;
block|}
name|String
name|tableName
decl_stmt|;
name|ArrayList
argument_list|<
name|PartSpec
argument_list|>
name|partSpecs
decl_stmt|;
name|boolean
name|expectView
decl_stmt|;
name|boolean
name|ifExists
decl_stmt|;
name|boolean
name|ignoreProtection
decl_stmt|;
specifier|public
name|DropTableDesc
parameter_list|()
block|{   }
comment|/**    * @param tableName    */
specifier|public
name|DropTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|expectView
parameter_list|,
name|boolean
name|ifExists
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partSpecs
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
name|this
operator|.
name|ifExists
operator|=
name|ifExists
expr_stmt|;
name|this
operator|.
name|ignoreProtection
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|DropTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|partSpecs
parameter_list|,
name|boolean
name|expectView
parameter_list|,
name|boolean
name|ignoreProtection
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partSpecs
operator|=
operator|new
name|ArrayList
argument_list|<
name|PartSpec
argument_list|>
argument_list|(
name|partSpecs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|partSpec
range|:
name|partSpecs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|prefixLength
init|=
name|partSpec
operator|.
name|getKey
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeGenericFuncDesc
name|expr
range|:
name|partSpec
operator|.
name|getValue
argument_list|()
control|)
block|{
name|this
operator|.
name|partSpecs
operator|.
name|add
argument_list|(
operator|new
name|PartSpec
argument_list|(
name|expr
argument_list|,
name|prefixLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|ignoreProtection
operator|=
name|ignoreProtection
expr_stmt|;
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
block|}
comment|/**    * @return the tableName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table"
argument_list|)
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * @param tableName    *          the tableName to set    */
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
comment|/**    * @return the partSpecs    */
specifier|public
name|ArrayList
argument_list|<
name|PartSpec
argument_list|>
name|getPartSpecs
parameter_list|()
block|{
return|return
name|partSpecs
return|;
block|}
comment|/**    * @return whether or not protection will be ignored for the partition    */
specifier|public
name|boolean
name|getIgnoreProtection
parameter_list|()
block|{
return|return
name|ignoreProtection
return|;
block|}
comment|/**    * @param ignoreProtection    *          set whether or not protection will be ignored for the partition    */
specifier|public
name|void
name|setIgnoreProtection
parameter_list|(
name|boolean
name|ignoreProtection
parameter_list|)
block|{
name|this
operator|.
name|ignoreProtection
operator|=
name|ignoreProtection
expr_stmt|;
block|}
comment|/**    * @return whether to expect a view being dropped    */
specifier|public
name|boolean
name|getExpectView
parameter_list|()
block|{
return|return
name|expectView
return|;
block|}
comment|/**    * @param expectView    *          set whether to expect a view being dropped    */
specifier|public
name|void
name|setExpectView
parameter_list|(
name|boolean
name|expectView
parameter_list|)
block|{
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
block|}
comment|/**    * @return whether IF EXISTS was specified    */
specifier|public
name|boolean
name|getIfExists
parameter_list|()
block|{
return|return
name|ifExists
return|;
block|}
comment|/**    * @param ifExists    *          set whether IF EXISTS was specified    */
specifier|public
name|void
name|setIfExists
parameter_list|(
name|boolean
name|ifExists
parameter_list|)
block|{
name|this
operator|.
name|ifExists
operator|=
name|ifExists
expr_stmt|;
block|}
block|}
end_class

end_unit


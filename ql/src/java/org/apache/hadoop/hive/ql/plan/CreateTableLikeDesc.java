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
name|Map
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * CreateTableLikeDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Table"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|CreateTableLikeDesc
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
name|String
name|tableName
decl_stmt|;
name|boolean
name|isExternal
decl_stmt|;
name|String
name|defaultInputFormat
decl_stmt|;
name|String
name|defaultOutputFormat
decl_stmt|;
name|String
name|defaultSerName
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|defaultSerdeProps
decl_stmt|;
name|String
name|location
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
decl_stmt|;
name|boolean
name|ifNotExists
decl_stmt|;
name|String
name|likeTableName
decl_stmt|;
name|boolean
name|isTemporary
init|=
literal|false
decl_stmt|;
name|boolean
name|isUserStorageFormat
init|=
literal|false
decl_stmt|;
specifier|public
name|CreateTableLikeDesc
parameter_list|()
block|{   }
specifier|public
name|CreateTableLikeDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|boolean
name|isTemporary
parameter_list|,
name|String
name|defaultInputFormat
parameter_list|,
name|String
name|defaultOutputFormat
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|defaultSerName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|defaultSerdeProps
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|String
name|likeTableName
parameter_list|,
name|boolean
name|isUserStorageFormat
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
name|isExternal
operator|=
name|isExternal
expr_stmt|;
name|this
operator|.
name|isTemporary
operator|=
name|isTemporary
expr_stmt|;
name|this
operator|.
name|defaultInputFormat
operator|=
name|defaultInputFormat
expr_stmt|;
name|this
operator|.
name|defaultOutputFormat
operator|=
name|defaultOutputFormat
expr_stmt|;
name|this
operator|.
name|defaultSerName
operator|=
name|defaultSerName
expr_stmt|;
name|this
operator|.
name|defaultSerdeProps
operator|=
name|defaultSerdeProps
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|tblProps
operator|=
name|tblProps
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|likeTableName
operator|=
name|likeTableName
expr_stmt|;
name|this
operator|.
name|isUserStorageFormat
operator|=
name|isUserStorageFormat
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|getIfNotExists
parameter_list|()
block|{
return|return
name|ifNotExists
return|;
block|}
specifier|public
name|void
name|setIfNotExists
parameter_list|(
name|boolean
name|ifNotExists
parameter_list|)
block|{
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"default input format"
argument_list|)
specifier|public
name|String
name|getDefaultInputFormat
parameter_list|()
block|{
return|return
name|defaultInputFormat
return|;
block|}
specifier|public
name|void
name|setInputFormat
parameter_list|(
name|String
name|inputFormat
parameter_list|)
block|{
name|this
operator|.
name|defaultInputFormat
operator|=
name|inputFormat
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"default output format"
argument_list|)
specifier|public
name|String
name|getDefaultOutputFormat
parameter_list|()
block|{
return|return
name|defaultOutputFormat
return|;
block|}
specifier|public
name|void
name|setOutputFormat
parameter_list|(
name|String
name|outputFormat
parameter_list|)
block|{
name|this
operator|.
name|defaultOutputFormat
operator|=
name|outputFormat
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"location"
argument_list|)
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"isExternal"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isExternal
parameter_list|()
block|{
return|return
name|isExternal
return|;
block|}
specifier|public
name|void
name|setExternal
parameter_list|(
name|boolean
name|isExternal
parameter_list|)
block|{
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
block|}
comment|/**    * @return the default serDeName    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"default serde name"
argument_list|)
specifier|public
name|String
name|getDefaultSerName
parameter_list|()
block|{
return|return
name|defaultSerName
return|;
block|}
comment|/**    * @param serName    *          the serName to set    */
specifier|public
name|void
name|setDefaultSerName
parameter_list|(
name|String
name|serName
parameter_list|)
block|{
name|this
operator|.
name|defaultSerName
operator|=
name|serName
expr_stmt|;
block|}
comment|/**    * @return the default serDe properties    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"serde properties"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getDefaultSerdeProps
parameter_list|()
block|{
return|return
name|defaultSerdeProps
return|;
block|}
comment|/**    * @param serdeProps    *          the default serde properties to set    */
specifier|public
name|void
name|setDefaultSerdeProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeProps
parameter_list|)
block|{
name|this
operator|.
name|defaultSerdeProps
operator|=
name|serdeProps
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"like"
argument_list|)
specifier|public
name|String
name|getLikeTableName
parameter_list|()
block|{
return|return
name|likeTableName
return|;
block|}
specifier|public
name|void
name|setLikeTableName
parameter_list|(
name|String
name|likeTableName
parameter_list|)
block|{
name|this
operator|.
name|likeTableName
operator|=
name|likeTableName
expr_stmt|;
block|}
comment|/**    * @return the table properties    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table properties"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getTblProps
parameter_list|()
block|{
return|return
name|tblProps
return|;
block|}
comment|/**    * @param tblProps    *          the table properties to set    */
specifier|public
name|void
name|setTblProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
parameter_list|)
block|{
name|this
operator|.
name|tblProps
operator|=
name|tblProps
expr_stmt|;
block|}
comment|/**    * @return the isTemporary    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"isTemporary"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isTemporary
parameter_list|()
block|{
return|return
name|isTemporary
return|;
block|}
comment|/**    * @param isTemporary table is Temporary or not.    */
specifier|public
name|void
name|setTemporary
parameter_list|(
name|boolean
name|isTemporary
parameter_list|)
block|{
name|this
operator|.
name|isTemporary
operator|=
name|isTemporary
expr_stmt|;
block|}
comment|/**    * True if user has specified storage format in query    * @return boolean    */
specifier|public
name|boolean
name|isUserStorageFormat
parameter_list|()
block|{
return|return
name|this
operator|.
name|isUserStorageFormat
return|;
block|}
block|}
end_class

end_unit


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
name|HashMap
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
name|fs
operator|.
name|Path
import|;
end_import

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Show Table Status"
argument_list|)
specifier|public
class|class
name|showTableStatusDesc
extends|extends
name|ddlDesc
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
name|pattern
decl_stmt|;
name|Path
name|resFile
decl_stmt|;
name|String
name|dbName
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
comment|/**    * table name for the result of show tables    */
specifier|private
specifier|final
name|String
name|table
init|=
literal|"show_tablestatus"
decl_stmt|;
comment|/**    * thrift ddl for the result of show tables    */
specifier|private
specifier|final
name|String
name|schema
init|=
literal|"tab_name#string"
decl_stmt|;
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|String
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
comment|/**    * @param pattern    *          names of tables to show    */
specifier|public
name|showTableStatusDesc
parameter_list|(
name|Path
name|resFile
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|pattern
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
block|}
comment|/**    * @param resFile    * @param dbName data base name    * @param pattern names of tables to show    * @param part partition specification    */
specifier|public
name|showTableStatusDesc
parameter_list|(
name|Path
name|resFile
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|pattern
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
comment|/**    * @return the pattern    */
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"pattern"
argument_list|)
specifier|public
name|String
name|getPattern
parameter_list|()
block|{
return|return
name|pattern
return|;
block|}
comment|/**    * @param pattern    *          the pattern to set    */
specifier|public
name|void
name|setPattern
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
block|}
comment|/**    * @return the resFile    */
specifier|public
name|Path
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"result file"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|String
name|getResFileString
parameter_list|()
block|{
return|return
name|getResFile
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * @param resFile    *          the resFile to set    */
specifier|public
name|void
name|setResFile
parameter_list|(
name|Path
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
block|}
comment|/**    * @return the database name    */
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"database"
argument_list|)
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
comment|/**    * @param dbName    *          the database name    */
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
comment|/**    * @return the partSpec    */
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"partition"
argument_list|)
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
comment|/**    * @param partSpec the partSpec to set    */
specifier|public
name|void
name|setPartSpecs
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
block|}
end_class

end_unit


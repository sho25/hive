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
comment|/**  * ShowIndexesDesc.  * Returns table index information per SQL syntax.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Indexes"
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
name|ShowIndexesDesc
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
name|String
name|resFile
decl_stmt|;
name|boolean
name|isFormatted
decl_stmt|;
comment|/**    * thrift ddl for the result of show indexes.    */
specifier|private
specifier|static
specifier|final
name|String
name|schema
init|=
literal|"idx_name,tab_name,col_names,idx_tab_name,idx_type,comment"
operator|+
literal|"#string:string:string:string:string:string"
decl_stmt|;
specifier|public
specifier|static
name|String
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
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
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
specifier|public
name|boolean
name|isFormatted
parameter_list|()
block|{
return|return
name|isFormatted
return|;
block|}
specifier|public
name|void
name|setFormatted
parameter_list|(
name|boolean
name|isFormatted
parameter_list|)
block|{
name|this
operator|.
name|isFormatted
operator|=
name|isFormatted
expr_stmt|;
block|}
comment|/**    *    * @param tableName    *          Name of the table whose indexes need to be listed.    * @param resFile    *          File to store the results in.    */
specifier|public
name|ShowIndexesDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Path
name|resFile
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
name|resFile
operator|=
name|resFile
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


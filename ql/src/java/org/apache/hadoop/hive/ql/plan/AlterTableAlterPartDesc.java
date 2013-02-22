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
name|api
operator|.
name|FieldSchema
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

begin_class
specifier|public
class|class
name|AlterTableAlterPartDesc
extends|extends
name|DDLDesc
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|FieldSchema
name|partKeySpec
decl_stmt|;
specifier|public
name|AlterTableAlterPartDesc
parameter_list|()
block|{   }
comment|/**    * @param dbName    *          database that contains the table / partition    * @param tableName    *          table containing the partition    * @param partKeySpec    *          key column specification.    */
specifier|public
name|AlterTableAlterPartDesc
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|FieldSchema
name|partKeySpec
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partKeySpec
operator|=
name|partKeySpec
expr_stmt|;
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
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
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
specifier|public
name|FieldSchema
name|getPartKeySpec
parameter_list|()
block|{
return|return
name|partKeySpec
return|;
block|}
specifier|public
name|void
name|setPartKeySpec
parameter_list|(
name|FieldSchema
name|partKeySpec
parameter_list|)
block|{
name|this
operator|.
name|partKeySpec
operator|=
name|partKeySpec
expr_stmt|;
block|}
block|}
end_class

end_unit


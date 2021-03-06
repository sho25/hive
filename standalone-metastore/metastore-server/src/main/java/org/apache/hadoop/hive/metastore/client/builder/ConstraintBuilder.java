begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|builder
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|conf
operator|.
name|Configuration
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
name|metastore
operator|.
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|metastore
operator|.
name|utils
operator|.
name|MetaStoreUtils
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

begin_comment
comment|/**  * Base builder for all types of constraints.  Database name, table name, and column name  * must be provided.  * @param<T> Type of builder extending this.  */
end_comment

begin_class
specifier|abstract
class|class
name|ConstraintBuilder
parameter_list|<
name|T
parameter_list|>
block|{
specifier|protected
name|String
name|catName
decl_stmt|,
name|dbName
decl_stmt|,
name|tableName
decl_stmt|,
name|constraintName
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
decl_stmt|;
specifier|protected
name|boolean
name|enable
decl_stmt|,
name|validate
decl_stmt|,
name|rely
decl_stmt|;
specifier|private
name|int
name|nextSeq
decl_stmt|;
specifier|private
name|T
name|child
decl_stmt|;
specifier|protected
name|ConstraintBuilder
parameter_list|()
block|{
name|nextSeq
operator|=
literal|1
expr_stmt|;
name|enable
operator|=
literal|true
expr_stmt|;
name|validate
operator|=
name|rely
operator|=
literal|false
expr_stmt|;
name|dbName
operator|=
name|Warehouse
operator|.
name|DEFAULT_DATABASE_NAME
expr_stmt|;
name|columns
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|setChild
parameter_list|(
name|T
name|child
parameter_list|)
block|{
name|this
operator|.
name|child
operator|=
name|child
expr_stmt|;
block|}
specifier|protected
name|void
name|checkBuildable
parameter_list|(
name|String
name|defaultConstraintName
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
name|tableName
operator|==
literal|null
operator|||
name|columns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"You must provide table name and columns"
argument_list|)
throw|;
block|}
if|if
condition|(
name|constraintName
operator|==
literal|null
condition|)
block|{
name|constraintName
operator|=
name|tableName
operator|+
literal|"_"
operator|+
name|defaultConstraintName
expr_stmt|;
block|}
if|if
condition|(
name|catName
operator|==
literal|null
condition|)
name|catName
operator|=
name|MetaStoreUtils
operator|.
name|getDefaultCatalog
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|int
name|getNextSeq
parameter_list|()
block|{
return|return
name|nextSeq
operator|++
return|;
block|}
specifier|public
name|T
name|setCatName
parameter_list|(
name|String
name|catName
parameter_list|)
block|{
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
return|return
name|child
return|;
block|}
specifier|public
name|T
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
return|return
name|child
return|;
block|}
specifier|public
name|T
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
return|return
name|child
return|;
block|}
specifier|public
name|T
name|onTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|this
operator|.
name|catName
operator|=
name|table
operator|.
name|getCatName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|table
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|table
operator|.
name|getTableName
argument_list|()
expr_stmt|;
return|return
name|child
return|;
block|}
specifier|public
name|T
name|addColumn
parameter_list|(
name|String
name|columnName
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
return|return
name|child
return|;
block|}
specifier|public
name|T
name|setConstraintName
parameter_list|(
name|String
name|constraintName
parameter_list|)
block|{
name|this
operator|.
name|constraintName
operator|=
name|constraintName
expr_stmt|;
return|return
name|child
return|;
block|}
specifier|public
name|T
name|setEnable
parameter_list|(
name|boolean
name|enable
parameter_list|)
block|{
name|this
operator|.
name|enable
operator|=
name|enable
expr_stmt|;
return|return
name|child
return|;
block|}
specifier|public
name|T
name|setValidate
parameter_list|(
name|boolean
name|validate
parameter_list|)
block|{
name|this
operator|.
name|validate
operator|=
name|validate
expr_stmt|;
return|return
name|child
return|;
block|}
specifier|public
name|T
name|setRely
parameter_list|(
name|boolean
name|rely
parameter_list|)
block|{
name|this
operator|.
name|rely
operator|=
name|rely
expr_stmt|;
return|return
name|child
return|;
block|}
block|}
end_class

end_unit


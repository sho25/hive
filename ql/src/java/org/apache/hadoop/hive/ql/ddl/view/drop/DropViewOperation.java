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
name|ddl
operator|.
name|view
operator|.
name|drop
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
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|ddl
operator|.
name|DDLOperation
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
name|hooks
operator|.
name|WriteEntity
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
name|HiveException
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
name|InvalidTableException
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
name|Table
import|;
end_import

begin_comment
comment|/**  * Operation process of dropping a view.  */
end_comment

begin_class
specifier|public
class|class
name|DropViewOperation
extends|extends
name|DDLOperation
argument_list|<
name|DropViewDesc
argument_list|>
block|{
specifier|public
name|DropViewOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|DropViewDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|Table
name|table
init|=
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
comment|// dropping not existing view is handled by DropViewAnalyzer
block|}
if|if
condition|(
operator|!
name|table
operator|.
name|isView
argument_list|()
condition|)
block|{
if|if
condition|(
name|desc
operator|.
name|isIfExists
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|table
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot drop a materialized view with DROP VIEW"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot drop a base table with DROP VIEW"
argument_list|)
throw|;
block|}
block|}
comment|// TODO: API w/catalog name
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|dropTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|table
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|Table
name|getTable
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
block|{
return|return
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit


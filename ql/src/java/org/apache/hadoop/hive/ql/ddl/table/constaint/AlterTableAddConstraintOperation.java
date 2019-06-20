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
name|table
operator|.
name|constaint
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
name|collections
operator|.
name|CollectionUtils
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
name|InvalidObjectException
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
name|NoSuchObjectException
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
name|table
operator|.
name|AbstractAlterTableWithConstraintsDesc
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
name|Hive
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

begin_comment
comment|/**  * Operation process of adding a new constraint.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableAddConstraintOperation
extends|extends
name|DDLOperation
argument_list|<
name|AlterTableAddConstraintDesc
argument_list|>
block|{
specifier|public
name|AlterTableAddConstraintOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableAddConstraintDesc
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
name|Exception
block|{
if|if
condition|(
operator|!
name|DDLUtils
operator|.
name|allowOperationInReplicationScope
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|desc
operator|.
name|getReplicationSpec
argument_list|()
argument_list|)
condition|)
block|{
comment|// no alter, the table is missing either due to drop/rename which follows the alter.
comment|// or the existing table is newer than our update.
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: Alter Table is skipped as table {} is newer than update"
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|addConstraints
argument_list|(
name|desc
argument_list|,
name|context
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
comment|// This function is used by other operations that may modify the constraints
specifier|public
specifier|static
name|void
name|addConstraints
parameter_list|(
name|AbstractAlterTableWithConstraintsDesc
name|desc
parameter_list|,
name|Hive
name|db
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|Constraints
name|constraints
init|=
name|desc
operator|.
name|getConstraints
argument_list|()
decl_stmt|;
comment|// This is either an alter table add foreign key or add primary key command.
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|constraints
operator|.
name|getPrimaryKeys
argument_list|()
argument_list|)
condition|)
block|{
name|db
operator|.
name|addPrimaryKey
argument_list|(
name|constraints
operator|.
name|getPrimaryKeys
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|constraints
operator|.
name|getForeignKeys
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|db
operator|.
name|addForeignKey
argument_list|(
name|constraints
operator|.
name|getForeignKeys
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|InvalidObjectException
operator|&&
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|!=
literal|null
operator|&&
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
comment|// During repl load, NoSuchObjectException in foreign key shall
comment|// ignore as the foreign table may not be part of the replication
name|LOG
operator|.
name|debug
argument_list|(
literal|"InvalidObjectException: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|constraints
operator|.
name|getUniqueConstraints
argument_list|()
argument_list|)
condition|)
block|{
name|db
operator|.
name|addUniqueConstraint
argument_list|(
name|constraints
operator|.
name|getUniqueConstraints
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|constraints
operator|.
name|getNotNullConstraints
argument_list|()
argument_list|)
condition|)
block|{
name|db
operator|.
name|addNotNullConstraint
argument_list|(
name|constraints
operator|.
name|getNotNullConstraints
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|constraints
operator|.
name|getDefaultConstraints
argument_list|()
argument_list|)
condition|)
block|{
name|db
operator|.
name|addDefaultConstraint
argument_list|(
name|constraints
operator|.
name|getDefaultConstraints
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|constraints
operator|.
name|getCheckConstraints
argument_list|()
argument_list|)
condition|)
block|{
name|db
operator|.
name|addCheckConstraint
argument_list|(
name|constraints
operator|.
name|getCheckConstraints
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


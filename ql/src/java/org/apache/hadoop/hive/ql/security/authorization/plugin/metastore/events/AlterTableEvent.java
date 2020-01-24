begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|metastore
operator|.
name|events
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|events
operator|.
name|PreAlterTableEvent
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
name|events
operator|.
name|PreEventContext
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
name|plugin
operator|.
name|HiveOperationType
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
name|plugin
operator|.
name|HivePrivilegeObject
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
name|plugin
operator|.
name|metastore
operator|.
name|HiveMetaStoreAuthorizableEvent
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
name|plugin
operator|.
name|metastore
operator|.
name|HiveMetaStoreAuthzInfo
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
comment|/*  Authorizable Event for HiveMetaStore operation  AlterTableEvent  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableEvent
extends|extends
name|HiveMetaStoreAuthorizableEvent
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AlterTableEvent
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|COMMAND_STR
init|=
literal|"alter table"
decl_stmt|;
specifier|public
name|AlterTableEvent
parameter_list|(
name|PreEventContext
name|preEventContext
parameter_list|)
block|{
name|super
argument_list|(
name|preEventContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveMetaStoreAuthzInfo
name|getAuthzContext
parameter_list|()
block|{
name|HiveMetaStoreAuthzInfo
name|ret
init|=
operator|new
name|HiveMetaStoreAuthzInfo
argument_list|(
name|preEventContext
argument_list|,
name|getOperationType
argument_list|()
argument_list|,
name|getInputHObjs
argument_list|()
argument_list|,
name|getOutputHObjs
argument_list|()
argument_list|,
name|COMMAND_STR
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
specifier|private
name|HiveOperationType
name|getOperationType
parameter_list|()
block|{
name|PreAlterTableEvent
name|event
init|=
operator|(
name|PreAlterTableEvent
operator|)
name|preEventContext
decl_stmt|;
name|Table
name|table
init|=
name|event
operator|.
name|getNewTable
argument_list|()
decl_stmt|;
name|Table
name|oldTable
init|=
name|event
operator|.
name|getOldTable
argument_list|()
decl_stmt|;
name|String
name|newUri
init|=
operator|(
name|table
operator|!=
literal|null
operator|)
condition|?
name|getSdLocation
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
argument_list|)
else|:
literal|""
decl_stmt|;
name|String
name|oldUri
init|=
operator|(
name|oldTable
operator|!=
literal|null
operator|)
condition|?
name|getSdLocation
argument_list|(
name|oldTable
operator|.
name|getSd
argument_list|()
argument_list|)
else|:
literal|""
decl_stmt|;
return|return
name|StringUtils
operator|.
name|equals
argument_list|(
name|oldUri
argument_list|,
name|newUri
argument_list|)
condition|?
name|HiveOperationType
operator|.
name|ALTERTABLE_ADDCOLS
else|:
name|HiveOperationType
operator|.
name|ALTERTABLE_LOCATION
return|;
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|getInputHObjs
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"==> AlterTableEvent.getInputHObjs()"
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|PreAlterTableEvent
name|event
init|=
operator|(
name|PreAlterTableEvent
operator|)
name|preEventContext
decl_stmt|;
name|Table
name|oldTable
init|=
name|event
operator|.
name|getOldTable
argument_list|()
decl_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|getHivePrivilegeObject
argument_list|(
name|oldTable
argument_list|)
argument_list|)
expr_stmt|;
name|COMMAND_STR
operator|=
name|buildCommandString
argument_list|(
name|COMMAND_STR
argument_list|,
name|oldTable
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"<== AlterTableEvent.getInputHObjs(): ret="
operator|+
name|ret
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|getOutputHObjs
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"==> AlterTableEvent.getOutputHObjs()"
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|PreAlterTableEvent
name|event
init|=
operator|(
name|PreAlterTableEvent
operator|)
name|preEventContext
decl_stmt|;
name|Table
name|newTable
init|=
name|event
operator|.
name|getNewTable
argument_list|()
decl_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|getHivePrivilegeObject
argument_list|(
name|newTable
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|oldTable
init|=
name|event
operator|.
name|getOldTable
argument_list|()
decl_stmt|;
name|String
name|oldUri
init|=
operator|(
name|oldTable
operator|!=
literal|null
operator|)
condition|?
name|getSdLocation
argument_list|(
name|oldTable
operator|.
name|getSd
argument_list|()
argument_list|)
else|:
literal|""
decl_stmt|;
name|String
name|newUri
init|=
name|getSdLocation
argument_list|(
name|newTable
operator|.
name|getSd
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|StringUtils
operator|.
name|equals
argument_list|(
name|oldUri
argument_list|,
name|newUri
argument_list|)
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|getHivePrivilegeObjectDfsUri
argument_list|(
name|newUri
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"<== AlterTableEvent.getOutputHObjs(): ret="
operator|+
name|ret
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|private
name|String
name|buildCommandString
parameter_list|(
name|String
name|cmdStr
parameter_list|,
name|Table
name|tbl
parameter_list|)
block|{
name|String
name|ret
init|=
name|cmdStr
decl_stmt|;
if|if
condition|(
name|tbl
operator|!=
literal|null
condition|)
block|{
name|String
name|tblName
init|=
name|tbl
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|ret
operator|=
name|ret
operator|+
operator|(
name|StringUtils
operator|.
name|isNotEmpty
argument_list|(
name|tblName
argument_list|)
condition|?
literal|" "
operator|+
name|tblName
else|:
literal|""
operator|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit


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
name|security
operator|.
name|authorization
operator|.
name|command
package|;
end_package

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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|Database
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
name|exec
operator|.
name|FunctionInfo
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
name|exec
operator|.
name|FunctionUtils
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
name|exec
operator|.
name|FunctionInfo
operator|.
name|FunctionType
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
name|Entity
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
name|ReadEntity
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
name|hooks
operator|.
name|Entity
operator|.
name|Type
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
name|ql
operator|.
name|parse
operator|.
name|BaseSemanticAnalyzer
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
name|HiveOperation
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
name|AuthorizationUtils
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
name|HiveAuthzContext
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
name|HivePrivilegeObject
operator|.
name|HivePrivObjectActionType
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
operator|.
name|HivePrivilegeObjectType
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * Command authorization, new type.  */
end_comment

begin_class
specifier|final
class|class
name|CommandAuthorizerV2
block|{
specifier|private
name|CommandAuthorizerV2
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"CommandAuthorizerV2 should not be instantiated"
argument_list|)
throw|;
block|}
specifier|static
name|void
name|doAuthorization
parameter_list|(
name|HiveOperation
name|op
parameter_list|,
name|BaseSemanticAnalyzer
name|sem
parameter_list|,
name|SessionState
name|ss
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|String
name|command
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveOperationType
name|hiveOpType
init|=
name|HiveOperationType
operator|.
name|valueOf
argument_list|(
name|op
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
comment|// colAccessInfo is set only in case of SemanticAnalyzer
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|selectTab2Cols
init|=
name|sem
operator|.
name|getColumnAccessInfo
argument_list|()
operator|!=
literal|null
condition|?
name|sem
operator|.
name|getColumnAccessInfo
argument_list|()
operator|.
name|getTableToColumnAccessMap
argument_list|()
else|:
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|updateTab2Cols
init|=
name|sem
operator|.
name|getUpdateColumnAccessInfo
argument_list|()
operator|!=
literal|null
condition|?
name|sem
operator|.
name|getUpdateColumnAccessInfo
argument_list|()
operator|.
name|getTableToColumnAccessMap
argument_list|()
else|:
literal|null
decl_stmt|;
name|List
argument_list|<
name|ReadEntity
argument_list|>
name|inputList
init|=
operator|new
name|ArrayList
argument_list|<
name|ReadEntity
argument_list|>
argument_list|(
name|inputs
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|WriteEntity
argument_list|>
name|outputList
init|=
operator|new
name|ArrayList
argument_list|<
name|WriteEntity
argument_list|>
argument_list|(
name|outputs
argument_list|)
decl_stmt|;
name|addPermanentFunctionEntities
argument_list|(
name|ss
argument_list|,
name|inputList
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputsHObjs
init|=
name|getHivePrivObjects
argument_list|(
name|inputList
argument_list|,
name|selectTab2Cols
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
init|=
name|getHivePrivObjects
argument_list|(
name|outputList
argument_list|,
name|updateTab2Cols
argument_list|)
decl_stmt|;
name|HiveAuthzContext
operator|.
name|Builder
name|authzContextBuilder
init|=
operator|new
name|HiveAuthzContext
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|authzContextBuilder
operator|.
name|setUserIpAddress
argument_list|(
name|ss
operator|.
name|getUserIpAddress
argument_list|()
argument_list|)
expr_stmt|;
name|authzContextBuilder
operator|.
name|setForwardedAddresses
argument_list|(
name|ss
operator|.
name|getForwardedAddresses
argument_list|()
argument_list|)
expr_stmt|;
name|authzContextBuilder
operator|.
name|setCommandString
argument_list|(
name|command
argument_list|)
expr_stmt|;
name|ss
operator|.
name|getAuthorizerV2
argument_list|()
operator|.
name|checkPrivileges
argument_list|(
name|hiveOpType
argument_list|,
name|inputsHObjs
argument_list|,
name|outputHObjs
argument_list|,
name|authzContextBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|addPermanentFunctionEntities
parameter_list|(
name|SessionState
name|ss
parameter_list|,
name|List
argument_list|<
name|ReadEntity
argument_list|>
name|inputList
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|FunctionInfo
argument_list|>
name|function
range|:
name|ss
operator|.
name|getCurrentFunctionsInUse
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|function
operator|.
name|getValue
argument_list|()
operator|.
name|getFunctionType
argument_list|()
operator|!=
name|FunctionType
operator|.
name|PERSISTENT
condition|)
block|{
comment|// Built-in function access is allowed to all users. If user can create a temp function, they may use it.
continue|continue;
block|}
name|String
index|[]
name|qualifiedFunctionName
init|=
name|FunctionUtils
operator|.
name|getQualifiedFunctionNameParts
argument_list|(
name|function
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
comment|// this is only for the purpose of authorization, only the name matters.
name|Database
name|db
init|=
operator|new
name|Database
argument_list|(
name|qualifiedFunctionName
index|[
literal|0
index|]
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|inputList
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|db
argument_list|,
name|qualifiedFunctionName
index|[
literal|1
index|]
argument_list|,
name|function
operator|.
name|getValue
argument_list|()
operator|.
name|getClassName
argument_list|()
argument_list|,
name|Type
operator|.
name|FUNCTION
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|getHivePrivObjects
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Entity
argument_list|>
name|privObjects
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableName2Cols
parameter_list|)
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|hivePrivobjs
init|=
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|privObjects
operator|==
literal|null
condition|)
block|{
return|return
name|hivePrivobjs
return|;
block|}
for|for
control|(
name|Entity
name|privObject
range|:
name|privObjects
control|)
block|{
if|if
condition|(
name|privObject
operator|.
name|isDummy
argument_list|()
condition|)
block|{
comment|//do not authorize dummy readEntity or writeEntity
continue|continue;
block|}
if|if
condition|(
name|privObject
operator|instanceof
name|ReadEntity
operator|&&
operator|!
operator|(
operator|(
name|ReadEntity
operator|)
name|privObject
operator|)
operator|.
name|isDirect
argument_list|()
condition|)
block|{
comment|// This ReadEntity represents one of the underlying tables/views of a view, so skip it.
continue|continue;
block|}
if|if
condition|(
name|privObject
operator|instanceof
name|WriteEntity
operator|&&
operator|(
operator|(
name|WriteEntity
operator|)
name|privObject
operator|)
operator|.
name|isTempURI
argument_list|()
condition|)
block|{
comment|// do not authorize temporary uris
continue|continue;
block|}
if|if
condition|(
name|privObject
operator|.
name|getTyp
argument_list|()
operator|==
name|Type
operator|.
name|TABLE
operator|&&
operator|(
name|privObject
operator|.
name|getT
argument_list|()
operator|==
literal|null
operator|||
name|privObject
operator|.
name|getT
argument_list|()
operator|.
name|isTemporary
argument_list|()
operator|)
condition|)
block|{
comment|// skip temporary tables from authorization
continue|continue;
block|}
name|addHivePrivObject
argument_list|(
name|privObject
argument_list|,
name|tableName2Cols
argument_list|,
name|hivePrivobjs
argument_list|)
expr_stmt|;
block|}
return|return
name|hivePrivobjs
return|;
block|}
specifier|private
specifier|static
name|void
name|addHivePrivObject
parameter_list|(
name|Entity
name|privObject
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableName2Cols
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|hivePrivObjs
parameter_list|)
block|{
name|HivePrivilegeObjectType
name|privObjType
init|=
name|AuthorizationUtils
operator|.
name|getHivePrivilegeObjectType
argument_list|(
name|privObject
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|HivePrivObjectActionType
name|actionType
init|=
name|AuthorizationUtils
operator|.
name|getActionType
argument_list|(
name|privObject
argument_list|)
decl_stmt|;
name|HivePrivilegeObject
name|hivePrivObject
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|privObject
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|DATABASE
case|:
name|Database
name|database
init|=
name|privObject
operator|.
name|getDatabase
argument_list|()
decl_stmt|;
name|hivePrivObject
operator|=
operator|new
name|HivePrivilegeObject
argument_list|(
name|privObjType
argument_list|,
name|database
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionType
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|database
operator|.
name|getOwnerName
argument_list|()
argument_list|,
name|database
operator|.
name|getOwnerType
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|TABLE
case|:
name|Table
name|table
init|=
name|privObject
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|tableName2Cols
operator|==
literal|null
condition|?
literal|null
else|:
name|tableName2Cols
operator|.
name|get
argument_list|(
name|Table
operator|.
name|getCompleteName
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|hivePrivObject
operator|=
operator|new
name|HivePrivilegeObject
argument_list|(
name|privObjType
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|columns
argument_list|,
name|actionType
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|table
operator|.
name|getOwner
argument_list|()
argument_list|,
name|table
operator|.
name|getOwnerType
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DFS_DIR
case|:
case|case
name|LOCAL_DIR
case|:
name|hivePrivObject
operator|=
operator|new
name|HivePrivilegeObject
argument_list|(
name|privObjType
argument_list|,
literal|null
argument_list|,
name|privObject
operator|.
name|getD
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionType
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
break|break;
case|case
name|FUNCTION
case|:
name|String
name|dbName
init|=
name|privObject
operator|.
name|getDatabase
argument_list|()
operator|!=
literal|null
condition|?
name|privObject
operator|.
name|getDatabase
argument_list|()
operator|.
name|getName
argument_list|()
else|:
literal|null
decl_stmt|;
name|hivePrivObject
operator|=
operator|new
name|HivePrivilegeObject
argument_list|(
name|privObjType
argument_list|,
name|dbName
argument_list|,
name|privObject
operator|.
name|getFunctionName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionType
argument_list|,
literal|null
argument_list|,
name|privObject
operator|.
name|getClassName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
break|break;
case|case
name|DUMMYPARTITION
case|:
case|case
name|PARTITION
case|:
comment|// TODO: not currently handled
return|return;
case|case
name|SERVICE_NAME
case|:
name|hivePrivObject
operator|=
operator|new
name|HivePrivilegeObject
argument_list|(
name|privObjType
argument_list|,
literal|null
argument_list|,
name|privObject
operator|.
name|getServiceName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionType
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected object type"
argument_list|)
throw|;
block|}
name|hivePrivObjs
operator|.
name|add
argument_list|(
name|hivePrivObject
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


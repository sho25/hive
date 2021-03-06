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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|HiveAccessControlException
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
name|HiveAuthzPluginException
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|ColumnDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|OperationState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|OperationType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|TableSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
operator|.
name|HiveSession
import|;
end_import

begin_comment
comment|/**  * MetadataOperation.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MetadataOperation
extends|extends
name|Operation
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|DEFAULT_HIVE_CATALOG
init|=
literal|""
decl_stmt|;
specifier|protected
specifier|static
name|TableSchema
name|RESULT_SET_SCHEMA
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|SEARCH_STRING_ESCAPE
init|=
literal|'\\'
decl_stmt|;
specifier|protected
name|MetadataOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|OperationType
name|opType
parameter_list|)
block|{
name|super
argument_list|(
name|parentSession
argument_list|,
name|opType
argument_list|)
expr_stmt|;
name|setHasResultSet
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hive.service.cli.Operation#close()    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveSQLException
block|{
name|setState
argument_list|(
name|OperationState
operator|.
name|CLOSED
argument_list|)
expr_stmt|;
name|cleanupOperationLog
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convert wildchars and escape sequence from JDBC format to datanucleous/regex    */
specifier|protected
name|String
name|convertIdentifierPattern
parameter_list|(
specifier|final
name|String
name|pattern
parameter_list|,
name|boolean
name|datanucleusFormat
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
condition|)
block|{
return|return
name|convertPattern
argument_list|(
literal|"%"
argument_list|,
literal|true
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|convertPattern
argument_list|(
name|pattern
argument_list|,
name|datanucleusFormat
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert wildchars and escape sequence of schema pattern from JDBC format to datanucleous/regex    * The schema pattern treats empty string also as wildchar    */
specifier|protected
name|String
name|convertSchemaPattern
parameter_list|(
specifier|final
name|String
name|pattern
parameter_list|)
block|{
if|if
condition|(
operator|(
name|pattern
operator|==
literal|null
operator|)
operator|||
name|pattern
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|convertPattern
argument_list|(
literal|"%"
argument_list|,
literal|true
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|convertPattern
argument_list|(
name|pattern
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
comment|/**    * Convert a pattern containing JDBC catalog search wildcards into    * Java regex patterns.    *    * @param pattern input which may contain '%' or '_' wildcard characters, or    * these characters escaped using {@link #getSearchStringEscape()}.    * @return replace %/_ with regex search characters, also handle escaped    * characters.    *    * The datanucleus module expects the wildchar as '*'. The columns search on the    * other hand is done locally inside the hive code and that requires the regex wildchar    * format '.*'  This is driven by the datanucleusFormat flag.    */
specifier|private
name|String
name|convertPattern
parameter_list|(
name|String
name|pattern
parameter_list|,
name|boolean
name|datanucleusFormat
parameter_list|)
block|{
name|String
name|wStr
decl_stmt|;
if|if
condition|(
name|datanucleusFormat
condition|)
block|{
name|wStr
operator|=
literal|"*"
expr_stmt|;
block|}
else|else
block|{
name|wStr
operator|=
literal|".*"
expr_stmt|;
block|}
name|pattern
operator|=
name|replaceAll
argument_list|(
name|pattern
argument_list|,
literal|"([^\\\\])%"
argument_list|,
literal|"$1"
operator|+
name|wStr
argument_list|)
expr_stmt|;
name|pattern
operator|=
name|replaceAll
argument_list|(
name|pattern
argument_list|,
literal|"\\\\%"
argument_list|,
literal|"%"
argument_list|)
expr_stmt|;
name|pattern
operator|=
name|replaceAll
argument_list|(
name|pattern
argument_list|,
literal|"^%"
argument_list|,
name|wStr
argument_list|)
expr_stmt|;
name|pattern
operator|=
name|replaceAll
argument_list|(
name|pattern
argument_list|,
literal|"([^\\\\])_"
argument_list|,
literal|"$1."
argument_list|)
expr_stmt|;
name|pattern
operator|=
name|replaceAll
argument_list|(
name|pattern
argument_list|,
literal|"\\\\_"
argument_list|,
literal|"_"
argument_list|)
expr_stmt|;
name|pattern
operator|=
name|replaceAll
argument_list|(
name|pattern
argument_list|,
literal|"^_"
argument_list|,
literal|"."
argument_list|)
expr_stmt|;
return|return
name|pattern
return|;
block|}
specifier|private
name|String
name|replaceAll
parameter_list|(
name|String
name|input
parameter_list|,
specifier|final
name|String
name|pattern
parameter_list|,
specifier|final
name|String
name|replace
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|String
name|replaced
init|=
name|input
operator|.
name|replaceAll
argument_list|(
name|pattern
argument_list|,
name|replace
argument_list|)
decl_stmt|;
if|if
condition|(
name|replaced
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
return|return
name|replaced
return|;
block|}
name|input
operator|=
name|replaced
expr_stmt|;
block|}
block|}
specifier|protected
name|boolean
name|isAuthV2Enabled
parameter_list|()
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
return|return
operator|(
name|ss
operator|.
name|isAuthorizationModeV2
argument_list|()
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|ss
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|)
operator|)
return|;
block|}
specifier|protected
name|void
name|authorizeMetaGets
parameter_list|(
name|HiveOperationType
name|opType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inpObjs
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|authorizeMetaGets
argument_list|(
name|opType
argument_list|,
name|inpObjs
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|authorizeMetaGets
parameter_list|(
name|HiveOperationType
name|opType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inpObjs
parameter_list|,
name|String
name|cmdString
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|HiveAuthzContext
operator|.
name|Builder
name|ctxBuilder
init|=
operator|new
name|HiveAuthzContext
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|ctxBuilder
operator|.
name|setUserIpAddress
argument_list|(
name|ss
operator|.
name|getUserIpAddress
argument_list|()
argument_list|)
expr_stmt|;
name|ctxBuilder
operator|.
name|setForwardedAddresses
argument_list|(
name|ss
operator|.
name|getForwardedAddresses
argument_list|()
argument_list|)
expr_stmt|;
name|ctxBuilder
operator|.
name|setCommandString
argument_list|(
name|cmdString
argument_list|)
expr_stmt|;
try|try
block|{
name|ss
operator|.
name|getAuthorizerV2
argument_list|()
operator|.
name|checkPrivileges
argument_list|(
name|opType
argument_list|,
name|inpObjs
argument_list|,
literal|null
argument_list|,
name|ctxBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveAuthzPluginException
decl||
name|HiveAccessControlException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|cancel
parameter_list|(
name|OperationState
name|stateAfterCancel
parameter_list|)
throws|throws
name|HiveSQLException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"MetadataOperation.cancel()"
argument_list|)
throw|;
block|}
specifier|protected
name|String
name|getDebugMessage
parameter_list|(
specifier|final
name|String
name|type
parameter_list|,
specifier|final
name|TableSchema
name|resultSetSchema
parameter_list|)
block|{
name|StringBuilder
name|debugMessage
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|debugMessage
operator|.
name|append
argument_list|(
literal|"Returning "
argument_list|)
expr_stmt|;
name|debugMessage
operator|.
name|append
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|debugMessage
operator|.
name|append
argument_list|(
literal|" metadata: "
argument_list|)
expr_stmt|;
name|boolean
name|firstColumn
init|=
literal|true
decl_stmt|;
for|for
control|(
name|ColumnDescriptor
name|column
range|:
name|resultSetSchema
operator|.
name|getColumnDescriptors
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|firstColumn
condition|)
block|{
name|debugMessage
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|debugMessage
operator|.
name|append
argument_list|(
name|column
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|debugMessage
operator|.
name|append
argument_list|(
literal|"={}"
argument_list|)
expr_stmt|;
name|firstColumn
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|debugMessage
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit


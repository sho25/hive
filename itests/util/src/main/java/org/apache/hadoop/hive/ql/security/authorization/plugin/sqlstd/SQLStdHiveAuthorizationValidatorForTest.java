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
name|plugin
operator|.
name|sqlstd
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
name|HashSet
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
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
name|parse
operator|.
name|SemanticException
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
name|HiveAuthenticationProvider
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
name|HiveAuthzSessionContext
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
name|HiveMetastoreClientFactory
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Extends SQLStdHiveAuthorizationValidator to relax the restriction of not  * being able to run dfs,set commands. To be used for testing purposes only!  *  * In addition, it parses a setting test.hive.authz.sstd.validator.bypassObjTypes  * as a comma-separated list of object types, which, if present, it will bypass  * validations of all input and output objects of those types.  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|SQLStdHiveAuthorizationValidatorForTest
extends|extends
name|SQLStdHiveAuthorizationValidator
block|{
specifier|final
name|String
name|BYPASS_OBJTYPES_KEY
init|=
literal|"test.hive.authz.sstd.validator.bypassObjTypes"
decl_stmt|;
name|Set
argument_list|<
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
argument_list|>
name|bypassObjectTypes
decl_stmt|;
specifier|public
name|SQLStdHiveAuthorizationValidatorForTest
parameter_list|(
name|HiveMetastoreClientFactory
name|metastoreClientFactory
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|HiveAuthenticationProvider
name|authenticator
parameter_list|,
name|SQLStdHiveAccessControllerWrapper
name|privController
parameter_list|,
name|HiveAuthzSessionContext
name|ctx
parameter_list|)
throws|throws
name|HiveAuthzPluginException
block|{
name|super
argument_list|(
name|metastoreClientFactory
argument_list|,
name|conf
argument_list|,
name|authenticator
argument_list|,
name|privController
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|setupBypass
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|BYPASS_OBJTYPES_KEY
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupBypass
parameter_list|(
name|String
name|bypassObjectTypesConf
parameter_list|)
block|{
name|bypassObjectTypes
operator|=
operator|new
name|HashSet
argument_list|<
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
argument_list|>
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|bypassObjectTypesConf
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|bypassType
range|:
name|bypassObjectTypesConf
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
if|if
condition|(
operator|(
name|bypassType
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|bypassType
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|bypassObjectTypes
operator|.
name|add
argument_list|(
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
operator|.
name|valueOf
argument_list|(
name|bypassType
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filterForBypass
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privilegeObjects
parameter_list|)
block|{
if|if
condition|(
name|privilegeObjects
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Iterables
operator|.
name|filter
argument_list|(
name|privilegeObjects
argument_list|,
operator|new
name|Predicate
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
annotation|@
name|Nullable
name|HivePrivilegeObject
name|hivePrivilegeObject
parameter_list|)
block|{
comment|// Return true to retain an item, and false to filter it out.
if|if
condition|(
name|hivePrivilegeObject
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
operator|!
name|bypassObjectTypes
operator|.
name|contains
argument_list|(
name|hivePrivilegeObject
operator|.
name|getType
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkPrivileges
parameter_list|(
name|HiveOperationType
name|hiveOpType
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|inputHObjs
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|outputHObjs
parameter_list|,
name|HiveAuthzContext
name|context
parameter_list|)
throws|throws
name|HiveAuthzPluginException
throws|,
name|HiveAccessControlException
block|{
switch|switch
condition|(
name|hiveOpType
condition|)
block|{
case|case
name|DFS
case|:
case|case
name|SET
case|:
comment|// allow SET and DFS commands to be used during testing
return|return;
default|default:
name|super
operator|.
name|checkPrivileges
argument_list|(
name|hiveOpType
argument_list|,
name|filterForBypass
argument_list|(
name|inputHObjs
argument_list|)
argument_list|,
name|filterForBypass
argument_list|(
name|outputHObjs
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|needTransform
parameter_list|()
block|{
comment|// In the future, we can add checking for username, groupname, etc based on
comment|// HiveAuthenticationProvider. For example,
comment|// "hive_test_user".equals(context.getUserName());
return|return
literal|true
return|;
block|}
comment|// Please take a look at the instructions in HiveAuthorizer.java before
comment|// implementing applyRowFilterAndColumnMasking
specifier|public
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|applyRowFilterAndColumnMasking
parameter_list|(
name|HiveAuthzContext
name|context
parameter_list|,
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|privObjs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|needRewritePrivObjs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|privObj
range|:
name|privObjs
control|)
block|{
if|if
condition|(
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"masking_test"
argument_list|)
operator|||
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"masking_test_n"
argument_list|)
condition|)
block|{
name|privObj
operator|.
name|setRowFilterExpression
argument_list|(
literal|"key % 2 = 0 and key< 10"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cellValueTransformers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|columnName
range|:
name|privObj
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|columnName
operator|.
name|equals
argument_list|(
literal|"value"
argument_list|)
condition|)
block|{
name|cellValueTransformers
operator|.
name|add
argument_list|(
literal|"reverse(value)"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cellValueTransformers
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
block|}
block|}
name|privObj
operator|.
name|setCellValueTransformers
argument_list|(
name|cellValueTransformers
argument_list|)
expr_stmt|;
name|needRewritePrivObjs
operator|.
name|add
argument_list|(
name|privObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"masking_test_view"
argument_list|)
operator|||
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"masking_test_view_n"
argument_list|)
condition|)
block|{
name|privObj
operator|.
name|setRowFilterExpression
argument_list|(
literal|"key> 6"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cellValueTransformers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|columnName
range|:
name|privObj
operator|.
name|getColumns
argument_list|()
control|)
block|{
if|if
condition|(
name|columnName
operator|.
name|equals
argument_list|(
literal|"key"
argument_list|)
condition|)
block|{
name|cellValueTransformers
operator|.
name|add
argument_list|(
literal|"key / 2"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cellValueTransformers
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
block|}
block|}
name|privObj
operator|.
name|setCellValueTransformers
argument_list|(
name|cellValueTransformers
argument_list|)
expr_stmt|;
name|needRewritePrivObjs
operator|.
name|add
argument_list|(
name|privObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"masking_test_subq"
argument_list|)
operator|||
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"masking_test_subq_n"
argument_list|)
condition|)
block|{
name|privObj
operator|.
name|setRowFilterExpression
argument_list|(
literal|"key in (select key from src where src.key = "
operator|+
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|+
literal|".key)"
argument_list|)
expr_stmt|;
name|needRewritePrivObjs
operator|.
name|add
argument_list|(
name|privObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"masking_acid_no_masking"
argument_list|)
operator|||
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"masking_acid_no_masking_n"
argument_list|)
condition|)
block|{
comment|// testing acid usage when no masking/filtering is present
name|needRewritePrivObjs
operator|.
name|add
argument_list|(
name|privObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"masking_test_druid"
argument_list|)
operator|||
name|privObj
operator|.
name|getObjectName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"masking_test_druid_n"
argument_list|)
condition|)
block|{
comment|// testing druid queries row filtering is present
name|privObj
operator|.
name|setRowFilterExpression
argument_list|(
literal|"key> 10"
argument_list|)
expr_stmt|;
name|needRewritePrivObjs
operator|.
name|add
argument_list|(
name|privObj
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|needRewritePrivObjs
return|;
block|}
block|}
end_class

end_unit


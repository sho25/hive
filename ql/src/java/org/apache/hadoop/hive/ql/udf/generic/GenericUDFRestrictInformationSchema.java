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
name|udf
operator|.
name|generic
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|Description
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
name|UDFArgumentException
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
name|UDFArgumentLengthException
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
name|HiveUtils
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
name|HiveMetastoreAuthorizationProvider
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
name|HiveAuthorizer
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|udf
operator|.
name|UDFType
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|io
operator|.
name|BooleanWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * UDF to determine whether to enforce restriction of information schema.  * This is intended for internal usage only. This function is not a deterministic function,  * but a runtime constant. The return value is constant within a query but can be different between queries  */
end_comment

begin_class
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|false
argument_list|,
name|runtimeConstant
operator|=
literal|true
argument_list|)
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"restrict_information_schema"
argument_list|,
name|value
operator|=
literal|"_FUNC_() - Returns whether or not to enable information schema restriction. "
operator|+
literal|"Currently it is enabled if either HS2 authorizer or metastore authorizer implements policy provider "
operator|+
literal|"interface."
argument_list|)
annotation|@
name|NDV
argument_list|(
name|maxNdv
operator|=
literal|1
argument_list|)
specifier|public
class|class
name|GenericUDFRestrictInformationSchema
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenericUDFRestrictInformationSchema
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|BooleanWritable
name|enabled
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function RestrictInformationSchema does not take any arguments, but found "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|enabled
operator|==
literal|null
condition|)
block|{
name|boolean
name|enableHS2PolicyProvider
init|=
literal|false
decl_stmt|;
name|boolean
name|enableMetastorePolicyProvider
init|=
literal|false
decl_stmt|;
name|HiveConf
name|hiveConf
init|=
name|SessionState
operator|.
name|getSessionConf
argument_list|()
decl_stmt|;
name|HiveAuthorizer
name|authorizer
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthorizerV2
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|authorizer
operator|.
name|getHivePolicyProvider
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|enableHS2PolicyProvider
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveAuthzPluginException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error getting HivePolicyProvider"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|enableHS2PolicyProvider
condition|)
block|{
if|if
condition|(
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PRE_EVENT_LISTENERS
argument_list|)
operator|!=
literal|null
operator|&&
operator|!
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|PRE_EVENT_LISTENERS
argument_list|)
operator|.
name|isEmpty
argument_list|()
operator|&&
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|HiveMetastoreAuthorizationProvider
argument_list|>
name|authorizerProviders
decl_stmt|;
try|try
block|{
name|authorizerProviders
operator|=
name|HiveUtils
operator|.
name|getMetaStoreAuthorizeProviderManagers
argument_list|(
name|hiveConf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthenticator
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HiveMetastoreAuthorizationProvider
name|authProvider
range|:
name|authorizerProviders
control|)
block|{
if|if
condition|(
name|authProvider
operator|.
name|getHivePolicyProvider
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|enableMetastorePolicyProvider
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HiveAuthzPluginException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error getting HivePolicyProvider"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error instantiating hive.security.metastore.authorization.manager"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|enableHS2PolicyProvider
operator|||
name|enableMetastorePolicyProvider
condition|)
block|{
name|enabled
operator|=
operator|new
name|BooleanWritable
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|enabled
operator|=
operator|new
name|BooleanWritable
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|enabled
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
literal|"RESTRICT_INFORMATION_SCHEMA()"
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyToNewInstance
parameter_list|(
name|Object
name|newInstance
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|super
operator|.
name|copyToNewInstance
argument_list|(
name|newInstance
argument_list|)
expr_stmt|;
comment|// Need to preserve enabled flag
name|GenericUDFRestrictInformationSchema
name|other
init|=
operator|(
name|GenericUDFRestrictInformationSchema
operator|)
name|newInstance
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|enabled
operator|!=
literal|null
condition|)
block|{
name|other
operator|.
name|enabled
operator|=
operator|new
name|BooleanWritable
argument_list|(
name|this
operator|.
name|enabled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


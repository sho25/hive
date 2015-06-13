begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|AccumuloException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|AccumuloSecurityException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|Connector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|Instance
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|ZooKeeperInstance
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|mock
operator|.
name|MockInstance
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|security
operator|.
name|tokens
operator|.
name|AuthenticationToken
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|client
operator|.
name|security
operator|.
name|tokens
operator|.
name|PasswordToken
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
name|common
operator|.
name|JavaUtils
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
name|Preconditions
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|AccumuloConnectionParameters
block|{
specifier|private
specifier|static
specifier|final
name|String
name|KERBEROS_TOKEN_CLASS
init|=
literal|"org.apache.accumulo.core.client.security.tokens.KerberosToken"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USER_NAME
init|=
literal|"accumulo.user.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USER_PASS
init|=
literal|"accumulo.user.pass"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPERS
init|=
literal|"accumulo.zookeepers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INSTANCE_NAME
init|=
literal|"accumulo.instance.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"accumulo.table.name"
decl_stmt|;
comment|// SASL/Kerberos properties
specifier|public
specifier|static
specifier|final
name|String
name|SASL_ENABLED
init|=
literal|"accumulo.sasl.enabled"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USER_KEYTAB
init|=
literal|"accumulo.user.keytab"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|USE_MOCK_INSTANCE
init|=
literal|"accumulo.mock.instance"
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|boolean
name|useMockInstance
init|=
literal|false
decl_stmt|;
specifier|public
name|AccumuloConnectionParameters
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// TableDesc#getDeserializer will ultimately instantiate the AccumuloSerDe with a null
comment|// Configuration
comment|// We have to accept this and just fail late if data is attempted to be pulled from the
comment|// Configuration
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|String
name|getAccumuloUserName
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|USER_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAccumuloPassword
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|USER_PASS
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAccumuloInstanceName
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|INSTANCE_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|getZooKeepers
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|ZOOKEEPERS
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAccumuloTableName
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|TABLE_NAME
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|useMockInstance
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|USE_MOCK_INSTANCE
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|useSasl
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|SASL_ENABLED
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|String
name|getAccumuloKeytab
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|conf
operator|.
name|get
argument_list|(
name|USER_KEYTAB
argument_list|)
return|;
block|}
specifier|public
name|Instance
name|getInstance
parameter_list|()
block|{
name|String
name|instanceName
init|=
name|getAccumuloInstanceName
argument_list|()
decl_stmt|;
comment|// Fail with a good message
if|if
condition|(
literal|null
operator|==
name|instanceName
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Accumulo instance name must be provided in hiveconf using "
operator|+
name|INSTANCE_NAME
argument_list|)
throw|;
block|}
if|if
condition|(
name|useMockInstance
argument_list|()
condition|)
block|{
return|return
operator|new
name|MockInstance
argument_list|(
name|instanceName
argument_list|)
return|;
block|}
name|String
name|zookeepers
init|=
name|getZooKeepers
argument_list|()
decl_stmt|;
comment|// Fail with a good message
if|if
condition|(
literal|null
operator|==
name|zookeepers
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ZooKeeper quorum string must be provided in hiveconf using "
operator|+
name|ZOOKEEPERS
argument_list|)
throw|;
block|}
return|return
operator|new
name|ZooKeeperInstance
argument_list|(
name|instanceName
argument_list|,
name|zookeepers
argument_list|)
return|;
block|}
specifier|public
name|Connector
name|getConnector
parameter_list|()
throws|throws
name|AccumuloException
throws|,
name|AccumuloSecurityException
block|{
name|Instance
name|inst
init|=
name|getInstance
argument_list|()
decl_stmt|;
return|return
name|getConnector
argument_list|(
name|inst
argument_list|)
return|;
block|}
specifier|public
name|Connector
name|getConnector
parameter_list|(
name|Instance
name|inst
parameter_list|)
throws|throws
name|AccumuloException
throws|,
name|AccumuloSecurityException
block|{
name|String
name|username
init|=
name|getAccumuloUserName
argument_list|()
decl_stmt|;
comment|// Fail with a good message
if|if
condition|(
literal|null
operator|==
name|username
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Accumulo user name must be provided in hiveconf using "
operator|+
name|USER_NAME
argument_list|)
throw|;
block|}
if|if
condition|(
name|useSasl
argument_list|()
condition|)
block|{
return|return
name|inst
operator|.
name|getConnector
argument_list|(
name|username
argument_list|,
name|getKerberosToken
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
comment|// Not using SASL/Kerberos -- use the password
name|String
name|password
init|=
name|getAccumuloPassword
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|password
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Accumulo password must be provided in hiveconf using "
operator|+
name|USER_PASS
argument_list|)
throw|;
block|}
return|return
name|inst
operator|.
name|getConnector
argument_list|(
name|username
argument_list|,
operator|new
name|PasswordToken
argument_list|(
name|password
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
name|AuthenticationToken
name|getKerberosToken
parameter_list|()
block|{
if|if
condition|(
operator|!
name|useSasl
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot construct KerberosToken when SASL is disabled"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|keytab
init|=
name|getAccumuloKeytab
argument_list|()
decl_stmt|,
name|username
init|=
name|getAccumuloUserName
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|keytab
condition|)
block|{
comment|// Use the keytab if one was provided
return|return
name|getKerberosToken
argument_list|(
name|username
argument_list|,
name|keytab
argument_list|)
return|;
block|}
else|else
block|{
comment|// Otherwise, expect the user is already logged in
return|return
name|getKerberosToken
argument_list|(
name|username
argument_list|)
return|;
block|}
block|}
comment|/**    * Instantiate a KerberosToken in a backwards compatible manner.    * @param username Kerberos principal    */
name|AuthenticationToken
name|getKerberosToken
parameter_list|(
name|String
name|username
parameter_list|)
block|{
comment|// Get the Class
name|Class
argument_list|<
name|?
extends|extends
name|AuthenticationToken
argument_list|>
name|krbTokenClz
init|=
name|getKerberosTokenClass
argument_list|()
decl_stmt|;
try|try
block|{
comment|// Invoke the `new KerberosToken(String)` constructor
comment|// Expects that the user is already logged-in
name|Constructor
argument_list|<
name|?
extends|extends
name|AuthenticationToken
argument_list|>
name|constructor
init|=
name|krbTokenClz
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|constructor
operator|.
name|newInstance
argument_list|(
name|username
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
decl||
name|SecurityException
decl||
name|InstantiationException
decl||
name|IllegalArgumentException
decl||
name|InvocationTargetException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed to instantiate KerberosToken."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Instantiate a KerberosToken in a backwards compatible manner.    * @param username Kerberos principal    * @param keytab Keytab on local filesystem    */
name|AuthenticationToken
name|getKerberosToken
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|keytab
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|AuthenticationToken
argument_list|>
name|krbTokenClz
init|=
name|getKerberosTokenClass
argument_list|()
decl_stmt|;
name|File
name|keytabFile
init|=
operator|new
name|File
argument_list|(
name|keytab
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|keytabFile
operator|.
name|isFile
argument_list|()
operator|||
operator|!
name|keytabFile
operator|.
name|canRead
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Keytab must be a readable file: "
operator|+
name|keytab
argument_list|)
throw|;
block|}
try|try
block|{
comment|// Invoke the `new KerberosToken(String, File, boolean)` constructor
comment|// Tries to log in as the provided user with the given keytab, overriding an already logged-in user if present
name|Constructor
argument_list|<
name|?
extends|extends
name|AuthenticationToken
argument_list|>
name|constructor
init|=
name|krbTokenClz
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|File
operator|.
name|class
argument_list|,
name|boolean
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|constructor
operator|.
name|newInstance
argument_list|(
name|username
argument_list|,
name|keytabFile
argument_list|,
literal|true
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
decl||
name|SecurityException
decl||
name|InstantiationException
decl||
name|IllegalArgumentException
decl||
name|InvocationTargetException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Failed to instantiate KerberosToken."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Attempt to instantiate the KerberosToken class    */
name|Class
argument_list|<
name|?
extends|extends
name|AuthenticationToken
argument_list|>
name|getKerberosTokenClass
parameter_list|()
block|{
try|try
block|{
comment|// Instantiate the class
name|Class
argument_list|<
name|?
argument_list|>
name|clz
init|=
name|JavaUtils
operator|.
name|loadClass
argument_list|(
name|KERBEROS_TOKEN_CLASS
argument_list|)
decl_stmt|;
comment|// Cast it to an AuthenticationToken since Connector will need that
return|return
name|clz
operator|.
name|asSubclass
argument_list|(
name|AuthenticationToken
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not load KerberosToken class.>=Accumulo 1.7.0 required"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


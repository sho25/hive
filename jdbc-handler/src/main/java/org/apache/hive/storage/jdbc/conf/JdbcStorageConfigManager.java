begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|conf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Constants
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
name|Utilities
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
name|shims
operator|.
name|ShimLoader
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
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|QueryConditionBuilder
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  * Main configuration handler class  */
end_comment

begin_class
specifier|public
class|class
name|JdbcStorageConfigManager
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JdbcStorageConfigManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONFIG_USERNAME
init|=
name|Constants
operator|.
name|JDBC_USERNAME
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONFIG_PWD
init|=
name|Constants
operator|.
name|JDBC_PASSWORD
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONFIG_PWD_KEYSTORE
init|=
name|Constants
operator|.
name|JDBC_KEYSTORE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONFIG_PWD_KEY
init|=
name|Constants
operator|.
name|JDBC_KEY
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|JdbcStorageConfig
argument_list|>
name|DEFAULT_REQUIRED_PROPERTIES
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|JdbcStorageConfig
operator|.
name|DATABASE_TYPE
argument_list|,
name|JdbcStorageConfig
operator|.
name|JDBC_URL
argument_list|,
name|JdbcStorageConfig
operator|.
name|JDBC_DRIVER_CLASS
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|JdbcStorageConfig
argument_list|>
name|METASTORE_REQUIRED_PROPERTIES
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|JdbcStorageConfig
operator|.
name|DATABASE_TYPE
argument_list|,
name|JdbcStorageConfig
operator|.
name|QUERY
argument_list|)
decl_stmt|;
specifier|private
name|JdbcStorageConfigManager
parameter_list|()
block|{   }
specifier|public
specifier|static
name|void
name|copyConfigurationToJob
parameter_list|(
name|Properties
name|props
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProps
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|checkRequiredPropertiesAreDefined
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|resolveMetadata
argument_list|(
name|props
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|props
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|CONFIG_PWD
argument_list|)
operator|&&
operator|!
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|CONFIG_PWD_KEYSTORE
argument_list|)
operator|&&
operator|!
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|CONFIG_PWD_KEY
argument_list|)
condition|)
block|{
name|jobProps
operator|.
name|put
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|copySecretsToJob
parameter_list|(
name|Properties
name|props
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobSecrets
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|checkRequiredPropertiesAreDefined
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|resolveMetadata
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|String
name|passwd
init|=
name|props
operator|.
name|getProperty
argument_list|(
name|CONFIG_PWD
argument_list|)
decl_stmt|;
if|if
condition|(
name|passwd
operator|==
literal|null
condition|)
block|{
name|String
name|keystore
init|=
name|props
operator|.
name|getProperty
argument_list|(
name|CONFIG_PWD_KEYSTORE
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|props
operator|.
name|getProperty
argument_list|(
name|CONFIG_PWD_KEY
argument_list|)
decl_stmt|;
name|passwd
operator|=
name|Utilities
operator|.
name|getPasswdFromKeystore
argument_list|(
name|keystore
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|passwd
operator|!=
literal|null
condition|)
block|{
name|jobSecrets
operator|.
name|put
argument_list|(
name|CONFIG_PWD
argument_list|,
name|passwd
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|Configuration
name|convertPropertiesToConfiguration
parameter_list|(
name|Properties
name|props
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|checkRequiredPropertiesAreDefined
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|resolveMetadata
argument_list|(
name|props
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|props
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|conf
return|;
block|}
specifier|private
specifier|static
name|void
name|checkRequiredPropertiesAreDefined
parameter_list|(
name|Properties
name|props
parameter_list|)
block|{
name|DatabaseType
name|dbType
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|dbTypeName
init|=
name|props
operator|.
name|getProperty
argument_list|(
name|JdbcStorageConfig
operator|.
name|DATABASE_TYPE
operator|.
name|getPropertyName
argument_list|()
argument_list|)
decl_stmt|;
name|dbType
operator|=
name|DatabaseType
operator|.
name|valueOf
argument_list|(
name|dbTypeName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown database type."
argument_list|,
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|JdbcStorageConfig
name|configKey
range|:
operator|(
name|DatabaseType
operator|.
name|METASTORE
operator|.
name|equals
argument_list|(
name|dbType
argument_list|)
condition|?
name|METASTORE_REQUIRED_PROPERTIES
else|:
name|DEFAULT_REQUIRED_PROPERTIES
operator|)
control|)
block|{
name|String
name|propertyKey
init|=
name|configKey
operator|.
name|getPropertyName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|props
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|props
operator|.
name|containsKey
argument_list|(
name|propertyKey
argument_list|)
operator|)
operator|||
operator|(
name|isEmptyString
argument_list|(
name|props
operator|.
name|getProperty
argument_list|(
name|propertyKey
argument_list|)
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Property "
operator|+
name|propertyKey
operator|+
literal|" is required."
argument_list|)
throw|;
block|}
block|}
name|CustomConfigManager
name|configManager
init|=
name|CustomConfigManagerFactory
operator|.
name|getCustomConfigManagerFor
argument_list|(
name|dbType
argument_list|)
decl_stmt|;
name|configManager
operator|.
name|checkRequiredProperties
argument_list|(
name|props
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getConfigValue
parameter_list|(
name|JdbcStorageConfig
name|key
parameter_list|,
name|Configuration
name|config
parameter_list|)
block|{
return|return
name|config
operator|.
name|get
argument_list|(
name|key
operator|.
name|getPropertyName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getOrigQueryToExecute
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|String
name|query
decl_stmt|;
name|String
name|tableName
init|=
name|config
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_TABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
block|{
comment|// We generate query as select *
name|query
operator|=
literal|"select * from "
operator|+
name|tableName
expr_stmt|;
name|String
name|hiveFilterCondition
init|=
name|QueryConditionBuilder
operator|.
name|getInstance
argument_list|()
operator|.
name|buildCondition
argument_list|(
name|config
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|hiveFilterCondition
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|hiveFilterCondition
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|query
operator|=
name|query
operator|+
literal|" WHERE "
operator|+
name|hiveFilterCondition
expr_stmt|;
block|}
block|}
else|else
block|{
name|query
operator|=
name|config
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_QUERY
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
specifier|public
specifier|static
name|String
name|getQueryToExecute
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|String
name|query
init|=
name|config
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_QUERY
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
comment|// Already defined query, we return it
return|return
name|query
return|;
block|}
comment|// We generate query as select *
name|String
name|tableName
init|=
name|config
operator|.
name|get
argument_list|(
name|JdbcStorageConfig
operator|.
name|TABLE
operator|.
name|getPropertyName
argument_list|()
argument_list|)
decl_stmt|;
name|query
operator|=
literal|"select * from "
operator|+
name|tableName
expr_stmt|;
name|String
name|hiveFilterCondition
init|=
name|QueryConditionBuilder
operator|.
name|getInstance
argument_list|()
operator|.
name|buildCondition
argument_list|(
name|config
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|hiveFilterCondition
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|hiveFilterCondition
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|query
operator|=
name|query
operator|+
literal|" WHERE "
operator|+
name|hiveFilterCondition
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isEmptyString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
operator|(
operator|(
name|value
operator|==
literal|null
operator|)
operator|||
operator|(
name|value
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
operator|)
return|;
block|}
specifier|private
specifier|static
name|void
name|resolveMetadata
parameter_list|(
name|Properties
name|props
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|DatabaseType
name|dbType
init|=
name|DatabaseType
operator|.
name|valueOf
argument_list|(
name|props
operator|.
name|getProperty
argument_list|(
name|JdbcStorageConfig
operator|.
name|DATABASE_TYPE
operator|.
name|getPropertyName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Resolving db type: {}"
argument_list|,
name|dbType
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|dbType
operator|==
name|DatabaseType
operator|.
name|METASTORE
condition|)
block|{
name|HiveConf
name|hconf
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|JdbcStorageConfig
operator|.
name|JDBC_URL
operator|.
name|getPropertyName
argument_list|()
argument_list|,
name|getMetastoreConnectionURL
argument_list|(
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|JdbcStorageConfig
operator|.
name|JDBC_DRIVER_CLASS
operator|.
name|getPropertyName
argument_list|()
argument_list|,
name|getMetastoreDriver
argument_list|(
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|user
init|=
name|getMetastoreJdbcUser
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
if|if
condition|(
name|user
operator|!=
literal|null
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|CONFIG_USERNAME
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
name|String
name|pwd
init|=
name|getMetastoreJdbcPasswd
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
if|if
condition|(
name|pwd
operator|!=
literal|null
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|CONFIG_PWD
argument_list|,
name|pwd
argument_list|)
expr_stmt|;
block|}
name|props
operator|.
name|setProperty
argument_list|(
name|JdbcStorageConfig
operator|.
name|DATABASE_TYPE
operator|.
name|getPropertyName
argument_list|()
argument_list|,
name|getMetastoreDatabaseType
argument_list|(
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|String
name|getMetastoreDatabaseType
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREDBTYPE
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getMetastoreConnectionURL
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getMetastoreDriver
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getMetastoreJdbcUser
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_CONNECTION_USER_NAME
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getMetastoreJdbcPasswd
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getPassword
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|)
return|;
block|}
block|}
end_class

end_unit


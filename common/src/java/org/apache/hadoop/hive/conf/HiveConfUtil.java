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
name|conf
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
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
name|common
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
operator|.
name|ConfVars
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
name|mapred
operator|.
name|JobConf
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
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
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
name|io
operator|.
name|File
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|Iterator
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
name|StringTokenizer
import|;
end_import

begin_comment
comment|/**  * Hive Configuration utils  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|HiveConfUtil
block|{
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|HiveConfUtil
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
comment|/**    * Check if metastore is being used in embedded mode.    * This utility function exists so that the logic for determining the mode is same    * in HiveConf and HiveMetaStoreClient    * @param msUri - metastore server uri    * @return    */
specifier|public
specifier|static
name|boolean
name|isEmbeddedMetaStore
parameter_list|(
name|String
name|msUri
parameter_list|)
block|{
return|return
operator|(
name|msUri
operator|==
literal|null
operator|)
condition|?
literal|true
else|:
name|msUri
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**    * Dumps all HiveConf for debugging.  Convenient to dump state at process start up and log it    * so that in later analysis the values of all variables is known    */
specifier|public
specifier|static
name|StringBuilder
name|dumpConfig
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"START========\"HiveConf()\"========\n"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"hiveDefaultUrl="
argument_list|)
operator|.
name|append
argument_list|(
name|conf
operator|.
name|getHiveDefaultLocation
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"hiveSiteURL="
argument_list|)
operator|.
name|append
argument_list|(
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"hiveServer2SiteUrl="
argument_list|)
operator|.
name|append
argument_list|(
name|HiveConf
operator|.
name|getHiveServer2SiteLocation
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"hivemetastoreSiteUrl="
argument_list|)
operator|.
name|append
argument_list|(
name|HiveConf
operator|.
name|getMetastoreSiteLocation
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
name|dumpConfig
argument_list|(
name|conf
argument_list|,
name|sb
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|append
argument_list|(
literal|"END========\"new HiveConf()\"========\n"
argument_list|)
return|;
block|}
comment|/**    * Getting the set of the hidden configurations    * @param configuration The original configuration    * @return The list of the configuration values to hide    */
specifier|public
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|getHiddenSet
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hiddenSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|hiddenListStr
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CONF_HIDDEN_LIST
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiddenListStr
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|entry
range|:
name|hiddenListStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|hiddenSet
operator|.
name|add
argument_list|(
name|entry
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|hiddenSet
return|;
block|}
comment|/**    * Strips hidden config entries from configuration    * @param conf The configuration to strip from    * @param hiddenSet The values to strip    */
specifier|public
specifier|static
name|void
name|stripConfigurations
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|hiddenSet
parameter_list|)
block|{
comment|// Find all configurations where the key contains any string from hiddenSet
name|Iterable
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|matching
init|=
name|Iterables
operator|.
name|filter
argument_list|(
name|conf
argument_list|,
name|confEntry
lambda|->
block|{
for|for
control|(
name|String
name|name
range|:
name|hiddenSet
control|)
block|{
if|if
condition|(
name|confEntry
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
argument_list|)
decl_stmt|;
comment|// Remove the value of every key found matching
name|matching
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
name|conf
operator|.
name|set
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|StringUtils
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Searches the given configuration object and replaces all the configuration values for keys    * defined hive.conf.hidden.list by empty String    *    * @param conf - Configuration object which needs to be modified to remove sensitive keys    */
specifier|public
specifier|static
name|void
name|stripConfigurations
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hiddenSet
init|=
name|getHiddenSet
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|stripConfigurations
argument_list|(
name|conf
argument_list|,
name|hiddenSet
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|dumpConfig
parameter_list|(
name|Configuration
name|originalConf
parameter_list|,
name|StringBuilder
name|sb
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hiddenSet
init|=
name|getHiddenSet
argument_list|(
name|originalConf
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Values omitted for security reason if present: "
argument_list|)
operator|.
name|append
argument_list|(
name|hiddenSet
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|originalConf
argument_list|)
decl_stmt|;
name|stripConfigurations
argument_list|(
name|conf
argument_list|,
name|hiddenSet
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|configIter
init|=
name|conf
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|configVals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|configIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|configVals
operator|.
name|add
argument_list|(
name|configIter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|configVals
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ent
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ent2
parameter_list|)
block|{
return|return
name|ent
operator|.
name|getKey
argument_list|()
operator|.
name|compareTo
argument_list|(
name|ent2
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|configVals
control|)
block|{
comment|//use get() to make sure variable substitution works
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|contains
argument_list|(
literal|"path"
argument_list|)
condition|)
block|{
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|File
operator|.
name|pathSeparator
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"=\n"
argument_list|)
expr_stmt|;
while|while
condition|(
name|st
operator|.
name|hasMoreTokens
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    "
argument_list|)
operator|.
name|append
argument_list|(
name|st
operator|.
name|nextToken
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
name|File
operator|.
name|pathSeparator
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Updates the job configuration with the job specific credential provider information available    * in the HiveConf.It uses the environment variables HADOOP_CREDSTORE_PASSWORD or    * HIVE_JOB_CREDSTORE_PASSWORD to get the custom password for all the keystores configured in the    * provider path. This usage of environment variables is similar in lines with Hadoop credential    * provider mechanism for getting the keystore passwords. The other way of communicating the    * password is through a file which stores the password in clear-text which needs to be readable    * by all the consumers and therefore is not supported.    *    *<ul>    *<li>If HIVE_SERVER2_JOB_CREDENTIAL_PROVIDER_PATH is set in the hive configuration this method    * overrides the MR job configuration property hadoop.security.credential.provider.path with its    * value. If not set then it does not change the value of hadoop.security.credential.provider.path    *<li>In order to choose the password for the credential provider we check :    *    *   (1) if job credential provider path HIVE_SERVER2_JOB_CREDENTIAL_PROVIDER_PATH is set we check if    *       HIVE_SERVER2_JOB_CREDSTORE_PASSWORD_ENVVAR is set. If it is set we use it.    *   (2) If password is not set using (1) above we use HADOOP_CREDSTORE_PASSWORD if it is set.    *   (3) If none of those are set, we do not set any password in the MR task environment. In this    *       case the hadoop credential provider should use the default password of "none" automatically    *</ul>    * @param jobConf - job specific configuration    */
specifier|public
specifier|static
name|void
name|updateJobCredentialProviders
parameter_list|(
name|Configuration
name|jobConf
parameter_list|)
block|{
if|if
condition|(
name|jobConf
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
name|jobKeyStoreLocation
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_JOB_CREDENTIAL_PROVIDER_PATH
operator|.
name|varname
argument_list|)
decl_stmt|;
name|String
name|oldKeyStoreLocation
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|HADOOP_CREDENTIAL_PROVIDER_PATH_CONFIG
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|jobKeyStoreLocation
argument_list|)
condition|)
block|{
name|jobConf
operator|.
name|set
argument_list|(
name|Constants
operator|.
name|HADOOP_CREDENTIAL_PROVIDER_PATH_CONFIG
argument_list|,
name|jobKeyStoreLocation
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting job conf credstore location to "
operator|+
name|jobKeyStoreLocation
operator|+
literal|" previous location was "
operator|+
name|oldKeyStoreLocation
argument_list|)
expr_stmt|;
block|}
name|String
name|credStorepassword
init|=
name|getJobCredentialProviderPassword
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|credStorepassword
operator|!=
literal|null
condition|)
block|{
comment|// if the execution engine is MR set the map/reduce env with the credential store password
name|String
name|execEngine
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"mr"
operator|.
name|equalsIgnoreCase
argument_list|(
name|execEngine
argument_list|)
condition|)
block|{
name|addKeyValuePair
argument_list|(
name|jobConf
argument_list|,
name|JobConf
operator|.
name|MAPRED_MAP_TASK_ENV
argument_list|,
name|Constants
operator|.
name|HADOOP_CREDENTIAL_PASSWORD_ENVVAR
argument_list|,
name|credStorepassword
argument_list|)
expr_stmt|;
name|addKeyValuePair
argument_list|(
name|jobConf
argument_list|,
name|JobConf
operator|.
name|MAPRED_REDUCE_TASK_ENV
argument_list|,
name|Constants
operator|.
name|HADOOP_CREDENTIAL_PASSWORD_ENVVAR
argument_list|,
name|credStorepassword
argument_list|)
expr_stmt|;
name|addKeyValuePair
argument_list|(
name|jobConf
argument_list|,
literal|"yarn.app.mapreduce.am.admin.user.env"
argument_list|,
name|Constants
operator|.
name|HADOOP_CREDENTIAL_PASSWORD_ENVVAR
argument_list|,
name|credStorepassword
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * If HIVE_SERVER2_JOB_CREDSTORE_LOCATION is set check HIVE_SERVER2_JOB_CREDSTORE_PASSWORD_ENVVAR before    * checking HADOOP_CREDENTIAL_PASSWORD_ENVVAR    */
specifier|public
specifier|static
name|String
name|getJobCredentialProviderPassword
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|jobKeyStoreLocation
init|=
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_JOB_CREDENTIAL_PROVIDER_PATH
operator|.
name|varname
argument_list|)
decl_stmt|;
name|String
name|password
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|jobKeyStoreLocation
argument_list|)
condition|)
block|{
name|password
operator|=
name|System
operator|.
name|getenv
argument_list|(
name|Constants
operator|.
name|HIVE_SERVER2_JOB_CREDSTORE_PASSWORD_ENVVAR
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|password
argument_list|)
condition|)
block|{
return|return
name|password
return|;
block|}
block|}
name|password
operator|=
name|System
operator|.
name|getenv
argument_list|(
name|Constants
operator|.
name|HADOOP_CREDENTIAL_PASSWORD_ENVVAR
argument_list|)
expr_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|password
argument_list|)
condition|)
block|{
return|return
name|password
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
name|void
name|addKeyValuePair
parameter_list|(
name|Configuration
name|jobConf
parameter_list|,
name|String
name|property
parameter_list|,
name|String
name|keyName
parameter_list|,
name|String
name|newKeyValue
parameter_list|)
block|{
name|String
name|existingValue
init|=
name|jobConf
operator|.
name|get
argument_list|(
name|property
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingValue
operator|==
literal|null
condition|)
block|{
name|jobConf
operator|.
name|set
argument_list|(
name|property
argument_list|,
operator|(
name|keyName
operator|+
literal|"="
operator|+
name|newKeyValue
operator|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
name|propertyValue
init|=
name|HiveStringUtils
operator|.
name|insertValue
argument_list|(
name|keyName
argument_list|,
name|newKeyValue
argument_list|,
name|existingValue
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|property
argument_list|,
name|propertyValue
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


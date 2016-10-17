begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
operator|.
name|hs2connection
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
name|List
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

begin_class
specifier|public
class|class
name|HS2ConnectionFileUtils
block|{
specifier|public
specifier|static
name|String
name|getUrl
parameter_list|(
name|Properties
name|props
parameter_list|)
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
if|if
condition|(
name|props
operator|==
literal|null
operator|||
name|props
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// use remove instead of get so that it is not parsed again
comment|// in the for loop below
name|String
name|urlPrefix
init|=
operator|(
name|String
operator|)
name|props
operator|.
name|remove
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|URL_PREFIX_PROPERTY_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|urlPrefix
operator|==
literal|null
operator|||
name|urlPrefix
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BeelineHS2ConnectionFileParseException
argument_list|(
literal|"url_prefix parameter cannot be empty"
argument_list|)
throw|;
block|}
name|String
name|hosts
init|=
operator|(
name|String
operator|)
name|props
operator|.
name|remove
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|HOST_PROPERTY_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|hosts
operator|==
literal|null
operator|||
name|hosts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BeelineHS2ConnectionFileParseException
argument_list|(
literal|"hosts parameter cannot be empty"
argument_list|)
throw|;
block|}
name|String
name|defaultDB
init|=
operator|(
name|String
operator|)
name|props
operator|.
name|remove
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|DEFAULT_DB_PROPERTY_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|defaultDB
operator|==
literal|null
condition|)
block|{
name|defaultDB
operator|=
literal|"default"
expr_stmt|;
block|}
comment|// collect the hiveConfList and HiveVarList separately so that they can be
comment|// appended once all the session list are added to the url
name|String
name|hiveConfProperties
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|props
operator|.
name|containsKey
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|HIVE_CONF_PROPERTY_KEY
argument_list|)
condition|)
block|{
name|hiveConfProperties
operator|=
name|extractHiveVariables
argument_list|(
operator|(
name|String
operator|)
name|props
operator|.
name|remove
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|HIVE_CONF_PROPERTY_KEY
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|String
name|hiveVarProperties
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|props
operator|.
name|containsKey
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|HIVE_VAR_PROPERTY_KEY
argument_list|)
condition|)
block|{
name|hiveVarProperties
operator|=
name|extractHiveVariables
argument_list|(
operator|(
name|String
operator|)
name|props
operator|.
name|remove
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|HIVE_VAR_PROPERTY_KEY
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|urlSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
name|urlPrefix
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
name|hosts
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
name|File
operator|.
name|separator
argument_list|)
expr_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
name|defaultDB
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|props
operator|.
name|stringPropertyNames
argument_list|()
argument_list|)
decl_stmt|;
comment|// sorting the keys from the properties helps to create
comment|// a deterministic url which is tested for various configuration in
comment|// TestHS2ConnectionConfigFileManager
name|Collections
operator|.
name|sort
argument_list|(
name|keys
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|propertyName
range|:
name|keys
control|)
block|{
name|urlSb
operator|.
name|append
argument_list|(
literal|";"
argument_list|)
expr_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
name|propertyName
argument_list|)
expr_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
name|urlSb
operator|.
name|append
argument_list|(
name|props
operator|.
name|getProperty
argument_list|(
name|propertyName
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hiveConfProperties
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|urlSb
operator|.
name|append
argument_list|(
name|hiveConfProperties
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hiveVarProperties
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|urlSb
operator|.
name|append
argument_list|(
name|hiveVarProperties
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|urlSb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|extractHiveVariables
parameter_list|(
name|String
name|propertyValue
parameter_list|,
name|boolean
name|isHiveConf
parameter_list|)
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
name|StringBuilder
name|hivePropertiesList
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|String
name|delimiter
decl_stmt|;
if|if
condition|(
name|isHiveConf
condition|)
block|{
name|delimiter
operator|=
literal|"?"
expr_stmt|;
block|}
else|else
block|{
name|delimiter
operator|=
literal|"#"
expr_stmt|;
block|}
name|hivePropertiesList
operator|.
name|append
argument_list|(
name|delimiter
argument_list|)
expr_stmt|;
name|addPropertyValues
argument_list|(
name|propertyValue
argument_list|,
name|hivePropertiesList
argument_list|)
expr_stmt|;
return|return
name|hivePropertiesList
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|addPropertyValues
parameter_list|(
name|String
name|value
parameter_list|,
name|StringBuilder
name|hivePropertiesList
parameter_list|)
throws|throws
name|BeelineHS2ConnectionFileParseException
block|{
comment|// There could be multiple keyValuePairs separated by comma
name|String
index|[]
name|values
init|=
name|value
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|String
name|keyValuePair
range|:
name|values
control|)
block|{
name|String
index|[]
name|keyValue
init|=
name|keyValuePair
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyValue
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|BeelineHS2ConnectionFileParseException
argument_list|(
literal|"Unable to parse "
operator|+
name|keyValuePair
operator|+
literal|" in hs2 connection config file"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|hivePropertiesList
operator|.
name|append
argument_list|(
literal|";"
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|hivePropertiesList
operator|.
name|append
argument_list|(
name|keyValue
index|[
literal|0
index|]
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|hivePropertiesList
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
name|hivePropertiesList
operator|.
name|append
argument_list|(
name|keyValue
index|[
literal|1
index|]
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


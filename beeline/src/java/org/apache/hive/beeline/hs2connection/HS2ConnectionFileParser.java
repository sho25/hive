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
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  * HS2ConnectionFileParser provides a interface to be used by Beeline to parse a configuration (file  * or otherwise) to return a Java Properties object which contain key value pairs to be used to construct  * connection URL automatically for the Beeline connection  */
end_comment

begin_interface
specifier|public
interface|interface
name|HS2ConnectionFileParser
block|{
comment|/**    * prefix string used for the keys    */
specifier|public
specifier|static
specifier|final
name|String
name|BEELINE_CONNECTION_PROPERTY_PREFIX
init|=
literal|"beeline.hs2.connection."
decl_stmt|;
comment|/**    * Property key used to provide the URL prefix for the connection URL    */
specifier|public
specifier|static
specifier|final
name|String
name|URL_PREFIX_PROPERTY_KEY
init|=
literal|"url_prefix"
decl_stmt|;
comment|/**    * Property key used to provide the default database in the connection URL    */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_DB_PROPERTY_KEY
init|=
literal|"defaultDB"
decl_stmt|;
comment|/**    * Property key used to provide the hosts in the connection URL    */
specifier|public
specifier|static
specifier|final
name|String
name|HOST_PROPERTY_KEY
init|=
literal|"hosts"
decl_stmt|;
comment|/**    * Property key used to provide the hive configuration key value pairs in the connection URL    */
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_CONF_PROPERTY_KEY
init|=
literal|"hiveconf"
decl_stmt|;
comment|/**    * Property key used to provide the hive variables in the connection URL    */
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_VAR_PROPERTY_KEY
init|=
literal|"hivevar"
decl_stmt|;
comment|/**    * Returns a Java properties object which contain the key value pairs which can be used in the    * Beeline connection URL<p>    * The properties returned must include url_prefix and hosts<p>    * Following are some examples of the URLs and returned properties object<p>    *<ul>    *<li> jdbc:hive2://hs2-instance1.example.com:10000/default;user=hive;password=mypassword should    * return [ "url_prefix"="jdbc:hive2://", "hosts"="hs2-instance1.example.com:10000",    * "defaultDB"=default, "user"="hive", "password"="mypassword" ]    *<li>jdbc:hive2://zk-instance1:10001,zk-instance2:10002,zk-instance3:10003/default;    * serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2 should return [    * "url_prefix"="jdbc:hive2://",    * "hosts"="zk-instance1:10001,zk-instance2:10002,zk-instance3:10003", "defaultDB"="default",    * "serviceDiscoveryMode"="zooKeeper", "zooKeeperNamespace"="hiveserver2" ]    *    *<li>If hive_conf_list and hive_var_list is present in the url it should return a comma separated    * key=value pairs for each of them<p>    * For example :<p>    * jdbc:hive2://hs2-instance1.example.com:10000/default;user=hive?hive.cli.print.currentdb=true;    * hive.cli.print.header=true#hivevar:mytbl=customers;hivevar:mycol=id it should return [    * "url_prefix"="jdbc:hive2://", "hosts"="hs2-instance1.example.com:10000", "defaultDB"="default",    * "user"="hive", "hiveconf"="hive.cli.print.currentdb=true, hive.cli.print.header=true",    * "hivevar"="hivevar:mytb1=customers, hivevar:mycol=id" ]    *</ul>    *    * @return Properties object which contain connection URL properties for Beeline connection. Returns an empty properties    * object if the connection configuration is not found    * @throws BeelineHS2ConnectionFileParseException if there is invalid key with appropriate message    */
name|Properties
name|getConnectionProperties
parameter_list|()
throws|throws
name|BeelineHS2ConnectionFileParseException
function_decl|;
comment|/**    *    * @return returns true if the configuration exists else returns false    */
name|boolean
name|configExists
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


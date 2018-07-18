begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
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
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|HashSet
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
name|hive
operator|.
name|jdbc
operator|.
name|miniHS2
operator|.
name|MiniHS2
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_class
specifier|public
class|class
name|TestRestrictedList
block|{
specifier|private
specifier|static
name|MiniHS2
name|miniHS2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|URL
name|oldHiveSiteURL
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|URL
name|oldHiveMetastoreSiteURL
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|expectedRestrictedMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startServices
parameter_list|()
throws|throws
name|Exception
block|{
name|Class
operator|.
name|forName
argument_list|(
name|MiniHS2
operator|.
name|getJdbcDriverName
argument_list|()
argument_list|)
expr_stmt|;
name|oldHiveSiteURL
operator|=
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
expr_stmt|;
name|oldHiveMetastoreSiteURL
operator|=
name|HiveConf
operator|.
name|getMetastoreSiteLocation
argument_list|()
expr_stmt|;
name|String
name|confDir
init|=
literal|"../../data/conf/rlist/"
decl_stmt|;
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/hive-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Setting hive-site: "
operator|+
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setHivemetastoreSiteUrl
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file://"
operator|+
operator|new
name|File
argument_list|(
name|confDir
argument_list|)
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"/hivemetastore-site.xml"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Setting hive-site: "
operator|+
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MIN_WORKER_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setIntVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_MAX_WORKER_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|miniHS2
operator|=
operator|new
name|MiniHS2
operator|.
name|Builder
argument_list|()
operator|.
name|withMiniMR
argument_list|()
operator|.
name|withRemoteMetastore
argument_list|()
operator|.
name|withConf
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
comment|// Add the parameter here if it cannot change at runtime
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.conf.restricted.list"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.security.authenticator.manager"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.security.authorization.manager"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.security.metastore.authorization.manager"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.security.metastore.authenticator.manager"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.users.in.admin.role"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.xsrf.filter.enabled"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.security.authorization.enabled"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.distcp.privileged.doAs"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.baseDN"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.url"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.Domain"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.groupDNPattern"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.groupFilter"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.userDNPattern"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.userFilter"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.groupMembershipKey"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.userMembershipKey"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.groupClassKey"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.server2.authentication.ldap.customLDAPQuery"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.channel.log.level"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.secret.bits"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.rpc.server.address"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.rpc.server.port"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.rpc.sasl.mechanisms"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"bonecp.test"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.druid.broker.address.default"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.druid.coordinator.address.default"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hikaricp.test"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hadoop.bin.path"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"yarn.bin.path"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.connect.timeout"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.server.connect.timeout"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.rpc.max.size"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.rpc.threads"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"_hive.local.session.path"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"_hive.tmp_table_space"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"_hive.hdfs.session.path"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.spark.client.rpc.server.address"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"spark.home"
argument_list|)
expr_stmt|;
name|addToExpectedRestrictedMap
argument_list|(
literal|"hive.privilege.synchronizer.interval"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopServices
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|miniHS2
operator|!=
literal|null
operator|&&
name|miniHS2
operator|.
name|isStarted
argument_list|()
condition|)
block|{
name|miniHS2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|HiveConf
operator|.
name|setHivemetastoreSiteUrl
argument_list|(
name|oldHiveMetastoreSiteURL
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setHiveSiteLocation
argument_list|(
name|oldHiveSiteURL
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRestrictedList
parameter_list|()
throws|throws
name|Exception
block|{
name|assertTrue
argument_list|(
literal|"Test setup failed. MiniHS2 is not initialized"
argument_list|,
name|miniHS2
operator|!=
literal|null
operator|&&
name|miniHS2
operator|.
name|isStarted
argument_list|()
argument_list|)
expr_stmt|;
name|checkRestrictedListMatch
argument_list|()
expr_stmt|;
try|try
init|(
name|Connection
name|hs2Conn
init|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|miniHS2
operator|.
name|getJdbcURL
argument_list|()
argument_list|,
literal|"hive"
argument_list|,
literal|"hive"
argument_list|)
init|;
name|Statement
name|stmt
operator|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
init|;
init|)
block|{
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
name|expectedRestrictedMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|parameter
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
try|try
block|{
name|stmt
operator|.
name|execute
argument_list|(
literal|"set "
operator|+
name|parameter
operator|+
literal|"="
operator|+
name|value
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Exception not thrown for parameter: "
operator|+
name|parameter
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Unexpected exception: "
operator|+
name|e1
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e1
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Error while processing statement: Cannot modify"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e2
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Unexpected Exception: "
operator|+
name|e2
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// This test will make sure that every entry in hive.conf.restricted.list, has a test here
specifier|private
name|void
name|checkRestrictedListMatch
parameter_list|()
block|{
name|HiveConf
operator|.
name|ConfVars
name|restrictedConfVar
init|=
name|HiveConf
operator|.
name|getConfVars
argument_list|(
literal|"hive.conf.restricted.list"
argument_list|)
decl_stmt|;
name|String
name|definedRestrictedListString
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hiveConf
argument_list|,
name|restrictedConfVar
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|definedRestrictedSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|definedRestrictedSet
operator|.
name|clear
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|definedRestrictedListString
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|// populate definedRestrictedSet with parameters defined in hive.conf.restricted.list
for|for
control|(
name|String
name|entry
range|:
name|definedRestrictedListString
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|definedRestrictedSet
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
comment|// remove all parameters that are tested.  if the parameter is tested it is part of
comment|// expectedRestrictedMap
name|definedRestrictedSet
operator|.
name|removeAll
argument_list|(
name|expectedRestrictedMap
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
comment|// the remaining parameters in definedRestrictedSet are starting parameter name
for|for
control|(
name|String
name|definedRestrictedParameter
range|:
name|definedRestrictedSet
control|)
block|{
name|boolean
name|definedRestrictedParameterTested
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|expectedRestrictedParameter
range|:
name|expectedRestrictedMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|expectedRestrictedParameter
operator|.
name|startsWith
argument_list|(
name|definedRestrictedParameter
argument_list|)
condition|)
block|{
name|definedRestrictedParameterTested
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|definedRestrictedParameter
operator|+
literal|" not tested."
argument_list|,
name|definedRestrictedParameterTested
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|addToExpectedRestrictedMap
parameter_list|(
name|String
name|parameter
parameter_list|)
block|{
name|HiveConf
operator|.
name|ConfVars
name|confVars
init|=
name|HiveConf
operator|.
name|getConfVars
argument_list|(
name|parameter
argument_list|)
decl_stmt|;
name|String
name|value
init|=
literal|"foo"
decl_stmt|;
if|if
condition|(
name|confVars
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|confVars
operator|.
name|isType
argument_list|(
literal|"foo"
argument_list|)
operator|&&
name|confVars
operator|.
name|validate
argument_list|(
literal|"foo"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|value
operator|=
literal|"foo"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|confVars
operator|.
name|isType
argument_list|(
literal|"1s"
argument_list|)
operator|&&
name|confVars
operator|.
name|validate
argument_list|(
literal|"1s"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|value
operator|=
literal|"1s"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|confVars
operator|.
name|isType
argument_list|(
literal|"1"
argument_list|)
operator|&&
name|confVars
operator|.
name|validate
argument_list|(
literal|"1"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|value
operator|=
literal|"1"
expr_stmt|;
block|}
block|}
name|expectedRestrictedMap
operator|.
name|put
argument_list|(
name|parameter
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


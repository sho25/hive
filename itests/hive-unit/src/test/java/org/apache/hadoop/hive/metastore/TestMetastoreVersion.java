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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
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
name|Field
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|cli
operator|.
name|CliSessionState
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
name|MetaException
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
name|ObjectStore
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
name|Driver
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
name|processors
operator|.
name|CommandProcessorResponse
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

begin_class
specifier|public
class|class
name|TestMetastoreVersion
extends|extends
name|TestCase
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
name|TestMetastoreVersion
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Driver
name|driver
decl_stmt|;
specifier|private
name|String
name|metaStoreRoot
decl_stmt|;
specifier|private
name|String
name|testMetastoreDB
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|Field
name|defDb
init|=
name|HiveMetaStore
operator|.
name|HMSHandler
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"currentUrl"
argument_list|)
decl_stmt|;
name|defDb
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|defDb
operator|.
name|set
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// reset defaults
name|ObjectStore
operator|.
name|setSchemaVerified
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTO_CREATE_ALL
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.support.concurrency"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.event.listeners"
argument_list|,
name|DummyListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"hive.metastore.pre.event.listeners"
argument_list|,
name|DummyPreListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|testMetastoreDB
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test_metastore-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
operator|.
name|varname
argument_list|,
literal|"jdbc:derby:"
operator|+
name|testMetastoreDB
operator|+
literal|";create=true"
argument_list|)
expr_stmt|;
name|metaStoreRoot
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|metaStoreDir
init|=
operator|new
name|File
argument_list|(
name|testMetastoreDB
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaStoreDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtils
operator|.
name|forceDeleteOnExit
argument_list|(
name|metaStoreDir
argument_list|)
expr_stmt|;
block|}
block|}
comment|/***    * Test config defaults    */
specifier|public
name|void
name|testDefaults
parameter_list|()
block|{
name|System
operator|.
name|clearProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTO_CREATE_ALL
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/***    * Test schema verification property    * @throws Exception    */
specifier|public
name|void
name|testVersionRestriction
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_AUTO_CREATE_ALL
argument_list|)
argument_list|)
expr_stmt|;
comment|// session creation should fail since the schema didn't get created
try|try
block|{
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|getMSC
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"An exception is expected since schema is not created."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|re
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Exception in testVersionRestriction: "
operator|+
name|re
argument_list|,
name|re
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
name|HiveStringUtils
operator|.
name|stringifyException
argument_list|(
name|re
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected 'Version information not found in metastore' in: "
operator|+
name|msg
argument_list|,
name|msg
operator|.
name|contains
argument_list|(
literal|"Version information not found in metastore"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/***    * Test that with no verification, and record verification enabled, hive populates the schema    * and version correctly    * @throws Exception    */
specifier|public
name|void
name|testMetastoreVersion
parameter_list|()
throws|throws
name|Exception
block|{
comment|// let the schema and version be auto created
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION_RECORD_VERSION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
expr_stmt|;
comment|// correct version stored by Metastore during startup
name|assertEquals
argument_list|(
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|,
name|getVersion
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|setVersion
argument_list|(
name|hiveConf
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|getVersion
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/***    * Test that with verification enabled, hive works when the correct schema is already populated    * @throws Exception    */
specifier|public
name|void
name|testVersionMatching
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
expr_stmt|;
name|ObjectStore
operator|.
name|setSchemaVerified
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|setVersion
argument_list|(
name|hiveConf
argument_list|,
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|proc
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|proc
operator|.
name|getResponseCode
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Store garbage version in metastore and verify that hive fails when verification is on    * @throws Exception    */
specifier|public
name|void
name|testVersionMisMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
expr_stmt|;
name|ObjectStore
operator|.
name|setSchemaVerified
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|setVersion
argument_list|(
name|hiveConf
argument_list|,
literal|"fooVersion"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|proc
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|proc
operator|.
name|getResponseCode
argument_list|()
operator|!=
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Store higher version in metastore and verify that hive works with the compatible    * version    * @throws Exception    */
specifier|public
name|void
name|testVersionCompatibility
parameter_list|()
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_SCHEMA_VERIFICATION
operator|.
name|toString
argument_list|()
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|setVersion
argument_list|(
name|hiveConf
argument_list|,
literal|"3.9000.0"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|proc
init|=
name|driver
operator|.
name|run
argument_list|(
literal|"show tables"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|proc
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//  write the given version to metastore
specifier|private
name|String
name|getVersion
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|MetaStoreSchemaInfo
name|schemInfo
init|=
operator|new
name|MetaStoreSchemaInfo
argument_list|(
name|metaStoreRoot
argument_list|,
literal|"derby"
argument_list|)
decl_stmt|;
return|return
name|getMetaStoreVersion
argument_list|()
return|;
block|}
comment|//  write the given version to metastore
specifier|private
name|void
name|setVersion
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|version
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|MetaStoreSchemaInfo
name|schemInfo
init|=
operator|new
name|MetaStoreSchemaInfo
argument_list|(
name|metaStoreRoot
argument_list|,
literal|"derby"
argument_list|)
decl_stmt|;
name|setMetaStoreVersion
argument_list|(
name|version
argument_list|,
literal|"setVersion test"
argument_list|)
expr_stmt|;
block|}
comment|// Load the version stored in the metastore db
specifier|public
name|String
name|getMetaStoreVersion
parameter_list|()
throws|throws
name|HiveMetaException
block|{
name|ObjectStore
name|objStore
init|=
operator|new
name|ObjectStore
argument_list|()
decl_stmt|;
name|objStore
operator|.
name|setConf
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|objStore
operator|.
name|getMetaStoreSchemaVersion
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to get version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Store the given version and comment in the metastore
specifier|public
name|void
name|setMetaStoreVersion
parameter_list|(
name|String
name|newVersion
parameter_list|,
name|String
name|comment
parameter_list|)
throws|throws
name|HiveMetaException
block|{
name|ObjectStore
name|objStore
init|=
operator|new
name|ObjectStore
argument_list|()
decl_stmt|;
name|objStore
operator|.
name|setConf
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
try|try
block|{
name|objStore
operator|.
name|setMetaStoreSchemaVersion
argument_list|(
name|newVersion
argument_list|,
name|comment
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveMetaException
argument_list|(
literal|"Failed to set version"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


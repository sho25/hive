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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedWriter
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
name|io
operator|.
name|FileWriter
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Random
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
name|HiveMetaException
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
name|MetaStoreSchemaInfo
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
name|beeline
operator|.
name|HiveSchemaHelper
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
name|beeline
operator|.
name|HiveSchemaHelper
operator|.
name|NestedScriptParser
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
name|beeline
operator|.
name|HiveSchemaTool
import|;
end_import

begin_class
specifier|public
class|class
name|TestSchemaTool
extends|extends
name|TestCase
block|{
specifier|private
name|HiveSchemaTool
name|schemaTool
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
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
operator|new
name|Random
argument_list|()
operator|.
name|nextInt
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
name|schemaTool
operator|=
operator|new
name|HiveSchemaTool
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|,
name|hiveConf
argument_list|,
literal|"derby"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"beeLine.system.exit"
argument_list|,
literal|"true"
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
name|deleteDirectory
argument_list|(
name|metaStoreDir
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test dryrun of schema initialization    * @throws Exception    */
specifier|public
name|void
name|testSchemaInitDryRun
parameter_list|()
throws|throws
name|Exception
block|{
name|schemaTool
operator|.
name|setDryRun
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|doInit
argument_list|(
literal|"0.7.0"
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|setDryRun
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|schemaTool
operator|.
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveMetaException
name|e
parameter_list|)
block|{
comment|// The connection should fail since it the dry run
return|return;
block|}
name|fail
argument_list|(
literal|"Dry run shouldn't create actual metastore"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test dryrun of schema upgrade    * @throws Exception    */
specifier|public
name|void
name|testSchemaUpgradeDryRun
parameter_list|()
throws|throws
name|Exception
block|{
name|schemaTool
operator|.
name|doInit
argument_list|(
literal|"0.7.0"
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|setDryRun
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|doUpgrade
argument_list|(
literal|"0.7.0"
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|setDryRun
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|schemaTool
operator|.
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveMetaException
name|e
parameter_list|)
block|{
comment|// The connection should fail since it the dry run
return|return;
block|}
name|fail
argument_list|(
literal|"Dry run shouldn't upgrade metastore schema"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test schema initialization    * @throws Exception    */
specifier|public
name|void
name|testSchemaInit
parameter_list|()
throws|throws
name|Exception
block|{
name|schemaTool
operator|.
name|doInit
argument_list|(
name|MetaStoreSchemaInfo
operator|.
name|getHiveSchemaVersion
argument_list|()
argument_list|)
expr_stmt|;
name|schemaTool
operator|.
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test schema upgrade    * @throws Exception    */
specifier|public
name|void
name|testSchemaUpgrade
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|foundException
init|=
literal|false
decl_stmt|;
comment|// Initialize 0.7.0 schema
name|schemaTool
operator|.
name|doInit
argument_list|(
literal|"0.7.0"
argument_list|)
expr_stmt|;
comment|// verify that driver fails due to older version schema
try|try
block|{
name|schemaTool
operator|.
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveMetaException
name|e
parameter_list|)
block|{
comment|// Expected to fail due to old schema
name|foundException
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|foundException
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Hive operations shouldn't pass with older version schema"
argument_list|)
throw|;
block|}
comment|// upgrade schema from 0.7.0 to latest
name|schemaTool
operator|.
name|doUpgrade
argument_list|(
literal|"0.7.0"
argument_list|)
expr_stmt|;
comment|// verify that driver works fine with latest schema
name|schemaTool
operator|.
name|verifySchemaVersion
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test script formatting    * @throws Exception    */
specifier|public
name|void
name|testScripts
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|testScript
index|[]
init|=
block|{
literal|"-- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS fooTab;"
block|,
literal|"/*!1234 this is comment code like mysql */;"
block|,
literal|"CREATE TABLE fooTab(id INTEGER);"
block|,
literal|"DROP TABLE footab;"
block|,
literal|"-- ending comment"
block|}
decl_stmt|;
name|String
name|resultScript
index|[]
init|=
block|{
literal|"DROP TABLE IF EXISTS fooTab"
block|,
literal|"/*!1234 this is comment code like mysql */"
block|,
literal|"CREATE TABLE fooTab(id INTEGER)"
block|,
literal|"DROP TABLE footab"
block|,     }
decl_stmt|;
name|String
name|expectedSQL
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|resultScript
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
decl_stmt|;
name|File
name|testScriptFile
init|=
name|generateTestScript
argument_list|(
name|testScript
argument_list|)
decl_stmt|;
name|String
name|flattenedSql
init|=
name|HiveSchemaTool
operator|.
name|buildCommand
argument_list|(
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
literal|"derby"
argument_list|)
argument_list|,
name|testScriptFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|testScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedSQL
argument_list|,
name|flattenedSql
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test nested script formatting    * @throws Exception    */
specifier|public
name|void
name|testNestedScriptsForDerby
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|childTab1
init|=
literal|"childTab1"
decl_stmt|;
name|String
name|childTab2
init|=
literal|"childTab2"
decl_stmt|;
name|String
name|parentTab
init|=
literal|"fooTab"
decl_stmt|;
name|String
name|childTestScript1
index|[]
init|=
block|{
literal|"-- this is a comment "
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|childTab1
operator|+
literal|";"
block|,
literal|"CREATE TABLE "
operator|+
name|childTab1
operator|+
literal|"(id INTEGER);"
block|,
literal|"DROP TABLE "
operator|+
name|childTab1
operator|+
literal|";"
block|}
decl_stmt|;
name|String
name|childTestScript2
index|[]
init|=
block|{
literal|"-- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|childTab2
operator|+
literal|";"
block|,
literal|"CREATE TABLE "
operator|+
name|childTab2
operator|+
literal|"(id INTEGER);"
block|,
literal|"-- this is also a comment"
block|,
literal|"DROP TABLE "
operator|+
name|childTab2
operator|+
literal|";"
block|}
decl_stmt|;
name|String
name|parentTestScript
index|[]
init|=
block|{
literal|" -- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|parentTab
operator|+
literal|";"
block|,
literal|" -- this is another comment "
block|,
literal|"CREATE TABLE "
operator|+
name|parentTab
operator|+
literal|"(id INTEGER);"
block|,
literal|"RUN '"
operator|+
name|generateTestScript
argument_list|(
name|childTestScript1
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|"';"
block|,
literal|"DROP TABLE "
operator|+
name|parentTab
operator|+
literal|";"
block|,
literal|"RUN '"
operator|+
name|generateTestScript
argument_list|(
name|childTestScript2
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|"';"
block|,
literal|"--ending comment "
block|,       }
decl_stmt|;
name|File
name|testScriptFile
init|=
name|generateTestScript
argument_list|(
name|parentTestScript
argument_list|)
decl_stmt|;
name|String
name|flattenedSql
init|=
name|HiveSchemaTool
operator|.
name|buildCommand
argument_list|(
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
literal|"derby"
argument_list|)
argument_list|,
name|testScriptFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|testScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
literal|"RUN"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
literal|"comment"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|childTab1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|childTab2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|parentTab
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test nested script formatting    * @throws Exception    */
specifier|public
name|void
name|testNestedScriptsForMySQL
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|childTab1
init|=
literal|"childTab1"
decl_stmt|;
name|String
name|childTab2
init|=
literal|"childTab2"
decl_stmt|;
name|String
name|parentTab
init|=
literal|"fooTab"
decl_stmt|;
name|String
name|childTestScript1
index|[]
init|=
block|{
literal|"/* this is a comment code */"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|childTab1
operator|+
literal|";"
block|,
literal|"CREATE TABLE "
operator|+
name|childTab1
operator|+
literal|"(id INTEGER);"
block|,
literal|"DROP TABLE "
operator|+
name|childTab1
operator|+
literal|";"
block|}
decl_stmt|;
name|String
name|childTestScript2
index|[]
init|=
block|{
literal|"/* this is a special exec code */;"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|childTab2
operator|+
literal|";"
block|,
literal|"CREATE TABLE "
operator|+
name|childTab2
operator|+
literal|"(id INTEGER);"
block|,
literal|"-- this is a comment"
block|,
literal|"DROP TABLE "
operator|+
name|childTab2
operator|+
literal|";"
block|}
decl_stmt|;
name|String
name|parentTestScript
index|[]
init|=
block|{
literal|" -- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|parentTab
operator|+
literal|";"
block|,
literal|" /* this is special exec code */;"
block|,
literal|"CREATE TABLE "
operator|+
name|parentTab
operator|+
literal|"(id INTEGER);"
block|,
literal|"SOURCE "
operator|+
name|generateTestScript
argument_list|(
name|childTestScript1
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|";"
block|,
literal|"DROP TABLE "
operator|+
name|parentTab
operator|+
literal|";"
block|,
literal|"SOURCE "
operator|+
name|generateTestScript
argument_list|(
name|childTestScript2
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|";"
block|,
literal|"--ending comment "
block|,       }
decl_stmt|;
name|File
name|testScriptFile
init|=
name|generateTestScript
argument_list|(
name|parentTestScript
argument_list|)
decl_stmt|;
name|String
name|flattenedSql
init|=
name|HiveSchemaTool
operator|.
name|buildCommand
argument_list|(
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
literal|"mysql"
argument_list|)
argument_list|,
name|testScriptFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|testScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
literal|"RUN"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
literal|"comment"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|childTab1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|childTab2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|parentTab
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test script formatting    * @throws Exception    */
specifier|public
name|void
name|testScriptWithDelimiter
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|testScript
index|[]
init|=
block|{
literal|"-- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS fooTab;"
block|,
literal|"DELIMITER $$"
block|,
literal|"/*!1234 this is comment code like mysql */$$"
block|,
literal|"CREATE TABLE fooTab(id INTEGER)$$"
block|,
literal|"CREATE PROCEDURE fooProc()"
block|,
literal|"SELECT * FROM fooTab;"
block|,
literal|"CALL barProc();"
block|,
literal|"END PROCEDURE$$"
block|,
literal|"DELIMITER ;"
block|,
literal|"DROP TABLE footab;"
block|,
literal|"-- ending comment"
block|}
decl_stmt|;
name|String
name|resultScript
index|[]
init|=
block|{
literal|"DROP TABLE IF EXISTS fooTab"
block|,
literal|"/*!1234 this is comment code like mysql */"
block|,
literal|"CREATE TABLE fooTab(id INTEGER)"
block|,
literal|"CREATE PROCEDURE fooProc()"
operator|+
literal|" "
operator|+
literal|"SELECT * FROM fooTab;"
operator|+
literal|" "
operator|+
literal|"CALL barProc();"
operator|+
literal|" "
operator|+
literal|"END PROCEDURE"
block|,
literal|"DROP TABLE footab"
block|,     }
decl_stmt|;
name|String
name|expectedSQL
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|resultScript
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
decl_stmt|;
name|File
name|testScriptFile
init|=
name|generateTestScript
argument_list|(
name|testScript
argument_list|)
decl_stmt|;
name|NestedScriptParser
name|testDbParser
init|=
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
literal|"mysql"
argument_list|)
decl_stmt|;
name|String
name|flattenedSql
init|=
name|HiveSchemaTool
operator|.
name|buildCommand
argument_list|(
name|testDbParser
argument_list|,
name|testScriptFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|testScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedSQL
argument_list|,
name|flattenedSql
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test script formatting    * @throws Exception    */
specifier|public
name|void
name|testScriptMultiRowComment
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|testScript
index|[]
init|=
block|{
literal|"-- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS fooTab;"
block|,
literal|"DELIMITER $$"
block|,
literal|"/*!1234 this is comment code like mysql */$$"
block|,
literal|"CREATE TABLE fooTab(id INTEGER)$$"
block|,
literal|"DELIMITER ;"
block|,
literal|"/* multiline comment started "
block|,
literal|" * multiline comment continue"
block|,
literal|" * multiline comment ended */"
block|,
literal|"DROP TABLE footab;"
block|,
literal|"-- ending comment"
block|}
decl_stmt|;
name|String
name|parsedScript
index|[]
init|=
block|{
literal|"DROP TABLE IF EXISTS fooTab"
block|,
literal|"/*!1234 this is comment code like mysql */"
block|,
literal|"CREATE TABLE fooTab(id INTEGER)"
block|,
literal|"DROP TABLE footab"
block|,     }
decl_stmt|;
name|String
name|expectedSQL
init|=
name|StringUtils
operator|.
name|join
argument_list|(
name|parsedScript
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|)
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
decl_stmt|;
name|File
name|testScriptFile
init|=
name|generateTestScript
argument_list|(
name|testScript
argument_list|)
decl_stmt|;
name|NestedScriptParser
name|testDbParser
init|=
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
literal|"mysql"
argument_list|)
decl_stmt|;
name|String
name|flattenedSql
init|=
name|HiveSchemaTool
operator|.
name|buildCommand
argument_list|(
name|testDbParser
argument_list|,
name|testScriptFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|testScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedSQL
argument_list|,
name|flattenedSql
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test nested script formatting    * @throws Exception    */
specifier|public
name|void
name|testNestedScriptsForOracle
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|childTab1
init|=
literal|"childTab1"
decl_stmt|;
name|String
name|childTab2
init|=
literal|"childTab2"
decl_stmt|;
name|String
name|parentTab
init|=
literal|"fooTab"
decl_stmt|;
name|String
name|childTestScript1
index|[]
init|=
block|{
literal|"-- this is a comment "
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|childTab1
operator|+
literal|";"
block|,
literal|"CREATE TABLE "
operator|+
name|childTab1
operator|+
literal|"(id INTEGER);"
block|,
literal|"DROP TABLE "
operator|+
name|childTab1
operator|+
literal|";"
block|}
decl_stmt|;
name|String
name|childTestScript2
index|[]
init|=
block|{
literal|"-- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|childTab2
operator|+
literal|";"
block|,
literal|"CREATE TABLE "
operator|+
name|childTab2
operator|+
literal|"(id INTEGER);"
block|,
literal|"-- this is also a comment"
block|,
literal|"DROP TABLE "
operator|+
name|childTab2
operator|+
literal|";"
block|}
decl_stmt|;
name|String
name|parentTestScript
index|[]
init|=
block|{
literal|" -- this is a comment"
block|,
literal|"DROP TABLE IF EXISTS "
operator|+
name|parentTab
operator|+
literal|";"
block|,
literal|" -- this is another comment "
block|,
literal|"CREATE TABLE "
operator|+
name|parentTab
operator|+
literal|"(id INTEGER);"
block|,
literal|"@"
operator|+
name|generateTestScript
argument_list|(
name|childTestScript1
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|";"
block|,
literal|"DROP TABLE "
operator|+
name|parentTab
operator|+
literal|";"
block|,
literal|"@"
operator|+
name|generateTestScript
argument_list|(
name|childTestScript2
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|";"
block|,
literal|"--ending comment "
block|,       }
decl_stmt|;
name|File
name|testScriptFile
init|=
name|generateTestScript
argument_list|(
name|parentTestScript
argument_list|)
decl_stmt|;
name|String
name|flattenedSql
init|=
name|HiveSchemaTool
operator|.
name|buildCommand
argument_list|(
name|HiveSchemaHelper
operator|.
name|getDbCommandParser
argument_list|(
literal|"oracle"
argument_list|)
argument_list|,
name|testScriptFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|testScriptFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
literal|"@"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
literal|"comment"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|childTab1
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|childTab2
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|flattenedSql
operator|.
name|contains
argument_list|(
name|parentTab
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|File
name|generateTestScript
parameter_list|(
name|String
index|[]
name|stmts
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|testScriptFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"schematest"
argument_list|,
literal|".sql"
argument_list|)
decl_stmt|;
name|testScriptFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|FileWriter
name|fstream
init|=
operator|new
name|FileWriter
argument_list|(
name|testScriptFile
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|BufferedWriter
name|out
init|=
operator|new
name|BufferedWriter
argument_list|(
name|fstream
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|line
range|:
name|stmts
control|)
block|{
name|out
operator|.
name|write
argument_list|(
name|line
argument_list|)
expr_stmt|;
name|out
operator|.
name|newLine
argument_list|()
expr_stmt|;
block|}
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|testScriptFile
return|;
block|}
block|}
end_class

end_unit


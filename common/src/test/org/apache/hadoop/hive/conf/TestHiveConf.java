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
name|conf
package|;
end_package

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
name|fs
operator|.
name|Path
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
name|common
operator|.
name|util
operator|.
name|HiveTestUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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

begin_comment
comment|/**  * TestHiveConf  *  * Test cases for HiveConf. Loads configuration files located  * in common/src/test/resources.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveConf
block|{
annotation|@
name|Test
specifier|public
name|void
name|testHiveSitePath
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|expectedPath
init|=
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
literal|"hive-site.xml"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedPath
argument_list|,
operator|new
name|HiveConf
argument_list|()
operator|.
name|getHiveSiteLocation
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkHadoopConf
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|expectedHadoopVal
parameter_list|)
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedHadoopVal
argument_list|,
operator|new
name|Configuration
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkConfVar
parameter_list|(
name|ConfVars
name|var
parameter_list|,
name|String
name|expectedConfVarVal
parameter_list|)
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedConfVarVal
argument_list|,
name|var
operator|.
name|defaultVal
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkHiveConf
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|expectedHiveVal
parameter_list|)
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedHiveVal
argument_list|,
operator|new
name|HiveConf
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConfProperties
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Make sure null-valued ConfVar properties do not override the Hadoop Configuration
comment|// NOTE: Comment out the following test case for now until a better way to test is found,
comment|// as this test case cannot be reliably tested. The reason for this is that Hive does
comment|// overwrite fs.default.name in HiveConf if the property is set in system properties.
comment|// checkHadoopConf(ConfVars.HADOOPFS.varname, "core-site.xml");
comment|// checkConfVar(ConfVars.HADOOPFS, null);
comment|// checkHiveConf(ConfVars.HADOOPFS.varname, "core-site.xml");
comment|// Make sure non-null-valued ConfVar properties *do* override the Hadoop Configuration
name|checkHadoopConf
argument_list|(
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
operator|.
name|varname
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|checkConfVar
argument_list|(
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
name|checkHiveConf
argument_list|(
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
operator|.
name|varname
argument_list|,
literal|"-1"
argument_list|)
expr_stmt|;
comment|// Non-null ConfVar only defined in ConfVars
name|checkHadoopConf
argument_list|(
name|ConfVars
operator|.
name|HIVESKEWJOINKEY
operator|.
name|varname
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|checkConfVar
argument_list|(
name|ConfVars
operator|.
name|HIVESKEWJOINKEY
argument_list|,
literal|"100000"
argument_list|)
expr_stmt|;
name|checkHiveConf
argument_list|(
name|ConfVars
operator|.
name|HIVESKEWJOINKEY
operator|.
name|varname
argument_list|,
literal|"100000"
argument_list|)
expr_stmt|;
comment|// ConfVar overridden in in hive-site.xml
name|checkHadoopConf
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
operator|.
name|varname
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|checkConfVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
argument_list|,
literal|"org.apache.derby.jdbc.EmbeddedDriver"
argument_list|)
expr_stmt|;
name|checkHiveConf
argument_list|(
name|ConfVars
operator|.
name|METASTORE_CONNECTION_DRIVER
operator|.
name|varname
argument_list|,
literal|"hive-site.xml"
argument_list|)
expr_stmt|;
comment|// Property defined in hive-site.xml only
name|checkHadoopConf
argument_list|(
literal|"test.property1"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|checkHiveConf
argument_list|(
literal|"test.property1"
argument_list|,
literal|"hive-site.xml"
argument_list|)
expr_stmt|;
comment|// Test HiveConf property variable substitution in hive-site.xml
name|checkHiveConf
argument_list|(
literal|"test.var.hiveconf.property"
argument_list|,
name|ConfVars
operator|.
name|DEFAULTPARTITIONNAME
operator|.
name|defaultVal
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testColumnNameMapping
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|i
operator|==
name|HiveConf
operator|.
name|getPositionFromInternalName
argument_list|(
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


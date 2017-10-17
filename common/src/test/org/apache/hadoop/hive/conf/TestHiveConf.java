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
name|util
operator|.
name|Shell
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|String
name|hiveSiteLocation
init|=
name|HiveConf
operator|.
name|getHiveSiteLocation
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedPath
argument_list|,
name|hiveSiteLocation
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
name|JobConf
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
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
name|getDefaultValue
argument_list|()
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
name|HIVETESTMODEDUMMYSTATAGGR
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
name|HIVETESTMODEDUMMYSTATAGGR
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|checkHiveConf
argument_list|(
name|ConfVars
operator|.
name|HIVETESTMODEDUMMYSTATAGGR
operator|.
name|varname
argument_list|,
literal|"value2"
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
literal|"value1"
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
name|getDefaultValue
argument_list|()
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
annotation|@
name|Test
specifier|public
name|void
name|testUnitFor
parameter_list|()
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"L"
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|""
argument_list|,
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"d"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"days"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"h"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"hours"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"m"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"minutes"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"s"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"seconds"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"ms"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"msecs"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"us"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"usecs"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"ns"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|,
name|HiveConf
operator|.
name|unitFor
argument_list|(
literal|"nsecs"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToSizeBytes
parameter_list|()
throws|throws
name|Exception
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1b"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1bytes"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1024L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1kb"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1048576L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1mb"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1073741824L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1gb"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1099511627776L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1tb"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1125899906842624L
argument_list|,
name|HiveConf
operator|.
name|toSizeBytes
argument_list|(
literal|"1pb"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHiddenConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
comment|// check password configs are hidden
name|Assert
operator|.
name|assertTrue
argument_list|(
name|conf
operator|.
name|isHiddenConfig
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|conf
operator|.
name|isHiddenConfig
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
comment|// check change hidden list should fail
try|try
block|{
specifier|final
name|String
name|name
init|=
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CONF_HIDDEN_LIST
operator|.
name|varname
decl_stmt|;
name|conf
operator|.
name|verifyAndSet
argument_list|(
name|name
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|conf
operator|.
name|verifyAndSet
argument_list|(
name|name
operator|+
literal|"postfix"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Setting config property "
operator|+
name|name
operator|+
literal|" should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// the verifyAndSet in this case is expected to fail with the IllegalArgumentException
block|}
comment|// check stripHiddenConfigurations
name|Configuration
name|conf2
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|conf2
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|,
literal|"password"
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
argument_list|,
literal|"password"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|stripHiddenConfigurations
argument_list|(
name|conf2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|conf
operator|.
name|isHiddenConfig
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
operator|+
literal|"postfix"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|conf
operator|.
name|isHiddenConfig
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
operator|+
literal|"postfix"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSparkConfigUpdate
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|verifyAndSet
argument_list|(
literal|"spark.master"
argument_list|,
literal|"yarn"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|verifyAndSet
argument_list|(
literal|"hive.execution.engine"
argument_list|,
literal|"spark"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Expected spark config updated."
argument_list|,
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setSparkConfigUpdated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEncodingDecoding
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|String
name|query
init|=
literal|"select blah, '\u0001' from random_table"
decl_stmt|;
name|conf
operator|.
name|setQueryString
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|query
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|ConfVars
operator|.
name|HIVEQUERYSTRING
operator|.
name|varname
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|query
argument_list|,
name|conf
operator|.
name|getQueryString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


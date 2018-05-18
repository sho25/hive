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
name|ql
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
name|hive
operator|.
name|ql
operator|.
name|QTestUtil
operator|.
name|FsType
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
comment|/**  * This class contains unit tests for QTestUtil  */
end_comment

begin_class
specifier|public
class|class
name|TestQOutProcessor
block|{
name|QOutProcessor
name|qOutProcessor
init|=
operator|new
name|QOutProcessor
argument_list|(
name|FsType
operator|.
name|local
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSelectiveHdfsPatternMaskOnlyHdfsPath
parameter_list|()
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"nothing to be masked"
argument_list|,
name|processLine
argument_list|(
literal|"nothing to be masked"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"hdfs://"
argument_list|,
name|processLine
argument_list|(
literal|"hdfs://"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"hdfs:///"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"hdfs://a"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"hdfs://%s other text"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"hdfs://tmp.dfs.com:50029/tmp other text"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"hdfs://localhost:51594/build/ql/test/data/warehouse/default/encrypted_table_dp/p=2014-09-23"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"hdfs://localhost:11111/tmp/ct_noperm_loc_foo1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"hdfs://%s hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"hdfs://one hdfs://two"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"some text before [name=hdfs://%s]] some text between hdfs://%s some text after"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"some text before [name=hdfs://localhost:11111/tmp/ct_noperm_loc_foo1]] some text between hdfs://localhost:22222/tmp/ct_noperm_loc_foo2 some text after"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"-rw-r--r--   3 %s %s       2557 %s hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_USER_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_GROUP_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_DATE_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"-rw-r--r--   3 hiveptest supergroup       2557 2018-01-11 17:09 hdfs://hello_hdfs_path"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"-rw-r--r--   3 %s %s       2557 %s hdfs://%s"
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_USER_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_GROUP_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_DATE_MASK
argument_list|,
name|QOutProcessor
operator|.
name|HDFS_MASK
argument_list|)
argument_list|,
name|processLine
argument_list|(
literal|"-rw-r--r--   3 hiveptest supergroup       2557 2018-01-11 17:09 hdfs://hello_hdfs_path"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|processLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
return|return
name|qOutProcessor
operator|.
name|processLine
argument_list|(
name|line
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit


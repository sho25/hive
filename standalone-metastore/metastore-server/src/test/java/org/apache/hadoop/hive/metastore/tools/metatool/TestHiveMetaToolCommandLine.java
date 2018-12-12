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
name|metastore
operator|.
name|tools
operator|.
name|metatool
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|ParseException
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
name|annotation
operator|.
name|MetastoreUnitTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
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
name|assertEquals
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
name|assertFalse
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
name|assertNull
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

begin_comment
comment|/** Unit tests for HiveMetaToolCommandLine. */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHiveMetaToolCommandLine
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|ExpectedException
name|exception
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testParseListFSRoot
parameter_list|()
throws|throws
name|ParseException
block|{
name|HiveMetaToolCommandLine
name|cl
init|=
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-listFSRoot"
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|cl
operator|.
name|isListFSRoot
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isExecuteJDOQL
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getJDOQLQuery
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isUpdateLocation
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getUpddateLocationParams
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isDryRun
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getSerdePropKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getTablePropKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseExecuteJDOQL
parameter_list|()
throws|throws
name|ParseException
block|{
name|HiveMetaToolCommandLine
name|cl
init|=
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-executeJDOQL"
block|,
literal|"select a from b"
block|}
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isListFSRoot
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cl
operator|.
name|isExecuteJDOQL
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"select a from b"
argument_list|,
name|cl
operator|.
name|getJDOQLQuery
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isUpdateLocation
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getUpddateLocationParams
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isDryRun
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getSerdePropKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getTablePropKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseUpdateLocation
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"-updateLocation"
block|,
literal|"hdfs://new.loc"
block|,
literal|"hdfs://old.loc"
block|,
literal|"-dryRun"
block|,
literal|"-serdePropKey"
block|,
literal|"abc"
block|,
literal|"-tablePropKey"
block|,
literal|"def"
block|}
decl_stmt|;
name|HiveMetaToolCommandLine
name|cl
init|=
operator|new
name|HiveMetaToolCommandLine
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isListFSRoot
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|cl
operator|.
name|isExecuteJDOQL
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cl
operator|.
name|getJDOQLQuery
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cl
operator|.
name|isUpdateLocation
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs://new.loc"
argument_list|,
name|cl
operator|.
name|getUpddateLocationParams
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs://old.loc"
argument_list|,
name|cl
operator|.
name|getUpddateLocationParams
argument_list|()
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cl
operator|.
name|isDryRun
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"abc"
argument_list|,
name|cl
operator|.
name|getSerdePropKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"def"
argument_list|,
name|cl
operator|.
name|getTablePropKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoTask
parameter_list|()
throws|throws
name|ParseException
block|{
name|exception
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"exectly one of -listFSRoot, -executeJDOQL, -updateLocation must be set"
argument_list|)
expr_stmt|;
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleTask
parameter_list|()
throws|throws
name|ParseException
block|{
name|exception
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"exectly one of -listFSRoot, -executeJDOQL, -updateLocation must be set"
argument_list|)
expr_stmt|;
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-listFSRoot"
block|,
literal|"-executeJDOQL"
block|,
literal|"select a from b"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateLocationOneArgument
parameter_list|()
throws|throws
name|ParseException
block|{
name|exception
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"HiveMetaTool:updateLocation takes in 2 arguments but was passed 1 arguments"
argument_list|)
expr_stmt|;
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-updateLocation"
block|,
literal|"hdfs://abc.de"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDryRunNotAllowed
parameter_list|()
throws|throws
name|ParseException
block|{
name|exception
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"-dryRun, -serdePropKey, -tablePropKey may be used only for the -updateLocation command"
argument_list|)
expr_stmt|;
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-listFSRoot"
block|,
literal|"-dryRun"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerdePropKeyNotAllowed
parameter_list|()
throws|throws
name|ParseException
block|{
name|exception
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"-dryRun, -serdePropKey, -tablePropKey may be used only for the -updateLocation command"
argument_list|)
expr_stmt|;
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-listFSRoot"
block|,
literal|"-serdePropKey"
block|,
literal|"abc"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTablePropKeyNotAllowed
parameter_list|()
throws|throws
name|ParseException
block|{
name|exception
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exception
operator|.
name|expectMessage
argument_list|(
literal|"-dryRun, -serdePropKey, -tablePropKey may be used only for the -updateLocation command"
argument_list|)
expr_stmt|;
operator|new
name|HiveMetaToolCommandLine
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-executeJDOQL"
block|,
literal|"select a from b"
block|,
literal|"-tablePropKey"
block|,
literal|"abc"
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


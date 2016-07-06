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
name|ql
operator|.
name|exec
package|;
end_package

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
name|fail
import|;
end_import

begin_import
import|import static
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
name|exec
operator|.
name|Utilities
operator|.
name|getFileExtension
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|List
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
name|FileSystem
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
name|io
operator|.
name|HiveIgnoreKeyTextOutputFormat
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
name|HiveException
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
name|Table
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
name|plan
operator|.
name|DynamicPartitionCtx
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
name|plan
operator|.
name|ExprNodeConstantDesc
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
name|plan
operator|.
name|ExprNodeDesc
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
name|plan
operator|.
name|ExprNodeGenericFuncDesc
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
name|plan
operator|.
name|FileSinkDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFFromUtcTimestamp
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|rules
operator|.
name|TemporaryFolder
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Files
import|;
end_import

begin_class
specifier|public
class|class
name|TestUtilities
block|{
annotation|@
name|Rule
specifier|public
name|TemporaryFolder
name|temporaryFolder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestUtilities
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_BUCKETS
init|=
literal|3
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testGetFileExtension
parameter_list|()
block|{
name|JobConf
name|jc
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for uncompressed unknown format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for compressed unknown format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for uncompressed text format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deflate for uncompressed text format"
argument_list|,
literal|".deflate"
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for uncompressed default format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deflate for uncompressed default format"
argument_list|,
literal|".deflate"
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|extension
init|=
literal|".myext"
decl_stmt|;
name|jc
operator|.
name|set
argument_list|(
literal|"hive.output.file.extension"
argument_list|,
name|extension
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for uncompressed unknown format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for compressed unknown format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for uncompressed text format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for uncompressed text format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSerializeTimestamp
parameter_list|()
block|{
name|Timestamp
name|ts
init|=
operator|new
name|Timestamp
argument_list|(
literal|1374554702000L
argument_list|)
decl_stmt|;
name|ts
operator|.
name|setNanos
argument_list|(
literal|123456
argument_list|)
expr_stmt|;
name|ExprNodeConstantDesc
name|constant
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|ts
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|constant
argument_list|)
expr_stmt|;
name|ExprNodeGenericFuncDesc
name|desc
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|,
operator|new
name|GenericUDFFromUtcTimestamp
argument_list|()
argument_list|,
name|children
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|desc
operator|.
name|getExprString
argument_list|()
argument_list|,
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|SerializationUtilities
operator|.
name|serializeExpression
argument_list|(
name|desc
argument_list|)
argument_list|)
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testgetDbTableName
parameter_list|()
throws|throws
name|HiveException
block|{
name|String
name|tablename
decl_stmt|;
name|String
index|[]
name|dbtab
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|curDefaultdb
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
decl_stmt|;
comment|//test table without db portion
name|tablename
operator|=
literal|"tab1"
expr_stmt|;
name|dbtab
operator|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"db name"
argument_list|,
name|curDefaultdb
argument_list|,
name|dbtab
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"table name"
argument_list|,
name|tablename
argument_list|,
name|dbtab
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|//test table with db portion
name|tablename
operator|=
literal|"dab1.tab1"
expr_stmt|;
name|dbtab
operator|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"db name"
argument_list|,
literal|"dab1"
argument_list|,
name|dbtab
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"table name"
argument_list|,
literal|"tab1"
argument_list|,
name|dbtab
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
comment|//test invalid table name
name|tablename
operator|=
literal|"dab1.tab1.x1"
expr_stmt|;
try|try
block|{
name|dbtab
operator|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception was expected for invalid table name"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Invalid table name "
operator|+
name|tablename
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetJarFilesByPath
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|File
name|f
init|=
name|Files
operator|.
name|createTempDir
argument_list|()
decl_stmt|;
name|String
name|jarFileName1
init|=
name|f
operator|.
name|getAbsolutePath
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"a.jar"
decl_stmt|;
name|String
name|jarFileName2
init|=
name|f
operator|.
name|getAbsolutePath
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"b.jar"
decl_stmt|;
name|File
name|jarFile
init|=
operator|new
name|File
argument_list|(
name|jarFileName1
argument_list|)
decl_stmt|;
try|try
block|{
name|FileUtils
operator|.
name|touch
argument_list|(
name|jarFile
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|jars
init|=
name|Utilities
operator|.
name|getJarFilesByPath
argument_list|(
name|f
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"file://"
operator|+
name|jarFileName1
argument_list|)
argument_list|,
name|jars
argument_list|)
expr_stmt|;
name|jars
operator|=
name|Utilities
operator|.
name|getJarFilesByPath
argument_list|(
literal|"/folder/not/exist"
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|jars
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|jarFile2
init|=
operator|new
name|File
argument_list|(
name|jarFileName2
argument_list|)
decl_stmt|;
name|FileUtils
operator|.
name|touch
argument_list|(
name|jarFile2
argument_list|)
expr_stmt|;
name|String
name|newPath
init|=
literal|"file://"
operator|+
name|jarFileName1
operator|+
literal|","
operator|+
literal|"file://"
operator|+
name|jarFileName2
operator|+
literal|",/file/not/exist"
decl_stmt|;
name|jars
operator|=
name|Utilities
operator|.
name|getJarFilesByPath
argument_list|(
name|newPath
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"file://"
operator|+
name|jarFileName1
argument_list|,
literal|"file://"
operator|+
name|jarFileName2
argument_list|)
argument_list|,
name|jars
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"failed to copy file to reloading folder"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReplaceTaskId
parameter_list|()
block|{
name|String
name|taskID
init|=
literal|"000000"
decl_stmt|;
name|int
name|bucketNum
init|=
literal|1
decl_stmt|;
name|String
name|newTaskID
init|=
name|Utilities
operator|.
name|replaceTaskId
argument_list|(
name|taskID
argument_list|,
name|bucketNum
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"000001"
argument_list|,
name|newTaskID
argument_list|)
expr_stmt|;
name|taskID
operator|=
literal|"(ds%3D1)000001"
expr_stmt|;
name|newTaskID
operator|=
name|Utilities
operator|.
name|replaceTaskId
argument_list|(
name|taskID
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"(ds%3D1)000005"
argument_list|,
name|newTaskID
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMaskIfPassword
parameter_list|()
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|""
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"test"
argument_list|,
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|null
argument_list|,
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"test2"
argument_list|,
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|"any"
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"###_MASKED_###"
argument_list|,
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|"password"
argument_list|,
literal|"test3"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"###_MASKED_###"
argument_list|,
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|"a_passWord"
argument_list|,
literal|"test4"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"###_MASKED_###"
argument_list|,
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|"password_a"
argument_list|,
literal|"test5"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"###_MASKED_###"
argument_list|,
name|Utilities
operator|.
name|maskIfPassword
argument_list|(
literal|"a_PassWord_a"
argument_list|,
literal|"test6"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveTempOrDuplicateFilesOnTezNoDp
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|runRemoveTempOrDuplicateFilesTestCase
argument_list|(
literal|"tez"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveTempOrDuplicateFilesOnTezWithDp
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|runRemoveTempOrDuplicateFilesTestCase
argument_list|(
literal|"tez"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveTempOrDuplicateFilesOnMrNoDp
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|runRemoveTempOrDuplicateFilesTestCase
argument_list|(
literal|"mr"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|NUM_BUCKETS
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRemoveTempOrDuplicateFilesOnMrWithDp
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|runRemoveTempOrDuplicateFilesTestCase
argument_list|(
literal|"mr"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|NUM_BUCKETS
argument_list|,
name|paths
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|runRemoveTempOrDuplicateFilesTestCase
parameter_list|(
name|String
name|executionEngine
parameter_list|,
name|boolean
name|dPEnabled
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|hconf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
comment|// do this to verify that Utilities.removeTempOrDuplicateFiles does not revert to default scheme information
name|hconf
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
literal|"hdfs://should-not-be-used/"
argument_list|)
expr_stmt|;
name|hconf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|varname
argument_list|,
name|executionEngine
argument_list|)
expr_stmt|;
name|FileSystem
name|localFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|DynamicPartitionCtx
name|dpCtx
init|=
name|getDynamicPartitionCtx
argument_list|(
name|dPEnabled
argument_list|)
decl_stmt|;
name|Path
name|tempDirPath
init|=
name|setupTempDirWithSingleOutputFile
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|FileSinkDesc
name|conf
init|=
name|getFileSinkDesc
argument_list|(
name|tempDirPath
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|Utilities
operator|.
name|removeTempOrDuplicateFiles
argument_list|(
name|localFs
argument_list|,
name|tempDirPath
argument_list|,
name|dpCtx
argument_list|,
name|conf
argument_list|,
name|hconf
argument_list|)
decl_stmt|;
name|String
name|expectedScheme
init|=
name|tempDirPath
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|expectedAuthority
init|=
name|tempDirPath
operator|.
name|toUri
argument_list|()
operator|.
name|getAuthority
argument_list|()
decl_stmt|;
name|assertPathsMatchSchemeAndAuthority
argument_list|(
name|expectedScheme
argument_list|,
name|expectedAuthority
argument_list|,
name|paths
argument_list|)
expr_stmt|;
return|return
name|paths
return|;
block|}
specifier|private
name|void
name|assertPathsMatchSchemeAndAuthority
parameter_list|(
name|String
name|expectedScheme
parameter_list|,
name|String
name|expectedAuthority
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|paths
parameter_list|)
block|{
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
name|assertEquals
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|expectedScheme
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|expectedAuthority
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|DynamicPartitionCtx
name|getDynamicPartitionCtx
parameter_list|(
name|boolean
name|dPEnabled
parameter_list|)
block|{
name|DynamicPartitionCtx
name|dpCtx
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dPEnabled
condition|)
block|{
name|dpCtx
operator|=
name|mock
argument_list|(
name|DynamicPartitionCtx
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|dpCtx
operator|.
name|getNumDPCols
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|dpCtx
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|NUM_BUCKETS
argument_list|)
expr_stmt|;
block|}
return|return
name|dpCtx
return|;
block|}
specifier|private
name|FileSinkDesc
name|getFileSinkDesc
parameter_list|(
name|Path
name|tempDirPath
parameter_list|)
block|{
name|Table
name|table
init|=
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|table
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|NUM_BUCKETS
argument_list|)
expr_stmt|;
name|FileSinkDesc
name|conf
init|=
operator|new
name|FileSinkDesc
argument_list|(
name|tempDirPath
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
specifier|private
name|Path
name|setupTempDirWithSingleOutputFile
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tempDirPath
init|=
operator|new
name|Path
argument_list|(
literal|"file://"
operator|+
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|taskOutputPath
init|=
operator|new
name|Path
argument_list|(
name|tempDirPath
argument_list|,
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|hconf
argument_list|)
argument_list|)
decl_stmt|;
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|hconf
argument_list|)
operator|.
name|create
argument_list|(
name|taskOutputPath
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|tempDirPath
return|;
block|}
block|}
end_class

end_unit


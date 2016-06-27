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
name|HashSet
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

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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

begin_class
specifier|public
class|class
name|TestUtilities
extends|extends
name|TestCase
block|{
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
specifier|public
name|void
name|testGetJarFilesByPath
parameter_list|()
block|{
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
name|HashSet
argument_list|<
name|String
argument_list|>
name|jars
init|=
operator|(
name|HashSet
operator|)
name|Utilities
operator|.
name|getJarFilesByPath
argument_list|(
name|f
operator|.
name|getAbsolutePath
argument_list|()
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
name|jarFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|,
name|jars
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
decl_stmt|;
name|jars
operator|=
operator|(
name|HashSet
operator|)
name|Utilities
operator|.
name|getJarFilesByPath
argument_list|(
name|newPath
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
block|}
end_class

end_unit


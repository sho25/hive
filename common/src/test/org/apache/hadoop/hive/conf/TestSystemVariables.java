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
name|LocalFileSystem
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertNull
import|;
end_import

begin_class
specifier|public
class|class
name|TestSystemVariables
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SYSTEM
init|=
literal|"system"
decl_stmt|;
specifier|private
name|String
name|makeVarName
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"${%s:%s}"
argument_list|,
name|prefix
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test_RelativeJavaIoTmpDir_CoercedTo_AbsolutePath
parameter_list|()
block|{
name|FileSystem
name|localFileSystem
init|=
operator|new
name|LocalFileSystem
argument_list|()
decl_stmt|;
name|String
name|systemJavaIoTmpDir
init|=
name|makeVarName
argument_list|(
name|SYSTEM
argument_list|,
literal|"java.io.tmpdir"
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
literal|"./relativePath"
argument_list|)
expr_stmt|;
name|Path
name|relativePath
init|=
operator|new
name|Path
argument_list|(
name|localFileSystem
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"./relativePath"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|relativePath
operator|.
name|toString
argument_list|()
argument_list|,
name|SystemVariables
operator|.
name|substitute
argument_list|(
name|systemJavaIoTmpDir
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
literal|"this/is/a/relative/path"
argument_list|)
expr_stmt|;
name|Path
name|thisIsARelativePath
init|=
operator|new
name|Path
argument_list|(
name|localFileSystem
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"this/is/a/relative/path"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|thisIsARelativePath
operator|.
name|toString
argument_list|()
argument_list|,
name|SystemVariables
operator|.
name|substitute
argument_list|(
name|systemJavaIoTmpDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test_AbsoluteJavaIoTmpDir_NotChanged
parameter_list|()
block|{
name|FileSystem
name|localFileSystem
init|=
operator|new
name|LocalFileSystem
argument_list|()
decl_stmt|;
name|String
name|systemJavaIoTmpDir
init|=
name|makeVarName
argument_list|(
name|SYSTEM
argument_list|,
literal|"java.io.tmpdir"
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
literal|"file:/this/is/an/absolute/path"
argument_list|)
expr_stmt|;
name|Path
name|absolutePath
init|=
operator|new
name|Path
argument_list|(
literal|"file:/this/is/an/absolute/path"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|absolutePath
operator|.
name|toString
argument_list|()
argument_list|,
name|SystemVariables
operator|.
name|substitute
argument_list|(
name|systemJavaIoTmpDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test_RelativePathWithNoCoercion_NotChanged
parameter_list|()
block|{
name|FileSystem
name|localFileSystem
init|=
operator|new
name|LocalFileSystem
argument_list|()
decl_stmt|;
name|String
name|systemJavaIoTmpDir
init|=
name|makeVarName
argument_list|(
name|SYSTEM
argument_list|,
literal|"java.io._NOT_tmpdir"
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io._NOT_tmpdir"
argument_list|,
literal|"this/is/an/relative/path"
argument_list|)
expr_stmt|;
name|Path
name|relativePath
init|=
operator|new
name|Path
argument_list|(
literal|"this/is/an/relative/path"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|relativePath
operator|.
name|toString
argument_list|()
argument_list|,
name|SystemVariables
operator|.
name|substitute
argument_list|(
name|systemJavaIoTmpDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test_EmptyJavaIoTmpDir_NotChanged
parameter_list|()
block|{
name|FileSystem
name|localFileSystem
init|=
operator|new
name|LocalFileSystem
argument_list|()
decl_stmt|;
name|String
name|systemJavaIoTmpDir
init|=
name|makeVarName
argument_list|(
name|SYSTEM
argument_list|,
literal|"java.io.tmpdir"
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|SystemVariables
operator|.
name|substitute
argument_list|(
name|systemJavaIoTmpDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


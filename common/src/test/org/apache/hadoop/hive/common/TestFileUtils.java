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
name|common
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
name|Set
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
name|TestFileUtils
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
name|TestFileUtils
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|isPathWithinSubtree_samePrefix
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"/somedir1"
argument_list|)
decl_stmt|;
name|Path
name|subtree
init|=
operator|new
name|Path
argument_list|(
literal|"/somedir"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|path
argument_list|,
name|subtree
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|isPathWithinSubtree_rootIsInside
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"/foo"
argument_list|)
decl_stmt|;
name|Path
name|subtree
init|=
operator|new
name|Path
argument_list|(
literal|"/foo"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|path
argument_list|,
name|subtree
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|isPathWithinSubtree_descendantInside
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"/foo/bar"
argument_list|)
decl_stmt|;
name|Path
name|subtree
init|=
operator|new
name|Path
argument_list|(
literal|"/foo"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|path
argument_list|,
name|subtree
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|isPathWithinSubtree_relativeWalk
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"foo/../../bar"
argument_list|)
decl_stmt|;
name|Path
name|subtree
init|=
operator|new
name|Path
argument_list|(
literal|"../bar"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|path
argument_list|,
name|subtree
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getParentRegardlessOfScheme_badCases
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"proto://host1/foo/bar/baz"
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|candidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
literal|"badproto://host1/foo"
argument_list|)
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
literal|"proto://badhost1/foo"
argument_list|)
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
literal|"proto://host1:71/foo/bar/baz"
argument_list|)
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
literal|"proto://host1/badfoo"
argument_list|)
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/badfoo"
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|res
init|=
name|FileUtils
operator|.
name|getParentRegardlessOfScheme
argument_list|(
name|path
argument_list|,
name|candidates
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"none of these paths may match"
argument_list|,
name|res
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getParentRegardlessOfScheme_priority
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"proto://host1/foo/bar/baz"
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|candidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Path
name|expectedPath
decl_stmt|;
name|candidates
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
literal|"proto://host1/"
argument_list|)
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|add
argument_list|(
name|expectedPath
operator|=
operator|new
name|Path
argument_list|(
literal|"proto://host1/foo"
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|res
init|=
name|FileUtils
operator|.
name|getParentRegardlessOfScheme
argument_list|(
name|path
argument_list|,
name|candidates
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedPath
argument_list|,
name|res
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getParentRegardlessOfScheme_root
parameter_list|()
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
literal|"proto://host1/foo"
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|candidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Path
name|expectedPath
decl_stmt|;
name|candidates
operator|.
name|add
argument_list|(
name|expectedPath
operator|=
operator|new
name|Path
argument_list|(
literal|"proto://host1/foo"
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|res
init|=
name|FileUtils
operator|.
name|getParentRegardlessOfScheme
argument_list|(
name|path
argument_list|,
name|candidates
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedPath
argument_list|,
name|res
argument_list|)
expr_stmt|;
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
name|tmpDir
init|=
name|Files
operator|.
name|createTempDir
argument_list|()
decl_stmt|;
name|String
name|jarFileName1
init|=
name|tmpDir
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
name|tmpDir
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
name|jarFile1
init|=
operator|new
name|File
argument_list|(
name|jarFileName1
argument_list|)
decl_stmt|;
try|try
block|{
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
operator|.
name|touch
argument_list|(
name|jarFile1
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|jars
init|=
name|FileUtils
operator|.
name|getJarFilesByPath
argument_list|(
name|tmpDir
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
name|FileUtils
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
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
name|FileUtils
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
operator|.
name|deleteQuietly
argument_list|(
name|tmpDir
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRelativePathToAbsolutePath
parameter_list|()
throws|throws
name|IOException
block|{
name|LocalFileSystem
name|localFileSystem
init|=
operator|new
name|LocalFileSystem
argument_list|()
decl_stmt|;
name|Path
name|actualPath
init|=
name|FileUtils
operator|.
name|makeAbsolute
argument_list|(
name|localFileSystem
argument_list|,
operator|new
name|Path
argument_list|(
literal|"relative/path"
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|expectedPath
init|=
operator|new
name|Path
argument_list|(
name|localFileSystem
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"relative/path"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedPath
operator|.
name|toString
argument_list|()
argument_list|,
name|actualPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|absolutePath
init|=
operator|new
name|Path
argument_list|(
literal|"/absolute/path"
argument_list|)
decl_stmt|;
name|Path
name|unchangedPath
init|=
name|FileUtils
operator|.
name|makeAbsolute
argument_list|(
name|localFileSystem
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/absolute/path"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|unchangedPath
operator|.
name|toString
argument_list|()
argument_list|,
name|absolutePath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


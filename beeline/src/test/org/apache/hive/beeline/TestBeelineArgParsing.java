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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
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
name|FileOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
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
name|Before
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
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Unit test for Beeline arg parser.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBeelineArgParsing
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
name|TestBeelineArgParsing
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dummyDriverClazzName
init|=
literal|"DummyDriver"
decl_stmt|;
specifier|private
name|String
name|connectionString
decl_stmt|;
specifier|private
name|String
name|driverClazzName
decl_stmt|;
specifier|private
name|String
name|driverJarFileName
decl_stmt|;
specifier|private
name|boolean
name|defaultSupported
decl_stmt|;
specifier|public
name|TestBeelineArgParsing
parameter_list|(
name|String
name|connectionString
parameter_list|,
name|String
name|driverClazzName
parameter_list|,
name|String
name|driverJarFileName
parameter_list|,
name|boolean
name|defaultSupported
parameter_list|)
block|{
name|this
operator|.
name|connectionString
operator|=
name|connectionString
expr_stmt|;
name|this
operator|.
name|driverClazzName
operator|=
name|driverClazzName
expr_stmt|;
name|this
operator|.
name|driverJarFileName
operator|=
name|driverJarFileName
expr_stmt|;
name|this
operator|.
name|defaultSupported
operator|=
name|defaultSupported
expr_stmt|;
block|}
specifier|public
class|class
name|TestBeeline
extends|extends
name|BeeLine
block|{
name|String
name|connectArgs
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|properties
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
name|boolean
name|dispatch
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|String
name|connectCommand
init|=
literal|"!connect"
decl_stmt|;
name|String
name|propertyCommand
init|=
literal|"!properties"
decl_stmt|;
if|if
condition|(
name|command
operator|.
name|startsWith
argument_list|(
name|connectCommand
argument_list|)
condition|)
block|{
name|this
operator|.
name|connectArgs
operator|=
name|command
operator|.
name|substring
argument_list|(
name|connectCommand
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|command
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|command
operator|.
name|startsWith
argument_list|(
name|propertyCommand
argument_list|)
condition|)
block|{
name|this
operator|.
name|properties
operator|.
name|add
argument_list|(
name|command
operator|.
name|substring
argument_list|(
name|propertyCommand
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|,
name|command
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|queries
operator|.
name|add
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|addlocaldrivername
parameter_list|(
name|String
name|driverName
parameter_list|)
block|{
name|String
name|line
init|=
literal|"addlocaldrivername "
operator|+
name|driverName
decl_stmt|;
return|return
name|getCommands
argument_list|()
operator|.
name|addlocaldrivername
argument_list|(
name|line
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|addLocalJar
parameter_list|(
name|String
name|url
parameter_list|)
block|{
name|String
name|line
init|=
literal|"addlocaldriverjar "
operator|+
name|url
decl_stmt|;
return|return
name|getCommands
argument_list|()
operator|.
name|addlocaldriverjar
argument_list|(
name|line
argument_list|)
return|;
block|}
block|}
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{1}"
argument_list|)
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|data
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// generate the dummy driver by using txt file
name|String
name|u
init|=
name|HiveTestUtils
operator|.
name|getFileFromClasspath
argument_list|(
literal|"DummyDriver.txt"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|File
argument_list|,
name|String
argument_list|>
name|extraContent
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|extraContent
operator|.
name|put
argument_list|(
operator|new
name|File
argument_list|(
literal|"META-INF/services/java.sql.Driver"
argument_list|)
argument_list|,
name|dummyDriverClazzName
argument_list|)
expr_stmt|;
name|File
name|jarFile
init|=
name|HiveTestUtils
operator|.
name|genLocalJarForTest
argument_list|(
name|u
argument_list|,
name|dummyDriverClazzName
argument_list|,
name|extraContent
argument_list|)
decl_stmt|;
name|String
name|pathToDummyDriver
init|=
name|jarFile
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
block|{
literal|"jdbc:postgresql://host:5432/testdb"
block|,
literal|"org.postgresql.Driver"
block|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"maven.local.repository"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
literal|"postgresql"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"postgresql"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"9.1-901.jdbc4"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"postgresql-9.1-901.jdbc4.jar"
block|,
literal|true
block|}
block|,
block|{
literal|"jdbc:dummy://host:5432/testdb"
block|,
name|dummyDriverClazzName
block|,
name|pathToDummyDriver
block|,
literal|false
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-a"
block|,
literal|"authType"
block|}
decl_stmt|;
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAuthType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"authType"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPasswordFileArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|File
name|passFile
init|=
operator|new
name|File
argument_list|(
literal|"file.password"
argument_list|)
decl_stmt|;
name|passFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|FileOutputStream
name|passFileOut
init|=
operator|new
name|FileOutputStream
argument_list|(
name|passFile
argument_list|)
decl_stmt|;
name|passFileOut
operator|.
name|write
argument_list|(
literal|"mypass\n"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|passFileOut
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-w"
block|,
literal|"file.password"
block|,
literal|"-p"
block|,
literal|"not-taken-if-w-is-present"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-a"
block|,
literal|"authType"
block|}
decl_stmt|;
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|bl
operator|.
name|connectArgs
argument_list|)
expr_stmt|;
comment|// Password file contents are trimmed of trailing whitespaces and newlines
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name mypass driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAuthType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"authType"
argument_list|)
argument_list|)
expr_stmt|;
name|passFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|/**    * The first flag is taken by the parser.    */
annotation|@
name|Test
specifier|public
name|void
name|testDuplicateArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-u"
block|,
literal|"url2"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQueryScripts
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-e"
block|,
literal|"select1"
block|,
literal|"-e"
block|,
literal|"select2"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|queries
operator|.
name|contains
argument_list|(
literal|"select1"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|queries
operator|.
name|contains
argument_list|(
literal|"select2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test setting hive conf and hive vars with --hiveconf and --hivevar    */
annotation|@
name|Test
specifier|public
name|void
name|testHiveConfAndVars
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"--hiveconf"
block|,
literal|"a=avalue"
block|,
literal|"--hiveconf"
block|,
literal|"b=bvalue"
block|,
literal|"--hivevar"
block|,
literal|"c=cvalue"
block|,
literal|"--hivevar"
block|,
literal|"d=dvalue"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveConfVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"a"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"avalue"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveConfVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"bvalue"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"c"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"cvalue"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getHiveVariables
argument_list|()
operator|.
name|get
argument_list|(
literal|"d"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"dvalue"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineOpts
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"--autoCommit=true"
block|,
literal|"--verbose"
block|,
literal|"--truncateTable"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAutoCommit
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getVerbose
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getTruncateTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineAutoCommit
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{}
decl_stmt|;
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAutoCommit
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|=
operator|new
name|String
index|[]
block|{
literal|"--autoCommit=false"
block|}
expr_stmt|;
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAutoCommit
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|=
operator|new
name|String
index|[]
block|{
literal|"--autoCommit=true"
block|}
expr_stmt|;
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getAutoCommit
argument_list|()
argument_list|)
expr_stmt|;
name|bl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineShowDbInPromptOptsDefault
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getShowDbInPrompt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|bl
operator|.
name|getFormattedDb
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineShowDbInPromptOptsTrue
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"--showDbInPrompt=true"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getShowDbInPrompt
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|" (default)"
argument_list|,
name|bl
operator|.
name|getFormattedDb
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test setting script file with -f option.    */
annotation|@
name|Test
specifier|public
name|void
name|testScriptFile
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"-f"
block|,
literal|"myscript"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|connectArgs
operator|.
name|equals
argument_list|(
literal|"url name password driver"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getScriptFile
argument_list|()
operator|.
name|equals
argument_list|(
literal|"myscript"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test beeline with -f and -e simultaneously    */
annotation|@
name|Test
specifier|public
name|void
name|testCommandAndFileSimultaneously
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"myselect"
block|,
literal|"-f"
block|,
literal|"myscript"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Displays the usage.    */
annotation|@
name|Test
specifier|public
name|void
name|testHelp
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"--help"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|isHelpAsked
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Displays the usage.    */
annotation|@
name|Test
specifier|public
name|void
name|testUnmatchedArgs
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddLocalJar
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|bl
operator|.
name|findLocalDriver
argument_list|(
name|connectionString
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Add "
operator|+
name|driverJarFileName
operator|+
literal|" for the driver class "
operator|+
name|driverClazzName
argument_list|)
expr_stmt|;
name|bl
operator|.
name|addLocalJar
argument_list|(
name|driverJarFileName
argument_list|)
expr_stmt|;
name|bl
operator|.
name|addlocaldrivername
argument_list|(
name|driverClazzName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|bl
operator|.
name|findLocalDriver
argument_list|(
name|connectionString
argument_list|)
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|driverClazzName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAddLocalJarWithoutAddDriverClazz
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Add "
operator|+
name|driverJarFileName
operator|+
literal|" for the driver class "
operator|+
name|driverClazzName
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"expected to exists: "
operator|+
name|driverJarFileName
argument_list|,
operator|new
name|File
argument_list|(
name|driverJarFileName
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|bl
operator|.
name|addLocalJar
argument_list|(
name|driverJarFileName
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|defaultSupported
condition|)
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|bl
operator|.
name|findLocalDriver
argument_list|(
name|connectionString
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// no need to add for the default supported local jar driver
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|bl
operator|.
name|findLocalDriver
argument_list|(
name|connectionString
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|bl
operator|.
name|findLocalDriver
argument_list|(
name|connectionString
argument_list|)
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|driverClazzName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelinePasswordMask
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|File
name|errFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|"tmp"
argument_list|)
decl_stmt|;
name|bl
operator|.
name|setErrorStream
argument_list|(
operator|new
name|PrintStream
argument_list|(
operator|new
name|FileOutputStream
argument_list|(
name|errFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
literal|"url"
block|,
literal|"-n"
block|,
literal|"name"
block|,
literal|"-p"
block|,
literal|"password"
block|,
literal|"-d"
block|,
literal|"driver"
block|,
literal|"--autoCommit=true"
block|,
literal|"--verbose"
block|,
literal|"--truncateTable"
block|}
decl_stmt|;
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|bl
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|errContents
init|=
operator|new
name|String
argument_list|(
name|Files
operator|.
name|readAllBytes
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|errFile
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|errContents
operator|.
name|contains
argument_list|(
name|BeeLine
operator|.
name|PASSWD_MASK
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test property file parameter option.    */
annotation|@
name|Test
specifier|public
name|void
name|testPropertyFile
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"--property-file"
block|,
literal|"props"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|properties
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
literal|"props"
argument_list|)
argument_list|)
expr_stmt|;
name|bl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test maxHistoryRows parameter option.    */
annotation|@
name|Test
specifier|public
name|void
name|testMaxHistoryRows
parameter_list|()
throws|throws
name|Exception
block|{
name|TestBeeline
name|bl
init|=
operator|new
name|TestBeeline
argument_list|()
decl_stmt|;
name|String
name|args
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"--maxHistoryRows=100"
block|}
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bl
operator|.
name|initArgs
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|bl
operator|.
name|getOpts
argument_list|()
operator|.
name|getMaxHistoryRows
argument_list|()
operator|==
literal|100
argument_list|)
expr_stmt|;
name|bl
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


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
operator|.
name|exec
operator|.
name|repl
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
name|common
operator|.
name|repl
operator|.
name|ReplScope
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
name|QueryState
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
name|Hive
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|HiveWrapper
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|Utils
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
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|core
operator|.
name|classloader
operator|.
name|annotations
operator|.
name|PowerMockIgnore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|core
operator|.
name|classloader
operator|.
name|annotations
operator|.
name|PrepareForTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|powermock
operator|.
name|modules
operator|.
name|junit4
operator|.
name|PowerMockRunner
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
name|Collections
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|same
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
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|mockStatic
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|verifyStatic
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|when
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|powermock
operator|.
name|api
operator|.
name|mockito
operator|.
name|PowerMockito
operator|.
name|whenNew
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
name|repl
operator|.
name|ReplExternalTables
operator|.
name|Writer
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|PowerMockRunner
operator|.
name|class
argument_list|)
annotation|@
name|PrepareForTest
argument_list|(
block|{
name|Utils
operator|.
name|class
block|,
name|ReplDumpTask
operator|.
name|class
block|}
argument_list|)
annotation|@
name|PowerMockIgnore
argument_list|(
block|{
literal|"javax.management.*"
block|}
argument_list|)
specifier|public
class|class
name|TestReplDumpTask
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestReplDumpTask
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Hive
name|hive
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|HiveConf
name|conf
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|QueryState
name|queryState
decl_stmt|;
class|class
name|StubReplDumpTask
extends|extends
name|ReplDumpTask
block|{
annotation|@
name|Override
specifier|protected
name|Hive
name|getHive
parameter_list|()
block|{
return|return
name|hive
return|;
block|}
annotation|@
name|Override
name|long
name|currentNotificationId
parameter_list|(
name|Hive
name|hiveDb
parameter_list|)
block|{
return|return
name|Long
operator|.
name|MAX_VALUE
return|;
block|}
annotation|@
name|Override
name|String
name|getValidTxnListForReplDump
parameter_list|(
name|Hive
name|hiveDb
parameter_list|,
name|long
name|waitUntilTime
parameter_list|)
block|{
return|return
literal|""
return|;
block|}
annotation|@
name|Override
name|void
name|dumpFunctionMetadata
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Path
name|dumpRoot
parameter_list|,
name|Hive
name|hiveDb
parameter_list|)
block|{     }
annotation|@
name|Override
name|Path
name|dumpDbMetadata
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Path
name|dumpRoot
parameter_list|,
name|long
name|lastReplId
parameter_list|,
name|Hive
name|hiveDb
parameter_list|)
block|{
return|return
name|Mockito
operator|.
name|mock
argument_list|(
name|Path
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
name|void
name|dumpConstraintMetadata
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Path
name|dbRoot
parameter_list|,
name|Hive
name|hiveDb
parameter_list|)
block|{     }
block|}
specifier|private
specifier|static
class|class
name|TestException
extends|extends
name|Exception
block|{   }
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|TestException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|removeDBPropertyToPreventRenameWhenBootstrapDumpOfTableFails
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tableList
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a1"
argument_list|,
literal|"a2"
argument_list|)
decl_stmt|;
name|String
name|dbRandomKey
init|=
literal|"akeytoberandom"
decl_stmt|;
name|ReplScope
name|replScope
init|=
operator|new
name|ReplScope
argument_list|(
literal|"default"
argument_list|)
decl_stmt|;
name|mockStatic
argument_list|(
name|Utils
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|Utils
operator|.
name|matchesDb
argument_list|(
name|same
argument_list|(
name|hive
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"default"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"default"
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|Utils
operator|.
name|getAllTables
argument_list|(
name|same
argument_list|(
name|hive
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"default"
argument_list|)
argument_list|,
name|eq
argument_list|(
name|replScope
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tableList
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|Utils
operator|.
name|setDbBootstrapDumpState
argument_list|(
name|same
argument_list|(
name|hive
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"default"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|dbRandomKey
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|Utils
operator|.
name|matchesTbl
argument_list|(
name|same
argument_list|(
name|hive
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"default"
argument_list|)
argument_list|,
name|eq
argument_list|(
name|replScope
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|tableList
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|hive
operator|.
name|getAllFunctions
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|queryState
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
literal|"hive.repl.last.repl.id"
argument_list|,
operator|-
literal|1L
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_INCLUDE_EXTERNAL_TABLES
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_BOOTSTRAP_DUMP_OPEN_TXN_TIMEOUT
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"1h"
argument_list|)
expr_stmt|;
name|whenNew
argument_list|(
name|Writer
operator|.
name|class
argument_list|)
operator|.
name|withAnyArguments
argument_list|()
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|Writer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|whenNew
argument_list|(
name|HiveWrapper
operator|.
name|class
argument_list|)
operator|.
name|withAnyArguments
argument_list|()
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|HiveWrapper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ReplDumpTask
name|task
init|=
operator|new
name|StubReplDumpTask
argument_list|()
block|{
specifier|private
name|int
name|tableDumpCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
name|void
name|dumpTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|validTxnList
parameter_list|,
name|Path
name|dbRoot
parameter_list|,
name|Path
name|replDataDir
parameter_list|,
name|long
name|lastReplId
parameter_list|,
name|Hive
name|hiveDb
parameter_list|,
name|HiveWrapper
operator|.
name|Tuple
argument_list|<
name|Table
argument_list|>
name|tuple
parameter_list|)
throws|throws
name|Exception
block|{
name|tableDumpCount
operator|++
expr_stmt|;
if|if
condition|(
name|tableDumpCount
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|TestException
argument_list|()
throw|;
block|}
block|}
block|}
decl_stmt|;
name|task
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|task
operator|.
name|setWork
argument_list|(
operator|new
name|ReplDumpWork
argument_list|(
name|replScope
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|task
operator|.
name|bootStrapDump
argument_list|(
name|mock
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|,
literal|null
argument_list|,
name|mock
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|,
name|hive
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|verifyStatic
argument_list|()
expr_stmt|;
name|Utils
operator|.
name|resetDbBootstrapDumpState
argument_list|(
name|same
argument_list|(
name|hive
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"default"
argument_list|)
argument_list|,
name|eq
argument_list|(
name|dbRandomKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


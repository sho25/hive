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
name|hcatalog
operator|.
name|pig
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
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
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|cli
operator|.
name|CliSessionState
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
name|hive
operator|.
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|MetaStoreUtils
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
name|api
operator|.
name|InvalidOperationException
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
name|api
operator|.
name|MetaException
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
name|api
operator|.
name|NoSuchObjectException
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
name|api
operator|.
name|Partition
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
name|api
operator|.
name|UnknownTableException
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
name|CommandNeedRetryException
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
name|Driver
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
name|RCFileInputFormat
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
name|RCFileOutputFormat
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|hcatalog
operator|.
name|cli
operator|.
name|SemanticAnalysis
operator|.
name|HCatSemanticAnalyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|pig
operator|.
name|HCatLoader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ExecType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|Tuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|logicalLayer
operator|.
name|FrontendException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|util
operator|.
name|UDFContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_class
specifier|public
class|class
name|TestPigStorageDriver
extends|extends
name|TestCase
block|{
specifier|private
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
name|Driver
name|hcatDriver
decl_stmt|;
specifier|private
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
specifier|static
name|String
name|tblLocation
init|=
literal|"/tmp/test_pig/data"
decl_stmt|;
specifier|private
specifier|static
name|String
name|anyExistingFileInCurDir
init|=
literal|"ivy.xml"
decl_stmt|;
specifier|private
specifier|static
name|String
name|warehouseDir
init|=
literal|"/tmp/hcat_junit_warehouse"
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|hcatConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
operator|.
name|varname
argument_list|,
name|HCatSemanticAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|hcatDriver
operator|=
operator|new
name|Driver
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hcatConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hcatConf
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testPigStorageDriver
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|String
name|fsLoc
init|=
name|hcatConf
operator|.
name|get
argument_list|(
literal|"fs.default.name"
argument_list|)
decl_stmt|;
name|Path
name|tblPath
init|=
operator|new
name|Path
argument_list|(
name|fsLoc
argument_list|,
name|tblLocation
argument_list|)
decl_stmt|;
name|String
name|tblName
init|=
literal|"junit_pigstorage"
decl_stmt|;
name|tblPath
operator|.
name|getFileSystem
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|copyFromLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|anyExistingFileInCurDir
argument_list|)
argument_list|,
name|tblPath
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tblName
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|resp
decl_stmt|;
name|String
name|createTable
init|=
literal|"create table "
operator|+
name|tblName
operator|+
literal|" (a string) partitioned by (b string) stored as TEXTFILE"
decl_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" add partition (b='2010-10-10') location '"
operator|+
operator|new
name|Path
argument_list|(
name|fsLoc
argument_list|,
literal|"/tmp/test_pig"
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" partition (b='2010-10-10') set fileformat TEXTFILE"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"desc extended "
operator|+
name|tblName
operator|+
literal|" partition (b='2010-10-10')"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|,
name|hcatConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
decl_stmt|;
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
operator|.
name|setClientSystemProps
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|" a = load '"
operator|+
name|tblName
operator|+
literal|"' using "
operator|+
name|HCatLoader
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|";"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|boolean
name|result
init|=
name|compareWithFile
argument_list|(
name|itr
argument_list|,
name|anyExistingFileInCurDir
argument_list|,
literal|2
argument_list|,
literal|"2010-10-10"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|tblPath
operator|.
name|toString
argument_list|()
operator|+
literal|"' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|store
argument_list|(
literal|"a"
argument_list|,
name|tblName
argument_list|,
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('b=2010-10-11')"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|warehouseDir
operator|+
literal|"/"
operator|+
name|tblName
operator|+
literal|"/b=2010-10-11' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|itr
operator|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|result
operator|=
name|compareWithFile
argument_list|(
name|itr
argument_list|,
name|anyExistingFileInCurDir
argument_list|,
literal|1
argument_list|,
literal|"2010-10-11"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
argument_list|)
expr_stmt|;
comment|// Test multi-store
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|tblPath
operator|.
name|toString
argument_list|()
operator|+
literal|"' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store a into '"
operator|+
name|tblName
operator|+
literal|"' using "
operator|+
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('b=2010-11-01');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store a into '"
operator|+
name|tblName
operator|+
literal|"' using "
operator|+
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('b=2010-11-02');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|warehouseDir
operator|+
literal|"/"
operator|+
name|tblName
operator|+
literal|"/b=2010-11-01' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|itr
operator|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|result
operator|=
name|compareWithFile
argument_list|(
name|itr
argument_list|,
name|anyExistingFileInCurDir
argument_list|,
literal|1
argument_list|,
literal|"2010-11-01"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|warehouseDir
operator|+
literal|"/"
operator|+
name|tblName
operator|+
literal|"/b=2010-11-02' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|itr
operator|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|result
operator|=
name|compareWithFile
argument_list|(
name|itr
argument_list|,
name|anyExistingFileInCurDir
argument_list|,
literal|1
argument_list|,
literal|"2010-11-02"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tblName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|compareWithFile
parameter_list|(
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
parameter_list|,
name|String
name|factFile
parameter_list|,
name|int
name|numColumn
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|valueSuffix
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputStream
name|stream
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|BufferedInputStream
argument_list|(
operator|new
name|FileInputStream
argument_list|(
operator|new
name|File
argument_list|(
name|factFile
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple
name|t
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|numColumn
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// If underlying data-field is empty. PigStorage inserts null instead
comment|// of empty String objects.
name|assertTrue
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|String
argument_list|)
expr_stmt|;
name|String
name|expected
init|=
name|stream
operator|.
name|readLine
argument_list|()
decl_stmt|;
if|if
condition|(
name|valueSuffix
operator|!=
literal|null
condition|)
name|expected
operator|+=
name|valueSuffix
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|stream
operator|.
name|readLine
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numColumn
operator|>
literal|1
condition|)
block|{
comment|// The second column must be key
name|assertTrue
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|String
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|stream
operator|.
name|available
argument_list|()
argument_list|)
expr_stmt|;
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|void
name|testDelim
parameter_list|()
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownTableException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidOperationException
throws|,
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_pigstorage_delim"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|resp
decl_stmt|;
name|String
name|createTable
init|=
literal|"create table junit_pigstorage_delim (a0 string, a1 string) partitioned by (b string) stored as RCFILE"
decl_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table junit_pigstorage_delim add partition (b='2010-10-10')"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table junit_pigstorage_delim partition (b='2010-10-10') set fileformat TEXTFILE"
argument_list|)
expr_stmt|;
name|Partition
name|part
init|=
name|msc
operator|.
name|getPartition
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"junit_pigstorage_delim"
argument_list|,
literal|"b=2010-10-10"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partParms
init|=
name|part
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|partParms
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_LOADER_ARGS
argument_list|,
literal|"control-A"
argument_list|)
expr_stmt|;
name|partParms
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_STORER_ARGS
argument_list|,
literal|"control-A"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|alter_partition
argument_list|(
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
argument_list|,
literal|"junit_pigstorage_delim"
argument_list|,
name|part
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|,
name|hcatConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
decl_stmt|;
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
operator|.
name|setClientSystemProps
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|" a = load 'junit_pigstorage_delim' using "
operator|+
name|HCatLoader
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|";"
argument_list|)
expr_stmt|;
try|try
block|{
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FrontendException
name|fe
parameter_list|)
block|{}
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table junit_pigstorage_delim set fileformat TEXTFILE"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table junit_pigstorage_delim set TBLPROPERTIES ('hcat.pig.loader.args'=':', 'hcat.pig.storer.args'=':')"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|inputFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hcat_test"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|PrintWriter
name|p
init|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|FileWriter
argument_list|(
name|inputFile
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|println
argument_list|(
literal|"1\t2"
argument_list|)
expr_stmt|;
name|p
operator|.
name|println
argument_list|(
literal|"3\t4"
argument_list|)
expr_stmt|;
name|p
operator|.
name|close
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|inputFile
operator|.
name|toString
argument_list|()
operator|+
literal|"' using PigStorage() as (a0:chararray, a1:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|store
argument_list|(
literal|"a"
argument_list|,
literal|"junit_pigstorage_delim"
argument_list|,
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('b=2010-10-11')"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '/tmp/hcat_junit_warehouse/junit_pigstorage_delim/b=2010-10-11' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Tuple
name|t
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
literal|"1:2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|=
name|itr
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
literal|"3:4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|inputFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testMultiConstructArgs
parameter_list|()
throws|throws
name|MetaException
throws|,
name|TException
throws|,
name|UnknownTableException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidOperationException
throws|,
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|String
name|fsLoc
init|=
name|hcatConf
operator|.
name|get
argument_list|(
literal|"fs.default.name"
argument_list|)
decl_stmt|;
name|Path
name|tblPath
init|=
operator|new
name|Path
argument_list|(
name|fsLoc
argument_list|,
name|tblLocation
argument_list|)
decl_stmt|;
name|String
name|tblName
init|=
literal|"junit_pigstorage_constructs"
decl_stmt|;
name|tblPath
operator|.
name|getFileSystem
argument_list|(
name|hcatConf
argument_list|)
operator|.
name|copyFromLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|anyExistingFileInCurDir
argument_list|)
argument_list|,
name|tblPath
argument_list|)
expr_stmt|;
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"drop table junit_pigstorage_constructs"
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|resp
decl_stmt|;
name|String
name|createTable
init|=
literal|"create table "
operator|+
name|tblName
operator|+
literal|" (a string) partitioned by (b string) stored as TEXTFILE"
decl_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|resp
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
literal|"alter table "
operator|+
name|tblName
operator|+
literal|" set TBLPROPERTIES ('hcat.pig.storer'='org.apache.hcatalog.pig.MyPigStorage', 'hcat.pig.storer.args'=':#hello', 'hcat.pig.args.delimiter'='#')"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|resp
operator|.
name|getErrorMessage
argument_list|()
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|,
name|hcatConf
operator|.
name|getAllProperties
argument_list|()
argument_list|)
decl_stmt|;
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
operator|.
name|setClientSystemProps
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|tblPath
operator|.
name|toString
argument_list|()
operator|+
literal|"' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|store
argument_list|(
literal|"a"
argument_list|,
name|tblName
argument_list|,
name|HCatStorer
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('b=2010-10-11')"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"a = load '"
operator|+
name|warehouseDir
operator|+
literal|"/"
operator|+
name|tblName
operator|+
literal|"/b=2010-10-11' using PigStorage() as (a:chararray);"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|boolean
name|result
init|=
name|compareWithFile
argument_list|(
name|itr
argument_list|,
name|anyExistingFileInCurDir
argument_list|,
literal|1
argument_list|,
literal|"2010-10-11"
argument_list|,
literal|":hello"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


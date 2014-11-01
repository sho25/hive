begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
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
name|HashSet
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
name|IOConstants
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
name|StorageFormats
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
name|backend
operator|.
name|executionengine
operator|.
name|ExecException
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
name|backend
operator|.
name|executionengine
operator|.
name|ExecJob
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
name|BagFactory
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
name|DataBag
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
name|data
operator|.
name|TupleFactory
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
name|logicalLayer
operator|.
name|schema
operator|.
name|Schema
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
name|schema
operator|.
name|Schema
operator|.
name|FieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assume
operator|.
name|assumeTrue
import|;
end_import

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
name|TestHCatLoaderComplexSchema
block|{
comment|//private static MiniCluster cluster = MiniCluster.buildCluster();
specifier|private
specifier|static
name|Driver
name|driver
decl_stmt|;
comment|//private static Properties props;
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
name|TestHCatLoaderComplexSchema
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|DISABLED_STORAGE_FORMATS
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|IOConstants
operator|.
name|AVRO
argument_list|,
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
block|{
name|add
argument_list|(
literal|"testMapNullKey"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|IOConstants
operator|.
name|PARQUETFILE
argument_list|,
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
block|{
name|add
argument_list|(
literal|"testSyntheticComplexSchema"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testTupleInBagInTupleInBag"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testMapWithComplexData"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testMapNullKey"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
name|String
name|storageFormat
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|generateParameters
parameter_list|()
block|{
return|return
name|StorageFormats
operator|.
name|names
argument_list|()
return|;
block|}
specifier|public
name|TestHCatLoaderComplexSchema
parameter_list|(
name|String
name|storageFormat
parameter_list|)
block|{
name|this
operator|.
name|storageFormat
operator|=
name|storageFormat
expr_stmt|;
block|}
specifier|private
name|void
name|dropTable
parameter_list|(
name|String
name|tablename
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tablename
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|partitionedBy
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|String
name|createTable
decl_stmt|;
name|createTable
operator|=
literal|"create table "
operator|+
name|tablename
operator|+
literal|"("
operator|+
name|schema
operator|+
literal|") "
expr_stmt|;
if|if
condition|(
operator|(
name|partitionedBy
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|partitionedBy
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|createTable
operator|=
name|createTable
operator|+
literal|"partitioned by ("
operator|+
name|partitionedBy
operator|+
literal|") "
expr_stmt|;
block|}
name|createTable
operator|=
name|createTable
operator|+
literal|"stored as "
operator|+
name|storageFormat
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table:\n {}"
argument_list|,
name|createTable
argument_list|)
expr_stmt|;
name|CommandProcessorResponse
name|result
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
decl_stmt|;
name|int
name|retCode
init|=
name|result
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|retCode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create table. ["
operator|+
name|createTable
operator|+
literal|"], return code from hive driver : ["
operator|+
name|retCode
operator|+
literal|" "
operator|+
name|result
operator|.
name|getErrorMessage
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|schema
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|createTable
argument_list|(
name|tablename
argument_list|,
name|schema
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|TestHCatLoaderComplexSchema
operator|.
name|class
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
comment|//props = new Properties();
comment|//props.setProperty("fs.default.name", cluster.getProperties().getProperty("fs.default.name"));
block|}
specifier|private
specifier|static
specifier|final
name|TupleFactory
name|tf
init|=
name|TupleFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|BagFactory
name|bf
init|=
name|BagFactory
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
name|Tuple
name|t
parameter_list|(
name|Object
modifier|...
name|objects
parameter_list|)
block|{
return|return
name|tf
operator|.
name|newTuple
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|objects
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|DataBag
name|b
parameter_list|(
name|Tuple
modifier|...
name|objects
parameter_list|)
block|{
return|return
name|bf
operator|.
name|newDefaultBag
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|objects
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * artificially complex nested schema to test nested schema conversion    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSyntheticComplexSchema
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|pigSchema
init|=
literal|"a: "
operator|+
literal|"("
operator|+
literal|"aa: chararray, "
operator|+
literal|"ab: long, "
operator|+
literal|"ac: map[], "
operator|+
literal|"ad: { t: (ada: long) }, "
operator|+
literal|"ae: { t: (aea:long, aeb: ( aeba: chararray, aebb: long)) },"
operator|+
literal|"af: (afa: chararray, afb: long) "
operator|+
literal|"),"
operator|+
literal|"b: chararray, "
operator|+
literal|"c: long, "
operator|+
literal|"d:  { t: (da:long, db: ( dba: chararray, dbb: long), dc: { t: (dca: long) } ) } "
decl_stmt|;
comment|// with extra structs
name|String
name|tableSchema
init|=
literal|"a struct<"
operator|+
literal|"aa: string, "
operator|+
literal|"ab: bigint, "
operator|+
literal|"ac: map<string, string>, "
operator|+
literal|"ad: array<struct<ada:bigint>>, "
operator|+
literal|"ae: array<struct<aea:bigint, aeb: struct<aeba: string, aebb: bigint>>>,"
operator|+
literal|"af: struct<afa: string, afb: bigint> "
operator|+
literal|">, "
operator|+
literal|"b string, "
operator|+
literal|"c bigint, "
operator|+
literal|"d array<struct<da: bigint, db: struct<dba:string, dbb:bigint>, dc: array<struct<dca: bigint>>>>"
decl_stmt|;
comment|// without extra structs
name|String
name|tableSchema2
init|=
literal|"a struct<"
operator|+
literal|"aa: string, "
operator|+
literal|"ab: bigint, "
operator|+
literal|"ac: map<string, string>, "
operator|+
literal|"ad: array<bigint>, "
operator|+
literal|"ae: array<struct<aea:bigint, aeb: struct<aeba: string, aebb: bigint>>>,"
operator|+
literal|"af: struct<afa: string, afb: bigint> "
operator|+
literal|">, "
operator|+
literal|"b string, "
operator|+
literal|"c bigint, "
operator|+
literal|"d array<struct<da: bigint, db: struct<dba:string, dbb:bigint>, dc: array<bigint>>>"
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Tuple
name|t
init|=
name|t
argument_list|(
name|t
argument_list|(
literal|"aa test"
argument_list|,
literal|2l
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"ac test1"
argument_list|,
literal|"test 1"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"ac test2"
argument_list|,
literal|"test 2"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|b
argument_list|(
name|t
argument_list|(
literal|3l
argument_list|)
argument_list|,
name|t
argument_list|(
literal|4l
argument_list|)
argument_list|)
argument_list|,
name|b
argument_list|(
name|t
argument_list|(
literal|5l
argument_list|,
name|t
argument_list|(
literal|"aeba test"
argument_list|,
literal|6l
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|t
argument_list|(
literal|"afa test"
argument_list|,
literal|7l
argument_list|)
argument_list|)
argument_list|,
literal|"b test"
argument_list|,
operator|(
name|long
operator|)
name|i
argument_list|,
name|b
argument_list|(
name|t
argument_list|(
literal|8l
argument_list|,
name|t
argument_list|(
literal|"dba test"
argument_list|,
literal|9l
argument_list|)
argument_list|,
name|b
argument_list|(
name|t
argument_list|(
literal|10l
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|verifyWriteRead
argument_list|(
literal|"testSyntheticComplexSchema"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"testSyntheticComplexSchema"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"testSyntheticComplexSchema2"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema2
argument_list|,
name|data
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"testSyntheticComplexSchema2"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema2
argument_list|,
name|data
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyWriteRead
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|pigSchema
parameter_list|,
name|String
name|tableSchema
parameter_list|,
name|List
argument_list|<
name|Tuple
argument_list|>
name|data
parameter_list|,
name|boolean
name|provideSchemaToStorer
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
throws|,
name|ExecException
throws|,
name|FrontendException
block|{
name|verifyWriteRead
argument_list|(
name|tablename
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
name|data
argument_list|,
name|provideSchemaToStorer
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyWriteRead
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|pigSchema
parameter_list|,
name|String
name|tableSchema
parameter_list|,
name|List
argument_list|<
name|Tuple
argument_list|>
name|data
parameter_list|,
name|List
argument_list|<
name|Tuple
argument_list|>
name|result
parameter_list|,
name|boolean
name|provideSchemaToStorer
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
throws|,
name|ExecException
throws|,
name|FrontendException
block|{
name|MockLoader
operator|.
name|setData
argument_list|(
name|tablename
operator|+
literal|"Input"
argument_list|,
name|data
argument_list|)
expr_stmt|;
try|try
block|{
name|createTable
argument_list|(
name|tablename
argument_list|,
name|tableSchema
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
argument_list|)
decl_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|tablename
operator|+
literal|"Input' using org.apache.hive.hcatalog.pig.MockLoader() AS ("
operator|+
name|pigSchema
operator|+
literal|");"
argument_list|)
expr_stmt|;
name|Schema
name|dumpedASchema
init|=
name|server
operator|.
name|dumpSchema
argument_list|(
literal|"A"
argument_list|)
decl_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"STORE A into '"
operator|+
name|tablename
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer("
operator|+
operator|(
name|provideSchemaToStorer
condition|?
literal|"'', '"
operator|+
name|pigSchema
operator|+
literal|"'"
else|:
literal|""
operator|)
operator|+
literal|");"
argument_list|)
expr_stmt|;
name|ExecJob
name|execJob
init|=
name|server
operator|.
name|executeBatch
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|execJob
operator|.
name|getStatistics
argument_list|()
operator|.
name|isSuccessful
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Import failed"
argument_list|,
name|execJob
operator|.
name|getException
argument_list|()
argument_list|)
throw|;
block|}
comment|// test that schema was loaded correctly
name|server
operator|.
name|registerQuery
argument_list|(
literal|"X = load '"
operator|+
name|tablename
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|dumpSchema
argument_list|(
literal|"X"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|it
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"X"
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple
name|input
init|=
name|result
operator|.
name|get
argument_list|(
name|i
operator|++
argument_list|)
decl_stmt|;
name|Tuple
name|output
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|compareTuples
argument_list|(
name|input
argument_list|,
name|output
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"tuple : {} "
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
name|Schema
name|dumpedXSchema
init|=
name|server
operator|.
name|dumpSchema
argument_list|(
literal|"X"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"expected "
operator|+
name|dumpedASchema
operator|+
literal|" but was "
operator|+
name|dumpedXSchema
operator|+
literal|" (ignoring field names)"
argument_list|,
literal|""
argument_list|,
name|compareIgnoreFiledNames
argument_list|(
name|dumpedASchema
argument_list|,
name|dumpedXSchema
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|dropTable
argument_list|(
name|tablename
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|compareTuples
parameter_list|(
name|Tuple
name|t1
parameter_list|,
name|Tuple
name|t2
parameter_list|)
throws|throws
name|ExecException
block|{
name|assertEquals
argument_list|(
literal|"Tuple Sizes don't match"
argument_list|,
name|t1
operator|.
name|size
argument_list|()
argument_list|,
name|t2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|t1
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|f1
init|=
name|t1
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|f2
init|=
name|t2
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"left"
argument_list|,
name|f1
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"right"
argument_list|,
name|f2
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|"right: "
operator|+
name|f1
operator|+
literal|", left: "
operator|+
name|f2
decl_stmt|;
name|assertEquals
argument_list|(
name|msg
argument_list|,
name|noOrder
argument_list|(
name|f1
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|noOrder
argument_list|(
name|f2
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|noOrder
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|char
index|[]
name|chars
init|=
name|s
operator|.
name|toCharArray
argument_list|()
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|chars
argument_list|)
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|chars
argument_list|)
return|;
block|}
specifier|private
name|String
name|compareIgnoreFiledNames
parameter_list|(
name|Schema
name|expected
parameter_list|,
name|Schema
name|got
parameter_list|)
throws|throws
name|FrontendException
block|{
if|if
condition|(
name|expected
operator|==
literal|null
operator|||
name|got
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|expected
operator|==
name|got
condition|)
block|{
return|return
literal|""
return|;
block|}
else|else
block|{
return|return
literal|"\nexpected "
operator|+
name|expected
operator|+
literal|" got "
operator|+
name|got
return|;
block|}
block|}
if|if
condition|(
name|expected
operator|.
name|size
argument_list|()
operator|!=
name|got
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|"\nsize expected "
operator|+
name|expected
operator|.
name|size
argument_list|()
operator|+
literal|" ("
operator|+
name|expected
operator|+
literal|") got "
operator|+
name|got
operator|.
name|size
argument_list|()
operator|+
literal|" ("
operator|+
name|got
operator|+
literal|")"
return|;
block|}
name|String
name|message
init|=
literal|""
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|FieldSchema
name|expectedField
init|=
name|expected
operator|.
name|getField
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|FieldSchema
name|gotField
init|=
name|got
operator|.
name|getField
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedField
operator|.
name|type
operator|!=
name|gotField
operator|.
name|type
condition|)
block|{
name|message
operator|+=
literal|"\ntype expected "
operator|+
name|expectedField
operator|.
name|type
operator|+
literal|" ("
operator|+
name|expectedField
operator|+
literal|") got "
operator|+
name|gotField
operator|.
name|type
operator|+
literal|" ("
operator|+
name|gotField
operator|+
literal|")"
expr_stmt|;
block|}
else|else
block|{
name|message
operator|+=
name|compareIgnoreFiledNames
argument_list|(
name|expectedField
operator|.
name|schema
argument_list|,
name|gotField
operator|.
name|schema
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|message
return|;
block|}
comment|/**    * tests that unnecessary tuples are drop while converting schema    * (Pig requires Tuples in Bags)    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testTupleInBagInTupleInBag
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|pigSchema
init|=
literal|"a: { b : ( c: { d: (i : long) } ) }"
decl_stmt|;
name|String
name|tableSchema
init|=
literal|"a array< array< bigint>>"
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|>
argument_list|()
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|100l
argument_list|)
argument_list|,
name|t
argument_list|(
literal|101l
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|110l
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|200l
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|210l
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|220l
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|300l
argument_list|)
argument_list|,
name|t
argument_list|(
literal|301l
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|400l
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|t
argument_list|(
name|b
argument_list|(
name|t
argument_list|(
literal|410l
argument_list|)
argument_list|,
name|t
argument_list|(
literal|411l
argument_list|)
argument_list|,
name|t
argument_list|(
literal|412l
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"TupleInBagInTupleInBag1"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"TupleInBagInTupleInBag2"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// test that we don't drop the unnecessary tuple if the table has the corresponding Struct
name|String
name|tableSchema2
init|=
literal|"a array< struct< c: array< struct< i: bigint>>>>"
decl_stmt|;
name|verifyWriteRead
argument_list|(
literal|"TupleInBagInTupleInBag3"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema2
argument_list|,
name|data
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"TupleInBagInTupleInBag4"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema2
argument_list|,
name|data
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapWithComplexData
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|pigSchema
init|=
literal|"a: long, b: map[]"
decl_stmt|;
name|String
name|tableSchema
init|=
literal|"a bigint, b map<string, struct<aa:bigint, ab:string>>"
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|Tuple
name|t
init|=
name|t
argument_list|(
operator|(
name|long
operator|)
name|i
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"b test 1"
argument_list|,
name|t
argument_list|(
literal|1l
argument_list|,
literal|"test 1"
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"b test 2"
argument_list|,
name|t
argument_list|(
literal|2l
argument_list|,
literal|"test 2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
name|verifyWriteRead
argument_list|(
literal|"testMapWithComplexData"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"testMapWithComplexData2"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * artificially complex nested schema to test nested schema conversion    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMapNullKey
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|pigSchema
init|=
literal|"m:map[]"
decl_stmt|;
name|String
name|tableSchema
init|=
literal|"m map<string, string>"
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|>
argument_list|()
decl_stmt|;
name|Tuple
name|t
init|=
name|t
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"ac test1"
argument_list|,
literal|"test 1"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"ac test2"
argument_list|,
literal|"test 2"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|null
argument_list|,
literal|"test 3"
argument_list|)
expr_stmt|;
block|}
empty_stmt|;
block|}
argument_list|)
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|>
argument_list|()
decl_stmt|;
name|t
operator|=
name|t
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
literal|"ac test1"
argument_list|,
literal|"test 1"
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|"ac test2"
argument_list|,
literal|"test 2"
argument_list|)
expr_stmt|;
block|}
empty_stmt|;
block|}
argument_list|)
expr_stmt|;
name|result
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"testSyntheticComplexSchema"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
name|result
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|verifyWriteRead
argument_list|(
literal|"testSyntheticComplexSchema"
argument_list|,
name|pigSchema
argument_list|,
name|tableSchema
argument_list|,
name|data
argument_list|,
name|result
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


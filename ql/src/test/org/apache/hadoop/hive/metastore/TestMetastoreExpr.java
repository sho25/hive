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
name|metastore
package|;
end_package

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
name|Stack
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
name|metastore
operator|.
name|api
operator|.
name|AlreadyExistsException
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
name|Database
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
name|FieldSchema
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
name|InvalidObjectException
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
name|Order
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
name|SerDeInfo
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
name|StorageDescriptor
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
name|exec
operator|.
name|FunctionRegistry
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
name|exec
operator|.
name|Utilities
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
name|HiveInputFormat
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
name|HiveOutputFormat
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
name|ExprNodeColumnDesc
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
name|serde
operator|.
name|serdeConstants
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
name|lazy
operator|.
name|LazySimpleSerDe
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
name|TypeInfo
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
name|util
operator|.
name|StringUtils
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
name|Lists
import|;
end_import

begin_comment
comment|/**  * Tests hive metastore expression support. This should be moved in metastore module  * as soon as we are able to use ql from metastore server (requires splitting metastore  * server and client).  * This is a "companion" test to test to TestHiveMetaStore#testPartitionFilter; thus,  * it doesn't test all the edge cases of the filter (if classes were merged, perhaps the  * filter test could be rolled into it); assumption is that they use the same path in SQL/JDO.  */
end_comment

begin_class
specifier|public
class|class
name|TestMetastoreExpr
extends|extends
name|TestCase
block|{
specifier|protected
specifier|static
name|HiveMetaStoreClient
name|client
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to close metastore"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
try|try
block|{
name|client
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Unable to open the metastore"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|silentDropDatabase
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|TException
block|{
try|try
block|{
for|for
control|(
name|String
name|tableName
range|:
name|client
operator|.
name|getTables
argument_list|(
name|dbName
argument_list|,
literal|"*"
argument_list|)
control|)
block|{
name|client
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{     }
catch|catch
parameter_list|(
name|InvalidOperationException
name|e
parameter_list|)
block|{     }
block|}
specifier|public
name|void
name|testPartitionExpr
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|dbName
init|=
literal|"filterdb"
decl_stmt|;
name|String
name|tblName
init|=
literal|"filtertbl"
decl_stmt|;
name|silentDropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|db
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c1"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|cols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"c2"
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"p1"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"p2"
argument_list|,
name|serdeConstants
operator|.
name|INT_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|Table
name|tbl
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|tbl
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
expr_stmt|;
name|addSd
argument_list|(
name|cols
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setPartitionKeys
argument_list|(
name|partCols
argument_list|)
expr_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|tbl
operator|=
name|client
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
expr_stmt|;
name|addPartition
argument_list|(
name|client
argument_list|,
name|tbl
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"p11"
argument_list|,
literal|"32"
argument_list|)
argument_list|,
literal|"part1"
argument_list|)
expr_stmt|;
name|addPartition
argument_list|(
name|client
argument_list|,
name|tbl
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"p12"
argument_list|,
literal|"32"
argument_list|)
argument_list|,
literal|"part2"
argument_list|)
expr_stmt|;
name|addPartition
argument_list|(
name|client
argument_list|,
name|tbl
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"p13"
argument_list|,
literal|"31"
argument_list|)
argument_list|,
literal|"part3"
argument_list|)
expr_stmt|;
name|addPartition
argument_list|(
name|client
argument_list|,
name|tbl
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"p14"
argument_list|,
literal|"-33"
argument_list|)
argument_list|,
literal|"part4"
argument_list|)
expr_stmt|;
name|ExprBuilder
name|e
init|=
operator|new
name|ExprBuilder
argument_list|(
name|tblName
argument_list|)
decl_stmt|;
name|checkExpr
argument_list|(
literal|3
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|0
argument_list|)
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|3
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|val
argument_list|(
literal|0
argument_list|)
operator|.
name|pred
argument_list|(
literal|"<"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|1
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|val
argument_list|(
literal|0
argument_list|)
operator|.
name|pred
argument_list|(
literal|">"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|2
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|31
argument_list|)
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"<="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|3
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|"p11"
argument_list|)
operator|.
name|strCol
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|1
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|"p11"
argument_list|)
operator|.
name|strCol
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">"
argument_list|,
literal|2
argument_list|)
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|val
argument_list|(
literal|31
argument_list|)
operator|.
name|pred
argument_list|(
literal|"<"
argument_list|,
literal|2
argument_list|)
operator|.
name|pred
argument_list|(
literal|"and"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|3
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|32
argument_list|)
operator|.
name|val
argument_list|(
literal|31
argument_list|)
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|val
argument_list|(
literal|false
argument_list|)
operator|.
name|pred
argument_list|(
literal|"between"
argument_list|,
literal|4
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Apply isnull and instr (not supported by pushdown) via name filtering.
name|checkExpr
argument_list|(
literal|4
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|"p"
argument_list|)
operator|.
name|strCol
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|fn
argument_list|(
literal|"instr"
argument_list|,
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
literal|2
argument_list|)
operator|.
name|val
argument_list|(
literal|0
argument_list|)
operator|.
name|pred
argument_list|(
literal|"<="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|0
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|intCol
argument_list|(
literal|"p2"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"isnull"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Cannot deserialize => throw the specific exception.
try|try
block|{
name|client
operator|.
name|listPartitionsByExpr
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown IncompatibleMetastoreException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IMetaStoreClient
operator|.
name|IncompatibleMetastoreException
name|ex
parameter_list|)
block|{     }
comment|// Invalid expression => throw some exception, but not incompatible metastore.
try|try
block|{
name|checkExpr
argument_list|(
operator|-
literal|1
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|e
operator|.
name|val
argument_list|(
literal|31
argument_list|)
operator|.
name|intCol
argument_list|(
literal|"p3"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should have thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IMetaStoreClient
operator|.
name|IncompatibleMetastoreException
name|ex
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Should not have thrown IncompatibleMetastoreException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{     }
block|}
specifier|public
name|void
name|checkExpr
parameter_list|(
name|int
name|numParts
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|ExprNodeGenericFuncDesc
name|expr
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|client
operator|.
name|listPartitionsByExpr
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|Utilities
operator|.
name|serializeExpressionToKryo
argument_list|(
name|expr
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
name|parts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Partition check failed: "
operator|+
name|expr
operator|.
name|getExprString
argument_list|()
argument_list|,
name|numParts
argument_list|,
name|parts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|ExprBuilder
block|{
specifier|private
specifier|final
name|String
name|tblName
decl_stmt|;
specifier|private
specifier|final
name|Stack
argument_list|<
name|ExprNodeDesc
argument_list|>
name|stack
init|=
operator|new
name|Stack
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|ExprBuilder
parameter_list|(
name|String
name|tblName
parameter_list|)
block|{
name|this
operator|.
name|tblName
operator|=
name|tblName
expr_stmt|;
block|}
specifier|public
name|ExprNodeGenericFuncDesc
name|build
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|stack
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Bad test: "
operator|+
name|stack
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|stack
operator|.
name|pop
argument_list|()
return|;
block|}
specifier|public
name|ExprBuilder
name|pred
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|args
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|fn
argument_list|(
name|name
argument_list|,
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|private
name|ExprBuilder
name|fn
parameter_list|(
name|String
name|name
parameter_list|,
name|TypeInfo
name|ti
parameter_list|,
name|int
name|args
parameter_list|)
throws|throws
name|Exception
block|{
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
name|args
condition|;
operator|++
name|i
control|)
block|{
name|children
operator|.
name|add
argument_list|(
name|stack
operator|.
name|pop
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|stack
operator|.
name|push
argument_list|(
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|name
argument_list|)
operator|.
name|getGenericUDF
argument_list|()
argument_list|,
name|children
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ExprBuilder
name|strCol
parameter_list|(
name|String
name|col
parameter_list|)
block|{
return|return
name|colInternal
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|col
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|ExprBuilder
name|intCol
parameter_list|(
name|String
name|col
parameter_list|)
block|{
return|return
name|colInternal
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
name|col
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|private
name|ExprBuilder
name|colInternal
parameter_list|(
name|TypeInfo
name|ti
parameter_list|,
name|String
name|col
parameter_list|,
name|boolean
name|part
parameter_list|)
block|{
name|stack
operator|.
name|push
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|ti
argument_list|,
name|col
argument_list|,
name|tblName
argument_list|,
name|part
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ExprBuilder
name|val
parameter_list|(
name|String
name|val
parameter_list|)
block|{
return|return
name|valInternal
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|val
argument_list|)
return|;
block|}
specifier|public
name|ExprBuilder
name|val
parameter_list|(
name|int
name|val
parameter_list|)
block|{
return|return
name|valInternal
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
name|val
argument_list|)
return|;
block|}
specifier|public
name|ExprBuilder
name|val
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
return|return
name|valInternal
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|val
argument_list|)
return|;
block|}
specifier|private
name|ExprBuilder
name|valInternal
parameter_list|(
name|TypeInfo
name|ti
parameter_list|,
name|Object
name|val
parameter_list|)
block|{
name|stack
operator|.
name|push
argument_list|(
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|ti
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
specifier|private
name|void
name|addSd
parameter_list|(
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|Table
name|tbl
parameter_list|)
block|{
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
name|sd
operator|.
name|setCols
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setCompressed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setNumBuckets
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setBucketCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSerdeInfo
argument_list|(
operator|new
name|SerDeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setName
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setSortCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setInputFormat
argument_list|(
name|HiveInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputFormat
argument_list|(
name|HiveOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addPartition
parameter_list|(
name|HiveMetaStoreClient
name|client
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|vals
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|AlreadyExistsException
throws|,
name|MetaException
throws|,
name|TException
block|{
name|Partition
name|part
init|=
operator|new
name|Partition
argument_list|()
decl_stmt|;
name|part
operator|.
name|setDbName
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|.
name|setTableName
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|.
name|setValues
argument_list|(
name|vals
argument_list|)
expr_stmt|;
name|part
operator|.
name|setParameters
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|.
name|setSd
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|setSerdeInfo
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|+
name|location
argument_list|)
expr_stmt|;
name|client
operator|.
name|add_partition
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


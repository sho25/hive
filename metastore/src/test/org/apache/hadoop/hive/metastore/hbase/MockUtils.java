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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|hbase
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
name|hbase
operator|.
name|Cell
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
name|hbase
operator|.
name|client
operator|.
name|Delete
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
name|hbase
operator|.
name|client
operator|.
name|Get
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
name|hbase
operator|.
name|client
operator|.
name|HTableInterface
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
name|hbase
operator|.
name|client
operator|.
name|Put
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
name|hbase
operator|.
name|client
operator|.
name|Result
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
name|hbase
operator|.
name|client
operator|.
name|ResultScanner
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
name|hbase
operator|.
name|client
operator|.
name|Scan
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
name|PartitionExpressionProxy
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
name|ql
operator|.
name|io
operator|.
name|sarg
operator|.
name|SearchArgument
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
name|PrimitiveTypeInfo
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|nio
operator|.
name|ByteBuffer
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
name|SortedMap
import|;
end_import

begin_comment
comment|/**  * Mock utilities for HBaseStore testing  */
end_comment

begin_class
specifier|public
class|class
name|MockUtils
block|{
comment|/**    * The default impl is in ql package and is not available in unit tests.    */
specifier|public
specifier|static
class|class
name|NOOPProxy
implements|implements
name|PartitionExpressionProxy
block|{
annotation|@
name|Override
specifier|public
name|String
name|convertExprToFilter
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterPartitionsByExpr
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|partColumnNames
parameter_list|,
name|List
argument_list|<
name|PrimitiveTypeInfo
argument_list|>
name|partColumnTypeInfos
parameter_list|,
name|byte
index|[]
name|expr
parameter_list|,
name|String
name|defaultPartitionName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionNames
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|SearchArgument
name|createSarg
parameter_list|(
name|byte
index|[]
name|expr
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|applySargToFileMetadata
parameter_list|(
name|SearchArgument
name|sarg
parameter_list|,
name|ByteBuffer
name|byteBuffer
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|static
name|HBaseStore
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HTableInterface
name|htable
parameter_list|,
specifier|final
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Cell
argument_list|>
name|rows
parameter_list|)
throws|throws
name|IOException
block|{
operator|(
operator|(
name|HiveConf
operator|)
name|conf
operator|)
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_EXPRESSION_PROXY_CLASS
argument_list|,
name|NOOPProxy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|htable
operator|.
name|get
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|Get
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Result
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Result
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Get
name|get
init|=
operator|(
name|Get
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|Cell
name|cell
init|=
name|rows
operator|.
name|get
argument_list|(
operator|new
name|String
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Result
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|cell
block|}
argument_list|)
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|htable
operator|.
name|get
argument_list|(
name|Mockito
operator|.
name|anyListOf
argument_list|(
name|Get
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Result
index|[]
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|Get
argument_list|>
name|gets
init|=
operator|(
name|List
argument_list|<
name|Get
argument_list|>
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|Result
index|[]
name|results
init|=
operator|new
name|Result
index|[
name|gets
operator|.
name|size
argument_list|()
index|]
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
name|gets
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Cell
name|cell
init|=
name|rows
operator|.
name|get
argument_list|(
operator|new
name|String
argument_list|(
name|gets
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Result
name|result
decl_stmt|;
if|if
condition|(
name|cell
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|Result
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|cell
block|}
argument_list|)
expr_stmt|;
block|}
name|results
index|[
name|i
index|]
operator|=
name|result
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|htable
operator|.
name|getScanner
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|Scan
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|ResultScanner
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ResultScanner
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Scan
name|scan
init|=
operator|(
name|Scan
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Result
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|start
init|=
operator|new
name|String
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|stop
init|=
operator|new
name|String
argument_list|(
name|scan
operator|.
name|getStopRow
argument_list|()
argument_list|)
decl_stmt|;
name|SortedMap
argument_list|<
name|String
argument_list|,
name|Cell
argument_list|>
name|sub
init|=
name|rows
operator|.
name|subMap
argument_list|(
name|start
argument_list|,
name|stop
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Cell
argument_list|>
name|e
range|:
name|sub
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|results
operator|.
name|add
argument_list|(
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|e
operator|.
name|getValue
argument_list|()
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Iterator
argument_list|<
name|Result
argument_list|>
name|iter
init|=
name|results
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|new
name|ResultScanner
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Result
name|next
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|next
parameter_list|(
name|int
name|nbRows
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Result
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{            }
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Result
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|iter
return|;
block|}
block|}
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Put
name|put
init|=
operator|(
name|Put
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|rows
operator|.
name|put
argument_list|(
operator|new
name|String
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|htable
argument_list|)
operator|.
name|put
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|Put
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|htable
operator|.
name|checkAndPut
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|Put
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// Always say it succeeded and overwrite
name|Put
name|put
init|=
operator|(
name|Put
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|4
index|]
decl_stmt|;
name|rows
operator|.
name|put
argument_list|(
operator|new
name|String
argument_list|(
name|put
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Delete
name|del
init|=
operator|(
name|Delete
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|rows
operator|.
name|remove
argument_list|(
operator|new
name|String
argument_list|(
name|del
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|htable
argument_list|)
operator|.
name|delete
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|Delete
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|htable
operator|.
name|checkAndDelete
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|Delete
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// Always say it succeeded
name|Delete
name|del
init|=
operator|(
name|Delete
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|4
index|]
decl_stmt|;
name|rows
operator|.
name|remove
argument_list|(
operator|new
name|String
argument_list|(
name|del
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Mock connection
name|HBaseConnection
name|hconn
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HBaseConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hconn
operator|.
name|getHBaseTable
argument_list|(
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|htable
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_HBASE_CONNECTION_CLASS
argument_list|,
name|HBaseReadWrite
operator|.
name|TEST_CONN
argument_list|)
expr_stmt|;
name|HBaseReadWrite
operator|.
name|setTestConnection
argument_list|(
name|hconn
argument_list|)
expr_stmt|;
name|HBaseReadWrite
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HBaseStore
name|store
init|=
operator|new
name|HBaseStore
argument_list|()
decl_stmt|;
name|store
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|store
return|;
block|}
block|}
end_class

end_unit


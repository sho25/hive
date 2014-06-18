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
operator|.
name|tez
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
name|assertNotNull
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
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
name|anyBoolean
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
name|times
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
name|verify
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
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Context
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
name|Operator
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
name|BaseWork
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
name|MapWork
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
name|OperatorDesc
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
name|ReduceWork
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
name|TezEdgeProperty
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
name|TezEdgeProperty
operator|.
name|EdgeType
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
name|TezWork
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
operator|.
name|LogHelper
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
name|apache
operator|.
name|hadoop
operator|.
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|LocalResource
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|Resource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|client
operator|.
name|TezSession
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|DAG
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|Edge
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|EdgeProperty
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|ProcessorDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|SessionNotRunning
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|Vertex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|client
operator|.
name|DAGClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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

begin_class
specifier|public
class|class
name|TestTezTask
block|{
name|DagUtils
name|utils
decl_stmt|;
name|MapWork
index|[]
name|mws
decl_stmt|;
name|ReduceWork
index|[]
name|rws
decl_stmt|;
name|TezWork
name|work
decl_stmt|;
name|TezTask
name|task
decl_stmt|;
name|TezSession
name|session
decl_stmt|;
name|TezSessionState
name|sessionState
decl_stmt|;
name|JobConf
name|conf
decl_stmt|;
name|LocalResource
name|appLr
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|op
decl_stmt|;
name|Path
name|path
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|utils
operator|=
name|mock
argument_list|(
name|DagUtils
operator|.
name|class
argument_list|)
expr_stmt|;
name|fs
operator|=
name|mock
argument_list|(
name|FileSystem
operator|.
name|class
argument_list|)
expr_stmt|;
name|path
operator|=
name|mock
argument_list|(
name|Path
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|path
operator|.
name|getFileSystem
argument_list|(
name|any
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|utils
operator|.
name|getTezDir
argument_list|(
name|any
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|utils
operator|.
name|createVertex
argument_list|(
name|any
argument_list|(
name|JobConf
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|BaseWork
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|LocalResource
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|FileSystem
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Context
operator|.
name|class
argument_list|)
argument_list|,
name|anyBoolean
argument_list|()
argument_list|,
name|any
argument_list|(
name|TezWork
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
name|Vertex
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Vertex
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Object
index|[]
name|args
init|=
name|invocation
operator|.
name|getArguments
argument_list|()
decl_stmt|;
return|return
operator|new
name|Vertex
argument_list|(
operator|(
operator|(
name|BaseWork
operator|)
name|args
index|[
literal|1
index|]
operator|)
operator|.
name|getName
argument_list|()
argument_list|,
name|mock
argument_list|(
name|ProcessorDescriptor
operator|.
name|class
argument_list|)
argument_list|,
literal|0
argument_list|,
name|mock
argument_list|(
name|Resource
operator|.
name|class
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|utils
operator|.
name|createEdge
argument_list|(
name|any
argument_list|(
name|JobConf
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Vertex
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|JobConf
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Vertex
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|TezEdgeProperty
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
name|Edge
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Edge
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|Object
index|[]
name|args
init|=
name|invocation
operator|.
name|getArguments
argument_list|()
decl_stmt|;
return|return
operator|new
name|Edge
argument_list|(
operator|(
name|Vertex
operator|)
name|args
index|[
literal|1
index|]
argument_list|,
operator|(
name|Vertex
operator|)
name|args
index|[
literal|3
index|]
argument_list|,
name|mock
argument_list|(
name|EdgeProperty
operator|.
name|class
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|work
operator|=
operator|new
name|TezWork
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|mws
operator|=
operator|new
name|MapWork
index|[]
block|{
operator|new
name|MapWork
argument_list|()
block|,
operator|new
name|MapWork
argument_list|()
block|}
expr_stmt|;
name|rws
operator|=
operator|new
name|ReduceWork
index|[]
block|{
operator|new
name|ReduceWork
argument_list|()
block|,
operator|new
name|ReduceWork
argument_list|()
block|}
expr_stmt|;
name|work
operator|.
name|addAll
argument_list|(
name|mws
argument_list|)
expr_stmt|;
name|work
operator|.
name|addAll
argument_list|(
name|rws
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|w
operator|.
name|setName
argument_list|(
literal|"Work "
operator|+
operator|(
operator|++
name|i
operator|)
argument_list|)
expr_stmt|;
block|}
name|op
operator|=
name|mock
argument_list|(
name|Operator
operator|.
name|class
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
name|op
argument_list|)
expr_stmt|;
name|mws
index|[
literal|0
index|]
operator|.
name|setAliasToWork
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|mws
index|[
literal|1
index|]
operator|.
name|setAliasToWork
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliasList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|aliasList
operator|.
name|add
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|pathMap
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
name|aliasList
argument_list|)
expr_stmt|;
name|mws
index|[
literal|0
index|]
operator|.
name|setPathToAliases
argument_list|(
name|pathMap
argument_list|)
expr_stmt|;
name|mws
index|[
literal|1
index|]
operator|.
name|setPathToAliases
argument_list|(
name|pathMap
argument_list|)
expr_stmt|;
name|rws
index|[
literal|0
index|]
operator|.
name|setReducer
argument_list|(
name|op
argument_list|)
expr_stmt|;
name|rws
index|[
literal|1
index|]
operator|.
name|setReducer
argument_list|(
name|op
argument_list|)
expr_stmt|;
name|TezEdgeProperty
name|edgeProp
init|=
operator|new
name|TezEdgeProperty
argument_list|(
name|EdgeType
operator|.
name|SIMPLE_EDGE
argument_list|)
decl_stmt|;
name|work
operator|.
name|connect
argument_list|(
name|mws
index|[
literal|0
index|]
argument_list|,
name|rws
index|[
literal|0
index|]
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|work
operator|.
name|connect
argument_list|(
name|mws
index|[
literal|1
index|]
argument_list|,
name|rws
index|[
literal|0
index|]
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|work
operator|.
name|connect
argument_list|(
name|rws
index|[
literal|0
index|]
argument_list|,
name|rws
index|[
literal|1
index|]
argument_list|,
name|edgeProp
argument_list|)
expr_stmt|;
name|task
operator|=
operator|new
name|TezTask
argument_list|(
name|utils
argument_list|)
expr_stmt|;
name|task
operator|.
name|setWork
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|task
operator|.
name|setConsole
argument_list|(
name|mock
argument_list|(
name|LogHelper
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|=
operator|new
name|JobConf
argument_list|()
expr_stmt|;
name|appLr
operator|=
name|mock
argument_list|(
name|LocalResource
operator|.
name|class
argument_list|)
expr_stmt|;
name|session
operator|=
name|mock
argument_list|(
name|TezSession
operator|.
name|class
argument_list|)
expr_stmt|;
name|sessionState
operator|=
name|mock
argument_list|(
name|TezSessionState
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|sessionState
operator|.
name|getSession
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|session
operator|.
name|submitDAG
argument_list|(
name|any
argument_list|(
name|DAG
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|SessionNotRunning
argument_list|(
literal|""
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mock
argument_list|(
name|DAGClient
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|utils
operator|=
literal|null
expr_stmt|;
name|work
operator|=
literal|null
expr_stmt|;
name|task
operator|=
literal|null
expr_stmt|;
name|path
operator|=
literal|null
expr_stmt|;
name|fs
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBuildDag
parameter_list|()
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
throws|,
name|Exception
block|{
name|DAG
name|dag
init|=
name|task
operator|.
name|build
argument_list|(
name|conf
argument_list|,
name|work
argument_list|,
name|path
argument_list|,
name|appLr
argument_list|,
literal|null
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|Vertex
name|v
init|=
name|dag
operator|.
name|getVertex
argument_list|(
name|w
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Vertex
argument_list|>
name|outs
init|=
name|v
operator|.
name|getOutputVertices
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|x
range|:
name|work
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
control|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Vertex
name|u
range|:
name|outs
control|)
block|{
if|if
condition|(
name|u
operator|.
name|getVertexName
argument_list|()
operator|.
name|equals
argument_list|(
name|x
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmptyWork
parameter_list|()
throws|throws
name|IllegalArgumentException
throws|,
name|IOException
throws|,
name|Exception
block|{
name|DAG
name|dag
init|=
name|task
operator|.
name|build
argument_list|(
name|conf
argument_list|,
operator|new
name|TezWork
argument_list|(
literal|""
argument_list|)
argument_list|,
name|path
argument_list|,
name|appLr
argument_list|,
literal|null
argument_list|,
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dag
operator|.
name|getVertices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubmit
parameter_list|()
throws|throws
name|Exception
block|{
name|DAG
name|dag
init|=
operator|new
name|DAG
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|task
operator|.
name|submit
argument_list|(
name|conf
argument_list|,
name|dag
argument_list|,
name|path
argument_list|,
name|appLr
argument_list|,
name|sessionState
argument_list|,
operator|new
name|LinkedList
argument_list|()
argument_list|)
expr_stmt|;
comment|// validate close/reopen
name|verify
argument_list|(
name|sessionState
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|open
argument_list|(
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|sessionState
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|close
argument_list|(
name|eq
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// now uses pool after HIVE-7043
name|verify
argument_list|(
name|session
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|submitDAG
argument_list|(
name|any
argument_list|(
name|DAG
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClose
parameter_list|()
throws|throws
name|HiveException
block|{
name|task
operator|.
name|close
argument_list|(
name|work
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|op
argument_list|,
name|times
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|jobClose
argument_list|(
name|any
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


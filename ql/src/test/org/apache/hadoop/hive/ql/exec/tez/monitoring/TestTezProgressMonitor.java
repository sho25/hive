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
operator|.
name|monitoring
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
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|TezException
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
name|DAGStatus
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
name|Progress
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
name|StatusGetOpts
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
name|VertexStatus
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
name|Matchers
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
name|runners
operator|.
name|MockitoJUnitRunner
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
name|HashMap
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|hasItems
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|sameInstance
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|Is
operator|.
name|is
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
name|assertThat
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
name|anySet
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
name|isNull
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
name|verifyNoMoreInteractions
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

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestTezProgressMonitor
block|{
specifier|private
specifier|static
specifier|final
name|String
name|REDUCER
init|=
literal|"Reducer"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MAPPER
init|=
literal|"Mapper"
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|DAGClient
name|dagClient
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|SessionState
operator|.
name|LogHelper
name|console
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|DAGStatus
name|dagStatus
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Progress
name|mapperProgress
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Progress
name|reducerProgress
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|VertexStatus
name|succeeded
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|VertexStatus
name|running
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
name|progressMap
parameter_list|()
block|{
return|return
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Progress
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|MAPPER
argument_list|,
name|setup
argument_list|(
name|mapperProgress
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|REDUCER
argument_list|,
name|setup
argument_list|(
name|reducerProgress
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
specifier|private
name|Progress
name|setup
parameter_list|(
name|Progress
name|progressMock
parameter_list|,
name|int
name|total
parameter_list|,
name|int
name|succeeded
parameter_list|,
name|int
name|failedAttempt
parameter_list|,
name|int
name|killedAttempt
parameter_list|,
name|int
name|running
parameter_list|)
block|{
name|when
argument_list|(
name|progressMock
operator|.
name|getTotalTaskCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|progressMock
operator|.
name|getSucceededTaskCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|succeeded
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|progressMock
operator|.
name|getFailedTaskAttemptCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|failedAttempt
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|progressMock
operator|.
name|getKilledTaskAttemptCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|killedAttempt
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|progressMock
operator|.
name|getRunningTaskCount
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|running
argument_list|)
expr_stmt|;
return|return
name|progressMock
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|setupInternalStateOnObjectCreation
parameter_list|()
throws|throws
name|IOException
throws|,
name|TezException
block|{
name|when
argument_list|(
name|dagStatus
operator|.
name|getState
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|DAGStatus
operator|.
name|State
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|dagClient
operator|.
name|getVertexStatus
argument_list|(
name|eq
argument_list|(
name|MAPPER
argument_list|)
argument_list|,
name|anySet
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|succeeded
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|dagClient
operator|.
name|getVertexStatus
argument_list|(
name|eq
argument_list|(
name|REDUCER
argument_list|)
argument_list|,
name|anySet
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|running
argument_list|)
expr_stmt|;
name|TezProgressMonitor
name|monitor
init|=
operator|new
name|TezProgressMonitor
argument_list|(
name|dagClient
argument_list|,
name|dagStatus
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
argument_list|,
name|progressMap
argument_list|()
argument_list|,
name|console
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|dagClient
argument_list|)
operator|.
name|getVertexStatus
argument_list|(
name|eq
argument_list|(
name|MAPPER
argument_list|)
argument_list|,
name|isNull
argument_list|(
name|Set
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|dagClient
argument_list|)
operator|.
name|getVertexStatus
argument_list|(
name|eq
argument_list|(
name|REDUCER
argument_list|)
argument_list|,
name|isNull
argument_list|(
name|Set
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|dagClient
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|monitor
operator|.
name|vertexStatusMap
operator|.
name|keySet
argument_list|()
argument_list|,
name|hasItems
argument_list|(
name|MAPPER
argument_list|,
name|REDUCER
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|monitor
operator|.
name|vertexStatusMap
operator|.
name|get
argument_list|(
name|MAPPER
argument_list|)
argument_list|,
name|is
argument_list|(
name|sameInstance
argument_list|(
name|succeeded
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|monitor
operator|.
name|vertexStatusMap
operator|.
name|get
argument_list|(
name|REDUCER
argument_list|)
argument_list|,
name|is
argument_list|(
name|sameInstance
argument_list|(
name|running
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|monitor
operator|.
name|progressCountsMap
operator|.
name|keySet
argument_list|()
argument_list|,
name|hasItems
argument_list|(
name|MAPPER
argument_list|,
name|REDUCER
argument_list|)
argument_list|)
expr_stmt|;
name|TezProgressMonitor
operator|.
name|VertexProgress
name|expectedMapperState
init|=
operator|new
name|TezProgressMonitor
operator|.
name|VertexProgress
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|,
literal|5
argument_list|,
name|DAGStatus
operator|.
name|State
operator|.
name|RUNNING
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|monitor
operator|.
name|progressCountsMap
operator|.
name|get
argument_list|(
name|MAPPER
argument_list|)
argument_list|,
name|is
argument_list|(
name|equalTo
argument_list|(
name|expectedMapperState
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|TezProgressMonitor
operator|.
name|VertexProgress
name|expectedReducerState
init|=
operator|new
name|TezProgressMonitor
operator|.
name|VertexProgress
argument_list|(
literal|3
argument_list|,
literal|2
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|DAGStatus
operator|.
name|State
operator|.
name|RUNNING
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|monitor
operator|.
name|progressCountsMap
operator|.
name|get
argument_list|(
name|REDUCER
argument_list|)
argument_list|,
name|is
argument_list|(
name|equalTo
argument_list|(
name|expectedReducerState
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


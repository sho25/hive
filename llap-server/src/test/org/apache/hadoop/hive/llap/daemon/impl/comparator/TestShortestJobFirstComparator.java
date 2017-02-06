begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|comparator
package|;
end_package

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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|TaskExecutorTestHelpers
operator|.
name|createSubmitWorkRequestProto
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|TaskExecutorTestHelpers
operator|.
name|createTaskWrapper
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
name|assertNull
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|EvictingPriorityBlockingQueue
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
name|llap
operator|.
name|daemon
operator|.
name|impl
operator|.
name|TaskExecutorService
operator|.
name|TaskWrapper
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

begin_class
specifier|public
class|class
name|TestShortestJobFirstComparator
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWaitQueueComparator
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|TaskWrapper
name|r1
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r2
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|,
literal|200
argument_list|,
literal|300
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r3
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r4
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|8
argument_list|,
literal|400
argument_list|,
literal|500
argument_list|,
literal|"q4"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r5
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|500
argument_list|,
literal|600
argument_list|,
literal|"q5"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
decl_stmt|;
name|EvictingPriorityBlockingQueue
argument_list|<
name|TaskWrapper
argument_list|>
name|queue
init|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
comment|// this offer will be rejected
name|assertEquals
argument_list|(
name|r5
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r5
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|r1
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r2
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|,
literal|200
argument_list|,
literal|300
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r3
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r4
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|8
argument_list|,
literal|400
argument_list|,
literal|500
argument_list|,
literal|"q4"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r5
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|500
argument_list|,
literal|600
argument_list|,
literal|"q5"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
comment|// this offer will be rejected
name|assertEquals
argument_list|(
name|r5
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r5
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|r1
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|,
literal|1000
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r2
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|,
literal|200
argument_list|,
literal|900
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r3
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|,
literal|300
argument_list|,
literal|800
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r4
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|1
argument_list|,
literal|400
argument_list|,
literal|700
argument_list|,
literal|"q4"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r5
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|500
argument_list|,
literal|600
argument_list|,
literal|"q5"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
comment|// offer accepted and r4 gets evicted
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r5
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r5
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|r1
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r2
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|,
literal|200
argument_list|,
literal|300
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r3
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r4
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|8
argument_list|,
literal|400
argument_list|,
literal|500
argument_list|,
literal|"q4"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r5
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|500
argument_list|,
literal|600
argument_list|,
literal|"q5"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
comment|// offer accepted and r4 gets evicted
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r5
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r5
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|r1
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r2
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|,
literal|200
argument_list|,
literal|300
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r3
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r4
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|8
argument_list|,
literal|400
argument_list|,
literal|500
argument_list|,
literal|"q4"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r5
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|500
argument_list|,
literal|600
argument_list|,
literal|"q5"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
comment|// offer accepted and r4 gets evicted
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r5
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r5
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|r1
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r2
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|,
literal|200
argument_list|,
literal|300
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|r3
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r4
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|8
argument_list|,
literal|400
argument_list|,
literal|500
argument_list|,
literal|"q4"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|r5
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|500
argument_list|,
literal|600
argument_list|,
literal|"q5"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
comment|// offer accepted, r1 evicted
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r5
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r5
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWaitQueueComparatorWithinDagPriority
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|TaskWrapper
name|r1
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r2
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r3
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|5
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|EvictingPriorityBlockingQueue
argument_list|<
name|TaskWrapper
argument_list|>
name|queue
init|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWaitQueueComparatorWithinSameDagPriority
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|TaskWrapper
name|r1
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r2
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r3
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|EvictingPriorityBlockingQueue
argument_list|<
name|TaskWrapper
argument_list|>
name|queue
init|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// can not queue more requests as queue is full
name|TaskWrapper
name|r4
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|4
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|r4
argument_list|,
name|queue
operator|.
name|offer
argument_list|(
name|r4
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWaitQueueComparatorParallelism
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|TaskWrapper
name|r1
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|1
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
comment|// 7 pending
name|TaskWrapper
name|r2
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|,
literal|7
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|1
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
comment|// 3 pending
name|TaskWrapper
name|r3
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|,
literal|5
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|1
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
comment|// 5 pending
name|EvictingPriorityBlockingQueue
argument_list|<
name|TaskWrapper
argument_list|>
name|queue
init|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testWaitQueueComparatorAging
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|TaskWrapper
name|r1
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r2
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|TaskWrapper
name|r3
init|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|30
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
decl_stmt|;
name|EvictingPriorityBlockingQueue
argument_list|<
name|TaskWrapper
argument_list|>
name|queue
init|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
comment|// priority = 10 / (200 - 100) = 0.01
name|r1
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|,
literal|100
argument_list|,
literal|200
argument_list|,
literal|"q1"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
comment|// priority = 20 / (3000 - 100) = 0.0069
name|r2
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|,
literal|100
argument_list|,
literal|3000
argument_list|,
literal|"q2"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
comment|// priority = 30 / (4000 - 100) = 0.0076
name|r3
operator|=
name|createTaskWrapper
argument_list|(
name|createSubmitWorkRequestProto
argument_list|(
literal|3
argument_list|,
literal|30
argument_list|,
literal|100
argument_list|,
literal|4000
argument_list|,
literal|"q3"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|100000
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|EvictingPriorityBlockingQueue
argument_list|<>
argument_list|(
operator|new
name|ShortestJobFirstComparator
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r1
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r2
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
name|r3
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r2
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r3
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|r1
argument_list|,
name|queue
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


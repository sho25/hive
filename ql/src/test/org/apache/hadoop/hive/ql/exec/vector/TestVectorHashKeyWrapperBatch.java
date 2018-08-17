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
name|vector
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
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|vector
operator|.
name|expressions
operator|.
name|IdentityExpression
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|vector
operator|.
name|util
operator|.
name|FakeVectorRowBatchFromObjectIterables
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
name|vector
operator|.
name|wrapper
operator|.
name|VectorHashKeyWrapperBase
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
name|vector
operator|.
name|wrapper
operator|.
name|VectorHashKeyWrapperBatch
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

begin_comment
comment|/**  * Unit test for VectorHashKeyWrapperBatch class.  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorHashKeyWrapperBatch
block|{
comment|// Specific test for HIVE-18744 --
comment|// Tests Timestamp assignment.
annotation|@
name|Test
specifier|public
name|void
name|testVectorHashKeyWrapperBatch
parameter_list|()
throws|throws
name|HiveException
block|{
name|VectorExpression
index|[]
name|keyExpressions
init|=
operator|new
name|VectorExpression
index|[]
block|{
operator|new
name|IdentityExpression
argument_list|(
literal|0
argument_list|)
block|}
decl_stmt|;
name|TypeInfo
index|[]
name|typeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
block|}
decl_stmt|;
name|VectorHashKeyWrapperBatch
name|vhkwb
init|=
name|VectorHashKeyWrapperBatch
operator|.
name|compileKeyWrapperBatch
argument_list|(
name|keyExpressions
argument_list|,
name|typeInfos
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|batch
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|false
expr_stmt|;
name|batch
operator|.
name|size
operator|=
literal|10
expr_stmt|;
name|TimestampColumnVector
name|timestampColVector
init|=
operator|new
name|TimestampColumnVector
argument_list|(
name|batch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
empty_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|=
name|timestampColVector
expr_stmt|;
name|timestampColVector
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// Cause Timestamp object to be replaced (in buggy code) with ZERO_TIMESTAMP.
name|timestampColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|timestampColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|Timestamp
name|scratch
init|=
operator|new
name|Timestamp
argument_list|(
literal|2039
argument_list|)
decl_stmt|;
name|Timestamp
name|ts0
init|=
operator|new
name|Timestamp
argument_list|(
literal|2039
argument_list|)
decl_stmt|;
name|scratch
operator|.
name|setTime
argument_list|(
name|ts0
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setNanos
argument_list|(
name|ts0
operator|.
name|getNanos
argument_list|()
argument_list|)
expr_stmt|;
name|timestampColVector
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|scratch
argument_list|)
expr_stmt|;
name|Timestamp
name|ts1
init|=
operator|new
name|Timestamp
argument_list|(
literal|33222
argument_list|)
decl_stmt|;
name|scratch
operator|.
name|setTime
argument_list|(
name|ts1
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setNanos
argument_list|(
name|ts1
operator|.
name|getNanos
argument_list|()
argument_list|)
expr_stmt|;
name|timestampColVector
operator|.
name|set
argument_list|(
literal|2
argument_list|,
name|scratch
argument_list|)
expr_stmt|;
name|batch
operator|.
name|size
operator|=
literal|3
expr_stmt|;
name|vhkwb
operator|.
name|evaluateBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|VectorHashKeyWrapperBase
index|[]
name|vhkwArray
init|=
name|vhkwb
operator|.
name|getVectorHashKeyWrappers
argument_list|()
decl_stmt|;
name|VectorHashKeyWrapperBase
name|vhk
init|=
name|vhkwArray
index|[
literal|0
index|]
decl_stmt|;
name|assertTrue
argument_list|(
name|vhk
operator|.
name|isNull
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|vhk
operator|=
name|vhkwArray
index|[
literal|1
index|]
expr_stmt|;
name|assertFalse
argument_list|(
name|vhk
operator|.
name|isNull
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|vhk
operator|.
name|getTimestamp
argument_list|(
literal|0
argument_list|)
argument_list|,
name|ts0
argument_list|)
expr_stmt|;
name|vhk
operator|=
name|vhkwArray
index|[
literal|2
index|]
expr_stmt|;
name|assertFalse
argument_list|(
name|vhk
operator|.
name|isNull
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|vhk
operator|.
name|getTimestamp
argument_list|(
literal|0
argument_list|)
argument_list|,
name|ts1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


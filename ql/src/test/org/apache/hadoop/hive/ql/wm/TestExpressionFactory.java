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
name|wm
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
name|wm
operator|.
name|Expression
operator|.
name|Predicate
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
name|TestExpressionFactory
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSize
parameter_list|()
block|{
name|Expression
name|expr
init|=
literal|null
decl_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"BYTES_READ> 5"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BYTES_READ"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"BYTES_READ> '5kb'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BYTES_READ"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
operator|*
operator|(
literal|1
operator|<<
literal|10
operator|)
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"BYTES_READ> '2mb'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BYTES_READ"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
operator|*
operator|(
literal|1
operator|<<
literal|20
operator|)
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"BYTES_READ> '3gb'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BYTES_READ"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3L
operator|*
operator|(
literal|1
operator|<<
literal|30
operator|)
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"SHUFFLE_BYTES> '7tb'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"SHUFFLE_BYTES"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7L
operator|*
operator|(
literal|1L
operator|<<
literal|40
operator|)
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"SHUFFLE_BYTES> '6pb'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"SHUFFLE_BYTES"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6L
operator|*
operator|(
literal|1L
operator|<<
literal|50
operator|)
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"BYTES_WRITTEN> 27"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BYTES_WRITTEN"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|27
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTime
parameter_list|()
block|{
name|Expression
name|expr
init|=
literal|null
decl_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"ELAPSED_TIME> 1"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ELAPSED_TIME"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"ELAPSED_TIME> '1ms'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ELAPSED_TIME"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"ELAPSED_TIME> '1sec'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ELAPSED_TIME"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"ELAPSED_TIME> '1min'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ELAPSED_TIME"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
operator|*
literal|1000
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"ELAPSED_TIME> '1hour'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ELAPSED_TIME"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3600
operator|*
literal|1000
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
name|expr
operator|=
name|ExpressionFactory
operator|.
name|fromString
argument_list|(
literal|"ELAPSED_TIME> '1day'"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|expr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Predicate
operator|.
name|GREATER_THAN
argument_list|,
name|expr
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ELAPSED_TIME"
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|24
operator|*
literal|3600
operator|*
literal|1000
argument_list|,
name|expr
operator|.
name|getCounterLimit
argument_list|()
operator|.
name|getLimit
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|io
operator|.
name|parquet
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
name|common
operator|.
name|type
operator|.
name|HiveChar
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
name|type
operator|.
name|HiveVarchar
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
name|parquet
operator|.
name|read
operator|.
name|ParquetFilterPredicateConverter
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
name|PredicateLeaf
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
name|ql
operator|.
name|io
operator|.
name|sarg
operator|.
name|SearchArgumentFactory
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
name|io
operator|.
name|HiveDecimalWritable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageTypeParser
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
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterPredicate
import|;
end_import

begin_comment
comment|/**  * These tests test the conversion to Parquet's sarg implementation.  */
end_comment

begin_class
specifier|public
class|class
name|TestParquetRecordReaderWrapper
block|{
annotation|@
name|Test
specifier|public
name|void
name|testBuilder
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchArgument
name|sarg
init|=
name|SearchArgumentFactory
operator|.
name|newBuilder
argument_list|()
operator|.
name|startNot
argument_list|()
operator|.
name|startOr
argument_list|()
operator|.
name|isNull
argument_list|(
literal|"x"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|)
operator|.
name|between
argument_list|(
literal|"y"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|10L
argument_list|,
literal|20L
argument_list|)
operator|.
name|in
argument_list|(
literal|"z"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|1L
argument_list|,
literal|2L
argument_list|,
literal|3L
argument_list|)
operator|.
name|nullSafeEquals
argument_list|(
literal|"a"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|"stinger"
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test {"
operator|+
literal|" optional int32 x; required int32 y; required int32 z;"
operator|+
literal|" optional binary a;}"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"and(and(and(not(eq(x, null)), not(and(lteq(y, 20), not(lt(y, 10))))), not(or(or(eq(z, 1), "
operator|+
literal|"eq(z, 2)), eq(z, 3)))), not(eq(a, Binary{\"stinger\"})))"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check the converted filter predicate is null if unsupported types are included    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBuilderComplexTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchArgument
name|sarg
init|=
name|SearchArgumentFactory
operator|.
name|newBuilder
argument_list|()
operator|.
name|startAnd
argument_list|()
operator|.
name|lessThan
argument_list|(
literal|"x"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DATE
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"1970-1-11"
argument_list|)
argument_list|)
operator|.
name|lessThanEquals
argument_list|(
literal|"y"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
operator|new
name|HiveChar
argument_list|(
literal|"hi"
argument_list|,
literal|10
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"z"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"1.0"
argument_list|)
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test {"
operator|+
literal|" required int32 x; required binary y; required binary z;}"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
argument_list|)
expr_stmt|;
name|sarg
operator|=
name|SearchArgumentFactory
operator|.
name|newBuilder
argument_list|()
operator|.
name|startNot
argument_list|()
operator|.
name|startOr
argument_list|()
operator|.
name|isNull
argument_list|(
literal|"x"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|)
operator|.
name|between
argument_list|(
literal|"y"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"10"
argument_list|)
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"20.0"
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
literal|"z"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|1L
argument_list|,
literal|2L
argument_list|,
literal|3L
argument_list|)
operator|.
name|nullSafeEquals
argument_list|(
literal|"a"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
operator|new
name|HiveVarchar
argument_list|(
literal|"stinger"
argument_list|,
literal|100
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
name|schema
operator|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test {"
operator|+
literal|" optional int32 x; required binary y; required int32 z;"
operator|+
literal|" optional binary a;}"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check the converted filter predicate is null if unsupported types are included    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testBuilderComplexTypes2
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchArgument
name|sarg
init|=
name|SearchArgumentFactory
operator|.
name|newBuilder
argument_list|()
operator|.
name|startAnd
argument_list|()
operator|.
name|lessThan
argument_list|(
literal|"x"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DATE
argument_list|,
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2005-3-12"
argument_list|)
argument_list|)
operator|.
name|lessThanEquals
argument_list|(
literal|"y"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
operator|new
name|HiveChar
argument_list|(
literal|"hi"
argument_list|,
literal|10
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"z"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"1.0"
argument_list|)
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test {"
operator|+
literal|" required int32 x; required binary y; required binary z;}"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
argument_list|)
expr_stmt|;
name|sarg
operator|=
name|SearchArgumentFactory
operator|.
name|newBuilder
argument_list|()
operator|.
name|startNot
argument_list|()
operator|.
name|startOr
argument_list|()
operator|.
name|isNull
argument_list|(
literal|"x"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|)
operator|.
name|between
argument_list|(
literal|"y"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"10"
argument_list|)
argument_list|,
operator|new
name|HiveDecimalWritable
argument_list|(
literal|"20.0"
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
literal|"z"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|1L
argument_list|,
literal|2L
argument_list|,
literal|3L
argument_list|)
operator|.
name|nullSafeEquals
argument_list|(
literal|"a"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
operator|new
name|HiveVarchar
argument_list|(
literal|"stinger"
argument_list|,
literal|100
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
name|schema
operator|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test {"
operator|+
literal|" optional int32 x; required binary y; required int32 z;"
operator|+
literal|" optional binary a;}"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBuilderFloat
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchArgument
name|sarg
init|=
name|SearchArgumentFactory
operator|.
name|newBuilder
argument_list|()
operator|.
name|startAnd
argument_list|()
operator|.
name|lessThan
argument_list|(
literal|"x"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|22L
argument_list|)
operator|.
name|lessThan
argument_list|(
literal|"x1"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
literal|22L
argument_list|)
operator|.
name|lessThanEquals
argument_list|(
literal|"y"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
operator|new
name|HiveChar
argument_list|(
literal|"hi"
argument_list|,
literal|10
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
literal|"z"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|FLOAT
argument_list|,
operator|new
name|Double
argument_list|(
literal|0.22
argument_list|)
argument_list|)
operator|.
name|equals
argument_list|(
literal|"z1"
argument_list|,
name|PredicateLeaf
operator|.
name|Type
operator|.
name|FLOAT
argument_list|,
operator|new
name|Double
argument_list|(
literal|0.22
argument_list|)
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test {"
operator|+
literal|" required int32 x; required int32 x1;"
operator|+
literal|" required binary y; required float z; required float z1;}"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"and(and(and(and(lt(x, 22), lt(x1, 22)),"
operator|+
literal|" lteq(y, Binary{\"hi        \"})), eq(z, "
operator|+
literal|"0.22)), eq(z1, 0.22))"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|read
package|;
end_package

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
name|Sets
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
name|HashSet
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|HiveParquetSchemaTestUtils
operator|.
name|testConversion
import|;
end_import

begin_class
specifier|public
class|class
name|TestDataWritableReadSupport
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetProjectedSchema1
parameter_list|()
throws|throws
name|Exception
block|{
name|MessageType
name|originalMsg
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a;\n"
operator|+
literal|"    optional double b;\n"
operator|+
literal|"    optional boolean c;\n"
operator|+
literal|"    optional fixed_len_byte_array(3) d (DECIMAL(5,2));\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:int>"
argument_list|,
name|DataWritableReadSupport
operator|.
name|getProjectedSchema
argument_list|(
name|originalMsg
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"structCol"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"structCol.a"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetProjectedSchema2
parameter_list|()
throws|throws
name|Exception
block|{
name|MessageType
name|originalMsg
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a;\n"
operator|+
literal|"    optional double b;\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:int,b:double>"
argument_list|,
name|DataWritableReadSupport
operator|.
name|getProjectedSchema
argument_list|(
name|originalMsg
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"structCol"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"structCol.a"
argument_list|,
literal|"structCol.b"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetProjectedSchema3
parameter_list|()
throws|throws
name|Exception
block|{
name|MessageType
name|originalMsg
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a;\n"
operator|+
literal|"    optional double b;\n"
operator|+
literal|"  }\n"
operator|+
literal|"  optional boolean c;\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|testConversion
argument_list|(
literal|"structCol,c"
argument_list|,
literal|"struct<b:double>,boolean"
argument_list|,
name|DataWritableReadSupport
operator|.
name|getProjectedSchema
argument_list|(
name|originalMsg
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"structCol"
argument_list|,
literal|"c"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"structCol.b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetProjectedSchema4
parameter_list|()
throws|throws
name|Exception
block|{
name|MessageType
name|originalMsg
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a;\n"
operator|+
literal|"    optional group subStructCol {\n"
operator|+
literal|"      optional int64 b;\n"
operator|+
literal|"      optional boolean c;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"  optional boolean d;\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<subStructCol:struct<b:bigint>>"
argument_list|,
name|DataWritableReadSupport
operator|.
name|getProjectedSchema
argument_list|(
name|originalMsg
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"structCol"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"structCol.subStructCol.b"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetProjectedSchema5
parameter_list|()
throws|throws
name|Exception
block|{
name|MessageType
name|originalMsg
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a;\n"
operator|+
literal|"    optional group subStructCol {\n"
operator|+
literal|"      optional int64 b;\n"
operator|+
literal|"      optional boolean c;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"  optional boolean d;\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<subStructCol:struct<b:bigint,c:boolean>>"
argument_list|,
name|DataWritableReadSupport
operator|.
name|getProjectedSchema
argument_list|(
name|originalMsg
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"structCol"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"structCol.subStructCol"
argument_list|,
literal|"structCol.subStructCol.b"
argument_list|,
literal|"structCol.subStructCol.c"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|List
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
name|convert
operator|.
name|HiveSchemaConverter
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
name|TypeInfoUtils
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
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|OriginalType
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
name|Type
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
name|Type
operator|.
name|Repetition
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
name|TestHiveSchemaConverter
block|{
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|createHiveColumnsFrom
parameter_list|(
specifier|final
name|String
name|columnNamesStr
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
if|if
condition|(
name|columnNamesStr
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnNames
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNamesStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|columnNames
return|;
block|}
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|createHiveTypeInfoFrom
parameter_list|(
specifier|final
name|String
name|columnsTypeStr
parameter_list|)
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
if|if
condition|(
name|columnsTypeStr
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnsTypeStr
argument_list|)
expr_stmt|;
block|}
return|return
name|columnTypes
return|;
block|}
specifier|private
name|void
name|testConversion
parameter_list|(
specifier|final
name|String
name|columnNamesStr
parameter_list|,
specifier|final
name|String
name|columnsTypeStr
parameter_list|,
specifier|final
name|String
name|expectedSchema
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|createHiveColumnsFrom
argument_list|(
name|columnNamesStr
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|createHiveTypeInfoFrom
argument_list|(
name|columnsTypeStr
argument_list|)
decl_stmt|;
specifier|final
name|MessageType
name|messageTypeFound
init|=
name|HiveSchemaConverter
operator|.
name|convert
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
specifier|final
name|MessageType
name|expectedMT
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
name|expectedSchema
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"converting "
operator|+
name|columnNamesStr
operator|+
literal|": "
operator|+
name|columnsTypeStr
operator|+
literal|" to "
operator|+
name|expectedSchema
argument_list|,
name|expectedMT
argument_list|,
name|messageTypeFound
argument_list|)
expr_stmt|;
comment|// Required to check the original types manually as PrimitiveType.equals does not care about it
name|List
argument_list|<
name|Type
argument_list|>
name|expectedFields
init|=
name|expectedMT
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Type
argument_list|>
name|actualFields
init|=
name|messageTypeFound
operator|.
name|getFields
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|n
init|=
name|expectedFields
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|OriginalType
name|exp
init|=
name|expectedFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getOriginalType
argument_list|()
decl_stmt|;
name|OriginalType
name|act
init|=
name|actualFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getOriginalType
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Original types of the field do not match"
argument_list|,
name|exp
argument_list|,
name|act
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSimpleType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a,b,c,d"
argument_list|,
literal|"int,bigint,double,boolean"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional int32 a;\n"
operator|+
literal|"  optional int64 b;\n"
operator|+
literal|"  optional double c;\n"
operator|+
literal|"  optional boolean d;\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSpecialIntType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a,b"
argument_list|,
literal|"tinyint,smallint"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional int32 a (INT_8);\n"
operator|+
literal|"  optional int32 b (INT_16);\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a"
argument_list|,
literal|"decimal(5,2)"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional fixed_len_byte_array(3) a (DECIMAL(5,2));\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCharType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a"
argument_list|,
literal|"char(5)"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional binary a (UTF8);\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testVarcharType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a"
argument_list|,
literal|"varchar(10)"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional binary a (UTF8);\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDateType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a"
argument_list|,
literal|"date"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional int32 a (DATE);\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArray
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"arrayCol"
argument_list|,
literal|"array<int>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional int32 array_element;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testArrayDecimal
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"arrayCol"
argument_list|,
literal|"array<decimal(5,2)>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional fixed_len_byte_array(3) array_element (DECIMAL(5,2));\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStruct
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:int,b:double,c:boolean,d:decimal(5,2)>"
argument_list|,
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
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMap
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"mapCol"
argument_list|,
literal|"map<string,string>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required binary key;\n"
operator|+
literal|"      optional binary value;\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapDecimal
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"mapCol"
argument_list|,
literal|"map<string,decimal(5,2)>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required binary key;\n"
operator|+
literal|"      optional fixed_len_byte_array(3) value (DECIMAL(5,2));\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMapOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|hiveColumnTypes
init|=
literal|"map<string,string>"
decl_stmt|;
specifier|final
name|String
name|hiveColumnNames
init|=
literal|"mapCol"
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|createHiveColumnsFrom
argument_list|(
name|hiveColumnNames
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|createHiveTypeInfoFrom
argument_list|(
name|hiveColumnTypes
argument_list|)
decl_stmt|;
specifier|final
name|MessageType
name|messageTypeFound
init|=
name|HiveSchemaConverter
operator|.
name|convert
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
comment|// this messageType only has one optional field, whose name is mapCol, original Type is MAP
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|messageTypeFound
operator|.
name|getFieldCount
argument_list|()
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|Type
name|topLevel
init|=
name|messageTypeFound
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"mapCol"
argument_list|,
name|topLevel
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|OriginalType
operator|.
name|MAP
argument_list|,
name|topLevel
operator|.
name|getOriginalType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|topLevel
operator|.
name|getRepetition
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|topLevel
operator|.
name|asGroupType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
argument_list|)
expr_stmt|;
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|Type
name|secondLevel
init|=
name|topLevel
operator|.
name|asGroupType
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|//there is one repeated field for mapCol, the field name is "map" and its original Type is MAP_KEY_VALUE;
name|assertEquals
argument_list|(
literal|"map"
argument_list|,
name|secondLevel
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|OriginalType
operator|.
name|MAP_KEY_VALUE
argument_list|,
name|secondLevel
operator|.
name|getOriginalType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Repetition
operator|.
name|REPEATED
argument_list|,
name|secondLevel
operator|.
name|getRepetition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|HiveParquetSchemaTestUtils
operator|.
name|createHiveColumnsFrom
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
name|createHiveTypeInfoFrom
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
name|testLogicalTypeAnnotation
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
name|ListTypeInfo
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
name|LogicalTypeAnnotation
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
literal|"a,b,c,d,e,f,g"
argument_list|,
literal|"int,bigint,double,boolean,string,float,binary"
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
literal|"  optional binary e (UTF8);\n"
operator|+
literal|"  optional float f;\n"
operator|+
literal|"  optional binary g;\n"
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
name|testSpecialIntTypeWithLogicatlTypeAnnotations
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
literal|"  optional int32 a (INTEGER(8,true));\n"
operator|+
literal|"  optional int32 b (INTEGER(16,true));\n"
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
name|testTimestampType
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"a"
argument_list|,
literal|"timestamp"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional int96 a;\n"
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
name|testArrayTinyInt
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"arrayCol"
argument_list|,
literal|"array<tinyint>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional int32 array_element (INT_8);\n"
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
name|testArraySmallInt
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"arrayCol"
argument_list|,
literal|"array<smallint>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional int32 array_element (INT_16);\n"
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
name|testArrayString
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"arrayCol"
argument_list|,
literal|"array<string>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional binary array_element (UTF8);\n"
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
name|testArrayTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"arrayCol"
argument_list|,
literal|"array<timestamp>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional int96 array_element;\n"
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
name|testArrayStruct
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"array<struct<a:string,b:int>>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional group array_element {\n"
operator|+
literal|"        optional binary a (UTF8);\n"
operator|+
literal|"        optional int32 b;\n"
operator|+
literal|"      }\n"
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
name|testArrayInArray
parameter_list|()
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
literal|"arrayCol"
argument_list|)
decl_stmt|;
name|ListTypeInfo
name|listTypeInfo
init|=
operator|new
name|ListTypeInfo
argument_list|()
decl_stmt|;
name|listTypeInfo
operator|.
name|setListElementTypeInfo
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
literal|"int"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ListTypeInfo
name|listTypeInfo2
init|=
operator|new
name|ListTypeInfo
argument_list|()
decl_stmt|;
name|listTypeInfo2
operator|.
name|setListElementTypeInfo
argument_list|(
name|listTypeInfo
argument_list|)
expr_stmt|;
name|typeInfos
operator|.
name|add
argument_list|(
name|listTypeInfo2
argument_list|)
expr_stmt|;
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
name|typeInfos
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
literal|"message hive_schema {\n"
operator|+
literal|"  optional group arrayCol (LIST) {\n"
operator|+
literal|"    repeated group bag {\n"
operator|+
literal|"      optional group array_element (LIST) {\n"
operator|+
literal|"        repeated group bag {\n"
operator|+
literal|"          optional int32 array_element;\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedMT
argument_list|,
name|messageTypeFound
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
name|testStructInts
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:tinyint,b:smallint,c:int,d:bigint>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int32 a (INT_8);\n"
operator|+
literal|"    optional int32 b (INT_16);\n"
operator|+
literal|"    optional int32 c;\n"
operator|+
literal|"    optional int64 d;\n"
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
name|testStructStrings
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:char(5),b:varchar(25),c:string>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional binary a (UTF8);\n"
operator|+
literal|"    optional binary b (UTF8);\n"
operator|+
literal|"    optional binary c (UTF8);\n"
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
name|testStructTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:timestamp>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional int96 a;\n"
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
name|testStructList
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"structCol"
argument_list|,
literal|"struct<a:array<string>,b:int,c:string>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group structCol {\n"
operator|+
literal|"    optional group a (LIST) {\n"
operator|+
literal|"      repeated group bag {\n"
operator|+
literal|"        optional binary array_element (UTF8);\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"    optional int32 b;\n"
operator|+
literal|"    optional binary c (UTF8);"
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
literal|"      required binary key (UTF8);\n"
operator|+
literal|"      optional binary value (UTF8);\n"
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
literal|"      required binary key (UTF8);\n"
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
name|testMapInts
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"mapCol"
argument_list|,
literal|"map<smallint,tinyint>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required int32 key (INT_16);\n"
operator|+
literal|"      optional int32 value (INT_8);\n"
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
name|testMapStruct
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"mapCol"
argument_list|,
literal|"map<string,struct<a:smallint,b:int>>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required binary key (UTF8);\n"
operator|+
literal|"      optional group value {\n"
operator|+
literal|"        optional int32 a (INT_16);\n"
operator|+
literal|"        optional int32 b;\n"
operator|+
literal|"      }\n"
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
name|testMapList
parameter_list|()
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
literal|"mapCol"
argument_list|,
literal|"map<string,array<string>>"
argument_list|,
literal|"message hive_schema {\n"
operator|+
literal|"  optional group mapCol (MAP) {\n"
operator|+
literal|"    repeated group map (MAP_KEY_VALUE) {\n"
operator|+
literal|"      required binary key (UTF8);\n"
operator|+
literal|"      optional group value (LIST) {\n"
operator|+
literal|"        repeated group bag {\n"
operator|+
literal|"          optional binary array_element (UTF8);\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
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
name|testLogicalTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|testLogicalTypeAnnotation
argument_list|(
literal|"string"
argument_list|,
literal|"a"
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|stringType
argument_list|()
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"int"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"smallint"
argument_list|,
literal|"a"
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|intType
argument_list|(
literal|16
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"tinyint"
argument_list|,
literal|"a"
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|intType
argument_list|(
literal|8
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"bigint"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"double"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"float"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"boolean"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"binary"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"timestamp"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"char(3)"
argument_list|,
literal|"a"
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|stringType
argument_list|()
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"varchar(30)"
argument_list|,
literal|"a"
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|stringType
argument_list|()
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotation
argument_list|(
literal|"decimal(7,2)"
argument_list|,
literal|"a"
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|decimalType
argument_list|(
literal|2
argument_list|,
literal|7
argument_list|)
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
name|MessageType
name|messageTypeFound
init|=
name|createSchema
argument_list|(
literal|"map<string,string>"
argument_list|,
literal|"mapCol"
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
name|checkField
argument_list|(
name|topLevel
argument_list|,
literal|"mapCol"
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|mapType
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
comment|// there is one repeated field for mapCol, the field name is "map" and its original Type is
comment|// MAP_KEY_VALUE;
name|checkField
argument_list|(
name|secondLevel
argument_list|,
literal|"map"
argument_list|,
name|Repetition
operator|.
name|REPEATED
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|MapKeyValueTypeAnnotation
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|MessageType
name|messageTypeFound
init|=
name|createSchema
argument_list|(
literal|"array<tinyint>"
argument_list|,
literal|"arrayCol"
argument_list|)
decl_stmt|;
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
name|checkField
argument_list|(
name|topLevel
argument_list|,
literal|"arrayCol"
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|listType
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
name|checkField
argument_list|(
name|secondLevel
argument_list|,
literal|"bag"
argument_list|,
name|Repetition
operator|.
name|REPEATED
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|secondLevel
operator|.
name|asGroupType
argument_list|()
operator|.
name|getFieldCount
argument_list|()
argument_list|)
expr_stmt|;
name|Type
name|thirdLevel
init|=
name|secondLevel
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
name|checkField
argument_list|(
name|thirdLevel
argument_list|,
literal|"array_element"
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|intType
argument_list|(
literal|8
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStructOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|MessageType
name|messageTypeFound
init|=
name|createSchema
argument_list|(
literal|"struct<a:smallint,b:string>"
argument_list|,
literal|"structCol"
argument_list|)
decl_stmt|;
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
name|checkField
argument_list|(
name|topLevel
argument_list|,
literal|"structCol"
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
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
name|Type
name|a
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
name|checkField
argument_list|(
name|a
argument_list|,
literal|"a"
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|intType
argument_list|(
literal|16
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Type
name|b
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
literal|1
argument_list|)
decl_stmt|;
name|checkField
argument_list|(
name|b
argument_list|,
literal|"b"
argument_list|,
name|Repetition
operator|.
name|OPTIONAL
argument_list|,
name|LogicalTypeAnnotation
operator|.
name|stringType
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MessageType
name|createSchema
parameter_list|(
name|String
name|hiveColumnTypes
parameter_list|,
name|String
name|hiveColumnNames
parameter_list|)
block|{
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
return|return
name|HiveSchemaConverter
operator|.
name|convert
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
return|;
block|}
specifier|private
name|void
name|checkField
parameter_list|(
name|Type
name|field
parameter_list|,
name|String
name|expectedName
parameter_list|,
name|Repetition
name|expectedRepetition
parameter_list|,
name|LogicalTypeAnnotation
name|expectedLogicalType
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expectedName
argument_list|,
name|field
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedLogicalType
argument_list|,
name|field
operator|.
name|getLogicalTypeAnnotation
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedRepetition
argument_list|,
name|field
operator|.
name|getRepetition
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


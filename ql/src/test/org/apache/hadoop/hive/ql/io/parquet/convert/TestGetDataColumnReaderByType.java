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
name|io
operator|.
name|parquet
operator|.
name|convert
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
name|assertTrue
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
name|vector
operator|.
name|ParquetDataColumnReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|DefaultParquetDataColumnReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromBooleanPageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromDecimalPageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromDoublePageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromFloatPageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromInt32PageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromInt64PageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromInt96PageReader
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
name|vector
operator|.
name|ParquetDataColumnReaderFactory
operator|.
name|TypesFromStringPageReader
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
name|DecimalTypeInfo
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
name|PrimitiveType
operator|.
name|PrimitiveTypeName
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
name|Types
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

begin_comment
comment|/**  * Tests for ParquetDataColumnReaderFactory#getDataColumnReaderByType.  */
end_comment

begin_class
specifier|public
class|class
name|TestGetDataColumnReaderByType
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetDecimalReader
parameter_list|()
throws|throws
name|Exception
block|{
name|TypeInfo
name|hiveTypeInfo
init|=
operator|new
name|DecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|FIXED_LEN_BYTE_ARRAY
argument_list|)
operator|.
name|length
argument_list|(
literal|20
argument_list|)
operator|.
name|as
argument_list|(
name|LogicalTypeAnnotation
operator|.
name|decimalType
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromDecimalPageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetStringReader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"string"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|as
argument_list|(
name|LogicalTypeAnnotation
operator|.
name|stringType
argument_list|()
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromStringPageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetDecimalReaderFromBinaryPrimitive
parameter_list|()
throws|throws
name|Exception
block|{
name|TypeInfo
name|hiveTypeInfo
init|=
operator|new
name|DecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|as
argument_list|(
name|LogicalTypeAnnotation
operator|.
name|decimalType
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromDecimalPageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetBinaryReaderNoOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"string"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|DefaultParquetDataColumnReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetBinaryReaderJsonOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"binary"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BINARY
argument_list|)
operator|.
name|as
argument_list|(
name|LogicalTypeAnnotation
operator|.
name|jsonType
argument_list|()
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|DefaultParquetDataColumnReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetIntReader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"int"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT32
argument_list|)
operator|.
name|as
argument_list|(
name|LogicalTypeAnnotation
operator|.
name|intType
argument_list|(
literal|32
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromInt32PageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetIntReaderNoOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"int"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT32
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromInt32PageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetInt64ReaderNoOriginalType
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"bigint"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT64
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromInt64PageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetInt64Reader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"bigint"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT64
argument_list|)
operator|.
name|as
argument_list|(
name|LogicalTypeAnnotation
operator|.
name|intType
argument_list|(
literal|64
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromInt64PageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetFloatReader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"float"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|FLOAT
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromFloatPageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetDoubleReader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"double"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|DOUBLE
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromDoublePageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetInt96Reader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"timestamp"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|INT96
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromInt96PageReader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetBooleanReader
parameter_list|()
throws|throws
name|Exception
block|{
name|PrimitiveTypeInfo
name|hiveTypeInfo
init|=
operator|new
name|PrimitiveTypeInfo
argument_list|()
decl_stmt|;
name|hiveTypeInfo
operator|.
name|setTypeName
argument_list|(
literal|"boolean"
argument_list|)
expr_stmt|;
name|ParquetDataColumnReader
name|reader
init|=
name|ParquetDataColumnReaderFactory
operator|.
name|getDataColumnReaderByType
argument_list|(
name|Types
operator|.
name|optional
argument_list|(
name|PrimitiveTypeName
operator|.
name|BOOLEAN
argument_list|)
operator|.
name|named
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|hiveTypeInfo
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|reader
operator|instanceof
name|TypesFromBooleanPageReader
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


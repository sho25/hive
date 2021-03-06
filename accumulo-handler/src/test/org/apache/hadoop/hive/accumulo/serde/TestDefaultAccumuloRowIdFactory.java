begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
operator|.
name|serde
package|;
end_package

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
name|Properties
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
name|hive
operator|.
name|accumulo
operator|.
name|columns
operator|.
name|ColumnEncoding
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
name|accumulo
operator|.
name|columns
operator|.
name|ColumnMapper
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
name|serde
operator|.
name|serdeConstants
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
name|SerDeException
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
name|lazy
operator|.
name|LazyObjectBase
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
name|lazy
operator|.
name|LazySerDeParameters
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
name|lazy
operator|.
name|LazyString
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyMapObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazySimpleStructObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyIntObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyPrimitiveObjectInspectorFactory
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyStringObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|junit
operator|.
name|Assert
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
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TestDefaultAccumuloRowIdFactory
block|{
annotation|@
name|Test
specifier|public
name|void
name|testCorrectPrimitiveInspectors
parameter_list|()
throws|throws
name|SerDeException
block|{
name|AccumuloSerDe
name|accumuloSerDe
init|=
operator|new
name|AccumuloSerDe
argument_list|()
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|AccumuloSerDeParameters
operator|.
name|COLUMN_MAPPINGS
argument_list|,
literal|":rowID,cf:cq"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"row,col"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"string,int"
argument_list|)
expr_stmt|;
name|accumuloSerDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|AccumuloRowIdFactory
name|factory
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getRowIdFactory
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getHiveColumnTypes
argument_list|()
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getColumnMapper
argument_list|()
decl_stmt|;
name|LazySerDeParameters
name|serDeParams
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getSerDeParameters
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|OIs
init|=
name|accumuloSerDe
operator|.
name|getColumnObjectInspectors
argument_list|(
name|columnTypes
argument_list|,
name|serDeParams
argument_list|,
name|mapper
operator|.
name|getColumnMappings
argument_list|()
argument_list|,
name|factory
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|OIs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LazyStringObjectInspector
operator|.
name|class
argument_list|,
name|OIs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LazyIntObjectInspector
operator|.
name|class
argument_list|,
name|OIs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCorrectComplexInspectors
parameter_list|()
throws|throws
name|SerDeException
block|{
name|AccumuloSerDe
name|accumuloSerDe
init|=
operator|new
name|AccumuloSerDe
argument_list|()
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|AccumuloSerDeParameters
operator|.
name|COLUMN_MAPPINGS
argument_list|,
literal|":rowID,cf:cq"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"row,col"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"struct<col1:int,col2:int>,map<string,string>"
argument_list|)
expr_stmt|;
name|accumuloSerDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|AccumuloRowIdFactory
name|factory
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getRowIdFactory
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getHiveColumnTypes
argument_list|()
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getColumnMapper
argument_list|()
decl_stmt|;
name|LazySerDeParameters
name|serDeParams
init|=
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
operator|.
name|getSerDeParameters
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|OIs
init|=
name|accumuloSerDe
operator|.
name|getColumnObjectInspectors
argument_list|(
name|columnTypes
argument_list|,
name|serDeParams
argument_list|,
name|mapper
operator|.
name|getColumnMappings
argument_list|()
argument_list|,
name|factory
argument_list|)
decl_stmt|;
comment|// Expect the correct OIs
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|OIs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LazySimpleStructObjectInspector
operator|.
name|class
argument_list|,
name|OIs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|LazyMapObjectInspector
operator|.
name|class
argument_list|,
name|OIs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|LazySimpleStructObjectInspector
name|structOI
init|=
operator|(
name|LazySimpleStructObjectInspector
operator|)
name|OIs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|int
operator|)
name|structOI
operator|.
name|getSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|LazyMapObjectInspector
name|mapOI
init|=
operator|(
name|LazyMapObjectInspector
operator|)
name|OIs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|int
operator|)
name|mapOI
operator|.
name|getItemSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
operator|(
name|int
operator|)
name|mapOI
operator|.
name|getKeyValueSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBinaryStringRowId
parameter_list|()
throws|throws
name|SerDeException
block|{
name|AccumuloSerDe
name|accumuloSerDe
init|=
operator|new
name|AccumuloSerDe
argument_list|()
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|AccumuloSerDeParameters
operator|.
name|COLUMN_MAPPINGS
argument_list|,
literal|":rowID,cf:cq"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"row,col"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"string,string"
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|AccumuloSerDeParameters
operator|.
name|DEFAULT_STORAGE_TYPE
argument_list|,
name|ColumnEncoding
operator|.
name|BINARY
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|accumuloSerDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|DefaultAccumuloRowIdFactory
name|rowIdFactory
init|=
operator|new
name|DefaultAccumuloRowIdFactory
argument_list|()
decl_stmt|;
name|rowIdFactory
operator|.
name|init
argument_list|(
name|accumuloSerDe
operator|.
name|getParams
argument_list|()
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|LazyStringObjectInspector
name|oi
init|=
name|LazyPrimitiveObjectInspectorFactory
operator|.
name|getLazyStringObjectInspector
argument_list|(
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|'\\'
argument_list|)
decl_stmt|;
name|LazyObjectBase
name|lazyObj
init|=
name|rowIdFactory
operator|.
name|createRowId
argument_list|(
name|oi
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|lazyObj
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|LazyString
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|lazyObj
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


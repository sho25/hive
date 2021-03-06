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
name|columns
package|;
end_package

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
name|Iterator
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
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|AccumuloHiveConstants
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
name|serde
operator|.
name|TooManyAccumuloColumnsException
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TestColumnMapper
block|{
annotation|@
name|Test
specifier|public
name|void
name|testNormalMapping
parameter_list|()
throws|throws
name|TooManyAccumuloColumnsException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rawMappings
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ROWID
argument_list|,
literal|"cf:cq"
argument_list|,
literal|"cf:_"
argument_list|,
literal|"cf:qual"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"row"
argument_list|,
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|Arrays
operator|.
expr|<
name|TypeInfo
operator|>
name|asList
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
operator|new
name|ColumnMapper
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COMMA
argument_list|)
operator|.
name|join
argument_list|(
name|rawMappings
argument_list|)
argument_list|,
name|ColumnEncoding
operator|.
name|STRING
operator|.
name|getName
argument_list|()
argument_list|,
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|mappings
init|=
name|mapper
operator|.
name|getColumnMappings
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rawMappings
operator|.
name|size
argument_list|()
argument_list|,
name|mappings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|mappings
operator|.
name|size
argument_list|()
argument_list|,
name|mapper
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Compare the Mapper get at offset method to the list of mappings
name|Iterator
argument_list|<
name|String
argument_list|>
name|rawIter
init|=
name|rawMappings
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|ColumnMapping
argument_list|>
name|iter
init|=
name|mappings
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|mappings
operator|.
name|size
argument_list|()
operator|&&
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|rawMapping
init|=
name|rawIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|ColumnMapping
name|mapping
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|ColumnMapping
name|mappingByOffset
init|=
name|mapper
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|mapping
argument_list|,
name|mappingByOffset
argument_list|)
expr_stmt|;
comment|// Ensure that we get the right concrete ColumnMapping
if|if
condition|(
name|AccumuloHiveConstants
operator|.
name|ROWID
operator|.
name|equals
argument_list|(
name|rawMapping
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveAccumuloRowIdColumnMapping
operator|.
name|class
argument_list|,
name|mapping
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveAccumuloColumnMapping
operator|.
name|class
argument_list|,
name|mapping
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|mapper
operator|.
name|getRowIdOffset
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mapper
operator|.
name|hasRowIdMapping
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testMultipleRowIDsFails
parameter_list|()
throws|throws
name|TooManyAccumuloColumnsException
block|{
operator|new
name|ColumnMapper
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ROWID
operator|+
name|AccumuloHiveConstants
operator|.
name|COMMA
operator|+
name|AccumuloHiveConstants
operator|.
name|ROWID
argument_list|,
literal|null
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"row"
argument_list|,
literal|"row2"
argument_list|)
argument_list|,
name|Arrays
operator|.
expr|<
name|TypeInfo
operator|>
name|asList
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetMappingFromHiveColumn
parameter_list|()
throws|throws
name|TooManyAccumuloColumnsException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|hiveColumns
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"rowid"
argument_list|,
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|Arrays
operator|.
expr|<
name|TypeInfo
operator|>
name|asList
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rawMappings
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ROWID
argument_list|,
literal|"cf:cq"
argument_list|,
literal|"cf:_"
argument_list|,
literal|"cf:qual"
argument_list|)
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
operator|new
name|ColumnMapper
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COMMA
argument_list|)
operator|.
name|join
argument_list|(
name|rawMappings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|hiveColumns
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hiveColumns
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|hiveColumn
init|=
name|hiveColumns
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|,
name|accumuloMapping
init|=
name|rawMappings
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ColumnMapping
name|mapping
init|=
name|mapper
operator|.
name|getColumnMappingForHiveColumn
argument_list|(
name|hiveColumns
argument_list|,
name|hiveColumn
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|accumuloMapping
argument_list|,
name|mapping
operator|.
name|getMappingSpec
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTypesString
parameter_list|()
throws|throws
name|TooManyAccumuloColumnsException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|hiveColumns
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"rowid"
argument_list|,
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rawMappings
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ROWID
argument_list|,
literal|"cf:cq"
argument_list|,
literal|"cf:_"
argument_list|,
literal|"cf:qual"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|Arrays
operator|.
expr|<
name|TypeInfo
operator|>
name|asList
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
operator|new
name|ColumnMapper
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COMMA
argument_list|)
operator|.
name|join
argument_list|(
name|rawMappings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|hiveColumns
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
name|String
name|typeString
init|=
name|mapper
operator|.
name|getTypesString
argument_list|()
decl_stmt|;
name|String
index|[]
name|types
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|typeString
argument_list|,
name|AccumuloHiveConstants
operator|.
name|COLON
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|rawMappings
operator|.
name|size
argument_list|()
argument_list|,
name|types
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultBinary
parameter_list|()
throws|throws
name|TooManyAccumuloColumnsException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|hiveColumns
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"rowid"
argument_list|,
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|,
literal|"col4"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rawMappings
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ROWID
argument_list|,
literal|"cf:cq"
argument_list|,
literal|"cf:_#s"
argument_list|,
literal|"cf:qual#s"
argument_list|,
literal|"cf:qual2"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|Arrays
operator|.
expr|<
name|TypeInfo
operator|>
name|asList
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
operator|new
name|ColumnMapper
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COMMA
argument_list|)
operator|.
name|join
argument_list|(
name|rawMappings
argument_list|)
argument_list|,
name|ColumnEncoding
operator|.
name|BINARY
operator|.
name|getName
argument_list|()
argument_list|,
name|hiveColumns
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|mappings
init|=
name|mapper
operator|.
name|getColumnMappings
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|mappings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|STRING
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|STRING
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|getColumnType
argument_list|()
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
name|TooManyAccumuloColumnsException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|hiveColumns
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"rowid"
argument_list|,
literal|"col1"
argument_list|,
literal|"col2"
argument_list|,
literal|"col3"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|Arrays
operator|.
expr|<
name|TypeInfo
operator|>
name|asList
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|getMapTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
argument_list|,
name|TypeInfoFactory
operator|.
name|getMapTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
argument_list|,
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rawMappings
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ROWID
argument_list|,
literal|"cf1:*"
argument_list|,
literal|"cf2:2*"
argument_list|,
literal|"cq3:bar\\*"
argument_list|)
decl_stmt|;
name|ColumnMapper
name|mapper
init|=
operator|new
name|ColumnMapper
argument_list|(
name|Joiner
operator|.
name|on
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COMMA
argument_list|)
operator|.
name|join
argument_list|(
name|rawMappings
argument_list|)
argument_list|,
name|ColumnEncoding
operator|.
name|BINARY
operator|.
name|getName
argument_list|()
argument_list|,
name|hiveColumns
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnMapping
argument_list|>
name|mappings
init|=
name|mapper
operator|.
name|getColumnMappings
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|mappings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveAccumuloRowIdColumnMapping
operator|.
name|class
argument_list|,
name|mappings
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
name|HiveAccumuloMapColumnMapping
operator|.
name|class
argument_list|,
name|mappings
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HiveAccumuloMapColumnMapping
operator|.
name|class
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|2
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
name|HiveAccumuloColumnMapping
operator|.
name|class
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|HiveAccumuloRowIdColumnMapping
name|row
init|=
operator|(
name|HiveAccumuloRowIdColumnMapping
operator|)
name|mappings
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
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|row
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|hiveColumns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|row
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|row
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|HiveAccumuloMapColumnMapping
name|map
init|=
operator|(
name|HiveAccumuloMapColumnMapping
operator|)
name|mappings
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
literal|"cf1"
argument_list|,
name|map
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|map
operator|.
name|getColumnQualifierPrefix
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|map
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|hiveColumns
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|map
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|map
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|=
operator|(
name|HiveAccumuloMapColumnMapping
operator|)
name|mappings
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cf2"
argument_list|,
name|map
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"2"
argument_list|,
name|map
operator|.
name|getColumnQualifierPrefix
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|map
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|hiveColumns
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|map
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|map
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
name|HiveAccumuloColumnMapping
name|column
init|=
operator|(
name|HiveAccumuloColumnMapping
operator|)
name|mappings
operator|.
name|get
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cq3"
argument_list|,
name|column
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"bar*"
argument_list|,
name|column
operator|.
name|getColumnQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ColumnEncoding
operator|.
name|BINARY
argument_list|,
name|column
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|hiveColumns
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|,
name|column
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|column
operator|.
name|getColumnType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


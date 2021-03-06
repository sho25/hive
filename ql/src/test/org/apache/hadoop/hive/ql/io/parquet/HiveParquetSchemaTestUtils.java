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
name|Type
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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|assertNull
import|;
end_import

begin_class
specifier|public
class|class
name|HiveParquetSchemaTestUtils
block|{
specifier|public
specifier|static
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
specifier|public
specifier|static
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
comment|/**    * Only use if Configuration/HiveConf not needed for converting schema.    */
specifier|public
specifier|static
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
name|actualSchema
parameter_list|)
throws|throws
name|Exception
block|{
name|testConversion
argument_list|(
name|columnNamesStr
argument_list|,
name|columnsTypeStr
argument_list|,
name|actualSchema
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
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
name|actualSchema
parameter_list|,
specifier|final
name|Configuration
name|conf
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
argument_list|,
name|conf
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
name|actualSchema
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
name|actualSchema
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
name|LogicalTypeAnnotation
name|expectedLogicalType
init|=
name|expectedFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getLogicalTypeAnnotation
argument_list|()
decl_stmt|;
name|LogicalTypeAnnotation
name|actualLogicalType
init|=
name|actualFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getLogicalTypeAnnotation
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Logical type annotations of the field do not match"
argument_list|,
name|expectedLogicalType
argument_list|,
name|actualLogicalType
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|testLogicalTypeAnnotation
parameter_list|(
name|String
name|hiveColumnType
parameter_list|,
name|String
name|hiveColumnName
parameter_list|,
name|LogicalTypeAnnotation
name|expectedLogicalType
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalTypeAnnotation
argument_list|>
name|expectedLogicalTypeForColumn
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expectedLogicalTypeForColumn
operator|.
name|put
argument_list|(
name|hiveColumnName
argument_list|,
name|expectedLogicalType
argument_list|)
expr_stmt|;
name|testLogicalTypeAnnotations
argument_list|(
name|hiveColumnName
argument_list|,
name|hiveColumnType
argument_list|,
name|expectedLogicalTypeForColumn
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|testLogicalTypeAnnotations
parameter_list|(
specifier|final
name|String
name|hiveColumnNames
parameter_list|,
specifier|final
name|String
name|hiveColumnTypes
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LogicalTypeAnnotation
argument_list|>
name|expectedLogicalTypes
parameter_list|,
name|Configuration
name|conf
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
argument_list|,
name|conf
argument_list|)
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
name|Type
name|actualField
range|:
name|actualFields
control|)
block|{
name|LogicalTypeAnnotation
name|expectedLogicalType
init|=
name|expectedLogicalTypes
operator|.
name|get
argument_list|(
name|actualField
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|LogicalTypeAnnotation
name|actualLogicalType
init|=
name|actualField
operator|.
name|getLogicalTypeAnnotation
argument_list|()
decl_stmt|;
if|if
condition|(
name|expectedLogicalType
operator|!=
literal|null
condition|)
block|{
name|assertNotNull
argument_list|(
literal|"The logical type annotation cannot be null."
argument_list|,
name|actualLogicalType
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Logical type annotations of the field do not match"
argument_list|,
name|expectedLogicalType
argument_list|,
name|actualLogicalType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
literal|"The logical type annotation must be null."
argument_list|,
name|actualLogicalType
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


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
name|serde
package|;
end_package

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|ql
operator|.
name|metadata
operator|.
name|Table
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
name|optimizer
operator|.
name|FieldNode
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
name|AbstractSerDe
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
name|ColumnProjectionUtils
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
name|SerDeSpec
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
name|SerDeStats
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
name|SerDeUtils
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
name|ParquetHiveRecord
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
name|objectinspector
operator|.
name|ObjectInspector
operator|.
name|Category
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
name|StructObjectInspector
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
name|StructTypeInfo
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
name|hadoop
operator|.
name|io
operator|.
name|ArrayWritable
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
name|io
operator|.
name|Text
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
name|io
operator|.
name|Writable
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
name|hadoop
operator|.
name|ParquetOutputFormat
import|;
end_import

begin_comment
comment|/**  *  * A ParquetHiveSerDe for Hive (with the deprecated package mapred)  *  */
end_comment

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|serdeConstants
operator|.
name|LIST_COLUMNS
block|,
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
block|,
name|ParquetOutputFormat
operator|.
name|COMPRESSION
block|}
argument_list|)
specifier|public
class|class
name|ParquetHiveSerDe
extends|extends
name|AbstractSerDe
block|{
specifier|public
specifier|static
specifier|final
name|Text
name|MAP_KEY
init|=
operator|new
name|Text
argument_list|(
literal|"key"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|MAP_VALUE
init|=
operator|new
name|Text
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|MAP
init|=
operator|new
name|Text
argument_list|(
literal|"map"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|ARRAY
init|=
operator|new
name|Text
argument_list|(
literal|"bag"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Text
name|LIST
init|=
operator|new
name|Text
argument_list|(
literal|"list"
argument_list|)
decl_stmt|;
comment|// Map precision to the number bytes needed for binary conversion.
specifier|public
specifier|static
specifier|final
name|int
name|PRECISION_TO_BYTE_COUNT
index|[]
init|=
operator|new
name|int
index|[
literal|38
index|]
decl_stmt|;
static|static
block|{
for|for
control|(
name|int
name|prec
init|=
literal|1
init|;
name|prec
operator|<=
literal|38
condition|;
name|prec
operator|++
control|)
block|{
comment|// Estimated number of bytes needed.
name|PRECISION_TO_BYTE_COUNT
index|[
name|prec
operator|-
literal|1
index|]
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|Math
operator|.
name|log
argument_list|(
name|Math
operator|.
name|pow
argument_list|(
literal|10
argument_list|,
name|prec
argument_list|)
operator|-
literal|1
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
operator|+
literal|1
operator|)
operator|/
literal|8
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ObjectInspector
name|objInspector
decl_stmt|;
specifier|private
name|ParquetHiveRecord
name|parquetRow
decl_stmt|;
specifier|public
name|ParquetHiveSerDe
parameter_list|()
block|{
name|parquetRow
operator|=
operator|new
name|ParquetHiveRecord
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|void
name|initialize
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
comment|// Get column names and sort order
specifier|final
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
specifier|final
name|String
name|columnNameDelimiter
init|=
name|tbl
operator|.
name|containsKey
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
condition|?
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|COLUMN_NAME_DELIMITER
argument_list|)
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|SerDeUtils
operator|.
name|COMMA
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnNameProperty
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
name|columnNameProperty
operator|.
name|split
argument_list|(
name|columnNameDelimiter
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnTypeProperty
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
name|columnTypeProperty
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|columnNames
operator|.
name|size
argument_list|()
operator|!=
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"ParquetHiveSerde initialization failed. Number of column "
operator|+
literal|"name and column type differs. columnNames = "
operator|+
name|columnNames
operator|+
literal|", columnTypes = "
operator|+
name|columnTypes
argument_list|)
throw|;
block|}
comment|// Create row related objects
name|StructTypeInfo
name|completeTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
name|StructTypeInfo
name|prunedTypeInfo
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|String
name|rawPrunedColumnPaths
init|=
name|conf
operator|.
name|get
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_NESTED_COLUMN_PATH_CONF_STR
argument_list|)
decl_stmt|;
if|if
condition|(
name|rawPrunedColumnPaths
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|prunedColumnPaths
init|=
name|processRawPrunedPaths
argument_list|(
name|rawPrunedColumnPaths
argument_list|)
decl_stmt|;
name|prunedTypeInfo
operator|=
name|pruneFromPaths
argument_list|(
name|completeTypeInfo
argument_list|,
name|prunedColumnPaths
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|objInspector
operator|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
name|completeTypeInfo
argument_list|,
name|prunedTypeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
specifier|final
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|blob
operator|instanceof
name|ArrayWritable
condition|)
block|{
return|return
name|blob
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|objInspector
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|ParquetHiveRecord
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
specifier|final
name|Object
name|obj
parameter_list|,
specifier|final
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
operator|!
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|Category
operator|.
name|STRUCT
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Cannot serialize "
operator|+
name|objInspector
operator|.
name|getCategory
argument_list|()
operator|+
literal|". Can only serialize a struct"
argument_list|)
throw|;
block|}
name|parquetRow
operator|.
name|value
operator|=
name|obj
expr_stmt|;
name|parquetRow
operator|.
name|inspector
operator|=
operator|(
name|StructObjectInspector
operator|)
name|objInspector
expr_stmt|;
return|return
name|parquetRow
return|;
block|}
comment|/**    * Return null for Parquet format and stats is collected in ParquetRecordWriterWrapper when writer gets    * closed.    *    * @return null    */
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * @param table    * @return true if the table has the parquet serde defined    */
specifier|public
specifier|static
name|boolean
name|isParquetTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
return|return
name|table
operator|==
literal|null
condition|?
literal|false
else|:
name|ParquetHiveSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|table
operator|.
name|getSerializationLib
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Given a list of raw pruned paths separated by ',', return a list of merged pruned paths.    * For instance, if the 'prunedPaths' is "s.a, s, s", this returns ["s"].    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|processRawPrunedPaths
parameter_list|(
name|String
name|prunedPaths
parameter_list|)
block|{
name|List
argument_list|<
name|FieldNode
argument_list|>
name|fieldNodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|p
range|:
name|prunedPaths
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|fieldNodes
operator|=
name|FieldNode
operator|.
name|mergeFieldNodes
argument_list|(
name|fieldNodes
argument_list|,
name|FieldNode
operator|.
name|fromPath
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|prunedPathList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldNode
name|fn
range|:
name|fieldNodes
control|)
block|{
name|prunedPathList
operator|.
name|addAll
argument_list|(
name|fn
operator|.
name|toPaths
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|prunedPathList
return|;
block|}
comment|/**    * Given a complete struct type info and pruned paths containing selected fields    * from the type info, return a pruned struct type info only with the selected fields.    *    * For instance, if 'originalTypeInfo' is: s:struct<a:struct<b:int, c:boolean>, d:string>    *   and 'prunedPaths' is ["s.a.b,s.d"], then the result will be:    *   s:struct<a:struct<b:int>, d:string>    *    * @param originalTypeInfo the complete struct type info    * @param prunedPaths a string representing the pruned paths, separated by ','    * @return the pruned struct type info    */
specifier|private
specifier|static
name|StructTypeInfo
name|pruneFromPaths
parameter_list|(
name|StructTypeInfo
name|originalTypeInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|prunedPaths
parameter_list|)
block|{
name|PrunedStructTypeInfo
name|prunedTypeInfo
init|=
operator|new
name|PrunedStructTypeInfo
argument_list|(
name|originalTypeInfo
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|path
range|:
name|prunedPaths
control|)
block|{
name|pruneFromSinglePath
argument_list|(
name|prunedTypeInfo
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
return|return
name|prunedTypeInfo
operator|.
name|prune
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|void
name|pruneFromSinglePath
parameter_list|(
name|PrunedStructTypeInfo
name|prunedInfo
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|prunedInfo
operator|!=
literal|null
argument_list|,
literal|"PrunedStructTypeInfo for path '"
operator|+
name|path
operator|+
literal|"' should not be null"
argument_list|)
expr_stmt|;
name|int
name|index
init|=
name|path
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
name|path
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|String
name|fieldName
init|=
name|path
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|prunedInfo
operator|.
name|markSelected
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|index
operator|<
name|path
operator|.
name|length
argument_list|()
condition|)
block|{
name|pruneFromSinglePath
argument_list|(
name|prunedInfo
operator|.
name|getChild
argument_list|(
name|fieldName
argument_list|)
argument_list|,
name|path
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|PrunedStructTypeInfo
block|{
specifier|final
name|StructTypeInfo
name|typeInfo
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PrunedStructTypeInfo
argument_list|>
name|children
decl_stmt|;
specifier|final
name|boolean
index|[]
name|selected
decl_stmt|;
name|PrunedStructTypeInfo
parameter_list|(
name|StructTypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
name|this
operator|.
name|children
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|selected
operator|=
operator|new
name|boolean
index|[
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|TypeInfo
name|ti
init|=
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|ti
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|STRUCT
condition|)
block|{
name|this
operator|.
name|children
operator|.
name|put
argument_list|(
name|typeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toLowerCase
argument_list|()
argument_list|,
operator|new
name|PrunedStructTypeInfo
argument_list|(
operator|(
name|StructTypeInfo
operator|)
name|ti
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|PrunedStructTypeInfo
name|getChild
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|children
operator|.
name|get
argument_list|(
name|fieldName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
name|void
name|markSelected
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|typeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|typeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|equalsIgnoreCase
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|selected
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
name|StructTypeInfo
name|prune
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|newNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|newTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|oldNames
init|=
name|typeInfo
operator|.
name|getAllStructFieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|oldTypes
init|=
name|typeInfo
operator|.
name|getAllStructFieldTypeInfos
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
name|oldNames
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|String
name|fn
init|=
name|oldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|selected
index|[
name|i
index|]
condition|)
block|{
name|newNames
operator|.
name|add
argument_list|(
name|fn
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|containsKey
argument_list|(
name|fn
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
name|newTypes
operator|.
name|add
argument_list|(
name|children
operator|.
name|get
argument_list|(
name|fn
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|prune
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newTypes
operator|.
name|add
argument_list|(
name|oldTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|newNames
argument_list|,
name|newTypes
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


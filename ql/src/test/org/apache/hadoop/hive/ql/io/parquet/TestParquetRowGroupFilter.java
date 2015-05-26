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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|fs
operator|.
name|Path
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
name|exec
operator|.
name|Utilities
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
name|ParquetRecordReaderWrapper
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
name|serde
operator|.
name|ObjectArrayWritableObjectInspector
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
name|plan
operator|.
name|*
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPGreaterThan
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|JobConf
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
name|Before
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
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|RecordConsumer
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|MessageTypeParser
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

begin_class
specifier|public
class|class
name|TestParquetRowGroupFilter
extends|extends
name|AbstractTestParquetDirect
block|{
name|JobConf
name|conf
decl_stmt|;
name|String
name|columnNames
decl_stmt|;
name|String
name|columnTypes
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|initConf
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|JobConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRowGroupFilterTakeEffect
parameter_list|()
throws|throws
name|Exception
block|{
comment|// define schema
name|columnNames
operator|=
literal|"intCol"
expr_stmt|;
name|columnTypes
operator|=
literal|"int"
expr_stmt|;
name|StructObjectInspector
name|inspector
init|=
name|getObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
decl_stmt|;
name|MessageType
name|fileSchema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message hive_schema {\n"
operator|+
literal|"  optional int32 intCol;\n"
operator|+
literal|"}\n"
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_COLUMN_NAMES_CONF_STR
argument_list|,
literal|"intCol"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"columns"
argument_list|,
literal|"intCol"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"columns.types"
argument_list|,
literal|"int"
argument_list|)
expr_stmt|;
comment|// create Parquet file with specific data
name|Path
name|testPath
init|=
name|writeDirect
argument_list|(
literal|"RowGroupFilterTakeEffect"
argument_list|,
name|fileSchema
argument_list|,
operator|new
name|DirectWriter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|RecordConsumer
name|consumer
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|consumer
operator|.
name|startMessage
argument_list|()
expr_stmt|;
name|consumer
operator|.
name|startField
argument_list|(
literal|"int"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|addInteger
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|endField
argument_list|(
literal|"int"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|consumer
operator|.
name|endMessage
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
comment|//> 50
name|GenericUDF
name|udf
init|=
operator|new
name|GenericUDFOPGreaterThan
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|ExprNodeColumnDesc
name|columnDesc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
literal|"intCol"
argument_list|,
literal|"T"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeConstantDesc
name|constantDesc
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|50
argument_list|)
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|columnDesc
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
name|constantDesc
argument_list|)
expr_stmt|;
name|ExprNodeGenericFuncDesc
name|genericFuncDesc
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|inspector
argument_list|,
name|udf
argument_list|,
name|children
argument_list|)
decl_stmt|;
name|String
name|searchArgumentStr
init|=
name|Utilities
operator|.
name|serializeExpression
argument_list|(
name|genericFuncDesc
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_EXPR_CONF_STR
argument_list|,
name|searchArgumentStr
argument_list|)
expr_stmt|;
name|ParquetRecordReaderWrapper
name|recordReader
init|=
operator|(
name|ParquetRecordReaderWrapper
operator|)
operator|new
name|MapredParquetInputFormat
argument_list|()
operator|.
name|getRecordReader
argument_list|(
operator|new
name|FileSplit
argument_list|(
name|testPath
argument_list|,
literal|0
argument_list|,
name|fileLength
argument_list|(
name|testPath
argument_list|)
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"row group is not filtered correctly"
argument_list|,
literal|1
argument_list|,
name|recordReader
operator|.
name|getFiltedBlocks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|//> 100
name|constantDesc
operator|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|children
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|constantDesc
argument_list|)
expr_stmt|;
name|genericFuncDesc
operator|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|inspector
argument_list|,
name|udf
argument_list|,
name|children
argument_list|)
expr_stmt|;
name|searchArgumentStr
operator|=
name|Utilities
operator|.
name|serializeExpression
argument_list|(
name|genericFuncDesc
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|TableScanDesc
operator|.
name|FILTER_EXPR_CONF_STR
argument_list|,
name|searchArgumentStr
argument_list|)
expr_stmt|;
name|recordReader
operator|=
operator|(
name|ParquetRecordReaderWrapper
operator|)
operator|new
name|MapredParquetInputFormat
argument_list|()
operator|.
name|getRecordReader
argument_list|(
operator|new
name|FileSplit
argument_list|(
name|testPath
argument_list|,
literal|0
argument_list|,
name|fileLength
argument_list|(
name|testPath
argument_list|)
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"row group is not filtered correctly"
argument_list|,
literal|0
argument_list|,
name|recordReader
operator|.
name|getFiltedBlocks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ObjectArrayWritableObjectInspector
name|getObjectInspector
parameter_list|(
specifier|final
name|String
name|columnNames
parameter_list|,
specifier|final
name|String
name|columnTypes
parameter_list|)
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypeList
init|=
name|createHiveTypeInfoFrom
argument_list|(
name|columnTypes
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnNameList
init|=
name|createHiveColumnsFrom
argument_list|(
name|columnNames
argument_list|)
decl_stmt|;
name|StructTypeInfo
name|rowTypeInfo
init|=
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNameList
argument_list|,
name|columnTypeList
argument_list|)
decl_stmt|;
return|return
operator|new
name|ObjectArrayWritableObjectInspector
argument_list|(
name|rowTypeInfo
argument_list|)
return|;
block|}
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
block|}
end_class

end_unit


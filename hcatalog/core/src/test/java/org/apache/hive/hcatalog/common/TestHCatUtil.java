begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|permission
operator|.
name|FsAction
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
name|permission
operator|.
name|FsPermission
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
name|metastore
operator|.
name|TableType
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|Order
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
name|metastore
operator|.
name|api
operator|.
name|SerDeInfo
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
name|metastore
operator|.
name|api
operator|.
name|StorageDescriptor
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
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
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
name|collect
operator|.
name|Lists
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
name|collect
operator|.
name|Maps
import|;
end_import

begin_class
specifier|public
class|class
name|TestHCatUtil
block|{
annotation|@
name|Test
specifier|public
name|void
name|testFsPermissionOperation
parameter_list|()
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|permsCode
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
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
literal|8
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|8
condition|;
name|j
operator|++
control|)
block|{
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
literal|8
condition|;
name|k
operator|++
control|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"0"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|j
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|k
argument_list|)
expr_stmt|;
name|Integer
name|code
init|=
operator|(
operator|(
operator|(
name|i
operator|*
literal|8
operator|)
operator|+
name|j
operator|)
operator|*
literal|8
operator|)
operator|+
name|k
decl_stmt|;
name|String
name|perms
init|=
operator|(
operator|new
name|FsPermission
argument_list|(
name|Short
operator|.
name|decode
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
operator|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|permsCode
operator|.
name|containsKey
argument_list|(
name|perms
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"permissions("
operator|+
name|perms
operator|+
literal|") mapped to multiple codes"
argument_list|,
name|code
argument_list|,
name|permsCode
operator|.
name|get
argument_list|(
name|perms
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|permsCode
operator|.
name|put
argument_list|(
name|perms
argument_list|,
name|code
argument_list|)
expr_stmt|;
name|assertFsPermissionTransformationIsGood
argument_list|(
name|perms
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|assertFsPermissionTransformationIsGood
parameter_list|(
name|String
name|perms
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|perms
argument_list|,
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-"
operator|+
name|perms
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
name|testValidateMorePermissive
parameter_list|()
block|{
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|ALL
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|READ
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|WRITE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|READ_WRITE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|WRITE_EXECUTE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertConsistentFsPermissionBehaviour
argument_list|(
name|FsAction
operator|.
name|NONE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertConsistentFsPermissionBehaviour
parameter_list|(
name|FsAction
name|base
parameter_list|,
name|boolean
name|versusAll
parameter_list|,
name|boolean
name|versusNone
parameter_list|,
name|boolean
name|versusX
parameter_list|,
name|boolean
name|versusR
parameter_list|,
name|boolean
name|versusW
parameter_list|,
name|boolean
name|versusRX
parameter_list|,
name|boolean
name|versusRW
parameter_list|,
name|boolean
name|versusWX
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusAll
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusX
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|EXECUTE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusNone
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusR
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|READ
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusRX
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusRW
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|READ_WRITE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusW
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|WRITE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|versusWX
operator|==
name|HCatUtil
operator|.
name|validateMorePermissive
argument_list|(
name|base
argument_list|,
name|FsAction
operator|.
name|WRITE_EXECUTE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExecutePermissionsCheck
parameter_list|()
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|ALL
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|EXECUTE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|READ_EXECUTE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|WRITE_EXECUTE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|READ
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|WRITE
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|HCatUtil
operator|.
name|validateExecuteBitPresentIfReadOrWrite
argument_list|(
name|FsAction
operator|.
name|READ_WRITE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableSchemaWithPtnColsApi
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Check the schema of a table with one field& no partition keys.
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"username"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|,
literal|"location"
argument_list|,
literal|"org.apache.hadoop.mapred.TextInputFormat"
argument_list|,
literal|"org.apache.hadoop.mapred.TextOutputFormat"
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|,
operator|new
name|SerDeInfo
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Table
name|apiTable
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Table
argument_list|(
literal|"test_tblname"
argument_list|,
literal|"test_dbname"
argument_list|,
literal|"test_owner"
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
literal|"viewOriginalText"
argument_list|,
literal|"viewExpandedText"
argument_list|,
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|apiTable
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|expectedHCatSchema
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"username"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|HCatSchema
argument_list|(
name|expectedHCatSchema
argument_list|)
argument_list|,
name|HCatUtil
operator|.
name|getTableSchemaWithPtnCols
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add a partition key& ensure its reflected in the schema.
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partitionKeys
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"dt"
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|setPartitionKeys
argument_list|(
name|partitionKeys
argument_list|)
expr_stmt|;
name|expectedHCatSchema
operator|.
name|add
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"dt"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|HCatSchema
argument_list|(
name|expectedHCatSchema
argument_list|)
argument_list|,
name|HCatUtil
operator|.
name|getTableSchemaWithPtnCols
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Hive represents tables in two ways:    *<ul>    *<li>org.apache.hadoop.hive.metastore.api.Table - exactly whats stored in the metastore</li>    *<li>org.apache.hadoop.hive.ql.metadata.Table - adds business logic over api.Table</li>    *</ul>    * Here we check SerDe-reported fields are included in the table schema.    */
annotation|@
name|Test
specifier|public
name|void
name|testGetTableSchemaWithPtnColsSerDeReportedFields
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_CLASS
argument_list|,
literal|"org.apache.hadoop.hive.serde2.thrift.test.IntString"
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|put
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"org.apache.thrift.protocol.TBinaryProtocol"
argument_list|)
expr_stmt|;
name|SerDeInfo
name|serDeInfo
init|=
operator|new
name|SerDeInfo
argument_list|(
literal|null
argument_list|,
literal|"org.apache.hadoop.hive.serde2.thrift.ThriftDeserializer"
argument_list|,
name|parameters
argument_list|)
decl_stmt|;
comment|// StorageDescriptor has an empty list of fields - SerDe will report them.
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
argument_list|,
literal|"location"
argument_list|,
literal|"org.apache.hadoop.mapred.TextInputFormat"
argument_list|,
literal|"org.apache.hadoop.mapred.TextOutputFormat"
argument_list|,
literal|false
argument_list|,
operator|-
literal|1
argument_list|,
name|serDeInfo
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Table
name|apiTable
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Table
argument_list|(
literal|"test_tblname"
argument_list|,
literal|"test_dbname"
argument_list|,
literal|"test_owner"
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
literal|"viewOriginalText"
argument_list|,
literal|"viewExpandedText"
argument_list|,
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|apiTable
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|expectedHCatSchema
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"myint"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|INT
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|HCatFieldSchema
argument_list|(
literal|"mystring"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|HCatFieldSchema
argument_list|(
literal|"underscore_int"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|INT
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|HCatSchema
argument_list|(
name|expectedHCatSchema
argument_list|)
argument_list|,
name|HCatUtil
operator|.
name|getTableSchemaWithPtnCols
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


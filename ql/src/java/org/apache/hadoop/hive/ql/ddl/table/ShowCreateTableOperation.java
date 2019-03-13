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
name|ddl
operator|.
name|table
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
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|lang
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
name|common
operator|.
name|StatsSetupConst
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
name|Warehouse
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
name|SkewedInfo
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
name|ddl
operator|.
name|DDLOperation
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
name|HiveException
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
name|parse
operator|.
name|BaseSemanticAnalyzer
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
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|stringtemplate
operator|.
name|v4
operator|.
name|ST
import|;
end_import

begin_comment
comment|/**  * Operation process showing the creation of a table.  */
end_comment

begin_class
specifier|public
class|class
name|ShowCreateTableOperation
extends|extends
name|DDLOperation
block|{
specifier|private
specifier|static
specifier|final
name|String
name|EXTERNAL
init|=
literal|"external"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEMPORARY
init|=
literal|"temporary"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LIST_COLUMNS
init|=
literal|"columns"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TBL_COMMENT
init|=
literal|"tbl_comment"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LIST_PARTITIONS
init|=
literal|"partitions"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SORT_BUCKET
init|=
literal|"sort_bucket"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SKEWED_INFO
init|=
literal|"tbl_skewedinfo"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_FORMAT
init|=
literal|"row_format"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TBL_LOCATION
init|=
literal|"tbl_location"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TBL_PROPERTIES
init|=
literal|"tbl_properties"
decl_stmt|;
specifier|private
specifier|final
name|ShowCreateTableDesc
name|desc
decl_stmt|;
specifier|public
name|ShowCreateTableOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|ShowCreateTableDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// get the create table statement for the table and populate the output
try|try
init|(
name|DataOutputStream
name|outStream
init|=
name|DDLUtils
operator|.
name|getOutputStream
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getResFile
argument_list|()
argument_list|)
argument_list|,
name|context
argument_list|)
init|)
block|{
return|return
name|showCreateTable
argument_list|(
name|outStream
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|int
name|showCreateTable
parameter_list|(
name|DataOutputStream
name|outStream
parameter_list|)
throws|throws
name|HiveException
block|{
name|boolean
name|needsLocation
init|=
literal|true
decl_stmt|;
name|StringBuilder
name|createTabCommand
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Table
name|tbl
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|duplicateProps
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|needsLocation
operator|=
name|CreateTableOperation
operator|.
name|doesTableNeedLocation
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isView
argument_list|()
condition|)
block|{
name|String
name|createTabStmt
init|=
literal|"CREATE VIEW `"
operator|+
name|desc
operator|.
name|getTableName
argument_list|()
operator|+
literal|"` AS "
operator|+
name|tbl
operator|.
name|getViewExpandedText
argument_list|()
decl_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|createTabStmt
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"CREATE<"
operator|+
name|TEMPORARY
operator|+
literal|"><"
operator|+
name|EXTERNAL
operator|+
literal|">TABLE `"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
operator|+
literal|"`(\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|LIST_COLUMNS
operator|+
literal|">)\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|TBL_COMMENT
operator|+
literal|">\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|LIST_PARTITIONS
operator|+
literal|">\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|SORT_BUCKET
operator|+
literal|">\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|SKEWED_INFO
operator|+
literal|">\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|ROW_FORMAT
operator|+
literal|">\n"
argument_list|)
expr_stmt|;
if|if
condition|(
name|needsLocation
condition|)
block|{
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"LOCATION\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|TBL_LOCATION
operator|+
literal|">\n"
argument_list|)
expr_stmt|;
block|}
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"TBLPROPERTIES (\n"
argument_list|)
expr_stmt|;
name|createTabCommand
operator|.
name|append
argument_list|(
literal|"<"
operator|+
name|TBL_PROPERTIES
operator|+
literal|">)\n"
argument_list|)
expr_stmt|;
name|ST
name|createTabStmt
init|=
operator|new
name|ST
argument_list|(
name|createTabCommand
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// For cases where the table is temporary
name|String
name|tblTemp
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isTemporary
argument_list|()
condition|)
block|{
name|duplicateProps
operator|.
name|add
argument_list|(
literal|"TEMPORARY"
argument_list|)
expr_stmt|;
name|tblTemp
operator|=
literal|"TEMPORARY "
expr_stmt|;
block|}
comment|// For cases where the table is external
name|String
name|tblExternal
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|EXTERNAL_TABLE
condition|)
block|{
name|duplicateProps
operator|.
name|add
argument_list|(
literal|"EXTERNAL"
argument_list|)
expr_stmt|;
name|tblExternal
operator|=
literal|"EXTERNAL "
expr_stmt|;
block|}
comment|// Columns
name|String
name|tblColumns
init|=
literal|""
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|tbl
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
name|String
name|columnDesc
init|=
literal|"  `"
operator|+
name|col
operator|.
name|getName
argument_list|()
operator|+
literal|"` "
operator|+
name|col
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|col
operator|.
name|getComment
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|columnDesc
operator|=
name|columnDesc
operator|+
literal|" COMMENT '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|col
operator|.
name|getComment
argument_list|()
argument_list|)
operator|+
literal|"'"
expr_stmt|;
block|}
name|columns
operator|.
name|add
argument_list|(
name|columnDesc
argument_list|)
expr_stmt|;
block|}
name|tblColumns
operator|=
name|StringUtils
operator|.
name|join
argument_list|(
name|columns
argument_list|,
literal|", \n"
argument_list|)
expr_stmt|;
comment|// Table comment
name|String
name|tblComment
init|=
literal|""
decl_stmt|;
name|String
name|tabComment
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"comment"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tabComment
operator|!=
literal|null
condition|)
block|{
name|duplicateProps
operator|.
name|add
argument_list|(
literal|"comment"
argument_list|)
expr_stmt|;
name|tblComment
operator|=
literal|"COMMENT '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|tabComment
argument_list|)
operator|+
literal|"'"
expr_stmt|;
block|}
comment|// Partitions
name|String
name|tblPartitions
init|=
literal|""
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partKeys
init|=
name|tbl
operator|.
name|getPartitionKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|partKeys
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tblPartitions
operator|+=
literal|"PARTITIONED BY ( \n"
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|partKey
range|:
name|partKeys
control|)
block|{
name|String
name|partColDesc
init|=
literal|"  `"
operator|+
name|partKey
operator|.
name|getName
argument_list|()
operator|+
literal|"` "
operator|+
name|partKey
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|partKey
operator|.
name|getComment
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|partColDesc
operator|=
name|partColDesc
operator|+
literal|" COMMENT '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|partKey
operator|.
name|getComment
argument_list|()
argument_list|)
operator|+
literal|"'"
expr_stmt|;
block|}
name|partCols
operator|.
name|add
argument_list|(
name|partColDesc
argument_list|)
expr_stmt|;
block|}
name|tblPartitions
operator|+=
name|StringUtils
operator|.
name|join
argument_list|(
name|partCols
argument_list|,
literal|", \n"
argument_list|)
expr_stmt|;
name|tblPartitions
operator|+=
literal|")"
expr_stmt|;
block|}
comment|// Clusters (Buckets)
name|String
name|tblSortBucket
init|=
literal|""
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|buckCols
init|=
name|tbl
operator|.
name|getBucketCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|buckCols
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|duplicateProps
operator|.
name|add
argument_list|(
literal|"SORTBUCKETCOLSPREFIX"
argument_list|)
expr_stmt|;
name|tblSortBucket
operator|+=
literal|"CLUSTERED BY ( \n  "
expr_stmt|;
name|tblSortBucket
operator|+=
name|StringUtils
operator|.
name|join
argument_list|(
name|buckCols
argument_list|,
literal|", \n  "
argument_list|)
expr_stmt|;
name|tblSortBucket
operator|+=
literal|") \n"
expr_stmt|;
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
init|=
name|tbl
operator|.
name|getSortCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|sortCols
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|tblSortBucket
operator|+=
literal|"SORTED BY ( \n"
expr_stmt|;
comment|// Order
name|List
argument_list|<
name|String
argument_list|>
name|sortKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Order
name|sortCol
range|:
name|sortCols
control|)
block|{
name|String
name|sortKeyDesc
init|=
literal|"  "
operator|+
name|sortCol
operator|.
name|getCol
argument_list|()
operator|+
literal|" "
decl_stmt|;
if|if
condition|(
name|sortCol
operator|.
name|getOrder
argument_list|()
operator|==
name|BaseSemanticAnalyzer
operator|.
name|HIVE_COLUMN_ORDER_ASC
condition|)
block|{
name|sortKeyDesc
operator|=
name|sortKeyDesc
operator|+
literal|"ASC"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sortCol
operator|.
name|getOrder
argument_list|()
operator|==
name|BaseSemanticAnalyzer
operator|.
name|HIVE_COLUMN_ORDER_DESC
condition|)
block|{
name|sortKeyDesc
operator|=
name|sortKeyDesc
operator|+
literal|"DESC"
expr_stmt|;
block|}
name|sortKeys
operator|.
name|add
argument_list|(
name|sortKeyDesc
argument_list|)
expr_stmt|;
block|}
name|tblSortBucket
operator|+=
name|StringUtils
operator|.
name|join
argument_list|(
name|sortKeys
argument_list|,
literal|", \n"
argument_list|)
expr_stmt|;
name|tblSortBucket
operator|+=
literal|") \n"
expr_stmt|;
block|}
name|tblSortBucket
operator|+=
literal|"INTO "
operator|+
name|tbl
operator|.
name|getNumBuckets
argument_list|()
operator|+
literal|" BUCKETS"
expr_stmt|;
block|}
comment|// Skewed Info
name|StringBuilder
name|tblSkewedInfo
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|SkewedInfo
name|skewedInfo
init|=
name|tbl
operator|.
name|getSkewedInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|skewedInfo
operator|!=
literal|null
operator|&&
operator|!
name|skewedInfo
operator|.
name|getSkewedColNames
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tblSkewedInfo
operator|.
name|append
argument_list|(
literal|"SKEWED BY ("
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|skewedInfo
operator|.
name|getSkewedColNames
argument_list|()
argument_list|,
literal|","
argument_list|)
operator|+
literal|")\n"
argument_list|)
expr_stmt|;
name|tblSkewedInfo
operator|.
name|append
argument_list|(
literal|"  ON ("
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colValueList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|colValues
range|:
name|skewedInfo
operator|.
name|getSkewedColValues
argument_list|()
control|)
block|{
name|colValueList
operator|.
name|add
argument_list|(
literal|"('"
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|colValues
argument_list|,
literal|"','"
argument_list|)
operator|+
literal|"')"
argument_list|)
expr_stmt|;
block|}
name|tblSkewedInfo
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|colValueList
argument_list|,
literal|","
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isStoredAsSubDirectories
argument_list|()
condition|)
block|{
name|tblSkewedInfo
operator|.
name|append
argument_list|(
literal|"\n  STORED AS DIRECTORIES"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Row format (SerDe)
name|StringBuilder
name|tblRowFormat
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StorageDescriptor
name|sd
init|=
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
decl_stmt|;
name|SerDeInfo
name|serdeInfo
init|=
name|sd
operator|.
name|getSerdeInfo
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeParams
init|=
name|serdeInfo
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|tblRowFormat
operator|.
name|append
argument_list|(
literal|"ROW FORMAT SERDE \n"
argument_list|)
expr_stmt|;
name|tblRowFormat
operator|.
name|append
argument_list|(
literal|"  '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|serdeInfo
operator|.
name|getSerializationLib
argument_list|()
argument_list|)
operator|+
literal|"' \n"
argument_list|)
expr_stmt|;
if|if
condition|(
name|tbl
operator|.
name|getStorageHandler
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// If serialization.format property has the default value, it will not to be included in
comment|// SERDE properties
if|if
condition|(
name|Warehouse
operator|.
name|DEFAULT_SERIALIZATION_FORMAT
operator|.
name|equals
argument_list|(
name|serdeParams
operator|.
name|get
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
argument_list|)
condition|)
block|{
name|serdeParams
operator|.
name|remove
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|serdeParams
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|appendSerdeParams
argument_list|(
name|tblRowFormat
argument_list|,
name|serdeParams
argument_list|)
operator|.
name|append
argument_list|(
literal|" \n"
argument_list|)
expr_stmt|;
block|}
name|tblRowFormat
operator|.
name|append
argument_list|(
literal|"STORED AS INPUTFORMAT \n  '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|sd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
operator|+
literal|"' \n"
argument_list|)
expr_stmt|;
name|tblRowFormat
operator|.
name|append
argument_list|(
literal|"OUTPUTFORMAT \n  '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|sd
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|duplicateProps
operator|.
name|add
argument_list|(
name|META_TABLE_STORAGE
argument_list|)
expr_stmt|;
name|tblRowFormat
operator|.
name|append
argument_list|(
literal|"STORED BY \n  '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|tbl
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|META_TABLE_STORAGE
argument_list|)
argument_list|)
operator|+
literal|"' \n"
argument_list|)
expr_stmt|;
comment|// SerDe Properties
if|if
condition|(
operator|!
name|serdeParams
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|appendSerdeParams
argument_list|(
name|tblRowFormat
argument_list|,
name|serdeInfo
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|tblLocation
init|=
literal|"  '"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|sd
operator|.
name|getLocation
argument_list|()
argument_list|)
operator|+
literal|"'"
decl_stmt|;
comment|// Table properties
name|duplicateProps
operator|.
name|addAll
argument_list|(
name|StatsSetupConst
operator|.
name|TABLE_PARAMS_STATS_KEYS
argument_list|)
expr_stmt|;
name|String
name|tblProperties
init|=
name|DDLUtils
operator|.
name|propertiesToString
argument_list|(
name|tbl
operator|.
name|getParameters
argument_list|()
argument_list|,
name|duplicateProps
argument_list|)
decl_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|TEMPORARY
argument_list|,
name|tblTemp
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|EXTERNAL
argument_list|,
name|tblExternal
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|LIST_COLUMNS
argument_list|,
name|tblColumns
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|TBL_COMMENT
argument_list|,
name|tblComment
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|LIST_PARTITIONS
argument_list|,
name|tblPartitions
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|SORT_BUCKET
argument_list|,
name|tblSortBucket
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|SKEWED_INFO
argument_list|,
name|tblSkewedInfo
argument_list|)
expr_stmt|;
name|createTabStmt
operator|.
name|add
argument_list|(
name|ROW_FORMAT
argument_list|,
name|tblRowFormat
argument_list|)
expr_stmt|;
comment|// Table location should not be printed with hbase backed tables
if|if
condition|(
name|needsLocation
condition|)
block|{
name|createTabStmt
operator|.
name|add
argument_list|(
name|TBL_LOCATION
argument_list|,
name|tblLocation
argument_list|)
expr_stmt|;
block|}
name|createTabStmt
operator|.
name|add
argument_list|(
name|TBL_PROPERTIES
argument_list|,
name|tblProperties
argument_list|)
expr_stmt|;
name|outStream
operator|.
name|write
argument_list|(
name|createTabStmt
operator|.
name|render
argument_list|()
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"show create table: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|public
specifier|static
name|StringBuilder
name|appendSerdeParams
parameter_list|(
name|StringBuilder
name|builder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeParam
parameter_list|)
block|{
name|serdeParam
operator|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|serdeParam
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"WITH SERDEPROPERTIES ( \n"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|serdeCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|serdeParam
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|serdeCols
operator|.
name|add
argument_list|(
literal|"  '"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"'='"
operator|+
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|serdeCols
argument_list|,
literal|", \n"
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit


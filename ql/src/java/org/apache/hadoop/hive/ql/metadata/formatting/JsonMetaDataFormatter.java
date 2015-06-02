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
name|metadata
operator|.
name|formatting
package|;
end_package

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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
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
name|Set
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|FileStatus
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
name|FileSystem
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
name|conf
operator|.
name|HiveConf
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
name|ColumnStatisticsObj
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|Partition
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
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_comment
comment|/**  * Format table and index information for machine readability using  * json.  */
end_comment

begin_class
specifier|public
class|class
name|JsonMetaDataFormatter
implements|implements
name|MetaDataFormatter
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|JsonMetaDataFormatter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Convert the map to a JSON string.    */
specifier|private
name|void
name|asJson
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|data
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
operator|new
name|ObjectMapper
argument_list|()
operator|.
name|writeValue
argument_list|(
name|out
argument_list|,
name|data
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unable to convert to json"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Write an error message.    */
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|String
name|msg
parameter_list|,
name|int
name|errorCode
parameter_list|,
name|String
name|sqlState
parameter_list|)
throws|throws
name|HiveException
block|{
name|error
argument_list|(
name|out
argument_list|,
name|msg
argument_list|,
name|errorCode
argument_list|,
name|sqlState
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|error
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|int
name|errorCode
parameter_list|,
name|String
name|sqlState
parameter_list|,
name|String
name|errorDetail
parameter_list|)
throws|throws
name|HiveException
block|{
name|MapBuilder
name|mb
init|=
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"error"
argument_list|,
name|errorMessage
argument_list|)
decl_stmt|;
if|if
condition|(
name|errorDetail
operator|!=
literal|null
condition|)
block|{
name|mb
operator|.
name|put
argument_list|(
literal|"errorDetail"
argument_list|,
name|errorDetail
argument_list|)
expr_stmt|;
block|}
name|mb
operator|.
name|put
argument_list|(
literal|"errorCode"
argument_list|,
name|errorCode
argument_list|)
expr_stmt|;
if|if
condition|(
name|sqlState
operator|!=
literal|null
condition|)
block|{
name|mb
operator|.
name|put
argument_list|(
literal|"sqlState"
argument_list|,
name|sqlState
argument_list|)
expr_stmt|;
block|}
name|asJson
argument_list|(
name|out
argument_list|,
name|mb
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Show a list of tables.    */
annotation|@
name|Override
specifier|public
name|void
name|showTables
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|tables
parameter_list|)
throws|throws
name|HiveException
block|{
name|asJson
argument_list|(
name|out
argument_list|,
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"tables"
argument_list|,
name|tables
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Describe table.    */
annotation|@
name|Override
specifier|public
name|void
name|describeTable
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|String
name|colPath
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Table
name|tbl
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|,
name|boolean
name|isFormatted
parameter_list|,
name|boolean
name|isExt
parameter_list|,
name|boolean
name|isPretty
parameter_list|,
name|boolean
name|isOutputPadded
parameter_list|,
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|colStats
parameter_list|)
throws|throws
name|HiveException
block|{
name|MapBuilder
name|builder
init|=
name|MapBuilder
operator|.
name|create
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"columns"
argument_list|,
name|makeColsUnformatted
argument_list|(
name|cols
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|isExt
condition|)
block|{
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"partitionInfo"
argument_list|,
name|part
operator|.
name|getTPartition
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"tableInfo"
argument_list|,
name|tbl
operator|.
name|getTTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|asJson
argument_list|(
name|out
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|makeColsUnformatted
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
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
name|res
operator|.
name|add
argument_list|(
name|makeOneColUnformatted
argument_list|(
name|col
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|makeOneColUnformatted
parameter_list|(
name|FieldSchema
name|col
parameter_list|)
block|{
return|return
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|col
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"type"
argument_list|,
name|col
operator|.
name|getType
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"comment"
argument_list|,
name|col
operator|.
name|getComment
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|showTableStatus
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|List
argument_list|<
name|Table
argument_list|>
name|tbls
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part
parameter_list|,
name|Partition
name|par
parameter_list|)
throws|throws
name|HiveException
block|{
name|asJson
argument_list|(
name|out
argument_list|,
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"tables"
argument_list|,
name|makeAllTableStatus
argument_list|(
name|db
argument_list|,
name|conf
argument_list|,
name|tbls
argument_list|,
name|part
argument_list|,
name|par
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|makeAllTableStatus
parameter_list|(
name|Hive
name|db
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|List
argument_list|<
name|Table
argument_list|>
name|tbls
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part
parameter_list|,
name|Partition
name|par
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Table
name|tbl
range|:
name|tbls
control|)
block|{
name|res
operator|.
name|add
argument_list|(
name|makeOneTableStatus
argument_list|(
name|tbl
argument_list|,
name|db
argument_list|,
name|conf
argument_list|,
name|part
argument_list|,
name|par
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
catch|catch
parameter_list|(
name|IOException
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
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|makeOneTableStatus
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part
parameter_list|,
name|Partition
name|par
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|String
name|tblLoc
init|=
literal|null
decl_stmt|;
name|String
name|inputFormattCls
init|=
literal|null
decl_stmt|;
name|String
name|outputFormattCls
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|par
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|par
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tblLoc
operator|=
name|par
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|inputFormattCls
operator|=
name|par
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|outputFormattCls
operator|=
name|par
operator|.
name|getOutputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|tbl
operator|.
name|getPath
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tblLoc
operator|=
name|tbl
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|inputFormattCls
operator|=
name|tbl
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|outputFormattCls
operator|=
name|tbl
operator|.
name|getOutputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|MapBuilder
name|builder
init|=
name|MapBuilder
operator|.
name|create
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"tableName"
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"owner"
argument_list|,
name|tbl
operator|.
name|getOwner
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|tblLoc
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"inputFormat"
argument_list|,
name|inputFormattCls
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"outputFormat"
argument_list|,
name|outputFormattCls
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"columns"
argument_list|,
name|makeColsUnformatted
argument_list|(
name|tbl
operator|.
name|getCols
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"partitioned"
argument_list|,
name|tbl
operator|.
name|isPartitioned
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"partitionColumns"
argument_list|,
name|makeColsUnformatted
argument_list|(
name|tbl
operator|.
name|getPartCols
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tbl
operator|.
name|getTableType
argument_list|()
operator|!=
name|TableType
operator|.
name|VIRTUAL_VIEW
condition|)
block|{
comment|//tbl.getPath() is null for views
name|putFileSystemsStats
argument_list|(
name|builder
argument_list|,
name|makeTableStatusLocations
argument_list|(
name|tbl
argument_list|,
name|db
argument_list|,
name|par
argument_list|)
argument_list|,
name|conf
argument_list|,
name|tbl
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|Path
argument_list|>
name|makeTableStatusLocations
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|Hive
name|db
parameter_list|,
name|Partition
name|par
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// output file system information
name|Path
name|tblPath
init|=
name|tbl
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
if|if
condition|(
name|par
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|Partition
name|curPart
range|:
name|db
operator|.
name|getPartitions
argument_list|(
name|tbl
argument_list|)
control|)
block|{
if|if
condition|(
name|curPart
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|locations
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|curPart
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|par
operator|.
name|getLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|locations
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|par
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|tblPath
operator|!=
literal|null
condition|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|tblPath
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|locations
return|;
block|}
comment|/**    * @param tblPath not NULL    * @throws IOException    */
comment|// Duplicates logic in TextMetaDataFormatter
specifier|private
name|void
name|putFileSystemsStats
parameter_list|(
name|MapBuilder
name|builder
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|locations
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Path
name|tblPath
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|totalFileSize
init|=
literal|0
decl_stmt|;
name|long
name|maxFileSize
init|=
literal|0
decl_stmt|;
name|long
name|minFileSize
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|lastAccessTime
init|=
literal|0
decl_stmt|;
name|long
name|lastUpdateTime
init|=
literal|0
decl_stmt|;
name|int
name|numOfFiles
init|=
literal|0
decl_stmt|;
name|boolean
name|unknown
init|=
literal|false
decl_stmt|;
name|FileSystem
name|fs
init|=
name|tblPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// in case all files in locations do not exist
try|try
block|{
name|FileStatus
name|tmpStatus
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|tblPath
argument_list|)
decl_stmt|;
name|lastAccessTime
operator|=
name|tmpStatus
operator|.
name|getAccessTime
argument_list|()
expr_stmt|;
name|lastUpdateTime
operator|=
name|tmpStatus
operator|.
name|getModificationTime
argument_list|()
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
name|warn
argument_list|(
literal|"Cannot access File System. File System status will be unknown: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|unknown
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|unknown
condition|)
block|{
for|for
control|(
name|Path
name|loc
range|:
name|locations
control|)
block|{
try|try
block|{
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|tblPath
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|loc
argument_list|)
decl_stmt|;
name|long
name|accessTime
init|=
name|status
operator|.
name|getAccessTime
argument_list|()
decl_stmt|;
name|long
name|updateTime
init|=
name|status
operator|.
name|getModificationTime
argument_list|()
decl_stmt|;
comment|// no matter loc is the table location or part location, it must be a
comment|// directory.
if|if
condition|(
operator|!
name|status
operator|.
name|isDir
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|accessTime
operator|>
name|lastAccessTime
condition|)
block|{
name|lastAccessTime
operator|=
name|accessTime
expr_stmt|;
block|}
if|if
condition|(
name|updateTime
operator|>
name|lastUpdateTime
condition|)
block|{
name|lastUpdateTime
operator|=
name|updateTime
expr_stmt|;
block|}
for|for
control|(
name|FileStatus
name|currentStatus
range|:
name|files
control|)
block|{
if|if
condition|(
name|currentStatus
operator|.
name|isDir
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|numOfFiles
operator|++
expr_stmt|;
name|long
name|fileLen
init|=
name|currentStatus
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|totalFileSize
operator|+=
name|fileLen
expr_stmt|;
if|if
condition|(
name|fileLen
operator|>
name|maxFileSize
condition|)
block|{
name|maxFileSize
operator|=
name|fileLen
expr_stmt|;
block|}
if|if
condition|(
name|fileLen
operator|<
name|minFileSize
condition|)
block|{
name|minFileSize
operator|=
name|fileLen
expr_stmt|;
block|}
name|accessTime
operator|=
name|currentStatus
operator|.
name|getAccessTime
argument_list|()
expr_stmt|;
name|updateTime
operator|=
name|currentStatus
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
if|if
condition|(
name|accessTime
operator|>
name|lastAccessTime
condition|)
block|{
name|lastAccessTime
operator|=
name|accessTime
expr_stmt|;
block|}
if|if
condition|(
name|updateTime
operator|>
name|lastUpdateTime
condition|)
block|{
name|lastUpdateTime
operator|=
name|updateTime
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
name|builder
operator|.
name|put
argument_list|(
literal|"totalNumberFiles"
argument_list|,
name|numOfFiles
argument_list|,
operator|!
name|unknown
argument_list|)
operator|.
name|put
argument_list|(
literal|"totalFileSize"
argument_list|,
name|totalFileSize
argument_list|,
operator|!
name|unknown
argument_list|)
operator|.
name|put
argument_list|(
literal|"maxFileSize"
argument_list|,
name|maxFileSize
argument_list|,
operator|!
name|unknown
argument_list|)
operator|.
name|put
argument_list|(
literal|"minFileSize"
argument_list|,
name|numOfFiles
operator|>
literal|0
condition|?
name|minFileSize
else|:
literal|0
argument_list|,
operator|!
name|unknown
argument_list|)
operator|.
name|put
argument_list|(
literal|"lastAccessTime"
argument_list|,
name|lastAccessTime
argument_list|,
operator|!
operator|(
name|unknown
operator|||
name|lastAccessTime
operator|<
literal|0
operator|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"lastUpdateTime"
argument_list|,
name|lastUpdateTime
argument_list|,
operator|!
name|unknown
argument_list|)
expr_stmt|;
block|}
comment|/**    * Show the table partitions.    */
annotation|@
name|Override
specifier|public
name|void
name|showTablePartitions
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|parts
parameter_list|)
throws|throws
name|HiveException
block|{
name|asJson
argument_list|(
name|out
argument_list|,
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"partitions"
argument_list|,
name|makeTablePartions
argument_list|(
name|parts
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|makeTablePartions
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|parts
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|part
range|:
name|parts
control|)
block|{
name|res
operator|.
name|add
argument_list|(
name|makeOneTablePartition
argument_list|(
name|part
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
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
comment|// This seems like a very wrong implementation.
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|makeOneTablePartition
parameter_list|(
name|String
name|partIdent
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|names
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
name|String
name|part
range|:
name|StringUtils
operator|.
name|split
argument_list|(
name|partIdent
argument_list|,
literal|"/"
argument_list|)
control|)
block|{
name|String
name|name
init|=
name|part
decl_stmt|;
name|String
name|val
init|=
literal|null
decl_stmt|;
name|String
index|[]
name|kv
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|part
argument_list|,
literal|"="
argument_list|,
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|kv
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|kv
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|kv
operator|.
name|length
operator|>
literal|1
condition|)
name|val
operator|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|kv
index|[
literal|1
index|]
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|names
operator|.
name|add
argument_list|(
name|name
operator|+
literal|"='"
operator|+
name|val
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|names
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|res
operator|.
name|add
argument_list|(
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"columnName"
argument_list|,
name|name
argument_list|)
operator|.
name|put
argument_list|(
literal|"columnValue"
argument_list|,
name|val
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|StringUtils
operator|.
name|join
argument_list|(
name|names
argument_list|,
literal|","
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"values"
argument_list|,
name|res
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Show a list of databases    */
annotation|@
name|Override
specifier|public
name|void
name|showDatabases
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|databases
parameter_list|)
throws|throws
name|HiveException
block|{
name|asJson
argument_list|(
name|out
argument_list|,
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"databases"
argument_list|,
name|databases
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Show the description of a database    */
annotation|@
name|Override
specifier|public
name|void
name|showDatabaseDescription
parameter_list|(
name|DataOutputStream
name|out
parameter_list|,
name|String
name|database
parameter_list|,
name|String
name|comment
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|ownerName
parameter_list|,
name|String
name|ownerType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
throws|throws
name|HiveException
block|{
name|MapBuilder
name|builder
init|=
name|MapBuilder
operator|.
name|create
argument_list|()
operator|.
name|put
argument_list|(
literal|"database"
argument_list|,
name|database
argument_list|)
operator|.
name|put
argument_list|(
literal|"comment"
argument_list|,
name|comment
argument_list|)
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|ownerName
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"owner"
argument_list|,
name|ownerName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|null
operator|!=
name|ownerType
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"ownerType"
argument_list|,
name|ownerType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|null
operator|!=
name|params
operator|&&
operator|!
name|params
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"params"
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|asJson
argument_list|(
name|out
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


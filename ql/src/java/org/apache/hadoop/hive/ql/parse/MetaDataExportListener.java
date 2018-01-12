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
name|parse
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
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|IHMSHandler
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
name|conf
operator|.
name|MetastoreConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|metastore
operator|.
name|MetaStorePreEventListener
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
name|InvalidOperationException
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
name|MetaException
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
name|NoSuchObjectException
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
name|metastore
operator|.
name|events
operator|.
name|PreDropTableEvent
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
name|events
operator|.
name|PreEventContext
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
name|events
operator|.
name|PreEventContext
operator|.
name|PreEventType
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * This class listens for drop events and, if set, exports the table's metadata as JSON to the trash  * of the user performing the drop  */
end_comment

begin_class
specifier|public
class|class
name|MetaDataExportListener
extends|extends
name|MetaStorePreEventListener
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MetaDataExportListener
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Configure the export listener */
specifier|public
name|MetaDataExportListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
comment|/** Export the metadata to a given path, and then move it to the user's trash */
specifier|private
name|void
name|export_meta_data
parameter_list|(
name|PreDropTableEvent
name|tableEvent
parameter_list|)
throws|throws
name|MetaException
block|{
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
name|Table
name|tbl
init|=
name|tableEvent
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|String
name|name
init|=
name|tbl
operator|.
name|getTableName
argument_list|()
decl_stmt|;
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
name|mTbl
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
name|ql
operator|.
name|metadata
operator|.
name|Table
argument_list|(
name|tbl
argument_list|)
decl_stmt|;
name|IHMSHandler
name|handler
init|=
name|tableEvent
operator|.
name|getHandler
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|handler
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|tblPath
init|=
operator|new
name|Path
argument_list|(
name|tbl
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|=
name|wh
operator|.
name|getFs
argument_list|(
name|tblPath
argument_list|)
expr_stmt|;
name|Date
name|now
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
name|SimpleDateFormat
name|sdf
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd-HH-mm-ss"
argument_list|)
decl_stmt|;
name|String
name|dateString
init|=
name|sdf
operator|.
name|format
argument_list|(
name|now
argument_list|)
decl_stmt|;
name|String
name|exportPathString
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|METADATA_EXPORT_LOCATION
argument_list|)
decl_stmt|;
name|boolean
name|moveMetadataToTrash
init|=
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|MOVE_EXPORTED_METADATA_TO_TRASH
argument_list|)
decl_stmt|;
name|Path
name|exportPath
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|exportPathString
operator|!=
literal|null
operator|&&
name|exportPathString
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|exportPath
operator|=
name|fs
operator|.
name|getHomeDirectory
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|exportPath
operator|=
operator|new
name|Path
argument_list|(
name|exportPathString
argument_list|)
expr_stmt|;
block|}
name|Path
name|metaPath
init|=
operator|new
name|Path
argument_list|(
name|exportPath
argument_list|,
name|name
operator|+
literal|"."
operator|+
name|dateString
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Exporting the metadata of table "
operator|+
name|tbl
operator|.
name|toString
argument_list|()
operator|+
literal|" to path "
operator|+
name|metaPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|metaPath
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
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|Path
name|outFile
init|=
operator|new
name|Path
argument_list|(
name|metaPath
argument_list|,
name|name
operator|+
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
decl_stmt|;
try|try
block|{
name|SessionState
operator|.
name|getConsole
argument_list|()
operator|.
name|printInfo
argument_list|(
literal|"Beginning metadata export"
argument_list|)
expr_stmt|;
name|EximUtil
operator|.
name|createExportDump
argument_list|(
name|fs
argument_list|,
name|outFile
argument_list|,
name|mTbl
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|,
name|MetaDataExportListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|moveMetadataToTrash
operator|==
literal|true
condition|)
block|{
name|wh
operator|.
name|deleteDir
argument_list|(
name|metaPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Listen for an event; if it is a DROP_TABLE event, call export_meta_data    * */
annotation|@
name|Override
specifier|public
name|void
name|onEvent
parameter_list|(
name|PreEventContext
name|context
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidOperationException
block|{
if|if
condition|(
name|context
operator|.
name|getEventType
argument_list|()
operator|==
name|PreEventType
operator|.
name|DROP_TABLE
condition|)
block|{
name|export_meta_data
argument_list|(
operator|(
name|PreDropTableEvent
operator|)
name|context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


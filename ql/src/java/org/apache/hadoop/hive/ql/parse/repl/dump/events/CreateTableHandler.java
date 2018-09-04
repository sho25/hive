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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|events
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
name|api
operator|.
name|NotificationEvent
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
name|messaging
operator|.
name|CreateTableMessage
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
name|EximUtil
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
name|repl
operator|.
name|DumpType
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
name|repl
operator|.
name|dump
operator|.
name|Utils
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
name|repl
operator|.
name|load
operator|.
name|DumpMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedWriter
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
name|OutputStreamWriter
import|;
end_import

begin_class
class|class
name|CreateTableHandler
extends|extends
name|AbstractEventHandler
block|{
name|CreateTableHandler
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
block|{
name|super
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|Context
name|withinContext
parameter_list|)
throws|throws
name|Exception
block|{
name|CreateTableMessage
name|ctm
init|=
name|deserializer
operator|.
name|getCreateTableMessage
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing#{} CREATE_TABLE message : {}"
argument_list|,
name|fromEventId
argument_list|()
argument_list|,
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
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
name|tobj
init|=
name|ctm
operator|.
name|getTableObj
argument_list|()
decl_stmt|;
if|if
condition|(
name|tobj
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Event#{} was a CREATE_TABLE_EVENT with no table listed"
argument_list|)
expr_stmt|;
return|return;
block|}
name|Table
name|qlMdTable
init|=
operator|new
name|Table
argument_list|(
name|tobj
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Utils
operator|.
name|shouldReplicate
argument_list|(
name|withinContext
operator|.
name|replicationSpec
argument_list|,
name|qlMdTable
argument_list|,
name|withinContext
operator|.
name|hiveConf
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|qlMdTable
operator|.
name|isView
argument_list|()
condition|)
block|{
name|withinContext
operator|.
name|replicationSpec
operator|.
name|setIsMetadataOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|Path
name|metaDataPath
init|=
operator|new
name|Path
argument_list|(
name|withinContext
operator|.
name|eventRoot
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
decl_stmt|;
name|EximUtil
operator|.
name|createExportDump
argument_list|(
name|metaDataPath
operator|.
name|getFileSystem
argument_list|(
name|withinContext
operator|.
name|hiveConf
argument_list|)
argument_list|,
name|metaDataPath
argument_list|,
name|qlMdTable
argument_list|,
literal|null
argument_list|,
name|withinContext
operator|.
name|replicationSpec
argument_list|,
name|withinContext
operator|.
name|hiveConf
argument_list|)
expr_stmt|;
name|Path
name|dataPath
init|=
operator|new
name|Path
argument_list|(
name|withinContext
operator|.
name|eventRoot
argument_list|,
literal|"data"
argument_list|)
decl_stmt|;
name|Iterable
argument_list|<
name|String
argument_list|>
name|files
init|=
name|ctm
operator|.
name|getFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
condition|)
block|{
comment|// encoded filename/checksum of files, write into _files
try|try
init|(
name|BufferedWriter
name|fileListWriter
init|=
name|writer
argument_list|(
name|withinContext
argument_list|,
name|dataPath
argument_list|)
init|)
block|{
for|for
control|(
name|String
name|file
range|:
name|files
control|)
block|{
name|fileListWriter
operator|.
name|write
argument_list|(
name|file
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|withinContext
operator|.
name|createDmd
argument_list|(
name|this
argument_list|)
operator|.
name|write
argument_list|()
expr_stmt|;
block|}
specifier|private
name|BufferedWriter
name|writer
parameter_list|(
name|Context
name|withinContext
parameter_list|,
name|Path
name|dataPath
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|dataPath
operator|.
name|getFileSystem
argument_list|(
name|withinContext
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|Path
name|filesPath
init|=
operator|new
name|Path
argument_list|(
name|dataPath
argument_list|,
name|EximUtil
operator|.
name|FILES_NAME
argument_list|)
decl_stmt|;
return|return
operator|new
name|BufferedWriter
argument_list|(
operator|new
name|OutputStreamWriter
argument_list|(
name|fs
operator|.
name|create
argument_list|(
name|filesPath
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DumpType
name|dumpType
parameter_list|()
block|{
return|return
name|DumpType
operator|.
name|EVENT_CREATE_TABLE
return|;
block|}
block|}
end_class

end_unit


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
name|InsertMessage
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
name|thrift
operator|.
name|TException
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|load
operator|.
name|DumpMetaData
import|;
end_import

begin_class
class|class
name|InsertHandler
extends|extends
name|AbstractEventHandler
block|{
name|InsertHandler
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
name|InsertMessage
name|insertMsg
init|=
name|deserializer
operator|.
name|getInsertMessage
argument_list|(
name|event
operator|.
name|getMessage
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
name|ql
operator|.
name|metadata
operator|.
name|Table
name|qlMdTable
init|=
name|tableObject
argument_list|(
name|withinContext
argument_list|,
name|insertMsg
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|insertMsg
operator|.
name|getPartitionKeyValues
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|qlPtns
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|qlMdTable
operator|.
name|isPartitioned
argument_list|()
operator|&&
operator|!
name|partSpec
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|qlPtns
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|withinContext
operator|.
name|db
operator|.
name|getPartition
argument_list|(
name|qlMdTable
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
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
comment|// Mark the replace type based on INSERT-INTO or INSERT_OVERWRITE operation
name|withinContext
operator|.
name|replicationSpec
operator|.
name|setIsReplace
argument_list|(
name|insertMsg
operator|.
name|isReplace
argument_list|()
argument_list|)
expr_stmt|;
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
name|qlPtns
argument_list|,
name|withinContext
operator|.
name|replicationSpec
argument_list|)
expr_stmt|;
name|Iterable
argument_list|<
name|String
argument_list|>
name|files
init|=
name|insertMsg
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
name|Path
name|dataPath
decl_stmt|;
if|if
condition|(
operator|(
literal|null
operator|==
name|qlPtns
operator|)
operator|||
name|qlPtns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|dataPath
operator|=
operator|new
name|Path
argument_list|(
name|withinContext
operator|.
name|eventRoot
argument_list|,
name|EximUtil
operator|.
name|DATA_PATH_NAME
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|/*          * Insert into/overwrite operation shall operate on one or more partitions or even partitions from multiple          * tables. But, Insert event is generated for each partition to which the data is inserted. So, qlPtns list          * will have only one entry.          */
assert|assert
operator|(
literal|1
operator|==
name|qlPtns
operator|.
name|size
argument_list|()
operator|)
assert|;
name|dataPath
operator|=
operator|new
name|Path
argument_list|(
name|withinContext
operator|.
name|eventRoot
argument_list|,
name|qlPtns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing#{} INSERT message : {}"
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
name|DumpMetaData
name|dmd
init|=
name|withinContext
operator|.
name|createDmd
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|dmd
operator|.
name|setPayload
argument_list|(
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|dmd
operator|.
name|write
argument_list|()
expr_stmt|;
block|}
specifier|private
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
name|tableObject
parameter_list|(
name|Context
name|withinContext
parameter_list|,
name|InsertMessage
name|insertMsg
parameter_list|)
throws|throws
name|TException
block|{
return|return
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
name|withinContext
operator|.
name|db
operator|.
name|getMSC
argument_list|()
operator|.
name|getTable
argument_list|(
name|insertMsg
operator|.
name|getDB
argument_list|()
argument_list|,
name|insertMsg
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|)
return|;
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
name|EVENT_INSERT
return|;
block|}
block|}
end_class

end_unit

begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|exec
operator|.
name|repl
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
name|primitives
operator|.
name|Ints
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
name|IMetaStoreClient
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
name|Database
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
name|Function
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
name|EventUtils
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
name|MessageFactory
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
name|event
operator|.
name|filters
operator|.
name|AndFilter
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
name|event
operator|.
name|filters
operator|.
name|DatabaseAndTableFilter
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
name|event
operator|.
name|filters
operator|.
name|EventBoundaryFilter
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
name|event
operator|.
name|filters
operator|.
name|MessageFormatFilter
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
name|DriverContext
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
name|Task
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
name|InvalidTableException
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
operator|.
name|TableSpec
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
name|ReplicationSpec
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
name|SemanticException
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
name|HiveWrapper
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
name|TableExport
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
name|dump
operator|.
name|events
operator|.
name|EventHandler
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
name|events
operator|.
name|EventHandlerFactory
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
name|io
operator|.
name|FunctionSerializer
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
name|io
operator|.
name|JsonWriter
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
name|api
operator|.
name|StageType
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
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|ReplDumpTask
extends|extends
name|Task
argument_list|<
name|ReplDumpWork
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|String
name|dumpSchema
init|=
literal|"dump_dir,last_repl_id#string,string"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FUNCTIONS_ROOT_DIR_NAME
init|=
literal|"_functions"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FUNCTION_METADATA_FILE_NAME
init|=
literal|"_metadata"
decl_stmt|;
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplDumpTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Logger
name|REPL_STATE_LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"ReplState"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"REPL_DUMP"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
try|try
block|{
name|Path
name|dumpRoot
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLDIR
argument_list|)
argument_list|,
name|getNextDumpDir
argument_list|()
argument_list|)
decl_stmt|;
name|DumpMetaData
name|dmd
init|=
operator|new
name|DumpMetaData
argument_list|(
name|dumpRoot
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Path
name|cmRoot
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPLCMDIR
argument_list|)
argument_list|)
decl_stmt|;
name|Long
name|lastReplId
decl_stmt|;
if|if
condition|(
name|work
operator|.
name|isBootStrapDump
argument_list|()
condition|)
block|{
name|lastReplId
operator|=
name|bootStrapDump
argument_list|(
name|dumpRoot
argument_list|,
name|dmd
argument_list|,
name|cmRoot
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lastReplId
operator|=
name|incrementalDump
argument_list|(
name|dumpRoot
argument_list|,
name|dmd
argument_list|,
name|cmRoot
argument_list|)
expr_stmt|;
block|}
name|prepareReturnValues
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|dumpRoot
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|lastReplId
argument_list|)
argument_list|)
argument_list|,
name|dumpSchema
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setException
argument_list|(
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
specifier|private
name|void
name|prepareReturnValues
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|,
name|String
name|schema
parameter_list|)
throws|throws
name|SemanticException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"prepareReturnValues : "
operator|+
name|schema
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|values
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"> "
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
name|Utils
operator|.
name|writeOutput
argument_list|(
name|values
argument_list|,
operator|new
name|Path
argument_list|(
name|work
operator|.
name|resultTempPath
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Long
name|incrementalDump
parameter_list|(
name|Path
name|dumpRoot
parameter_list|,
name|DumpMetaData
name|dmd
parameter_list|,
name|Path
name|cmRoot
parameter_list|)
throws|throws
name|Exception
block|{
name|Long
name|lastReplId
decl_stmt|;
comment|// get list of events matching dbPattern& tblPattern
comment|// go through each event, and dump out each event to a event-level dump dir inside dumproot
comment|// TODO : instead of simply restricting by message format, we should eventually
comment|// move to a jdbc-driver-stype registering of message format, and picking message
comment|// factory per event to decode. For now, however, since all messages have the
comment|// same factory, restricting by message format is effectively a guard against
comment|// older leftover data that would cause us problems.
name|work
operator|.
name|overrideEventTo
argument_list|(
name|getHive
argument_list|()
argument_list|)
expr_stmt|;
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|evFilter
init|=
operator|new
name|AndFilter
argument_list|(
operator|new
name|DatabaseAndTableFilter
argument_list|(
name|work
operator|.
name|dbNameOrPattern
argument_list|,
name|work
operator|.
name|tableNameOrPattern
argument_list|)
argument_list|,
operator|new
name|EventBoundaryFilter
argument_list|(
name|work
operator|.
name|eventFrom
argument_list|,
name|work
operator|.
name|eventTo
argument_list|)
argument_list|,
operator|new
name|MessageFormatFilter
argument_list|(
name|MessageFactory
operator|.
name|getInstance
argument_list|()
operator|.
name|getMessageFormat
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|EventUtils
operator|.
name|MSClientNotificationFetcher
name|evFetcher
init|=
operator|new
name|EventUtils
operator|.
name|MSClientNotificationFetcher
argument_list|(
name|getHive
argument_list|()
operator|.
name|getMSC
argument_list|()
argument_list|)
decl_stmt|;
name|EventUtils
operator|.
name|NotificationEventIterator
name|evIter
init|=
operator|new
name|EventUtils
operator|.
name|NotificationEventIterator
argument_list|(
name|evFetcher
argument_list|,
name|work
operator|.
name|eventFrom
argument_list|,
name|work
operator|.
name|maxEventLimit
argument_list|()
argument_list|,
name|evFilter
argument_list|)
decl_stmt|;
name|lastReplId
operator|=
name|work
operator|.
name|eventTo
expr_stmt|;
name|String
name|dbName
init|=
operator|(
literal|null
operator|!=
name|work
operator|.
name|dbNameOrPattern
operator|&&
operator|!
name|work
operator|.
name|dbNameOrPattern
operator|.
name|isEmpty
argument_list|()
operator|)
condition|?
name|work
operator|.
name|dbNameOrPattern
else|:
literal|"?"
decl_stmt|;
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Started Repl Dump for DB: {}, Dump Type: INCREMENTAL"
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
while|while
condition|(
name|evIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|NotificationEvent
name|ev
init|=
name|evIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|lastReplId
operator|=
name|ev
operator|.
name|getEventId
argument_list|()
expr_stmt|;
name|Path
name|evRoot
init|=
operator|new
name|Path
argument_list|(
name|dumpRoot
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|lastReplId
argument_list|)
argument_list|)
decl_stmt|;
name|dumpEvent
argument_list|(
name|ev
argument_list|,
name|evRoot
argument_list|,
name|cmRoot
argument_list|)
expr_stmt|;
block|}
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Completed Repl Dump for DB: {}"
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done dumping events, preparing to return {},{}"
argument_list|,
name|dumpRoot
operator|.
name|toUri
argument_list|()
argument_list|,
name|lastReplId
argument_list|)
expr_stmt|;
name|Utils
operator|.
name|writeOutput
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"incremental"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|work
operator|.
name|eventFrom
argument_list|)
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|lastReplId
argument_list|)
argument_list|)
argument_list|,
name|dmd
operator|.
name|getDumpFilePath
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|dmd
operator|.
name|setDump
argument_list|(
name|DumpType
operator|.
name|INCREMENTAL
argument_list|,
name|work
operator|.
name|eventFrom
argument_list|,
name|lastReplId
argument_list|,
name|cmRoot
argument_list|)
expr_stmt|;
name|dmd
operator|.
name|write
argument_list|()
expr_stmt|;
return|return
name|lastReplId
return|;
block|}
specifier|private
name|void
name|dumpEvent
parameter_list|(
name|NotificationEvent
name|ev
parameter_list|,
name|Path
name|evRoot
parameter_list|,
name|Path
name|cmRoot
parameter_list|)
throws|throws
name|Exception
block|{
name|EventHandler
operator|.
name|Context
name|context
init|=
operator|new
name|EventHandler
operator|.
name|Context
argument_list|(
name|evRoot
argument_list|,
name|cmRoot
argument_list|,
name|getHive
argument_list|()
argument_list|,
name|conf
argument_list|,
name|getNewEventOnlyReplicationSpec
argument_list|(
name|ev
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|EventHandlerFactory
operator|.
name|handlerFor
argument_list|(
name|ev
argument_list|)
operator|.
name|handle
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Dumped event with ID: {}, Type: {} and dumped metadata and data to path {}"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|ev
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|,
name|ev
operator|.
name|getEventType
argument_list|()
argument_list|,
name|evRoot
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ReplicationSpec
name|getNewEventOnlyReplicationSpec
parameter_list|(
name|Long
name|eventId
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReplicationSpec
name|rspec
init|=
name|getNewReplicationSpec
argument_list|(
name|eventId
operator|.
name|toString
argument_list|()
argument_list|,
name|eventId
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|rspec
operator|.
name|setIsIncrementalDump
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|rspec
return|;
block|}
specifier|private
name|Long
name|bootStrapDump
parameter_list|(
name|Path
name|dumpRoot
parameter_list|,
name|DumpMetaData
name|dmd
parameter_list|,
name|Path
name|cmRoot
parameter_list|)
throws|throws
name|Exception
block|{
comment|// bootstrap case
name|Long
name|bootDumpBeginReplId
init|=
name|getHive
argument_list|()
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|dbName
range|:
name|Utils
operator|.
name|matchesDb
argument_list|(
name|getHive
argument_list|()
argument_list|,
name|work
operator|.
name|dbNameOrPattern
argument_list|)
control|)
block|{
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Started analyzing Repl Dump for DB: {}, Dump Type: BOOTSTRAP"
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"ReplicationSemanticAnalyzer: analyzeReplDump dumping db: "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|Path
name|dbRoot
init|=
name|dumpDbMetadata
argument_list|(
name|dbName
argument_list|,
name|dumpRoot
argument_list|)
decl_stmt|;
name|dumpFunctionMetadata
argument_list|(
name|dbName
argument_list|,
name|dumpRoot
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|tblName
range|:
name|Utils
operator|.
name|matchesTbl
argument_list|(
name|getHive
argument_list|()
argument_list|,
name|dbName
argument_list|,
name|work
operator|.
name|tableNameOrPattern
argument_list|)
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"analyzeReplDump dumping table: "
operator|+
name|tblName
operator|+
literal|" to db root "
operator|+
name|dbRoot
operator|.
name|toUri
argument_list|()
argument_list|)
expr_stmt|;
name|dumpTable
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|,
name|dbRoot
argument_list|)
expr_stmt|;
block|}
block|}
name|Long
name|bootDumpEndReplId
init|=
name|getHive
argument_list|()
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Bootstrap object dump phase took from {} to {}"
argument_list|,
name|bootDumpBeginReplId
argument_list|,
name|bootDumpEndReplId
argument_list|)
expr_stmt|;
comment|// Now that bootstrap has dumped all objects related, we have to account for the changes
comment|// that occurred while bootstrap was happening - i.e. we have to look through all events
comment|// during the bootstrap period and consolidate them with our dump.
name|IMetaStoreClient
operator|.
name|NotificationFilter
name|evFilter
init|=
operator|new
name|DatabaseAndTableFilter
argument_list|(
name|work
operator|.
name|dbNameOrPattern
argument_list|,
name|work
operator|.
name|tableNameOrPattern
argument_list|)
decl_stmt|;
name|EventUtils
operator|.
name|MSClientNotificationFetcher
name|evFetcher
init|=
operator|new
name|EventUtils
operator|.
name|MSClientNotificationFetcher
argument_list|(
name|getHive
argument_list|()
operator|.
name|getMSC
argument_list|()
argument_list|)
decl_stmt|;
name|EventUtils
operator|.
name|NotificationEventIterator
name|evIter
init|=
operator|new
name|EventUtils
operator|.
name|NotificationEventIterator
argument_list|(
name|evFetcher
argument_list|,
name|bootDumpBeginReplId
argument_list|,
name|Ints
operator|.
name|checkedCast
argument_list|(
name|bootDumpEndReplId
operator|-
name|bootDumpBeginReplId
argument_list|)
operator|+
literal|1
argument_list|,
name|evFilter
argument_list|)
decl_stmt|;
comment|// Now we consolidate all the events that happenned during the objdump into the objdump
while|while
condition|(
name|evIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|NotificationEvent
name|ev
init|=
name|evIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|Path
name|eventRoot
init|=
operator|new
name|Path
argument_list|(
name|dumpRoot
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|ev
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// FIXME : implement consolidateEvent(..) similar to dumpEvent(ev,evRoot)
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Consolidation done, preparing to return {},{}->{}"
argument_list|,
name|dumpRoot
operator|.
name|toUri
argument_list|()
argument_list|,
name|bootDumpBeginReplId
argument_list|,
name|bootDumpEndReplId
argument_list|)
expr_stmt|;
name|dmd
operator|.
name|setDump
argument_list|(
name|DumpType
operator|.
name|BOOTSTRAP
argument_list|,
name|bootDumpBeginReplId
argument_list|,
name|bootDumpEndReplId
argument_list|,
name|cmRoot
argument_list|)
expr_stmt|;
name|dmd
operator|.
name|write
argument_list|()
expr_stmt|;
comment|// Set the correct last repl id to return to the user
return|return
name|bootDumpEndReplId
return|;
block|}
specifier|private
name|Path
name|dumpDbMetadata
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Path
name|dumpRoot
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|dbRoot
init|=
operator|new
name|Path
argument_list|(
name|dumpRoot
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
comment|// TODO : instantiating FS objects are generally costly. Refactor
name|FileSystem
name|fs
init|=
name|dbRoot
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|dumpPath
init|=
operator|new
name|Path
argument_list|(
name|dbRoot
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
decl_stmt|;
name|HiveWrapper
operator|.
name|Tuple
argument_list|<
name|Database
argument_list|>
name|database
init|=
operator|new
name|HiveWrapper
argument_list|(
name|getHive
argument_list|()
argument_list|,
name|dbName
argument_list|)
operator|.
name|database
argument_list|()
decl_stmt|;
name|EximUtil
operator|.
name|createDbExportDump
argument_list|(
name|fs
argument_list|,
name|dumpPath
argument_list|,
name|database
operator|.
name|object
argument_list|,
name|database
operator|.
name|replicationSpec
argument_list|)
expr_stmt|;
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Dumped DB metadata"
argument_list|)
expr_stmt|;
return|return
name|dbRoot
return|;
block|}
specifier|private
name|void
name|dumpTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Path
name|dbRoot
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|Hive
name|db
init|=
name|getHive
argument_list|()
decl_stmt|;
name|TableSpec
name|ts
init|=
operator|new
name|TableSpec
argument_list|(
name|db
argument_list|,
name|conf
argument_list|,
name|dbName
operator|+
literal|"."
operator|+
name|tblName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|TableExport
operator|.
name|Paths
name|exportPaths
init|=
operator|new
name|TableExport
operator|.
name|Paths
argument_list|(
name|work
operator|.
name|astRepresentationForErrorMsg
argument_list|,
name|dbRoot
argument_list|,
name|tblName
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|String
name|distCpDoAsUser
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DISTCP_DOAS_USER
argument_list|)
decl_stmt|;
operator|new
name|TableExport
argument_list|(
name|exportPaths
argument_list|,
name|ts
argument_list|,
name|getNewReplicationSpec
argument_list|()
argument_list|,
name|db
argument_list|,
name|distCpDoAsUser
argument_list|,
name|conf
argument_list|)
operator|.
name|write
argument_list|()
expr_stmt|;
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Analyzed dump for table/view: {}.{} and dumping metadata and data to path {}"
argument_list|,
name|dbName
argument_list|,
name|tblName
argument_list|,
name|exportPaths
operator|.
name|exportRootDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|te
parameter_list|)
block|{
comment|// Bootstrap dump shouldn't fail if the table is dropped/renamed while dumping it.
comment|// Just log a debug message and skip it.
name|LOG
operator|.
name|debug
argument_list|(
name|te
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ReplicationSpec
name|getNewReplicationSpec
parameter_list|()
throws|throws
name|TException
block|{
name|ReplicationSpec
name|rspec
init|=
name|getNewReplicationSpec
argument_list|(
literal|"replv2"
argument_list|,
literal|"will-be-set"
argument_list|)
decl_stmt|;
name|rspec
operator|.
name|setCurrentReplicationState
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|getHive
argument_list|()
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|rspec
return|;
block|}
specifier|private
name|ReplicationSpec
name|getNewReplicationSpec
parameter_list|(
name|String
name|evState
parameter_list|,
name|String
name|objState
parameter_list|)
block|{
return|return
operator|new
name|ReplicationSpec
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|evState
argument_list|,
name|objState
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|private
name|String
name|getNextDumpDir
parameter_list|()
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
condition|)
block|{
comment|// make it easy to write .q unit tests, instead of unique id generation.
comment|// however, this does mean that in writing tests, we have to be aware that
comment|// repl dump will clash with prior dumps, and thus have to clean up properly.
if|if
condition|(
name|ReplDumpWork
operator|.
name|testInjectDumpDir
operator|==
literal|null
condition|)
block|{
return|return
literal|"next"
return|;
block|}
else|else
block|{
return|return
name|ReplDumpWork
operator|.
name|testInjectDumpDir
return|;
block|}
block|}
else|else
block|{
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
return|;
comment|// TODO: time good enough for now - we'll likely improve this.
comment|// We may also work in something the equivalent of pid, thrid and move to nanos to ensure
comment|// uniqueness.
block|}
block|}
specifier|private
name|void
name|dumpFunctionMetadata
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Path
name|dumpRoot
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|functionsRoot
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|dumpRoot
argument_list|,
name|dbName
argument_list|)
argument_list|,
name|FUNCTIONS_ROOT_DIR_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|functionNames
init|=
name|getHive
argument_list|()
operator|.
name|getFunctions
argument_list|(
name|dbName
argument_list|,
literal|"*"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|functionName
range|:
name|functionNames
control|)
block|{
name|HiveWrapper
operator|.
name|Tuple
argument_list|<
name|Function
argument_list|>
name|tuple
init|=
name|functionTuple
argument_list|(
name|functionName
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tuple
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Path
name|functionRoot
init|=
operator|new
name|Path
argument_list|(
name|functionsRoot
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|Path
name|functionMetadataFile
init|=
operator|new
name|Path
argument_list|(
name|functionRoot
argument_list|,
name|FUNCTION_METADATA_FILE_NAME
argument_list|)
decl_stmt|;
try|try
init|(
name|JsonWriter
name|jsonWriter
init|=
operator|new
name|JsonWriter
argument_list|(
name|functionMetadataFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|,
name|functionMetadataFile
argument_list|)
init|)
block|{
name|FunctionSerializer
name|serializer
init|=
operator|new
name|FunctionSerializer
argument_list|(
name|tuple
operator|.
name|object
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|writeTo
argument_list|(
name|jsonWriter
argument_list|,
name|tuple
operator|.
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
name|REPL_STATE_LOG
operator|.
name|info
argument_list|(
literal|"Repl Dump: Dumped metadata for function: {}"
argument_list|,
name|functionName
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HiveWrapper
operator|.
name|Tuple
argument_list|<
name|Function
argument_list|>
name|functionTuple
parameter_list|(
name|String
name|functionName
parameter_list|,
name|String
name|dbName
parameter_list|)
block|{
try|try
block|{
name|HiveWrapper
operator|.
name|Tuple
argument_list|<
name|Function
argument_list|>
name|tuple
init|=
operator|new
name|HiveWrapper
argument_list|(
name|getHive
argument_list|()
argument_list|,
name|dbName
argument_list|)
operator|.
name|function
argument_list|(
name|functionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tuple
operator|.
name|object
operator|.
name|getResourceUris
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|REPL_STATE_LOG
operator|.
name|warn
argument_list|(
literal|"Not replicating function: "
operator|+
name|functionName
operator|+
literal|" as it seems to have been created "
operator|+
literal|"without USING clause"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|tuple
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|//This can happen as we are querying the getFunctions before we are getting the actual function
comment|//in between there can be a drop function by a user in which case our call will fail.
name|LOG
operator|.
name|info
argument_list|(
literal|"Function "
operator|+
name|functionName
operator|+
literal|" could not be found, we are ignoring it as it can be a valid state "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|REPL_DUMP
return|;
block|}
block|}
end_class

end_unit


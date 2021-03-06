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
name|hive
operator|.
name|common
operator|.
name|repl
operator|.
name|ReplScope
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
name|plan
operator|.
name|Explain
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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Replication Dump Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Explain
operator|.
name|Level
operator|.
name|USER
block|,
name|Explain
operator|.
name|Level
operator|.
name|DEFAULT
block|,
name|Explain
operator|.
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|ReplDumpWork
implements|implements
name|Serializable
block|{
specifier|final
name|ReplScope
name|replScope
decl_stmt|;
specifier|final
name|ReplScope
name|oldReplScope
decl_stmt|;
specifier|final
name|String
name|dbNameOrPattern
decl_stmt|,
name|astRepresentationForErrorMsg
decl_stmt|,
name|resultTempPath
decl_stmt|;
name|Long
name|eventTo
decl_stmt|;
name|Long
name|eventFrom
decl_stmt|;
specifier|static
name|String
name|testInjectDumpDir
init|=
literal|null
decl_stmt|;
specifier|private
name|Integer
name|maxEventLimit
decl_stmt|;
specifier|public
specifier|static
name|void
name|injectNextDumpDirForTest
parameter_list|(
name|String
name|dumpDir
parameter_list|)
block|{
name|testInjectDumpDir
operator|=
name|dumpDir
expr_stmt|;
block|}
specifier|public
name|ReplDumpWork
parameter_list|(
name|ReplScope
name|replScope
parameter_list|,
name|ReplScope
name|oldReplScope
parameter_list|,
name|String
name|astRepresentationForErrorMsg
parameter_list|,
name|String
name|resultTempPath
parameter_list|)
block|{
name|this
operator|.
name|replScope
operator|=
name|replScope
expr_stmt|;
name|this
operator|.
name|oldReplScope
operator|=
name|oldReplScope
expr_stmt|;
name|this
operator|.
name|dbNameOrPattern
operator|=
name|replScope
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|astRepresentationForErrorMsg
operator|=
name|astRepresentationForErrorMsg
expr_stmt|;
name|this
operator|.
name|resultTempPath
operator|=
name|resultTempPath
expr_stmt|;
block|}
name|int
name|maxEventLimit
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|eventTo
operator|<
name|eventFrom
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Invalid event ID input received in TO clause"
argument_list|)
throw|;
block|}
name|Integer
name|maxRange
init|=
name|Ints
operator|.
name|checkedCast
argument_list|(
name|this
operator|.
name|eventTo
operator|-
name|eventFrom
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|maxEventLimit
operator|==
literal|null
operator|)
operator|||
operator|(
name|maxEventLimit
operator|>
name|maxRange
operator|)
condition|)
block|{
name|maxEventLimit
operator|=
name|maxRange
expr_stmt|;
block|}
return|return
name|maxEventLimit
return|;
block|}
name|void
name|setEventFrom
parameter_list|(
name|long
name|eventId
parameter_list|)
block|{
name|eventFrom
operator|=
name|eventId
expr_stmt|;
block|}
comment|// Override any user specification that changes the last event to be dumped.
name|void
name|overrideLastEventToDump
parameter_list|(
name|Hive
name|fromDb
parameter_list|,
name|long
name|bootstrapLastId
parameter_list|)
throws|throws
name|Exception
block|{
comment|// If we are bootstrapping ACID tables, we need to dump all the events upto the event id at
comment|// the beginning of the bootstrap dump and also not dump any event after that. So we override
comment|// both, the last event as well as any user specified limit on the number of events. See
comment|// bootstrampDump() for more details.
if|if
condition|(
name|bootstrapLastId
operator|>
literal|0
condition|)
block|{
name|eventTo
operator|=
name|bootstrapLastId
expr_stmt|;
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|debug
argument_list|(
literal|"eventTo restricted to event id : {} because of bootstrap of ACID tables"
argument_list|,
name|eventTo
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// If no last event is specified get the current last from the metastore.
if|if
condition|(
name|eventTo
operator|==
literal|null
condition|)
block|{
name|eventTo
operator|=
name|fromDb
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
expr_stmt|;
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|debug
argument_list|(
literal|"eventTo not specified, using current event id : {}"
argument_list|,
name|eventTo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


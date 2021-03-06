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
name|repl
operator|.
name|ReplLogger
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
operator|.
name|Level
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
name|Map
import|;
end_import

begin_comment
comment|/**  * ReplStateLogWork  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Repl State Log"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|ReplStateLogWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|final
name|ReplLogger
name|replLogger
decl_stmt|;
specifier|private
specifier|final
name|LOG_TYPE
name|logType
decl_stmt|;
specifier|private
name|String
name|eventId
decl_stmt|;
specifier|private
name|String
name|eventType
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|TableType
name|tableType
decl_stmt|;
specifier|private
name|String
name|functionName
decl_stmt|;
specifier|private
name|String
name|lastReplId
decl_stmt|;
specifier|private
enum|enum
name|LOG_TYPE
block|{
name|TABLE
block|,
name|FUNCTION
block|,
name|EVENT
block|,
name|END
block|}
specifier|public
name|ReplStateLogWork
parameter_list|(
name|ReplLogger
name|replLogger
parameter_list|,
name|String
name|eventId
parameter_list|,
name|String
name|eventType
parameter_list|)
block|{
name|this
operator|.
name|logType
operator|=
name|LOG_TYPE
operator|.
name|EVENT
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|eventId
operator|=
name|eventId
expr_stmt|;
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
block|}
specifier|public
name|ReplStateLogWork
parameter_list|(
name|ReplLogger
name|replLogger
parameter_list|,
name|String
name|tableName
parameter_list|,
name|TableType
name|tableType
parameter_list|)
block|{
name|this
operator|.
name|logType
operator|=
name|LOG_TYPE
operator|.
name|TABLE
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|tableType
operator|=
name|tableType
expr_stmt|;
block|}
specifier|public
name|ReplStateLogWork
parameter_list|(
name|ReplLogger
name|replLogger
parameter_list|,
name|String
name|functionName
parameter_list|)
block|{
name|this
operator|.
name|logType
operator|=
name|LOG_TYPE
operator|.
name|FUNCTION
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|functionName
operator|=
name|functionName
expr_stmt|;
block|}
specifier|public
name|ReplStateLogWork
parameter_list|(
name|ReplLogger
name|replLogger
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProps
parameter_list|)
block|{
name|this
operator|.
name|logType
operator|=
name|LOG_TYPE
operator|.
name|END
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|lastReplId
operator|=
name|ReplicationSpec
operator|.
name|getLastReplicatedStateFromParameters
argument_list|(
name|dbProps
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|replStateLog
parameter_list|()
block|{
switch|switch
condition|(
name|logType
condition|)
block|{
case|case
name|TABLE
case|:
block|{
name|replLogger
operator|.
name|tableLog
argument_list|(
name|tableName
argument_list|,
name|tableType
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|FUNCTION
case|:
block|{
name|replLogger
operator|.
name|functionLog
argument_list|(
name|functionName
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|EVENT
case|:
block|{
name|replLogger
operator|.
name|eventLog
argument_list|(
name|eventId
argument_list|,
name|eventType
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|END
case|:
block|{
name|replLogger
operator|.
name|endLog
argument_list|(
name|lastReplId
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit


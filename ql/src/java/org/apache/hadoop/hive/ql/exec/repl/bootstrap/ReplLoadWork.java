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
operator|.
name|bootstrap
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
name|ql
operator|.
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|DatabaseEvent
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
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
operator|.
name|BootstrapEventsIterator
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
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
operator|.
name|ConstraintEventsIterator
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
name|session
operator|.
name|LineageState
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
name|Serializable
import|;
end_import

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Replication Load Operator"
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
name|ReplLoadWork
implements|implements
name|Serializable
block|{
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|final
name|String
name|tableNameToLoadIn
decl_stmt|;
specifier|private
specifier|final
name|BootstrapEventsIterator
name|iterator
decl_stmt|;
specifier|private
specifier|final
name|ConstraintEventsIterator
name|constraintsIterator
decl_stmt|;
specifier|private
name|int
name|loadTaskRunCount
init|=
literal|0
decl_stmt|;
specifier|private
name|DatabaseEvent
operator|.
name|State
name|state
init|=
literal|null
decl_stmt|;
comment|/*   these are sessionState objects that are copied over to work to allow for parallel execution.   based on the current use case the methods are selectively synchronized, which might need to be   taken care when using other methods.   */
specifier|final
name|LineageState
name|sessionStateLineageState
decl_stmt|;
specifier|public
name|ReplLoadWork
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|dumpDirectory
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|String
name|tableNameToLoadIn
parameter_list|,
name|LineageState
name|lineageState
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|tableNameToLoadIn
operator|=
name|tableNameToLoadIn
expr_stmt|;
name|sessionStateLineageState
operator|=
name|lineageState
expr_stmt|;
name|this
operator|.
name|iterator
operator|=
operator|new
name|BootstrapEventsIterator
argument_list|(
name|dumpDirectory
argument_list|,
name|dbNameToLoadIn
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|constraintsIterator
operator|=
operator|new
name|ConstraintEventsIterator
argument_list|(
name|dumpDirectory
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|dbNameToLoadIn
operator|=
name|dbNameToLoadIn
expr_stmt|;
block|}
specifier|public
name|ReplLoadWork
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|dumpDirectory
parameter_list|,
name|String
name|dbNameOrPattern
parameter_list|,
name|LineageState
name|lineageState
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|hiveConf
argument_list|,
name|dumpDirectory
argument_list|,
name|dbNameOrPattern
argument_list|,
literal|null
argument_list|,
name|lineageState
argument_list|)
expr_stmt|;
block|}
specifier|public
name|BootstrapEventsIterator
name|iterator
parameter_list|()
block|{
return|return
name|iterator
return|;
block|}
specifier|public
name|ConstraintEventsIterator
name|constraintIterator
parameter_list|()
block|{
return|return
name|constraintsIterator
return|;
block|}
name|int
name|executedLoadTask
parameter_list|()
block|{
return|return
operator|++
name|loadTaskRunCount
return|;
block|}
name|void
name|updateDbEventState
parameter_list|(
name|DatabaseEvent
operator|.
name|State
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
block|}
name|DatabaseEvent
name|databaseEvent
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
return|return
name|state
operator|.
name|toEvent
argument_list|(
name|hiveConf
argument_list|)
return|;
block|}
name|boolean
name|hasDbState
parameter_list|()
block|{
return|return
name|state
operator|!=
literal|null
return|;
block|}
block|}
end_class

end_unit


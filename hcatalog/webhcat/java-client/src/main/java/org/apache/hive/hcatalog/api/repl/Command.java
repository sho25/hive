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
name|api
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|io
operator|.
name|Writable
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

begin_comment
comment|/**  * Interface that abstracts the notion of one atomic command to execute.  * If the command does not execute and raises some exception, then Command  * provides a conditional to check if the operation is intended to be  * retriable - i.e. whether the command is considered idempotent. If it is,  * then the user could attempt to redo the particular command they were  * running. If not, then they can check another conditional to check  * if their action is undo-able. If undoable, then they can then attempt  * to undo the action by asking the command how to undo it. If not, they  * can then in turn act upon the exception in whatever manner they see  * fit (typically by raising an error).  *  * We also have two more methods that help cleanup of temporary locations  * used by this Command. cleanupLocationsPerRetry() provides a list of  * directories that are intended to be cleaned up every time this Command  * needs to be retried. cleanupLocationsAfterEvent() provides a list of  * directories that should be cleaned up after the event for which this  * Command is generated is successfully processed.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
interface|interface
name|Command
extends|extends
name|Writable
block|{
name|List
argument_list|<
name|String
argument_list|>
name|get
parameter_list|()
function_decl|;
name|boolean
name|isRetriable
parameter_list|()
function_decl|;
name|boolean
name|isUndoable
parameter_list|()
function_decl|;
name|List
argument_list|<
name|String
argument_list|>
name|getUndo
parameter_list|()
function_decl|;
name|List
argument_list|<
name|String
argument_list|>
name|cleanupLocationsPerRetry
parameter_list|()
function_decl|;
name|List
argument_list|<
name|String
argument_list|>
name|cleanupLocationsAfterEvent
parameter_list|()
function_decl|;
name|long
name|getEventId
parameter_list|()
function_decl|;
block|}
end_interface

end_unit


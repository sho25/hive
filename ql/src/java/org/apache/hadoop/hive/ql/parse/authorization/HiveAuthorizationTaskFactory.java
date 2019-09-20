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
name|authorization
package|;
end_package

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
name|Set
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
name|classification
operator|.
name|InterfaceAudience
operator|.
name|LimitedPrivate
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
operator|.
name|Evolving
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|ASTNode
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

begin_comment
comment|/**  * HiveAuthorizationTaskFactory creates DDL authorization related  * tasks. Every method in this class may return null, indicating no task  * needs to be executed or can throw a SemanticException.  */
end_comment

begin_interface
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Apache Hive, Apache Sentry (incubating)"
block|}
argument_list|)
annotation|@
name|Evolving
specifier|public
interface|interface
name|HiveAuthorizationTaskFactory
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createCreateRoleTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createDropRoleTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createShowRoleGrantTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Path
name|resultFile
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createGrantRoleTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createRevokeRoleTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createGrantTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createShowGrantTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Path
name|resultFile
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createRevokeTask
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createSetRoleTask
parameter_list|(
name|String
name|roleName
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createShowCurrentRoleTask
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|Path
name|resFile
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createShowRolePrincipalsTask
parameter_list|(
name|ASTNode
name|ast
parameter_list|,
name|Path
name|resFile
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createShowRolesTask
parameter_list|(
name|ASTNode
name|ast
parameter_list|,
name|Path
name|resFile
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
block|}
end_interface

end_unit


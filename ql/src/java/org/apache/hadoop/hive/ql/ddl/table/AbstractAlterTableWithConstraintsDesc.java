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
name|ddl
operator|.
name|table
package|;
end_package

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
name|ddl
operator|.
name|table
operator|.
name|constaint
operator|.
name|Constraints
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
name|plan
operator|.
name|AlterTableDesc
operator|.
name|AlterTableTypes
import|;
end_import

begin_comment
comment|/**  * Abstract ancestor of all ALTER TABLE descriptors that are handled by the AlterTableWithWriteIdOperations framework  * and also has constraint changes.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractAlterTableWithConstraintsDesc
extends|extends
name|AbstractAlterTableDesc
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
name|Constraints
name|constraints
decl_stmt|;
specifier|public
name|AbstractAlterTableWithConstraintsDesc
parameter_list|(
name|AlterTableTypes
name|type
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|boolean
name|isCascade
parameter_list|,
name|boolean
name|expectView
parameter_list|,
name|Constraints
name|constraints
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|type
argument_list|,
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
name|replicationSpec
argument_list|,
name|isCascade
argument_list|,
name|expectView
argument_list|)
expr_stmt|;
name|this
operator|.
name|constraints
operator|=
name|constraints
expr_stmt|;
block|}
specifier|public
name|Constraints
name|getConstraints
parameter_list|()
block|{
return|return
name|constraints
return|;
block|}
block|}
end_class

end_unit


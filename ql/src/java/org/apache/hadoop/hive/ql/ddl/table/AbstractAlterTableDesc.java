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
name|TableName
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
name|EnvironmentContext
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
name|DDLDesc
operator|.
name|DDLDescWithWriteId
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

begin_comment
comment|/**  * Abstract ancestor of all ALTER TABLE descriptors that are handled by the AlterTableWithWriteIdOperations framework.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractAlterTableDesc
implements|implements
name|DDLDescWithWriteId
implements|,
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
name|AlterTableType
name|type
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isCascade
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|expectView
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
decl_stmt|;
specifier|private
name|Long
name|writeId
decl_stmt|;
specifier|public
name|AbstractAlterTableDesc
parameter_list|(
name|AlterTableType
name|type
parameter_list|,
name|TableName
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|isCascade
operator|=
name|isCascade
expr_stmt|;
name|this
operator|.
name|expectView
operator|=
name|expectView
expr_stmt|;
name|this
operator|.
name|props
operator|=
name|props
expr_stmt|;
block|}
specifier|public
name|AlterTableType
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table name"
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
name|String
name|getDbTableName
parameter_list|()
block|{
return|return
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition"
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionSpec
parameter_list|()
block|{
return|return
name|partitionSpec
return|;
block|}
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
return|return
name|replicationSpec
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"cascade"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
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
name|boolean
name|isCascade
parameter_list|()
block|{
return|return
name|isCascade
return|;
block|}
specifier|public
name|boolean
name|expectView
parameter_list|()
block|{
return|return
name|expectView
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"properties"
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getProps
parameter_list|()
block|{
return|return
name|props
return|;
block|}
specifier|public
name|EnvironmentContext
name|getEnvironmentContext
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getFullTableName
parameter_list|()
block|{
return|return
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWriteId
parameter_list|(
name|long
name|writeId
parameter_list|)
block|{
name|this
operator|.
name|writeId
operator|=
name|writeId
expr_stmt|;
block|}
specifier|public
name|Long
name|getWriteId
parameter_list|()
block|{
return|return
name|writeId
return|;
block|}
block|}
end_class

end_unit


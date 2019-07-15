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
operator|.
name|creation
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
comment|/**  * DDL task description for DROP TABLE commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Drop Table"
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
name|DropTableDesc
implements|implements
name|DDLDesc
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
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|ifExists
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|purge
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|validationRequired
decl_stmt|;
specifier|public
name|DropTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|ifExists
parameter_list|,
name|boolean
name|ifPurge
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|ifExists
argument_list|,
name|ifPurge
argument_list|,
name|replicationSpec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DropTableDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|ifExists
parameter_list|,
name|boolean
name|purge
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|boolean
name|validationRequired
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|ifExists
operator|=
name|ifExists
expr_stmt|;
name|this
operator|.
name|purge
operator|=
name|purge
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
operator|==
literal|null
condition|?
operator|new
name|ReplicationSpec
argument_list|()
else|:
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|validationRequired
operator|=
name|validationRequired
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table"
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
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|boolean
name|isIfExists
parameter_list|()
block|{
return|return
name|ifExists
return|;
block|}
specifier|public
name|boolean
name|isPurge
parameter_list|()
block|{
return|return
name|purge
return|;
block|}
comment|/**    * @return what kind of replication scope this drop is running under.    * This can result in a "DROP IF OLDER THAN" kind of semantic    */
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
return|return
name|replicationSpec
return|;
block|}
specifier|public
name|boolean
name|getValidationRequired
parameter_list|()
block|{
return|return
name|validationRequired
return|;
block|}
block|}
end_class

end_unit


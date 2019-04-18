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
name|database
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
name|ddl
operator|.
name|DDLTask2
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
name|privilege
operator|.
name|PrincipalDesc
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
comment|/**  * DDL task description for ALTER DATABASE commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alter Database"
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
name|AlterDatabaseDesc
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
static|static
block|{
name|DDLTask2
operator|.
name|registerOperation
argument_list|(
name|AlterDatabaseDesc
operator|.
name|class
argument_list|,
name|AlterDatabaseOperation
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Supported type of alter db commands.    * Only altering the database property and owner is currently supported    */
specifier|public
enum|enum
name|AlterDbType
block|{
name|ALTER_PROPERTY
block|,
name|ALTER_OWNER
block|,
name|ALTER_LOCATION
block|}
empty_stmt|;
specifier|private
specifier|final
name|AlterDbType
name|alterType
decl_stmt|;
specifier|private
specifier|final
name|String
name|databaseName
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProperties
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|private
specifier|final
name|PrincipalDesc
name|ownerPrincipal
decl_stmt|;
specifier|private
specifier|final
name|String
name|location
decl_stmt|;
specifier|public
name|AlterDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProperties
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|this
operator|.
name|alterType
operator|=
name|AlterDbType
operator|.
name|ALTER_PROPERTY
expr_stmt|;
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
name|this
operator|.
name|dbProperties
operator|=
name|dbProperties
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|ownerPrincipal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|location
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|AlterDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|PrincipalDesc
name|ownerPrincipal
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|this
operator|.
name|alterType
operator|=
name|AlterDbType
operator|.
name|ALTER_OWNER
expr_stmt|;
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
name|this
operator|.
name|dbProperties
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|ownerPrincipal
operator|=
name|ownerPrincipal
expr_stmt|;
name|this
operator|.
name|location
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|AlterDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|alterType
operator|=
name|AlterDbType
operator|.
name|ALTER_LOCATION
expr_stmt|;
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
name|this
operator|.
name|dbProperties
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|ownerPrincipal
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
specifier|public
name|AlterDbType
name|getAlterType
parameter_list|()
block|{
return|return
name|alterType
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
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
name|getDatabaseName
parameter_list|()
block|{
return|return
name|databaseName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"properties"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getDatabaseProperties
parameter_list|()
block|{
return|return
name|dbProperties
return|;
block|}
comment|/**    * @return what kind of replication scope this alter is running under.    * This can result in a "ALTER IF NEWER THAN" kind of semantic    */
specifier|public
name|ReplicationSpec
name|getReplicationSpec
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationSpec
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"owner"
argument_list|)
specifier|public
name|PrincipalDesc
name|getOwnerPrincipal
parameter_list|()
block|{
return|return
name|ownerPrincipal
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"location"
argument_list|)
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
block|}
end_class

end_unit


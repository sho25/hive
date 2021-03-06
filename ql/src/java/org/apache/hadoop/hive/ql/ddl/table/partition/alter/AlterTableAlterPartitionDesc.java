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
name|partition
operator|.
name|alter
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
name|api
operator|.
name|FieldSchema
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
comment|/**  * DDL task description for ALTER TABLE ... PARTITION COLUMN ... commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Alter Partition"
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
name|AlterTableAlterPartitionDesc
implements|implements
name|DDLDescWithWriteId
block|{
specifier|public
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1
decl_stmt|;
specifier|private
specifier|final
name|String
name|fqTableName
decl_stmt|;
specifier|private
specifier|final
name|FieldSchema
name|partKeySpec
decl_stmt|;
specifier|public
name|AlterTableAlterPartitionDesc
parameter_list|(
name|String
name|fqTableName
parameter_list|,
name|FieldSchema
name|partKeySpec
parameter_list|)
block|{
name|this
operator|.
name|fqTableName
operator|=
name|fqTableName
expr_stmt|;
name|this
operator|.
name|partKeySpec
operator|=
name|partKeySpec
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
name|fqTableName
return|;
block|}
specifier|public
name|FieldSchema
name|getPartKeySpec
parameter_list|()
block|{
return|return
name|partKeySpec
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition key name"
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
name|getPartKeyName
parameter_list|()
block|{
return|return
name|partKeySpec
operator|.
name|getName
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition key type"
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
name|getPartKeyType
parameter_list|()
block|{
return|return
name|partKeySpec
operator|.
name|getType
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
comment|// We don't actually need the write id, but by implementing DDLDescWithWriteId it ensures that it is allocated
block|}
annotation|@
name|Override
specifier|public
name|String
name|getFullTableName
parameter_list|()
block|{
return|return
name|fqTableName
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mayNeedWriteId
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// Checked before setting as the acid desc.
block|}
block|}
end_class

end_unit


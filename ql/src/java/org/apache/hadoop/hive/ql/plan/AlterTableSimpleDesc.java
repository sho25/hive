begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|plan
operator|.
name|AlterTableDesc
operator|.
name|AlterTableTypes
import|;
end_import

begin_comment
comment|/**  * Contains information needed to modify a partition or a table  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableSimpleDesc
extends|extends
name|DDLDesc
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
decl_stmt|;
specifier|private
name|String
name|compactionType
decl_stmt|;
name|AlterTableTypes
name|type
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
decl_stmt|;
specifier|public
name|AlterTableSimpleDesc
parameter_list|()
block|{   }
comment|/**    * @param tableName    *          table containing the partition    * @param partSpec    */
specifier|public
name|AlterTableSimpleDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|AlterTableTypes
name|type
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|partSpec
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|partSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|partSpec
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Constructor for ALTER TABLE ... COMPACT.    * @param tableName name of the table to compact    * @param partSpec partition to compact    * @param compactionType currently supported values: 'major' and 'minor'    */
specifier|public
name|AlterTableSimpleDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|String
name|compactionType
parameter_list|)
block|{
name|type
operator|=
name|AlterTableTypes
operator|.
name|COMPACT
expr_stmt|;
name|this
operator|.
name|compactionType
operator|=
name|compactionType
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
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
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|AlterTableDesc
operator|.
name|AlterTableTypes
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|void
name|setType
parameter_list|(
name|AlterTableDesc
operator|.
name|AlterTableTypes
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartSpec
parameter_list|()
block|{
return|return
name|partSpec
return|;
block|}
specifier|public
name|void
name|setPartSpec
parameter_list|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|partSpec
operator|=
name|partSpec
expr_stmt|;
block|}
comment|/**    * Get what type of compaction is being done by a ALTER TABLE ... COMPACT statement.    * @return Compaction type, currently supported values are 'major' and 'minor'.    */
specifier|public
name|String
name|getCompactionType
parameter_list|()
block|{
return|return
name|compactionType
return|;
block|}
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
name|void
name|setProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
block|{
name|this
operator|.
name|props
operator|=
name|props
expr_stmt|;
block|}
block|}
end_class

end_unit


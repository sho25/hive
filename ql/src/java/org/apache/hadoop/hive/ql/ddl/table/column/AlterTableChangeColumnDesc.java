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
name|column
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
name|AbstractAlterTableWithConstraintsDesc
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
name|AlterTableType
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
comment|/**  * DDL task description for ALTER TABLE ... CHANGE COLUMN ... commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Change Column"
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
name|AlterTableChangeColumnDesc
extends|extends
name|AbstractAlterTableWithConstraintsDesc
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
name|oldColumnName
decl_stmt|;
specifier|private
specifier|final
name|String
name|newColumnName
decl_stmt|;
specifier|private
specifier|final
name|String
name|newColumnType
decl_stmt|;
specifier|private
specifier|final
name|String
name|newColumnComment
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|first
decl_stmt|;
specifier|private
specifier|final
name|String
name|afterColumn
decl_stmt|;
specifier|public
name|AlterTableChangeColumnDesc
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
name|partitionSpec
parameter_list|,
name|boolean
name|isCascade
parameter_list|,
name|Constraints
name|constraints
parameter_list|,
name|String
name|oldColumnName
parameter_list|,
name|String
name|newColumnName
parameter_list|,
name|String
name|newColumnType
parameter_list|,
name|String
name|newColumnComment
parameter_list|,
name|boolean
name|first
parameter_list|,
name|String
name|afterColumn
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|AlterTableType
operator|.
name|RENAME_COLUMN
argument_list|,
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
literal|null
argument_list|,
name|isCascade
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
name|constraints
argument_list|)
expr_stmt|;
name|this
operator|.
name|oldColumnName
operator|=
name|oldColumnName
expr_stmt|;
name|this
operator|.
name|newColumnName
operator|=
name|newColumnName
expr_stmt|;
name|this
operator|.
name|newColumnType
operator|=
name|newColumnType
expr_stmt|;
name|this
operator|.
name|newColumnComment
operator|=
name|newColumnComment
expr_stmt|;
name|this
operator|.
name|first
operator|=
name|first
expr_stmt|;
name|this
operator|.
name|afterColumn
operator|=
name|afterColumn
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"old column name"
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
name|getOldColumnName
parameter_list|()
block|{
return|return
name|oldColumnName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"new column name"
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
name|getNewColumnName
parameter_list|()
block|{
return|return
name|newColumnName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"new column type"
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
name|getNewColumnType
parameter_list|()
block|{
return|return
name|newColumnType
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"new column comment"
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
name|getNewColumnComment
parameter_list|()
block|{
return|return
name|newColumnComment
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"first"
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
name|isFirst
parameter_list|()
block|{
return|return
name|first
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"after column"
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
name|getAfterColumn
parameter_list|()
block|{
return|return
name|afterColumn
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
block|}
block|}
end_class

end_unit


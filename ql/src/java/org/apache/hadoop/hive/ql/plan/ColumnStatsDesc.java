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
name|plan
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
name|List
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
comment|/**  * Contains the information needed to persist column level statistics  */
end_comment

begin_class
specifier|public
class|class
name|ColumnStatsDesc
implements|implements
name|Serializable
implements|,
name|Cloneable
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
name|FetchWork
name|fWork
decl_stmt|;
specifier|private
name|boolean
name|isTblLevel
decl_stmt|;
specifier|private
name|int
name|numBitVector
decl_stmt|;
specifier|private
name|boolean
name|needMerge
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|colType
decl_stmt|;
specifier|public
name|ColumnStatsDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colType
parameter_list|,
name|boolean
name|isTblLevel
parameter_list|,
name|int
name|numBitVector
parameter_list|,
name|FetchWork
name|fWork1
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
name|colName
operator|=
name|colName
expr_stmt|;
name|this
operator|.
name|colType
operator|=
name|colType
expr_stmt|;
name|this
operator|.
name|isTblLevel
operator|=
name|isTblLevel
expr_stmt|;
name|this
operator|.
name|numBitVector
operator|=
name|numBitVector
expr_stmt|;
name|this
operator|.
name|needMerge
operator|=
name|this
operator|.
name|numBitVector
operator|!=
literal|0
expr_stmt|;
name|this
operator|.
name|fWork
operator|=
name|fWork1
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Table"
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Is Table Level Stats"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|isTblLevel
parameter_list|()
block|{
return|return
name|isTblLevel
return|;
block|}
specifier|public
name|void
name|setTblLevel
parameter_list|(
name|boolean
name|isTblLevel
parameter_list|)
block|{
name|this
operator|.
name|isTblLevel
operator|=
name|isTblLevel
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Columns"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColName
parameter_list|()
block|{
return|return
name|colName
return|;
block|}
specifier|public
name|void
name|setColName
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colName
parameter_list|)
block|{
name|this
operator|.
name|colName
operator|=
name|colName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Column Types"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColType
parameter_list|()
block|{
return|return
name|colType
return|;
block|}
specifier|public
name|void
name|setColType
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|colType
parameter_list|)
block|{
name|this
operator|.
name|colType
operator|=
name|colType
expr_stmt|;
block|}
specifier|public
name|int
name|getNumBitVector
parameter_list|()
block|{
return|return
name|numBitVector
return|;
block|}
specifier|public
name|void
name|setNumBitVector
parameter_list|(
name|int
name|numBitVector
parameter_list|)
block|{
name|this
operator|.
name|numBitVector
operator|=
name|numBitVector
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNeedMerge
parameter_list|()
block|{
return|return
name|needMerge
return|;
block|}
specifier|public
name|FetchWork
name|getFWork
parameter_list|()
block|{
return|return
name|fWork
return|;
block|}
block|}
end_class

end_unit


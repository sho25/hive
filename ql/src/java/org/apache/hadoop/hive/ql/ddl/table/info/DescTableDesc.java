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
name|info
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * DDL task description for DESC table_name commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Describe Table"
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
name|DescTableDesc
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
specifier|public
specifier|static
specifier|final
name|String
name|SCHEMA
init|=
literal|"col_name,data_type,comment#string:string:string"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_STATISTICS_SCHEMA
init|=
literal|"column_property,value#string:string"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|COLUMN_STATISTICS_HEADERS
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"col_name"
argument_list|,
literal|"data_type"
argument_list|,
literal|"min"
argument_list|,
literal|"max"
argument_list|,
literal|"num_nulls"
argument_list|,
literal|"distinct_count"
argument_list|,
literal|"avg_col_len"
argument_list|,
literal|"max_col_len"
argument_list|,
literal|"num_trues"
argument_list|,
literal|"num_falses"
argument_list|,
literal|"bit_vector"
argument_list|,
literal|"comment"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|resFile
decl_stmt|;
specifier|private
specifier|final
name|String
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
name|String
name|columnPath
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isExtended
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isFormatted
decl_stmt|;
specifier|public
name|DescTableDesc
parameter_list|(
name|Path
name|resFile
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
name|String
name|columnPath
parameter_list|,
name|boolean
name|isExtended
parameter_list|,
name|boolean
name|isFormatted
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
operator|.
name|toString
argument_list|()
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
name|columnPath
operator|=
name|columnPath
expr_stmt|;
name|this
operator|.
name|isExtended
operator|=
name|isExtended
expr_stmt|;
name|this
operator|.
name|isFormatted
operator|=
name|isFormatted
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"result file"
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
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
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
name|String
name|getColumnPath
parameter_list|()
block|{
return|return
name|columnPath
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"extended"
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
name|isExtended
parameter_list|()
block|{
return|return
name|isExtended
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"formatted"
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
name|isFormatted
parameter_list|()
block|{
return|return
name|isFormatted
return|;
block|}
block|}
end_class

end_unit


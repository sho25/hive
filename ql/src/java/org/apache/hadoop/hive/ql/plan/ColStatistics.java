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

begin_class
specifier|public
class|class
name|ColStatistics
block|{
specifier|private
name|String
name|colName
decl_stmt|;
specifier|private
name|String
name|colType
decl_stmt|;
specifier|private
name|long
name|countDistint
decl_stmt|;
specifier|private
name|long
name|numNulls
decl_stmt|;
specifier|private
name|double
name|avgColLen
decl_stmt|;
specifier|private
name|long
name|numTrues
decl_stmt|;
specifier|private
name|long
name|numFalses
decl_stmt|;
specifier|private
name|Range
name|range
decl_stmt|;
specifier|private
name|boolean
name|isPrimaryKey
decl_stmt|;
specifier|private
name|boolean
name|isEstimated
decl_stmt|;
specifier|private
name|boolean
name|isFilteredColumn
decl_stmt|;
specifier|public
name|ColStatistics
parameter_list|(
name|String
name|colName
parameter_list|,
name|String
name|colType
parameter_list|)
block|{
name|this
operator|.
name|setColumnName
argument_list|(
name|colName
argument_list|)
expr_stmt|;
name|this
operator|.
name|setColumnType
argument_list|(
name|colType
argument_list|)
expr_stmt|;
name|this
operator|.
name|setPrimaryKey
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|setIsEstimated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ColStatistics
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getColumnName
parameter_list|()
block|{
return|return
name|colName
return|;
block|}
specifier|public
name|void
name|setColumnName
parameter_list|(
name|String
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
specifier|public
name|String
name|getColumnType
parameter_list|()
block|{
return|return
name|colType
return|;
block|}
specifier|public
name|void
name|setColumnType
parameter_list|(
name|String
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
name|long
name|getCountDistint
parameter_list|()
block|{
return|return
name|countDistint
return|;
block|}
specifier|public
name|void
name|setCountDistint
parameter_list|(
name|long
name|countDistint
parameter_list|)
block|{
name|this
operator|.
name|countDistint
operator|=
name|countDistint
expr_stmt|;
block|}
specifier|public
name|long
name|getNumNulls
parameter_list|()
block|{
return|return
name|numNulls
return|;
block|}
specifier|public
name|void
name|setNumNulls
parameter_list|(
name|long
name|numNulls
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
block|}
specifier|public
name|double
name|getAvgColLen
parameter_list|()
block|{
return|return
name|avgColLen
return|;
block|}
specifier|public
name|void
name|setAvgColLen
parameter_list|(
name|double
name|avgColLen
parameter_list|)
block|{
name|this
operator|.
name|avgColLen
operator|=
name|avgColLen
expr_stmt|;
block|}
specifier|public
name|long
name|getNumTrues
parameter_list|()
block|{
return|return
name|numTrues
return|;
block|}
specifier|public
name|void
name|setNumTrues
parameter_list|(
name|long
name|numTrues
parameter_list|)
block|{
name|this
operator|.
name|numTrues
operator|=
name|numTrues
expr_stmt|;
block|}
specifier|public
name|long
name|getNumFalses
parameter_list|()
block|{
return|return
name|numFalses
return|;
block|}
specifier|public
name|void
name|setNumFalses
parameter_list|(
name|long
name|numFalses
parameter_list|)
block|{
name|this
operator|.
name|numFalses
operator|=
name|numFalses
expr_stmt|;
block|}
specifier|public
name|Range
name|getRange
parameter_list|()
block|{
return|return
name|range
return|;
block|}
specifier|public
name|void
name|setRange
parameter_list|(
name|Number
name|minVal
parameter_list|,
name|Number
name|maxVal
parameter_list|)
block|{
name|this
operator|.
name|range
operator|=
operator|new
name|Range
argument_list|(
name|minVal
argument_list|,
name|maxVal
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRange
parameter_list|(
name|Range
name|r
parameter_list|)
block|{
name|this
operator|.
name|range
operator|=
name|r
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" colName: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|colName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" colType: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|colType
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" countDistincts: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|countDistint
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" numNulls: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numNulls
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" avgColLen: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|avgColLen
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" numTrues: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numTrues
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" numFalses: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|numFalses
argument_list|)
expr_stmt|;
if|if
condition|(
name|range
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|range
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" isPrimaryKey: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|isPrimaryKey
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" isEstimated: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|isEstimated
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ColStatistics
name|clone
parameter_list|()
block|{
name|ColStatistics
name|clone
init|=
operator|new
name|ColStatistics
argument_list|(
name|colName
argument_list|,
name|colType
argument_list|)
decl_stmt|;
name|clone
operator|.
name|setAvgColLen
argument_list|(
name|avgColLen
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setCountDistint
argument_list|(
name|countDistint
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setNumNulls
argument_list|(
name|numNulls
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setNumTrues
argument_list|(
name|numTrues
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setNumFalses
argument_list|(
name|numFalses
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setPrimaryKey
argument_list|(
name|isPrimaryKey
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setIsEstimated
argument_list|(
name|isEstimated
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setIsFilteredColumn
argument_list|(
name|isFilteredColumn
argument_list|)
expr_stmt|;
if|if
condition|(
name|range
operator|!=
literal|null
condition|)
block|{
name|clone
operator|.
name|setRange
argument_list|(
name|range
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|clone
return|;
block|}
specifier|public
name|boolean
name|isPrimaryKey
parameter_list|()
block|{
return|return
name|isPrimaryKey
return|;
block|}
specifier|public
name|void
name|setPrimaryKey
parameter_list|(
name|boolean
name|isPrimaryKey
parameter_list|)
block|{
name|this
operator|.
name|isPrimaryKey
operator|=
name|isPrimaryKey
expr_stmt|;
block|}
specifier|public
name|void
name|setIsEstimated
parameter_list|(
name|boolean
name|isEstimated
parameter_list|)
block|{
name|this
operator|.
name|isEstimated
operator|=
name|isEstimated
expr_stmt|;
block|}
specifier|public
name|boolean
name|isEstimated
parameter_list|()
block|{
return|return
name|isEstimated
return|;
block|}
specifier|public
specifier|static
class|class
name|Range
block|{
specifier|public
specifier|final
name|Number
name|minValue
decl_stmt|;
specifier|public
specifier|final
name|Number
name|maxValue
decl_stmt|;
specifier|public
name|Range
parameter_list|(
name|Number
name|minValue
parameter_list|,
name|Number
name|maxValue
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
name|this
operator|.
name|maxValue
operator|=
name|maxValue
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Range
name|clone
parameter_list|()
block|{
return|return
operator|new
name|Range
argument_list|(
name|minValue
argument_list|,
name|maxValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Range: ["
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" min: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|minValue
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" max: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|maxValue
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ]"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|public
name|void
name|setFilterColumn
parameter_list|()
block|{
name|isFilteredColumn
operator|=
literal|true
expr_stmt|;
block|}
specifier|private
name|void
name|setIsFilteredColumn
parameter_list|(
name|boolean
name|isFilteredColumn2
parameter_list|)
block|{
name|isFilteredColumn
operator|=
name|isFilteredColumn2
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFilteredColumn
parameter_list|()
block|{
return|return
name|isFilteredColumn
return|;
block|}
block|}
end_class

end_unit


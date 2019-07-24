begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/**  *  */
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
name|metastore
operator|.
name|model
package|;
end_package

begin_comment
comment|/**  *  * MTableColumnStatistics - Represents Hive's Column Statistics Description. The fields in this  * class with the exception of table are persisted in the metastore. In case of table, tbl_id is  * persisted in its place.  *  */
end_comment

begin_class
specifier|public
class|class
name|MTableColumnStatistics
block|{
specifier|private
name|MTable
name|table
decl_stmt|;
specifier|private
name|String
name|catName
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|colName
decl_stmt|;
specifier|private
name|String
name|colType
decl_stmt|;
specifier|private
name|String
name|engine
decl_stmt|;
specifier|private
name|Long
name|longLowValue
decl_stmt|;
specifier|private
name|Long
name|longHighValue
decl_stmt|;
specifier|private
name|Double
name|doubleLowValue
decl_stmt|;
specifier|private
name|Double
name|doubleHighValue
decl_stmt|;
specifier|private
name|String
name|decimalLowValue
decl_stmt|;
specifier|private
name|String
name|decimalHighValue
decl_stmt|;
specifier|private
name|Long
name|numNulls
decl_stmt|;
specifier|private
name|Long
name|numDVs
decl_stmt|;
specifier|private
name|byte
index|[]
name|bitVector
decl_stmt|;
specifier|private
name|Double
name|avgColLen
decl_stmt|;
specifier|private
name|Long
name|maxColLen
decl_stmt|;
specifier|private
name|Long
name|numTrues
decl_stmt|;
specifier|private
name|Long
name|numFalses
decl_stmt|;
specifier|private
name|long
name|lastAnalyzed
decl_stmt|;
specifier|public
name|MTableColumnStatistics
parameter_list|()
block|{}
specifier|public
name|MTable
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|MTable
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
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
name|String
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
name|Long
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
name|Long
name|getNumDVs
parameter_list|()
block|{
return|return
name|numDVs
return|;
block|}
specifier|public
name|void
name|setNumDVs
parameter_list|(
name|long
name|numDVs
parameter_list|)
block|{
name|this
operator|.
name|numDVs
operator|=
name|numDVs
expr_stmt|;
block|}
specifier|public
name|Double
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
name|Long
name|getMaxColLen
parameter_list|()
block|{
return|return
name|maxColLen
return|;
block|}
specifier|public
name|void
name|setMaxColLen
parameter_list|(
name|long
name|maxColLen
parameter_list|)
block|{
name|this
operator|.
name|maxColLen
operator|=
name|maxColLen
expr_stmt|;
block|}
specifier|public
name|Long
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
name|Long
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
name|long
name|getLastAnalyzed
parameter_list|()
block|{
return|return
name|lastAnalyzed
return|;
block|}
specifier|public
name|void
name|setLastAnalyzed
parameter_list|(
name|long
name|lastAnalyzed
parameter_list|)
block|{
name|this
operator|.
name|lastAnalyzed
operator|=
name|lastAnalyzed
expr_stmt|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
specifier|public
name|String
name|getCatName
parameter_list|()
block|{
return|return
name|catName
return|;
block|}
specifier|public
name|void
name|setCatName
parameter_list|(
name|String
name|catName
parameter_list|)
block|{
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
block|}
specifier|public
name|void
name|setBooleanStats
parameter_list|(
name|Long
name|numTrues
parameter_list|,
name|Long
name|numFalses
parameter_list|,
name|Long
name|numNulls
parameter_list|)
block|{
name|this
operator|.
name|numTrues
operator|=
name|numTrues
expr_stmt|;
name|this
operator|.
name|numFalses
operator|=
name|numFalses
expr_stmt|;
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
block|}
specifier|public
name|void
name|setLongStats
parameter_list|(
name|Long
name|numNulls
parameter_list|,
name|Long
name|numNDVs
parameter_list|,
name|byte
index|[]
name|bitVector
parameter_list|,
name|Long
name|lowValue
parameter_list|,
name|Long
name|highValue
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
name|this
operator|.
name|numDVs
operator|=
name|numNDVs
expr_stmt|;
name|this
operator|.
name|bitVector
operator|=
name|bitVector
expr_stmt|;
name|this
operator|.
name|longLowValue
operator|=
name|lowValue
expr_stmt|;
name|this
operator|.
name|longHighValue
operator|=
name|highValue
expr_stmt|;
block|}
specifier|public
name|void
name|setDoubleStats
parameter_list|(
name|Long
name|numNulls
parameter_list|,
name|Long
name|numNDVs
parameter_list|,
name|byte
index|[]
name|bitVector
parameter_list|,
name|Double
name|lowValue
parameter_list|,
name|Double
name|highValue
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
name|this
operator|.
name|numDVs
operator|=
name|numNDVs
expr_stmt|;
name|this
operator|.
name|bitVector
operator|=
name|bitVector
expr_stmt|;
name|this
operator|.
name|doubleLowValue
operator|=
name|lowValue
expr_stmt|;
name|this
operator|.
name|doubleHighValue
operator|=
name|highValue
expr_stmt|;
block|}
specifier|public
name|void
name|setDecimalStats
parameter_list|(
name|Long
name|numNulls
parameter_list|,
name|Long
name|numNDVs
parameter_list|,
name|byte
index|[]
name|bitVector
parameter_list|,
name|String
name|lowValue
parameter_list|,
name|String
name|highValue
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
name|this
operator|.
name|numDVs
operator|=
name|numNDVs
expr_stmt|;
name|this
operator|.
name|bitVector
operator|=
name|bitVector
expr_stmt|;
name|this
operator|.
name|decimalLowValue
operator|=
name|lowValue
expr_stmt|;
name|this
operator|.
name|decimalHighValue
operator|=
name|highValue
expr_stmt|;
block|}
specifier|public
name|void
name|setStringStats
parameter_list|(
name|Long
name|numNulls
parameter_list|,
name|Long
name|numNDVs
parameter_list|,
name|byte
index|[]
name|bitVector
parameter_list|,
name|Long
name|maxColLen
parameter_list|,
name|Double
name|avgColLen
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
name|this
operator|.
name|numDVs
operator|=
name|numNDVs
expr_stmt|;
name|this
operator|.
name|bitVector
operator|=
name|bitVector
expr_stmt|;
name|this
operator|.
name|maxColLen
operator|=
name|maxColLen
expr_stmt|;
name|this
operator|.
name|avgColLen
operator|=
name|avgColLen
expr_stmt|;
block|}
specifier|public
name|void
name|setBinaryStats
parameter_list|(
name|Long
name|numNulls
parameter_list|,
name|Long
name|maxColLen
parameter_list|,
name|Double
name|avgColLen
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
name|this
operator|.
name|maxColLen
operator|=
name|maxColLen
expr_stmt|;
name|this
operator|.
name|avgColLen
operator|=
name|avgColLen
expr_stmt|;
block|}
specifier|public
name|void
name|setDateStats
parameter_list|(
name|Long
name|numNulls
parameter_list|,
name|Long
name|numNDVs
parameter_list|,
name|byte
index|[]
name|bitVector
parameter_list|,
name|Long
name|lowValue
parameter_list|,
name|Long
name|highValue
parameter_list|)
block|{
name|this
operator|.
name|numNulls
operator|=
name|numNulls
expr_stmt|;
name|this
operator|.
name|numDVs
operator|=
name|numNDVs
expr_stmt|;
name|this
operator|.
name|bitVector
operator|=
name|bitVector
expr_stmt|;
name|this
operator|.
name|longLowValue
operator|=
name|lowValue
expr_stmt|;
name|this
operator|.
name|longHighValue
operator|=
name|highValue
expr_stmt|;
block|}
specifier|public
name|Long
name|getLongLowValue
parameter_list|()
block|{
return|return
name|longLowValue
return|;
block|}
specifier|public
name|void
name|setLongLowValue
parameter_list|(
name|long
name|longLowValue
parameter_list|)
block|{
name|this
operator|.
name|longLowValue
operator|=
name|longLowValue
expr_stmt|;
block|}
specifier|public
name|Long
name|getLongHighValue
parameter_list|()
block|{
return|return
name|longHighValue
return|;
block|}
specifier|public
name|void
name|setLongHighValue
parameter_list|(
name|long
name|longHighValue
parameter_list|)
block|{
name|this
operator|.
name|longHighValue
operator|=
name|longHighValue
expr_stmt|;
block|}
specifier|public
name|Double
name|getDoubleLowValue
parameter_list|()
block|{
return|return
name|doubleLowValue
return|;
block|}
specifier|public
name|void
name|setDoubleLowValue
parameter_list|(
name|double
name|doubleLowValue
parameter_list|)
block|{
name|this
operator|.
name|doubleLowValue
operator|=
name|doubleLowValue
expr_stmt|;
block|}
specifier|public
name|Double
name|getDoubleHighValue
parameter_list|()
block|{
return|return
name|doubleHighValue
return|;
block|}
specifier|public
name|void
name|setDoubleHighValue
parameter_list|(
name|double
name|doubleHighValue
parameter_list|)
block|{
name|this
operator|.
name|doubleHighValue
operator|=
name|doubleHighValue
expr_stmt|;
block|}
specifier|public
name|String
name|getDecimalLowValue
parameter_list|()
block|{
return|return
name|decimalLowValue
return|;
block|}
specifier|public
name|void
name|setDecimalLowValue
parameter_list|(
name|String
name|decimalLowValue
parameter_list|)
block|{
name|this
operator|.
name|decimalLowValue
operator|=
name|decimalLowValue
expr_stmt|;
block|}
specifier|public
name|String
name|getDecimalHighValue
parameter_list|()
block|{
return|return
name|decimalHighValue
return|;
block|}
specifier|public
name|void
name|setDecimalHighValue
parameter_list|(
name|String
name|decimalHighValue
parameter_list|)
block|{
name|this
operator|.
name|decimalHighValue
operator|=
name|decimalHighValue
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getBitVector
parameter_list|()
block|{
return|return
name|bitVector
return|;
block|}
specifier|public
name|void
name|setBitVector
parameter_list|(
name|byte
index|[]
name|bitVector
parameter_list|)
block|{
name|this
operator|.
name|bitVector
operator|=
name|bitVector
expr_stmt|;
block|}
specifier|public
name|String
name|getEngine
parameter_list|()
block|{
return|return
name|engine
return|;
block|}
specifier|public
name|void
name|setEngine
parameter_list|(
name|String
name|engine
parameter_list|)
block|{
name|this
operator|.
name|engine
operator|=
name|engine
expr_stmt|;
block|}
block|}
end_class

end_unit


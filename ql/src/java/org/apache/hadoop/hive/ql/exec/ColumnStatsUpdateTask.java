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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|BinaryColumnStatsData
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
name|BooleanColumnStatsData
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
name|ColumnStatistics
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
name|ColumnStatisticsData
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
name|ColumnStatisticsDesc
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
name|ColumnStatisticsObj
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
name|Date
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
name|DateColumnStatsData
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
name|Decimal
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
name|DecimalColumnStatsData
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
name|DoubleColumnStatsData
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
name|LongColumnStatsData
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
name|MetaException
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
name|SetPartitionsStatsRequest
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
name|StringColumnStatsData
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
name|CompilationOpContext
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
name|DriverContext
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
name|QueryPlan
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
name|QueryState
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|HiveException
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
name|ColumnStatsDesc
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
name|ColumnStatsUpdateWork
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
name|api
operator|.
name|StageType
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
name|session
operator|.
name|SessionState
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
name|serde2
operator|.
name|io
operator|.
name|DateWritable
import|;
end_import

begin_comment
comment|/**  * ColumnStatsUpdateTask implementation. For example, ALTER TABLE src_stat  * UPDATE STATISTICS for column key SET ('numDVs'='1111','avgColLen'='1.111');  * For another example, ALTER TABLE src_stat_part PARTITION(partitionId=100)  * UPDATE STATISTICS for column value SET  * ('maxColLen'='4444','avgColLen'='44.4');  **/
end_comment

begin_class
specifier|public
class|class
name|ColumnStatsUpdateTask
extends|extends
name|Task
argument_list|<
name|ColumnStatsUpdateWork
argument_list|>
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
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ColumnStatsUpdateTask
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|ctx
parameter_list|,
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|ctx
argument_list|,
name|opContext
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ColumnStatistics
name|constructColumnStatsFromInput
parameter_list|()
throws|throws
name|SemanticException
throws|,
name|MetaException
block|{
name|String
name|dbName
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getCurrentDatabase
argument_list|()
decl_stmt|;
name|ColumnStatsDesc
name|desc
init|=
name|work
operator|.
name|getColStats
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|desc
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|String
name|partName
init|=
name|work
operator|.
name|getPartName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colName
init|=
name|desc
operator|.
name|getColName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colType
init|=
name|desc
operator|.
name|getColType
argument_list|()
decl_stmt|;
name|ColumnStatisticsObj
name|statsObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
comment|// grammar prohibits more than 1 column so we are guaranteed to have only 1
comment|// element in this lists.
name|statsObj
operator|.
name|setColName
argument_list|(
name|colName
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setColType
argument_list|(
name|colType
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ColumnStatisticsData
name|statsData
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
name|String
name|columnType
init|=
name|colType
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"long"
argument_list|)
operator|||
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tinyint"
argument_list|)
operator|||
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"smallint"
argument_list|)
operator|||
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"int"
argument_list|)
operator|||
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"bigint"
argument_list|)
operator|||
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"timestamp"
argument_list|)
condition|)
block|{
name|LongColumnStatsData
name|longStats
init|=
operator|new
name|LongColumnStatsData
argument_list|()
decl_stmt|;
name|longStats
operator|.
name|setNumNullsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|longStats
operator|.
name|setNumDVsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|longStats
operator|.
name|setLowValueIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|longStats
operator|.
name|setHighValueIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|longStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numDVs"
argument_list|)
condition|)
block|{
name|longStats
operator|.
name|setNumDVs
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"lowValue"
argument_list|)
condition|)
block|{
name|longStats
operator|.
name|setLowValue
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"highValue"
argument_list|)
condition|)
block|{
name|longStats
operator|.
name|setHighValue
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setLongStats
argument_list|(
name|longStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
argument_list|)
operator|||
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
name|DoubleColumnStatsData
name|doubleStats
init|=
operator|new
name|DoubleColumnStatsData
argument_list|()
decl_stmt|;
name|doubleStats
operator|.
name|setNumNullsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|doubleStats
operator|.
name|setNumDVsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|doubleStats
operator|.
name|setLowValueIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|doubleStats
operator|.
name|setHighValueIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|doubleStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numDVs"
argument_list|)
condition|)
block|{
name|doubleStats
operator|.
name|setNumDVs
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"lowValue"
argument_list|)
condition|)
block|{
name|doubleStats
operator|.
name|setLowValue
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"highValue"
argument_list|)
condition|)
block|{
name|doubleStats
operator|.
name|setHighValue
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setDoubleStats
argument_list|(
name|doubleStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"string"
argument_list|)
operator|||
name|columnType
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"char"
argument_list|)
operator|||
name|columnType
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"varchar"
argument_list|)
condition|)
block|{
comment|//char(x),varchar(x) types
name|StringColumnStatsData
name|stringStats
init|=
operator|new
name|StringColumnStatsData
argument_list|()
decl_stmt|;
name|stringStats
operator|.
name|setMaxColLenIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|stringStats
operator|.
name|setAvgColLenIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|stringStats
operator|.
name|setNumNullsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|stringStats
operator|.
name|setNumDVsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|stringStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numDVs"
argument_list|)
condition|)
block|{
name|stringStats
operator|.
name|setNumDVs
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"avgColLen"
argument_list|)
condition|)
block|{
name|stringStats
operator|.
name|setAvgColLen
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"maxColLen"
argument_list|)
condition|)
block|{
name|stringStats
operator|.
name|setMaxColLen
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setStringStats
argument_list|(
name|stringStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"boolean"
argument_list|)
condition|)
block|{
name|BooleanColumnStatsData
name|booleanStats
init|=
operator|new
name|BooleanColumnStatsData
argument_list|()
decl_stmt|;
name|booleanStats
operator|.
name|setNumNullsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|booleanStats
operator|.
name|setNumTruesIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|booleanStats
operator|.
name|setNumFalsesIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|booleanStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numTrues"
argument_list|)
condition|)
block|{
name|booleanStats
operator|.
name|setNumTrues
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numFalses"
argument_list|)
condition|)
block|{
name|booleanStats
operator|.
name|setNumFalses
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setBooleanStats
argument_list|(
name|booleanStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"binary"
argument_list|)
condition|)
block|{
name|BinaryColumnStatsData
name|binaryStats
init|=
operator|new
name|BinaryColumnStatsData
argument_list|()
decl_stmt|;
name|binaryStats
operator|.
name|setNumNullsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|binaryStats
operator|.
name|setAvgColLenIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|binaryStats
operator|.
name|setMaxColLenIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|binaryStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"avgColLen"
argument_list|)
condition|)
block|{
name|binaryStats
operator|.
name|setAvgColLen
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"maxColLen"
argument_list|)
condition|)
block|{
name|binaryStats
operator|.
name|setMaxColLen
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setBinaryStats
argument_list|(
name|binaryStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnType
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"decimal"
argument_list|)
condition|)
block|{
comment|//decimal(a,b) type
name|DecimalColumnStatsData
name|decimalStats
init|=
operator|new
name|DecimalColumnStatsData
argument_list|()
decl_stmt|;
name|decimalStats
operator|.
name|setNumNullsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|decimalStats
operator|.
name|setNumDVsIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|decimalStats
operator|.
name|setLowValueIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|decimalStats
operator|.
name|setHighValueIsSet
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|decimalStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numDVs"
argument_list|)
condition|)
block|{
name|decimalStats
operator|.
name|setNumDVs
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"lowValue"
argument_list|)
condition|)
block|{
name|BigDecimal
name|d
init|=
operator|new
name|BigDecimal
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|decimalStats
operator|.
name|setLowValue
argument_list|(
operator|new
name|Decimal
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|d
operator|.
name|unscaledValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|,
operator|(
name|short
operator|)
name|d
operator|.
name|scale
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"highValue"
argument_list|)
condition|)
block|{
name|BigDecimal
name|d
init|=
operator|new
name|BigDecimal
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|decimalStats
operator|.
name|setHighValue
argument_list|(
operator|new
name|Decimal
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|d
operator|.
name|unscaledValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|,
operator|(
name|short
operator|)
name|d
operator|.
name|scale
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setDecimalStats
argument_list|(
name|decimalStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"date"
argument_list|)
condition|)
block|{
name|DateColumnStatsData
name|dateStats
init|=
operator|new
name|DateColumnStatsData
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
name|work
operator|.
name|getMapProp
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mapProp
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numNulls"
argument_list|)
condition|)
block|{
name|dateStats
operator|.
name|setNumNulls
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"numDVs"
argument_list|)
condition|)
block|{
name|dateStats
operator|.
name|setNumDVs
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"lowValue"
argument_list|)
condition|)
block|{
comment|// Date high/low value is stored as long in stats DB, but allow users to set high/low
comment|// value using either date format (yyyy-mm-dd) or numeric format (days since epoch)
name|dateStats
operator|.
name|setLowValue
argument_list|(
name|readDateValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"highValue"
argument_list|)
condition|)
block|{
name|dateStats
operator|.
name|setHighValue
argument_list|(
name|readDateValue
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unknown stat"
argument_list|)
throw|;
block|}
block|}
name|statsData
operator|.
name|setDateStats
argument_list|(
name|dateStats
argument_list|)
expr_stmt|;
name|statsObj
operator|.
name|setStatsData
argument_list|(
name|statsData
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Unsupported type"
argument_list|)
throw|;
block|}
name|String
index|[]
name|names
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|ColumnStatisticsDesc
name|statsDesc
init|=
name|getColumnStatsDesc
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|,
name|partName
argument_list|,
name|partName
operator|==
literal|null
argument_list|)
decl_stmt|;
name|ColumnStatistics
name|colStat
init|=
operator|new
name|ColumnStatistics
argument_list|()
decl_stmt|;
name|colStat
operator|.
name|setStatsDesc
argument_list|(
name|statsDesc
argument_list|)
expr_stmt|;
name|colStat
operator|.
name|addToStatsObj
argument_list|(
name|statsObj
argument_list|)
expr_stmt|;
return|return
name|colStat
return|;
block|}
specifier|private
name|ColumnStatisticsDesc
name|getColumnStatsDesc
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|,
name|boolean
name|isTblLevel
parameter_list|)
block|{
name|ColumnStatisticsDesc
name|statsDesc
init|=
operator|new
name|ColumnStatisticsDesc
argument_list|()
decl_stmt|;
name|statsDesc
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|statsDesc
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|statsDesc
operator|.
name|setIsTblLevel
argument_list|(
name|isTblLevel
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isTblLevel
condition|)
block|{
name|statsDesc
operator|.
name|setPartName
argument_list|(
name|partName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|statsDesc
operator|.
name|setPartName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|statsDesc
return|;
block|}
specifier|private
name|int
name|persistColumnStats
parameter_list|(
name|Hive
name|db
parameter_list|)
throws|throws
name|HiveException
throws|,
name|MetaException
throws|,
name|IOException
block|{
name|List
argument_list|<
name|ColumnStatistics
argument_list|>
name|colStats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|colStats
operator|.
name|add
argument_list|(
name|constructColumnStatsFromInput
argument_list|()
argument_list|)
expr_stmt|;
name|SetPartitionsStatsRequest
name|request
init|=
operator|new
name|SetPartitionsStatsRequest
argument_list|(
name|colStats
argument_list|)
decl_stmt|;
name|db
operator|.
name|setPartitionColumnStatistics
argument_list|(
name|request
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
try|try
block|{
name|Hive
name|db
init|=
name|getHive
argument_list|()
decl_stmt|;
return|return
name|persistColumnStats
argument_list|(
name|db
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed to persist stats in metastore"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|COLUMNSTATS
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"COLUMNSTATS UPDATE TASK"
return|;
block|}
specifier|private
name|Date
name|readDateValue
parameter_list|(
name|String
name|dateStr
parameter_list|)
block|{
comment|// try either yyyy-mm-dd, or integer representing days since epoch
try|try
block|{
name|DateWritable
name|writableVal
init|=
operator|new
name|DateWritable
argument_list|(
name|java
operator|.
name|sql
operator|.
name|Date
operator|.
name|valueOf
argument_list|(
name|dateStr
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|Date
argument_list|(
name|writableVal
operator|.
name|getDays
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|err
parameter_list|)
block|{
comment|// Fallback to integer parsing
name|LOG
operator|.
name|debug
argument_list|(
literal|"Reading date value as days since epoch: "
operator|+
name|dateStr
argument_list|)
expr_stmt|;
return|return
operator|new
name|Date
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|dateStr
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


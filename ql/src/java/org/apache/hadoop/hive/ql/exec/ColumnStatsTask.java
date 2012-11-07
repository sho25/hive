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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
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
name|CommandNeedRetryException
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
name|Context
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
name|plan
operator|.
name|ColumnStatsWork
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
name|FetchWork
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
name|serde2
operator|.
name|objectinspector
operator|.
name|InspectableObject
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
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
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
name|objectinspector
operator|.
name|StructField
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
name|objectinspector
operator|.
name|StructObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|DoubleObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|LongObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|StringObjectInspector
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
name|mapred
operator|.
name|JobConf
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * ColumnStatsTask implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|ColumnStatsTask
extends|extends
name|Task
argument_list|<
name|ColumnStatsWork
argument_list|>
implements|implements
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
name|FetchOperator
name|ftOp
decl_stmt|;
specifier|private
specifier|static
specifier|transient
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ColumnStatsTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ColumnStatsTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|ctx
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
try|try
block|{
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|ExecDriver
operator|.
name|class
argument_list|)
decl_stmt|;
name|ftOp
operator|=
operator|new
name|FetchOperator
argument_list|(
name|work
operator|.
name|getfWork
argument_list|()
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|unpackBooleanStats
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fName
parameter_list|,
name|ColumnStatisticsObj
name|statsObj
parameter_list|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"counttrues"
argument_list|)
condition|)
block|{
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBooleanStats
argument_list|()
operator|.
name|setNumTrues
argument_list|(
name|v
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
literal|"countfalses"
argument_list|)
condition|)
block|{
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBooleanStats
argument_list|()
operator|.
name|setNumFalses
argument_list|(
name|v
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
literal|"countnulls"
argument_list|)
condition|)
block|{
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBooleanStats
argument_list|()
operator|.
name|setNumNulls
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|unpackDoubleStats
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fName
parameter_list|,
name|ColumnStatisticsObj
name|statsObj
parameter_list|)
block|{
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"countnulls"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
operator|.
name|setNumNulls
argument_list|(
name|v
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
literal|"numdistinctvalues"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
operator|.
name|setNumDVs
argument_list|(
name|v
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
literal|"max"
argument_list|)
condition|)
block|{
name|double
name|d
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
operator|.
name|setHighValue
argument_list|(
name|d
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
literal|"min"
argument_list|)
condition|)
block|{
name|double
name|d
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getDoubleStats
argument_list|()
operator|.
name|setLowValue
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|unpackLongStats
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fName
parameter_list|,
name|ColumnStatisticsObj
name|statsObj
parameter_list|)
block|{
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"countnulls"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
operator|.
name|setNumNulls
argument_list|(
name|v
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
literal|"numdistinctvalues"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
operator|.
name|setNumDVs
argument_list|(
name|v
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
literal|"max"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
operator|.
name|setHighValue
argument_list|(
name|v
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
literal|"min"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getLongStats
argument_list|()
operator|.
name|setLowValue
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|unpackStringStats
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fName
parameter_list|,
name|ColumnStatisticsObj
name|statsObj
parameter_list|)
block|{
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"countnulls"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|setNumNulls
argument_list|(
name|v
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
literal|"numdistinctvalues"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|setNumDVs
argument_list|(
name|v
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
literal|"avglength"
argument_list|)
condition|)
block|{
name|double
name|d
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|setAvgColLen
argument_list|(
name|d
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
literal|"maxlength"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getStringStats
argument_list|()
operator|.
name|setMaxColLen
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|unpackBinaryStats
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fName
parameter_list|,
name|ColumnStatisticsObj
name|statsObj
parameter_list|)
block|{
if|if
condition|(
name|fName
operator|.
name|equals
argument_list|(
literal|"countnulls"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBinaryStats
argument_list|()
operator|.
name|setNumNulls
argument_list|(
name|v
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
literal|"avglength"
argument_list|)
condition|)
block|{
name|double
name|d
init|=
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBinaryStats
argument_list|()
operator|.
name|setAvgColLen
argument_list|(
name|d
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
literal|"maxlength"
argument_list|)
condition|)
block|{
name|long
name|v
init|=
operator|(
operator|(
name|LongObjectInspector
operator|)
name|oi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|getBinaryStats
argument_list|()
operator|.
name|setMaxColLen
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|unpackPrimitiveObject
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ColumnStatisticsObj
name|statsObj
parameter_list|)
block|{
comment|// First infer the type of object
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"columntype"
argument_list|)
condition|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
decl_stmt|;
name|String
name|s
init|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|ColumnStatisticsData
name|statsData
init|=
operator|new
name|ColumnStatisticsData
argument_list|()
decl_stmt|;
if|if
condition|(
name|s
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"long"
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
name|s
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
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
name|s
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
name|StringColumnStatsData
name|stringStats
init|=
operator|new
name|StringColumnStatsData
argument_list|()
decl_stmt|;
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
name|s
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
name|s
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
block|}
else|else
block|{
comment|// invoke the right unpack method depending on data type of the column
if|if
condition|(
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|isSetBooleanStats
argument_list|()
condition|)
block|{
name|unpackBooleanStats
argument_list|(
name|oi
argument_list|,
name|o
argument_list|,
name|fieldName
argument_list|,
name|statsObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|isSetLongStats
argument_list|()
condition|)
block|{
name|unpackLongStats
argument_list|(
name|oi
argument_list|,
name|o
argument_list|,
name|fieldName
argument_list|,
name|statsObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|isSetDoubleStats
argument_list|()
condition|)
block|{
name|unpackDoubleStats
argument_list|(
name|oi
argument_list|,
name|o
argument_list|,
name|fieldName
argument_list|,
name|statsObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|isSetStringStats
argument_list|()
condition|)
block|{
name|unpackStringStats
argument_list|(
name|oi
argument_list|,
name|o
argument_list|,
name|fieldName
argument_list|,
name|statsObj
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|statsObj
operator|.
name|getStatsData
argument_list|()
operator|.
name|isSetBinaryStats
argument_list|()
condition|)
block|{
name|unpackBinaryStats
argument_list|(
name|oi
argument_list|,
name|o
argument_list|,
name|fieldName
argument_list|,
name|statsObj
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|unpackStructObject
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|,
name|String
name|fName
parameter_list|,
name|ColumnStatisticsObj
name|cStatsObj
parameter_list|)
block|{
if|if
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid object datatype : "
operator|+
name|oi
operator|.
name|getCategory
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|oi
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|o
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// Get the field objectInspector, fieldName and the field object.
name|ObjectInspector
name|foi
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|f
init|=
operator|(
name|list
operator|==
literal|null
condition|?
literal|null
else|:
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
decl_stmt|;
name|String
name|fieldName
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
decl_stmt|;
if|if
condition|(
name|foi
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|unpackPrimitiveObject
argument_list|(
name|foi
argument_list|,
name|f
argument_list|,
name|fieldName
argument_list|,
name|cStatsObj
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unpackStructObject
argument_list|(
name|foi
argument_list|,
name|f
argument_list|,
name|fieldName
argument_list|,
name|cStatsObj
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|ColumnStatistics
name|constructColumnStatsFromPackedRow
parameter_list|(
name|ObjectInspector
name|oi
parameter_list|,
name|Object
name|o
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|STRUCT
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unexpected object type encountered while unpacking row"
argument_list|)
throw|;
block|}
name|String
name|dbName
init|=
name|db
operator|.
name|getCurrentDatabase
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
name|work
operator|.
name|getColStats
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|String
name|partName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colName
init|=
name|work
operator|.
name|getColStats
argument_list|()
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
name|work
operator|.
name|getColStats
argument_list|()
operator|.
name|getColType
argument_list|()
decl_stmt|;
name|boolean
name|isTblLevel
init|=
name|work
operator|.
name|getColStats
argument_list|()
operator|.
name|isTblLevel
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isTblLevel
condition|)
block|{
name|partName
operator|=
name|work
operator|.
name|getColStats
argument_list|()
operator|.
name|getPartName
argument_list|()
expr_stmt|;
block|}
name|ColumnStatisticsDesc
name|statsDesc
init|=
name|getColumnStatsDesc
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partName
argument_list|,
name|isTblLevel
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ColumnStatisticsObj
argument_list|>
name|statsObjs
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnStatisticsObj
argument_list|>
argument_list|()
decl_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|oi
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|o
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// Get the field objectInspector, fieldName and the field object.
name|ObjectInspector
name|foi
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|f
init|=
operator|(
name|list
operator|==
literal|null
condition|?
literal|null
else|:
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
decl_stmt|;
name|String
name|fieldName
init|=
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
decl_stmt|;
name|ColumnStatisticsObj
name|statsObj
init|=
operator|new
name|ColumnStatisticsObj
argument_list|()
decl_stmt|;
name|statsObj
operator|.
name|setColName
argument_list|(
name|colName
operator|.
name|get
argument_list|(
name|i
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
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|unpackStructObject
argument_list|(
name|foi
argument_list|,
name|f
argument_list|,
name|fieldName
argument_list|,
name|statsObj
argument_list|)
expr_stmt|;
name|statsObjs
operator|.
name|add
argument_list|(
name|statsObj
argument_list|)
expr_stmt|;
block|}
name|ColumnStatistics
name|colStats
init|=
operator|new
name|ColumnStatistics
argument_list|()
decl_stmt|;
name|colStats
operator|.
name|setStatsDesc
argument_list|(
name|statsDesc
argument_list|)
expr_stmt|;
name|colStats
operator|.
name|setStatsObj
argument_list|(
name|statsObjs
argument_list|)
expr_stmt|;
return|return
name|colStats
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
name|persistPartitionStats
parameter_list|()
throws|throws
name|HiveException
block|{
name|InspectableObject
name|io
init|=
literal|null
decl_stmt|;
comment|// Fetch result of the analyze table .. compute statistics for columns ..
try|try
block|{
name|io
operator|=
name|fetchColumnStats
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|// Construct a column statistics object from the result
name|ColumnStatistics
name|colStats
init|=
name|constructColumnStatsFromPackedRow
argument_list|(
name|io
operator|.
name|oi
argument_list|,
name|io
operator|.
name|o
argument_list|)
decl_stmt|;
comment|// Persist the column statistics object to the metastore
try|try
block|{
name|db
operator|.
name|updatePartitionColumnStatistics
argument_list|(
name|colStats
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|int
name|persistTableStats
parameter_list|()
throws|throws
name|HiveException
block|{
name|InspectableObject
name|io
init|=
literal|null
decl_stmt|;
comment|// Fetch result of the analyze table .. compute statistics for columns ..
try|try
block|{
name|io
operator|=
name|fetchColumnStats
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|// Construct a column statistics object from the result
name|ColumnStatistics
name|colStats
init|=
name|constructColumnStatsFromPackedRow
argument_list|(
name|io
operator|.
name|oi
argument_list|,
name|io
operator|.
name|o
argument_list|)
decl_stmt|;
comment|// Persist the column statistics object to the metastore
try|try
block|{
name|db
operator|.
name|updateTableColumnStatistics
argument_list|(
name|colStats
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
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
if|if
condition|(
name|work
operator|.
name|getColStats
argument_list|()
operator|.
name|isTblLevel
argument_list|()
condition|)
block|{
return|return
name|persistTableStats
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|persistPartitionStats
argument_list|()
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
return|return
literal|1
return|;
block|}
specifier|private
name|InspectableObject
name|fetchColumnStats
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
try|try
block|{
name|InspectableObject
name|io
init|=
name|ftOp
operator|.
name|getNextRow
argument_list|()
decl_stmt|;
if|if
condition|(
name|io
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|CommandNeedRetryException
argument_list|()
throw|;
block|}
return|return
name|io
return|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
literal|"COLUMNSTATS TASK"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|localizeMRTmpFilesImpl
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
name|FetchWork
name|fWork
init|=
name|work
operator|.
name|getfWork
argument_list|()
decl_stmt|;
name|String
name|s
init|=
name|fWork
operator|.
name|getTblDir
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|s
operator|!=
literal|null
operator|)
operator|&&
name|ctx
operator|.
name|isMRTmpFileURI
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|fWork
operator|.
name|setTblDir
argument_list|(
name|ctx
operator|.
name|localizeMRTmpFileURI
argument_list|(
name|s
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|ls
init|=
name|fWork
operator|.
name|getPartDir
argument_list|()
decl_stmt|;
if|if
condition|(
name|ls
operator|!=
literal|null
condition|)
block|{
name|ctx
operator|.
name|localizePaths
argument_list|(
name|ls
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


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
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveIntervalDayTime
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
name|common
operator|.
name|type
operator|.
name|HiveIntervalYearMonth
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
name|exec
operator|.
name|Description
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
name|exec
operator|.
name|NoMatchingMethodException
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
name|exec
operator|.
name|UDFArgumentException
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
name|exec
operator|.
name|UDFArgumentTypeException
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
name|util
operator|.
name|DateTimeMath
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
name|HiveIntervalDayTimeWritable
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
name|HiveIntervalYearMonthWritable
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
name|TimestampWritable
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
name|ObjectInspector
operator|.
name|Category
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
name|ObjectInspectorConverters
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|PrimitiveObjectInspectorFactory
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
name|PrimitiveObjectInspectorUtils
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"+"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns a+b"
argument_list|)
specifier|public
class|class
name|GenericUDFOPDTIPlus
extends|extends
name|GenericUDFBaseDTI
block|{
specifier|protected
specifier|transient
name|DateTimeMath
name|dtm
init|=
operator|new
name|DateTimeMath
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|OperationType
name|plusOpType
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|intervalArg1Idx
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|intervalArg2Idx
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|dtArgIdx
decl_stmt|;
specifier|protected
specifier|transient
name|Converter
name|dtConverter
decl_stmt|;
specifier|protected
specifier|transient
name|TimestampWritable
name|timestampResult
init|=
operator|new
name|TimestampWritable
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|DateWritable
name|dateResult
init|=
operator|new
name|DateWritable
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|HiveIntervalDayTimeWritable
name|intervalDayTimeResult
init|=
operator|new
name|HiveIntervalDayTimeWritable
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|HiveIntervalYearMonthWritable
name|intervalYearMonthResult
init|=
operator|new
name|HiveIntervalYearMonthWritable
argument_list|()
decl_stmt|;
enum|enum
name|OperationType
block|{
name|INTERVALYM_PLUS_INTERVALYM
block|,
name|INTERVALYM_PLUS_DATE
block|,
name|INTERVALYM_PLUS_TIMESTAMP
block|,
name|INTERVALDT_PLUS_INTERVALDT
block|,
name|INTERVALDT_PLUS_TIMESTAMP
block|,   }
empty_stmt|;
specifier|public
name|GenericUDFOPDTIPlus
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"+"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|opName
operator|+
literal|" requires two arguments."
argument_list|)
throw|;
block|}
name|PrimitiveObjectInspector
name|resultOI
init|=
literal|null
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|Category
name|category
init|=
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
decl_stmt|;
if|if
condition|(
name|category
operator|!=
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"The "
operator|+
name|GenericUDFUtils
operator|.
name|getOrdinal
argument_list|(
name|i
operator|+
literal|1
argument_list|)
operator|+
literal|" argument of "
operator|+
name|opName
operator|+
literal|"  is expected to a "
operator|+
name|Category
operator|.
name|PRIMITIVE
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|" type, but "
operator|+
name|category
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|" is found"
argument_list|)
throw|;
block|}
block|}
name|inputOIs
operator|=
operator|new
name|PrimitiveObjectInspector
index|[]
block|{
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
block|,
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
block|}
expr_stmt|;
name|PrimitiveObjectInspector
name|leftOI
init|=
name|inputOIs
index|[
literal|0
index|]
decl_stmt|;
name|PrimitiveObjectInspector
name|rightOI
init|=
name|inputOIs
index|[
literal|1
index|]
decl_stmt|;
comment|// Allowed operations:
comment|// IntervalYearMonth + IntervalYearMonth = IntervalYearMonth
comment|// IntervalYearMonth + Date = Date (operands reversible)
comment|// IntervalYearMonth + Timestamp = Timestamp (operands reversible)
comment|// IntervalDayTime + IntervalDayTime = IntervalDayTime
comment|// IntervalDayTime + Date = Timestamp (operands reversible)
comment|// IntervalDayTime + Timestamp = Timestamp (operands reversible)
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|,
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALYM_PLUS_INTERVALYM
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|0
expr_stmt|;
name|intervalArg2Idx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intervalYearMonthTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|,
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALYM_PLUS_DATE
expr_stmt|;
name|dtArgIdx
operator|=
literal|0
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|dateTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|,
name|PrimitiveCategory
operator|.
name|DATE
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALYM_PLUS_DATE
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|0
expr_stmt|;
name|dtArgIdx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|dateTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|,
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALYM_PLUS_TIMESTAMP
expr_stmt|;
name|dtArgIdx
operator|=
literal|0
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_YEAR_MONTH
argument_list|,
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALYM_PLUS_TIMESTAMP
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|0
expr_stmt|;
name|dtArgIdx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|,
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALDT_PLUS_INTERVALDT
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|0
expr_stmt|;
name|intervalArg2Idx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|intervalDayTimeTypeInfo
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|,
name|PrimitiveCategory
operator|.
name|DATE
argument_list|)
operator|||
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|,
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALDT_PLUS_TIMESTAMP
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|0
expr_stmt|;
name|dtArgIdx
operator|=
literal|1
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
expr_stmt|;
name|dtConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|leftOI
argument_list|,
name|resultOI
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|DATE
argument_list|,
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|)
operator|||
name|checkArgs
argument_list|(
name|PrimitiveCategory
operator|.
name|TIMESTAMP
argument_list|,
name|PrimitiveCategory
operator|.
name|INTERVAL_DAY_TIME
argument_list|)
condition|)
block|{
name|plusOpType
operator|=
name|OperationType
operator|.
name|INTERVALDT_PLUS_TIMESTAMP
expr_stmt|;
name|intervalArg1Idx
operator|=
literal|1
expr_stmt|;
name|dtArgIdx
operator|=
literal|0
expr_stmt|;
name|resultOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
expr_stmt|;
name|dtConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|leftOI
argument_list|,
name|resultOI
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Unsupported types - error
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|argTypeInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|argTypeInfos
operator|.
name|add
argument_list|(
name|leftOI
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
name|argTypeInfos
operator|.
name|add
argument_list|(
name|rightOI
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NoMatchingMethodException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
name|argTypeInfos
argument_list|,
literal|null
argument_list|)
throw|;
block|}
return|return
name|resultOI
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
switch|switch
condition|(
name|plusOpType
condition|)
block|{
case|case
name|INTERVALYM_PLUS_INTERVALYM
case|:
block|{
name|HiveIntervalYearMonth
name|iym1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalYearMonth
argument_list|(
name|arguments
index|[
name|intervalArg1Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg1Idx
index|]
argument_list|)
decl_stmt|;
name|HiveIntervalYearMonth
name|iym2
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalYearMonth
argument_list|(
name|arguments
index|[
name|intervalArg2Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg2Idx
index|]
argument_list|)
decl_stmt|;
return|return
name|handleIntervalYearMonthResult
argument_list|(
name|dtm
operator|.
name|add
argument_list|(
name|iym1
argument_list|,
name|iym2
argument_list|)
argument_list|)
return|;
block|}
case|case
name|INTERVALYM_PLUS_DATE
case|:
block|{
name|HiveIntervalYearMonth
name|iym1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalYearMonth
argument_list|(
name|arguments
index|[
name|intervalArg1Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg1Idx
index|]
argument_list|)
decl_stmt|;
name|Date
name|dt1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDate
argument_list|(
name|arguments
index|[
name|dtArgIdx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|dtArgIdx
index|]
argument_list|)
decl_stmt|;
return|return
name|handleDateResult
argument_list|(
name|dtm
operator|.
name|add
argument_list|(
name|dt1
argument_list|,
name|iym1
argument_list|)
argument_list|)
return|;
block|}
case|case
name|INTERVALYM_PLUS_TIMESTAMP
case|:
block|{
name|HiveIntervalYearMonth
name|iym1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalYearMonth
argument_list|(
name|arguments
index|[
name|intervalArg1Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg1Idx
index|]
argument_list|)
decl_stmt|;
name|Timestamp
name|ts1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|arguments
index|[
name|dtArgIdx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|dtArgIdx
index|]
argument_list|)
decl_stmt|;
return|return
name|handleTimestampResult
argument_list|(
name|dtm
operator|.
name|add
argument_list|(
name|ts1
argument_list|,
name|iym1
argument_list|)
argument_list|)
return|;
block|}
case|case
name|INTERVALDT_PLUS_INTERVALDT
case|:
block|{
name|HiveIntervalDayTime
name|idt1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalDayTime
argument_list|(
name|arguments
index|[
name|intervalArg1Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg1Idx
index|]
argument_list|)
decl_stmt|;
name|HiveIntervalDayTime
name|idt2
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalDayTime
argument_list|(
name|arguments
index|[
name|intervalArg2Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg2Idx
index|]
argument_list|)
decl_stmt|;
return|return
name|handleIntervalDayTimeResult
argument_list|(
name|dtm
operator|.
name|add
argument_list|(
name|idt1
argument_list|,
name|idt2
argument_list|)
argument_list|)
return|;
block|}
case|case
name|INTERVALDT_PLUS_TIMESTAMP
case|:
block|{
name|HiveIntervalDayTime
name|idt1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalDayTime
argument_list|(
name|arguments
index|[
name|intervalArg1Idx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|intervalArg1Idx
index|]
argument_list|)
decl_stmt|;
name|Timestamp
name|ts1
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|arguments
index|[
name|dtArgIdx
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOIs
index|[
name|dtArgIdx
index|]
argument_list|)
decl_stmt|;
return|return
name|handleTimestampResult
argument_list|(
name|dtm
operator|.
name|add
argument_list|(
name|ts1
argument_list|,
name|idt1
argument_list|)
argument_list|)
return|;
block|}
default|default:
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unknown PlusOpType "
operator|+
name|plusOpType
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|DateWritable
name|handleDateResult
parameter_list|(
name|Date
name|result
parameter_list|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|dateResult
operator|.
name|set
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|dateResult
return|;
block|}
specifier|protected
name|TimestampWritable
name|handleTimestampResult
parameter_list|(
name|Timestamp
name|result
parameter_list|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|timestampResult
operator|.
name|set
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|timestampResult
return|;
block|}
specifier|protected
name|HiveIntervalYearMonthWritable
name|handleIntervalYearMonthResult
parameter_list|(
name|HiveIntervalYearMonth
name|result
parameter_list|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|intervalYearMonthResult
operator|.
name|set
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|intervalYearMonthResult
return|;
block|}
specifier|protected
name|HiveIntervalDayTimeWritable
name|handleIntervalDayTimeResult
parameter_list|(
name|HiveIntervalDayTime
name|result
parameter_list|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|intervalDayTimeResult
operator|.
name|set
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|intervalDayTimeResult
return|;
block|}
block|}
end_class

end_unit


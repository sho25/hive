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
name|udf
operator|.
name|generic
package|;
end_package

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
name|parse
operator|.
name|HiveParser
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
name|objectinspector
operator|.
name|ConstantObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveGrouping
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
name|PrimitiveTypeInfo
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|DateUtils
import|;
end_import

begin_comment
comment|/**  * GenericUDF Class for support of "INTERVAL (expression) (DAY|YEAR|...)".  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"internal_interval"
argument_list|,
name|value
operator|=
literal|"_FUNC_(intervalType,intervalArg)"
argument_list|,
name|extended
operator|=
literal|"this method is not designed to be used by directly calling it - it provides internal support for 'INTERVAL (intervalArg) intervalType' constructs"
argument_list|)
specifier|public
class|class
name|GenericUDFInternalInterval
extends|extends
name|GenericUDF
block|{
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|IntervalProcessor
argument_list|>
name|processorMap
decl_stmt|;
specifier|private
specifier|transient
name|IntervalProcessor
name|processor
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
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
comment|// read operation mode
if|if
condition|(
operator|!
operator|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|ConstantObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
name|getFuncName
argument_list|()
operator|+
literal|": may only accept constant as first argument"
argument_list|)
throw|;
block|}
name|Integer
name|operationMode
init|=
name|getConstantIntValue
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|operationMode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"must supply operationmode"
argument_list|)
throw|;
block|}
name|processor
operator|=
name|getProcessorMap
argument_list|()
operator|.
name|get
argument_list|(
name|operationMode
argument_list|)
expr_stmt|;
if|if
condition|(
name|processor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
name|getFuncName
argument_list|()
operator|+
literal|": unsupported operationMode: "
operator|+
name|operationMode
argument_list|)
throw|;
block|}
comment|// check value argument
if|if
condition|(
name|arguments
index|[
literal|1
index|]
operator|.
name|getCategory
argument_list|()
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
literal|1
argument_list|,
literal|"The first argument to "
operator|+
name|getFuncName
argument_list|()
operator|+
literal|" must be primitive"
argument_list|)
throw|;
block|}
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
expr_stmt|;
name|PrimitiveCategory
name|inputCategory
init|=
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isValidInputCategory
argument_list|(
name|inputCategory
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"The second argument to "
operator|+
name|getFuncName
argument_list|()
operator|+
literal|" must be from the string group or numeric group (except:float/double)"
argument_list|)
throw|;
block|}
if|if
condition|(
name|arguments
index|[
literal|1
index|]
operator|instanceof
name|ConstantObjectInspector
condition|)
block|{
comment|// return value as constant in case arg is constant
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableConstantObjectInspector
argument_list|(
name|processor
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|processor
operator|.
name|evaluate
argument_list|(
name|getConstantStringValue
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|processor
operator|.
name|getTypeInfo
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|private
name|boolean
name|isValidInputCategory
parameter_list|(
name|PrimitiveCategory
name|cat
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|PrimitiveGrouping
name|inputOIGroup
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|cat
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputOIGroup
operator|==
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|inputOIGroup
operator|==
name|PrimitiveGrouping
operator|.
name|NUMERIC_GROUP
condition|)
block|{
switch|switch
condition|(
name|cat
condition|)
block|{
case|case
name|DOUBLE
case|:
case|case
name|FLOAT
case|:
return|return
literal|false
return|;
default|default:
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
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
name|String
name|argString
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
if|if
condition|(
name|argString
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
return|return
name|processor
operator|.
name|evaluate
argument_list|(
name|argString
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Error parsing interval "
operator|+
name|argString
operator|+
literal|" using:"
operator|+
name|processor
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
interface|interface
name|IntervalProcessor
block|{
name|Integer
name|getKey
parameter_list|()
function_decl|;
name|PrimitiveTypeInfo
name|getTypeInfo
parameter_list|()
function_decl|;
name|Object
name|evaluate
parameter_list|(
name|String
name|arg
parameter_list|)
throws|throws
name|UDFArgumentException
function_decl|;
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|AbstractDayTimeIntervalProcessor
implements|implements
name|IntervalProcessor
block|{
specifier|private
specifier|transient
name|HiveIntervalDayTimeWritable
name|intervalResult
init|=
operator|new
name|HiveIntervalDayTimeWritable
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|final
name|PrimitiveTypeInfo
name|getTypeInfo
parameter_list|()
block|{
return|return
name|TypeInfoFactory
operator|.
name|intervalDayTimeTypeInfo
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Object
name|evaluate
parameter_list|(
name|String
name|arg
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|intervalResult
operator|.
name|set
argument_list|(
name|getIntervalDayTime
argument_list|(
name|arg
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|intervalResult
return|;
block|}
specifier|abstract
specifier|protected
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|String
name|arg
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|AbstractYearMonthIntervalProcessor
implements|implements
name|IntervalProcessor
block|{
specifier|private
specifier|transient
name|HiveIntervalYearMonthWritable
name|intervalResult
init|=
operator|new
name|HiveIntervalYearMonthWritable
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|final
name|PrimitiveTypeInfo
name|getTypeInfo
parameter_list|()
block|{
return|return
name|TypeInfoFactory
operator|.
name|intervalYearMonthTypeInfo
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|Object
name|evaluate
parameter_list|(
name|String
name|arg
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|intervalResult
operator|.
name|set
argument_list|(
name|getIntervalYearMonth
argument_list|(
name|arg
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|intervalResult
return|;
block|}
specifier|abstract
specifier|protected
name|HiveIntervalYearMonth
name|getIntervalYearMonth
parameter_list|(
name|String
name|arg
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|static
class|class
name|IntervalDayLiteralProcessor
extends|extends
name|AbstractDayTimeIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_DAY_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
operator|new
name|HiveIntervalDayTime
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalHourLiteralProcessor
extends|extends
name|AbstractDayTimeIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_HOUR_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
operator|new
name|HiveIntervalDayTime
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalMinuteLiteralProcessor
extends|extends
name|AbstractDayTimeIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_MINUTE_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
operator|new
name|HiveIntervalDayTime
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalSecondLiteralProcessor
extends|extends
name|AbstractDayTimeIntervalProcessor
block|{
specifier|private
specifier|static
specifier|final
name|BigDecimal
name|NANOS_PER_SEC_BD
init|=
operator|new
name|BigDecimal
argument_list|(
name|DateUtils
operator|.
name|NANOS_PER_SEC
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_SECOND_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
name|BigDecimal
name|bd
init|=
operator|new
name|BigDecimal
argument_list|(
name|arg
argument_list|)
decl_stmt|;
name|BigDecimal
name|bdSeconds
init|=
operator|new
name|BigDecimal
argument_list|(
name|bd
operator|.
name|toBigInteger
argument_list|()
argument_list|)
decl_stmt|;
name|BigDecimal
name|bdNanos
init|=
name|bd
operator|.
name|subtract
argument_list|(
name|bdSeconds
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveIntervalDayTime
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|bdSeconds
operator|.
name|intValueExact
argument_list|()
argument_list|,
name|bdNanos
operator|.
name|multiply
argument_list|(
name|NANOS_PER_SEC_BD
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalDayTimeLiteralProcessor
extends|extends
name|AbstractDayTimeIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_DAY_TIME_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
name|HiveIntervalDayTime
operator|.
name|valueOf
argument_list|(
name|arg
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalYearMonthLiteralProcessor
extends|extends
name|AbstractYearMonthIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_YEAR_MONTH_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalYearMonth
name|getIntervalYearMonth
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
name|HiveIntervalYearMonth
operator|.
name|valueOf
argument_list|(
name|arg
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalYearLiteralProcessor
extends|extends
name|AbstractYearMonthIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_YEAR_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalYearMonth
name|getIntervalYearMonth
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
operator|new
name|HiveIntervalYearMonth
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
argument_list|)
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntervalMonthLiteralProcessor
extends|extends
name|AbstractYearMonthIntervalProcessor
block|{
annotation|@
name|Override
specifier|public
name|Integer
name|getKey
parameter_list|()
block|{
return|return
name|HiveParser
operator|.
name|TOK_INTERVAL_MONTH_LITERAL
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveIntervalYearMonth
name|getIntervalYearMonth
parameter_list|(
name|String
name|arg
parameter_list|)
block|{
return|return
operator|new
name|HiveIntervalYearMonth
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|arg
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|IntervalProcessor
argument_list|>
name|getProcessorMap
parameter_list|()
block|{
if|if
condition|(
name|processorMap
operator|!=
literal|null
condition|)
block|{
return|return
name|processorMap
return|;
block|}
name|Map
argument_list|<
name|Integer
argument_list|,
name|IntervalProcessor
argument_list|>
name|ret
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|IntervalProcessor
name|ips
index|[]
init|=
operator|new
name|IntervalProcessor
index|[]
block|{
operator|new
name|IntervalDayTimeLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalDayLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalHourLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalMinuteLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalSecondLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalYearMonthLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalYearLiteralProcessor
argument_list|()
block|,
operator|new
name|IntervalMonthLiteralProcessor
argument_list|()
block|,     }
decl_stmt|;
for|for
control|(
name|IntervalProcessor
name|ip
range|:
name|ips
control|)
block|{
name|ret
operator|.
name|put
argument_list|(
name|ip
operator|.
name|getKey
argument_list|()
argument_list|,
name|ip
argument_list|)
expr_stmt|;
block|}
return|return
name|processorMap
operator|=
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s(%s)"
argument_list|,
name|processor
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|children
index|[
literal|1
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit


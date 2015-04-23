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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
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
name|HiveChar
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
name|HiveDecimal
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
name|HiveVarchar
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
name|ByteStream
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
name|HiveCharWritable
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
name|HiveVarcharWritable
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
name|lazy
operator|.
name|LazyInteger
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
name|lazy
operator|.
name|LazyLong
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
name|io
operator|.
name|BytesWritable
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * PrimitiveObjectInspectorConverter.  *  */
end_comment

begin_class
specifier|public
class|class
name|PrimitiveObjectInspectorConverter
block|{
comment|/**    * A converter for the byte type.    */
specifier|public
specifier|static
class|class
name|BooleanConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableBooleanObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|BooleanConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableBooleanObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getBoolean
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the byte type.    */
specifier|public
specifier|static
class|class
name|ByteConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableByteObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|ByteConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableByteObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getByte
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the short type.    */
specifier|public
specifier|static
class|class
name|ShortConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableShortObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|ShortConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableShortObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getShort
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the int type.    */
specifier|public
specifier|static
class|class
name|IntConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableIntObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|IntConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableIntObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the long type.    */
specifier|public
specifier|static
class|class
name|LongConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableLongObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|LongConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableLongObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getLong
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the float type.    */
specifier|public
specifier|static
class|class
name|FloatConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableFloatObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|FloatConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableFloatObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getFloat
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * A converter for the double type.    */
specifier|public
specifier|static
class|class
name|DoubleConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableDoubleObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|DoubleConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableDoubleObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
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
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|DateConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableDateObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|DateConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableDateObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getDate
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TimestampConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableTimestampObjectInspector
name|outputOI
decl_stmt|;
name|boolean
name|intToTimestampInSeconds
init|=
literal|false
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|TimestampConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableTimestampObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|new
name|Timestamp
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setIntToTimestampInSeconds
parameter_list|(
name|boolean
name|intToTimestampInSeconds
parameter_list|)
block|{
name|this
operator|.
name|intToTimestampInSeconds
operator|=
name|intToTimestampInSeconds
expr_stmt|;
block|}
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getTimestamp
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|,
name|intToTimestampInSeconds
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HiveIntervalYearMonthConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableHiveIntervalYearMonthObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|HiveIntervalYearMonthConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableHiveIntervalYearMonthObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|new
name|HiveIntervalYearMonth
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalYearMonth
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HiveIntervalDayTimeConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableHiveIntervalDayTimeObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|HiveIntervalDayTimeConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableHiveIntervalDayTimeObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|new
name|HiveIntervalDayTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveIntervalDayTime
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HiveDecimalConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableHiveDecimalObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|HiveDecimalConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableHiveDecimalObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|this
operator|.
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveDecimal
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|BinaryConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableBinaryObjectInspector
name|outputOI
decl_stmt|;
name|Object
name|r
decl_stmt|;
specifier|public
name|BinaryConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableBinaryObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|r
operator|=
name|outputOI
operator|.
name|create
argument_list|(
operator|new
name|byte
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getBinary
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * A helper class to convert any primitive to Text.    */
specifier|public
specifier|static
class|class
name|TextConverter
implements|implements
name|Converter
block|{
specifier|private
specifier|final
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|private
specifier|final
name|Text
name|t
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ByteStream
operator|.
name|Output
name|out
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|trueBytes
init|=
block|{
literal|'T'
block|,
literal|'R'
block|,
literal|'U'
block|,
literal|'E'
block|}
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|falseBytes
init|=
block|{
literal|'F'
block|,
literal|'A'
block|,
literal|'L'
block|,
literal|'S'
block|,
literal|'E'
block|}
decl_stmt|;
specifier|public
name|TextConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|)
block|{
comment|// The output ObjectInspector is writableStringObjectInspector.
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
block|}
specifier|public
name|Text
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|VOID
case|:
return|return
literal|null
return|;
case|case
name|BOOLEAN
case|:
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
condition|?
name|trueBytes
else|:
name|falseBytes
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|BYTE
case|:
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyInteger
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|SHORT
case|:
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyInteger
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|INT
case|:
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyInteger
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|IntObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|LONG
case|:
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
name|LazyLong
operator|.
name|writeUTF8NoException
argument_list|(
name|out
argument_list|,
operator|(
operator|(
name|LongObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|set
argument_list|(
name|out
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|out
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|FLOAT
case|:
name|t
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|DOUBLE
case|:
name|t
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|STRING
case|:
if|if
condition|(
name|inputOI
operator|.
name|preferWritable
argument_list|()
condition|)
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
case|case
name|CHAR
case|:
comment|// when converting from char, the value should be stripped of any trailing spaces.
if|if
condition|(
name|inputOI
operator|.
name|preferWritable
argument_list|()
condition|)
block|{
comment|// char text value is already stripped of trailing space
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveCharObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|getStrippedValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveCharObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|input
argument_list|)
operator|.
name|getStrippedValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
case|case
name|VARCHAR
case|:
if|if
condition|(
name|inputOI
operator|.
name|preferWritable
argument_list|()
condition|)
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveVarcharObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveVarcharObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
case|case
name|DATE
case|:
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|DateObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|TIMESTAMP
case|:
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|TimestampObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|INTERVAL_YEAR_MONTH
case|:
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveIntervalYearMonthObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|INTERVAL_DAY_TIME
case|:
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveIntervalDayTimeObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
case|case
name|BINARY
case|:
name|BinaryObjectInspector
name|binaryOI
init|=
operator|(
name|BinaryObjectInspector
operator|)
name|inputOI
decl_stmt|;
if|if
condition|(
name|binaryOI
operator|.
name|preferWritable
argument_list|()
condition|)
block|{
name|BytesWritable
name|bytes
init|=
name|binaryOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|t
operator|.
name|set
argument_list|(
name|bytes
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|t
operator|.
name|set
argument_list|(
name|binaryOI
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
case|case
name|DECIMAL
case|:
name|t
operator|.
name|set
argument_list|(
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|input
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Hive 2 Internal error: type = "
operator|+
name|inputOI
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * A helper class to convert any primitive to String.    */
specifier|public
specifier|static
class|class
name|StringConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|public
name|StringConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|)
block|{
comment|// The output ObjectInspector is writableStringObjectInspector.
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HiveVarcharConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableHiveVarcharObjectInspector
name|outputOI
decl_stmt|;
name|HiveVarcharWritable
name|hc
decl_stmt|;
specifier|public
name|HiveVarcharConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableHiveVarcharObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
comment|// unfortunately we seem to get instances of varchar object inspectors without params
comment|// when an old-style UDF has an evaluate() method with varchar arguments.
comment|// If we disallow varchar in old-style UDFs and only allow GenericUDFs to be defined
comment|// with varchar arguments, then we might be able to enforce this properly.
comment|//if (typeParams == null) {
comment|//  throw new RuntimeException("varchar type used without type params");
comment|//}
name|hc
operator|=
operator|new
name|HiveVarcharWritable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|hc
argument_list|,
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
condition|?
operator|new
name|HiveVarchar
argument_list|(
literal|"TRUE"
argument_list|,
operator|-
literal|1
argument_list|)
else|:
operator|new
name|HiveVarchar
argument_list|(
literal|"FALSE"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
return|;
default|default:
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|hc
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveVarchar
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|HiveCharConverter
implements|implements
name|Converter
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
name|SettableHiveCharObjectInspector
name|outputOI
decl_stmt|;
name|HiveCharWritable
name|hc
decl_stmt|;
specifier|public
name|HiveCharConverter
parameter_list|(
name|PrimitiveObjectInspector
name|inputOI
parameter_list|,
name|SettableHiveCharObjectInspector
name|outputOI
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|outputOI
operator|=
name|outputOI
expr_stmt|;
name|hc
operator|=
operator|new
name|HiveCharWritable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|input
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
switch|switch
condition|(
name|inputOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|hc
argument_list|,
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|inputOI
operator|)
operator|.
name|get
argument_list|(
name|input
argument_list|)
condition|?
operator|new
name|HiveChar
argument_list|(
literal|"TRUE"
argument_list|,
operator|-
literal|1
argument_list|)
else|:
operator|new
name|HiveChar
argument_list|(
literal|"FALSE"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
return|;
default|default:
return|return
name|outputOI
operator|.
name|set
argument_list|(
name|hc
argument_list|,
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveChar
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit


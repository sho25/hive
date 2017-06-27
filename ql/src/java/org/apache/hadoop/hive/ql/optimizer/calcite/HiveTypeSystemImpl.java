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
name|optimizer
operator|.
name|calcite
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataTypeSystemImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
import|;
end_import

begin_class
specifier|public
class|class
name|HiveTypeSystemImpl
extends|extends
name|RelDataTypeSystemImpl
block|{
comment|// TODO: This should come from type system; Currently there is no definition
comment|// in type system for this.
specifier|private
specifier|static
specifier|final
name|int
name|MAX_DECIMAL_PRECISION
init|=
literal|38
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_DECIMAL_SCALE
init|=
literal|38
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_DECIMAL_PRECISION
init|=
literal|10
decl_stmt|;
comment|// STRING type in Hive is represented as VARCHAR with precision Integer.MAX_VALUE.
comment|// In turn, the max VARCHAR precision should be 65535. However, the value is not
comment|// used for validation, but rather only internally by the optimizer to know the max
comment|// precision supported by the system. Thus, no VARCHAR precision should fall between
comment|// 65535 and Integer.MAX_VALUE; the check for VARCHAR precision is done in Hive.
specifier|private
specifier|static
specifier|final
name|int
name|MAX_CHAR_PRECISION
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_VARCHAR_PRECISION
init|=
literal|65535
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_CHAR_PRECISION
init|=
literal|255
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_BINARY_PRECISION
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_TIMESTAMP_PRECISION
init|=
literal|9
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|getMaxScale
parameter_list|(
name|SqlTypeName
name|typeName
parameter_list|)
block|{
switch|switch
condition|(
name|typeName
condition|)
block|{
case|case
name|DECIMAL
case|:
return|return
name|getMaxNumericScale
argument_list|()
return|;
case|case
name|INTERVAL_YEAR
case|:
case|case
name|INTERVAL_MONTH
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
case|case
name|INTERVAL_DAY
case|:
case|case
name|INTERVAL_DAY_HOUR
case|:
case|case
name|INTERVAL_DAY_MINUTE
case|:
case|case
name|INTERVAL_DAY_SECOND
case|:
case|case
name|INTERVAL_HOUR
case|:
case|case
name|INTERVAL_HOUR_MINUTE
case|:
case|case
name|INTERVAL_HOUR_SECOND
case|:
case|case
name|INTERVAL_MINUTE
case|:
case|case
name|INTERVAL_MINUTE_SECOND
case|:
case|case
name|INTERVAL_SECOND
case|:
return|return
name|SqlTypeName
operator|.
name|MAX_INTERVAL_FRACTIONAL_SECOND_PRECISION
return|;
default|default:
return|return
operator|-
literal|1
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getDefaultPrecision
parameter_list|(
name|SqlTypeName
name|typeName
parameter_list|)
block|{
switch|switch
condition|(
name|typeName
condition|)
block|{
comment|// Hive will always require user to specify exact sizes for char, varchar;
comment|// Binary doesn't need any sizes; Decimal has the default of 10.
case|case
name|BINARY
case|:
case|case
name|VARBINARY
case|:
case|case
name|TIME
case|:
case|case
name|TIMESTAMP
case|:
return|return
name|getMaxPrecision
argument_list|(
name|typeName
argument_list|)
return|;
case|case
name|CHAR
case|:
return|return
name|DEFAULT_CHAR_PRECISION
return|;
case|case
name|VARCHAR
case|:
return|return
name|DEFAULT_VARCHAR_PRECISION
return|;
case|case
name|DECIMAL
case|:
return|return
name|DEFAULT_DECIMAL_PRECISION
return|;
case|case
name|INTERVAL_YEAR
case|:
case|case
name|INTERVAL_MONTH
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
case|case
name|INTERVAL_DAY
case|:
case|case
name|INTERVAL_DAY_HOUR
case|:
case|case
name|INTERVAL_DAY_MINUTE
case|:
case|case
name|INTERVAL_DAY_SECOND
case|:
case|case
name|INTERVAL_HOUR
case|:
case|case
name|INTERVAL_HOUR_MINUTE
case|:
case|case
name|INTERVAL_HOUR_SECOND
case|:
case|case
name|INTERVAL_MINUTE
case|:
case|case
name|INTERVAL_MINUTE_SECOND
case|:
case|case
name|INTERVAL_SECOND
case|:
return|return
name|SqlTypeName
operator|.
name|DEFAULT_INTERVAL_START_PRECISION
return|;
default|default:
return|return
operator|-
literal|1
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxPrecision
parameter_list|(
name|SqlTypeName
name|typeName
parameter_list|)
block|{
switch|switch
condition|(
name|typeName
condition|)
block|{
case|case
name|DECIMAL
case|:
return|return
name|getMaxNumericPrecision
argument_list|()
return|;
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
return|return
name|MAX_CHAR_PRECISION
return|;
case|case
name|VARBINARY
case|:
case|case
name|BINARY
case|:
return|return
name|MAX_BINARY_PRECISION
return|;
case|case
name|TIME
case|:
case|case
name|TIMESTAMP
case|:
return|return
name|MAX_TIMESTAMP_PRECISION
return|;
case|case
name|INTERVAL_YEAR
case|:
case|case
name|INTERVAL_MONTH
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
case|case
name|INTERVAL_DAY
case|:
case|case
name|INTERVAL_DAY_HOUR
case|:
case|case
name|INTERVAL_DAY_MINUTE
case|:
case|case
name|INTERVAL_DAY_SECOND
case|:
case|case
name|INTERVAL_HOUR
case|:
case|case
name|INTERVAL_HOUR_MINUTE
case|:
case|case
name|INTERVAL_HOUR_SECOND
case|:
case|case
name|INTERVAL_MINUTE
case|:
case|case
name|INTERVAL_MINUTE_SECOND
case|:
case|case
name|INTERVAL_SECOND
case|:
return|return
name|SqlTypeName
operator|.
name|MAX_INTERVAL_START_PRECISION
return|;
default|default:
return|return
operator|-
literal|1
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxNumericScale
parameter_list|()
block|{
return|return
name|MAX_DECIMAL_SCALE
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxNumericPrecision
parameter_list|()
block|{
return|return
name|MAX_DECIMAL_PRECISION
return|;
block|}
block|}
end_class

end_unit


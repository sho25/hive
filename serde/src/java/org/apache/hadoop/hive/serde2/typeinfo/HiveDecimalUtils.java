begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|typeinfo
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
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
import|;
end_import

begin_class
specifier|public
class|class
name|HiveDecimalUtils
block|{
specifier|public
specifier|static
name|HiveDecimal
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|,
name|DecimalTypeInfo
name|typeInfo
parameter_list|)
block|{
return|return
name|enforcePrecisionScale
argument_list|(
name|dec
argument_list|,
name|typeInfo
operator|.
name|precision
argument_list|()
argument_list|,
name|typeInfo
operator|.
name|scale
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|,
name|int
name|maxPrecision
parameter_list|,
name|int
name|maxScale
parameter_list|)
block|{
if|if
condition|(
name|dec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Minor optimization, avoiding creating new objects.
if|if
condition|(
name|dec
operator|.
name|precision
argument_list|()
operator|-
name|dec
operator|.
name|scale
argument_list|()
operator|<=
name|maxPrecision
operator|-
name|maxScale
operator|&&
name|dec
operator|.
name|scale
argument_list|()
operator|<=
name|maxScale
condition|)
block|{
return|return
name|dec
return|;
block|}
name|BigDecimal
name|bd
init|=
name|HiveDecimal
operator|.
name|enforcePrecisionScale
argument_list|(
name|dec
operator|.
name|bigDecimalValue
argument_list|()
argument_list|,
name|maxPrecision
argument_list|,
name|maxScale
argument_list|)
decl_stmt|;
if|if
condition|(
name|bd
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|HiveDecimal
operator|.
name|create
argument_list|(
name|bd
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimalWritable
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|,
name|DecimalTypeInfo
name|typeInfo
parameter_list|)
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveDecimal
name|dec
init|=
name|enforcePrecisionScale
argument_list|(
name|writable
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|typeInfo
argument_list|)
decl_stmt|;
return|return
name|dec
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimalWritable
argument_list|(
name|dec
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimalWritable
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|,
name|int
name|precision
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HiveDecimal
name|dec
init|=
name|enforcePrecisionScale
argument_list|(
name|writable
operator|.
name|getHiveDecimal
argument_list|()
argument_list|,
name|precision
argument_list|,
name|scale
argument_list|)
decl_stmt|;
return|return
name|dec
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimalWritable
argument_list|(
name|dec
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|validateParameter
parameter_list|(
name|int
name|precision
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
if|if
condition|(
name|precision
argument_list|<
literal|1
operator|||
name|precision
argument_list|>
name|HiveDecimal
operator|.
name|MAX_PRECISION
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Decimal precision out of allowed range [1,"
operator|+
name|HiveDecimal
operator|.
name|MAX_PRECISION
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|scale
argument_list|<
literal|0
operator|||
name|scale
argument_list|>
name|HiveDecimal
operator|.
name|MAX_SCALE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Decimal scale out of allowed range [0,"
operator|+
name|HiveDecimal
operator|.
name|MAX_SCALE
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|precision
operator|<
name|scale
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Decimal scale must be less than or equal to precision"
argument_list|)
throw|;
block|}
block|}
comment|/**    * Need to keep consistent with JdbcColumn.columnPrecision    *    */
specifier|public
specifier|static
name|int
name|getPrecisionForType
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|DECIMAL
case|:
return|return
operator|(
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|precision
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
literal|7
return|;
case|case
name|DOUBLE
case|:
return|return
literal|15
return|;
case|case
name|BYTE
case|:
return|return
literal|3
return|;
case|case
name|SHORT
case|:
return|return
literal|5
return|;
case|case
name|INT
case|:
return|return
literal|10
return|;
case|case
name|LONG
case|:
return|return
literal|19
return|;
case|case
name|VOID
case|:
return|return
literal|1
return|;
default|default:
return|return
name|HiveDecimal
operator|.
name|MAX_PRECISION
return|;
block|}
block|}
comment|/**    * Need to keep consistent with JdbcColumn.columnScale()    *    */
specifier|public
specifier|static
name|int
name|getScaleForType
parameter_list|(
name|PrimitiveTypeInfo
name|typeInfo
parameter_list|)
block|{
switch|switch
condition|(
name|typeInfo
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|DECIMAL
case|:
return|return
operator|(
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|scale
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
literal|7
return|;
case|case
name|DOUBLE
case|:
return|return
literal|15
return|;
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|VOID
case|:
return|return
literal|0
return|;
default|default:
return|return
name|HiveDecimal
operator|.
name|MAX_SCALE
return|;
block|}
block|}
block|}
end_class

end_unit


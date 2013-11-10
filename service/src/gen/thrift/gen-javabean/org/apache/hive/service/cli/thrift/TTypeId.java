begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.9.0)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
package|;
end_package

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
name|HashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
import|;
end_import

begin_enum
specifier|public
enum|enum
name|TTypeId
implements|implements
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TEnum
block|{
name|BOOLEAN_TYPE
argument_list|(
literal|0
argument_list|)
block|,
name|TINYINT_TYPE
argument_list|(
literal|1
argument_list|)
block|,
name|SMALLINT_TYPE
argument_list|(
literal|2
argument_list|)
block|,
name|INT_TYPE
argument_list|(
literal|3
argument_list|)
block|,
name|BIGINT_TYPE
argument_list|(
literal|4
argument_list|)
block|,
name|FLOAT_TYPE
argument_list|(
literal|5
argument_list|)
block|,
name|DOUBLE_TYPE
argument_list|(
literal|6
argument_list|)
block|,
name|STRING_TYPE
argument_list|(
literal|7
argument_list|)
block|,
name|TIMESTAMP_TYPE
argument_list|(
literal|8
argument_list|)
block|,
name|BINARY_TYPE
argument_list|(
literal|9
argument_list|)
block|,
name|ARRAY_TYPE
argument_list|(
literal|10
argument_list|)
block|,
name|MAP_TYPE
argument_list|(
literal|11
argument_list|)
block|,
name|STRUCT_TYPE
argument_list|(
literal|12
argument_list|)
block|,
name|UNION_TYPE
argument_list|(
literal|13
argument_list|)
block|,
name|USER_DEFINED_TYPE
argument_list|(
literal|14
argument_list|)
block|,
name|DECIMAL_TYPE
argument_list|(
literal|15
argument_list|)
block|,
name|NULL_TYPE
argument_list|(
literal|16
argument_list|)
block|,
name|DATE_TYPE
argument_list|(
literal|17
argument_list|)
block|,
name|VARCHAR_TYPE
argument_list|(
literal|18
argument_list|)
block|,
name|CHAR_TYPE
argument_list|(
literal|19
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
specifier|private
name|TTypeId
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Get the integer value of this enum value, as defined in the Thrift IDL.    */
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**    * Find a the enum type by its integer value, as defined in the Thrift IDL.    * @return null if the value is not found.    */
specifier|public
specifier|static
name|TTypeId
name|findByValue
parameter_list|(
name|int
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|value
condition|)
block|{
case|case
literal|0
case|:
return|return
name|BOOLEAN_TYPE
return|;
case|case
literal|1
case|:
return|return
name|TINYINT_TYPE
return|;
case|case
literal|2
case|:
return|return
name|SMALLINT_TYPE
return|;
case|case
literal|3
case|:
return|return
name|INT_TYPE
return|;
case|case
literal|4
case|:
return|return
name|BIGINT_TYPE
return|;
case|case
literal|5
case|:
return|return
name|FLOAT_TYPE
return|;
case|case
literal|6
case|:
return|return
name|DOUBLE_TYPE
return|;
case|case
literal|7
case|:
return|return
name|STRING_TYPE
return|;
case|case
literal|8
case|:
return|return
name|TIMESTAMP_TYPE
return|;
case|case
literal|9
case|:
return|return
name|BINARY_TYPE
return|;
case|case
literal|10
case|:
return|return
name|ARRAY_TYPE
return|;
case|case
literal|11
case|:
return|return
name|MAP_TYPE
return|;
case|case
literal|12
case|:
return|return
name|STRUCT_TYPE
return|;
case|case
literal|13
case|:
return|return
name|UNION_TYPE
return|;
case|case
literal|14
case|:
return|return
name|USER_DEFINED_TYPE
return|;
case|case
literal|15
case|:
return|return
name|DECIMAL_TYPE
return|;
case|case
literal|16
case|:
return|return
name|NULL_TYPE
return|;
case|case
literal|17
case|:
return|return
name|DATE_TYPE
return|;
case|case
literal|18
case|:
return|return
name|VARCHAR_TYPE
return|;
case|case
literal|19
case|:
return|return
name|CHAR_TYPE
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_enum

end_unit


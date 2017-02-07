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
name|hive
operator|.
name|common
operator|.
name|util
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
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_comment
comment|/**  * DateUtils. Thread-safe class  *  */
end_comment

begin_class
specifier|public
class|class
name|DateUtils
block|{
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|SimpleDateFormat
argument_list|>
name|dateFormatLocal
init|=
operator|new
name|ThreadLocal
argument_list|<
name|SimpleDateFormat
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|SimpleDateFormat
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|public
specifier|static
name|SimpleDateFormat
name|getDateFormat
parameter_list|()
block|{
return|return
name|dateFormatLocal
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
specifier|final
name|int
name|NANOS_PER_SEC
init|=
literal|1000000000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|BigDecimal
name|MAX_INT_BD
init|=
operator|new
name|BigDecimal
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|BigDecimal
name|NANOS_PER_SEC_BD
init|=
operator|new
name|BigDecimal
argument_list|(
name|NANOS_PER_SEC
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|int
name|parseNumericValueWithRange
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
name|strVal
parameter_list|,
name|int
name|minValue
parameter_list|,
name|int
name|maxValue
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|int
name|result
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|strVal
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|strVal
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
argument_list|<
name|minValue
operator|||
name|result
argument_list|>
name|maxValue
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s value %d outside range [%d, %d]"
argument_list|,
name|fieldName
argument_list|,
name|result
argument_list|,
name|minValue
argument_list|,
name|maxValue
argument_list|)
argument_list|)
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|// From java.util.Calendar
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|FIELD_NAME
init|=
block|{
literal|"ERA"
block|,
literal|"YEAR"
block|,
literal|"MONTH"
block|,
literal|"WEEK_OF_YEAR"
block|,
literal|"WEEK_OF_MONTH"
block|,
literal|"DAY_OF_MONTH"
block|,
literal|"DAY_OF_YEAR"
block|,
literal|"DAY_OF_WEEK"
block|,
literal|"DAY_OF_WEEK_IN_MONTH"
block|,
literal|"AM_PM"
block|,
literal|"HOUR"
block|,
literal|"HOUR_OF_DAY"
block|,
literal|"MINUTE"
block|,
literal|"SECOND"
block|,
literal|"MILLISECOND"
block|,
literal|"ZONE_OFFSET"
block|,
literal|"DST_OFFSET"
block|}
decl_stmt|;
comment|/**    * Returns the name of the specified calendar field.    *    * @param field the calendar field    * @return the calendar field name    * @exception IndexOutOfBoundsException if<code>field</code> is negative,    * equal to or greater then<code>FIELD_COUNT</code>.    */
specifier|public
specifier|static
name|String
name|getFieldName
parameter_list|(
name|int
name|field
parameter_list|)
block|{
return|return
name|FIELD_NAME
index|[
name|field
index|]
return|;
block|}
block|}
end_class

end_unit


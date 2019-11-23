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
name|exec
operator|.
name|vector
package|;
end_package

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|GregorianCalendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * This class extends LongColumnVector in order to introduce some date-specific semantics. In  * DateColumnVector, the elements of vector[] represent the days since 1970-01-01  */
end_comment

begin_class
specifier|public
class|class
name|DateColumnVector
extends|extends
name|LongColumnVector
block|{
specifier|private
specifier|static
specifier|final
name|TimeZone
name|UTC
init|=
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|GregorianCalendar
name|PROLEPTIC_GREGORIAN_CALENDAR
init|=
operator|new
name|GregorianCalendar
argument_list|(
name|UTC
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|GregorianCalendar
name|GREGORIAN_CALENDAR
init|=
operator|new
name|GregorianCalendar
argument_list|(
name|UTC
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SimpleDateFormat
name|PROLEPTIC_GREGORIAN_DATE_FORMATTER
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|SimpleDateFormat
name|GREGORIAN_DATE_FORMATTER
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
decl_stmt|;
comment|/**   * -141427: hybrid: 1582-10-15 proleptic: 1582-10-15   * -141428: hybrid: 1582-10-04 proleptic: 1582-10-14   */
specifier|private
specifier|static
specifier|final
name|int
name|CUTOVER_DAY_EPOCH
init|=
operator|-
literal|141427
decl_stmt|;
comment|// it's 1582-10-15 in both calendars
static|static
block|{
name|PROLEPTIC_GREGORIAN_CALENDAR
operator|.
name|setGregorianChange
argument_list|(
operator|new
name|java
operator|.
name|util
operator|.
name|Date
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|PROLEPTIC_GREGORIAN_DATE_FORMATTER
operator|.
name|setCalendar
argument_list|(
name|PROLEPTIC_GREGORIAN_CALENDAR
argument_list|)
expr_stmt|;
name|GREGORIAN_DATE_FORMATTER
operator|.
name|setCalendar
argument_list|(
name|GREGORIAN_CALENDAR
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|usingProlepticCalendar
init|=
literal|false
decl_stmt|;
specifier|public
name|DateColumnVector
parameter_list|()
block|{
name|this
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Change the calendar to or from proleptic. If the new and old values of the flag are the same,    * nothing is done. useProleptic - set the flag for the proleptic calendar updateData - change the    * data to match the new value of the flag.    */
specifier|public
name|void
name|changeCalendar
parameter_list|(
name|boolean
name|useProleptic
parameter_list|,
name|boolean
name|updateData
parameter_list|)
block|{
if|if
condition|(
name|useProleptic
operator|==
name|usingProlepticCalendar
condition|)
block|{
return|return;
block|}
name|usingProlepticCalendar
operator|=
name|useProleptic
expr_stmt|;
if|if
condition|(
name|updateData
condition|)
block|{
try|try
block|{
name|updateDataAccordingProlepticSetting
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|updateDataAccordingProlepticSetting
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|vector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|vector
index|[
name|i
index|]
operator|>=
name|CUTOVER_DAY_EPOCH
condition|)
block|{
comment|// no need for conversion
continue|continue;
block|}
name|long
name|millis
init|=
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toMillis
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|String
name|originalFormatted
init|=
name|usingProlepticCalendar
condition|?
name|GREGORIAN_DATE_FORMATTER
operator|.
name|format
argument_list|(
name|millis
argument_list|)
else|:
name|PROLEPTIC_GREGORIAN_DATE_FORMATTER
operator|.
name|format
argument_list|(
name|millis
argument_list|)
decl_stmt|;
name|millis
operator|=
operator|(
name|usingProlepticCalendar
condition|?
name|PROLEPTIC_GREGORIAN_DATE_FORMATTER
operator|.
name|parse
argument_list|(
name|originalFormatted
argument_list|)
else|:
name|GREGORIAN_DATE_FORMATTER
operator|.
name|parse
argument_list|(
name|originalFormatted
argument_list|)
operator|)
operator|.
name|getTime
argument_list|()
expr_stmt|;
name|vector
index|[
name|i
index|]
operator|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toDays
argument_list|(
name|millis
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|formatDate
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|long
name|millis
init|=
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toMillis
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
decl_stmt|;
return|return
name|usingProlepticCalendar
condition|?
name|PROLEPTIC_GREGORIAN_DATE_FORMATTER
operator|.
name|format
argument_list|(
name|millis
argument_list|)
else|:
name|GREGORIAN_DATE_FORMATTER
operator|.
name|format
argument_list|(
name|millis
argument_list|)
return|;
block|}
specifier|public
name|DateColumnVector
name|setUsingProlepticCalendar
parameter_list|(
name|boolean
name|usingProlepticCalendar
parameter_list|)
block|{
name|this
operator|.
name|usingProlepticCalendar
operator|=
name|usingProlepticCalendar
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * Detect whether this data is using the proleptic calendar.    */
specifier|public
name|boolean
name|isUsingProlepticCalendar
parameter_list|()
block|{
return|return
name|usingProlepticCalendar
return|;
block|}
comment|/**    * Don't use this except for testing purposes.    *    * @param len the number of rows    */
specifier|public
name|DateColumnVector
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|super
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shallowCopyTo
parameter_list|(
name|ColumnVector
name|otherCv
parameter_list|)
block|{
name|DateColumnVector
name|other
init|=
operator|(
name|DateColumnVector
operator|)
name|otherCv
decl_stmt|;
name|super
operator|.
name|shallowCopyTo
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|other
operator|.
name|vector
operator|=
name|vector
expr_stmt|;
block|}
block|}
end_class

end_unit


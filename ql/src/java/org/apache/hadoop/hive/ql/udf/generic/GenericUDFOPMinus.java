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
name|vector
operator|.
name|VectorizedExpressions
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
name|vector
operator|.
name|expressions
operator|.
name|gen
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"-"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns the difference a-b"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColSubtractLongColumn
operator|.
name|class
block|,
name|LongColSubtractDoubleColumn
operator|.
name|class
block|,
name|DoubleColSubtractLongColumn
operator|.
name|class
block|,
name|DoubleColSubtractDoubleColumn
operator|.
name|class
block|,
name|LongColSubtractLongScalar
operator|.
name|class
block|,
name|LongColSubtractDoubleScalar
operator|.
name|class
block|,
name|DoubleColSubtractLongScalar
operator|.
name|class
block|,
name|DoubleColSubtractDoubleScalar
operator|.
name|class
block|,
name|LongScalarSubtractLongColumn
operator|.
name|class
block|,
name|LongScalarSubtractDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarSubtractLongColumn
operator|.
name|class
block|,
name|DoubleScalarSubtractDoubleColumn
operator|.
name|class
block|,
name|DecimalColSubtractDecimalColumn
operator|.
name|class
block|,
name|DecimalColSubtractDecimalScalar
operator|.
name|class
block|,
name|DecimalScalarSubtractDecimalColumn
operator|.
name|class
block|,
name|IntervalYearMonthColSubtractIntervalYearMonthColumn
operator|.
name|class
block|,
name|IntervalYearMonthColSubtractIntervalYearMonthScalar
operator|.
name|class
block|,
name|IntervalYearMonthScalarSubtractIntervalYearMonthColumn
operator|.
name|class
block|,
name|IntervalDayTimeColSubtractIntervalDayTimeColumn
operator|.
name|class
block|,
name|IntervalDayTimeColSubtractIntervalDayTimeScalar
operator|.
name|class
block|,
name|IntervalDayTimeScalarSubtractIntervalDayTimeColumn
operator|.
name|class
block|,
name|TimestampColSubtractIntervalDayTimeColumn
operator|.
name|class
block|,
name|TimestampColSubtractIntervalDayTimeScalar
operator|.
name|class
block|,
name|TimestampScalarSubtractIntervalDayTimeColumn
operator|.
name|class
block|,
name|TimestampColSubtractTimestampColumn
operator|.
name|class
block|,
name|TimestampColSubtractTimestampScalar
operator|.
name|class
block|,
name|TimestampScalarSubtractTimestampColumn
operator|.
name|class
block|,
name|DateColSubtractDateColumn
operator|.
name|class
block|,
name|DateColSubtractDateScalar
operator|.
name|class
block|,
name|DateScalarSubtractDateColumn
operator|.
name|class
block|,
name|DateColSubtractTimestampColumn
operator|.
name|class
block|,
name|DateColSubtractTimestampScalar
operator|.
name|class
block|,
name|DateScalarSubtractTimestampColumn
operator|.
name|class
block|,
name|TimestampColSubtractDateColumn
operator|.
name|class
block|,
name|TimestampColSubtractDateScalar
operator|.
name|class
block|,
name|TimestampScalarSubtractDateColumn
operator|.
name|class
block|,
name|DateColSubtractIntervalDayTimeColumn
operator|.
name|class
block|,
name|DateColSubtractIntervalDayTimeScalar
operator|.
name|class
block|,
name|DateScalarSubtractIntervalDayTimeColumn
operator|.
name|class
block|,
name|DateColSubtractIntervalYearMonthColumn
operator|.
name|class
block|,
name|DateScalarSubtractIntervalYearMonthColumn
operator|.
name|class
block|,
name|DateColSubtractIntervalYearMonthScalar
operator|.
name|class
block|,
name|TimestampColSubtractIntervalYearMonthColumn
operator|.
name|class
block|,
name|TimestampScalarSubtractIntervalYearMonthColumn
operator|.
name|class
block|,
name|TimestampColSubtractIntervalYearMonthScalar
operator|.
name|class
block|,
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFOPMinus
extends|extends
name|GenericUDFBaseArithmetic
block|{
specifier|public
name|GenericUDFOPMinus
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"-"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|GenericUDFBaseNumeric
name|instantiateNumericUDF
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPNumericMinus
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|GenericUDF
name|instantiateDTIUDF
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPDTIMinus
argument_list|()
return|;
block|}
block|}
end_class

end_unit


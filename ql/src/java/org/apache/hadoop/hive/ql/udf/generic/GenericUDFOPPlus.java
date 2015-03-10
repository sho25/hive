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

begin_comment
comment|/**  * The reason that we list evaluate methods with all numeric types is for both  * better performance and type checking (so we know int + int is still an int  * instead of a double); otherwise a single method that takes (Number a, Number  * b) and use a.doubleValue() == b.doubleValue() is enough.  *  * The case of int + double will be handled by implicit type casting using  * UDFRegistry.implicitConvertable method.  */
end_comment

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
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|LongColAddLongColumn
operator|.
name|class
block|,
name|LongColAddDoubleColumn
operator|.
name|class
block|,
name|DoubleColAddLongColumn
operator|.
name|class
block|,
name|DoubleColAddDoubleColumn
operator|.
name|class
block|,
name|LongColAddLongScalar
operator|.
name|class
block|,
name|LongColAddDoubleScalar
operator|.
name|class
block|,
name|DoubleColAddLongScalar
operator|.
name|class
block|,
name|DoubleColAddDoubleScalar
operator|.
name|class
block|,
name|LongScalarAddLongColumn
operator|.
name|class
block|,
name|LongScalarAddDoubleColumn
operator|.
name|class
block|,
name|DoubleScalarAddLongColumn
operator|.
name|class
block|,
name|DoubleScalarAddDoubleColumn
operator|.
name|class
block|,
name|DecimalScalarAddDecimalColumn
operator|.
name|class
block|,
name|DecimalColAddDecimalColumn
operator|.
name|class
block|,
name|DecimalColAddDecimalScalar
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFOPPlus
extends|extends
name|GenericUDFBaseArithmetic
block|{
specifier|public
name|GenericUDFOPPlus
parameter_list|()
block|{
name|super
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
specifier|protected
name|GenericUDFBaseNumeric
name|instantiateNumericUDF
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPNumericPlus
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
comment|// TODO: implement date-time/interval version of UDF
return|return
operator|new
name|GenericUDFOPNumericPlus
argument_list|()
return|;
block|}
block|}
end_class

end_unit


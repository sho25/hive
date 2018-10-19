begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|spitter
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|MutablePair
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
name|DecimalTypeInfo
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
name|math
operator|.
name|MathContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|RoundingMode
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

begin_class
specifier|public
class|class
name|DecimalIntervalSplitter
implements|implements
name|IntervalSplitter
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MutablePair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getIntervals
parameter_list|(
name|String
name|lowerBound
parameter_list|,
name|String
name|upperBound
parameter_list|,
name|int
name|numPartitions
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|List
argument_list|<
name|MutablePair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|intervals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|DecimalTypeInfo
name|decimalTypeInfo
init|=
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
decl_stmt|;
name|int
name|scale
init|=
name|decimalTypeInfo
operator|.
name|getScale
argument_list|()
decl_stmt|;
name|BigDecimal
name|decimalLower
init|=
operator|new
name|BigDecimal
argument_list|(
name|lowerBound
argument_list|)
decl_stmt|;
name|BigDecimal
name|decimalUpper
init|=
operator|new
name|BigDecimal
argument_list|(
name|upperBound
argument_list|)
decl_stmt|;
name|BigDecimal
name|decimalInterval
init|=
operator|(
name|decimalUpper
operator|.
name|subtract
argument_list|(
name|decimalLower
argument_list|)
operator|)
operator|.
name|divide
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|numPartitions
argument_list|)
argument_list|,
name|MathContext
operator|.
name|DECIMAL64
argument_list|)
decl_stmt|;
name|BigDecimal
name|splitDecimalLower
decl_stmt|,
name|splitDecimalUpper
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
name|numPartitions
condition|;
name|i
operator|++
control|)
block|{
name|splitDecimalLower
operator|=
name|decimalLower
operator|.
name|add
argument_list|(
name|decimalInterval
operator|.
name|multiply
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setScale
argument_list|(
name|scale
argument_list|,
name|RoundingMode
operator|.
name|HALF_EVEN
argument_list|)
expr_stmt|;
name|splitDecimalUpper
operator|=
name|decimalLower
operator|.
name|add
argument_list|(
name|decimalInterval
operator|.
name|multiply
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setScale
argument_list|(
name|scale
argument_list|,
name|RoundingMode
operator|.
name|HALF_EVEN
argument_list|)
expr_stmt|;
if|if
condition|(
name|splitDecimalLower
operator|.
name|compareTo
argument_list|(
name|splitDecimalUpper
argument_list|)
operator|<
literal|0
condition|)
block|{
name|intervals
operator|.
name|add
argument_list|(
operator|new
name|MutablePair
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|splitDecimalLower
operator|.
name|toPlainString
argument_list|()
argument_list|,
name|splitDecimalUpper
operator|.
name|toPlainString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|intervals
return|;
block|}
block|}
end_class

end_unit


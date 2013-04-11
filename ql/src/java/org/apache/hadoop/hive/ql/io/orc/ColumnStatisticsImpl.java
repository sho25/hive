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
name|io
operator|.
name|orc
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
name|PrimitiveObjectInspector
import|;
end_import

begin_class
class|class
name|ColumnStatisticsImpl
implements|implements
name|ColumnStatistics
block|{
specifier|private
specifier|static
specifier|final
class|class
name|BooleanStatisticsImpl
extends|extends
name|ColumnStatisticsImpl
implements|implements
name|BooleanColumnStatistics
block|{
specifier|private
name|long
name|trueCount
init|=
literal|0
decl_stmt|;
name|BooleanStatisticsImpl
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
name|super
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|BucketStatistics
name|bkt
init|=
name|stats
operator|.
name|getBucketStatistics
argument_list|()
decl_stmt|;
name|trueCount
operator|=
name|bkt
operator|.
name|getCount
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|BooleanStatisticsImpl
parameter_list|()
block|{     }
annotation|@
name|Override
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|trueCount
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|updateBoolean
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
condition|)
block|{
name|trueCount
operator|+=
literal|1
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|void
name|merge
parameter_list|(
name|ColumnStatisticsImpl
name|other
parameter_list|)
block|{
name|super
operator|.
name|merge
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|BooleanStatisticsImpl
name|bkt
init|=
operator|(
name|BooleanStatisticsImpl
operator|)
name|other
decl_stmt|;
name|trueCount
operator|+=
name|bkt
operator|.
name|trueCount
expr_stmt|;
block|}
annotation|@
name|Override
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|serialize
parameter_list|()
block|{
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|builder
init|=
name|super
operator|.
name|serialize
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|BucketStatistics
operator|.
name|Builder
name|bucket
init|=
name|OrcProto
operator|.
name|BucketStatistics
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|bucket
operator|.
name|addCount
argument_list|(
name|trueCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setBucketStatistics
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getFalseCount
parameter_list|()
block|{
return|return
name|getNumberOfValues
argument_list|()
operator|-
name|trueCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTrueCount
parameter_list|()
block|{
return|return
name|trueCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|" true: "
operator|+
name|trueCount
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|IntegerStatisticsImpl
extends|extends
name|ColumnStatisticsImpl
implements|implements
name|IntegerColumnStatistics
block|{
specifier|private
name|long
name|minimum
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|long
name|maximum
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
specifier|private
name|long
name|sum
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|hasMinimum
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|overflow
init|=
literal|false
decl_stmt|;
name|IntegerStatisticsImpl
parameter_list|()
block|{     }
name|IntegerStatisticsImpl
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
name|super
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|IntegerStatistics
name|intStat
init|=
name|stats
operator|.
name|getIntStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|intStat
operator|.
name|hasMinimum
argument_list|()
condition|)
block|{
name|hasMinimum
operator|=
literal|true
expr_stmt|;
name|minimum
operator|=
name|intStat
operator|.
name|getMinimum
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|intStat
operator|.
name|hasMaximum
argument_list|()
condition|)
block|{
name|maximum
operator|=
name|intStat
operator|.
name|getMaximum
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|intStat
operator|.
name|hasSum
argument_list|()
condition|)
block|{
name|sum
operator|=
name|intStat
operator|.
name|getSum
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|overflow
operator|=
literal|true
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|hasMinimum
operator|=
literal|false
expr_stmt|;
name|minimum
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|maximum
operator|=
name|Long
operator|.
name|MIN_VALUE
expr_stmt|;
name|sum
operator|=
literal|0
expr_stmt|;
name|overflow
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|updateInteger
parameter_list|(
name|long
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|hasMinimum
condition|)
block|{
name|hasMinimum
operator|=
literal|true
expr_stmt|;
name|minimum
operator|=
name|value
expr_stmt|;
name|maximum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|<
name|minimum
condition|)
block|{
name|minimum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|>
name|maximum
condition|)
block|{
name|maximum
operator|=
name|value
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|overflow
condition|)
block|{
name|boolean
name|wasPositive
init|=
name|sum
operator|>=
literal|0
decl_stmt|;
name|sum
operator|+=
name|value
expr_stmt|;
if|if
condition|(
operator|(
name|value
operator|>=
literal|0
operator|)
operator|==
name|wasPositive
condition|)
block|{
name|overflow
operator|=
operator|(
name|sum
operator|>=
literal|0
operator|)
operator|!=
name|wasPositive
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
name|void
name|merge
parameter_list|(
name|ColumnStatisticsImpl
name|other
parameter_list|)
block|{
name|IntegerStatisticsImpl
name|otherInt
init|=
operator|(
name|IntegerStatisticsImpl
operator|)
name|other
decl_stmt|;
if|if
condition|(
operator|!
name|hasMinimum
condition|)
block|{
name|hasMinimum
operator|=
name|otherInt
operator|.
name|hasMinimum
expr_stmt|;
name|minimum
operator|=
name|otherInt
operator|.
name|minimum
expr_stmt|;
name|maximum
operator|=
name|otherInt
operator|.
name|maximum
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|otherInt
operator|.
name|hasMinimum
condition|)
block|{
if|if
condition|(
name|otherInt
operator|.
name|minimum
operator|<
name|minimum
condition|)
block|{
name|minimum
operator|=
name|otherInt
operator|.
name|minimum
expr_stmt|;
block|}
if|if
condition|(
name|otherInt
operator|.
name|maximum
operator|>
name|maximum
condition|)
block|{
name|maximum
operator|=
name|otherInt
operator|.
name|maximum
expr_stmt|;
block|}
block|}
name|super
operator|.
name|merge
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|overflow
operator||=
name|otherInt
operator|.
name|overflow
expr_stmt|;
if|if
condition|(
operator|!
name|overflow
condition|)
block|{
name|boolean
name|wasPositive
init|=
name|sum
operator|>=
literal|0
decl_stmt|;
name|sum
operator|+=
name|otherInt
operator|.
name|sum
expr_stmt|;
if|if
condition|(
operator|(
name|otherInt
operator|.
name|sum
operator|>=
literal|0
operator|)
operator|==
name|wasPositive
condition|)
block|{
name|overflow
operator|=
operator|(
name|sum
operator|>=
literal|0
operator|)
operator|!=
name|wasPositive
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|serialize
parameter_list|()
block|{
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|builder
init|=
name|super
operator|.
name|serialize
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|IntegerStatistics
operator|.
name|Builder
name|intb
init|=
name|OrcProto
operator|.
name|IntegerStatistics
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasMinimum
condition|)
block|{
name|intb
operator|.
name|setMinimum
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|intb
operator|.
name|setMaximum
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|overflow
condition|)
block|{
name|intb
operator|.
name|setSum
argument_list|(
name|sum
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|setIntStatistics
argument_list|(
name|intb
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMinimum
parameter_list|()
block|{
return|return
name|minimum
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getMaximum
parameter_list|()
block|{
return|return
name|maximum
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSumDefined
parameter_list|()
block|{
return|return
operator|!
name|overflow
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSum
parameter_list|()
block|{
return|return
name|sum
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|(
name|super
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasMinimum
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" min: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" max: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|overflow
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" sum: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|sum
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|DoubleStatisticsImpl
extends|extends
name|ColumnStatisticsImpl
implements|implements
name|DoubleColumnStatistics
block|{
specifier|private
name|boolean
name|hasMinimum
init|=
literal|false
decl_stmt|;
specifier|private
name|double
name|minimum
init|=
name|Double
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|double
name|maximum
init|=
name|Double
operator|.
name|MIN_VALUE
decl_stmt|;
specifier|private
name|double
name|sum
init|=
literal|0
decl_stmt|;
name|DoubleStatisticsImpl
parameter_list|()
block|{     }
name|DoubleStatisticsImpl
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
name|super
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|DoubleStatistics
name|dbl
init|=
name|stats
operator|.
name|getDoubleStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|dbl
operator|.
name|hasMinimum
argument_list|()
condition|)
block|{
name|hasMinimum
operator|=
literal|true
expr_stmt|;
name|minimum
operator|=
name|dbl
operator|.
name|getMinimum
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|dbl
operator|.
name|hasMaximum
argument_list|()
condition|)
block|{
name|maximum
operator|=
name|dbl
operator|.
name|getMaximum
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|dbl
operator|.
name|hasSum
argument_list|()
condition|)
block|{
name|sum
operator|=
name|dbl
operator|.
name|getSum
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|hasMinimum
operator|=
literal|false
expr_stmt|;
name|minimum
operator|=
name|Double
operator|.
name|MAX_VALUE
expr_stmt|;
name|maximum
operator|=
name|Double
operator|.
name|MIN_VALUE
expr_stmt|;
name|sum
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|updateDouble
parameter_list|(
name|double
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|hasMinimum
condition|)
block|{
name|hasMinimum
operator|=
literal|true
expr_stmt|;
name|minimum
operator|=
name|value
expr_stmt|;
name|maximum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|<
name|minimum
condition|)
block|{
name|minimum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|>
name|maximum
condition|)
block|{
name|maximum
operator|=
name|value
expr_stmt|;
block|}
name|sum
operator|+=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|merge
parameter_list|(
name|ColumnStatisticsImpl
name|other
parameter_list|)
block|{
name|super
operator|.
name|merge
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|DoubleStatisticsImpl
name|dbl
init|=
operator|(
name|DoubleStatisticsImpl
operator|)
name|other
decl_stmt|;
if|if
condition|(
operator|!
name|hasMinimum
condition|)
block|{
name|hasMinimum
operator|=
name|dbl
operator|.
name|hasMinimum
expr_stmt|;
name|minimum
operator|=
name|dbl
operator|.
name|minimum
expr_stmt|;
name|maximum
operator|=
name|dbl
operator|.
name|maximum
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|dbl
operator|.
name|hasMinimum
condition|)
block|{
if|if
condition|(
name|dbl
operator|.
name|minimum
operator|<
name|minimum
condition|)
block|{
name|minimum
operator|=
name|dbl
operator|.
name|minimum
expr_stmt|;
block|}
if|if
condition|(
name|dbl
operator|.
name|maximum
operator|>
name|maximum
condition|)
block|{
name|maximum
operator|=
name|dbl
operator|.
name|maximum
expr_stmt|;
block|}
block|}
name|sum
operator|+=
name|dbl
operator|.
name|sum
expr_stmt|;
block|}
annotation|@
name|Override
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|serialize
parameter_list|()
block|{
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|builder
init|=
name|super
operator|.
name|serialize
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|DoubleStatistics
operator|.
name|Builder
name|dbl
init|=
name|OrcProto
operator|.
name|DoubleStatistics
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|hasMinimum
condition|)
block|{
name|dbl
operator|.
name|setMinimum
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|dbl
operator|.
name|setMaximum
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
block|}
name|dbl
operator|.
name|setSum
argument_list|(
name|sum
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setDoubleStatistics
argument_list|(
name|dbl
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getMinimum
parameter_list|()
block|{
return|return
name|minimum
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getMaximum
parameter_list|()
block|{
return|return
name|maximum
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getSum
parameter_list|()
block|{
return|return
name|sum
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|(
name|super
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasMinimum
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" min: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" max: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
literal|" sum: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|sum
argument_list|)
expr_stmt|;
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|StringStatisticsImpl
extends|extends
name|ColumnStatisticsImpl
implements|implements
name|StringColumnStatistics
block|{
specifier|private
name|String
name|minimum
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|maximum
init|=
literal|null
decl_stmt|;
name|StringStatisticsImpl
parameter_list|()
block|{     }
name|StringStatisticsImpl
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
name|super
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|StringStatistics
name|str
init|=
name|stats
operator|.
name|getStringStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|str
operator|.
name|hasMaximum
argument_list|()
condition|)
block|{
name|maximum
operator|=
name|str
operator|.
name|getMaximum
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|str
operator|.
name|hasMinimum
argument_list|()
condition|)
block|{
name|minimum
operator|=
name|str
operator|.
name|getMinimum
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|minimum
operator|=
literal|null
expr_stmt|;
name|maximum
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|updateString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|minimum
operator|==
literal|null
condition|)
block|{
name|minimum
operator|=
name|value
expr_stmt|;
name|maximum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minimum
operator|.
name|compareTo
argument_list|(
name|value
argument_list|)
operator|>
literal|0
condition|)
block|{
name|minimum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|maximum
operator|.
name|compareTo
argument_list|(
name|value
argument_list|)
operator|<
literal|0
condition|)
block|{
name|maximum
operator|=
name|value
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|void
name|merge
parameter_list|(
name|ColumnStatisticsImpl
name|other
parameter_list|)
block|{
name|super
operator|.
name|merge
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|StringStatisticsImpl
name|str
init|=
operator|(
name|StringStatisticsImpl
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|minimum
operator|==
literal|null
condition|)
block|{
name|minimum
operator|=
name|str
operator|.
name|minimum
expr_stmt|;
name|maximum
operator|=
name|str
operator|.
name|maximum
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|str
operator|.
name|minimum
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|minimum
operator|.
name|compareTo
argument_list|(
name|str
operator|.
name|minimum
argument_list|)
operator|>
literal|0
condition|)
block|{
name|minimum
operator|=
name|str
operator|.
name|minimum
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|maximum
operator|.
name|compareTo
argument_list|(
name|str
operator|.
name|maximum
argument_list|)
operator|<
literal|0
condition|)
block|{
name|maximum
operator|=
name|str
operator|.
name|maximum
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|serialize
parameter_list|()
block|{
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|result
init|=
name|super
operator|.
name|serialize
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|StringStatistics
operator|.
name|Builder
name|str
init|=
name|OrcProto
operator|.
name|StringStatistics
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|getNumberOfValues
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|str
operator|.
name|setMinimum
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|str
operator|.
name|setMaximum
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|setStringStatistics
argument_list|(
name|str
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMinimum
parameter_list|()
block|{
return|return
name|minimum
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMaximum
parameter_list|()
block|{
return|return
name|maximum
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|(
name|super
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|getNumberOfValues
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" min: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" max: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|DecimalStatisticsImpl
extends|extends
name|ColumnStatisticsImpl
implements|implements
name|DecimalColumnStatistics
block|{
specifier|private
name|HiveDecimal
name|minimum
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveDecimal
name|maximum
init|=
literal|null
decl_stmt|;
specifier|private
name|HiveDecimal
name|sum
init|=
name|HiveDecimal
operator|.
name|ZERO
decl_stmt|;
name|DecimalStatisticsImpl
parameter_list|()
block|{     }
name|DecimalStatisticsImpl
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
name|super
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|OrcProto
operator|.
name|DecimalStatistics
name|dec
init|=
name|stats
operator|.
name|getDecimalStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|dec
operator|.
name|hasMaximum
argument_list|()
condition|)
block|{
name|maximum
operator|=
operator|new
name|HiveDecimal
argument_list|(
name|dec
operator|.
name|getMaximum
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dec
operator|.
name|hasMinimum
argument_list|()
condition|)
block|{
name|minimum
operator|=
operator|new
name|HiveDecimal
argument_list|(
name|dec
operator|.
name|getMinimum
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dec
operator|.
name|hasSum
argument_list|()
condition|)
block|{
name|sum
operator|=
operator|new
name|HiveDecimal
argument_list|(
name|dec
operator|.
name|getSum
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sum
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
name|void
name|reset
parameter_list|()
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|minimum
operator|=
literal|null
expr_stmt|;
name|maximum
operator|=
literal|null
expr_stmt|;
name|sum
operator|=
name|HiveDecimal
operator|.
name|ZERO
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|updateDecimal
parameter_list|(
name|HiveDecimal
name|value
parameter_list|)
block|{
if|if
condition|(
name|minimum
operator|==
literal|null
condition|)
block|{
name|minimum
operator|=
name|value
expr_stmt|;
name|maximum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|minimum
operator|.
name|compareTo
argument_list|(
name|value
argument_list|)
operator|>
literal|0
condition|)
block|{
name|minimum
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|maximum
operator|.
name|compareTo
argument_list|(
name|value
argument_list|)
operator|<
literal|0
condition|)
block|{
name|maximum
operator|=
name|value
expr_stmt|;
block|}
if|if
condition|(
name|sum
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|sum
operator|=
name|sum
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
name|sum
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
name|void
name|merge
parameter_list|(
name|ColumnStatisticsImpl
name|other
parameter_list|)
block|{
name|super
operator|.
name|merge
argument_list|(
name|other
argument_list|)
expr_stmt|;
name|DecimalStatisticsImpl
name|dec
init|=
operator|(
name|DecimalStatisticsImpl
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|minimum
operator|==
literal|null
condition|)
block|{
name|minimum
operator|=
name|dec
operator|.
name|minimum
expr_stmt|;
name|maximum
operator|=
name|dec
operator|.
name|maximum
expr_stmt|;
name|sum
operator|=
name|dec
operator|.
name|sum
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|dec
operator|.
name|minimum
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|minimum
operator|.
name|compareTo
argument_list|(
name|dec
operator|.
name|minimum
argument_list|)
operator|>
literal|0
condition|)
block|{
name|minimum
operator|=
name|dec
operator|.
name|minimum
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|maximum
operator|.
name|compareTo
argument_list|(
name|dec
operator|.
name|maximum
argument_list|)
operator|<
literal|0
condition|)
block|{
name|maximum
operator|=
name|dec
operator|.
name|maximum
expr_stmt|;
block|}
if|if
condition|(
name|sum
operator|==
literal|null
operator|||
name|dec
operator|.
name|sum
operator|==
literal|null
condition|)
block|{
name|sum
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|sum
operator|=
name|sum
operator|.
name|add
argument_list|(
name|dec
operator|.
name|sum
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|serialize
parameter_list|()
block|{
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|result
init|=
name|super
operator|.
name|serialize
argument_list|()
decl_stmt|;
name|OrcProto
operator|.
name|DecimalStatistics
operator|.
name|Builder
name|dec
init|=
name|OrcProto
operator|.
name|DecimalStatistics
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|getNumberOfValues
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|dec
operator|.
name|setMinimum
argument_list|(
name|minimum
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|dec
operator|.
name|setMaximum
argument_list|(
name|maximum
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sum
operator|!=
literal|null
condition|)
block|{
name|dec
operator|.
name|setSum
argument_list|(
name|sum
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|setDecimalStatistics
argument_list|(
name|dec
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimal
name|getMinimum
parameter_list|()
block|{
return|return
name|minimum
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimal
name|getMaximum
parameter_list|()
block|{
return|return
name|maximum
return|;
block|}
annotation|@
name|Override
specifier|public
name|HiveDecimal
name|getSum
parameter_list|()
block|{
return|return
name|sum
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|(
name|super
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|getNumberOfValues
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" min: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|minimum
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|" max: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|maximum
argument_list|)
expr_stmt|;
if|if
condition|(
name|sum
operator|!=
literal|null
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|" sum: "
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|sum
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|private
name|long
name|count
init|=
literal|0
decl_stmt|;
name|ColumnStatisticsImpl
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|.
name|hasNumberOfValues
argument_list|()
condition|)
block|{
name|count
operator|=
name|stats
operator|.
name|getNumberOfValues
argument_list|()
expr_stmt|;
block|}
block|}
name|ColumnStatisticsImpl
parameter_list|()
block|{   }
name|void
name|increment
parameter_list|()
block|{
name|count
operator|+=
literal|1
expr_stmt|;
block|}
name|void
name|updateBoolean
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't update boolean"
argument_list|)
throw|;
block|}
name|void
name|updateInteger
parameter_list|(
name|long
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't update integer"
argument_list|)
throw|;
block|}
name|void
name|updateDouble
parameter_list|(
name|double
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't update double"
argument_list|)
throw|;
block|}
name|void
name|updateString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't update string"
argument_list|)
throw|;
block|}
name|void
name|updateDecimal
parameter_list|(
name|HiveDecimal
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't update decimal"
argument_list|)
throw|;
block|}
name|void
name|merge
parameter_list|(
name|ColumnStatisticsImpl
name|stats
parameter_list|)
block|{
name|count
operator|+=
name|stats
operator|.
name|count
expr_stmt|;
block|}
name|void
name|reset
parameter_list|()
block|{
name|count
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumberOfValues
parameter_list|()
block|{
return|return
name|count
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"count: "
operator|+
name|count
return|;
block|}
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|serialize
parameter_list|()
block|{
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|Builder
name|builder
init|=
name|OrcProto
operator|.
name|ColumnStatistics
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setNumberOfValues
argument_list|(
name|count
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
specifier|static
name|ColumnStatisticsImpl
name|create
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|)
block|{
switch|switch
condition|(
name|inspector
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|inspector
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|BooleanStatisticsImpl
argument_list|()
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
return|return
operator|new
name|IntegerStatisticsImpl
argument_list|()
return|;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
return|return
operator|new
name|DoubleStatisticsImpl
argument_list|()
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|StringStatisticsImpl
argument_list|()
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|DecimalStatisticsImpl
argument_list|()
return|;
default|default:
return|return
operator|new
name|ColumnStatisticsImpl
argument_list|()
return|;
block|}
default|default:
return|return
operator|new
name|ColumnStatisticsImpl
argument_list|()
return|;
block|}
block|}
specifier|static
name|ColumnStatisticsImpl
name|deserialize
parameter_list|(
name|OrcProto
operator|.
name|ColumnStatistics
name|stats
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|.
name|hasBucketStatistics
argument_list|()
condition|)
block|{
return|return
operator|new
name|BooleanStatisticsImpl
argument_list|(
name|stats
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|stats
operator|.
name|hasIntStatistics
argument_list|()
condition|)
block|{
return|return
operator|new
name|IntegerStatisticsImpl
argument_list|(
name|stats
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|stats
operator|.
name|hasDoubleStatistics
argument_list|()
condition|)
block|{
return|return
operator|new
name|DoubleStatisticsImpl
argument_list|(
name|stats
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|stats
operator|.
name|hasStringStatistics
argument_list|()
condition|)
block|{
return|return
operator|new
name|StringStatisticsImpl
argument_list|(
name|stats
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|stats
operator|.
name|hasDecimalStatistics
argument_list|()
condition|)
block|{
return|return
operator|new
name|DecimalStatisticsImpl
argument_list|(
name|stats
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|ColumnStatisticsImpl
argument_list|(
name|stats
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|SqlFunction
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
name|SqlFunctionCategory
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
name|SqlKind
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
name|SqlOperatorBinding
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
name|fun
operator|.
name|SqlMonotonicUnaryFunction
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
name|OperandTypes
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
name|ReturnTypes
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
name|validate
operator|.
name|SqlMonotonicity
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|HiveFloorDate
extends|extends
name|SqlMonotonicUnaryFunction
block|{
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|YEAR
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_YEAR"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|QUARTER
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_QUARTER"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|MONTH
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_MONTH"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|WEEK
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_WEEK"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|DAY
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_DAY"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|HOUR
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_HOUR"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|MINUTE
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_MINUTE"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|SqlFunction
name|SECOND
init|=
operator|new
name|HiveFloorDate
argument_list|(
literal|"FLOOR_SECOND"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|SqlFunction
argument_list|>
name|ALL_FUNCTIONS
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|YEAR
argument_list|,
name|QUARTER
argument_list|,
name|MONTH
argument_list|,
name|WEEK
argument_list|,
name|DAY
argument_list|,
name|HOUR
argument_list|,
name|MINUTE
argument_list|,
name|SECOND
argument_list|)
decl_stmt|;
specifier|private
name|HiveFloorDate
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|SqlKind
operator|.
name|FLOOR
argument_list|,
name|ReturnTypes
operator|.
name|ARG0_OR_EXACT_NO_SCALE
argument_list|,
literal|null
argument_list|,
name|OperandTypes
operator|.
name|sequence
argument_list|(
literal|"'"
operator|+
name|SqlKind
operator|.
name|FLOOR
operator|+
literal|"(<DATE> TO<TIME_UNIT>)'\n"
operator|+
literal|"'"
operator|+
name|SqlKind
operator|.
name|FLOOR
operator|+
literal|"(<TIME> TO<TIME_UNIT>)'\n"
operator|+
literal|"'"
operator|+
name|SqlKind
operator|.
name|FLOOR
operator|+
literal|"(<TIMESTAMP> TO<TIME_UNIT>)'"
argument_list|,
name|OperandTypes
operator|.
name|DATETIME
argument_list|,
name|OperandTypes
operator|.
name|ANY
argument_list|)
argument_list|,
name|SqlFunctionCategory
operator|.
name|NUMERIC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SqlMonotonicity
name|getMonotonicity
parameter_list|(
name|SqlOperatorBinding
name|call
parameter_list|)
block|{
comment|// Monotonic iff its first argument is, but not strict.
return|return
name|call
operator|.
name|getOperandMonotonicity
argument_list|(
literal|0
argument_list|)
operator|.
name|unstrict
argument_list|()
return|;
block|}
block|}
end_class

end_unit


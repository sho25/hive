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
name|parse
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
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
name|base
operator|.
name|Preconditions
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
name|ImmutableList
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
name|Iterables
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
name|Maps
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
name|adapter
operator|.
name|druid
operator|.
name|DirectOperatorConversion
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
name|adapter
operator|.
name|druid
operator|.
name|DruidExpressions
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
name|adapter
operator|.
name|druid
operator|.
name|DruidQuery
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
name|adapter
operator|.
name|druid
operator|.
name|ExtractOperatorConversion
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
name|adapter
operator|.
name|druid
operator|.
name|FloorOperatorConversion
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
name|adapter
operator|.
name|druid
operator|.
name|UnarySuffixOperatorConversion
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
name|config
operator|.
name|CalciteConnectionConfig
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
name|rel
operator|.
name|type
operator|.
name|RelDataType
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
name|rex
operator|.
name|RexCall
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
name|rex
operator|.
name|RexLiteral
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
name|rex
operator|.
name|RexNode
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
name|SqlOperator
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
name|SqlStdOperatorTable
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
name|SqlTypeUtil
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveConcat
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveDateAddSqlOperator
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveDateSubSqlOperator
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveExtractDate
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveFloorDate
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveFromUnixTimeSqlOperator
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveToDateSqlOperator
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTruncSqlOperator
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveUnixTimestampSqlOperator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Period
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

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
name|TimeZone
import|;
end_import

begin_comment
comment|/**  * Contains custom Druid SQL operator converter classes, contains either:  * Hive specific OperatorConversion logic that can not be part of Calcite  * Some temporary OperatorConversion that is not release by Calcite yet  */
end_comment

begin_class
specifier|public
class|class
name|DruidSqlOperatorConverter
block|{
specifier|private
specifier|static
specifier|final
name|String
name|YYYY_MM_DD
init|=
literal|"yyyy-MM-dd"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_TS_FORMAT
init|=
literal|"yyyy-MM-dd HH:mm:ss"
decl_stmt|;
specifier|private
name|DruidSqlOperatorConverter
parameter_list|()
block|{   }
specifier|private
specifier|static
name|Map
name|druidOperatorMap
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|SqlOperator
argument_list|,
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
argument_list|>
name|getDefaultMap
parameter_list|()
block|{
if|if
condition|(
name|druidOperatorMap
operator|==
literal|null
condition|)
block|{
name|druidOperatorMap
operator|=
operator|new
name|HashMap
argument_list|<
name|SqlOperator
argument_list|,
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
argument_list|>
argument_list|()
expr_stmt|;
name|DruidQuery
operator|.
name|DEFAULT_OPERATORS_LIST
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|op
lambda|->
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|op
operator|.
name|calciteOperator
argument_list|()
argument_list|,
name|op
argument_list|)
argument_list|)
expr_stmt|;
comment|//Override Hive specific operators
name|druidOperatorMap
operator|.
name|putAll
argument_list|(
name|Maps
operator|.
name|asMap
argument_list|(
name|HiveFloorDate
operator|.
name|ALL_FUNCTIONS
argument_list|,
operator|(
name|Function
argument_list|<
name|SqlFunction
argument_list|,
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
argument_list|>
operator|)
name|input
lambda|->
operator|new
name|FloorOperatorConversion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|putAll
argument_list|(
name|Maps
operator|.
name|asMap
argument_list|(
name|HiveExtractDate
operator|.
name|ALL_FUNCTIONS
argument_list|,
operator|(
name|Function
argument_list|<
name|SqlFunction
argument_list|,
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
argument_list|>
operator|)
name|input
lambda|->
operator|new
name|ExtractOperatorConversion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveConcat
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DirectOperatorConversion
argument_list|(
name|HiveConcat
operator|.
name|INSTANCE
argument_list|,
literal|"concat"
argument_list|)
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|SqlStdOperatorTable
operator|.
name|SUBSTRING
argument_list|,
operator|new
name|DruidSqlOperatorConverter
operator|.
name|DruidSubstringOperatorConversion
argument_list|()
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|SqlStdOperatorTable
operator|.
name|IS_NULL
argument_list|,
operator|new
name|UnaryFunctionOperatorConversion
argument_list|(
name|SqlStdOperatorTable
operator|.
name|IS_NULL
argument_list|,
literal|"isnull"
argument_list|)
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|SqlStdOperatorTable
operator|.
name|IS_NOT_NULL
argument_list|,
operator|new
name|UnaryFunctionOperatorConversion
argument_list|(
name|SqlStdOperatorTable
operator|.
name|IS_NOT_NULL
argument_list|,
literal|"notnull"
argument_list|)
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveTruncSqlOperator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DruidDateTruncOperatorConversion
argument_list|()
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveToDateSqlOperator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DruidToDateOperatorConversion
argument_list|()
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveFromUnixTimeSqlOperator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DruidFormUnixTimeOperatorConversion
argument_list|()
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveUnixTimestampSqlOperator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DruidUnixTimestampOperatorConversion
argument_list|()
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveDateAddSqlOperator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DruidDateArithmeticOperatorConversion
argument_list|(
literal|1
argument_list|,
name|HiveDateAddSqlOperator
operator|.
name|INSTANCE
argument_list|)
argument_list|)
expr_stmt|;
name|druidOperatorMap
operator|.
name|put
argument_list|(
name|HiveDateSubSqlOperator
operator|.
name|INSTANCE
argument_list|,
operator|new
name|DruidDateArithmeticOperatorConversion
argument_list|(
operator|-
literal|1
argument_list|,
name|HiveDateSubSqlOperator
operator|.
name|INSTANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|druidOperatorMap
return|;
block|}
comment|/**    * Druid operator converter from Hive Substring to Druid SubString.    * This is a temporary fix that can be removed once we move to a Calcite version including the following.    * https://issues.apache.org/jira/browse/CALCITE-2226    */
specifier|public
specifier|static
class|class
name|DruidSubstringOperatorConversion
extends|extends
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|SubstringOperatorConversion
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|query
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
specifier|final
name|String
name|arg
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|indexStart
decl_stmt|;
specifier|final
name|String
name|length
decl_stmt|;
comment|// SQL is 1-indexed, Druid is 0-indexed.
if|if
condition|(
operator|!
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|isA
argument_list|(
name|SqlKind
operator|.
name|LITERAL
argument_list|)
condition|)
block|{
specifier|final
name|String
name|indexExp
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexExp
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|indexStart
operator|=
name|DruidQuery
operator|.
name|format
argument_list|(
literal|"(%s - 1)"
argument_list|,
name|indexExp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|int
name|index
init|=
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|-
literal|1
decl_stmt|;
name|indexStart
operator|=
name|DruidExpressions
operator|.
name|numberLiteral
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|2
condition|)
block|{
comment|//case substring from index with length
if|if
condition|(
operator|!
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|isA
argument_list|(
name|SqlKind
operator|.
name|LITERAL
argument_list|)
condition|)
block|{
name|length
operator|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
name|length
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
name|length
operator|=
name|DruidExpressions
operator|.
name|numberLiteral
argument_list|(
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|//case substring from index to the end
name|length
operator|=
name|DruidExpressions
operator|.
name|numberLiteral
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|DruidQuery
operator|.
name|format
argument_list|(
literal|"substring(%s, %s, %s)"
argument_list|,
name|arg
argument_list|,
name|indexStart
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
comment|/**    * Operator conversion form Hive TRUNC UDF to Druid Date Time UDFs.    */
specifier|public
specifier|static
class|class
name|DruidDateTruncOperatorConversion
implements|implements
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
block|{
annotation|@
name|Override
specifier|public
name|SqlOperator
name|calciteOperator
parameter_list|()
block|{
return|return
name|HiveTruncSqlOperator
operator|.
name|INSTANCE
return|;
block|}
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|query
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
comment|//can handle only case trunc date type
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"trunc() requires at least 1 argument, got "
operator|+
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
specifier|final
name|String
name|arg
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|SqlTypeUtil
operator|.
name|isDatetime
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
specifier|final
name|TimeZone
name|tz
init|=
name|timezoneId
argument_list|(
name|query
argument_list|,
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|applyTimestampFormat
argument_list|(
name|DruidExpressions
operator|.
name|applyTimestampFloor
argument_list|(
name|arg
argument_list|,
name|Period
operator|.
name|days
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
argument_list|,
name|tz
argument_list|)
argument_list|,
name|YYYY_MM_DD
argument_list|,
name|tz
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|)
block|{
specifier|final
name|String
name|arg
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|granularity
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|granularity
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|unit
decl_stmt|;
if|if
condition|(
literal|"'MONTH'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
operator|||
literal|"'MON'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
operator|||
literal|"'MM'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
condition|)
block|{
name|unit
operator|=
name|Period
operator|.
name|months
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"'YEAR'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
operator|||
literal|"'YYYY'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
operator|||
literal|"'YY'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
condition|)
block|{
name|unit
operator|=
name|Period
operator|.
name|years
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"'QUARTER'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
operator|||
literal|"'Q'"
operator|.
name|equals
argument_list|(
name|granularity
argument_list|)
condition|)
block|{
name|unit
operator|=
name|Period
operator|.
name|months
argument_list|(
literal|3
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|unit
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|unit
operator|==
literal|null
condition|)
block|{
comment|//bail out can not infer unit
return|return
literal|null
return|;
block|}
specifier|final
name|TimeZone
name|tz
init|=
name|timezoneId
argument_list|(
name|query
argument_list|,
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|applyTimestampFormat
argument_list|(
name|DruidExpressions
operator|.
name|applyTimestampFloor
argument_list|(
name|arg
argument_list|,
name|unit
argument_list|,
literal|""
argument_list|,
name|tz
argument_list|)
argument_list|,
name|YYYY_MM_DD
argument_list|,
name|tz
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Expression operator conversion form Hive TO_DATE operator to Druid Date cast.    */
specifier|public
specifier|static
class|class
name|DruidToDateOperatorConversion
implements|implements
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
block|{
annotation|@
name|Override
specifier|public
name|SqlOperator
name|calciteOperator
parameter_list|()
block|{
return|return
name|HiveToDateSqlOperator
operator|.
name|INSTANCE
return|;
block|}
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|query
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"to_date() requires 1 argument, got "
operator|+
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
specifier|final
name|String
name|arg
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|DruidExpressions
operator|.
name|applyTimestampFloor
argument_list|(
name|arg
argument_list|,
name|Period
operator|.
name|days
argument_list|(
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|""
argument_list|,
name|timezoneId
argument_list|(
name|query
argument_list|,
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|DruidUnixTimestampOperatorConversion
implements|implements
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
block|{
annotation|@
name|Override
specifier|public
name|SqlOperator
name|calciteOperator
parameter_list|()
block|{
return|return
name|HiveUnixTimestampSqlOperator
operator|.
name|INSTANCE
return|;
block|}
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|query
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
specifier|final
name|String
name|arg0
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg0
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|SqlTypeUtil
operator|.
name|isDatetime
argument_list|(
operator|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
operator|)
argument_list|)
condition|)
block|{
comment|// Timestamp is represented as long internally no need to any thing here
return|return
name|DruidExpressions
operator|.
name|functionCall
argument_list|(
literal|"div"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|arg0
argument_list|,
name|DruidExpressions
operator|.
name|numberLiteral
argument_list|(
literal|1000
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|// dealing with String type
specifier|final
name|String
name|format
init|=
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|?
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
else|:
name|DEFAULT_TS_FORMAT
decl_stmt|;
return|return
name|DruidExpressions
operator|.
name|functionCall
argument_list|(
literal|"unix_timestamp"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|arg0
argument_list|,
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
name|format
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|DruidFormUnixTimeOperatorConversion
implements|implements
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
block|{
annotation|@
name|Override
specifier|public
name|SqlOperator
name|calciteOperator
parameter_list|()
block|{
return|return
name|HiveFromUnixTimeSqlOperator
operator|.
name|INSTANCE
return|;
block|}
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|query
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|1
operator|||
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"form_unixtime() requires 1 or 2 argument, got "
operator|+
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
specifier|final
name|String
name|arg
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|numMillis
init|=
name|DruidQuery
operator|.
name|format
argument_list|(
literal|"(%s * '1000')"
argument_list|,
name|arg
argument_list|)
decl_stmt|;
specifier|final
name|String
name|format
init|=
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
name|DEFAULT_TS_FORMAT
argument_list|)
else|:
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
return|return
name|DruidExpressions
operator|.
name|functionCall
argument_list|(
literal|"timestamp_format"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|numMillis
argument_list|,
name|format
argument_list|,
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
operator|.
name|getID
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * Base class for Date Add/Sub operator conversion    */
specifier|public
specifier|static
class|class
name|DruidDateArithmeticOperatorConversion
implements|implements
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
block|{
specifier|private
specifier|final
name|int
name|direction
decl_stmt|;
specifier|private
specifier|final
name|SqlOperator
name|operator
decl_stmt|;
specifier|public
name|DruidDateArithmeticOperatorConversion
parameter_list|(
name|int
name|direction
parameter_list|,
name|SqlOperator
name|operator
parameter_list|)
block|{
name|this
operator|.
name|direction
operator|=
name|direction
expr_stmt|;
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|direction
operator|==
literal|1
operator|||
name|direction
operator|==
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SqlOperator
name|calciteOperator
parameter_list|()
block|{
return|return
name|operator
return|;
block|}
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|query
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"date_add/date_sub() requires 2 arguments, got "
operator|+
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
specifier|final
name|String
name|arg0
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
specifier|final
name|String
name|arg1
init|=
name|DruidExpressions
operator|.
name|toDruidExpression
argument_list|(
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|rowType
argument_list|,
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|arg0
operator|==
literal|null
operator|||
name|arg1
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|steps
init|=
name|direction
operator|==
operator|-
literal|1
condition|?
name|DruidQuery
operator|.
name|format
argument_list|(
literal|"-( %s )"
argument_list|,
name|arg1
argument_list|)
else|:
name|arg1
decl_stmt|;
return|return
name|DruidExpressions
operator|.
name|functionCall
argument_list|(
literal|"timestamp_shift"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|arg0
argument_list|,
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
literal|"P1D"
argument_list|)
argument_list|,
name|steps
argument_list|,
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
name|timezoneId
argument_list|(
name|query
argument_list|,
name|call
operator|.
name|getOperands
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|getID
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * utility function to extract timezone id from Druid query    * @param query Druid Rel    * @return time zone    */
specifier|private
specifier|static
name|TimeZone
name|timezoneId
parameter_list|(
specifier|final
name|DruidQuery
name|query
parameter_list|,
specifier|final
name|RexNode
name|arg
parameter_list|)
block|{
return|return
name|arg
operator|.
name|getType
argument_list|()
operator|.
name|getSqlTypeName
argument_list|()
operator|==
name|SqlTypeName
operator|.
name|TIMESTAMP_WITH_LOCAL_TIME_ZONE
condition|?
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
name|query
operator|.
name|getTopNode
argument_list|()
operator|.
name|getCluster
argument_list|()
operator|.
name|getPlanner
argument_list|()
operator|.
name|getContext
argument_list|()
operator|.
name|unwrap
argument_list|(
name|CalciteConnectionConfig
operator|.
name|class
argument_list|)
operator|.
name|timeZone
argument_list|()
argument_list|)
else|:
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
literal|"UTC"
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|applyTimestampFormat
parameter_list|(
name|String
name|arg
parameter_list|,
name|String
name|format
parameter_list|,
name|TimeZone
name|timeZone
parameter_list|)
block|{
return|return
name|DruidExpressions
operator|.
name|functionCall
argument_list|(
literal|"timestamp_format"
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|arg
argument_list|,
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
name|format
argument_list|)
argument_list|,
name|DruidExpressions
operator|.
name|stringLiteral
argument_list|(
name|timeZone
operator|.
name|getID
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|UnaryFunctionOperatorConversion
implements|implements
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidSqlOperatorConverter
block|{
specifier|private
specifier|final
name|SqlOperator
name|operator
decl_stmt|;
specifier|private
specifier|final
name|String
name|druidOperator
decl_stmt|;
specifier|public
name|UnaryFunctionOperatorConversion
parameter_list|(
name|SqlOperator
name|operator
parameter_list|,
name|String
name|druidOperator
parameter_list|)
block|{
name|this
operator|.
name|operator
operator|=
name|operator
expr_stmt|;
name|this
operator|.
name|druidOperator
operator|=
name|druidOperator
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SqlOperator
name|calciteOperator
parameter_list|()
block|{
return|return
name|operator
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toDruidExpression
parameter_list|(
name|RexNode
name|rexNode
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|DruidQuery
name|druidQuery
parameter_list|)
block|{
specifier|final
name|RexCall
name|call
init|=
operator|(
name|RexCall
operator|)
name|rexNode
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|druidExpressions
init|=
name|DruidExpressions
operator|.
name|toDruidExpressions
argument_list|(
name|druidQuery
argument_list|,
name|rowType
argument_list|,
name|call
operator|.
name|getOperands
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|druidExpressions
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|DruidQuery
operator|.
name|format
argument_list|(
literal|"%s(%s)"
argument_list|,
name|druidOperator
argument_list|,
name|Iterables
operator|.
name|getOnlyElement
argument_list|(
name|druidExpressions
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


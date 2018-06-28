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
name|udf
operator|.
name|generic
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
name|VectorUDFDateSubColCol
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
name|VectorUDFDateSubColScalar
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
name|VectorUDFDateSubScalarCol
import|;
end_import

begin_comment
comment|/**  * UDFDateSub.  *  * Subtract a number of days to the date. The time part of the string will be  * ignored.  *  * NOTE: This is a subset of what MySQL offers as:  * http://dev.mysql.com/doc/refman  * /5.1/en/date-and-time-functions.html#function_date-sub  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"date_sub"
argument_list|,
name|value
operator|=
literal|"_FUNC_(start_date, num_days) - Returns the date that is num_days before start_date."
argument_list|,
name|extended
operator|=
literal|"start_date is a string in the format 'yyyy-MM-dd HH:mm:ss' or"
operator|+
literal|" 'yyyy-MM-dd'. num_days is a number. The time part of start_date is "
operator|+
literal|"ignored.\n"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_('2009-07-30', 1) FROM src LIMIT 1;\n"
operator|+
literal|"  '2009-07-29'"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|VectorUDFDateSubColScalar
operator|.
name|class
block|,
name|VectorUDFDateSubScalarCol
operator|.
name|class
block|,
name|VectorUDFDateSubColCol
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFDateSub
extends|extends
name|GenericUDFDateAdd
block|{
specifier|private
specifier|transient
name|SimpleDateFormat
name|formatter
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
decl_stmt|;
specifier|public
name|GenericUDFDateSub
parameter_list|()
block|{
name|this
operator|.
name|signModifier
operator|=
operator|-
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|getStandardDisplayString
argument_list|(
literal|"date_sub"
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

end_unit


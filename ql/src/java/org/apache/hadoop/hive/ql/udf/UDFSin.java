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
name|FuncSinDoubleToDouble
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
name|FuncSinLongToDouble
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
name|io
operator|.
name|DoubleWritable
import|;
end_import

begin_comment
comment|/**  * UDFSin.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"sin"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - returns the sine of x (x is in radians)"
argument_list|,
name|extended
operator|=
literal|"Example:\n "
operator|+
literal|"> SELECT _FUNC_(0) FROM src LIMIT 1;\n"
operator|+
literal|"  0"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|FuncSinLongToDouble
operator|.
name|class
block|,
name|FuncSinDoubleToDouble
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|UDFSin
extends|extends
name|UDFMath
block|{
specifier|private
specifier|final
name|DoubleWritable
name|result
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
comment|/**    * Take Sine of a.    */
annotation|@
name|Override
specifier|protected
name|DoubleWritable
name|doEvaluate
parameter_list|(
name|DoubleWritable
name|a
parameter_list|)
block|{
name|result
operator|.
name|set
argument_list|(
name|Math
operator|.
name|sin
argument_list|(
name|a
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit


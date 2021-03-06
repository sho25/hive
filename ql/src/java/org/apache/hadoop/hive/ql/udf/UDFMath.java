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
name|UDF
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
name|HiveDecimalWritable
import|;
end_import

begin_comment
comment|/**  * This class can be used for math based UDFs that only have an evaluate method for {@code doubles}. By extending from  * this class these UDFs will automatically support decimals as well.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|UDFMath
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|DoubleWritable
name|doubleWritable
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
comment|/**    * For subclass to implement.    */
specifier|protected
specifier|abstract
name|DoubleWritable
name|doEvaluate
parameter_list|(
name|DoubleWritable
name|a
parameter_list|)
function_decl|;
comment|/**    * Returns {@code null} if the passed in value is {@code} and passes on to {@link #doEvaluate(DoubleWritable)} if not.    */
specifier|public
specifier|final
name|DoubleWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|a
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|doEvaluate
argument_list|(
name|a
argument_list|)
return|;
block|}
comment|/**    * Convert HiveDecimal to a double and call evaluate() on it.    */
specifier|public
specifier|final
name|DoubleWritable
name|evaluate
parameter_list|(
name|HiveDecimalWritable
name|writable
parameter_list|)
block|{
if|if
condition|(
name|writable
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|double
name|d
init|=
name|writable
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|doubleWritable
operator|.
name|set
argument_list|(
name|d
argument_list|)
expr_stmt|;
return|return
name|doEvaluate
argument_list|(
name|doubleWritable
argument_list|)
return|;
block|}
block|}
end_class

end_unit


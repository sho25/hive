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
name|RoundingMode
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
name|ql
operator|.
name|exec
operator|.
name|description
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
name|io
operator|.
name|IntWritable
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
name|io
operator|.
name|LongWritable
import|;
end_import

begin_class
annotation|@
name|description
argument_list|(
name|name
operator|=
literal|"round"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x[, d]) - round x to d decimal places"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(12.3456, 1) FROM src LIMIT 1;\n"
operator|+
literal|"  12.3'"
argument_list|)
specifier|public
class|class
name|UDFRound
extends|extends
name|UDF
block|{
name|DoubleWritable
name|doubleWritable
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
name|LongWritable
name|longWritable
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFRound
parameter_list|()
block|{   }
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|longWritable
operator|.
name|set
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|setScale
argument_list|(
literal|0
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
specifier|public
name|DoubleWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|n
parameter_list|,
name|IntWritable
name|i
parameter_list|)
block|{
if|if
condition|(
operator|(
name|n
operator|==
literal|null
operator|)
operator|||
operator|(
name|i
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|doubleWritable
operator|.
name|set
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|n
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|setScale
argument_list|(
name|i
operator|.
name|get
argument_list|()
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|doubleWritable
return|;
block|}
block|}
end_class

end_unit


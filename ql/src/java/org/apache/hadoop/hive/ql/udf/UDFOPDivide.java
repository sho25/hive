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

begin_comment
comment|/**  * UDFOPDivide.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"/"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Divide a by b"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT 3 _FUNC_ 2 FROM src LIMIT 1;\n"
operator|+
literal|"  1.5"
argument_list|)
comment|/**  * Note that in SQL, the return type of divide is not necessarily the same   * as the parameters. For example, 3 / 2 = 1.5, not 1. To follow SQL, we always  * return a double for divide.  */
specifier|public
class|class
name|UDFOPDivide
extends|extends
name|UDF
block|{
specifier|private
name|DoubleWritable
name|doubleWritable
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
specifier|public
name|DoubleWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|a
parameter_list|,
name|DoubleWritable
name|b
parameter_list|)
block|{
comment|// LOG.info("Get input " + a.getClass() + ":" + a + " " + b.getClass() + ":"
comment|// + b);
if|if
condition|(
operator|(
name|a
operator|==
literal|null
operator|)
operator|||
operator|(
name|b
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
name|a
operator|.
name|get
argument_list|()
operator|/
name|b
operator|.
name|get
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


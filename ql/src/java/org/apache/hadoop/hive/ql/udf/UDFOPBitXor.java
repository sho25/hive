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
name|serde2
operator|.
name|io
operator|.
name|ByteWritable
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
name|ShortWritable
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

begin_comment
comment|/**  * UDFOPBitXor.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"^"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Bitwise exclusive or"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT 3 _FUNC_ 5 FROM src LIMIT 1;\n"
operator|+
literal|"  2"
argument_list|)
specifier|public
class|class
name|UDFOPBitXor
extends|extends
name|UDFBaseBitOP
block|{
specifier|public
name|UDFOPBitXor
parameter_list|()
block|{   }
specifier|public
name|ByteWritable
name|evaluate
parameter_list|(
name|ByteWritable
name|a
parameter_list|,
name|ByteWritable
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
operator|||
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byteWritable
operator|.
name|set
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|^
name|b
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|byteWritable
return|;
block|}
specifier|public
name|ShortWritable
name|evaluate
parameter_list|(
name|ShortWritable
name|a
parameter_list|,
name|ShortWritable
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
operator|||
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|shortWritable
operator|.
name|set
argument_list|(
call|(
name|short
call|)
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|^
name|b
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|shortWritable
return|;
block|}
specifier|public
name|IntWritable
name|evaluate
parameter_list|(
name|IntWritable
name|a
parameter_list|,
name|IntWritable
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
operator|||
name|b
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|intWritable
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|^
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
specifier|public
name|LongWritable
name|evaluate
parameter_list|(
name|LongWritable
name|a
parameter_list|,
name|LongWritable
name|b
parameter_list|)
block|{
if|if
condition|(
name|a
operator|==
literal|null
operator|||
name|b
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
name|a
operator|.
name|get
argument_list|()
operator|^
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|longWritable
return|;
block|}
block|}
end_class

end_unit


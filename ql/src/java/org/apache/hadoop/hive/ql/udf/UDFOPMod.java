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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FloatWritable
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
literal|"%"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns the remainder when dividing a by b"
argument_list|)
specifier|public
class|class
name|UDFOPMod
extends|extends
name|UDFBaseNumericOp
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hive.ql.udf.UDFOPMod"
argument_list|)
decl_stmt|;
specifier|public
name|UDFOPMod
parameter_list|()
block|{   }
annotation|@
name|Override
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
operator|%
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
annotation|@
name|Override
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
operator|%
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
annotation|@
name|Override
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
name|intWritable
operator|.
name|set
argument_list|(
operator|(
name|a
operator|.
name|get
argument_list|()
operator|%
name|b
operator|.
name|get
argument_list|()
operator|)
argument_list|)
expr_stmt|;
return|return
name|intWritable
return|;
block|}
annotation|@
name|Override
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
name|longWritable
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|%
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
annotation|@
name|Override
specifier|public
name|FloatWritable
name|evaluate
parameter_list|(
name|FloatWritable
name|a
parameter_list|,
name|FloatWritable
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
name|floatWritable
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|%
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|floatWritable
return|;
block|}
annotation|@
name|Override
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
operator|%
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


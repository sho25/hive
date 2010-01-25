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
name|BooleanWritable
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
name|Text
import|;
end_import

begin_comment
comment|/**  * The reason that we list evaluate methods with all numeric types is for better  * performance; otherwise a single method that takes (Number a, Number b) and  * use a.doubleValue() == b.doubleValue() is enough.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"=,=="
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns TRUE if a equals b and false otherwise"
argument_list|)
specifier|public
class|class
name|UDFOPEqual
extends|extends
name|UDFBaseCompare
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
name|UDFOPEqual
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|BooleanWritable
name|resultCache
decl_stmt|;
specifier|public
name|UDFOPEqual
parameter_list|()
block|{
name|resultCache
operator|=
operator|new
name|BooleanWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|Text
name|a
parameter_list|,
name|Text
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|ByteWritable
name|a
parameter_list|,
name|ByteWritable
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|==
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|ShortWritable
name|a
parameter_list|,
name|ShortWritable
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|==
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|IntWritable
name|a
parameter_list|,
name|IntWritable
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|==
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|LongWritable
name|a
parameter_list|,
name|LongWritable
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|==
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|FloatWritable
name|a
parameter_list|,
name|FloatWritable
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|==
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
annotation|@
name|Override
specifier|public
name|BooleanWritable
name|evaluate
parameter_list|(
name|DoubleWritable
name|a
parameter_list|,
name|DoubleWritable
name|b
parameter_list|)
block|{
name|BooleanWritable
name|r
init|=
name|resultCache
decl_stmt|;
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
name|r
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|r
operator|.
name|set
argument_list|(
name|a
operator|.
name|get
argument_list|()
operator|==
name|b
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// LOG.info("evaluate(" + a + "," + b + ")=" + r);
return|return
name|r
return|;
block|}
block|}
end_class

end_unit


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
name|contrib
operator|.
name|udaf
operator|.
name|example
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
name|UDAF
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
name|UDAFEvaluator
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
name|hive
operator|.
name|shims
operator|.
name|ShimLoader
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"example_max"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr) - Returns the maximum value of expr"
argument_list|)
specifier|public
class|class
name|UDAFExampleMax
extends|extends
name|UDAF
block|{
specifier|static
specifier|public
class|class
name|MaxShortEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|short
name|mMax
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|MaxShortEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mMax
operator|=
literal|0
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|ShortWritable
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|mEmpty
condition|)
block|{
name|mMax
operator|=
name|o
operator|.
name|get
argument_list|()
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|mMax
operator|=
operator|(
name|short
operator|)
name|Math
operator|.
name|max
argument_list|(
name|mMax
argument_list|,
name|o
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|ShortWritable
name|terminatePartial
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|ShortWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|ShortWritable
name|o
parameter_list|)
block|{
return|return
name|iterate
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|ShortWritable
name|terminate
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|ShortWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|MaxIntEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|int
name|mMax
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|MaxIntEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mMax
operator|=
literal|0
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|IntWritable
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|mEmpty
condition|)
block|{
name|mMax
operator|=
name|o
operator|.
name|get
argument_list|()
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|mMax
operator|=
name|Math
operator|.
name|max
argument_list|(
name|mMax
argument_list|,
name|o
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|IntWritable
name|terminatePartial
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|IntWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|IntWritable
name|o
parameter_list|)
block|{
return|return
name|iterate
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|IntWritable
name|terminate
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|IntWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|MaxLongEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|long
name|mMax
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|MaxLongEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mMax
operator|=
literal|0
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|LongWritable
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|mEmpty
condition|)
block|{
name|mMax
operator|=
name|o
operator|.
name|get
argument_list|()
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|mMax
operator|=
name|Math
operator|.
name|max
argument_list|(
name|mMax
argument_list|,
name|o
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|LongWritable
name|terminatePartial
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|LongWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|LongWritable
name|o
parameter_list|)
block|{
return|return
name|iterate
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|LongWritable
name|terminate
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|LongWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|MaxFloatEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|float
name|mMax
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|MaxFloatEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mMax
operator|=
literal|0
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|FloatWritable
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|mEmpty
condition|)
block|{
name|mMax
operator|=
name|o
operator|.
name|get
argument_list|()
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|mMax
operator|=
name|Math
operator|.
name|max
argument_list|(
name|mMax
argument_list|,
name|o
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|FloatWritable
name|terminatePartial
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|FloatWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|FloatWritable
name|o
parameter_list|)
block|{
return|return
name|iterate
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|FloatWritable
name|terminate
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|FloatWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|MaxDoubleEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|double
name|mMax
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|MaxDoubleEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mMax
operator|=
literal|0
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|DoubleWritable
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|mEmpty
condition|)
block|{
name|mMax
operator|=
name|o
operator|.
name|get
argument_list|()
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|mMax
operator|=
name|Math
operator|.
name|max
argument_list|(
name|mMax
argument_list|,
name|o
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|DoubleWritable
name|terminatePartial
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|DoubleWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|DoubleWritable
name|o
parameter_list|)
block|{
return|return
name|iterate
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|DoubleWritable
name|terminate
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
operator|new
name|DoubleWritable
argument_list|(
name|mMax
argument_list|)
return|;
block|}
block|}
specifier|static
specifier|public
class|class
name|MaxStringEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|Text
name|mMax
decl_stmt|;
specifier|private
name|boolean
name|mEmpty
decl_stmt|;
specifier|public
name|MaxStringEvaluator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|()
block|{
name|mMax
operator|=
literal|null
expr_stmt|;
name|mEmpty
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|iterate
parameter_list|(
name|Text
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|mEmpty
condition|)
block|{
name|mMax
operator|=
operator|new
name|Text
argument_list|(
name|o
argument_list|)
expr_stmt|;
name|mEmpty
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mMax
operator|.
name|compareTo
argument_list|(
name|o
argument_list|)
operator|<
literal|0
condition|)
block|{
name|mMax
operator|.
name|set
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|Text
name|terminatePartial
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
name|mMax
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|(
name|Text
name|o
parameter_list|)
block|{
return|return
name|iterate
argument_list|(
name|o
argument_list|)
return|;
block|}
specifier|public
name|Text
name|terminate
parameter_list|()
block|{
return|return
name|mEmpty
condition|?
literal|null
else|:
name|mMax
return|;
block|}
block|}
block|}
end_class

end_unit


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
comment|/**  * UDAFWrongArgLengthForTestCase.  *  */
end_comment

begin_class
specifier|public
class|class
name|UDAFWrongArgLengthForTestCase
extends|extends
name|UDAF
block|{
comment|/**    * UDAFWrongArgLengthForTestCaseEvaluator.    *    */
specifier|public
specifier|static
class|class
name|UDAFWrongArgLengthForTestCaseEvaluator
implements|implements
name|UDAFEvaluator
block|{
specifier|private
name|long
name|mCount
decl_stmt|;
specifier|public
name|UDAFWrongArgLengthForTestCaseEvaluator
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
name|mCount
operator|=
literal|0
expr_stmt|;
block|}
name|Text
name|emptyText
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|public
name|boolean
name|iterate
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
operator|&&
operator|!
name|emptyText
operator|.
name|equals
argument_list|(
name|o
argument_list|)
condition|)
block|{
name|mCount
operator|++
expr_stmt|;
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
operator|new
name|LongWritable
argument_list|(
name|mCount
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|merge
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|LongWritable
name|terminate
parameter_list|()
block|{
return|return
operator|new
name|LongWritable
argument_list|(
name|mCount
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


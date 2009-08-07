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
name|util
operator|.
name|Random
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
name|LongWritable
import|;
end_import

begin_class
annotation|@
name|description
argument_list|(
name|name
operator|=
literal|"rand"
argument_list|,
name|value
operator|=
literal|"_FUNC_([seed]) - Returns a pseudorandom number between 0 and 1"
argument_list|)
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|false
argument_list|)
specifier|public
class|class
name|UDFRand
extends|extends
name|UDF
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
name|UDFRand
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Random
name|random
decl_stmt|;
name|DoubleWritable
name|result
init|=
operator|new
name|DoubleWritable
argument_list|()
decl_stmt|;
specifier|public
name|UDFRand
parameter_list|()
block|{   }
specifier|public
name|DoubleWritable
name|evaluate
parameter_list|()
block|{
if|if
condition|(
name|random
operator|==
literal|null
condition|)
block|{
name|random
operator|=
operator|new
name|Random
argument_list|()
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|random
operator|.
name|nextDouble
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|DoubleWritable
name|evaluate
parameter_list|(
name|LongWritable
name|seed
parameter_list|)
block|{
if|if
condition|(
name|random
operator|==
literal|null
condition|)
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|random
operator|.
name|nextDouble
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit


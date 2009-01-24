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
name|UDF
import|;
end_import

begin_class
specifier|public
class|class
name|UDFOPPositive
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
name|UDFOPPositive
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|UDFOPPositive
parameter_list|()
block|{   }
specifier|public
name|Byte
name|evaluate
parameter_list|(
name|Byte
name|a
parameter_list|)
block|{
return|return
name|a
return|;
block|}
specifier|public
name|Integer
name|evaluate
parameter_list|(
name|Integer
name|a
parameter_list|)
block|{
return|return
name|a
return|;
block|}
specifier|public
name|Long
name|evaluate
parameter_list|(
name|Long
name|a
parameter_list|)
block|{
return|return
name|a
return|;
block|}
specifier|public
name|Float
name|evaluate
parameter_list|(
name|Float
name|a
parameter_list|)
block|{
return|return
name|a
return|;
block|}
specifier|public
name|Double
name|evaluate
parameter_list|(
name|Double
name|a
parameter_list|)
block|{
return|return
name|a
return|;
block|}
block|}
end_class

end_unit


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
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|mapreduce
operator|.
name|Job
import|;
end_import

begin_comment
comment|/** The InputFormat to use to read data from HCat */
end_comment

begin_class
specifier|public
class|class
name|HCatInputFormat
extends|extends
name|HCatBaseInputFormat
block|{
comment|/**    * Set the input to use for the Job. This queries the metadata server with    * the specified partition predicates, gets the matching partitions, puts    * the information in the conf object. The inputInfo object is updated with    * information needed in the client context    * @param job the job object    * @param inputInfo the table input info    * @throws IOException the exception in communicating with the metadata server    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|HCatTableInfo
name|inputInfo
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|InitializeInput
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|inputInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


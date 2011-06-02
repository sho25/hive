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
name|io
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
name|mapred
operator|.
name|InputFormat
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
name|mapred
operator|.
name|RecordReader
import|;
end_import

begin_comment
comment|/**  * HiveIOExceptionHandler defines an interface that all io exception handler in  * Hive should implement. Different IO exception handlers can implement  * different logics based on the exception input into it.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveIOExceptionHandler
block|{
comment|/**    * process exceptions raised when creating a record reader.    *     * @param e    * @return    */
specifier|public
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|handleRecordReaderCreationException
parameter_list|(
name|Exception
name|e
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * process exceptions thrown when calling rr's next    *     * @param e    * @param result    * @throws IOException    */
specifier|public
name|void
name|handleRecorReaderNextException
parameter_list|(
name|Exception
name|e
parameter_list|,
name|HiveIOExceptionNextHandleResult
name|result
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit


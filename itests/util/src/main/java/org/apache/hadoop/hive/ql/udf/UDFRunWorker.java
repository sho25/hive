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
name|TestTxnCommands2
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * A UDF for testing, which does key/value lookup from a file  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"runWorker"
argument_list|,
name|value
operator|=
literal|"_FUNC_() - UDF launching Compaction Worker"
argument_list|)
specifier|public
class|class
name|UDFRunWorker
extends|extends
name|UDF
block|{
specifier|public
name|void
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|TestTxnCommands2
operator|.
name|runWorker
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


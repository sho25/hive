begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|txn
operator|.
name|compactor
package|;
end_package

begin_comment
comment|/**  * Same as TestCleaner but tests delta file names in Hive 1.3.0 format   */
end_comment

begin_class
specifier|public
class|class
name|TestCleaner2
extends|extends
name|TestCleaner
block|{
specifier|public
name|TestCleaner2
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
name|boolean
name|useHive130DeltaDirName
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit


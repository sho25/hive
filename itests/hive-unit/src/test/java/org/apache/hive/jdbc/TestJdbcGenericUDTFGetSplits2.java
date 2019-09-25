begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|jdbc
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestJdbcGenericUDTFGetSplits2.  */
end_comment

begin_class
specifier|public
class|class
name|TestJdbcGenericUDTFGetSplits2
extends|extends
name|AbstractTestJdbcGenericUDTFGetSplits
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|200000
argument_list|)
specifier|public
name|void
name|testGenericUDTFOrderBySplitCount1
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testGenericUDTFOrderBySplitCount1
argument_list|(
literal|"get_llap_splits"
argument_list|,
operator|new
name|int
index|[]
block|{
literal|12
block|,
literal|3
block|,
literal|1
block|,
literal|3
block|,
literal|12
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


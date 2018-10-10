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
name|hbase
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|hbase
operator|.
name|mapreduce
operator|.
name|TableOutputFormat
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
name|io
operator|.
name|HiveOutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  *  This is a simple test to make sure HiveHBaseTableOutputFormat implements HiveOutputFormat for HBase tables.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveHBaseTableOutputFormat
block|{
annotation|@
name|Test
specifier|public
name|void
name|testInstanceOfHiveHBaseTableOutputFormat
parameter_list|()
block|{
name|HiveHBaseTableOutputFormat
name|hBaseOutputFormat
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HiveHBaseTableOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hBaseOutputFormat
operator|instanceof
name|TableOutputFormat
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hBaseOutputFormat
operator|instanceof
name|HiveOutputFormat
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


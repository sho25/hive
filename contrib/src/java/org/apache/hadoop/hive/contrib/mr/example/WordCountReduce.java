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
name|contrib
operator|.
name|mr
operator|.
name|example
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|contrib
operator|.
name|mr
operator|.
name|GenericMR
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
name|contrib
operator|.
name|mr
operator|.
name|Output
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
name|contrib
operator|.
name|mr
operator|.
name|Reducer
import|;
end_import

begin_comment
comment|/**  * Example Reducer (WordCount).   */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|WordCountReduce
block|{
specifier|public
specifier|static
name|void
name|main
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
operator|new
name|GenericMR
argument_list|()
operator|.
name|reduce
argument_list|(
name|System
operator|.
name|in
argument_list|,
name|System
operator|.
name|out
argument_list|,
operator|new
name|Reducer
argument_list|()
block|{
specifier|public
name|void
name|reduce
parameter_list|(
name|String
name|key
parameter_list|,
name|Iterator
argument_list|<
name|String
index|[]
argument_list|>
name|records
parameter_list|,
name|Output
name|output
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|records
operator|.
name|hasNext
argument_list|()
condition|)
block|{
comment|// note we use col[1] -- the key is provided again as col[0]
name|count
operator|+=
name|Integer
operator|.
name|parseInt
argument_list|(
name|records
operator|.
name|next
argument_list|()
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|collect
argument_list|(
operator|new
name|String
index|[]
block|{
name|key
block|,
name|String
operator|.
name|valueOf
argument_list|(
name|count
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


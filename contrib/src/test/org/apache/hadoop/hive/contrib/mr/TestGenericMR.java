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
name|contrib
operator|.
name|mr
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|util
operator|.
name|Shell
import|;
end_import

begin_comment
comment|/**  * TestGenericMR.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|TestGenericMR
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testReduceTooFar
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
operator|new
name|GenericMR
argument_list|()
operator|.
name|reduce
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|"a\tb\tc"
argument_list|)
argument_list|,
operator|new
name|StringWriter
argument_list|()
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
while|while
condition|(
literal|true
condition|)
block|{
name|records
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|NoSuchElementException
name|nsee
parameter_list|)
block|{
comment|// expected
return|return;
block|}
name|fail
argument_list|(
literal|"Expected NoSuchElementException"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testEmptyMap
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|StringWriter
name|out
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
operator|new
name|GenericMR
argument_list|()
operator|.
name|map
argument_list|(
operator|new
name|StringReader
argument_list|(
literal|""
argument_list|)
argument_list|,
name|out
argument_list|,
name|identityMapper
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|out
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testIdentityMap
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|in
init|=
literal|"a\tb\nc\td"
decl_stmt|;
specifier|final
name|StringWriter
name|out
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
operator|new
name|GenericMR
argument_list|()
operator|.
name|map
argument_list|(
operator|new
name|StringReader
argument_list|(
name|in
argument_list|)
argument_list|,
name|out
argument_list|,
name|identityMapper
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|in
operator|+
literal|"\n"
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testKVSplitMap
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|in
init|=
literal|"k1=v1,k2=v2\nk1=v2,k2=v3"
decl_stmt|;
specifier|final
name|String
name|expected
init|=
literal|"k1\tv1\nk2\tv2\nk1\tv2\nk2\tv3\n"
decl_stmt|;
specifier|final
name|StringWriter
name|out
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
operator|new
name|GenericMR
argument_list|()
operator|.
name|map
argument_list|(
operator|new
name|StringReader
argument_list|(
name|in
argument_list|)
argument_list|,
name|out
argument_list|,
operator|new
name|Mapper
argument_list|()
block|{
specifier|public
name|void
name|map
parameter_list|(
name|String
index|[]
name|record
parameter_list|,
name|Output
name|output
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
specifier|final
name|String
name|kvs
range|:
name|record
index|[
literal|0
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
specifier|final
name|String
index|[]
name|kv
init|=
name|kvs
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
name|output
operator|.
name|collect
argument_list|(
operator|new
name|String
index|[]
block|{
name|kv
index|[
literal|0
index|]
block|,
name|kv
index|[
literal|1
index|]
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testIdentityReduce
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|in
init|=
literal|"a\tb\nc\td"
decl_stmt|;
specifier|final
name|StringWriter
name|out
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
operator|new
name|GenericMR
argument_list|()
operator|.
name|reduce
argument_list|(
operator|new
name|StringReader
argument_list|(
name|in
argument_list|)
argument_list|,
name|out
argument_list|,
name|identityReducer
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|in
operator|+
literal|"\n"
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testWordCountReduce
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|in
init|=
literal|"hello\t1\nhello\t2\nokay\t4\nokay\t6\nokay\t2"
decl_stmt|;
specifier|final
name|StringWriter
name|out
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
operator|new
name|GenericMR
argument_list|()
operator|.
name|reduce
argument_list|(
operator|new
name|StringReader
argument_list|(
name|in
argument_list|)
argument_list|,
name|out
argument_list|,
operator|new
name|Reducer
argument_list|()
block|{
annotation|@
name|Override
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
specifier|final
name|String
name|expected
init|=
literal|"hello\t3\nokay\t12\n"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Mapper
name|identityMapper
parameter_list|()
block|{
return|return
operator|new
name|Mapper
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|String
index|[]
name|record
parameter_list|,
name|Output
name|output
parameter_list|)
throws|throws
name|Exception
block|{
name|output
operator|.
name|collect
argument_list|(
name|record
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
specifier|private
name|Reducer
name|identityReducer
parameter_list|()
block|{
return|return
operator|new
name|Reducer
argument_list|()
block|{
annotation|@
name|Override
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
while|while
condition|(
name|records
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|output
operator|.
name|collect
argument_list|(
name|records
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
end_class

end_unit


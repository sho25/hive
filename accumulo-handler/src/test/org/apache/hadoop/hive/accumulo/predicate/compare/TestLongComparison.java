begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
operator|.
name|predicate
operator|.
name|compare
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
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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

begin_class
specifier|public
class|class
name|TestLongComparison
block|{
specifier|private
name|LongCompare
name|longComp
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|byte
index|[]
name|lBytes
init|=
operator|new
name|byte
index|[
literal|8
index|]
decl_stmt|;
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|lBytes
argument_list|)
operator|.
name|putLong
argument_list|(
literal|10l
argument_list|)
expr_stmt|;
name|longComp
operator|=
operator|new
name|LongCompare
argument_list|()
expr_stmt|;
name|longComp
operator|.
name|init
argument_list|(
name|lBytes
argument_list|)
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|byte
index|[]
name|lonBytes
init|=
operator|new
name|byte
index|[
literal|8
index|]
decl_stmt|;
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|lonBytes
argument_list|)
operator|.
name|putLong
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|long
name|lon
init|=
name|longComp
operator|.
name|serialize
argument_list|(
name|lonBytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|lon
argument_list|,
name|val
argument_list|)
expr_stmt|;
return|return
name|lonBytes
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|equal
parameter_list|()
block|{
name|Equal
name|equalObj
init|=
operator|new
name|Equal
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|getBytes
argument_list|(
literal|10l
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|equalObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|notEqual
parameter_list|()
block|{
name|NotEqual
name|notEqualObj
init|=
operator|new
name|NotEqual
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|getBytes
argument_list|(
literal|11l
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|notEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|10l
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|notEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|greaterThan
parameter_list|()
block|{
name|GreaterThan
name|greaterThanObj
init|=
operator|new
name|GreaterThan
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|getBytes
argument_list|(
literal|11l
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|greaterThanObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|4l
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|greaterThanObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|10l
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|greaterThanObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|greaterThanOrEqual
parameter_list|()
block|{
name|GreaterThanOrEqual
name|greaterThanOrEqualObj
init|=
operator|new
name|GreaterThanOrEqual
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|getBytes
argument_list|(
literal|11l
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|greaterThanOrEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|4l
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|greaterThanOrEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|10l
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|greaterThanOrEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|lessThan
parameter_list|()
block|{
name|LessThan
name|lessThanObj
init|=
operator|new
name|LessThan
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|getBytes
argument_list|(
literal|11l
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|lessThanObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|4l
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lessThanObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|10l
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lessThanObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|lessThanOrEqual
parameter_list|()
block|{
name|LessThanOrEqual
name|lessThanOrEqualObj
init|=
operator|new
name|LessThanOrEqual
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
name|getBytes
argument_list|(
literal|11l
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|lessThanOrEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|4l
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lessThanOrEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getBytes
argument_list|(
literal|10l
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lessThanOrEqualObj
operator|.
name|accept
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|like
parameter_list|()
block|{
try|try
block|{
name|Like
name|likeObj
init|=
operator|new
name|Like
argument_list|(
name|longComp
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
operator|new
name|byte
index|[]
block|{}
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should not accept"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Like not supported for "
operator|+
name|longComp
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|invalidSerialization
parameter_list|()
block|{
try|try
block|{
name|byte
index|[]
name|badVal
init|=
operator|new
name|byte
index|[
literal|4
index|]
decl_stmt|;
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|badVal
argument_list|)
operator|.
name|putInt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|longComp
operator|.
name|serialize
argument_list|(
name|badVal
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|" occurred trying to build long value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


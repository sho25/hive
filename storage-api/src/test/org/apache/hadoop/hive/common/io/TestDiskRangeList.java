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
name|common
operator|.
name|io
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

begin_class
specifier|public
class|class
name|TestDiskRangeList
block|{
annotation|@
name|Test
specifier|public
name|void
name|testErrorConditions
parameter_list|()
throws|throws
name|Exception
block|{
name|DiskRangeList
name|d510
init|=
operator|new
name|DiskRangeList
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|d510
operator|.
name|insertPartBefore
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|DiskRangeList
name|d1015
init|=
name|d510
operator|.
name|insertAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|10
argument_list|,
literal|15
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|d510
operator|.
name|replaceSelfWith
argument_list|(
name|d510
argument_list|)
expr_stmt|;
comment|// The arg is self.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
name|DiskRangeList
name|existing
init|=
operator|new
name|DiskRangeList
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|existing
operator|.
name|insertPartBefore
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|d510
operator|.
name|replaceSelfWith
argument_list|(
name|existing
argument_list|)
expr_stmt|;
comment|// The arg is part of another list.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d510
operator|.
name|replaceSelfWith
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|4
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not sequential with previous.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d510
operator|.
name|replaceSelfWith
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|5
argument_list|,
literal|11
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not sequential with next.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d510
operator|.
name|insertPartBefore
argument_list|(
name|d510
argument_list|)
expr_stmt|;
comment|// The arg is self.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
name|existing
operator|=
operator|new
name|DiskRangeList
argument_list|(
literal|5
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|existing
operator|.
name|insertPartBefore
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|5
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|d510
operator|.
name|insertPartBefore
argument_list|(
name|existing
argument_list|)
expr_stmt|;
comment|// The arg is part of another list.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d510
operator|.
name|insertPartBefore
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|3
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not a part.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d510
operator|.
name|insertPartBefore
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|4
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not sequential with previous.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d510
operator|.
name|insertAfter
argument_list|(
name|d510
argument_list|)
expr_stmt|;
comment|// The arg is self.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
name|existing
operator|=
operator|new
name|DiskRangeList
argument_list|(
literal|15
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|existing
operator|.
name|insertAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|20
argument_list|,
literal|25
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|d1015
operator|.
name|insertAfter
argument_list|(
name|existing
argument_list|)
expr_stmt|;
comment|// The arg is part of another list.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d1015
operator|.
name|insertAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|14
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not sequential.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
name|d1015
operator|.
name|insertAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|20
argument_list|,
literal|25
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|d1015
operator|.
name|insertAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|15
argument_list|,
literal|21
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not sequential with next.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d1015
operator|.
name|insertPartAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|16
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not a part.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d1015
operator|.
name|insertPartAfter
argument_list|(
operator|new
name|DiskRangeList
argument_list|(
literal|9
argument_list|,
literal|11
argument_list|)
argument_list|)
expr_stmt|;
comment|// Not a part.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
try|try
block|{
name|d1015
operator|.
name|setEnd
argument_list|(
literal|21
argument_list|)
expr_stmt|;
comment|// Not sequential with next.
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|error
parameter_list|)
block|{}
block|}
specifier|private
name|void
name|fail
parameter_list|()
throws|throws
name|Exception
block|{
throw|throw
operator|new
name|Exception
argument_list|()
throw|;
comment|// Don't use Assert.fail, we are catching assertion errors.
block|}
block|}
end_class

end_unit


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
name|TestStringCompare
block|{
specifier|private
name|StringCompare
name|strCompare
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|strCompare
operator|=
operator|new
name|StringCompare
argument_list|()
expr_stmt|;
name|strCompare
operator|.
name|init
argument_list|(
literal|"aaa"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
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
name|strCompare
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"aaa"
operator|.
name|getBytes
argument_list|()
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
name|strCompare
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"aab"
operator|.
name|getBytes
argument_list|()
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
literal|"aaa"
operator|.
name|getBytes
argument_list|()
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
name|strCompare
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"aab"
operator|.
name|getBytes
argument_list|()
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
literal|"aa"
operator|.
name|getBytes
argument_list|()
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
literal|"aaa"
operator|.
name|getBytes
argument_list|()
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
name|strCompare
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"aab"
operator|.
name|getBytes
argument_list|()
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
literal|"aa"
operator|.
name|getBytes
argument_list|()
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
literal|"aaa"
operator|.
name|getBytes
argument_list|()
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
name|strCompare
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"aab"
operator|.
name|getBytes
argument_list|()
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
literal|"aa"
operator|.
name|getBytes
argument_list|()
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
literal|"aaa"
operator|.
name|getBytes
argument_list|()
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
name|strCompare
argument_list|)
decl_stmt|;
name|byte
index|[]
name|val
init|=
literal|"aab"
operator|.
name|getBytes
argument_list|()
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
literal|"aa"
operator|.
name|getBytes
argument_list|()
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
literal|"aaa"
operator|.
name|getBytes
argument_list|()
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
name|Like
name|likeObj
init|=
operator|new
name|Like
argument_list|(
name|strCompare
argument_list|)
decl_stmt|;
name|String
name|condition
init|=
literal|"%a"
decl_stmt|;
name|assertTrue
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|condition
operator|=
literal|"%a%"
expr_stmt|;
name|assertTrue
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|condition
operator|=
literal|"a%"
expr_stmt|;
name|assertTrue
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|condition
operator|=
literal|"a%aa"
expr_stmt|;
name|assertFalse
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|condition
operator|=
literal|"b%"
expr_stmt|;
name|assertFalse
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|condition
operator|=
literal|"%ab%"
expr_stmt|;
name|assertFalse
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|condition
operator|=
literal|"%ba"
expr_stmt|;
name|assertFalse
argument_list|(
name|likeObj
operator|.
name|accept
argument_list|(
name|condition
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


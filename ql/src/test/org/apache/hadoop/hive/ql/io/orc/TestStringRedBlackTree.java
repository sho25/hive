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
name|ql
operator|.
name|io
operator|.
name|orc
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
name|io
operator|.
name|DataOutputBuffer
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
name|io
operator|.
name|IntWritable
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
name|io
operator|.
name|Text
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
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

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
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_comment
comment|/**  * Test the red-black tree with string keys.  */
end_comment

begin_class
specifier|public
class|class
name|TestStringRedBlackTree
block|{
comment|/**    * Checks the red-black tree rules to make sure that we have correctly built    * a valid tree.    *    * Properties:    *   1. Red nodes must have black children    *   2. Each node must have the same black height on both sides.    *    * @param node The id of the root of the subtree to check for the red-black    *        tree properties.    * @return The black-height of the subtree.    */
specifier|private
name|int
name|checkSubtree
parameter_list|(
name|RedBlackTree
name|tree
parameter_list|,
name|int
name|node
parameter_list|,
name|IntWritable
name|count
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|node
operator|==
name|RedBlackTree
operator|.
name|NULL
condition|)
block|{
return|return
literal|1
return|;
block|}
name|count
operator|.
name|set
argument_list|(
name|count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
name|boolean
name|is_red
init|=
name|tree
operator|.
name|isRed
argument_list|(
name|node
argument_list|)
decl_stmt|;
name|int
name|left
init|=
name|tree
operator|.
name|getLeft
argument_list|(
name|node
argument_list|)
decl_stmt|;
name|int
name|right
init|=
name|tree
operator|.
name|getRight
argument_list|(
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|is_red
condition|)
block|{
if|if
condition|(
name|tree
operator|.
name|isRed
argument_list|(
name|left
argument_list|)
condition|)
block|{
name|printTree
argument_list|(
name|tree
argument_list|,
literal|""
argument_list|,
name|tree
operator|.
name|root
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Left node of "
operator|+
name|node
operator|+
literal|" is "
operator|+
name|left
operator|+
literal|" and both are red."
argument_list|)
throw|;
block|}
if|if
condition|(
name|tree
operator|.
name|isRed
argument_list|(
name|right
argument_list|)
condition|)
block|{
name|printTree
argument_list|(
name|tree
argument_list|,
literal|""
argument_list|,
name|tree
operator|.
name|root
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Right node of "
operator|+
name|node
operator|+
literal|" is "
operator|+
name|right
operator|+
literal|" and both are red."
argument_list|)
throw|;
block|}
block|}
name|int
name|left_depth
init|=
name|checkSubtree
argument_list|(
name|tree
argument_list|,
name|left
argument_list|,
name|count
argument_list|)
decl_stmt|;
name|int
name|right_depth
init|=
name|checkSubtree
argument_list|(
name|tree
argument_list|,
name|right
argument_list|,
name|count
argument_list|)
decl_stmt|;
if|if
condition|(
name|left_depth
operator|!=
name|right_depth
condition|)
block|{
name|printTree
argument_list|(
name|tree
argument_list|,
literal|""
argument_list|,
name|tree
operator|.
name|root
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Lopsided tree at node "
operator|+
name|node
operator|+
literal|" with depths "
operator|+
name|left_depth
operator|+
literal|" and "
operator|+
name|right_depth
argument_list|)
throw|;
block|}
if|if
condition|(
name|is_red
condition|)
block|{
return|return
name|left_depth
return|;
block|}
else|else
block|{
return|return
name|left_depth
operator|+
literal|1
return|;
block|}
block|}
comment|/**    * Checks the validity of the entire tree. Also ensures that the number of    * nodes visited is the same as the size of the set.    */
name|void
name|checkTree
parameter_list|(
name|RedBlackTree
name|tree
parameter_list|)
throws|throws
name|IOException
block|{
name|IntWritable
name|count
init|=
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|tree
operator|.
name|isRed
argument_list|(
name|tree
operator|.
name|root
argument_list|)
condition|)
block|{
name|printTree
argument_list|(
name|tree
argument_list|,
literal|""
argument_list|,
name|tree
operator|.
name|root
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"root is red"
argument_list|)
throw|;
block|}
name|checkSubtree
argument_list|(
name|tree
argument_list|,
name|tree
operator|.
name|root
argument_list|,
name|count
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|.
name|get
argument_list|()
operator|!=
name|tree
operator|.
name|size
condition|)
block|{
name|printTree
argument_list|(
name|tree
argument_list|,
literal|""
argument_list|,
name|tree
operator|.
name|root
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Broken tree! visited= "
operator|+
name|count
operator|.
name|get
argument_list|()
operator|+
literal|" size="
operator|+
name|tree
operator|.
name|size
argument_list|)
throw|;
block|}
block|}
name|void
name|printTree
parameter_list|(
name|RedBlackTree
name|tree
parameter_list|,
name|String
name|indent
parameter_list|,
name|int
name|node
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|node
operator|==
name|RedBlackTree
operator|.
name|NULL
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|indent
operator|+
literal|"NULL"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|indent
operator|+
literal|"Node "
operator|+
name|node
operator|+
literal|" color "
operator|+
operator|(
name|tree
operator|.
name|isRed
argument_list|(
name|node
argument_list|)
condition|?
literal|"red"
else|:
literal|"black"
operator|)
argument_list|)
expr_stmt|;
name|printTree
argument_list|(
name|tree
argument_list|,
name|indent
operator|+
literal|"  "
argument_list|,
name|tree
operator|.
name|getLeft
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
name|printTree
argument_list|(
name|tree
argument_list|,
name|indent
operator|+
literal|"  "
argument_list|,
name|tree
operator|.
name|getRight
argument_list|(
name|node
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MyVisitor
implements|implements
name|StringRedBlackTree
operator|.
name|Visitor
block|{
specifier|private
specifier|final
name|String
index|[]
name|words
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|order
decl_stmt|;
specifier|private
specifier|final
name|DataOutputBuffer
name|buffer
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|int
name|current
init|=
literal|0
decl_stmt|;
name|MyVisitor
parameter_list|(
name|String
index|[]
name|args
parameter_list|,
name|int
index|[]
name|order
parameter_list|)
block|{
name|words
operator|=
name|args
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|StringRedBlackTree
operator|.
name|VisitorContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|word
init|=
name|context
operator|.
name|getText
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"in word "
operator|+
name|current
argument_list|,
name|words
index|[
name|current
index|]
argument_list|,
name|word
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"in word "
operator|+
name|current
argument_list|,
name|order
index|[
name|current
index|]
argument_list|,
name|context
operator|.
name|getOriginalPosition
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|context
operator|.
name|writeBytes
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|word
argument_list|,
operator|new
name|String
argument_list|(
name|buffer
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|current
operator|+=
literal|1
expr_stmt|;
block|}
block|}
name|void
name|checkContents
parameter_list|(
name|StringRedBlackTree
name|tree
parameter_list|,
name|int
index|[]
name|order
parameter_list|,
name|String
modifier|...
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|tree
operator|.
name|visit
argument_list|(
operator|new
name|MyVisitor
argument_list|(
name|params
argument_list|,
name|order
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StringRedBlackTree
name|buildTree
parameter_list|(
name|String
modifier|...
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|StringRedBlackTree
name|result
init|=
operator|new
name|StringRedBlackTree
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|word
range|:
name|params
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test1
parameter_list|()
throws|throws
name|Exception
block|{
name|StringRedBlackTree
name|tree
init|=
operator|new
name|StringRedBlackTree
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tree
operator|.
name|getSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"owen"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"ashutosh"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"owen"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"alan"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"alan"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"ashutosh"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"greg"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"eric"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"arun"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|tree
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"eric14"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"o"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"ziggy"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"z"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|checkContents
argument_list|(
name|tree
argument_list|,
operator|new
name|int
index|[]
block|{
literal|2
block|,
literal|5
block|,
literal|1
block|,
literal|4
block|,
literal|6
block|,
literal|3
block|,
literal|7
block|,
literal|0
block|,
literal|9
block|,
literal|8
block|}
argument_list|,
literal|"alan"
argument_list|,
literal|"arun"
argument_list|,
literal|"ashutosh"
argument_list|,
literal|"eric"
argument_list|,
literal|"eric14"
argument_list|,
literal|"greg"
argument_list|,
literal|"o"
argument_list|,
literal|"owen"
argument_list|,
literal|"z"
argument_list|,
literal|"ziggy"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|32888
argument_list|,
name|tree
operator|.
name|getSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// check that adding greg again bumps the count
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"greg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|41
argument_list|,
name|tree
operator|.
name|getCharacterSize
argument_list|()
argument_list|)
expr_stmt|;
comment|// add some more strings to test the different branches of the
comment|// rebalancing
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"zak"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"eric1"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"ash"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|13
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"harry"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|14
argument_list|,
name|tree
operator|.
name|add
argument_list|(
literal|"john"
argument_list|)
argument_list|)
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|tree
operator|.
name|clear
argument_list|()
expr_stmt|;
name|checkTree
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tree
operator|.
name|getSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tree
operator|.
name|getCharacterSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test2
parameter_list|()
throws|throws
name|Exception
block|{
name|StringRedBlackTree
name|tree
init|=
name|buildTree
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|,
literal|"d"
argument_list|,
literal|"e"
argument_list|,
literal|"f"
argument_list|,
literal|"g"
argument_list|,
literal|"h"
argument_list|,
literal|"i"
argument_list|,
literal|"j"
argument_list|,
literal|"k"
argument_list|,
literal|"l"
argument_list|,
literal|"m"
argument_list|,
literal|"n"
argument_list|,
literal|"o"
argument_list|,
literal|"p"
argument_list|,
literal|"q"
argument_list|,
literal|"r"
argument_list|,
literal|"s"
argument_list|,
literal|"t"
argument_list|,
literal|"u"
argument_list|,
literal|"v"
argument_list|,
literal|"w"
argument_list|,
literal|"x"
argument_list|,
literal|"y"
argument_list|,
literal|"z"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|26
argument_list|,
name|tree
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|checkContents
argument_list|(
name|tree
argument_list|,
operator|new
name|int
index|[]
block|{
literal|0
block|,
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|15
block|,
literal|16
block|,
literal|17
block|,
literal|18
block|,
literal|19
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|24
block|,
literal|25
block|}
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|,
literal|"d"
argument_list|,
literal|"e"
argument_list|,
literal|"f"
argument_list|,
literal|"g"
argument_list|,
literal|"h"
argument_list|,
literal|"i"
argument_list|,
literal|"j"
argument_list|,
literal|"k"
argument_list|,
literal|"l"
argument_list|,
literal|"m"
argument_list|,
literal|"n"
argument_list|,
literal|"o"
argument_list|,
literal|"p"
argument_list|,
literal|"q"
argument_list|,
literal|"r"
argument_list|,
literal|"s"
argument_list|,
literal|"t"
argument_list|,
literal|"u"
argument_list|,
literal|"v"
argument_list|,
literal|"w"
argument_list|,
literal|"x"
argument_list|,
literal|"y"
argument_list|,
literal|"z"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test3
parameter_list|()
throws|throws
name|Exception
block|{
name|StringRedBlackTree
name|tree
init|=
name|buildTree
argument_list|(
literal|"z"
argument_list|,
literal|"y"
argument_list|,
literal|"x"
argument_list|,
literal|"w"
argument_list|,
literal|"v"
argument_list|,
literal|"u"
argument_list|,
literal|"t"
argument_list|,
literal|"s"
argument_list|,
literal|"r"
argument_list|,
literal|"q"
argument_list|,
literal|"p"
argument_list|,
literal|"o"
argument_list|,
literal|"n"
argument_list|,
literal|"m"
argument_list|,
literal|"l"
argument_list|,
literal|"k"
argument_list|,
literal|"j"
argument_list|,
literal|"i"
argument_list|,
literal|"h"
argument_list|,
literal|"g"
argument_list|,
literal|"f"
argument_list|,
literal|"e"
argument_list|,
literal|"d"
argument_list|,
literal|"c"
argument_list|,
literal|"b"
argument_list|,
literal|"a"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|26
argument_list|,
name|tree
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|checkContents
argument_list|(
name|tree
argument_list|,
operator|new
name|int
index|[]
block|{
literal|25
block|,
literal|24
block|,
literal|23
block|,
literal|22
block|,
literal|21
block|,
literal|20
block|,
literal|19
block|,
literal|18
block|,
literal|17
block|,
literal|16
block|,
literal|15
block|,
literal|14
block|,
literal|13
block|,
literal|12
block|,
literal|11
block|,
literal|10
block|,
literal|9
block|,
literal|8
block|,
literal|7
block|,
literal|6
block|,
literal|5
block|,
literal|4
block|,
literal|3
block|,
literal|2
block|,
literal|1
block|,
literal|0
block|}
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|,
literal|"d"
argument_list|,
literal|"e"
argument_list|,
literal|"f"
argument_list|,
literal|"g"
argument_list|,
literal|"h"
argument_list|,
literal|"i"
argument_list|,
literal|"j"
argument_list|,
literal|"k"
argument_list|,
literal|"l"
argument_list|,
literal|"m"
argument_list|,
literal|"n"
argument_list|,
literal|"o"
argument_list|,
literal|"p"
argument_list|,
literal|"q"
argument_list|,
literal|"r"
argument_list|,
literal|"s"
argument_list|,
literal|"t"
argument_list|,
literal|"u"
argument_list|,
literal|"v"
argument_list|,
literal|"w"
argument_list|,
literal|"x"
argument_list|,
literal|"y"
argument_list|,
literal|"z"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|TestStringRedBlackTree
name|test
init|=
operator|new
name|TestStringRedBlackTree
argument_list|()
decl_stmt|;
name|test
operator|.
name|test1
argument_list|()
expr_stmt|;
name|test
operator|.
name|test2
argument_list|()
expr_stmt|;
name|test
operator|.
name|test3
argument_list|()
expr_stmt|;
name|TestSerializationUtils
name|serUtils
init|=
operator|new
name|TestSerializationUtils
argument_list|()
decl_stmt|;
name|serUtils
operator|.
name|testDoubles
argument_list|()
expr_stmt|;
name|TestDynamicArray
name|test6
init|=
operator|new
name|TestDynamicArray
argument_list|()
decl_stmt|;
name|test6
operator|.
name|testByteArray
argument_list|()
expr_stmt|;
name|test6
operator|.
name|testIntArray
argument_list|()
expr_stmt|;
name|TestZlib
name|zlib
init|=
operator|new
name|TestZlib
argument_list|()
decl_stmt|;
name|zlib
operator|.
name|testCorrupt
argument_list|()
expr_stmt|;
name|zlib
operator|.
name|testNoOverflow
argument_list|()
expr_stmt|;
name|TestInStream
name|inStreamTest
init|=
operator|new
name|TestInStream
argument_list|()
decl_stmt|;
name|inStreamTest
operator|.
name|testUncompressed
argument_list|()
expr_stmt|;
name|inStreamTest
operator|.
name|testCompressed
argument_list|()
expr_stmt|;
name|inStreamTest
operator|.
name|testCorruptStream
argument_list|()
expr_stmt|;
name|TestRunLengthByteReader
name|rleByte
init|=
operator|new
name|TestRunLengthByteReader
argument_list|()
decl_stmt|;
name|rleByte
operator|.
name|testUncompressedSeek
argument_list|()
expr_stmt|;
name|rleByte
operator|.
name|testCompressedSeek
argument_list|()
expr_stmt|;
name|rleByte
operator|.
name|testSkips
argument_list|()
expr_stmt|;
name|TestRunLengthIntegerReader
name|rleInt
init|=
operator|new
name|TestRunLengthIntegerReader
argument_list|()
decl_stmt|;
name|rleInt
operator|.
name|testUncompressedSeek
argument_list|()
expr_stmt|;
name|rleInt
operator|.
name|testCompressedSeek
argument_list|()
expr_stmt|;
name|rleInt
operator|.
name|testSkips
argument_list|()
expr_stmt|;
name|TestBitFieldReader
name|bit
init|=
operator|new
name|TestBitFieldReader
argument_list|()
decl_stmt|;
name|bit
operator|.
name|testUncompressedSeek
argument_list|()
expr_stmt|;
name|bit
operator|.
name|testCompressedSeek
argument_list|()
expr_stmt|;
name|bit
operator|.
name|testBiggerItems
argument_list|()
expr_stmt|;
name|bit
operator|.
name|testSkips
argument_list|()
expr_stmt|;
name|TestOrcFile
name|test1
init|=
operator|new
name|TestOrcFile
argument_list|()
decl_stmt|;
name|test1
operator|.
name|test1
argument_list|()
expr_stmt|;
name|test1
operator|.
name|emptyFile
argument_list|()
expr_stmt|;
name|test1
operator|.
name|metaData
argument_list|()
expr_stmt|;
name|test1
operator|.
name|testUnionAndTimestamp
argument_list|()
expr_stmt|;
name|test1
operator|.
name|columnProjection
argument_list|()
expr_stmt|;
name|test1
operator|.
name|testSnappy
argument_list|()
expr_stmt|;
name|test1
operator|.
name|testWithoutIndex
argument_list|()
expr_stmt|;
name|test1
operator|.
name|testSeek
argument_list|()
expr_stmt|;
name|TestFileDump
name|test2
init|=
operator|new
name|TestFileDump
argument_list|()
decl_stmt|;
name|test2
operator|.
name|testDump
argument_list|()
expr_stmt|;
name|TestStreamName
name|test3
init|=
operator|new
name|TestStreamName
argument_list|()
decl_stmt|;
name|test3
operator|.
name|test1
argument_list|()
expr_stmt|;
name|TestInputOutputFormat
name|test4
init|=
operator|new
name|TestInputOutputFormat
argument_list|()
decl_stmt|;
name|test4
operator|.
name|testInOutFormat
argument_list|()
expr_stmt|;
name|test4
operator|.
name|testMROutput
argument_list|()
expr_stmt|;
name|test4
operator|.
name|testEmptyFile
argument_list|()
expr_stmt|;
name|test4
operator|.
name|testDefaultTypes
argument_list|()
expr_stmt|;
name|TestOrcStruct
name|test5
init|=
operator|new
name|TestOrcStruct
argument_list|()
decl_stmt|;
name|test5
operator|.
name|testStruct
argument_list|()
expr_stmt|;
name|test5
operator|.
name|testInspectorFromTypeInfo
argument_list|()
expr_stmt|;
name|test5
operator|.
name|testUnion
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


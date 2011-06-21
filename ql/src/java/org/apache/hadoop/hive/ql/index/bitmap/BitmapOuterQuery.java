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
name|index
operator|.
name|bitmap
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
name|plan
operator|.
name|ExprNodeDesc
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
name|index
operator|.
name|bitmap
operator|.
name|BitmapQuery
import|;
end_import

begin_comment
comment|/**  * Representation of the outer query on bitmap indexes that JOINs the result of  * inner SELECT scans on bitmap indexes (represented in BitmapQuery objects)  * using EWAH_* bitwise operations  */
end_comment

begin_class
specifier|public
class|class
name|BitmapOuterQuery
implements|implements
name|BitmapQuery
block|{
specifier|private
name|String
name|alias
decl_stmt|;
specifier|private
name|BitmapQuery
name|lhs
decl_stmt|;
specifier|private
name|BitmapQuery
name|rhs
decl_stmt|;
specifier|private
name|String
name|queryStr
decl_stmt|;
specifier|public
name|BitmapOuterQuery
parameter_list|(
name|String
name|alias
parameter_list|,
name|BitmapQuery
name|lhs
parameter_list|,
name|BitmapQuery
name|rhs
parameter_list|)
block|{
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|lhs
operator|=
name|lhs
expr_stmt|;
name|this
operator|.
name|rhs
operator|=
name|rhs
expr_stmt|;
name|constructQueryStr
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getAlias
parameter_list|()
block|{
return|return
name|alias
return|;
block|}
comment|/**    * Return a string representation of the query for compilation    */
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|queryStr
return|;
block|}
comment|/**    * Construct a string representation of the query to be compiled    */
specifier|private
name|void
name|constructQueryStr
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"(SELECT "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_bucketname`, "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_offset`, "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"EWAH_BITMAP_AND("
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_bitmaps`, "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_bitmaps`) AS `_bitmaps` FROM "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lhs
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" JOIN "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rhs
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ON "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_bucketname` = "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_bucketname` AND "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|lhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_offset` = "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rhs
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|".`_offset`) "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|alias
argument_list|)
expr_stmt|;
name|queryStr
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


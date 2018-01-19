begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
package|;
end_package

begin_comment
comment|/**  * OutputFormat for vertical column name: value format.  *  */
end_comment

begin_class
class|class
name|VerticalOutputFormat
implements|implements
name|OutputFormat
block|{
specifier|private
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
comment|/**    * @param beeLine    */
name|VerticalOutputFormat
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|)
block|{
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
block|}
specifier|public
name|int
name|print
parameter_list|(
name|Rows
name|rows
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|Rows
operator|.
name|Row
name|header
init|=
operator|(
name|Rows
operator|.
name|Row
operator|)
name|rows
operator|.
name|next
argument_list|()
decl_stmt|;
while|while
condition|(
name|rows
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|printRow
argument_list|(
name|rows
argument_list|,
name|header
argument_list|,
operator|(
name|Rows
operator|.
name|Row
operator|)
name|rows
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|public
name|void
name|printRow
parameter_list|(
name|Rows
name|rows
parameter_list|,
name|Rows
operator|.
name|Row
name|header
parameter_list|,
name|Rows
operator|.
name|Row
name|row
parameter_list|)
block|{
name|String
index|[]
name|head
init|=
name|header
operator|.
name|values
decl_stmt|;
name|String
index|[]
name|vals
init|=
name|row
operator|.
name|values
decl_stmt|;
name|int
name|headwidth
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|head
operator|.
name|length
operator|&&
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|headwidth
operator|=
name|Math
operator|.
name|max
argument_list|(
name|headwidth
argument_list|,
name|head
index|[
name|i
index|]
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|headwidth
operator|+=
literal|2
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|head
operator|.
name|length
operator|&&
name|i
operator|<
name|vals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|beeLine
operator|.
name|output
argument_list|(
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|bold
argument_list|(
name|beeLine
operator|.
name|getColorBuffer
argument_list|()
operator|.
name|pad
argument_list|(
name|head
index|[
name|i
index|]
argument_list|,
name|headwidth
argument_list|)
operator|.
name|getMono
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
name|vals
index|[
name|i
index|]
operator|==
literal|null
condition|?
literal|""
else|:
name|vals
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|beeLine
operator|.
name|output
argument_list|(
literal|""
argument_list|)
expr_stmt|;
comment|// spacing
block|}
block|}
end_class

end_unit


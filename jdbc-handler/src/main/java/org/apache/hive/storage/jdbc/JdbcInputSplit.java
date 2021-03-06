begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
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
name|fs
operator|.
name|Path
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|InputSplit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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

begin_class
specifier|public
class|class
name|JdbcInputSplit
extends|extends
name|FileSplit
implements|implements
name|InputSplit
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|EMPTY_ARRAY
init|=
operator|new
name|String
index|[]
block|{}
decl_stmt|;
specifier|private
name|int
name|limit
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|offset
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|partitionColumn
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|lowerBound
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|upperBound
init|=
literal|null
decl_stmt|;
specifier|public
name|JdbcInputSplit
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|JdbcInputSplit
parameter_list|(
name|Path
name|dummyPath
parameter_list|)
block|{
name|super
argument_list|(
name|dummyPath
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|JdbcInputSplit
parameter_list|(
name|int
name|limit
parameter_list|,
name|int
name|offset
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
block|{
name|super
argument_list|(
name|dummyPath
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
block|}
specifier|public
name|JdbcInputSplit
parameter_list|(
name|String
name|partitionColumn
parameter_list|,
name|String
name|lowerBound
parameter_list|,
name|String
name|upperBound
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
block|{
name|super
argument_list|(
name|dummyPath
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
name|this
operator|.
name|partitionColumn
operator|=
name|partitionColumn
expr_stmt|;
name|this
operator|.
name|lowerBound
operator|=
name|lowerBound
expr_stmt|;
name|this
operator|.
name|upperBound
operator|=
name|upperBound
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|limit
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
name|partitionColumn
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|partitionColumn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lowerBound
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|lowerBound
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|upperBound
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|upperBound
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|limit
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|offset
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|boolean
name|partitionColumnExists
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionColumnExists
condition|)
block|{
name|partitionColumn
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|boolean
name|lowerBoundExists
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|lowerBoundExists
condition|)
block|{
name|lowerBound
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|boolean
name|upperBoundExists
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|upperBoundExists
condition|)
block|{
name|upperBound
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|EMPTY_ARRAY
return|;
block|}
specifier|public
name|int
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
specifier|public
name|void
name|setLimit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
specifier|public
name|void
name|setOffset
parameter_list|(
name|int
name|offset
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
block|}
specifier|public
name|String
name|getPartitionColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|partitionColumn
return|;
block|}
specifier|public
name|String
name|getLowerBound
parameter_list|()
block|{
return|return
name|this
operator|.
name|lowerBound
return|;
block|}
specifier|public
name|String
name|getUpperBound
parameter_list|()
block|{
return|return
name|this
operator|.
name|upperBound
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionColumn
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"interval:"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|partitionColumn
argument_list|)
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
if|if
condition|(
name|lowerBound
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|lowerBound
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
if|if
condition|(
name|upperBound
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|upperBound
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"limit:"
operator|+
name|limit
operator|+
literal|", offset:"
operator|+
name|offset
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit


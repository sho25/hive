begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
package|;
end_package

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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_comment
comment|/**  * @deprecated Use/modify {@link org.apache.hive.hcatalog.data.DefaultHCatRecord} instead  */
end_comment

begin_class
specifier|public
class|class
name|DefaultHCatRecord
extends|extends
name|HCatRecord
block|{
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|contents
decl_stmt|;
specifier|public
name|DefaultHCatRecord
parameter_list|()
block|{
name|contents
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|DefaultHCatRecord
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|contents
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|size
argument_list|)
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|contents
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|(
name|int
name|idx
parameter_list|)
throws|throws
name|HCatException
block|{
name|contents
operator|.
name|remove
argument_list|(
name|idx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DefaultHCatRecord
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|list
parameter_list|)
block|{
name|contents
operator|=
name|list
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|(
name|int
name|fieldNum
parameter_list|)
block|{
return|return
name|contents
operator|.
name|get
argument_list|(
name|fieldNum
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getAll
parameter_list|()
block|{
return|return
name|contents
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|int
name|fieldNum
parameter_list|,
name|Object
name|val
parameter_list|)
block|{
name|contents
operator|.
name|set
argument_list|(
name|fieldNum
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|contents
operator|.
name|size
argument_list|()
return|;
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
name|contents
operator|.
name|clear
argument_list|()
expr_stmt|;
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
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
name|len
condition|;
name|i
operator|++
control|)
block|{
name|contents
operator|.
name|add
argument_list|(
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|int
name|sz
init|=
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|sz
argument_list|)
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
name|sz
condition|;
name|i
operator|++
control|)
block|{
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|out
argument_list|,
name|contents
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hash
init|=
literal|1
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|contents
control|)
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|hash
operator|=
literal|31
operator|*
name|hash
operator|+
name|o
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|hash
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
for|for
control|(
name|Object
name|o
range|:
name|contents
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|o
operator|+
literal|"\t"
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
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
name|get
argument_list|(
name|recordSchema
operator|.
name|getPosition
argument_list|(
name|fieldName
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|HCatSchema
name|recordSchema
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|HCatException
block|{
name|set
argument_list|(
name|recordSchema
operator|.
name|getPosition
argument_list|(
name|fieldName
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copy
parameter_list|(
name|HCatRecord
name|r
parameter_list|)
throws|throws
name|HCatException
block|{
name|this
operator|.
name|contents
operator|=
name|r
operator|.
name|getAll
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


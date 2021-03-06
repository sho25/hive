begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|impl
package|;
end_package

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
name|io
operator|.
name|ObjectInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutput
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
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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
name|conf
operator|.
name|Configuration
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
name|mapreduce
operator|.
name|InputSplit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|ReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatSplit
import|;
end_import

begin_comment
comment|/**  * This class contains the list of {@link InputSplit}s obtained  * at master node and the configuration.  */
end_comment

begin_class
class|class
name|ReaderContextImpl
implements|implements
name|ReaderContext
implements|,
name|Configurable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|2656468331739574367L
decl_stmt|;
specifier|private
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|ReaderContextImpl
parameter_list|()
block|{
name|this
operator|.
name|splits
operator|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
block|}
name|void
name|setInputSplits
parameter_list|(
specifier|final
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
parameter_list|)
block|{
name|this
operator|.
name|splits
operator|=
name|splits
expr_stmt|;
block|}
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|()
block|{
return|return
name|splits
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|numSplits
parameter_list|()
block|{
return|return
name|splits
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|)
block|{
name|conf
operator|=
name|config
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeExternal
parameter_list|(
name|ObjectOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
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
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|InputSplit
name|split
range|:
name|splits
control|)
block|{
operator|(
operator|(
name|HCatSplit
operator|)
name|split
operator|)
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readExternal
parameter_list|(
name|ObjectInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|conf
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|numOfSplits
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
name|numOfSplits
condition|;
name|i
operator|++
control|)
block|{
name|HCatSplit
name|split
init|=
operator|new
name|HCatSplit
argument_list|()
decl_stmt|;
name|split
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|splits
operator|.
name|add
argument_list|(
name|split
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


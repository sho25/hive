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
name|ql
operator|.
name|exec
operator|.
name|persistence
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractMapJoinTableContainer
implements|implements
name|MapJoinPersistableTableContainer
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|THESHOLD_NAME
init|=
literal|"threshold"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|LOAD_NAME
init|=
literal|"load"
decl_stmt|;
specifier|private
name|String
name|key
decl_stmt|;
comment|/** Creates metadata for implementation classes' ctors from threshold and load factor. */
specifier|protected
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|createConstructorMetaData
parameter_list|(
name|int
name|threshold
parameter_list|,
name|float
name|loadFactor
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|metaData
operator|.
name|put
argument_list|(
name|THESHOLD_NAME
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|threshold
argument_list|)
argument_list|)
expr_stmt|;
name|metaData
operator|.
name|put
argument_list|(
name|LOAD_NAME
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|loadFactor
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|metaData
return|;
block|}
specifier|protected
name|AbstractMapJoinTableContainer
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
parameter_list|)
block|{
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getMetaData
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|metaData
argument_list|)
return|;
block|}
specifier|protected
name|void
name|putMetaData
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|metaData
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setKey
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
block|}
end_class

end_unit


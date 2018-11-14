begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|druid
operator|.
name|json
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonCreator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonProperty
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|ByteBufferInputRowParser
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|InputRow
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|data
operator|.
name|input
operator|.
name|impl
operator|.
name|ParseSpec
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * This class is copied from druid source code  * in order to avoid adding additional dependencies on druid-indexing-service.  */
end_comment

begin_class
specifier|public
class|class
name|AvroStreamInputRowParser
implements|implements
name|ByteBufferInputRowParser
block|{
specifier|private
specifier|final
name|ParseSpec
name|parseSpec
decl_stmt|;
specifier|private
specifier|final
name|AvroBytesDecoder
name|avroBytesDecoder
decl_stmt|;
annotation|@
name|JsonCreator
specifier|public
name|AvroStreamInputRowParser
parameter_list|(
annotation|@
name|JsonProperty
argument_list|(
literal|"parseSpec"
argument_list|)
name|ParseSpec
name|parseSpec
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"avroBytesDecoder"
argument_list|)
name|AvroBytesDecoder
name|avroBytesDecoder
parameter_list|)
block|{
name|this
operator|.
name|parseSpec
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|parseSpec
argument_list|,
literal|"parseSpec"
argument_list|)
expr_stmt|;
name|this
operator|.
name|avroBytesDecoder
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|avroBytesDecoder
argument_list|,
literal|"avroBytesDecoder"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputRow
argument_list|>
name|parseBatch
parameter_list|(
name|ByteBuffer
name|input
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This class is only used for JSON serde"
argument_list|)
throw|;
block|}
annotation|@
name|JsonProperty
annotation|@
name|Override
specifier|public
name|ParseSpec
name|getParseSpec
parameter_list|()
block|{
return|return
name|parseSpec
return|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|AvroBytesDecoder
name|getAvroBytesDecoder
parameter_list|()
block|{
return|return
name|avroBytesDecoder
return|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBufferInputRowParser
name|withParseSpec
parameter_list|(
name|ParseSpec
name|parseSpec
parameter_list|)
block|{
return|return
operator|new
name|AvroStreamInputRowParser
argument_list|(
name|parseSpec
argument_list|,
name|avroBytesDecoder
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|AvroStreamInputRowParser
name|that
init|=
operator|(
name|AvroStreamInputRowParser
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|parseSpec
argument_list|,
name|that
operator|.
name|parseSpec
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|avroBytesDecoder
argument_list|,
name|that
operator|.
name|avroBytesDecoder
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|parseSpec
argument_list|,
name|avroBytesDecoder
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|JsonIgnore
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
name|DimensionsSpec
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
name|TimestampSpec
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|java
operator|.
name|util
operator|.
name|common
operator|.
name|parsers
operator|.
name|JSONPathSpec
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|java
operator|.
name|util
operator|.
name|common
operator|.
name|parsers
operator|.
name|Parser
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
name|AvroParseSpec
extends|extends
name|ParseSpec
block|{
annotation|@
name|JsonIgnore
specifier|private
specifier|final
name|JSONPathSpec
name|flattenSpec
decl_stmt|;
annotation|@
name|JsonCreator
specifier|public
name|AvroParseSpec
parameter_list|(
annotation|@
name|JsonProperty
argument_list|(
literal|"timestampSpec"
argument_list|)
name|TimestampSpec
name|timestampSpec
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"dimensionsSpec"
argument_list|)
name|DimensionsSpec
name|dimensionsSpec
parameter_list|,
annotation|@
name|JsonProperty
argument_list|(
literal|"flattenSpec"
argument_list|)
name|JSONPathSpec
name|flattenSpec
parameter_list|)
block|{
name|super
argument_list|(
name|timestampSpec
operator|!=
literal|null
condition|?
name|timestampSpec
else|:
operator|new
name|TimestampSpec
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|,
name|dimensionsSpec
operator|!=
literal|null
condition|?
name|dimensionsSpec
else|:
operator|new
name|DimensionsSpec
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|flattenSpec
operator|=
name|flattenSpec
operator|!=
literal|null
condition|?
name|flattenSpec
else|:
name|JSONPathSpec
operator|.
name|DEFAULT
expr_stmt|;
block|}
annotation|@
name|JsonProperty
specifier|public
name|JSONPathSpec
name|getFlattenSpec
parameter_list|()
block|{
return|return
name|flattenSpec
return|;
block|}
annotation|@
name|Override
specifier|public
name|Parser
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|makeParser
parameter_list|()
block|{
comment|// makeParser is only used by StringInputRowParser, which cannot parse avro anyway.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"makeParser not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|ParseSpec
name|withTimestampSpec
parameter_list|(
name|TimestampSpec
name|spec
parameter_list|)
block|{
return|return
operator|new
name|AvroParseSpec
argument_list|(
name|spec
argument_list|,
name|getDimensionsSpec
argument_list|()
argument_list|,
name|flattenSpec
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ParseSpec
name|withDimensionsSpec
parameter_list|(
name|DimensionsSpec
name|spec
parameter_list|)
block|{
return|return
operator|new
name|AvroParseSpec
argument_list|(
name|getTimestampSpec
argument_list|()
argument_list|,
name|spec
argument_list|,
name|flattenSpec
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
if|if
condition|(
operator|!
name|super
operator|.
name|equals
argument_list|(
name|o
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|AvroParseSpec
name|that
init|=
operator|(
name|AvroParseSpec
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|flattenSpec
argument_list|,
name|that
operator|.
name|flattenSpec
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
name|super
operator|.
name|hashCode
argument_list|()
argument_list|,
name|flattenSpec
argument_list|)
return|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|conf
package|;
end_package

begin_class
specifier|public
class|class
name|RangeValidator
implements|implements
name|Validator
block|{
specifier|private
specifier|final
name|TYPE
name|type
decl_stmt|;
specifier|private
specifier|final
name|Object
name|lower
decl_stmt|,
name|upper
decl_stmt|;
specifier|public
name|RangeValidator
parameter_list|(
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
block|{
name|this
operator|.
name|lower
operator|=
name|lower
expr_stmt|;
name|this
operator|.
name|upper
operator|=
name|upper
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|TYPE
operator|.
name|valueOf
argument_list|(
name|lower
argument_list|,
name|upper
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|validate
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
operator|||
operator|!
name|type
operator|.
name|inRange
argument_list|(
name|value
operator|.
name|trim
argument_list|()
argument_list|,
name|lower
argument_list|,
name|upper
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid value  "
operator|+
name|value
operator|+
literal|", which should be in between "
operator|+
name|lower
operator|+
literal|" and "
operator|+
name|upper
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


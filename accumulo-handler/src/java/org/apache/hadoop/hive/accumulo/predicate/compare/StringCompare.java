begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
operator|.
name|predicate
operator|.
name|compare
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Set of comparison operations over a string constant. Used for Hive predicates involving string  * comparison.  *  * Used by {@link org.apache.hadoop.hive.accumulo.predicate.PrimitiveComparisonFilter}  */
end_comment

begin_class
specifier|public
class|class
name|StringCompare
implements|implements
name|PrimitiveComparison
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|StringCompare
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|constant
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|byte
index|[]
name|constant
parameter_list|)
block|{
name|this
operator|.
name|constant
operator|=
name|serialize
argument_list|(
name|constant
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|equals
argument_list|(
name|constant
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isNotEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
operator|!
name|isEqual
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|greaterThanOrEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|>=
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|greaterThan
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|>
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|lessThanOrEqual
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|<=
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|lessThan
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
name|serialize
argument_list|(
name|value
argument_list|)
operator|.
name|compareTo
argument_list|(
name|constant
argument_list|)
operator|<
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|like
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|String
name|temp
init|=
operator|new
name|String
argument_list|(
name|value
argument_list|)
operator|.
name|replaceAll
argument_list|(
literal|"%"
argument_list|,
literal|"[\\\\\\w]+?"
argument_list|)
decl_stmt|;
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|temp
argument_list|)
decl_stmt|;
name|boolean
name|match
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|constant
argument_list|)
operator|.
name|matches
argument_list|()
decl_stmt|;
return|return
name|match
return|;
block|}
specifier|public
name|String
name|serialize
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
operator|new
name|String
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
end_class

end_unit


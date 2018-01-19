begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|spark
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
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|storage
operator|.
name|StorageLevel
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|CacheTran
parameter_list|<
name|KI
extends|extends
name|WritableComparable
parameter_list|,
name|VI
parameter_list|,
name|KO
extends|extends
name|WritableComparable
parameter_list|,
name|VO
parameter_list|>
implements|implements
name|SparkTran
argument_list|<
name|KI
argument_list|,
name|VI
argument_list|,
name|KO
argument_list|,
name|VO
argument_list|>
block|{
comment|// whether to cache current RDD.
specifier|private
name|boolean
name|caching
init|=
literal|false
decl_stmt|;
specifier|private
name|JavaPairRDD
argument_list|<
name|KO
argument_list|,
name|VO
argument_list|>
name|cachedRDD
decl_stmt|;
specifier|protected
name|CacheTran
parameter_list|(
name|boolean
name|cache
parameter_list|)
block|{
name|this
operator|.
name|caching
operator|=
name|cache
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|JavaPairRDD
argument_list|<
name|KO
argument_list|,
name|VO
argument_list|>
name|transform
parameter_list|(
name|JavaPairRDD
argument_list|<
name|KI
argument_list|,
name|VI
argument_list|>
name|input
parameter_list|)
block|{
if|if
condition|(
name|caching
condition|)
block|{
if|if
condition|(
name|cachedRDD
operator|==
literal|null
condition|)
block|{
name|cachedRDD
operator|=
name|doTransform
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|cachedRDD
operator|.
name|persist
argument_list|(
name|StorageLevel
operator|.
name|MEMORY_AND_DISK
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|cachedRDD
return|;
block|}
else|else
block|{
return|return
name|doTransform
argument_list|(
name|input
argument_list|)
return|;
block|}
block|}
specifier|public
name|Boolean
name|isCacheEnable
parameter_list|()
block|{
return|return
name|caching
return|;
block|}
specifier|protected
specifier|abstract
name|JavaPairRDD
argument_list|<
name|KO
argument_list|,
name|VO
argument_list|>
name|doTransform
parameter_list|(
name|JavaPairRDD
argument_list|<
name|KI
argument_list|,
name|VI
argument_list|>
name|input
parameter_list|)
function_decl|;
block|}
end_class

end_unit


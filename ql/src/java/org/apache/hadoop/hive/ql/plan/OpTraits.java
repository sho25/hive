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
name|plan
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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|BucketingVersion
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

begin_class
specifier|public
class|class
name|OpTraits
block|{
specifier|private
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bucketColNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|sortColNames
decl_stmt|;
specifier|private
name|int
name|numBuckets
decl_stmt|;
specifier|private
name|int
name|numReduceSinks
decl_stmt|;
specifier|private
name|BucketingVersion
name|bucketingVersion
decl_stmt|;
specifier|public
name|OpTraits
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bucketColNames
parameter_list|,
name|int
name|numBuckets
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|sortColNames
parameter_list|,
name|int
name|numReduceSinks
parameter_list|,
name|BucketingVersion
name|bucketingVersion
parameter_list|)
block|{
name|this
operator|.
name|bucketColNames
operator|=
name|bucketColNames
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
name|this
operator|.
name|sortColNames
operator|=
name|sortColNames
expr_stmt|;
name|this
operator|.
name|numReduceSinks
operator|=
name|numReduceSinks
expr_stmt|;
name|this
operator|.
name|bucketingVersion
operator|=
name|bucketingVersion
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getBucketColNames
parameter_list|()
block|{
return|return
name|bucketColNames
return|;
block|}
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
specifier|public
name|void
name|setBucketColNames
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|bucketColNames
parameter_list|)
block|{
name|this
operator|.
name|bucketColNames
operator|=
name|bucketColNames
expr_stmt|;
block|}
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
specifier|public
name|void
name|setSortColNames
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|sortColNames
parameter_list|)
block|{
name|this
operator|.
name|sortColNames
operator|=
name|sortColNames
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortColNames
return|;
block|}
specifier|public
name|void
name|setNumReduceSinks
parameter_list|(
name|int
name|numReduceSinks
parameter_list|)
block|{
name|this
operator|.
name|numReduceSinks
operator|=
name|numReduceSinks
expr_stmt|;
block|}
specifier|public
name|int
name|getNumReduceSinks
parameter_list|()
block|{
return|return
name|this
operator|.
name|numReduceSinks
return|;
block|}
specifier|public
name|void
name|setBucketingVersion
parameter_list|(
name|BucketingVersion
name|bucketingVersion
parameter_list|)
block|{
name|this
operator|.
name|bucketingVersion
operator|=
name|bucketingVersion
expr_stmt|;
block|}
specifier|public
name|BucketingVersion
name|getBucketingVersion
parameter_list|()
block|{
return|return
name|bucketingVersion
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"{ bucket column names: "
operator|+
name|bucketColNames
operator|+
literal|"; sort column names: "
operator|+
name|sortColNames
operator|+
literal|"; bucket count: "
operator|+
name|numBuckets
operator|+
literal|" }"
return|;
block|}
block|}
end_class

end_unit


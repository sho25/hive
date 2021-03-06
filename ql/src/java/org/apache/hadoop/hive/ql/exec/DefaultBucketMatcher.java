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
package|;
end_package

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
name|LinkedHashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|fs
operator|.
name|Path
import|;
end_import

begin_class
specifier|public
class|class
name|DefaultBucketMatcher
implements|implements
name|BucketMatcher
block|{
specifier|protected
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|//MAPPING: bucket_file_name_in_big_table->{alias_table->corresonding_bucket_file_names}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|aliasBucketMapping
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|bucketFileNameMapping
decl_stmt|;
specifier|public
name|DefaultBucketMatcher
parameter_list|()
block|{
name|bucketFileNameMapping
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getAliasBucketFiles
parameter_list|(
name|String
name|refTableInputFile
parameter_list|,
name|String
name|refTableAlias
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|pathStr
init|=
name|aliasBucketMapping
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|get
argument_list|(
name|refTableInputFile
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|pathStr
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|p
range|:
name|pathStr
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Loading file "
operator|+
name|p
operator|+
literal|" for "
operator|+
name|alias
operator|+
literal|". ("
operator|+
name|refTableInputFile
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|paths
return|;
block|}
specifier|public
name|void
name|setAliasBucketFileNameMapping
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|aliasBucketFileNameMapping
parameter_list|)
block|{
name|this
operator|.
name|aliasBucketMapping
operator|=
name|aliasBucketFileNameMapping
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|getBucketFileNameMapping
parameter_list|()
block|{
return|return
name|bucketFileNameMapping
return|;
block|}
specifier|public
name|void
name|setBucketFileNameMapping
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|bucketFileNameMapping
parameter_list|)
block|{
name|this
operator|.
name|bucketFileNameMapping
operator|=
name|bucketFileNameMapping
expr_stmt|;
block|}
block|}
end_class

end_unit


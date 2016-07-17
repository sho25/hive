begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|stats
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
name|Arrays
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Task
import|;
end_import

begin_class
specifier|public
class|class
name|StatsCollectionContext
block|{
specifier|private
specifier|final
name|Configuration
name|hiveConf
decl_stmt|;
specifier|private
name|Task
name|task
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|statsTmpDirs
decl_stmt|;
specifier|private
name|int
name|indexForTezUnion
decl_stmt|;
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getStatsTmpDirs
parameter_list|()
block|{
return|return
name|statsTmpDirs
return|;
block|}
specifier|public
name|void
name|setStatsTmpDirs
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|statsTmpDirs
parameter_list|)
block|{
name|this
operator|.
name|statsTmpDirs
operator|=
name|statsTmpDirs
expr_stmt|;
block|}
specifier|public
name|void
name|setStatsTmpDir
parameter_list|(
name|String
name|statsTmpDir
parameter_list|)
block|{
name|this
operator|.
name|statsTmpDirs
operator|=
name|statsTmpDir
operator|==
literal|null
condition|?
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
else|:
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
name|statsTmpDir
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StatsCollectionContext
parameter_list|(
name|Configuration
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getHiveConf
parameter_list|()
block|{
return|return
name|hiveConf
return|;
block|}
specifier|public
name|Task
name|getTask
parameter_list|()
block|{
return|return
name|task
return|;
block|}
specifier|public
name|void
name|setTask
parameter_list|(
name|Task
name|task
parameter_list|)
block|{
name|this
operator|.
name|task
operator|=
name|task
expr_stmt|;
block|}
specifier|public
name|int
name|getIndexForTezUnion
parameter_list|()
block|{
return|return
name|indexForTezUnion
return|;
block|}
specifier|public
name|void
name|setIndexForTezUnion
parameter_list|(
name|int
name|indexForTezUnion
parameter_list|)
block|{
name|this
operator|.
name|indexForTezUnion
operator|=
name|indexForTezUnion
expr_stmt|;
block|}
block|}
end_class

end_unit


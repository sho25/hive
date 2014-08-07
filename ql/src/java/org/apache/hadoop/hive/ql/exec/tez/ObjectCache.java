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
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|common
operator|.
name|objectregistry
operator|.
name|ObjectRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|common
operator|.
name|objectregistry
operator|.
name|ObjectRegistryImpl
import|;
end_import

begin_comment
comment|/**  * ObjectCache. Tez implementation based on the tez object registry.  *  */
end_comment

begin_class
specifier|public
class|class
name|ObjectCache
implements|implements
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
name|ObjectCache
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ObjectCache
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ObjectRegistry
name|registry
init|=
operator|new
name|ObjectRegistryImpl
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|cache
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding "
operator|+
name|key
operator|+
literal|" to cache with value "
operator|+
name|value
argument_list|)
expr_stmt|;
name|registry
operator|.
name|cacheForVertex
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
name|Object
name|retrieve
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|Object
name|o
init|=
name|registry
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|key
operator|+
literal|" in cache with value: "
operator|+
name|o
argument_list|)
expr_stmt|;
block|}
return|return
name|o
return|;
block|}
block|}
end_class

end_unit


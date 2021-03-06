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
name|lockmgr
operator|.
name|zookeeper
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
name|lang3
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|lockmgr
operator|.
name|HiveLock
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
name|lockmgr
operator|.
name|HiveLockMode
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
name|lockmgr
operator|.
name|HiveLockObject
import|;
end_import

begin_class
specifier|public
class|class
name|ZooKeeperHiveLock
extends|extends
name|HiveLock
block|{
specifier|private
name|String
name|path
decl_stmt|;
specifier|private
name|HiveLockObject
name|obj
decl_stmt|;
specifier|private
name|HiveLockMode
name|mode
decl_stmt|;
specifier|public
name|ZooKeeperHiveLock
parameter_list|(
name|String
name|path
parameter_list|,
name|HiveLockObject
name|obj
parameter_list|,
name|HiveLockMode
name|mode
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|obj
operator|=
name|obj
expr_stmt|;
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
specifier|public
name|String
name|getPath
parameter_list|()
block|{
return|return
name|path
return|;
block|}
specifier|public
name|void
name|setPath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLockObject
name|getHiveLockObject
parameter_list|()
block|{
return|return
name|obj
return|;
block|}
specifier|public
name|void
name|setHiveLockObject
parameter_list|(
name|HiveLockObject
name|obj
parameter_list|)
block|{
name|this
operator|.
name|obj
operator|=
name|obj
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HiveLockMode
name|getHiveLockMode
parameter_list|()
block|{
return|return
name|mode
return|;
block|}
specifier|public
name|void
name|setHiveLockMode
parameter_list|(
name|HiveLockMode
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ZooKeeperHiveLock
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ZooKeeperHiveLock
name|zLock
init|=
operator|(
name|ZooKeeperHiveLock
operator|)
name|o
decl_stmt|;
return|return
name|path
operator|.
name|equals
argument_list|(
name|zLock
operator|.
name|getPath
argument_list|()
argument_list|)
operator|&&
name|obj
operator|.
name|equals
argument_list|(
name|zLock
operator|.
name|getHiveLockObject
argument_list|()
argument_list|)
operator|&&
name|mode
operator|==
name|zLock
operator|.
name|getHiveLockMode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|HashCodeBuilder
name|builder
init|=
operator|new
name|HashCodeBuilder
argument_list|()
decl_stmt|;
name|boolean
name|pathPresent
init|=
name|path
operator|!=
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|pathPresent
argument_list|)
expr_stmt|;
if|if
condition|(
name|pathPresent
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|path
operator|.
name|toCharArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boolean
name|lockObjectPresent
init|=
name|obj
operator|!=
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|lockObjectPresent
argument_list|)
expr_stmt|;
if|if
condition|(
name|lockObjectPresent
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|obj
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|boolean
name|modePresent
init|=
name|mode
operator|!=
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|modePresent
argument_list|)
expr_stmt|;
if|if
condition|(
name|modePresent
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|mode
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toHashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit


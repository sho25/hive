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
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|Database
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
import|;
end_import

begin_comment
comment|/**  * This class encapsulates the information on the partition and tables that are  * read by the query.  */
end_comment

begin_class
specifier|public
class|class
name|ReadEntity
extends|extends
name|Entity
implements|implements
name|Serializable
block|{
comment|// Consider a query like: select * from V, where the view V is defined as:
comment|// select * from T
comment|// The inputs will contain V and T (parent: V)
comment|// T will be marked as an indirect entity using isDirect flag.
comment|// This will help in distinguishing from the case where T is a direct dependency
comment|// For example in the case of "select * from V join T ..." T would be direct dependency
specifier|private
name|boolean
name|isDirect
init|=
literal|true
decl_stmt|;
comment|// For views, the entities can be nested - by default, entities are at the top level
specifier|private
specifier|final
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|parents
init|=
operator|new
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * For serialization only.    */
specifier|public
name|ReadEntity
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor for a database.    */
specifier|public
name|ReadEntity
parameter_list|(
name|Database
name|database
parameter_list|)
block|{
name|super
argument_list|(
name|database
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    *    * @param t    *          The Table that the query reads from.    */
specifier|public
name|ReadEntity
parameter_list|(
name|Table
name|t
parameter_list|)
block|{
name|super
argument_list|(
name|t
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|initParent
parameter_list|(
name|ReadEntity
name|parent
parameter_list|)
block|{
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|parents
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ReadEntity
parameter_list|(
name|Table
name|t
parameter_list|,
name|ReadEntity
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|t
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|initParent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReadEntity
parameter_list|(
name|Table
name|t
parameter_list|,
name|ReadEntity
name|parent
parameter_list|,
name|boolean
name|isDirect
parameter_list|)
block|{
name|this
argument_list|(
name|t
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|isDirect
operator|=
name|isDirect
expr_stmt|;
block|}
comment|/**    * Constructor given a partition.    *    * @param p    *          The partition that the query reads from.    */
specifier|public
name|ReadEntity
parameter_list|(
name|Partition
name|p
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReadEntity
parameter_list|(
name|Partition
name|p
parameter_list|,
name|ReadEntity
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|initParent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReadEntity
parameter_list|(
name|Partition
name|p
parameter_list|,
name|ReadEntity
name|parent
parameter_list|,
name|boolean
name|isDirect
parameter_list|)
block|{
name|this
argument_list|(
name|p
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|isDirect
operator|=
name|isDirect
expr_stmt|;
block|}
comment|/**    * Constructor for a file.    *    * @param d    *          The name of the directory that is being written to.    * @param islocal    *          Flag to decide whether this directory is local or in dfs.    */
specifier|public
name|ReadEntity
parameter_list|(
name|Path
name|d
parameter_list|,
name|boolean
name|islocal
parameter_list|)
block|{
name|super
argument_list|(
name|d
operator|.
name|toString
argument_list|()
argument_list|,
name|islocal
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|getParents
parameter_list|()
block|{
return|return
name|parents
return|;
block|}
comment|/**    * Equals function.    */
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
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|ReadEntity
condition|)
block|{
name|ReadEntity
name|ore
init|=
operator|(
name|ReadEntity
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ore
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|boolean
name|isDirect
parameter_list|()
block|{
return|return
name|isDirect
return|;
block|}
specifier|public
name|void
name|setDirect
parameter_list|(
name|boolean
name|isDirect
parameter_list|)
block|{
name|this
operator|.
name|isDirect
operator|=
name|isDirect
expr_stmt|;
block|}
block|}
end_class

end_unit


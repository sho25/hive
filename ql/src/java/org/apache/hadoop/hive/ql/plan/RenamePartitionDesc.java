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
name|plan
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
name|LinkedHashMap
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

begin_comment
comment|/**  * Contains the information needed to rename a partition.  */
end_comment

begin_class
specifier|public
class|class
name|RenamePartitionDesc
extends|extends
name|DDLDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|String
name|tableName
decl_stmt|;
name|String
name|location
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|oldPartSpec
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newPartSpec
decl_stmt|;
comment|/**    * For serialization only.    */
specifier|public
name|RenamePartitionDesc
parameter_list|()
block|{   }
comment|/**    * @param dbName    *          database to add to.    * @param tableName    *          table to add to.    * @param oldPartSpec    *          old partition specification.    * @param newPartSpec    *          new partition specification.    */
specifier|public
name|RenamePartitionDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|oldPartSpec
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newPartSpec
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|oldPartSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|oldPartSpec
argument_list|)
expr_stmt|;
name|this
operator|.
name|newPartSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|newPartSpec
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the table we're going to add the partitions to.    */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * @param tableName    *          the table we're going to add the partitions to.    */
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
comment|/**    * @return location of partition in relation to table    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
comment|/**    * @param location    *          location of partition in relation to table    */
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
comment|/**    * @return old partition specification.    */
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getOldPartSpec
parameter_list|()
block|{
return|return
name|oldPartSpec
return|;
block|}
comment|/**    * @param partSpec    *          partition specification    */
specifier|public
name|void
name|setOldPartSpec
parameter_list|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|oldPartSpec
operator|=
name|partSpec
expr_stmt|;
block|}
comment|/**    * @return new partition specification.    */
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getNewPartSpec
parameter_list|()
block|{
return|return
name|newPartSpec
return|;
block|}
comment|/**    * @param partSpec    *          partition specification    */
specifier|public
name|void
name|setNewPartSpec
parameter_list|(
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
block|{
name|this
operator|.
name|newPartSpec
operator|=
name|partSpec
expr_stmt|;
block|}
block|}
end_class

end_unit


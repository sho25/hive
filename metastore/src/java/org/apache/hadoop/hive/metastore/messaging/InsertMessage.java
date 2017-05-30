begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|messaging
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
name|metastore
operator|.
name|api
operator|.
name|Table
import|;
end_import

begin_comment
comment|/**  * HCat message sent when an insert is done to a table or partition.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|InsertMessage
extends|extends
name|EventMessage
block|{
specifier|protected
name|InsertMessage
parameter_list|()
block|{
name|super
argument_list|(
name|EventType
operator|.
name|INSERT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Getter for the name of the table being insert into.    * @return Table-name (String).    */
specifier|public
specifier|abstract
name|String
name|getTable
parameter_list|()
function_decl|;
comment|/**    * Getter for the replace flag being insert into/overwrite    * @return Replace flag to represent INSERT INTO or INSERT OVERWRITE (Boolean).    */
specifier|public
specifier|abstract
name|boolean
name|isReplace
parameter_list|()
function_decl|;
comment|/**    * Get list of file name and checksum created as a result of this DML operation    *    * @return The iterable of files    */
specifier|public
specifier|abstract
name|Iterable
argument_list|<
name|String
argument_list|>
name|getFiles
parameter_list|()
function_decl|;
comment|/**    * Get the table object associated with the insert    *    * @return The Json format of Table object    */
specifier|public
specifier|abstract
name|Table
name|getTableObj
parameter_list|()
throws|throws
name|Exception
function_decl|;
comment|/**    * Get the partition object associated with the insert    *    * @return The Json format of Partition object if the table is partitioned else return null.    */
specifier|public
specifier|abstract
name|Partition
name|getPtnObj
parameter_list|()
throws|throws
name|Exception
function_decl|;
annotation|@
name|Override
specifier|public
name|EventMessage
name|checkValid
parameter_list|()
block|{
if|if
condition|(
name|getTable
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table name unset."
argument_list|)
throw|;
return|return
name|super
operator|.
name|checkValid
argument_list|()
return|;
block|}
block|}
end_class

end_unit


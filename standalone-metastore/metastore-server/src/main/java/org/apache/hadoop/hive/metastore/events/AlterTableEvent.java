begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one   * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|events
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|IHMSHandler
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|AlterTableEvent
extends|extends
name|ListenerEvent
block|{
specifier|private
specifier|final
name|Table
name|newTable
decl_stmt|;
specifier|private
specifier|final
name|Table
name|oldTable
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isTruncateOp
decl_stmt|;
specifier|private
name|Long
name|writeId
decl_stmt|;
specifier|public
name|AlterTableEvent
parameter_list|(
name|Table
name|oldTable
parameter_list|,
name|Table
name|newTable
parameter_list|,
name|boolean
name|isTruncateOp
parameter_list|,
name|boolean
name|status
parameter_list|,
name|Long
name|writeId
parameter_list|,
name|IHMSHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
name|status
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|this
operator|.
name|oldTable
operator|=
name|oldTable
expr_stmt|;
name|this
operator|.
name|newTable
operator|=
name|newTable
expr_stmt|;
name|this
operator|.
name|isTruncateOp
operator|=
name|isTruncateOp
expr_stmt|;
name|this
operator|.
name|writeId
operator|=
name|writeId
expr_stmt|;
block|}
comment|/**    * @return the old table    */
specifier|public
name|Table
name|getOldTable
parameter_list|()
block|{
return|return
name|oldTable
return|;
block|}
comment|/**    * @return the new table    */
specifier|public
name|Table
name|getNewTable
parameter_list|()
block|{
return|return
name|newTable
return|;
block|}
comment|/**    * @return the flag for truncate    */
specifier|public
name|boolean
name|getIsTruncateOp
parameter_list|()
block|{
return|return
name|isTruncateOp
return|;
block|}
specifier|public
name|Long
name|getWriteId
parameter_list|()
block|{
return|return
name|writeId
return|;
block|}
block|}
end_class

end_unit


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
name|hive
operator|.
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|GetTableRequest
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
name|HiveMetaStore
operator|.
name|HMSHandler
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
name|InsertEventRequestData
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
name|MetaException
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
name|NoSuchObjectException
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
name|List
import|;
end_import

begin_class
specifier|public
class|class
name|InsertEvent
extends|extends
name|ListenerEvent
block|{
specifier|private
specifier|final
name|Table
name|tableObj
decl_stmt|;
specifier|private
specifier|final
name|Partition
name|ptnObj
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|replace
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|files
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|fileChecksums
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    *    * @param db name of the database the table is in    * @param table name of the table being inserted into    * @param partVals list of partition values, can be null    * @param insertData the inserted files& their checksums    * @param status status of insert, true = success, false = failure    * @param handler handler that is firing the event    */
specifier|public
name|InsertEvent
parameter_list|(
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|,
name|InsertEventRequestData
name|insertData
parameter_list|,
name|boolean
name|status
parameter_list|,
name|HMSHandler
name|handler
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
name|super
argument_list|(
name|status
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|GetTableRequest
name|req
init|=
operator|new
name|GetTableRequest
argument_list|(
name|db
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|req
operator|.
name|setCapabilities
argument_list|(
name|HiveMetaStoreClient
operator|.
name|TEST_VERSION
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableObj
operator|=
name|handler
operator|.
name|get_table_req
argument_list|(
name|req
argument_list|)
operator|.
name|getTable
argument_list|()
expr_stmt|;
if|if
condition|(
name|partVals
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|ptnObj
operator|=
name|handler
operator|.
name|get_partition
argument_list|(
name|db
argument_list|,
name|table
argument_list|,
name|partVals
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|ptnObj
operator|=
literal|null
expr_stmt|;
block|}
comment|// If replace flag is not set by caller, then by default set it to true to maintain backward compatibility
name|this
operator|.
name|replace
operator|=
operator|(
name|insertData
operator|.
name|isSetReplace
argument_list|()
condition|?
name|insertData
operator|.
name|isReplace
argument_list|()
else|:
literal|true
operator|)
expr_stmt|;
name|this
operator|.
name|files
operator|=
name|insertData
operator|.
name|getFilesAdded
argument_list|()
expr_stmt|;
if|if
condition|(
name|insertData
operator|.
name|isSetFilesAddedChecksum
argument_list|()
condition|)
block|{
name|fileChecksums
operator|=
name|insertData
operator|.
name|getFilesAddedChecksum
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @return Table object    */
specifier|public
name|Table
name|getTableObj
parameter_list|()
block|{
return|return
name|tableObj
return|;
block|}
comment|/**    * @return Partition object    */
specifier|public
name|Partition
name|getPartitionObj
parameter_list|()
block|{
return|return
name|ptnObj
return|;
block|}
comment|/**    * @return The replace flag.    */
specifier|public
name|boolean
name|isReplace
parameter_list|()
block|{
return|return
name|replace
return|;
block|}
comment|/**    * Get list of files created as a result of this DML operation    *    * @return list of new files    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getFiles
parameter_list|()
block|{
return|return
name|files
return|;
block|}
comment|/**    * Get a list of file checksums corresponding to the files created (if available)    *    * @return    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getFileChecksums
parameter_list|()
block|{
return|return
name|fileChecksums
return|;
block|}
block|}
end_class

end_unit


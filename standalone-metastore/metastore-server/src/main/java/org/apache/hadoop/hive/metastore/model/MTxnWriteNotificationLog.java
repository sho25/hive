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
name|metastore
operator|.
name|model
package|;
end_package

begin_comment
comment|/**  * MTxnWriteNotificationLog  * DN table for ACID write events.  */
end_comment

begin_class
specifier|public
class|class
name|MTxnWriteNotificationLog
block|{
specifier|private
name|long
name|txnId
decl_stmt|;
specifier|private
name|long
name|writeId
decl_stmt|;
specifier|private
name|int
name|eventTime
decl_stmt|;
specifier|private
name|String
name|database
decl_stmt|;
specifier|private
name|String
name|table
decl_stmt|;
specifier|private
name|String
name|partition
decl_stmt|;
specifier|private
name|String
name|tableObject
decl_stmt|;
specifier|private
name|String
name|partObject
decl_stmt|;
specifier|private
name|String
name|files
decl_stmt|;
specifier|public
name|MTxnWriteNotificationLog
parameter_list|()
block|{   }
specifier|public
name|MTxnWriteNotificationLog
parameter_list|(
name|long
name|txnId
parameter_list|,
name|long
name|writeId
parameter_list|,
name|int
name|eventTime
parameter_list|,
name|String
name|database
parameter_list|,
name|String
name|table
parameter_list|,
name|String
name|partition
parameter_list|,
name|String
name|tableObject
parameter_list|,
name|String
name|partObject
parameter_list|,
name|String
name|files
parameter_list|)
block|{
name|this
operator|.
name|txnId
operator|=
name|txnId
expr_stmt|;
name|this
operator|.
name|writeId
operator|=
name|writeId
expr_stmt|;
name|this
operator|.
name|eventTime
operator|=
name|eventTime
expr_stmt|;
name|this
operator|.
name|database
operator|=
name|database
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
name|this
operator|.
name|tableObject
operator|=
name|tableObject
expr_stmt|;
name|this
operator|.
name|partObject
operator|=
name|partObject
expr_stmt|;
name|this
operator|.
name|files
operator|=
name|files
expr_stmt|;
block|}
specifier|public
name|long
name|getTxnId
parameter_list|()
block|{
return|return
name|txnId
return|;
block|}
specifier|public
name|void
name|setTxnId
parameter_list|(
name|long
name|txnId
parameter_list|)
block|{
name|this
operator|.
name|txnId
operator|=
name|txnId
expr_stmt|;
block|}
specifier|public
name|long
name|getWriteId
parameter_list|()
block|{
return|return
name|writeId
return|;
block|}
specifier|public
name|void
name|setWriteId
parameter_list|(
name|long
name|writeId
parameter_list|)
block|{
name|this
operator|.
name|writeId
operator|=
name|writeId
expr_stmt|;
block|}
specifier|public
name|int
name|getEventTime
parameter_list|()
block|{
return|return
name|eventTime
return|;
block|}
specifier|public
name|void
name|setEventTime
parameter_list|(
name|int
name|eventTime
parameter_list|)
block|{
name|this
operator|.
name|eventTime
operator|=
name|eventTime
expr_stmt|;
block|}
specifier|public
name|String
name|getDatabase
parameter_list|()
block|{
return|return
name|database
return|;
block|}
specifier|public
name|void
name|setDatabase
parameter_list|(
name|String
name|database
parameter_list|)
block|{
name|this
operator|.
name|database
operator|=
name|database
expr_stmt|;
block|}
specifier|public
name|String
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|String
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|String
name|getPartition
parameter_list|()
block|{
return|return
name|partition
return|;
block|}
specifier|public
name|void
name|setPartition
parameter_list|(
name|String
name|partition
parameter_list|)
block|{
name|this
operator|.
name|partition
operator|=
name|partition
expr_stmt|;
block|}
specifier|public
name|String
name|getTableObject
parameter_list|()
block|{
return|return
name|tableObject
return|;
block|}
specifier|public
name|void
name|setTableObject
parameter_list|(
name|String
name|tableObject
parameter_list|)
block|{
name|this
operator|.
name|tableObject
operator|=
name|tableObject
expr_stmt|;
block|}
specifier|public
name|String
name|getPartObject
parameter_list|()
block|{
return|return
name|partObject
return|;
block|}
specifier|public
name|void
name|setPartObject
parameter_list|(
name|String
name|partObject
parameter_list|)
block|{
name|this
operator|.
name|partObject
operator|=
name|partObject
expr_stmt|;
block|}
specifier|public
name|String
name|getFiles
parameter_list|()
block|{
return|return
name|files
return|;
block|}
specifier|public
name|void
name|setFiles
parameter_list|(
name|String
name|files
parameter_list|)
block|{
name|this
operator|.
name|files
operator|=
name|files
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|hbase
operator|.
name|mapreduce
operator|.
name|TableSplit
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|InputSplit
import|;
end_import

begin_comment
comment|/**  * HBaseSplit augments FileSplit with HBase column mapping.  */
end_comment

begin_class
specifier|public
class|class
name|HBaseSplit
extends|extends
name|FileSplit
implements|implements
name|InputSplit
block|{
specifier|private
specifier|final
name|TableSplit
name|tableSplit
decl_stmt|;
specifier|private
specifier|final
name|InputSplit
name|snapshotSplit
decl_stmt|;
specifier|private
name|boolean
name|isTableSplit
decl_stmt|;
comment|// should be final but Writable
comment|/**    * For Writable    */
specifier|public
name|HBaseSplit
parameter_list|()
block|{
name|super
argument_list|(
operator|(
name|Path
operator|)
literal|null
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|tableSplit
operator|=
operator|new
name|TableSplit
argument_list|()
expr_stmt|;
name|snapshotSplit
operator|=
name|HBaseTableSnapshotInputFormatUtil
operator|.
name|createTableSnapshotRegionSplit
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HBaseSplit
parameter_list|(
name|TableSplit
name|tableSplit
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
block|{
name|super
argument_list|(
name|dummyPath
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableSplit
operator|=
name|tableSplit
expr_stmt|;
name|this
operator|.
name|snapshotSplit
operator|=
name|HBaseTableSnapshotInputFormatUtil
operator|.
name|createTableSnapshotRegionSplit
argument_list|()
expr_stmt|;
name|this
operator|.
name|isTableSplit
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * TODO: use TableSnapshotRegionSplit HBASE-11555 is fixed.    */
specifier|public
name|HBaseSplit
parameter_list|(
name|InputSplit
name|snapshotSplit
parameter_list|,
name|Path
name|dummyPath
parameter_list|)
block|{
name|super
argument_list|(
name|dummyPath
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableSplit
operator|=
operator|new
name|TableSplit
argument_list|()
expr_stmt|;
name|this
operator|.
name|snapshotSplit
operator|=
name|snapshotSplit
expr_stmt|;
name|this
operator|.
name|isTableSplit
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|TableSplit
name|getTableSplit
parameter_list|()
block|{
assert|assert
name|isTableSplit
assert|;
return|return
name|this
operator|.
name|tableSplit
return|;
block|}
specifier|public
name|InputSplit
name|getSnapshotSplit
parameter_list|()
block|{
assert|assert
operator|!
name|isTableSplit
assert|;
return|return
name|this
operator|.
name|snapshotSplit
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
operator|(
name|isTableSplit
condition|?
name|tableSplit
else|:
name|snapshotSplit
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|isTableSplit
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|isTableSplit
condition|)
block|{
name|tableSplit
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|snapshotSplit
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isTableSplit
argument_list|)
expr_stmt|;
if|if
condition|(
name|isTableSplit
condition|)
block|{
name|tableSplit
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|snapshotSplit
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
name|long
name|val
init|=
literal|0
decl_stmt|;
try|try
block|{
name|val
operator|=
name|isTableSplit
condition|?
name|tableSplit
operator|.
name|getLength
argument_list|()
else|:
name|snapshotSplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
return|return
name|val
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|isTableSplit
condition|?
name|tableSplit
operator|.
name|getLocations
argument_list|()
else|:
name|snapshotSplit
operator|.
name|getLocations
argument_list|()
return|;
block|}
block|}
end_class

end_unit


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
name|mapred
operator|.
name|TableSplit
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
name|String
name|hbaseColumnMapping
decl_stmt|;
specifier|private
name|TableSplit
name|split
decl_stmt|;
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
name|hbaseColumnMapping
operator|=
literal|""
expr_stmt|;
name|split
operator|=
operator|new
name|TableSplit
argument_list|()
expr_stmt|;
block|}
specifier|public
name|HBaseSplit
parameter_list|(
name|TableSplit
name|split
parameter_list|,
name|String
name|columnsMapping
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
name|split
operator|=
name|split
expr_stmt|;
name|hbaseColumnMapping
operator|=
name|columnsMapping
expr_stmt|;
block|}
specifier|public
name|TableSplit
name|getSplit
parameter_list|()
block|{
return|return
name|this
operator|.
name|split
return|;
block|}
specifier|public
name|String
name|getColumnsMapping
parameter_list|()
block|{
return|return
name|this
operator|.
name|hbaseColumnMapping
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
name|hbaseColumnMapping
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|split
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TableSplit "
operator|+
name|split
operator|+
literal|" : "
operator|+
name|hbaseColumnMapping
return|;
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
name|writeUTF
argument_list|(
name|hbaseColumnMapping
argument_list|)
expr_stmt|;
name|split
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
return|return
name|split
operator|.
name|getLength
argument_list|()
return|;
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
name|split
operator|.
name|getLocations
argument_list|()
return|;
block|}
block|}
end_class

end_unit


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
name|index
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
name|ql
operator|.
name|io
operator|.
name|HiveInputFormat
operator|.
name|HiveInputSplit
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
name|mapred
operator|.
name|SequenceFileInputFormat
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|MockHiveInputSplits
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|HOSTS
init|=
block|{}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INPUT_FORMAT_CLASS_NAME
init|=
name|SequenceFileInputFormat
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
decl_stmt|;
specifier|private
name|MockHiveInputSplits
parameter_list|()
block|{   }
specifier|public
specifier|static
name|HiveInputSplit
name|createMockSplit
parameter_list|(
name|String
name|pathString
parameter_list|,
name|long
name|start
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|InputSplit
name|inputSplit
init|=
operator|new
name|FileSplit
argument_list|(
operator|new
name|Path
argument_list|(
name|pathString
argument_list|)
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|HOSTS
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveInputSplit
argument_list|(
name|inputSplit
argument_list|,
name|INPUT_FORMAT_CLASS_NAME
argument_list|)
return|;
block|}
block|}
end_class

end_unit


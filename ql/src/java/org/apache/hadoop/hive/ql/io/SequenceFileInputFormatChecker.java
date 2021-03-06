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
name|io
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|List
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
name|FileStatus
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
name|FileSystem
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
name|conf
operator|.
name|HiveConf
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
name|io
operator|.
name|IOUtils
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
name|io
operator|.
name|SequenceFile
import|;
end_import

begin_comment
comment|/**  * SequenceFileInputFormatChecker.  *  */
end_comment

begin_class
specifier|public
class|class
name|SequenceFileInputFormatChecker
implements|implements
name|InputFormatChecker
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|validateInput
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|<=
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|fileId
init|=
literal|0
init|;
name|fileId
operator|<
name|files
operator|.
name|size
argument_list|()
condition|;
name|fileId
operator|++
control|)
block|{
name|SequenceFile
operator|.
name|Reader
name|reader
init|=
literal|null
decl_stmt|;
try|try
block|{
name|reader
operator|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|files
operator|.
name|get
argument_list|(
name|fileId
argument_list|)
operator|.
name|getPath
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|reader
operator|=
literal|null
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit


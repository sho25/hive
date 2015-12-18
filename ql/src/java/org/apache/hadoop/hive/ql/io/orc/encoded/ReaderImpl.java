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
name|io
operator|.
name|orc
operator|.
name|encoded
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
name|common
operator|.
name|io
operator|.
name|DataCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|DataReader
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
name|orc
operator|.
name|OrcFile
operator|.
name|ReaderOptions
import|;
end_import

begin_class
class|class
name|ReaderImpl
extends|extends
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
name|orc
operator|.
name|ReaderImpl
implements|implements
name|Reader
block|{
specifier|public
name|ReaderImpl
parameter_list|(
name|Path
name|path
parameter_list|,
name|ReaderOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|path
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|EncodedReader
name|encodedReader
parameter_list|(
name|Long
name|fileId
parameter_list|,
name|DataCache
name|dataCache
parameter_list|,
name|DataReader
name|dataReader
parameter_list|,
name|PoolFactory
name|pf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|EncodedReaderImpl
argument_list|(
name|fileId
argument_list|,
name|types
argument_list|,
name|codec
argument_list|,
name|bufferSize
argument_list|,
name|rowIndexStride
argument_list|,
name|dataCache
argument_list|,
name|dataReader
argument_list|,
name|pf
argument_list|)
return|;
block|}
block|}
end_class

end_unit


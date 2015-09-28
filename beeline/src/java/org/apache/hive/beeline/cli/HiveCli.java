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
name|hive
operator|.
name|beeline
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
operator|.
name|BeeLine
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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_class
specifier|public
class|class
name|HiveCli
block|{
specifier|private
name|BeeLine
name|beeLine
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|status
init|=
operator|new
name|HiveCli
argument_list|()
operator|.
name|runWithArgs
argument_list|(
name|args
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|runWithArgs
parameter_list|(
name|String
index|[]
name|cmd
parameter_list|,
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|IOException
block|{
name|beeLine
operator|=
operator|new
name|BeeLine
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|beeLine
operator|.
name|begin
argument_list|(
name|cmd
argument_list|,
name|inputStream
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|common
operator|.
name|io
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Base64
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_class
specifier|public
class|class
name|SortAndDigestPrintStream
extends|extends
name|SortPrintStream
block|{
specifier|private
specifier|final
name|MessageDigest
name|digest
decl_stmt|;
specifier|public
name|SortAndDigestPrintStream
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|String
name|encoding
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|out
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
name|this
operator|.
name|digest
operator|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processFinal
parameter_list|()
block|{
while|while
condition|(
operator|!
name|outputs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|row
init|=
name|outputs
operator|.
name|removeFirst
argument_list|()
decl_stmt|;
name|digest
operator|.
name|update
argument_list|(
name|row
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|printDirect
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|printDirect
argument_list|(
operator|new
name|String
argument_list|(
name|Base64
operator|.
name|encodeBase64
argument_list|(
name|digest
operator|.
name|digest
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|digest
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


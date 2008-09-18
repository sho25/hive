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
name|serde2
package|;
end_package

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TReflectionUtils
block|{
specifier|public
specifier|static
specifier|final
name|String
name|thriftReaderFname
init|=
literal|"read"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|thriftWriterFname
init|=
literal|"write"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|thriftRWParams
decl_stmt|;
static|static
block|{
try|try
block|{
name|thriftRWParams
operator|=
operator|new
name|Class
index|[]
block|{
name|Class
operator|.
name|forName
argument_list|(
literal|"com.facebook.thrift.protocol.TProtocol"
argument_list|)
block|}
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|TProtocolFactory
name|getProtocolFactoryByName
parameter_list|(
name|String
name|protocolName
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|protoClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|protocolName
operator|+
literal|"$Factory"
argument_list|)
decl_stmt|;
return|return
operator|(
operator|(
name|TProtocolFactory
operator|)
name|protoClass
operator|.
name|newInstance
argument_list|()
operator|)
return|;
block|}
block|}
end_class

end_unit


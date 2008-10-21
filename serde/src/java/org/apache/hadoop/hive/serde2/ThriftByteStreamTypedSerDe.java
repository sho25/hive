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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|conf
operator|.
name|Configuration
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|Writable
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TBase
import|;
end_import

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
name|TProtocol
import|;
end_import

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

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TIOStreamTransport
import|;
end_import

begin_class
specifier|public
class|class
name|ThriftByteStreamTypedSerDe
extends|extends
name|ByteStreamTypedSerDe
block|{
specifier|protected
name|TIOStreamTransport
name|outTransport
decl_stmt|,
name|inTransport
decl_stmt|;
specifier|protected
name|TProtocol
name|outProtocol
decl_stmt|,
name|inProtocol
decl_stmt|;
specifier|private
name|void
name|init
parameter_list|(
name|TProtocolFactory
name|inFactory
parameter_list|,
name|TProtocolFactory
name|outFactory
parameter_list|)
throws|throws
name|Exception
block|{
name|outTransport
operator|=
operator|new
name|TIOStreamTransport
argument_list|(
name|bos
argument_list|)
expr_stmt|;
name|inTransport
operator|=
operator|new
name|TIOStreamTransport
argument_list|(
name|bis
argument_list|)
expr_stmt|;
name|outProtocol
operator|=
name|outFactory
operator|.
name|getProtocol
argument_list|(
name|outTransport
argument_list|)
expr_stmt|;
name|inProtocol
operator|=
name|inFactory
operator|.
name|getProtocol
argument_list|(
name|inTransport
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"ThriftByteStreamTypedSerDe is still semi-abstract"
argument_list|)
throw|;
block|}
specifier|public
name|ThriftByteStreamTypedSerDe
parameter_list|(
name|Type
name|objectType
parameter_list|,
name|TProtocolFactory
name|inFactory
parameter_list|,
name|TProtocolFactory
name|outFactory
parameter_list|)
throws|throws
name|SerDeException
block|{
name|super
argument_list|(
name|objectType
argument_list|)
expr_stmt|;
try|try
block|{
name|init
argument_list|(
name|inFactory
argument_list|,
name|outFactory
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
name|getObjectInspectorOptions
parameter_list|()
block|{
return|return
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|THRIFT
return|;
block|}
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|field
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Object
name|obj
init|=
name|super
operator|.
name|deserialize
argument_list|(
name|field
argument_list|)
decl_stmt|;
try|try
block|{
operator|(
operator|(
name|TBase
operator|)
name|obj
operator|)
operator|.
name|read
argument_list|(
name|inProtocol
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|obj
return|;
block|}
block|}
end_class

end_unit


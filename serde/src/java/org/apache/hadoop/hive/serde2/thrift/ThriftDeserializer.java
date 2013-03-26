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
operator|.
name|thrift
package|;
end_package

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
name|AbstractDeserializer
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
name|SerDeException
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
name|SerDeStats
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
name|ObjectInspector
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_comment
comment|/**  * ThriftDeserializer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ThriftDeserializer
extends|extends
name|AbstractDeserializer
block|{
specifier|private
name|ThriftByteStreamTypedSerDe
name|tsd
decl_stmt|;
specifier|public
name|ThriftDeserializer
parameter_list|()
block|{   }
annotation|@
name|Override
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
try|try
block|{
comment|// both the classname and the protocol name are Table properties
comment|// the only hardwired assumption is that records are fixed on a
comment|// per Table basis
name|String
name|className
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|SERIALIZATION_CLASS
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|recordClass
init|=
name|job
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
decl_stmt|;
name|String
name|protoName
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
name|protoName
operator|==
literal|null
condition|)
block|{
name|protoName
operator|=
literal|"TBinaryProtocol"
expr_stmt|;
block|}
comment|// For backward compatibility
name|protoName
operator|=
name|protoName
operator|.
name|replace
argument_list|(
literal|"com.facebook.thrift.protocol"
argument_list|,
literal|"org.apache.thrift.protocol"
argument_list|)
expr_stmt|;
name|TProtocolFactory
name|tp
init|=
name|TReflectionUtils
operator|.
name|getProtocolFactoryByName
argument_list|(
name|protoName
argument_list|)
decl_stmt|;
name|tsd
operator|=
operator|new
name|ThriftByteStreamTypedSerDe
argument_list|(
name|recordClass
argument_list|,
name|tp
argument_list|,
name|tp
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
annotation|@
name|Override
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
return|return
name|tsd
operator|.
name|deserialize
argument_list|(
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|tsd
operator|.
name|getObjectInspector
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


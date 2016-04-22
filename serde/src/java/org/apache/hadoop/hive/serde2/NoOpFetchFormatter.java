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
name|ObjectInspector
import|;
end_import

begin_comment
comment|/**  * A No-op fetch formatter.  * ListSinkOperator uses this when reading from the destination table which has data serialized by  * ThriftJDBCBinarySerDe to a SequenceFile.  */
end_comment

begin_class
specifier|public
class|class
name|NoOpFetchFormatter
parameter_list|<
name|T
parameter_list|>
implements|implements
name|FetchFormatter
argument_list|<
name|Object
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Properties
name|props
parameter_list|)
throws|throws
name|SerDeException
block|{   }
comment|// this returns the row as is because this formatter is only called when
comment|// the ThriftJDBCBinarySerDe was used to serialize the rows to thrift-able objects.
annotation|@
name|Override
specifier|public
name|Object
name|convert
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowOI
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|new
name|Object
index|[]
block|{
name|row
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
block|}
end_class

end_unit


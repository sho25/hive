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
name|serde
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|*
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
name|*
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
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|ColumnSet
implements|implements
name|TBase
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|col
decl_stmt|;
specifier|public
name|ColumnSet
parameter_list|()
block|{   }
specifier|public
name|ColumnSet
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|col
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|col
operator|=
name|col
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|col
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|read
parameter_list|(
name|TProtocol
name|iprot
parameter_list|)
throws|throws
name|TException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Should not be called"
argument_list|)
throw|;
block|}
specifier|public
name|void
name|write
parameter_list|(
name|TProtocol
name|oprot
parameter_list|)
throws|throws
name|TException
block|{
name|TStruct
name|struct
init|=
operator|new
name|TStruct
argument_list|(
literal|"columnset"
argument_list|)
decl_stmt|;
name|oprot
operator|.
name|writeStructBegin
argument_list|(
name|struct
argument_list|)
expr_stmt|;
name|TField
name|field
init|=
operator|new
name|TField
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|col
operator|!=
literal|null
condition|)
block|{
name|field
operator|.
name|name
operator|=
literal|"col"
expr_stmt|;
name|field
operator|.
name|type
operator|=
name|TType
operator|.
name|LIST
expr_stmt|;
name|field
operator|.
name|id
operator|=
literal|1
expr_stmt|;
name|oprot
operator|.
name|writeFieldBegin
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|{
name|oprot
operator|.
name|writeListBegin
argument_list|(
operator|new
name|TList
argument_list|(
name|TType
operator|.
name|STRING
argument_list|,
name|this
operator|.
name|col
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|_iter3
range|:
name|this
operator|.
name|col
control|)
block|{
name|oprot
operator|.
name|writeString
argument_list|(
name|_iter3
argument_list|)
expr_stmt|;
block|}
name|oprot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
block|}
name|oprot
operator|.
name|writeFieldStop
argument_list|()
expr_stmt|;
name|oprot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


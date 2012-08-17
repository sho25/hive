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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * description for ListSinkOperator, just for explain result.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"ListSink"
argument_list|)
specifier|public
class|class
name|ListSinkDesc
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|serializationNullFormat
init|=
literal|"NULL"
decl_stmt|;
specifier|public
name|ListSinkDesc
parameter_list|()
block|{   }
specifier|public
name|ListSinkDesc
parameter_list|(
name|String
name|serializationNullFormat
parameter_list|)
block|{
name|this
operator|.
name|serializationNullFormat
operator|=
name|serializationNullFormat
expr_stmt|;
block|}
specifier|public
name|String
name|getSerializationNullFormat
parameter_list|()
block|{
return|return
name|serializationNullFormat
return|;
block|}
specifier|public
name|void
name|setSerializationNullFormat
parameter_list|(
name|String
name|serializationNullFormat
parameter_list|)
block|{
name|this
operator|.
name|serializationNullFormat
operator|=
name|serializationNullFormat
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|service
package|;
end_package

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
name|TProtocol
import|;
end_import

begin_comment
comment|/**  * Thrift Hive Client Just an empty class that can be used to run queries on a  * stand alone hive server.  */
end_comment

begin_class
specifier|public
class|class
name|HiveClient
extends|extends
name|ThriftHive
operator|.
name|Client
implements|implements
name|HiveInterface
block|{
specifier|public
name|HiveClient
parameter_list|(
name|TProtocol
name|prot
parameter_list|)
block|{
name|super
argument_list|(
name|prot
argument_list|,
name|prot
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


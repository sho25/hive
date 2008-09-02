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
name|metastore
package|;
end_package

begin_comment
comment|// hadoop stuff
end_comment

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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|UnknownTableException
import|;
end_import

begin_class
specifier|public
class|class
name|Table
extends|extends
name|RWTable
block|{
specifier|protected
name|Table
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// used internally for creates
block|}
comment|// called by DB.getTable()
specifier|protected
name|Table
parameter_list|(
name|DB
name|parent
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|o_rdonly
parameter_list|)
throws|throws
name|UnknownTableException
throws|,
name|MetaException
block|{
name|super
argument_list|(
name|parent
argument_list|,
name|tableName
argument_list|,
name|conf
argument_list|,
name|o_rdonly
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


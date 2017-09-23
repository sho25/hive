begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|util
package|;
end_package

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
name|conf
operator|.
name|HiveConf
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
name|Warehouse
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|ql
operator|.
name|session
operator|.
name|LineageState
import|;
end_import

begin_class
specifier|public
class|class
name|Context
block|{
specifier|public
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|public
specifier|final
name|Hive
name|hiveDb
decl_stmt|;
specifier|public
specifier|final
name|Warehouse
name|warehouse
decl_stmt|;
specifier|public
specifier|final
name|PathInfo
name|pathInfo
decl_stmt|;
comment|/*   these are sessionState objects that are copied over to work to allow for parallel execution.   based on the current use case the methods are selectively synchronized, which might need to be   taken care when using other methods.  */
specifier|public
specifier|final
name|LineageState
name|sessionStateLineageState
decl_stmt|;
specifier|public
specifier|final
name|long
name|currentTransactionId
decl_stmt|;
specifier|public
name|Context
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|Hive
name|hiveDb
parameter_list|,
name|LineageState
name|lineageState
parameter_list|,
name|long
name|currentTransactionId
parameter_list|)
throws|throws
name|MetaException
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|hiveDb
operator|=
name|hiveDb
expr_stmt|;
name|this
operator|.
name|warehouse
operator|=
operator|new
name|Warehouse
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|this
operator|.
name|pathInfo
operator|=
operator|new
name|PathInfo
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|sessionStateLineageState
operator|=
name|lineageState
expr_stmt|;
name|this
operator|.
name|currentTransactionId
operator|=
name|currentTransactionId
expr_stmt|;
block|}
block|}
end_class

end_unit


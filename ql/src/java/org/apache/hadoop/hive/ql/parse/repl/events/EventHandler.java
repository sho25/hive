begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|parse
operator|.
name|repl
operator|.
name|events
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
name|fs
operator|.
name|Path
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
name|parse
operator|.
name|ReplicationSpec
import|;
end_import

begin_import
import|import static
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
name|parse
operator|.
name|ReplicationSemanticAnalyzer
operator|.
name|DumpMetaData
import|;
end_import

begin_import
import|import static
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
name|parse
operator|.
name|ReplicationSemanticAnalyzer
operator|.
name|DUMPTYPE
import|;
end_import

begin_interface
specifier|public
interface|interface
name|EventHandler
block|{
name|void
name|handle
parameter_list|(
name|Context
name|withinContext
parameter_list|)
throws|throws
name|Exception
function_decl|;
name|long
name|fromEventId
parameter_list|()
function_decl|;
name|long
name|toEventId
parameter_list|()
function_decl|;
name|DUMPTYPE
name|dumpType
parameter_list|()
function_decl|;
class|class
name|Context
block|{
specifier|final
name|Path
name|eventRoot
decl_stmt|,
name|cmRoot
decl_stmt|;
specifier|final
name|Hive
name|db
decl_stmt|;
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|final
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|public
name|Context
parameter_list|(
name|Path
name|eventRoot
parameter_list|,
name|Path
name|cmRoot
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|this
operator|.
name|eventRoot
operator|=
name|eventRoot
expr_stmt|;
name|this
operator|.
name|cmRoot
operator|=
name|cmRoot
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
block|}
name|DumpMetaData
name|createDmd
parameter_list|(
name|EventHandler
name|eventHandler
parameter_list|)
block|{
return|return
operator|new
name|DumpMetaData
argument_list|(
name|eventRoot
argument_list|,
name|eventHandler
operator|.
name|dumpType
argument_list|()
argument_list|,
name|eventHandler
operator|.
name|fromEventId
argument_list|()
argument_list|,
name|eventHandler
operator|.
name|toEventId
argument_list|()
argument_list|,
name|cmRoot
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
block|}
block|}
end_interface

end_unit


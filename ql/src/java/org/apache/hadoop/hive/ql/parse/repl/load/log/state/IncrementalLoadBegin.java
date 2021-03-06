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
name|load
operator|.
name|log
operator|.
name|state
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
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|ReplState
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
name|repl
operator|.
name|DumpType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonProperty
import|;
end_import

begin_class
specifier|public
class|class
name|IncrementalLoadBegin
extends|extends
name|ReplState
block|{
annotation|@
name|JsonProperty
specifier|private
name|String
name|dbName
decl_stmt|;
annotation|@
name|JsonProperty
specifier|private
name|String
name|dumpDir
decl_stmt|;
annotation|@
name|JsonProperty
specifier|private
name|DumpType
name|loadType
decl_stmt|;
annotation|@
name|JsonProperty
specifier|private
name|Long
name|numEvents
decl_stmt|;
annotation|@
name|JsonProperty
specifier|private
name|Long
name|loadStartTime
decl_stmt|;
specifier|public
name|IncrementalLoadBegin
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|dumpDir
parameter_list|,
name|long
name|numEvents
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|dumpDir
operator|=
name|dumpDir
expr_stmt|;
name|this
operator|.
name|loadType
operator|=
name|DumpType
operator|.
name|INCREMENTAL
expr_stmt|;
name|this
operator|.
name|numEvents
operator|=
name|numEvents
expr_stmt|;
name|this
operator|.
name|loadStartTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
expr_stmt|;
block|}
block|}
end_class

end_unit


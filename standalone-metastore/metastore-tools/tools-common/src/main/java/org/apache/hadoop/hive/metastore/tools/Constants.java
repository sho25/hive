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
name|metastore
operator|.
name|tools
package|;
end_package

begin_comment
comment|/**  * Common constants for metastore tools.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|Constants
block|{
specifier|static
specifier|final
name|String
name|OPT_HOST
init|=
literal|"host"
decl_stmt|;
specifier|static
specifier|final
name|String
name|OPT_PORT
init|=
literal|"port"
decl_stmt|;
specifier|static
specifier|final
name|String
name|OPT_DATABASE
init|=
literal|"database"
decl_stmt|;
specifier|static
specifier|final
name|String
name|OPT_CONF
init|=
literal|"conf"
decl_stmt|;
specifier|static
specifier|final
name|String
name|OPT_VERBOSE
init|=
literal|"verbose"
decl_stmt|;
specifier|static
specifier|final
name|int
name|HMS_DEFAULT_PORT
init|=
literal|8093
decl_stmt|;
comment|// Disable object construction
specifier|private
name|Constants
parameter_list|()
block|{}
block|}
end_class

end_unit


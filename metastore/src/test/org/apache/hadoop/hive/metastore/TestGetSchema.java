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
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|MetaException
import|;
end_import

begin_class
specifier|public
class|class
name|TestGetSchema
extends|extends
name|MetaStoreTestBase
block|{
specifier|public
name|TestGetSchema
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|public
name|void
name|testGetSchema
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|String
name|dbname
init|=
literal|"bar"
decl_stmt|;
name|String
name|name
init|=
literal|"pete_ms_test2"
decl_stmt|;
name|DB
name|db
init|=
name|DB
operator|.
name|createDB
argument_list|(
name|dbname
argument_list|,
name|conf_
argument_list|)
decl_stmt|;
name|Properties
name|schema
init|=
name|createSchema
argument_list|(
name|dbname
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|Table
name|t
init|=
name|Table
operator|.
name|create
argument_list|(
name|db
argument_list|,
name|name
argument_list|,
name|schema
argument_list|,
name|conf_
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|t
operator|.
name|equals
argument_list|(
name|db
operator|.
name|getTable
argument_list|(
name|name
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|db
operator|.
name|getTable
argument_list|(
name|name
argument_list|,
literal|false
argument_list|)
operator|.
name|getSchema
argument_list|()
operator|.
name|equals
argument_list|(
name|schema
argument_list|)
argument_list|)
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit


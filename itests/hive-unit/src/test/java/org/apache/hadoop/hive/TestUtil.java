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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
import|;
end_import

begin_comment
comment|/**  * Test utilities  */
end_comment

begin_class
specifier|public
class|class
name|TestUtil
block|{
comment|/**    * Use this if you want a fresh metastore for your test, without any existing entries.    * It updates the configuration to point to new derby dir location    * @param conf HiveConf to be updated    * @param newloc new location within test temp dir for the metastore db    */
specifier|public
specifier|static
name|void
name|setNewDerbyDbLocation
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|newloc
parameter_list|)
block|{
name|String
name|newDbLoc
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
operator|+
name|newloc
operator|+
literal|"metastore_db"
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
argument_list|,
literal|"jdbc:derby:;databaseName="
operator|+
name|newDbLoc
operator|+
literal|";create=true"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


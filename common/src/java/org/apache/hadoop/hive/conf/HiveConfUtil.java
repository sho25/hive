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
name|conf
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
import|;
end_import

begin_comment
comment|/**  * Hive Configuration utils  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|HiveConfUtil
block|{
comment|/**    * Check if metastore is being used in embedded mode.    * This utility function exists so that the logic for determining the mode is same    * in HiveConf and HiveMetaStoreClient    * @param msUri - metastore server uri    * @return    */
specifier|public
specifier|static
name|boolean
name|isEmbeddedMetaStore
parameter_list|(
name|String
name|msUri
parameter_list|)
block|{
return|return
operator|(
name|msUri
operator|==
literal|null
operator|)
condition|?
literal|true
else|:
name|msUri
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
end_class

end_unit


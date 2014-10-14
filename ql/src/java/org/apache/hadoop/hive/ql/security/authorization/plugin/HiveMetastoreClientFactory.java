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
name|security
operator|.
name|authorization
operator|.
name|plugin
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
name|LimitedPrivate
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
operator|.
name|Evolving
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
name|IMetaStoreClient
import|;
end_import

begin_comment
comment|/**  * Factory for getting current valid instance of IMetaStoreClient  * Metastore client cannot be cached in authorization interface as that  * can get invalidated between the calls with the logic in Hive class.  * The standard way of getting metastore client object is through Hive.get().getMSC().  * But Hive class is not a public interface, so this factory helps in hiding Hive  * class from the authorization interface users.  */
end_comment

begin_interface
annotation|@
name|LimitedPrivate
argument_list|(
name|value
operator|=
block|{
literal|"Apache Argus (incubating)"
block|}
argument_list|)
annotation|@
name|Evolving
specifier|public
interface|interface
name|HiveMetastoreClientFactory
block|{
name|IMetaStoreClient
name|getHiveMetastoreClient
parameter_list|()
throws|throws
name|HiveAuthzPluginException
function_decl|;
block|}
end_interface

end_unit


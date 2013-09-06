begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|HiveStorageHandler
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
name|plan
operator|.
name|TableDesc
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
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
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
name|mapred
operator|.
name|OutputFormat
import|;
end_import

begin_comment
comment|/**  * The abstract Class HCatStorageHandler would server as the base class for all  * the storage handlers required for non-native tables in HCatalog.  * @deprecated Use/modify {@link org.apache.hive.hcatalog.mapreduce.HCatStorageHandler} instead  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatStorageHandler
implements|implements
name|HiveStorageHandler
block|{
comment|//TODO move this to HiveStorageHandler
comment|/**      * This method is called to allow the StorageHandlers the chance      * to populate the JobContext.getConfiguration() with properties that      * maybe be needed by the handler's bundled artifacts (ie InputFormat, SerDe, etc).      * Key value pairs passed into jobProperties is guaranteed to be set in the job's      * configuration object. User's can retrieve "context" information from tableDesc.      * User's should avoid mutating tableDesc and only make changes in jobProperties.      * This method is expected to be idempotent such that a job called with the      * same tableDesc values should return the same key-value pairs in jobProperties.      * Any external state set by this method should remain the same if this method is      * called again. It is up to the user to determine how best guarantee this invariant.      *      * This method in particular is to create a configuration for input.      * @param tableDesc      * @param jobProperties      */
specifier|public
specifier|abstract
name|void
name|configureInputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
function_decl|;
comment|//TODO move this to HiveStorageHandler
comment|/**      * This method is called to allow the StorageHandlers the chance      * to populate the JobContext.getConfiguration() with properties that      * maybe be needed by the handler's bundled artifacts (ie InputFormat, SerDe, etc).      * Key value pairs passed into jobProperties is guaranteed to be set in the job's      * configuration object. User's can retrieve "context" information from tableDesc.      * User's should avoid mutating tableDesc and only make changes in jobProperties.      * This method is expected to be idempotent such that a job called with the      * same tableDesc values should return the same key-value pairs in jobProperties.      * Any external state set by this method should remain the same if this method is      * called again. It is up to the user to determine how best guarantee this invariant.      *      * This method in particular is to create a configuration for output.      * @param tableDesc      * @param jobProperties      */
specifier|public
specifier|abstract
name|void
name|configureOutputJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
function_decl|;
comment|/**      *      *      * @return authorization provider      * @throws HiveException      */
specifier|public
specifier|abstract
name|HiveAuthorizationProvider
name|getAuthorizationProvider
parameter_list|()
throws|throws
name|HiveException
function_decl|;
comment|/*     * (non-Javadoc)     *     * @see org.apache.hadoop.hive.ql.metadata.HiveStorageHandler#     * configureTableJobProperties(org.apache.hadoop.hive.ql.plan.TableDesc,     * java.util.Map)     */
annotation|@
name|Override
annotation|@
name|Deprecated
specifier|public
specifier|final
name|void
name|configureTableJobProperties
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jobProperties
parameter_list|)
block|{     }
comment|/*     * (non-Javadoc)     *     * @see org.apache.hadoop.conf.Configurable#getConf()     */
annotation|@
name|Override
specifier|public
specifier|abstract
name|Configuration
name|getConf
parameter_list|()
function_decl|;
comment|/*     * (non-Javadoc)     *     * @see org.apache.hadoop.conf.Configurable#setConf(org.apache.hadoop.conf.     * Configuration)     */
annotation|@
name|Override
specifier|public
specifier|abstract
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
function_decl|;
name|OutputFormatContainer
name|getOutputFormatContainer
parameter_list|(
name|OutputFormat
name|outputFormat
parameter_list|)
block|{
return|return
operator|new
name|DefaultOutputFormatContainer
argument_list|(
name|outputFormat
argument_list|)
return|;
block|}
block|}
end_class

end_unit


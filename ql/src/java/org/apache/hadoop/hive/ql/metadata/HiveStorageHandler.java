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
name|metadata
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
name|Configurable
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
name|HiveMetaHook
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
name|serde2
operator|.
name|SerDe
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
name|InputFormat
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
comment|/**  * HiveStorageHandler defines a pluggable interface for adding  * new storage handlers to Hive.  A storage handler consists of  * a bundle of the following:  *  *<ul>  *<li>input format  *<li>output format  *<li>serde  *<li>metadata hooks for keeping an external catalog in sync  * with Hive's metastore  *<li>rules for setting up the configuration properties on  * map/reduce jobs which access tables stored by this handler  *</ul>  *  * Storage handler classes are plugged in using the STORED BY 'classname'  * clause in CREATE TABLE.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveStorageHandler
extends|extends
name|Configurable
block|{
comment|/**    * @return Class providing an implementation of {@link InputFormat}    */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|getInputFormatClass
parameter_list|()
function_decl|;
comment|/**    * @return Class providing an implementation of {@link OutputFormat}    */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|OutputFormat
argument_list|>
name|getOutputFormatClass
parameter_list|()
function_decl|;
comment|/**    * @return Class providing an implementation of {@link SerDe}    */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|SerDe
argument_list|>
name|getSerDeClass
parameter_list|()
function_decl|;
comment|/**    * @return metadata hook implementation, or null if this    * storage handler does not need any metadata notifications    */
specifier|public
name|HiveMetaHook
name|getMetaHook
parameter_list|()
function_decl|;
comment|/**    * Configures properties for a job based on the definition of the    * source or target table it accesses.    *    * @param tableDesc descriptor for the table being accessed    *    * @param jobProperties receives properties copied or transformed    * from the table properties    */
specifier|public
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
function_decl|;
block|}
end_interface

end_unit


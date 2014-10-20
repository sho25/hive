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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_comment
comment|/**  * An interface to handle different Templeton storage methods, including  * ZooKeeper and HDFS.  Any storage scheme must be able to handle being  * run in an HDFS environment, where specific file systems and virtual  * machines may not be available.  *  * Storage is done individually in a hierarchy: type (the data type,  * as listed below), then the id (a given jobid, jobtrackingid, etc.),  * then the key/value pairs.  So an entry might look like:  *  * JOB  *   jobid00035  *     user -&gt; rachel  *     datecreated -&gt; 2/5/12  *     etc.  *  * Each field must be available to be fetched/changed individually.  */
end_comment

begin_interface
specifier|public
interface|interface
name|TempletonStorage
block|{
comment|// These are the possible types referenced by 'type' below.
specifier|public
enum|enum
name|Type
block|{
name|UNKNOWN
block|,
name|JOB
block|,
name|JOBTRACKING
block|}
specifier|public
specifier|static
specifier|final
name|String
name|STORAGE_CLASS
init|=
literal|"templeton.storage.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STORAGE_ROOT
init|=
literal|"templeton.storage.root"
decl_stmt|;
comment|/**    * Start the cleanup process for this storage type.    * @param config    */
specifier|public
name|void
name|startCleanup
parameter_list|(
name|Configuration
name|config
parameter_list|)
function_decl|;
comment|/**    * Save a single key/value pair for a specific job id.    * @param type The data type (as listed above)    * @param id The String id of this data grouping (jobid, etc.)    * @param key The name of the field to save    * @param val The value of the field to save    */
specifier|public
name|void
name|saveField
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|val
parameter_list|)
throws|throws
name|NotFoundException
function_decl|;
comment|/**    * Get the value of one field for a given data type.  If the type    * is UNKNOWN, search for the id in all types.    * @param type The data type (as listed above)    * @param id The String id of this data grouping (jobid, etc.)    * @param key The name of the field to retrieve    * @return The value of the field requested, or null if not    * found.    */
specifier|public
name|String
name|getField
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|key
parameter_list|)
function_decl|;
comment|/**    * Delete a data grouping (all data for a jobid, all tracking data    * for a job, etc.).  If the type is UNKNOWN, search for the id    * in all types.    *    * @param type The data type (as listed above)    * @param id The String id of this data grouping (jobid, etc.)    * @return True if successful, false if not, throws NotFoundException    * if the id wasn't found.    */
specifier|public
name|boolean
name|delete
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|id
parameter_list|)
throws|throws
name|NotFoundException
function_decl|;
comment|/**    * Get the id of each data grouping of a given type in the storage    * system.    * @param type The data type (as listed above)    * @return An ArrayList<String> of ids.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllForType
parameter_list|(
name|Type
name|type
parameter_list|)
function_decl|;
comment|/**    * For storage methods that require a connection, this is a hint    * that it's time to open a connection.    */
specifier|public
name|void
name|openStorage
parameter_list|(
name|Configuration
name|config
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For storage methods that require a connection, this is a hint    * that it's time to close the connection.    */
specifier|public
name|void
name|closeStorage
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit


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
name|minihms
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|TrashPolicy
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
name|HiveMetaStoreClient
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
name|Warehouse
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
name|conf
operator|.
name|MetastoreConf
import|;
end_import

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
name|Map
import|;
end_import

begin_comment
comment|/**  * The tests should use this abstract class to access the MetaStore services.  * This abstract class ensures, that the same tests could be run against the different MetaStore  * configurations.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractMetaStoreService
block|{
specifier|private
name|Configuration
name|configuration
decl_stmt|;
specifier|private
name|Warehouse
name|warehouse
decl_stmt|;
specifier|private
name|FileSystem
name|warehouseRootFs
decl_stmt|;
specifier|private
name|Path
name|trashDir
decl_stmt|;
specifier|public
name|AbstractMetaStoreService
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
operator|new
name|Configuration
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the actual configuration of the MetaStore.    * @return The actual configuration    */
specifier|protected
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
comment|/**    * Starts the MetaStoreService. Be aware, as the current MetaStore does not implement clean    * shutdown, starting MetaStoreService is possible only once per test.    *    * @throws Exception if any Exception occurs    */
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|Exception
block|{
name|warehouse
operator|=
operator|new
name|Warehouse
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|warehouseRootFs
operator|=
name|warehouse
operator|.
name|getFs
argument_list|(
name|warehouse
operator|.
name|getWhRoot
argument_list|()
argument_list|)
expr_stmt|;
name|TrashPolicy
name|trashPolicy
init|=
name|TrashPolicy
operator|.
name|getInstance
argument_list|(
name|configuration
argument_list|,
name|warehouseRootFs
argument_list|)
decl_stmt|;
name|trashDir
operator|=
name|trashPolicy
operator|.
name|getCurrentTrashDir
argument_list|()
expr_stmt|;
block|}
comment|/**    * Starts the service with adding extra configuration to the default ones. Be aware, as the    * current MetaStore does not implement clean shutdown, starting MetaStoreService is possible only    * once per test.    *    * @param metastoreOverlay The extra metastore parameters which should be set before starting the    *          service    * @param configurationOverlay The extra other parameters which should be set before starting the    *          service    * @throws Exception if any Exception occurs    */
specifier|public
name|void
name|start
parameter_list|(
name|Map
argument_list|<
name|MetastoreConf
operator|.
name|ConfVars
argument_list|,
name|String
argument_list|>
name|metastoreOverlay
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configurationOverlay
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Set metastoreOverlay parameters
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|MetastoreConf
operator|.
name|ConfVars
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|metastoreOverlay
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|configuration
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Set other configurationOverlay parameters
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|configurationOverlay
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|configuration
operator|.
name|set
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Start the service
name|start
argument_list|()
expr_stmt|;
block|}
comment|/**    * Returns the MetaStoreClient for this MetaStoreService.    *    * @return The client connected to this service    * @throws MetaException if any Exception occurs during client configuration    */
specifier|public
name|IMetaStoreClient
name|getClient
parameter_list|()
throws|throws
name|MetaException
block|{
return|return
operator|new
name|HiveMetaStoreClient
argument_list|(
name|configuration
argument_list|)
return|;
block|}
comment|/**    * Returns the MetaStore Warehouse root directory name.    *    * @return The warehouse root directory    * @throws MetaException IO failure    */
specifier|public
name|Path
name|getWarehouseRoot
parameter_list|()
throws|throws
name|MetaException
block|{
return|return
name|warehouse
operator|.
name|getWhRoot
argument_list|()
return|;
block|}
comment|/**    * Returns the External MetaStore Warehouse root directory name.    *    * @return The external warehouse root directory    * @throws MetaException IO failure    */
specifier|public
name|Path
name|getExternalWarehouseRoot
parameter_list|()
throws|throws
name|MetaException
block|{
return|return
name|warehouse
operator|.
name|getWhRootExternal
argument_list|()
return|;
block|}
comment|/**    * Check if a path exists.    *    * @param path The path to check    * @return true if the path exists    * @throws IOException IO failure    */
specifier|public
name|boolean
name|isPathExists
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|warehouseRootFs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
return|;
block|}
comment|/**    * Check if a path exists in the thrash directory.    *    * @param path The path to check    * @return True if the path exists    * @throws IOException IO failure    */
specifier|public
name|boolean
name|isPathExistsInTrash
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|pathInTrash
init|=
operator|new
name|Path
argument_list|(
name|trashDir
operator|.
name|toUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
name|trashDir
operator|.
name|toUri
argument_list|()
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|trashDir
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|isPathExists
argument_list|(
name|pathInTrash
argument_list|)
return|;
block|}
comment|/**    * Creates a file on the given path.    *    * @param path Destination path    * @param content The content of the file    * @throws IOException IO failure    */
specifier|public
name|void
name|createFile
parameter_list|(
name|Path
name|path
parameter_list|,
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|outputStream
init|=
name|warehouseRootFs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|outputStream
operator|.
name|write
argument_list|(
name|content
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|outputStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Cleans the warehouse and the thrash dirs in preparation for the tests.    *    * @throws MetaException IO failure    */
specifier|public
name|void
name|cleanWarehouseDirs
parameter_list|()
throws|throws
name|MetaException
block|{
name|warehouse
operator|.
name|deleteDir
argument_list|(
name|getWarehouseRoot
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|warehouse
operator|.
name|deleteDir
argument_list|(
name|trashDir
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stops the MetaStoreService. When MetaStore will implement clean shutdown, this method will    * call shutdown on MetaStore. Currently this does nothing :(    */
specifier|public
name|void
name|stop
parameter_list|()
block|{   }
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|configuration
return|;
block|}
block|}
end_class

end_unit


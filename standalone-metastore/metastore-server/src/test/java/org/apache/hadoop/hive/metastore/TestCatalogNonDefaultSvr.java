begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Catalog
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
name|client
operator|.
name|builder
operator|.
name|CatalogBuilder
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_comment
comment|/**  * This tests metastore client calls that do not specify a catalog but with the config on the  * server set to go to a non-default catalog.  */
end_comment

begin_class
specifier|public
class|class
name|TestCatalogNonDefaultSvr
extends|extends
name|NonCatCallsWithCatalog
block|{
specifier|final
specifier|private
name|String
name|catName
init|=
literal|"non_default_svr_catalog"
decl_stmt|;
specifier|private
name|String
name|catLocation
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|catalogCapableClient
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|dropCatalog
parameter_list|()
throws|throws
name|TException
block|{
name|MetaStoreTestUtils
operator|.
name|dropCatalogCascade
argument_list|(
name|catalogCapableClient
argument_list|,
name|catName
argument_list|)
expr_stmt|;
name|catalogCapableClient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|IMetaStoreClient
name|getClient
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Separate client to create the catalog
name|catalogCapableClient
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|catLocation
operator|=
name|MetaStoreTestUtils
operator|.
name|getTestWarehouseDir
argument_list|(
name|catName
argument_list|)
expr_stmt|;
name|Catalog
name|cat
init|=
operator|new
name|CatalogBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|catName
argument_list|)
operator|.
name|setLocation
argument_list|(
name|catLocation
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|catalogCapableClient
operator|.
name|createCatalog
argument_list|(
name|cat
argument_list|)
expr_stmt|;
name|catalogCapableClient
operator|.
name|close
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CATALOG_DEFAULT
argument_list|,
name|catName
argument_list|)
expr_stmt|;
return|return
operator|new
name|HiveMetaStoreClientPreCatalog
argument_list|(
name|conf
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|expectedCatalog
parameter_list|()
block|{
return|return
name|catName
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|expectedBaseDir
parameter_list|()
throws|throws
name|MetaException
block|{
return|return
name|catLocation
return|;
block|}
block|}
end_class

end_unit


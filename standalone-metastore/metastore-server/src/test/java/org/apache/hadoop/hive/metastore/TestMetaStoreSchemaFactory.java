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
name|hive
operator|.
name|metastore
operator|.
name|annotation
operator|.
name|MetastoreUnitTest
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
operator|.
name|ConfVars
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMetaStoreSchemaFactory
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conf
operator|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultConfig
parameter_list|()
block|{
name|IMetaStoreSchemaInfo
name|metastoreSchemaInfo
init|=
name|MetaStoreSchemaInfoFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metastoreSchemaInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithConfigSet
parameter_list|()
block|{
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|SCHEMA_INFO_CLASS
argument_list|,
name|MetaStoreSchemaInfo
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
name|IMetaStoreSchemaInfo
name|metastoreSchemaInfo
init|=
name|MetaStoreSchemaInfoFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|metastoreSchemaInfo
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Unexpected instance type of the class MetaStoreSchemaInfo"
argument_list|,
name|metastoreSchemaInfo
operator|instanceof
name|MetaStoreSchemaInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConstructor
parameter_list|()
block|{
name|String
name|className
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|SCHEMA_INFO_CLASS
argument_list|,
name|MetaStoreSchemaInfo
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|clasz
init|=
literal|null
decl_stmt|;
try|try
block|{
name|clasz
operator|=
name|conf
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|clasz
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|String
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
decl||
name|IllegalArgumentException
decl||
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testInvalidClassName
parameter_list|()
block|{
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|SCHEMA_INFO_CLASS
argument_list|,
literal|"invalid.class.name"
argument_list|)
expr_stmt|;
name|MetaStoreSchemaInfoFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
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
name|hbase
operator|.
name|HBaseConfiguration
import|;
end_import

begin_class
specifier|public
class|class
name|RevisionManagerConfiguration
block|{
specifier|public
specifier|static
name|Configuration
name|addResources
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|addDefaultResource
argument_list|(
literal|"revision-manager-default.xml"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
literal|"revision-manager-site.xml"
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
comment|/**    * Creates a Configuration with Revision Manager resources    * @return a Configuration with Revision Manager resources    */
specifier|public
specifier|static
name|Configuration
name|create
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
return|return
name|addResources
argument_list|(
name|conf
argument_list|)
return|;
block|}
comment|/**    * Creates a clone of passed configuration.    * @param that Configuration to clone.    * @return a Configuration created with the revision-manager-*.xml files plus    * the given configuration.    */
specifier|public
specifier|static
name|Configuration
name|create
parameter_list|(
specifier|final
name|Configuration
name|that
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|create
argument_list|()
decl_stmt|;
comment|//we need to merge things instead of doing new Configuration(that)
comment|//because of a bug in Configuration wherein the config
comment|//set on the MR fronted will get loaded on the backend as resouce called job.xml
comment|//hence adding resources on the backed could potentially overwrite properties
comment|//set on the frontend which we shouldn't be doing here
name|HBaseConfiguration
operator|.
name|merge
argument_list|(
name|conf
argument_list|,
name|that
argument_list|)
expr_stmt|;
return|return
name|conf
return|;
block|}
block|}
end_class

end_unit


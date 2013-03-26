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
name|Properties
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|mapreduce
operator|.
name|Job
import|;
end_import

begin_comment
comment|/**  * The InputFormat to use to read data from HCatalog.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|HCatInputFormat
extends|extends
name|HCatBaseInputFormat
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|InputJobInfo
name|inputJobInfo
decl_stmt|;
comment|/**      * @deprecated as of release 0.5, and will be removed in a future release      */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|InputJobInfo
name|inputJobInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|setInput
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|inputJobInfo
argument_list|)
expr_stmt|;
block|}
comment|/**      * @deprecated as of release 0.5, and will be removed in a future release      */
annotation|@
name|Deprecated
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|InputJobInfo
name|inputJobInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|setInput
argument_list|(
name|conf
argument_list|,
name|inputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|setFilter
argument_list|(
name|inputJobInfo
operator|.
name|getFilter
argument_list|()
argument_list|)
operator|.
name|setProperties
argument_list|(
name|inputJobInfo
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * See {@link #setInput(org.apache.hadoop.conf.Configuration, String, String)}      */
specifier|public
specifier|static
name|HCatInputFormat
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|setInput
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|)
return|;
block|}
comment|/**      * Set inputs to use for the job. This queries the metastore with the given input      * specification and serializes matching partitions into the job conf for use by MR tasks.      * @param conf the job configuration      * @param dbName database name, which if null 'default' is used      * @param tableName table name      * @throws IOException on all errors      */
specifier|public
specifier|static
name|HCatInputFormat
name|setInput
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
argument_list|,
literal|"required argument 'conf' is null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|tableName
argument_list|,
literal|"required argument 'tableName' is null"
argument_list|)
expr_stmt|;
name|HCatInputFormat
name|hCatInputFormat
init|=
operator|new
name|HCatInputFormat
argument_list|()
decl_stmt|;
name|hCatInputFormat
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|hCatInputFormat
operator|.
name|inputJobInfo
operator|=
name|InputJobInfo
operator|.
name|create
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
name|InitializeInput
operator|.
name|setInput
argument_list|(
name|conf
argument_list|,
name|hCatInputFormat
operator|.
name|inputJobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|hCatInputFormat
return|;
block|}
comment|/**      * Set a filter on the input table.      * @param filter the filter specification, which may be null      * @return this      * @throws IOException on all errors      */
specifier|public
name|HCatInputFormat
name|setFilter
parameter_list|(
name|String
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// null filters are supported to simplify client code
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|inputJobInfo
operator|=
name|InputJobInfo
operator|.
name|create
argument_list|(
name|inputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|filter
argument_list|,
name|inputJobInfo
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|InitializeInput
operator|.
name|setInput
argument_list|(
name|conf
argument_list|,
name|inputJobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|this
return|;
block|}
comment|/**      * Set properties for the input format.      * @param properties properties for the input specification      * @return this      * @throws IOException on all errors      */
specifier|public
name|HCatInputFormat
name|setProperties
parameter_list|(
name|Properties
name|properties
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|properties
argument_list|,
literal|"required argument 'properties' is null"
argument_list|)
expr_stmt|;
name|inputJobInfo
operator|=
name|InputJobInfo
operator|.
name|create
argument_list|(
name|inputJobInfo
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getTableName
argument_list|()
argument_list|,
name|inputJobInfo
operator|.
name|getFilter
argument_list|()
argument_list|,
name|properties
argument_list|)
expr_stmt|;
try|try
block|{
name|InitializeInput
operator|.
name|setInput
argument_list|(
name|conf
argument_list|,
name|inputJobInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit


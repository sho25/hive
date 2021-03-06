begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|pig
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|util
operator|.
name|UDFContext
import|;
end_import

begin_comment
comment|/**  * This class is used to test the HCAT_PIG_STORER_EXTERNAL_LOCATION property used in HCatStorer.  * When this property is set, HCatStorer writes the output to the location it specifies. Since  * the property can only be set in the UDFContext, we need this simpler wrapper to do three things:  *<ol>  *<li> save the external dir specified in the Pig script</li>  *<li> set the same UDFContext signature as HCatStorer</li>  *<li> before {@link HCatStorer#setStoreLocation(String, Job)}, set the external dir in the UDFContext.</li>  *</ol>  */
end_comment

begin_class
specifier|public
class|class
name|HCatStorerWrapper
extends|extends
name|HCatStorer
block|{
specifier|private
name|String
name|sign
decl_stmt|;
specifier|private
name|String
name|externalDir
decl_stmt|;
specifier|public
name|HCatStorerWrapper
parameter_list|(
name|String
name|partSpecs
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|externalDir
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|partSpecs
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|this
operator|.
name|externalDir
operator|=
name|externalDir
expr_stmt|;
block|}
specifier|public
name|HCatStorerWrapper
parameter_list|(
name|String
name|partSpecs
parameter_list|,
name|String
name|externalDir
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|partSpecs
argument_list|)
expr_stmt|;
name|this
operator|.
name|externalDir
operator|=
name|externalDir
expr_stmt|;
block|}
specifier|public
name|HCatStorerWrapper
parameter_list|(
name|String
name|externalDir
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|externalDir
operator|=
name|externalDir
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStoreLocation
parameter_list|(
name|String
name|location
parameter_list|,
name|Job
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|udfProps
init|=
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
operator|.
name|getUDFProperties
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|sign
block|}
argument_list|)
decl_stmt|;
name|udfProps
operator|.
name|setProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_STORER_EXTERNAL_LOCATION
argument_list|,
name|externalDir
argument_list|)
expr_stmt|;
name|super
operator|.
name|setStoreLocation
argument_list|(
name|location
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStoreFuncUDFContextSignature
parameter_list|(
name|String
name|signature
parameter_list|)
block|{
name|sign
operator|=
name|signature
expr_stmt|;
name|super
operator|.
name|setStoreFuncUDFContextSignature
argument_list|(
name|signature
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
name|Serializable
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

begin_comment
comment|/** Info about the storer to use for writing the data */
end_comment

begin_class
class|class
name|StorerInfo
implements|implements
name|Serializable
block|{
comment|/** The serialization version */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/** The name of the input storage driver class */
specifier|private
name|String
name|inputSDClass
decl_stmt|;
comment|/** The name of the output storage driver class */
specifier|private
name|String
name|outputSDClass
decl_stmt|;
comment|/** The properties for the storage driver */
specifier|private
name|Properties
name|properties
decl_stmt|;
comment|/**      * Initialize the storage driver      * @param inputSDClass      * @param outputSDClass      * @param properties      */
name|StorerInfo
parameter_list|(
name|String
name|inputSDClass
parameter_list|,
name|String
name|outputSDClass
parameter_list|,
name|Properties
name|properties
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|inputSDClass
operator|=
name|inputSDClass
expr_stmt|;
name|this
operator|.
name|outputSDClass
operator|=
name|outputSDClass
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
block|}
comment|/**      * @return the inputSDClass      */
specifier|public
name|String
name|getInputSDClass
parameter_list|()
block|{
return|return
name|inputSDClass
return|;
block|}
comment|/**      * @param inputSDClass the inputSDClass to set      */
specifier|public
name|void
name|setInputSDClass
parameter_list|(
name|String
name|inputSDClass
parameter_list|)
block|{
name|this
operator|.
name|inputSDClass
operator|=
name|inputSDClass
expr_stmt|;
block|}
comment|/**      * @return the outputSDClass      */
specifier|public
name|String
name|getOutputSDClass
parameter_list|()
block|{
return|return
name|outputSDClass
return|;
block|}
comment|/**      * @param outputSDClass the outputSDClass to set      */
specifier|public
name|void
name|setOutputSDClass
parameter_list|(
name|String
name|outputSDClass
parameter_list|)
block|{
name|this
operator|.
name|outputSDClass
operator|=
name|outputSDClass
expr_stmt|;
block|}
comment|/**      * @return the properties      */
specifier|public
name|Properties
name|getProperties
parameter_list|()
block|{
return|return
name|properties
return|;
block|}
comment|/**      * @param properties the properties to set      */
specifier|public
name|void
name|setProperties
parameter_list|(
name|Properties
name|properties
parameter_list|)
block|{
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
block|}
block|}
end_class

end_unit


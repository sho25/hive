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
comment|/** Information about the storer to use for writing the data. */
end_comment

begin_class
specifier|public
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
comment|/** The properties for the storage handler */
specifier|private
name|Properties
name|properties
decl_stmt|;
specifier|private
name|String
name|ofClass
decl_stmt|;
specifier|private
name|String
name|ifClass
decl_stmt|;
specifier|private
name|String
name|serdeClass
decl_stmt|;
specifier|private
name|String
name|storageHandlerClass
decl_stmt|;
comment|/**    * Initialize the storer information.    * @param ifClass the input format class    * @param ofClass the output format class    * @param serdeClass the SerDe class    * @param storageHandlerClass the storage handler class    * @param properties the properties for the storage handler    */
specifier|public
name|StorerInfo
parameter_list|(
name|String
name|ifClass
parameter_list|,
name|String
name|ofClass
parameter_list|,
name|String
name|serdeClass
parameter_list|,
name|String
name|storageHandlerClass
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
name|ifClass
operator|=
name|ifClass
expr_stmt|;
name|this
operator|.
name|ofClass
operator|=
name|ofClass
expr_stmt|;
name|this
operator|.
name|serdeClass
operator|=
name|serdeClass
expr_stmt|;
name|this
operator|.
name|storageHandlerClass
operator|=
name|storageHandlerClass
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
block|}
comment|/**    * @return the input format class    */
specifier|public
name|String
name|getIfClass
parameter_list|()
block|{
return|return
name|ifClass
return|;
block|}
comment|/**    * @param ifClass the input format class    */
specifier|public
name|void
name|setIfClass
parameter_list|(
name|String
name|ifClass
parameter_list|)
block|{
name|this
operator|.
name|ifClass
operator|=
name|ifClass
expr_stmt|;
block|}
comment|/**    * @return the output format class    */
specifier|public
name|String
name|getOfClass
parameter_list|()
block|{
return|return
name|ofClass
return|;
block|}
comment|/**    * @return the serdeClass    */
specifier|public
name|String
name|getSerdeClass
parameter_list|()
block|{
return|return
name|serdeClass
return|;
block|}
comment|/**    * @return the storageHandlerClass    */
specifier|public
name|String
name|getStorageHandlerClass
parameter_list|()
block|{
return|return
name|storageHandlerClass
return|;
block|}
comment|/**    * @return the storer properties    */
specifier|public
name|Properties
name|getProperties
parameter_list|()
block|{
return|return
name|properties
return|;
block|}
comment|/**    * @param properties the storer properties to set     */
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


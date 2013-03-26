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
name|Map
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
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_comment
comment|/** The Class used to serialize the partition information read from the metadata server that maps to a partition */
end_comment

begin_class
specifier|public
class|class
name|PartInfo
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
comment|/** The partition schema. */
specifier|private
specifier|final
name|HCatSchema
name|partitionSchema
decl_stmt|;
comment|/** The information about which input storage driver to use */
specifier|private
specifier|final
name|String
name|inputStorageDriverClass
decl_stmt|;
comment|/** Howl-specific properties set at the partition */
specifier|private
specifier|final
name|Properties
name|howlProperties
decl_stmt|;
comment|/** The data location. */
specifier|private
specifier|final
name|String
name|location
decl_stmt|;
comment|/** The map of partition key names and their values. */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
decl_stmt|;
comment|/**    * Instantiates a new howl partition info.    * @param partitionSchema the partition schema    * @param inputStorageDriverClass the input storage driver class name    * @param location the location    * @param howlProperties howl-specific properties at the partition    */
specifier|public
name|PartInfo
parameter_list|(
name|HCatSchema
name|partitionSchema
parameter_list|,
name|String
name|inputStorageDriverClass
parameter_list|,
name|String
name|location
parameter_list|,
name|Properties
name|howlProperties
parameter_list|)
block|{
name|this
operator|.
name|partitionSchema
operator|=
name|partitionSchema
expr_stmt|;
name|this
operator|.
name|inputStorageDriverClass
operator|=
name|inputStorageDriverClass
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|howlProperties
operator|=
name|howlProperties
expr_stmt|;
block|}
comment|/**    * Gets the value of partitionSchema.    * @return the partitionSchema    */
specifier|public
name|HCatSchema
name|getPartitionSchema
parameter_list|()
block|{
return|return
name|partitionSchema
return|;
block|}
comment|/**    * Gets the value of input storage driver class name.    * @return the input storage driver class name    */
specifier|public
name|String
name|getInputStorageDriverClass
parameter_list|()
block|{
return|return
name|inputStorageDriverClass
return|;
block|}
comment|/**    * Gets the value of howlProperties.    * @return the howlProperties    */
specifier|public
name|Properties
name|getInputStorageDriverProperties
parameter_list|()
block|{
return|return
name|howlProperties
return|;
block|}
comment|/**    * Gets the value of location.    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
comment|/**    * Sets the partition values.    * @param partitionValues the new partition values    */
specifier|public
name|void
name|setPartitionValues
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|)
block|{
name|this
operator|.
name|partitionValues
operator|=
name|partitionValues
expr_stmt|;
block|}
comment|/**    * Gets the partition values.    * @return the partition values    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionValues
parameter_list|()
block|{
return|return
name|partitionValues
return|;
block|}
block|}
end_class

end_unit


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
name|model
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|MStorageDescriptor
block|{
specifier|private
name|MColumnDescriptor
name|cd
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
specifier|private
name|String
name|inputFormat
decl_stmt|;
specifier|private
name|String
name|outputFormat
decl_stmt|;
specifier|private
name|boolean
name|isCompressed
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|numBuckets
init|=
literal|1
decl_stmt|;
specifier|private
name|MSerDeInfo
name|serDeInfo
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
decl_stmt|;
specifier|private
name|List
argument_list|<
name|MOrder
argument_list|>
name|sortCols
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|MStringList
argument_list|>
name|skewedColValues
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|MStringList
argument_list|,
name|String
argument_list|>
name|skewedColValueLocationMaps
decl_stmt|;
specifier|private
name|boolean
name|isStoredAsSubDirectories
decl_stmt|;
specifier|public
name|MStorageDescriptor
parameter_list|()
block|{}
comment|/**    * @param cd    * @param location    * @param inputFormat    * @param outputFormat    * @param isCompressed    * @param numBuckets    * @param serDeInfo    * @param bucketCols    * @param sortOrder    * @param parameters    */
specifier|public
name|MStorageDescriptor
parameter_list|(
name|MColumnDescriptor
name|cd
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|inputFormat
parameter_list|,
name|String
name|outputFormat
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|int
name|numBuckets
parameter_list|,
name|MSerDeInfo
name|serDeInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|,
name|List
argument_list|<
name|MOrder
argument_list|>
name|sortOrder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
parameter_list|,
name|List
argument_list|<
name|MStringList
argument_list|>
name|skewedColValues
parameter_list|,
name|Map
argument_list|<
name|MStringList
argument_list|,
name|String
argument_list|>
name|skewedColValueLocationMaps
parameter_list|,
name|boolean
name|storedAsSubDirectories
parameter_list|)
block|{
name|this
operator|.
name|cd
operator|=
name|cd
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|inputFormat
operator|=
name|inputFormat
expr_stmt|;
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
name|this
operator|.
name|isCompressed
operator|=
name|isCompressed
expr_stmt|;
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
name|this
operator|.
name|serDeInfo
operator|=
name|serDeInfo
expr_stmt|;
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
name|this
operator|.
name|sortCols
operator|=
name|sortOrder
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
name|this
operator|.
name|skewedColNames
operator|=
name|skewedColNames
expr_stmt|;
name|this
operator|.
name|skewedColValues
operator|=
name|skewedColValues
expr_stmt|;
name|this
operator|.
name|skewedColValueLocationMaps
operator|=
name|skewedColValueLocationMaps
expr_stmt|;
name|this
operator|.
name|isStoredAsSubDirectories
operator|=
name|storedAsSubDirectories
expr_stmt|;
block|}
comment|/**    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
comment|/**    * @param location the location to set    */
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
comment|/**    * @return the isCompressed    */
specifier|public
name|boolean
name|isCompressed
parameter_list|()
block|{
return|return
name|isCompressed
return|;
block|}
comment|/**    * @param isCompressed the isCompressed to set    */
specifier|public
name|void
name|setIsCompressed
parameter_list|(
name|boolean
name|isCompressed
parameter_list|)
block|{
name|this
operator|.
name|isCompressed
operator|=
name|isCompressed
expr_stmt|;
block|}
comment|/**    * @return the numBuckets    */
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
comment|/**    * @param numBuckets the numBuckets to set    */
specifier|public
name|void
name|setNumBuckets
parameter_list|(
name|int
name|numBuckets
parameter_list|)
block|{
name|this
operator|.
name|numBuckets
operator|=
name|numBuckets
expr_stmt|;
block|}
comment|/**    * @return the bucketCols    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|bucketCols
return|;
block|}
comment|/**    * @param bucketCols the bucketCols to set    */
specifier|public
name|void
name|setBucketCols
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
parameter_list|)
block|{
name|this
operator|.
name|bucketCols
operator|=
name|bucketCols
expr_stmt|;
block|}
comment|/**    * @return the parameters    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParameters
parameter_list|()
block|{
return|return
name|parameters
return|;
block|}
comment|/**    * @param parameters the parameters to set    */
specifier|public
name|void
name|setParameters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
name|this
operator|.
name|parameters
operator|=
name|parameters
expr_stmt|;
block|}
comment|/**    * @return the inputFormat    */
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
return|return
name|inputFormat
return|;
block|}
comment|/**    * @param inputFormat the inputFormat to set    */
specifier|public
name|void
name|setInputFormat
parameter_list|(
name|String
name|inputFormat
parameter_list|)
block|{
name|this
operator|.
name|inputFormat
operator|=
name|inputFormat
expr_stmt|;
block|}
comment|/**    * @return the outputFormat    */
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|outputFormat
return|;
block|}
comment|/**    * @param outputFormat the outputFormat to set    */
specifier|public
name|void
name|setOutputFormat
parameter_list|(
name|String
name|outputFormat
parameter_list|)
block|{
name|this
operator|.
name|outputFormat
operator|=
name|outputFormat
expr_stmt|;
block|}
comment|/**    * @return the column descriptor    */
specifier|public
name|MColumnDescriptor
name|getCD
parameter_list|()
block|{
return|return
name|cd
return|;
block|}
comment|/**    * @param cd the Column Descriptor to set    */
specifier|public
name|void
name|setCD
parameter_list|(
name|MColumnDescriptor
name|cd
parameter_list|)
block|{
name|this
operator|.
name|cd
operator|=
name|cd
expr_stmt|;
block|}
comment|/**    * @return the serDe    */
specifier|public
name|MSerDeInfo
name|getSerDeInfo
parameter_list|()
block|{
return|return
name|serDeInfo
return|;
block|}
comment|/**    * @param serDe the serDe to set    */
specifier|public
name|void
name|setSerDeInfo
parameter_list|(
name|MSerDeInfo
name|serDe
parameter_list|)
block|{
name|this
operator|.
name|serDeInfo
operator|=
name|serDe
expr_stmt|;
block|}
comment|/**    * @param sortOrder the sortOrder to set    */
specifier|public
name|void
name|setSortCols
parameter_list|(
name|List
argument_list|<
name|MOrder
argument_list|>
name|sortOrder
parameter_list|)
block|{
name|this
operator|.
name|sortCols
operator|=
name|sortOrder
expr_stmt|;
block|}
comment|/**    * @return the sortOrder    */
specifier|public
name|List
argument_list|<
name|MOrder
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortCols
return|;
block|}
comment|/**    * @return the skewedColNames    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSkewedColNames
parameter_list|()
block|{
return|return
name|skewedColNames
return|;
block|}
comment|/**    * @param skewedColNames the skewedColNames to set    */
specifier|public
name|void
name|setSkewedColNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
parameter_list|)
block|{
name|this
operator|.
name|skewedColNames
operator|=
name|skewedColNames
expr_stmt|;
block|}
comment|/**    * @return the skewedColValues    */
specifier|public
name|List
argument_list|<
name|MStringList
argument_list|>
name|getSkewedColValues
parameter_list|()
block|{
return|return
name|skewedColValues
return|;
block|}
comment|/**    * @param skewedColValues the skewedColValues to set    */
specifier|public
name|void
name|setSkewedColValues
parameter_list|(
name|List
argument_list|<
name|MStringList
argument_list|>
name|skewedColValues
parameter_list|)
block|{
name|this
operator|.
name|skewedColValues
operator|=
name|skewedColValues
expr_stmt|;
block|}
comment|/**    * @return the skewedColValueLocationMaps    */
specifier|public
name|Map
argument_list|<
name|MStringList
argument_list|,
name|String
argument_list|>
name|getSkewedColValueLocationMaps
parameter_list|()
block|{
return|return
name|skewedColValueLocationMaps
return|;
block|}
comment|/**    * @param listBucketColValuesMapping the skewedColValueLocationMaps to set    */
specifier|public
name|void
name|setSkewedColValueLocationMaps
parameter_list|(
name|Map
argument_list|<
name|MStringList
argument_list|,
name|String
argument_list|>
name|listBucketColValuesMapping
parameter_list|)
block|{
name|this
operator|.
name|skewedColValueLocationMaps
operator|=
name|listBucketColValuesMapping
expr_stmt|;
block|}
comment|/**    * @return the storedAsSubDirectories    */
specifier|public
name|boolean
name|isStoredAsSubDirectories
parameter_list|()
block|{
return|return
name|isStoredAsSubDirectories
return|;
block|}
comment|/**    * @param storedAsSubDirectories the storedAsSubDirectories to set    */
specifier|public
name|void
name|setStoredAsSubDirectories
parameter_list|(
name|boolean
name|storedAsSubDirectories
parameter_list|)
block|{
name|this
operator|.
name|isStoredAsSubDirectories
operator|=
name|storedAsSubDirectories
expr_stmt|;
block|}
specifier|public
name|MColumnDescriptor
name|getCd
parameter_list|()
block|{
return|return
name|cd
return|;
block|}
specifier|public
name|void
name|setCd
parameter_list|(
name|MColumnDescriptor
name|cd
parameter_list|)
block|{
name|this
operator|.
name|cd
operator|=
name|cd
expr_stmt|;
block|}
block|}
end_class

end_unit


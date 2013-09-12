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
name|hive
operator|.
name|hcatalog
operator|.
name|api
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|FieldSchema
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
name|Order
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
name|Table
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
name|HCatException
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
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
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
name|data
operator|.
name|schema
operator|.
name|HCatSchemaUtils
import|;
end_import

begin_comment
comment|/**  * The HCatTable is a wrapper around org.apache.hadoop.hive.metastore.api.Table.  */
end_comment

begin_class
specifier|public
class|class
name|HCatTable
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|tabletype
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|cols
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|partCols
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
name|Order
argument_list|>
name|sortCols
decl_stmt|;
specifier|private
name|int
name|numBuckets
decl_stmt|;
specifier|private
name|String
name|inputFileFormat
decl_stmt|;
specifier|private
name|String
name|outputFileFormat
decl_stmt|;
specifier|private
name|String
name|storageHandler
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|serde
decl_stmt|;
specifier|private
name|String
name|location
decl_stmt|;
name|HCatTable
parameter_list|(
name|Table
name|hiveTable
parameter_list|)
throws|throws
name|HCatException
block|{
name|this
operator|.
name|tableName
operator|=
name|hiveTable
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|hiveTable
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|tabletype
operator|=
name|hiveTable
operator|.
name|getTableType
argument_list|()
expr_stmt|;
name|cols
operator|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FieldSchema
name|colFS
range|:
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
control|)
block|{
name|cols
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
name|colFS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|partCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|HCatFieldSchema
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|FieldSchema
name|colFS
range|:
name|hiveTable
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
name|partCols
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
name|colFS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|bucketCols
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getBucketCols
argument_list|()
expr_stmt|;
name|sortCols
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getSortCols
argument_list|()
expr_stmt|;
name|numBuckets
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
expr_stmt|;
name|inputFileFormat
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
expr_stmt|;
name|outputFileFormat
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
expr_stmt|;
name|storageHandler
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
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
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
argument_list|)
expr_stmt|;
name|tblProps
operator|=
name|hiveTable
operator|.
name|getParameters
argument_list|()
expr_stmt|;
name|serde
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
expr_stmt|;
name|location
operator|=
name|hiveTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
expr_stmt|;
block|}
comment|/**    * Gets the table name.    *    * @return the table name    */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * Gets the db name.    *    * @return the db name    */
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
comment|/**    * Gets the columns.    *    * @return the columns    */
specifier|public
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getCols
parameter_list|()
block|{
return|return
name|cols
return|;
block|}
comment|/**    * Gets the part columns.    *    * @return the part columns    */
specifier|public
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getPartCols
parameter_list|()
block|{
return|return
name|partCols
return|;
block|}
comment|/**    * Gets the bucket columns.    *    * @return the bucket columns    */
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
comment|/**    * Gets the sort columns.    *    * @return the sort columns    */
specifier|public
name|List
argument_list|<
name|Order
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortCols
return|;
block|}
comment|/**    * Gets the number of buckets.    *    * @return the number of buckets    */
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|numBuckets
return|;
block|}
comment|/**    * Gets the storage handler.    *    * @return the storage handler    */
specifier|public
name|String
name|getStorageHandler
parameter_list|()
block|{
return|return
name|storageHandler
return|;
block|}
comment|/**    * Gets the table props.    *    * @return the table props    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getTblProps
parameter_list|()
block|{
return|return
name|tblProps
return|;
block|}
comment|/**    * Gets the tabletype.    *    * @return the tabletype    */
specifier|public
name|String
name|getTabletype
parameter_list|()
block|{
return|return
name|tabletype
return|;
block|}
comment|/**    * Gets the input file format.    *    * @return the input file format    */
specifier|public
name|String
name|getInputFileFormat
parameter_list|()
block|{
return|return
name|inputFileFormat
return|;
block|}
comment|/**    * Gets the output file format.    *    * @return the output file format    */
specifier|public
name|String
name|getOutputFileFormat
parameter_list|()
block|{
return|return
name|outputFileFormat
return|;
block|}
comment|/**    * Gets the serde lib.    *    * @return the serde lib    */
specifier|public
name|String
name|getSerdeLib
parameter_list|()
block|{
return|return
name|serde
return|;
block|}
comment|/**    * Gets the location.    *    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HCatTable ["
operator|+
operator|(
name|tableName
operator|!=
literal|null
condition|?
literal|"tableName="
operator|+
name|tableName
operator|+
literal|", "
else|:
literal|"tableName=null"
operator|)
operator|+
operator|(
name|dbName
operator|!=
literal|null
condition|?
literal|"dbName="
operator|+
name|dbName
operator|+
literal|", "
else|:
literal|"dbName=null"
operator|)
operator|+
operator|(
name|tabletype
operator|!=
literal|null
condition|?
literal|"tabletype="
operator|+
name|tabletype
operator|+
literal|", "
else|:
literal|"tabletype=null"
operator|)
operator|+
operator|(
name|cols
operator|!=
literal|null
condition|?
literal|"cols="
operator|+
name|cols
operator|+
literal|", "
else|:
literal|"cols=null"
operator|)
operator|+
operator|(
name|partCols
operator|!=
literal|null
condition|?
literal|"partCols="
operator|+
name|partCols
operator|+
literal|", "
else|:
literal|"partCols==null"
operator|)
operator|+
operator|(
name|bucketCols
operator|!=
literal|null
condition|?
literal|"bucketCols="
operator|+
name|bucketCols
operator|+
literal|", "
else|:
literal|"bucketCols=null"
operator|)
operator|+
operator|(
name|sortCols
operator|!=
literal|null
condition|?
literal|"sortCols="
operator|+
name|sortCols
operator|+
literal|", "
else|:
literal|"sortCols=null"
operator|)
operator|+
literal|"numBuckets="
operator|+
name|numBuckets
operator|+
literal|", "
operator|+
operator|(
name|inputFileFormat
operator|!=
literal|null
condition|?
literal|"inputFileFormat="
operator|+
name|inputFileFormat
operator|+
literal|", "
else|:
literal|"inputFileFormat=null"
operator|)
operator|+
operator|(
name|outputFileFormat
operator|!=
literal|null
condition|?
literal|"outputFileFormat="
operator|+
name|outputFileFormat
operator|+
literal|", "
else|:
literal|"outputFileFormat=null"
operator|)
operator|+
operator|(
name|storageHandler
operator|!=
literal|null
condition|?
literal|"storageHandler="
operator|+
name|storageHandler
operator|+
literal|", "
else|:
literal|"storageHandler=null"
operator|)
operator|+
operator|(
name|tblProps
operator|!=
literal|null
condition|?
literal|"tblProps="
operator|+
name|tblProps
operator|+
literal|", "
else|:
literal|"tblProps=null"
operator|)
operator|+
operator|(
name|serde
operator|!=
literal|null
condition|?
literal|"serde="
operator|+
name|serde
operator|+
literal|", "
else|:
literal|"serde="
operator|)
operator|+
operator|(
name|location
operator|!=
literal|null
condition|?
literal|"location="
operator|+
name|location
else|:
literal|"location="
operator|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit


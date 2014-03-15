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
name|Partition
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
name|StorageDescriptor
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
comment|/**  * The HCatPartition is a wrapper around org.apache.hadoop.hive.metastore.api.Partition.  * @deprecated Use/modify {@link org.apache.hive.hcatalog.api.HCatPartition} instead  */
end_comment

begin_class
specifier|public
class|class
name|HCatPartition
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|values
decl_stmt|;
specifier|private
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|tableCols
decl_stmt|;
specifier|private
name|int
name|createTime
decl_stmt|;
specifier|private
name|int
name|lastAccessTime
decl_stmt|;
specifier|private
name|StorageDescriptor
name|sd
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
name|HCatPartition
parameter_list|(
name|Partition
name|partition
parameter_list|)
throws|throws
name|HCatException
block|{
name|this
operator|.
name|tableName
operator|=
name|partition
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|partition
operator|.
name|getDbName
argument_list|()
expr_stmt|;
name|this
operator|.
name|createTime
operator|=
name|partition
operator|.
name|getCreateTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|lastAccessTime
operator|=
name|partition
operator|.
name|getLastAccessTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|parameters
operator|=
name|partition
operator|.
name|getParameters
argument_list|()
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|partition
operator|.
name|getValues
argument_list|()
expr_stmt|;
name|this
operator|.
name|sd
operator|=
name|partition
operator|.
name|getSd
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableCols
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
name|fs
range|:
name|this
operator|.
name|sd
operator|.
name|getCols
argument_list|()
control|)
block|{
name|this
operator|.
name|tableCols
operator|.
name|add
argument_list|(
name|HCatSchemaUtils
operator|.
name|getHCatFieldSchema
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Gets the table name.    *    * @return the table name    */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
block|}
comment|/**    * Gets the database name.    *    * @return the database name    */
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|this
operator|.
name|dbName
return|;
block|}
comment|/**    * Gets the columns of the table.    *    * @return the columns    */
specifier|public
name|List
argument_list|<
name|HCatFieldSchema
argument_list|>
name|getColumns
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableCols
return|;
block|}
comment|/**    * Gets the input format.    *    * @return the input format    */
specifier|public
name|String
name|getInputFormat
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getInputFormat
argument_list|()
return|;
block|}
comment|/**    * Gets the output format.    *    * @return the output format    */
specifier|public
name|String
name|getOutputFormat
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getOutputFormat
argument_list|()
return|;
block|}
comment|/**    * Gets the storage handler.    *    * @return the storage handler    */
specifier|public
name|String
name|getStorageHandler
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
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
return|;
block|}
comment|/**    * Gets the location.    *    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getLocation
argument_list|()
return|;
block|}
comment|/**    * Gets the serde.    *    * @return the serde    */
specifier|public
name|String
name|getSerDe
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
return|;
block|}
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
name|this
operator|.
name|parameters
return|;
block|}
comment|/**    * Gets the last access time.    *    * @return the last access time    */
specifier|public
name|int
name|getLastAccessTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|lastAccessTime
return|;
block|}
comment|/**    * Gets the creates the time.    *    * @return the creates the time    */
specifier|public
name|int
name|getCreateTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|createTime
return|;
block|}
comment|/**    * Gets the values.    *    * @return the values    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|this
operator|.
name|values
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
name|this
operator|.
name|sd
operator|.
name|getBucketCols
argument_list|()
return|;
block|}
comment|/**    * Gets the number of buckets.    *    * @return the number of buckets    */
specifier|public
name|int
name|getNumBuckets
parameter_list|()
block|{
return|return
name|this
operator|.
name|sd
operator|.
name|getNumBuckets
argument_list|()
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
name|this
operator|.
name|sd
operator|.
name|getSortCols
argument_list|()
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
literal|"HCatPartition ["
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
name|values
operator|!=
literal|null
condition|?
literal|"values="
operator|+
name|values
operator|+
literal|", "
else|:
literal|"values=null"
operator|)
operator|+
literal|"createTime="
operator|+
name|createTime
operator|+
literal|", lastAccessTime="
operator|+
name|lastAccessTime
operator|+
literal|", "
operator|+
operator|(
name|sd
operator|!=
literal|null
condition|?
literal|"sd="
operator|+
name|sd
operator|+
literal|", "
else|:
literal|"sd=null"
operator|)
operator|+
operator|(
name|parameters
operator|!=
literal|null
condition|?
literal|"parameters="
operator|+
name|parameters
else|:
literal|"parameters=null"
operator|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit


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
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreUtils
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
comment|/**   * The class used to serialize and store the output related information    * @deprecated Use/modify {@link org.apache.hive.hcatalog.mapreduce.OutputJobInfo} instead  */
end_comment

begin_class
specifier|public
class|class
name|OutputJobInfo
implements|implements
name|Serializable
block|{
comment|/** The db and table names. */
specifier|private
specifier|final
name|String
name|databaseName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
comment|/** The serialization version. */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/** The table info provided by user. */
specifier|private
name|HCatTableInfo
name|tableInfo
decl_stmt|;
comment|/** The output schema. This is given to us by user.  This wont contain any    * partition columns ,even if user has specified them.    * */
specifier|private
name|HCatSchema
name|outputSchema
decl_stmt|;
comment|/** The location of the partition being written */
specifier|private
name|String
name|location
decl_stmt|;
comment|/** The partition values to publish to, if used for output*/
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|posOfPartCols
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|posOfDynPartCols
decl_stmt|;
specifier|private
name|Properties
name|properties
decl_stmt|;
specifier|private
name|int
name|maxDynamicPartitions
decl_stmt|;
comment|/** List of keys for which values were not specified at write setup time, to be infered at write time */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartitioningKeys
decl_stmt|;
specifier|private
name|boolean
name|harRequested
decl_stmt|;
comment|/**    * Initializes a new OutputJobInfo instance    * for writing data from a table.    * @param databaseName the db name    * @param tableName the table name    * @param partitionValues The partition values to publish to, can be null or empty Map to    * work with hadoop security, the kerberos principal name of the server - else null    * The principal name should be of the form:    *<servicename>/_HOST@<realm> like "hcat/_HOST@myrealm.com"    * The special string _HOST will be replaced automatically with the correct host name    * indicate write to a unpartitioned table. For partitioned tables, this map should    * contain keys for all partition columns with corresponding values.    */
specifier|public
specifier|static
name|OutputJobInfo
name|create
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|)
block|{
return|return
operator|new
name|OutputJobInfo
argument_list|(
name|databaseName
argument_list|,
name|tableName
argument_list|,
name|partitionValues
argument_list|)
return|;
block|}
specifier|private
name|OutputJobInfo
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
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
name|databaseName
operator|=
operator|(
name|databaseName
operator|==
literal|null
operator|)
condition|?
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
else|:
name|databaseName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partitionValues
operator|=
name|partitionValues
expr_stmt|;
name|this
operator|.
name|properties
operator|=
operator|new
name|Properties
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the posOfPartCols    */
specifier|protected
name|List
argument_list|<
name|Integer
argument_list|>
name|getPosOfPartCols
parameter_list|()
block|{
return|return
name|posOfPartCols
return|;
block|}
comment|/**    * @return the posOfDynPartCols    */
specifier|protected
name|List
argument_list|<
name|Integer
argument_list|>
name|getPosOfDynPartCols
parameter_list|()
block|{
return|return
name|posOfDynPartCols
return|;
block|}
comment|/**    * @param posOfPartCols the posOfPartCols to set    */
specifier|protected
name|void
name|setPosOfPartCols
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|posOfPartCols
parameter_list|)
block|{
comment|// sorting the list in the descending order so that deletes happen back-to-front
name|Collections
operator|.
name|sort
argument_list|(
name|posOfPartCols
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Integer
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Integer
name|earlier
parameter_list|,
name|Integer
name|later
parameter_list|)
block|{
return|return
operator|(
name|earlier
operator|>
name|later
operator|)
condition|?
operator|-
literal|1
else|:
operator|(
operator|(
name|earlier
operator|==
name|later
operator|)
condition|?
literal|0
else|:
literal|1
operator|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|this
operator|.
name|posOfPartCols
operator|=
name|posOfPartCols
expr_stmt|;
block|}
comment|/**    * @param posOfDynPartCols the posOfDynPartCols to set    */
specifier|protected
name|void
name|setPosOfDynPartCols
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|posOfDynPartCols
parameter_list|)
block|{
comment|// Important - no sorting here! We retain order, it's used to match with values at runtime
name|this
operator|.
name|posOfDynPartCols
operator|=
name|posOfDynPartCols
expr_stmt|;
block|}
comment|/**    * @return the tableInfo    */
specifier|public
name|HCatTableInfo
name|getTableInfo
parameter_list|()
block|{
return|return
name|tableInfo
return|;
block|}
comment|/**    * @return the outputSchema    */
specifier|public
name|HCatSchema
name|getOutputSchema
parameter_list|()
block|{
return|return
name|outputSchema
return|;
block|}
comment|/**    * @param schema the outputSchema to set    */
specifier|public
name|void
name|setOutputSchema
parameter_list|(
name|HCatSchema
name|schema
parameter_list|)
block|{
name|this
operator|.
name|outputSchema
operator|=
name|schema
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
comment|/**    * @param location location to write to    */
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
comment|/**    * Sets the value of partitionValues    * @param partitionValues the partition values to set    */
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
comment|/**    * Gets the value of partitionValues    * @return the partitionValues    */
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
comment|/**    * set the tablInfo instance    * this should be the same instance    * determined by this object's DatabaseName and TableName    * @param tableInfo    */
name|void
name|setTableInfo
parameter_list|(
name|HCatTableInfo
name|tableInfo
parameter_list|)
block|{
name|this
operator|.
name|tableInfo
operator|=
name|tableInfo
expr_stmt|;
block|}
comment|/**    * @return database name of table to write to    */
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|databaseName
return|;
block|}
comment|/**    * @return name of table to write to    */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * Set/Get Property information to be passed down to *StorageHandler implementation    * put implementation specific storage handler configurations here    * @return the implementation specific job properties    */
specifier|public
name|Properties
name|getProperties
parameter_list|()
block|{
return|return
name|properties
return|;
block|}
comment|/**    * Set maximum number of allowable dynamic partitions    * @param maxDynamicPartitions    */
specifier|public
name|void
name|setMaximumDynamicPartitions
parameter_list|(
name|int
name|maxDynamicPartitions
parameter_list|)
block|{
name|this
operator|.
name|maxDynamicPartitions
operator|=
name|maxDynamicPartitions
expr_stmt|;
block|}
comment|/**    * Returns maximum number of allowable dynamic partitions    * @return maximum number of allowable dynamic partitions    */
specifier|public
name|int
name|getMaxDynamicPartitions
parameter_list|()
block|{
return|return
name|this
operator|.
name|maxDynamicPartitions
return|;
block|}
comment|/**    * Sets whether or not hadoop archiving has been requested for this job    * @param harRequested    */
specifier|public
name|void
name|setHarRequested
parameter_list|(
name|boolean
name|harRequested
parameter_list|)
block|{
name|this
operator|.
name|harRequested
operator|=
name|harRequested
expr_stmt|;
block|}
comment|/**    * Returns whether or not hadoop archiving has been requested for this job    * @return whether or not hadoop archiving has been requested for this job    */
specifier|public
name|boolean
name|getHarRequested
parameter_list|()
block|{
return|return
name|this
operator|.
name|harRequested
return|;
block|}
comment|/**    * Returns whether or not Dynamic Partitioning is used    * @return whether or not dynamic partitioning is currently enabled and used    */
specifier|public
name|boolean
name|isDynamicPartitioningUsed
parameter_list|()
block|{
return|return
operator|!
operator|(
operator|(
name|dynamicPartitioningKeys
operator|==
literal|null
operator|)
operator|||
operator|(
name|dynamicPartitioningKeys
operator|.
name|isEmpty
argument_list|()
operator|)
operator|)
return|;
block|}
comment|/**    * Sets the list of dynamic partitioning keys used for outputting without specifying all the keys    * @param dynamicPartitioningKeys    */
specifier|public
name|void
name|setDynamicPartitioningKeys
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dynamicPartitioningKeys
parameter_list|)
block|{
name|this
operator|.
name|dynamicPartitioningKeys
operator|=
name|dynamicPartitioningKeys
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getDynamicPartitioningKeys
parameter_list|()
block|{
return|return
name|this
operator|.
name|dynamicPartitioningKeys
return|;
block|}
block|}
end_class

end_unit


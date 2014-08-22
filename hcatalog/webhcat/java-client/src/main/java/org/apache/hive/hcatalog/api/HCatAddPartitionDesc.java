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
name|Map
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * The Class HCatAddPartitionDesc helps users in defining partition attributes.  */
end_comment

begin_class
specifier|public
class|class
name|HCatAddPartitionDesc
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HCatAddPartitionDesc
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HCatPartition
name|hcatPartition
decl_stmt|;
comment|// The following data members are only required to support the deprecated constructor (and builder).
name|String
name|dbName
decl_stmt|,
name|tableName
decl_stmt|,
name|location
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionKeyValues
decl_stmt|;
specifier|private
name|HCatAddPartitionDesc
parameter_list|(
name|HCatPartition
name|hcatPartition
parameter_list|)
block|{
name|this
operator|.
name|hcatPartition
operator|=
name|hcatPartition
expr_stmt|;
block|}
specifier|private
name|HCatAddPartitionDesc
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|location
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionKeyValues
parameter_list|)
block|{
name|this
operator|.
name|hcatPartition
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|partitionKeyValues
operator|=
name|partitionKeyValues
expr_stmt|;
block|}
name|HCatPartition
name|getHCatPartition
parameter_list|()
block|{
return|return
name|hcatPartition
return|;
block|}
name|HCatPartition
name|getHCatPartition
parameter_list|(
name|HCatTable
name|hcatTable
parameter_list|)
throws|throws
name|HCatException
block|{
assert|assert
name|hcatPartition
operator|==
literal|null
operator|:
literal|"hcatPartition should have been null at this point."
assert|;
assert|assert
name|dbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|hcatTable
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|:
literal|"DB names don't match."
assert|;
assert|assert
name|tableName
operator|.
name|equalsIgnoreCase
argument_list|(
name|hcatTable
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|:
literal|"Table names don't match."
assert|;
return|return
operator|new
name|HCatPartition
argument_list|(
name|hcatTable
argument_list|,
name|partitionKeyValues
argument_list|,
name|location
argument_list|)
return|;
block|}
comment|/**    * Gets the location.    *    * @return the location    */
annotation|@
name|Deprecated
comment|// @deprecated in favour of {@link HCatPartition.#getLocation()}. To be removed in Hive 0.16.
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|hcatPartition
operator|==
literal|null
condition|?
name|location
else|:
name|hcatPartition
operator|.
name|getLocation
argument_list|()
return|;
block|}
comment|/**    * Gets the partition spec.    *    * @return the partition spec    */
annotation|@
name|Deprecated
comment|// @deprecated in favour of {@link HCatPartition.#getPartitionKeyValMap()}. To be removed in Hive 0.16.
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionSpec
parameter_list|()
block|{
return|return
name|hcatPartition
operator|==
literal|null
condition|?
name|partitionKeyValues
else|:
name|hcatPartition
operator|.
name|getPartitionKeyValMap
argument_list|()
return|;
block|}
comment|/**    * Gets the table name.    *    * @return the table name    */
annotation|@
name|Deprecated
comment|// @deprecated in favour of {@link HCatPartition.#getTableName()}. To be removed in Hive 0.16.
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|hcatPartition
operator|==
literal|null
condition|?
name|tableName
else|:
name|hcatPartition
operator|.
name|getTableName
argument_list|()
return|;
block|}
comment|/**    * Gets the database name.    *    * @return the database name    */
annotation|@
name|Deprecated
comment|// @deprecated in favour of {@link HCatPartition.#getDatabaseName()}. To be removed in Hive 0.16.
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|hcatPartition
operator|==
literal|null
condition|?
name|dbName
else|:
name|hcatPartition
operator|.
name|getDatabaseName
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
literal|"HCatAddPartitionDesc ["
operator|+
name|hcatPartition
operator|+
literal|"]"
return|;
block|}
comment|/**    * Creates the builder for specifying attributes.    *    * @param dbName the db name    * @param tableName the table name    * @param location the location    * @param partSpec the part spec    * @return the builder    * @throws HCatException    */
annotation|@
name|Deprecated
comment|// @deprecated in favour of {@link HCatAddPartitionDesc.#create(HCatPartition)}. To be removed in Hive 0.16.
specifier|public
specifier|static
name|Builder
name|create
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|location
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|HCatException
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unsupported! HCatAddPartitionDesc requires HCatTable to be specified explicitly."
argument_list|)
expr_stmt|;
return|return
operator|new
name|Builder
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|location
argument_list|,
name|partSpec
argument_list|)
return|;
block|}
comment|/**    * Constructs a Builder instance, using an HCatPartition object.    * @param partition An HCatPartition instance.    * @return A Builder object that can build an appropriate HCatAddPartitionDesc.    * @throws HCatException    */
specifier|public
specifier|static
name|Builder
name|create
parameter_list|(
name|HCatPartition
name|partition
parameter_list|)
throws|throws
name|HCatException
block|{
return|return
operator|new
name|Builder
argument_list|(
name|partition
argument_list|)
return|;
block|}
comment|/**    * Builder class for constructing an HCatAddPartition instance.    */
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|HCatPartition
name|hcatPartition
decl_stmt|;
comment|// The following data members are only required to support the deprecated constructor (and builder).
name|String
name|dbName
decl_stmt|,
name|tableName
decl_stmt|,
name|location
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
decl_stmt|;
specifier|private
name|Builder
parameter_list|(
name|HCatPartition
name|hcatPartition
parameter_list|)
block|{
name|this
operator|.
name|hcatPartition
operator|=
name|hcatPartition
expr_stmt|;
block|}
annotation|@
name|Deprecated
comment|// To be removed in Hive 0.16.
specifier|private
name|Builder
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|location
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
block|{
name|this
operator|.
name|hcatPartition
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
block|}
comment|/**      * Builds the HCatAddPartitionDesc.      *      * @return the h cat add partition desc      * @throws HCatException      */
specifier|public
name|HCatAddPartitionDesc
name|build
parameter_list|()
throws|throws
name|HCatException
block|{
return|return
name|hcatPartition
operator|==
literal|null
condition|?
operator|new
name|HCatAddPartitionDesc
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|location
argument_list|,
name|partitionSpec
argument_list|)
else|:
operator|new
name|HCatAddPartitionDesc
argument_list|(
name|hcatPartition
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


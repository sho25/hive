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
name|io
operator|.
name|Serializable
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
name|HCatUtil
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
name|HCatSchema
import|;
end_import

begin_comment
comment|/**  *  * HCatTableInfo - class to communicate table information to {@link HCatInputFormat}  * and {@link HCatOutputFormat}  *  */
end_comment

begin_class
specifier|public
class|class
name|HCatTableInfo
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/** The db and table names */
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
comment|/** The table schema. */
specifier|private
specifier|final
name|HCatSchema
name|dataColumns
decl_stmt|;
specifier|private
specifier|final
name|HCatSchema
name|partitionColumns
decl_stmt|;
comment|/** The table being written to */
specifier|private
specifier|final
name|Table
name|table
decl_stmt|;
comment|/** The storer info */
specifier|private
name|StorerInfo
name|storerInfo
decl_stmt|;
comment|/**      * Initializes a new HCatTableInfo instance to be used with {@link HCatInputFormat}      * for reading data from a table.      * work with hadoop security, the kerberos principal name of the server - else null      * The principal name should be of the form:      *<servicename>/_HOST@<realm> like "hcat/_HOST@myrealm.com"      * The special string _HOST will be replaced automatically with the correct host name      * @param databaseName the db name      * @param tableName the table name      * @param dataColumns schema of columns which contain data      * @param partitionColumns schema of partition columns      * @param storerInfo information about storage descriptor      * @param table hive metastore table class      */
name|HCatTableInfo
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|HCatSchema
name|dataColumns
parameter_list|,
name|HCatSchema
name|partitionColumns
parameter_list|,
name|StorerInfo
name|storerInfo
parameter_list|,
name|Table
name|table
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
name|dataColumns
operator|=
name|dataColumns
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|storerInfo
operator|=
name|storerInfo
expr_stmt|;
name|this
operator|.
name|partitionColumns
operator|=
name|partitionColumns
expr_stmt|;
block|}
comment|/**      * Gets the value of databaseName      * @return the databaseName      */
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|databaseName
return|;
block|}
comment|/**      * Gets the value of tableName      * @return the tableName      */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**      * @return return schema of data columns as defined in meta store      */
specifier|public
name|HCatSchema
name|getDataColumns
parameter_list|()
block|{
return|return
name|dataColumns
return|;
block|}
comment|/**      * @return schema of partition columns      */
specifier|public
name|HCatSchema
name|getPartitionColumns
parameter_list|()
block|{
return|return
name|partitionColumns
return|;
block|}
comment|/**      * @return the storerInfo      */
specifier|public
name|StorerInfo
name|getStorerInfo
parameter_list|()
block|{
return|return
name|storerInfo
return|;
block|}
specifier|public
name|String
name|getTableLocation
parameter_list|()
block|{
return|return
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
return|;
block|}
comment|/**      * minimize dependency on hive classes so this is package private      * this should eventually no longer be used      * @return hive metastore representation of table      */
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
comment|/**      * create an HCatTableInfo instance from the supplied Hive Table instance      * @param table to create an instance from      * @return HCatTableInfo      * @throws IOException      */
specifier|static
name|HCatTableInfo
name|valueOf
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Explicitly use {@link org.apache.hadoop.hive.ql.metadata.Table} when getting the schema,
comment|// but store @{link org.apache.hadoop.hive.metastore.api.Table} as this class is serialized
comment|// into the job conf.
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Table
name|mTable
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|Table
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|HCatSchema
name|schema
init|=
name|HCatUtil
operator|.
name|extractSchema
argument_list|(
name|mTable
argument_list|)
decl_stmt|;
name|StorerInfo
name|storerInfo
init|=
name|InternalUtil
operator|.
name|extractStorerInfo
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|HCatSchema
name|partitionColumns
init|=
name|HCatUtil
operator|.
name|getPartitionColumns
argument_list|(
name|mTable
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatTableInfo
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|schema
argument_list|,
name|partitionColumns
argument_list|,
name|storerInfo
argument_list|,
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|HCatTableInfo
name|tableInfo
init|=
operator|(
name|HCatTableInfo
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|dataColumns
operator|!=
literal|null
condition|?
operator|!
name|dataColumns
operator|.
name|equals
argument_list|(
name|tableInfo
operator|.
name|dataColumns
argument_list|)
else|:
name|tableInfo
operator|.
name|dataColumns
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|databaseName
operator|!=
literal|null
condition|?
operator|!
name|databaseName
operator|.
name|equals
argument_list|(
name|tableInfo
operator|.
name|databaseName
argument_list|)
else|:
name|tableInfo
operator|.
name|databaseName
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|partitionColumns
operator|!=
literal|null
condition|?
operator|!
name|partitionColumns
operator|.
name|equals
argument_list|(
name|tableInfo
operator|.
name|partitionColumns
argument_list|)
else|:
name|tableInfo
operator|.
name|partitionColumns
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|storerInfo
operator|!=
literal|null
condition|?
operator|!
name|storerInfo
operator|.
name|equals
argument_list|(
name|tableInfo
operator|.
name|storerInfo
argument_list|)
else|:
name|tableInfo
operator|.
name|storerInfo
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|table
operator|!=
literal|null
condition|?
operator|!
name|table
operator|.
name|equals
argument_list|(
name|tableInfo
operator|.
name|table
argument_list|)
else|:
name|tableInfo
operator|.
name|table
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|?
operator|!
name|tableName
operator|.
name|equals
argument_list|(
name|tableInfo
operator|.
name|tableName
argument_list|)
else|:
name|tableInfo
operator|.
name|tableName
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|databaseName
operator|!=
literal|null
condition|?
name|databaseName
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|tableName
operator|!=
literal|null
condition|?
name|tableName
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|dataColumns
operator|!=
literal|null
condition|?
name|dataColumns
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|partitionColumns
operator|!=
literal|null
condition|?
name|partitionColumns
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|table
operator|!=
literal|null
condition|?
name|table
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|storerInfo
operator|!=
literal|null
condition|?
name|storerInfo
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit


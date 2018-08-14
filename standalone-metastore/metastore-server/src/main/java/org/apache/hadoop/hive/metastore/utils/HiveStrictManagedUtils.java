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
name|utils
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|MetastoreConf
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
name|MetaException
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|TableType
import|;
end_import

begin_class
specifier|public
class|class
name|HiveStrictManagedUtils
block|{
specifier|public
specifier|static
name|void
name|validateStrictManagedTableWithThrow
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|String
name|reason
init|=
name|validateStrictManagedTable
argument_list|(
name|conf
argument_list|,
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|reason
argument_list|)
throw|;
block|}
block|}
comment|/**    * Checks if the table is valid based on the rules for strict managed tables.    * @param conf    * @param table    * @return  Null if the table is valid, otherwise a string message indicating why the table is invalid.    */
specifier|public
specifier|static
name|String
name|validateStrictManagedTable
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Table
name|table
parameter_list|)
block|{
if|if
condition|(
name|MetastoreConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|STRICT_MANAGED_TABLES
argument_list|)
condition|)
block|{
if|if
condition|(
name|table
operator|.
name|isTemporary
argument_list|()
condition|)
block|{
comment|// temp tables exempted from checks.
return|return
literal|null
return|;
block|}
name|TableType
name|tableType
init|=
name|TableType
operator|.
name|valueOf
argument_list|(
name|table
operator|.
name|getTableType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableType
operator|==
name|TableType
operator|.
name|MANAGED_TABLE
condition|)
block|{
if|if
condition|(
operator|!
name|MetaStoreServerUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|createValidationError
argument_list|(
name|table
argument_list|,
literal|"Table is marked as a managed table but is not transactional."
argument_list|)
return|;
block|}
if|if
condition|(
name|MetaStoreUtils
operator|.
name|isNonNativeTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
name|createValidationError
argument_list|(
name|table
argument_list|,
literal|"Table is marked as a managed table but is non-native."
argument_list|)
return|;
block|}
if|if
condition|(
name|isAvroTableWithExternalSchema
argument_list|(
name|table
argument_list|)
condition|)
block|{
return|return
name|createValidationError
argument_list|(
name|table
argument_list|,
literal|"Managed Avro table has externally defined schema."
argument_list|)
return|;
block|}
block|}
block|}
comment|// Table is valid
return|return
literal|null
return|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|AVRO_SERDE_CLASSNAME
init|=
literal|"org.apache.hadoop.hive.serde2.avro.AvroSerDe"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|AVRO_SCHEMA_URL_PROPERTY
init|=
literal|"avro.schema.url"
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|isAvroTableWithExternalSchema
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
if|if
condition|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
operator|.
name|equals
argument_list|(
name|AVRO_SERDE_CLASSNAME
argument_list|)
condition|)
block|{
name|String
name|schemaUrl
init|=
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|AVRO_SCHEMA_URL_PROPERTY
argument_list|)
decl_stmt|;
if|if
condition|(
name|schemaUrl
operator|!=
literal|null
operator|&&
operator|!
name|schemaUrl
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isListBucketedTable
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
return|return
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|isStoredAsSubDirectories
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|createValidationError
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|message
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Table "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" failed strict managed table checks due to the following reason: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|message
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit


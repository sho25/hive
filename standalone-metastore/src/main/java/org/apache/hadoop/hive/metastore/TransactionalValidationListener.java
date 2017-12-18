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
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|LocatedFileStatus
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
name|fs
operator|.
name|Path
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
name|fs
operator|.
name|RemoteIterator
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
name|InvalidOperationException
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
name|NoSuchObjectException
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
name|api
operator|.
name|hive_metastoreConstants
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
name|events
operator|.
name|PreAlterTableEvent
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
name|events
operator|.
name|PreCreateTableEvent
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
name|events
operator|.
name|PreEventContext
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
name|txn
operator|.
name|TxnUtils
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
name|utils
operator|.
name|MetaStoreUtils
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

begin_class
specifier|public
specifier|final
class|class
name|TransactionalValidationListener
extends|extends
name|MetaStorePreEventListener
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TransactionalValidationListener
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// These constants are also imported by org.apache.hadoop.hive.ql.io.AcidUtils.
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_TRANSACTIONAL_PROPERTY
init|=
literal|"default"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INSERTONLY_TRANSACTIONAL_PROPERTY
init|=
literal|"insert_only"
decl_stmt|;
name|TransactionalValidationListener
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onEvent
parameter_list|(
name|PreEventContext
name|context
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidOperationException
block|{
switch|switch
condition|(
name|context
operator|.
name|getEventType
argument_list|()
condition|)
block|{
case|case
name|CREATE_TABLE
case|:
name|handle
argument_list|(
operator|(
name|PreCreateTableEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|ALTER_TABLE
case|:
name|handle
argument_list|(
operator|(
name|PreAlterTableEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|//no validation required..
block|}
block|}
specifier|private
name|void
name|handle
parameter_list|(
name|PreAlterTableEvent
name|context
parameter_list|)
throws|throws
name|MetaException
block|{
name|handleAlterTableTransactionalProp
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|handle
parameter_list|(
name|PreCreateTableEvent
name|context
parameter_list|)
throws|throws
name|MetaException
block|{
name|handleCreateTableTransactionalProp
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**    * once a table is marked transactional, you cannot go back.  Enforce this.    * Also in current version, 'transactional_properties' of the table cannot be altered after    * the table is created. Any attempt to alter it will throw a MetaException.    */
specifier|private
name|void
name|handleAlterTableTransactionalProp
parameter_list|(
name|PreAlterTableEvent
name|context
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|newTable
init|=
name|context
operator|.
name|getNewTable
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|newTable
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|==
literal|null
operator|||
name|parameters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|parameters
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|transactionalValue
init|=
literal|null
decl_stmt|;
name|boolean
name|transactionalValuePresent
init|=
literal|false
decl_stmt|;
name|boolean
name|isTransactionalPropertiesPresent
init|=
literal|false
decl_stmt|;
name|String
name|transactionalPropertiesValue
init|=
literal|null
decl_stmt|;
name|boolean
name|hasValidTransactionalValue
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|transactionalValuePresent
operator|=
literal|true
expr_stmt|;
name|transactionalValue
operator|=
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|isTransactionalPropertiesPresent
operator|=
literal|true
expr_stmt|;
name|transactionalPropertiesValue
operator|=
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
comment|// Do not remove the parameter yet, because we have separate initialization routine
comment|// that will use it down below.
block|}
block|}
name|Table
name|oldTable
init|=
name|context
operator|.
name|getOldTable
argument_list|()
decl_stmt|;
name|String
name|oldTransactionalValue
init|=
literal|null
decl_stmt|;
name|String
name|oldTransactionalPropertiesValue
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|oldTable
operator|.
name|getParameters
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|oldTransactionalValue
operator|=
name|oldTable
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|oldTransactionalPropertiesValue
operator|=
name|oldTable
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|transactionalValuePresent
operator|&&
literal|"false"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactionalValue
argument_list|)
condition|)
block|{
name|transactionalValuePresent
operator|=
literal|false
expr_stmt|;
name|transactionalValue
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|transactionalValuePresent
condition|)
block|{
comment|//normalize prop name
name|parameters
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
name|transactionalValue
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactionalValue
argument_list|)
operator|&&
operator|!
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|oldTransactionalValue
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|isTransactionalPropertiesPresent
condition|)
block|{
name|normazlieTransactionalPropertyDefault
argument_list|(
name|newTable
argument_list|)
expr_stmt|;
name|isTransactionalPropertiesPresent
operator|=
literal|true
expr_stmt|;
name|transactionalPropertiesValue
operator|=
name|DEFAULT_TRANSACTIONAL_PROPERTY
expr_stmt|;
block|}
comment|//only need to check conformance if alter table enabled acid
if|if
condition|(
operator|!
name|conformToAcid
argument_list|(
name|newTable
argument_list|)
condition|)
block|{
comment|// INSERT_ONLY tables don't have to conform to ACID requirement like ORC or bucketing
if|if
condition|(
name|transactionalPropertiesValue
operator|==
literal|null
operator|||
operator|!
literal|"insert_only"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactionalPropertiesValue
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The table must be stored using an ACID compliant format (such as ORC)"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|newTable
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|newTable
argument_list|)
operator|+
literal|" cannot be declared transactional because it's an external table"
argument_list|)
throw|;
block|}
name|validateTableStructure
argument_list|(
name|context
operator|.
name|getHandler
argument_list|()
argument_list|,
name|newTable
argument_list|)
expr_stmt|;
name|hasValidTransactionalValue
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|oldTransactionalValue
operator|==
literal|null
condition|?
name|transactionalValue
operator|==
literal|null
else|:
name|oldTransactionalValue
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactionalValue
argument_list|)
condition|)
block|{
comment|//this covers backward compat cases where this prop may have been set already
name|hasValidTransactionalValue
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|hasValidTransactionalValue
operator|&&
operator|!
name|MetaStoreUtils
operator|.
name|isInsertOnlyTableParam
argument_list|(
name|oldTable
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
comment|// if here, there is attempt to set transactional to something other than 'true'
comment|// and NOT the same value it was before
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"TBLPROPERTIES with 'transactional'='true' cannot be unset"
argument_list|)
throw|;
block|}
if|if
condition|(
name|isTransactionalPropertiesPresent
condition|)
block|{
comment|// Now validate transactional_properties for the table.
if|if
condition|(
name|oldTransactionalValue
operator|==
literal|null
condition|)
block|{
comment|// If this is the first time the table is being initialized to 'transactional=true',
comment|// any valid value can be set for the 'transactional_properties'.
name|initializeTransactionalProperties
argument_list|(
name|newTable
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// If the table was already marked as 'transactional=true', then the new value of
comment|// 'transactional_properties' must match the old value. Any attempt to alter the previous
comment|// value will throw an error. An exception will still be thrown if the previous value was
comment|// null and an attempt is made to set it. This behaviour can be changed in the future.
if|if
condition|(
operator|(
name|oldTransactionalPropertiesValue
operator|==
literal|null
operator|||
operator|!
name|oldTransactionalPropertiesValue
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactionalPropertiesValue
argument_list|)
operator|)
operator|&&
operator|!
name|MetaStoreUtils
operator|.
name|isInsertOnlyTableParam
argument_list|(
name|oldTable
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"TBLPROPERTIES with 'transactional_properties' cannot be "
operator|+
literal|"altered after the table is created"
argument_list|)
throw|;
block|}
block|}
block|}
name|checkSorted
argument_list|(
name|newTable
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkSorted
parameter_list|(
name|Table
name|newTable
parameter_list|)
throws|throws
name|MetaException
block|{
if|if
condition|(
operator|!
name|TxnUtils
operator|.
name|isAcidTable
argument_list|(
name|newTable
argument_list|)
condition|)
block|{
return|return;
block|}
name|StorageDescriptor
name|sd
init|=
name|newTable
operator|.
name|getSd
argument_list|()
decl_stmt|;
if|if
condition|(
name|sd
operator|.
name|getSortCols
argument_list|()
operator|!=
literal|null
operator|&&
name|sd
operator|.
name|getSortCols
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Table "
operator|+
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|newTable
argument_list|)
operator|+
literal|" cannot support full ACID functionality since it is sorted."
argument_list|)
throw|;
block|}
block|}
comment|/**    * Normalize case and make sure:    * 1. 'true' is the only value to be set for 'transactional' (if set at all)    * 2. If set to 'true', we should also enforce bucketing and ORC format    */
specifier|private
name|void
name|handleCreateTableTransactionalProp
parameter_list|(
name|PreCreateTableEvent
name|context
parameter_list|)
throws|throws
name|MetaException
block|{
name|Table
name|newTable
init|=
name|context
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|newTable
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|==
literal|null
operator|||
name|parameters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|String
name|transactional
init|=
literal|null
decl_stmt|;
name|String
name|transactionalProperties
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|parameters
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
comment|// Get the "transactional" tblproperties value
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|transactional
operator|=
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|parameters
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|// Get the "transactional_properties" tblproperties value
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|transactionalProperties
operator|=
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|transactional
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
literal|"false"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactional
argument_list|)
condition|)
block|{
comment|// just drop transactional=false.  For backward compatibility in case someone has scripts
comment|// with transactional=false
name|LOG
operator|.
name|info
argument_list|(
literal|"'transactional'='false' is no longer a valid property and will be ignored"
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactional
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|conformToAcid
argument_list|(
name|newTable
argument_list|)
condition|)
block|{
comment|// INSERT_ONLY tables don't have to conform to ACID requirement like ORC or bucketing
if|if
condition|(
name|transactionalProperties
operator|==
literal|null
operator|||
operator|!
literal|"insert_only"
operator|.
name|equalsIgnoreCase
argument_list|(
name|transactionalProperties
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The table must be stored using an ACID compliant format (such as ORC)"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|newTable
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|newTable
argument_list|)
operator|+
literal|" cannot be declared transactional because it's an external table"
argument_list|)
throw|;
block|}
comment|// normalize prop name
name|parameters
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|transactionalProperties
operator|==
literal|null
condition|)
block|{
name|normazlieTransactionalPropertyDefault
argument_list|(
name|newTable
argument_list|)
expr_stmt|;
block|}
name|initializeTransactionalProperties
argument_list|(
name|newTable
argument_list|)
expr_stmt|;
name|checkSorted
argument_list|(
name|newTable
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// transactional is found, but the value is not in expected range
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"'transactional' property of TBLPROPERTIES may only have value 'true'"
argument_list|)
throw|;
block|}
comment|/**    * When a table is marked transactional=true but transactional_properties is not set then    * transactional_properties should take on the default value.  Easier to make this explicit in    * table definition than keep checking everywhere if it's set or not.    */
specifier|private
name|void
name|normazlieTransactionalPropertyDefault
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
argument_list|,
name|DEFAULT_TRANSACTIONAL_PROPERTY
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that InputFormatClass/OutputFormatClass should implement    * AcidInputFormat/AcidOutputFormat    */
specifier|private
name|boolean
name|conformToAcid
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|StorageDescriptor
name|sd
init|=
name|table
operator|.
name|getSd
argument_list|()
decl_stmt|;
try|try
block|{
name|Class
name|inputFormatClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|sd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
decl_stmt|;
name|Class
name|outputFormatClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|sd
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputFormatClass
operator|==
literal|null
operator|||
name|outputFormatClass
operator|==
literal|null
operator|||
operator|!
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hive.ql.io.AcidInputFormat"
argument_list|)
operator|.
name|isAssignableFrom
argument_list|(
name|inputFormatClass
argument_list|)
operator|||
operator|!
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hive.ql.io.AcidOutputFormat"
argument_list|)
operator|.
name|isAssignableFrom
argument_list|(
name|outputFormatClass
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Invalid input/output format for table"
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|initializeTransactionalProperties
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
comment|// All new versions of Acid tables created after the introduction of Acid version/type system
comment|// can have TRANSACTIONAL_PROPERTIES property defined. This parameter can be used to change
comment|// the operational behavior of ACID. However if this parameter is not defined, the new Acid
comment|// tables will still behave as the old ones. This is done so to preserve the behavior
comment|// in case of rolling downgrade.
comment|// Initialize transaction table properties with default string value.
name|String
name|tableTransactionalProperties
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|table
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|keys
init|=
name|parameters
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
if|if
condition|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
operator|.
name|equalsIgnoreCase
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|tableTransactionalProperties
operator|=
name|parameters
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|parameters
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|String
name|validationError
init|=
name|validateTransactionalProperties
argument_list|(
name|tableTransactionalProperties
argument_list|)
decl_stmt|;
if|if
condition|(
name|validationError
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"Invalid transactional properties specified for the "
operator|+
literal|"table with the error "
operator|+
name|validationError
argument_list|)
throw|;
block|}
break|break;
block|}
block|}
block|}
if|if
condition|(
name|tableTransactionalProperties
operator|!=
literal|null
condition|)
block|{
name|parameters
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_TRANSACTIONAL_PROPERTIES
argument_list|,
name|tableTransactionalProperties
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|validateTransactionalProperties
parameter_list|(
name|String
name|transactionalProperties
parameter_list|)
block|{
name|boolean
name|isValid
init|=
literal|false
decl_stmt|;
switch|switch
condition|(
name|transactionalProperties
condition|)
block|{
case|case
name|DEFAULT_TRANSACTIONAL_PROPERTY
case|:
case|case
name|INSERTONLY_TRANSACTIONAL_PROPERTY
case|:
name|isValid
operator|=
literal|true
expr_stmt|;
break|break;
default|default:
name|isValid
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isValid
condition|)
block|{
return|return
literal|"unknown value "
operator|+
name|transactionalProperties
operator|+
literal|" for transactional_properties"
return|;
block|}
return|return
literal|null
return|;
comment|// All checks passed, return null.
block|}
specifier|private
specifier|final
name|Pattern
name|ORIGINAL_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[0-9]+_[0-9]+"
argument_list|)
decl_stmt|;
comment|/**    * @see org.apache.hadoop.hive.ql.exec.Utilities#COPY_KEYWORD    */
specifier|private
specifier|static
specifier|final
name|Pattern
name|ORIGINAL_PATTERN_COPY
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[0-9]+_[0-9]+"
operator|+
literal|"_copy_"
operator|+
literal|"[0-9]+"
argument_list|)
decl_stmt|;
comment|/**    * It's assumed everywhere that original data files are named according to    * {@link #ORIGINAL_PATTERN} or{@link #ORIGINAL_PATTERN_COPY}    * This checks that when transaction=true is set and throws if it finds any files that don't    * follow convention.    */
specifier|private
name|void
name|validateTableStructure
parameter_list|(
name|IHMSHandler
name|hmsHandler
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|MetaException
block|{
name|Path
name|tablePath
decl_stmt|;
try|try
block|{
name|Warehouse
name|wh
init|=
name|hmsHandler
operator|.
name|getWh
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
operator|||
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tablePath
operator|=
name|wh
operator|.
name|getDefaultTablePath
argument_list|(
name|hmsHandler
operator|.
name|getMS
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tablePath
operator|=
name|wh
operator|.
name|getDnsPath
argument_list|(
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FileSystem
name|fs
init|=
name|wh
operator|.
name|getFs
argument_list|(
name|tablePath
argument_list|)
decl_stmt|;
comment|//FileSystem fs = FileSystem.get(getConf());
name|RemoteIterator
argument_list|<
name|LocatedFileStatus
argument_list|>
name|iterator
init|=
name|fs
operator|.
name|listFiles
argument_list|(
name|tablePath
argument_list|,
literal|true
argument_list|)
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|LocatedFileStatus
name|fileStatus
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fileStatus
operator|.
name|isFile
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|boolean
name|validFile
init|=
operator|(
name|ORIGINAL_PATTERN
operator|.
name|matcher
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
operator|||
name|ORIGINAL_PATTERN_COPY
operator|.
name|matcher
argument_list|(
name|fileStatus
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
operator|)
decl_stmt|;
if|if
condition|(
operator|!
name|validFile
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unexpected data file name format.  Cannot convert "
operator|+
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|table
argument_list|)
operator|+
literal|" to transactional table.  File: "
operator|+
name|fileStatus
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|NoSuchObjectException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Unable to list files for "
operator|+
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|MetaException
name|e1
init|=
operator|new
name|MetaException
argument_list|(
name|msg
argument_list|)
decl_stmt|;
name|e1
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e1
throw|;
block|}
block|}
block|}
end_class

end_unit


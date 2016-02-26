begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|api
operator|.
name|*
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

begin_class
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
comment|/**    * once a table is marked transactional, you cannot go back.  Enforce this.    */
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
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The table must be bucketed and stored using an ACID compliant"
operator|+
literal|" format (such as ORC)"
argument_list|)
throw|;
block|}
return|return;
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
return|return;
block|}
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
name|transactionalValue
init|=
literal|null
decl_stmt|;
name|boolean
name|transactionalPropFound
init|=
literal|false
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
name|transactionalPropFound
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
block|}
if|if
condition|(
operator|!
name|transactionalPropFound
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
name|transactionalValue
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
name|transactionalValue
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
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"The table must be bucketed and stored using an ACID compliant"
operator|+
literal|" format (such as ORC)"
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
return|return;
block|}
comment|// transactional prop is found, but the value is not in expected range
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"'transactional' property of TBLPROPERTIES may only have value 'true'"
argument_list|)
throw|;
block|}
comment|// Check if table is bucketed and InputFormatClass/OutputFormatClass should implement
comment|// AcidInputFormat/AcidOutputFormat
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
if|if
condition|(
name|sd
operator|.
name|getBucketColsSize
argument_list|()
operator|<
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
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
block|}
end_class

end_unit


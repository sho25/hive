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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|storage
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|collections
operator|.
name|MapUtils
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
name|HiveMetaStoreUtils
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
name|ql
operator|.
name|ErrorMsg
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperationContext
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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|AlterTableUtils
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
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|AbstractAlterTableOperation
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
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|OrcSerde
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
name|ql
operator|.
name|metadata
operator|.
name|Hive
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|metadata
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
name|ql
operator|.
name|metadata
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
name|serde2
operator|.
name|Deserializer
import|;
end_import

begin_comment
comment|/**  * Operation process of setting the serde.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableSetSerdeOperation
extends|extends
name|AbstractAlterTableOperation
block|{
specifier|private
specifier|final
name|AlterTableSetSerdeDesc
name|desc
decl_stmt|;
specifier|public
name|AlterTableSetSerdeOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableSetSerdeDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doAlteration
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|HiveException
block|{
name|StorageDescriptor
name|sd
init|=
name|getStorageDescriptor
argument_list|(
name|table
argument_list|,
name|partition
argument_list|)
decl_stmt|;
name|String
name|serdeName
init|=
name|desc
operator|.
name|getSerdeName
argument_list|()
decl_stmt|;
name|String
name|oldSerdeName
init|=
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
decl_stmt|;
comment|// if orc table, restrict changing the serde as it can break schema evolution
if|if
condition|(
name|AlterTableUtils
operator|.
name|isSchemaEvolutionEnabled
argument_list|(
name|table
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
operator|&&
name|oldSerdeName
operator|.
name|equalsIgnoreCase
argument_list|(
name|OrcSerde
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|&&
operator|!
name|serdeName
operator|.
name|equalsIgnoreCase
argument_list|(
name|OrcSerde
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ErrorMsg
operator|.
name|CANNOT_CHANGE_SERDE
argument_list|,
name|OrcSerde
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
throw|;
block|}
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|setSerializationLib
argument_list|(
name|serdeName
argument_list|)
expr_stmt|;
if|if
condition|(
name|MapUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getProps
argument_list|()
argument_list|)
condition|)
block|{
name|sd
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|putAll
argument_list|(
name|desc
operator|.
name|getProps
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|partition
operator|!=
literal|null
condition|)
block|{
comment|// TODO: wtf? This doesn't do anything.
name|partition
operator|.
name|getTPartition
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|setCols
argument_list|(
name|partition
operator|.
name|getTPartition
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|Table
operator|.
name|shouldStoreFieldsInMetastore
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|serdeName
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|&&
operator|!
name|Table
operator|.
name|hasMetastoreBasedSchema
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|oldSerdeName
argument_list|)
condition|)
block|{
comment|// If new SerDe needs to store fields in metastore, but the old serde doesn't, save
comment|// the fields so that new SerDe could operate. Note that this may fail if some fields
comment|// from old SerDe are too long to be stored in metastore, but there's nothing we can do.
try|try
block|{
name|Deserializer
name|oldSerde
init|=
name|HiveMetaStoreUtils
operator|.
name|getDeserializer
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|table
operator|.
name|getTTable
argument_list|()
argument_list|,
literal|false
argument_list|,
name|oldSerdeName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setFields
argument_list|(
name|Hive
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|oldSerde
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit


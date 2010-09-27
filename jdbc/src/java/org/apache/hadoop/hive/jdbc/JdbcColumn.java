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
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_comment
comment|/**  * Column metadata.  */
end_comment

begin_class
specifier|public
class|class
name|JdbcColumn
block|{
specifier|private
specifier|final
name|String
name|columnName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableCatalog
decl_stmt|;
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
specifier|private
specifier|final
name|String
name|comment
decl_stmt|;
specifier|private
specifier|final
name|int
name|ordinalPos
decl_stmt|;
name|JdbcColumn
parameter_list|(
name|String
name|columnName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|tableCatalog
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|comment
parameter_list|,
name|int
name|ordinalPos
parameter_list|)
block|{
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|tableCatalog
operator|=
name|tableCatalog
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|ordinalPos
operator|=
name|ordinalPos
expr_stmt|;
block|}
specifier|public
name|String
name|getColumnName
parameter_list|()
block|{
return|return
name|columnName
return|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|String
name|getTableCatalog
parameter_list|()
block|{
return|return
name|tableCatalog
return|;
block|}
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|Integer
name|getSqlType
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|HiveResultSetMetaData
operator|.
name|hiveTypeToSqlType
argument_list|(
name|type
argument_list|)
return|;
block|}
specifier|public
name|Integer
name|getColumnSize
parameter_list|()
block|{
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tinyint"
argument_list|)
condition|)
block|{
return|return
literal|3
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"smallint"
argument_list|)
condition|)
block|{
return|return
literal|5
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"bigint"
argument_list|)
condition|)
block|{
return|return
literal|19
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
return|return
literal|12
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
argument_list|)
condition|)
block|{
return|return
literal|22
return|;
block|}
else|else
block|{
comment|// anything else including boolean is null
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|Integer
name|getNumPrecRadix
parameter_list|()
block|{
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tinyint"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"smallint"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"bigint"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
return|return
literal|2
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
argument_list|)
condition|)
block|{
return|return
literal|2
return|;
block|}
else|else
block|{
comment|// anything else including boolean and string is null
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|Integer
name|getDecimalDigits
parameter_list|()
block|{
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tinyint"
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"smallint"
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"bigint"
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
comment|// anything else including float and double is null
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
specifier|public
name|int
name|getOrdinalPos
parameter_list|()
block|{
return|return
name|ordinalPos
return|;
block|}
block|}
end_class

end_unit


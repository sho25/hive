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
name|ResultSetMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|thrift
operator|.
name|Type
import|;
end_import

begin_comment
comment|/**  * HiveResultSetMetaData.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveResultSetMetaData
implements|implements
name|java
operator|.
name|sql
operator|.
name|ResultSetMetaData
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|JdbcColumnAttributes
argument_list|>
name|columnAttributes
decl_stmt|;
specifier|public
name|HiveResultSetMetaData
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
parameter_list|,
name|List
argument_list|<
name|JdbcColumnAttributes
argument_list|>
name|columnAttributes
parameter_list|)
block|{
name|this
operator|.
name|columnNames
operator|=
name|columnNames
expr_stmt|;
name|this
operator|.
name|columnTypes
operator|=
name|columnTypes
expr_stmt|;
name|this
operator|.
name|columnAttributes
operator|=
name|columnAttributes
expr_stmt|;
block|}
specifier|public
name|String
name|getCatalogName
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|private
name|Type
name|getHiveType
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|JdbcColumn
operator|.
name|typeStringToHiveType
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getColumnClassName
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|JdbcColumn
operator|.
name|columnClassName
argument_list|(
name|getHiveType
argument_list|(
name|column
argument_list|)
argument_list|,
name|columnAttributes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getColumnCount
parameter_list|()
throws|throws
name|SQLException
block|{
return|return
name|columnNames
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|int
name|getColumnDisplaySize
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|JdbcColumn
operator|.
name|columnDisplaySize
argument_list|(
name|getHiveType
argument_list|(
name|column
argument_list|)
argument_list|,
name|columnAttributes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getColumnLabel
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|columnNames
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getColumnName
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|columnNames
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getColumnType
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// we need to convert the thrift type to the SQL type
name|String
name|type
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
comment|// we need to convert the thrift type to the SQL type
return|return
name|JdbcColumn
operator|.
name|hiveTypeToSqlType
argument_list|(
name|type
argument_list|)
return|;
block|}
specifier|public
name|String
name|getColumnTypeName
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|JdbcColumn
operator|.
name|getColumnTypeName
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getPrecision
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|JdbcColumn
operator|.
name|columnPrecision
argument_list|(
name|getHiveType
argument_list|(
name|column
argument_list|)
argument_list|,
name|columnAttributes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getScale
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|JdbcColumn
operator|.
name|columnScale
argument_list|(
name|getHiveType
argument_list|(
name|column
argument_list|)
argument_list|,
name|columnAttributes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|getSchemaName
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isAutoIncrement
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// Hive doesn't have an auto-increment concept
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|isCaseSensitive
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// we need to convert the Hive type to the SQL type name
comment|// TODO: this would be better handled in an enum
name|String
name|type
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|toZeroIndex
argument_list|(
name|column
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"string"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|boolean
name|isCurrency
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// Hive doesn't support a currency type
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|isDefinitelyWritable
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
name|int
name|isNullable
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// Hive doesn't have the concept of not-null
return|return
name|ResultSetMetaData
operator|.
name|columnNullable
return|;
block|}
specifier|public
name|boolean
name|isReadOnly
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|isSearchable
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isSigned
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isWritable
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isWrapperFor
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|iface
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|iface
parameter_list|)
throws|throws
name|SQLException
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Method not supported"
argument_list|)
throw|;
block|}
specifier|protected
name|int
name|toZeroIndex
parameter_list|(
name|int
name|column
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|columnTypes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not determine column type name for ResultSet"
argument_list|)
throw|;
block|}
if|if
condition|(
name|column
argument_list|<
literal|1
operator|||
name|column
argument_list|>
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Invalid column value: "
operator|+
name|column
argument_list|)
throw|;
block|}
return|return
name|column
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit


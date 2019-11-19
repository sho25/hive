begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/*  * This source file is based on code taken from SQLLine 1.0.2  * See SQLLine notice in LICENSE  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|beeline
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

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
name|text
operator|.
name|DecimalFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|NumberFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|common
operator|.
name|cli
operator|.
name|EscapeCRLFHelper
import|;
end_import

begin_comment
comment|/**  * Abstract base class representing a set of rows to be displayed.  * Holds column values as strings  */
end_comment

begin_class
specifier|abstract
class|class
name|Rows
implements|implements
name|Iterator
block|{
specifier|protected
specifier|final
name|BeeLine
name|beeLine
decl_stmt|;
specifier|final
name|ResultSetMetaData
name|rsMeta
decl_stmt|;
specifier|final
name|Boolean
index|[]
name|primaryKeys
decl_stmt|;
specifier|final
name|NumberFormat
name|numberFormat
decl_stmt|;
specifier|private
name|boolean
name|convertBinaryArray
decl_stmt|;
specifier|private
specifier|final
name|String
name|nullStr
decl_stmt|;
name|Rows
parameter_list|(
name|BeeLine
name|beeLine
parameter_list|,
name|ResultSet
name|rs
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|beeLine
operator|=
name|beeLine
expr_stmt|;
name|nullStr
operator|=
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getNullString
argument_list|()
expr_stmt|;
name|rsMeta
operator|=
name|rs
operator|.
name|getMetaData
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|rsMeta
operator|.
name|getColumnCount
argument_list|()
decl_stmt|;
name|primaryKeys
operator|=
operator|new
name|Boolean
index|[
name|count
index|]
expr_stmt|;
if|if
condition|(
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getNumberFormat
argument_list|()
operator|.
name|equals
argument_list|(
literal|"default"
argument_list|)
condition|)
block|{
name|numberFormat
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|numberFormat
operator|=
operator|new
name|DecimalFormat
argument_list|(
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getNumberFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|convertBinaryArray
operator|=
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getConvertBinaryArrayToString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**    * Update all of the rows to have the same size, set to the    * maximum length of each column in the Rows.    */
specifier|abstract
name|void
name|normalizeWidths
parameter_list|()
function_decl|;
comment|/**    * Return whether the specified column (0-based index) is    * a primary key. Since this method depends on whether the    * JDBC driver property implements {@link ResultSetMetaData#getTableName} (many do not), it    * is not reliable for all databases.    */
name|boolean
name|isPrimaryKey
parameter_list|(
name|int
name|col
parameter_list|)
block|{
if|if
condition|(
name|primaryKeys
index|[
name|col
index|]
operator|==
literal|null
condition|)
block|{
try|try
block|{
comment|// this doesn't always work, since some JDBC drivers (e.g.,
comment|// Oracle's) return a blank string from getDbTableName.
name|String
name|table
init|=
name|rsMeta
operator|.
name|getTableName
argument_list|(
name|col
operator|+
literal|1
argument_list|)
decl_stmt|;
name|String
name|column
init|=
name|rsMeta
operator|.
name|getColumnName
argument_list|(
name|col
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
operator|||
name|table
operator|.
name|isEmpty
argument_list|()
operator|||
name|column
operator|==
literal|null
operator|||
name|column
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|primaryKeys
index|[
name|col
index|]
operator|=
name|Boolean
operator|.
name|FALSE
expr_stmt|;
block|}
else|else
block|{
name|ResultSet
name|pks
init|=
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getPrimaryKeys
argument_list|(
name|beeLine
operator|.
name|getDatabaseConnection
argument_list|()
operator|.
name|getDatabaseMetaData
argument_list|()
operator|.
name|getConnection
argument_list|()
operator|.
name|getCatalog
argument_list|()
argument_list|,
literal|null
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|primaryKeys
index|[
name|col
index|]
operator|=
name|Boolean
operator|.
name|FALSE
expr_stmt|;
try|try
block|{
while|while
condition|(
name|pks
operator|.
name|next
argument_list|()
condition|)
block|{
if|if
condition|(
name|column
operator|.
name|equalsIgnoreCase
argument_list|(
name|pks
operator|.
name|getString
argument_list|(
literal|"COLUMN_NAME"
argument_list|)
argument_list|)
condition|)
block|{
name|primaryKeys
index|[
name|col
index|]
operator|=
name|Boolean
operator|.
name|TRUE
expr_stmt|;
break|break;
block|}
block|}
block|}
finally|finally
block|{
name|pks
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|sqle
parameter_list|)
block|{
name|primaryKeys
index|[
name|col
index|]
operator|=
name|Boolean
operator|.
name|FALSE
expr_stmt|;
block|}
block|}
return|return
name|primaryKeys
index|[
name|col
index|]
operator|.
name|booleanValue
argument_list|()
return|;
block|}
class|class
name|Row
block|{
specifier|final
name|String
index|[]
name|values
decl_stmt|;
specifier|final
name|boolean
name|isMeta
decl_stmt|;
name|boolean
name|deleted
decl_stmt|;
name|boolean
name|inserted
decl_stmt|;
name|boolean
name|updated
decl_stmt|;
name|int
index|[]
name|sizes
decl_stmt|;
name|Row
parameter_list|(
name|int
name|size
parameter_list|)
throws|throws
name|SQLException
block|{
name|isMeta
operator|=
literal|true
expr_stmt|;
name|values
operator|=
operator|new
name|String
index|[
name|size
index|]
expr_stmt|;
name|sizes
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|rsMeta
operator|.
name|getColumnLabel
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
name|sizes
index|[
name|i
index|]
operator|=
name|values
index|[
name|i
index|]
operator|==
literal|null
condition|?
literal|1
else|:
name|values
index|[
name|i
index|]
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|deleted
operator|=
literal|false
expr_stmt|;
name|updated
operator|=
literal|false
expr_stmt|;
name|inserted
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|values
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
name|Row
parameter_list|(
name|int
name|size
parameter_list|,
name|ResultSet
name|rs
parameter_list|)
throws|throws
name|SQLException
block|{
name|isMeta
operator|=
literal|false
expr_stmt|;
name|values
operator|=
operator|new
name|String
index|[
name|size
index|]
expr_stmt|;
name|sizes
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
try|try
block|{
name|deleted
operator|=
name|rs
operator|.
name|rowDeleted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{       }
try|try
block|{
name|updated
operator|=
name|rs
operator|.
name|rowUpdated
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{       }
try|try
block|{
name|inserted
operator|=
name|rs
operator|.
name|rowInserted
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{       }
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|o
init|=
name|rs
operator|.
name|getObject
argument_list|(
name|i
operator|+
literal|1
argument_list|)
decl_stmt|;
name|String
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|nullStr
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|Number
condition|)
block|{
name|value
operator|=
name|numberFormat
operator|!=
literal|null
condition|?
name|numberFormat
operator|.
name|format
argument_list|(
name|o
argument_list|)
else|:
name|o
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|byte
index|[]
condition|)
block|{
name|value
operator|=
name|convertBinaryArray
condition|?
operator|new
name|String
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|o
argument_list|)
else|:
name|Arrays
operator|.
name|toString
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|o
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|o
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|beeLine
operator|.
name|getOpts
argument_list|()
operator|.
name|getEscapeCRLF
argument_list|()
condition|)
block|{
name|value
operator|=
name|EscapeCRLFHelper
operator|.
name|escapeCRLF
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|values
index|[
name|i
index|]
operator|=
name|value
operator|.
name|intern
argument_list|()
expr_stmt|;
name|sizes
index|[
name|i
index|]
operator|=
name|value
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit


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
name|hplsql
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|PreparedStatement
import|;
end_import

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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|v4
operator|.
name|runtime
operator|.
name|ParserRuleContext
import|;
end_import

begin_comment
comment|/**  * Metadata  */
end_comment

begin_class
specifier|public
class|class
name|Meta
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
argument_list|>
name|dataTypes
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Exec
name|exec
decl_stmt|;
name|boolean
name|trace
init|=
literal|false
decl_stmt|;
name|boolean
name|info
init|=
literal|false
decl_stmt|;
name|Meta
parameter_list|(
name|Exec
name|e
parameter_list|)
block|{
name|exec
operator|=
name|e
expr_stmt|;
name|trace
operator|=
name|exec
operator|.
name|getTrace
argument_list|()
expr_stmt|;
name|info
operator|=
name|exec
operator|.
name|getInfo
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get the data type of column (column name is qualified i.e. schema.table.column)    */
name|String
name|getDataType
parameter_list|(
name|ParserRuleContext
name|ctx
parameter_list|,
name|String
name|conn
parameter_list|,
name|String
name|column
parameter_list|)
block|{
name|String
name|type
init|=
literal|null
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
name|map
init|=
name|dataTypes
operator|.
name|get
argument_list|(
name|conn
argument_list|)
decl_stmt|;
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
name|map
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
argument_list|()
expr_stmt|;
name|dataTypes
operator|.
name|put
argument_list|(
name|conn
argument_list|,
name|map
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|twoparts
init|=
name|splitIdentifierToTwoParts
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|twoparts
operator|!=
literal|null
condition|)
block|{
name|String
name|tab
init|=
name|twoparts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|col
init|=
name|twoparts
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|toUpperCase
argument_list|()
decl_stmt|;
name|Row
name|row
init|=
name|map
operator|.
name|get
argument_list|(
name|tab
argument_list|)
decl_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
name|type
operator|=
name|row
operator|.
name|getType
argument_list|(
name|col
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|row
operator|=
name|readColumns
argument_list|(
name|ctx
argument_list|,
name|conn
argument_list|,
name|tab
argument_list|,
name|map
argument_list|)
expr_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
name|type
operator|=
name|row
operator|.
name|getType
argument_list|(
name|col
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|type
return|;
block|}
comment|/**    * Get data types for all columns of the table    */
name|Row
name|getRowDataType
parameter_list|(
name|ParserRuleContext
name|ctx
parameter_list|,
name|String
name|conn
parameter_list|,
name|String
name|table
parameter_list|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
name|map
init|=
name|dataTypes
operator|.
name|get
argument_list|(
name|conn
argument_list|)
decl_stmt|;
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
name|map
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
argument_list|()
expr_stmt|;
name|dataTypes
operator|.
name|put
argument_list|(
name|conn
argument_list|,
name|map
argument_list|)
expr_stmt|;
block|}
name|Row
name|row
init|=
name|map
operator|.
name|get
argument_list|(
name|table
argument_list|)
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|row
operator|=
name|readColumns
argument_list|(
name|ctx
argument_list|,
name|conn
argument_list|,
name|table
argument_list|,
name|map
argument_list|)
expr_stmt|;
block|}
return|return
name|row
return|;
block|}
comment|/**    * Read the column data from the database and cache it    */
name|Row
name|readColumns
parameter_list|(
name|ParserRuleContext
name|ctx
parameter_list|,
name|String
name|conn
parameter_list|,
name|String
name|table
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Row
argument_list|>
name|map
parameter_list|)
block|{
name|Row
name|row
init|=
literal|null
decl_stmt|;
name|Conn
operator|.
name|Type
name|connType
init|=
name|exec
operator|.
name|getConnectionType
argument_list|(
name|conn
argument_list|)
decl_stmt|;
if|if
condition|(
name|connType
operator|==
name|Conn
operator|.
name|Type
operator|.
name|HIVE
condition|)
block|{
name|String
name|sql
init|=
literal|"DESCRIBE "
operator|+
name|table
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|Query
argument_list|(
name|sql
argument_list|)
decl_stmt|;
name|exec
operator|.
name|executeQuery
argument_list|(
name|ctx
argument_list|,
name|query
argument_list|,
name|conn
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|query
operator|.
name|error
argument_list|()
condition|)
block|{
name|ResultSet
name|rs
init|=
name|query
operator|.
name|getResultSet
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
name|rs
operator|.
name|next
argument_list|()
condition|)
block|{
name|String
name|col
init|=
name|rs
operator|.
name|getString
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|typ
init|=
name|rs
operator|.
name|getString
argument_list|(
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|row
operator|=
operator|new
name|Row
argument_list|()
expr_stmt|;
block|}
name|row
operator|.
name|addColumn
argument_list|(
name|col
operator|.
name|toUpperCase
argument_list|()
argument_list|,
name|typ
argument_list|)
expr_stmt|;
block|}
name|map
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
block|}
name|exec
operator|.
name|closeQuery
argument_list|(
name|query
argument_list|,
name|conn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Query
name|query
init|=
name|exec
operator|.
name|prepareQuery
argument_list|(
name|ctx
argument_list|,
literal|"SELECT * FROM "
operator|+
name|table
argument_list|,
name|conn
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|query
operator|.
name|error
argument_list|()
condition|)
block|{
try|try
block|{
name|PreparedStatement
name|stmt
init|=
name|query
operator|.
name|getPreparedStatement
argument_list|()
decl_stmt|;
name|ResultSetMetaData
name|rm
init|=
name|stmt
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|int
name|cols
init|=
name|rm
operator|.
name|getColumnCount
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|cols
condition|;
name|i
operator|++
control|)
block|{
name|String
name|col
init|=
name|rm
operator|.
name|getColumnName
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|typ
init|=
name|rm
operator|.
name|getColumnTypeName
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|row
operator|=
operator|new
name|Row
argument_list|()
expr_stmt|;
block|}
name|row
operator|.
name|addColumn
argument_list|(
name|col
operator|.
name|toUpperCase
argument_list|()
argument_list|,
name|typ
argument_list|)
expr_stmt|;
block|}
name|map
operator|.
name|put
argument_list|(
name|table
argument_list|,
name|row
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{}
block|}
name|exec
operator|.
name|closeQuery
argument_list|(
name|query
argument_list|,
name|conn
argument_list|)
expr_stmt|;
block|}
return|return
name|row
return|;
block|}
comment|/**    * Normalize identifier for a database object (convert "" [] to `` i.e.)    */
specifier|public
name|String
name|normalizeObjectIdentifier
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|parts
init|=
name|splitIdentifier
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|!=
literal|null
condition|)
block|{
comment|// more then one part exist
name|StringBuilder
name|norm
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|parts
operator|.
name|size
argument_list|()
decl_stmt|;
name|boolean
name|appended
init|=
literal|false
decl_stmt|;
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
if|if
condition|(
name|i
operator|==
name|size
operator|-
literal|2
condition|)
block|{
comment|// schema name
name|String
name|schema
init|=
name|getTargetSchemaName
argument_list|(
name|parts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|schema
operator|!=
literal|null
condition|)
block|{
name|norm
operator|.
name|append
argument_list|(
name|schema
argument_list|)
expr_stmt|;
name|appended
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|norm
operator|.
name|append
argument_list|(
name|normalizeIdentifierPart
argument_list|(
name|parts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|appended
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|+
literal|1
operator|<
name|parts
operator|.
name|size
argument_list|()
operator|&&
name|appended
condition|)
block|{
name|norm
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|norm
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
name|normalizeIdentifierPart
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Get the schema name to be used in the final executed SQL    */
name|String
name|getTargetSchemaName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"dbo"
argument_list|)
operator|||
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"[dbo]"
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|normalizeIdentifierPart
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Normalize identifier (single part) - convert "" [] to `` i.e.    */
specifier|public
name|String
name|normalizeIdentifierPart
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|char
name|start
init|=
name|name
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|char
name|end
init|=
name|name
operator|.
name|charAt
argument_list|(
name|name
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|start
operator|==
literal|'['
operator|&&
name|end
operator|==
literal|']'
operator|)
operator|||
operator|(
name|start
operator|==
literal|'"'
operator|&&
name|end
operator|==
literal|'"'
operator|)
condition|)
block|{
return|return
literal|'`'
operator|+
name|name
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|name
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|+
literal|'`'
return|;
block|}
return|return
name|name
return|;
block|}
comment|/**    * Split qualified object to 2 parts: schema.tab.col -> schema.tab|col; tab.col -> tab|col     */
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|splitIdentifierToTwoParts
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|parts
init|=
name|splitIdentifier
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|twoparts
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|parts
operator|!=
literal|null
condition|)
block|{
name|StringBuilder
name|id
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<
name|parts
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|id
operator|.
name|append
argument_list|(
name|parts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|+
literal|1
operator|<
name|parts
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|id
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
block|}
block|}
name|twoparts
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|twoparts
operator|.
name|add
argument_list|(
name|id
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|id
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|id
operator|.
name|append
argument_list|(
name|parts
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|twoparts
operator|.
name|add
argument_list|(
name|id
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|twoparts
return|;
block|}
comment|/**    * Split identifier to parts (schema, table, colum name etc.)    * @return null if identifier contains single part    */
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|splitIdentifier
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|parts
init|=
literal|null
decl_stmt|;
name|int
name|start
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|name
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|name
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|char
name|del
init|=
literal|'\0'
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'`'
operator|||
name|c
operator|==
literal|'"'
condition|)
block|{
name|del
operator|=
name|c
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'['
condition|)
block|{
name|del
operator|=
literal|']'
expr_stmt|;
block|}
if|if
condition|(
name|del
operator|!=
literal|'\0'
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
name|i
operator|+
literal|1
init|;
name|i
operator|<
name|name
operator|.
name|length
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|i
operator|++
expr_stmt|;
if|if
condition|(
name|name
operator|.
name|charAt
argument_list|(
name|j
argument_list|)
operator|==
name|del
condition|)
block|{
break|break;
block|}
block|}
continue|continue;
block|}
if|if
condition|(
name|c
operator|==
literal|'.'
condition|)
block|{
if|if
condition|(
name|parts
operator|==
literal|null
condition|)
block|{
name|parts
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|parts
operator|.
name|add
argument_list|(
name|name
operator|.
name|substring
argument_list|(
name|start
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|start
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|parts
operator|!=
literal|null
condition|)
block|{
name|parts
operator|.
name|add
argument_list|(
name|name
operator|.
name|substring
argument_list|(
name|start
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|parts
return|;
block|}
block|}
end_class

end_unit


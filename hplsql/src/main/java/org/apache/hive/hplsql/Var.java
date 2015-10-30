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
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|RoundingMode
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
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_comment
comment|/**  * Variable or the result of expression   */
end_comment

begin_class
specifier|public
class|class
name|Var
block|{
comment|// Data types
specifier|public
enum|enum
name|Type
block|{
name|BOOL
block|,
name|CURSOR
block|,
name|DATE
block|,
name|DECIMAL
block|,
name|DERIVED_TYPE
block|,
name|DERIVED_ROWTYPE
block|,
name|DOUBLE
block|,
name|FILE
block|,
name|IDENT
block|,
name|BIGINT
block|,
name|INTERVAL
block|,
name|ROW
block|,
name|RS_LOCATOR
block|,
name|STRING
block|,
name|STRINGLIST
block|,
name|TIMESTAMP
block|,
name|NULL
block|}
empty_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DERIVED_TYPE
init|=
literal|"DERIVED%TYPE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DERIVED_ROWTYPE
init|=
literal|"DERIVED%ROWTYPE"
decl_stmt|;
specifier|public
specifier|static
name|Var
name|Empty
init|=
operator|new
name|Var
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|Var
name|Null
init|=
operator|new
name|Var
argument_list|(
name|Type
operator|.
name|NULL
argument_list|)
decl_stmt|;
specifier|public
name|String
name|name
decl_stmt|;
specifier|public
name|Type
name|type
decl_stmt|;
specifier|public
name|Object
name|value
decl_stmt|;
name|int
name|len
decl_stmt|;
name|int
name|scale
decl_stmt|;
name|boolean
name|constant
init|=
literal|false
decl_stmt|;
specifier|public
name|Var
parameter_list|()
block|{
name|type
operator|=
name|Type
operator|.
name|NULL
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Var
name|var
parameter_list|)
block|{
name|name
operator|=
name|var
operator|.
name|name
expr_stmt|;
name|type
operator|=
name|var
operator|.
name|type
expr_stmt|;
name|value
operator|=
name|var
operator|.
name|value
expr_stmt|;
name|len
operator|=
name|var
operator|.
name|len
expr_stmt|;
name|scale
operator|=
name|var
operator|.
name|scale
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Long
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|BIGINT
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|BigDecimal
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|DECIMAL
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|String
name|name
parameter_list|,
name|Long
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|BIGINT
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|STRING
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Double
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|DOUBLE
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Date
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|DATE
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Timestamp
name|value
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|TIMESTAMP
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|scale
operator|=
name|scale
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Interval
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|INTERVAL
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|STRINGLIST
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Boolean
name|b
parameter_list|)
block|{
name|type
operator|=
name|Type
operator|.
name|BOOL
expr_stmt|;
name|value
operator|=
name|b
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|String
name|name
parameter_list|,
name|Row
name|row
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|ROW
expr_stmt|;
name|this
operator|.
name|value
operator|=
operator|new
name|Row
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Type
name|type
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Type
name|type
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|Var
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|len
parameter_list|,
name|String
name|scale
parameter_list|,
name|Var
name|def
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|setType
argument_list|(
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|len
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|len
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scale
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scale
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|scale
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|def
operator|!=
literal|null
condition|)
block|{
name|cast
argument_list|(
name|def
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** 	 * Cast a new value to the variable  	 */
specifier|public
name|Var
name|cast
parameter_list|(
name|Var
name|val
parameter_list|)
block|{
if|if
condition|(
name|constant
condition|)
block|{
return|return
name|this
return|;
block|}
elseif|else
if|if
condition|(
name|val
operator|==
literal|null
operator|||
name|val
operator|.
name|value
operator|==
literal|null
condition|)
block|{
name|value
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DERIVED_TYPE
condition|)
block|{
name|type
operator|=
name|val
operator|.
name|type
expr_stmt|;
name|value
operator|=
name|val
operator|.
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|val
operator|.
name|type
operator|&&
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
name|cast
argument_list|(
operator|(
name|String
operator|)
name|val
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|val
operator|.
name|type
condition|)
block|{
name|value
operator|=
name|val
operator|.
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
name|cast
argument_list|(
name|val
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DECIMAL
condition|)
block|{
if|if
condition|(
name|val
operator|.
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
name|value
operator|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|val
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|val
operator|.
name|type
operator|==
name|Type
operator|.
name|DOUBLE
condition|)
block|{
name|value
operator|=
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|val
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DOUBLE
condition|)
block|{
if|if
condition|(
name|val
operator|.
name|type
operator|==
name|Type
operator|.
name|BIGINT
operator|||
name|val
operator|.
name|type
operator|==
name|Type
operator|.
name|DECIMAL
condition|)
block|{
name|value
operator|=
name|Double
operator|.
name|valueOf
argument_list|(
name|val
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DATE
condition|)
block|{
name|value
operator|=
name|Utils
operator|.
name|toDate
argument_list|(
name|val
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|TIMESTAMP
condition|)
block|{
name|value
operator|=
name|Utils
operator|.
name|toTimestamp
argument_list|(
name|val
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**    * Cast a new string value to the variable     */
specifier|public
name|Var
name|cast
parameter_list|(
name|String
name|val
parameter_list|)
block|{
if|if
condition|(
operator|!
name|constant
operator|&&
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
if|if
condition|(
name|len
operator|!=
literal|0
condition|)
block|{
name|int
name|l
init|=
name|val
operator|.
name|length
argument_list|()
decl_stmt|;
if|if
condition|(
name|l
operator|>
name|len
condition|)
block|{
name|value
operator|=
name|val
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
name|value
operator|=
name|val
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/** 	 * Set the new value  	 */
specifier|public
name|void
name|setValue
parameter_list|(
name|String
name|str
parameter_list|)
block|{
if|if
condition|(
operator|!
name|constant
operator|&&
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
name|value
operator|=
name|str
expr_stmt|;
block|}
block|}
specifier|public
name|Var
name|setValue
parameter_list|(
name|Long
name|val
parameter_list|)
block|{
if|if
condition|(
operator|!
name|constant
operator|&&
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
name|value
operator|=
name|val
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|Var
name|setValue
parameter_list|(
name|Boolean
name|val
parameter_list|)
block|{
if|if
condition|(
operator|!
name|constant
operator|&&
name|type
operator|==
name|Type
operator|.
name|BOOL
condition|)
block|{
name|value
operator|=
name|val
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|void
name|setValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
operator|!
name|constant
condition|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
block|}
comment|/**    * Set the new value from the result set    */
specifier|public
name|Var
name|setValue
parameter_list|(
name|ResultSet
name|rs
parameter_list|,
name|ResultSetMetaData
name|rsm
parameter_list|,
name|int
name|idx
parameter_list|)
throws|throws
name|SQLException
block|{
name|int
name|type
init|=
name|rsm
operator|.
name|getColumnType
argument_list|(
name|idx
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|CHAR
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
condition|)
block|{
name|cast
argument_list|(
operator|new
name|Var
argument_list|(
name|rs
operator|.
name|getString
argument_list|(
name|idx
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|INTEGER
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|BIGINT
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|SMALLINT
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|TINYINT
condition|)
block|{
name|cast
argument_list|(
operator|new
name|Var
argument_list|(
operator|new
name|Long
argument_list|(
name|rs
operator|.
name|getLong
argument_list|(
name|idx
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|DECIMAL
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|NUMERIC
condition|)
block|{
name|cast
argument_list|(
operator|new
name|Var
argument_list|(
name|rs
operator|.
name|getBigDecimal
argument_list|(
name|idx
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|FLOAT
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|DOUBLE
condition|)
block|{
name|cast
argument_list|(
operator|new
name|Var
argument_list|(
operator|new
name|Double
argument_list|(
name|rs
operator|.
name|getDouble
argument_list|(
name|idx
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**    * Set ROW values from the result set    */
specifier|public
name|Var
name|setValues
parameter_list|(
name|ResultSet
name|rs
parameter_list|,
name|ResultSetMetaData
name|rsm
parameter_list|)
throws|throws
name|SQLException
block|{
name|Row
name|row
init|=
operator|(
name|Row
operator|)
name|this
operator|.
name|value
decl_stmt|;
name|int
name|idx
init|=
literal|1
decl_stmt|;
for|for
control|(
name|Column
name|column
range|:
name|row
operator|.
name|getColumns
argument_list|()
control|)
block|{
name|Var
name|var
init|=
operator|new
name|Var
argument_list|(
name|column
operator|.
name|getName
argument_list|()
argument_list|,
name|column
operator|.
name|getType
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|var
operator|.
name|setValue
argument_list|(
name|rs
argument_list|,
name|rsm
argument_list|,
name|idx
argument_list|)
expr_stmt|;
name|column
operator|.
name|setValue
argument_list|(
name|var
argument_list|)
expr_stmt|;
name|idx
operator|++
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/** 	 * Set the data type from string representation 	 */
name|void
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|defineType
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the data type from JDBC type code    */
name|void
name|setType
parameter_list|(
name|int
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|defineType
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the variable as constant    */
name|void
name|setConstant
parameter_list|(
name|boolean
name|constant
parameter_list|)
block|{
name|this
operator|.
name|constant
operator|=
name|constant
expr_stmt|;
block|}
comment|/**    * Define the data type from string representation    */
specifier|public
specifier|static
name|Type
name|defineType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
name|Type
operator|.
name|NULL
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INT"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INTEGER"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BIGINT"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"SMALLINT"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"TINYINT"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BINARY_INTEGER"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"PLS_INTEGER"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"SIMPLE_INTEGER"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|BIGINT
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"CHAR"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"VARCHAR"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"STRING"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"XML"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|STRING
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"DEC"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"DECIMAL"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NUMERIC"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NUMBER"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DECIMAL
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"REAL"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"FLOAT"
argument_list|)
operator|||
name|type
operator|.
name|toUpperCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"DOUBLE"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BINARY_FLOAT"
argument_list|)
operator|||
name|type
operator|.
name|toUpperCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"BINARY_DOUBLE"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"SIMPLE_FLOAT"
argument_list|)
operator|||
name|type
operator|.
name|toUpperCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"SIMPLE_DOUBLE"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DOUBLE
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"DATE"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DATE
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"TIMESTAMP"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|TIMESTAMP
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BOOL"
argument_list|)
operator|||
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BOOLEAN"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|BOOL
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"SYS_REFCURSOR"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|CURSOR
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"UTL_FILE.FILE_TYPE"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|FILE
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|toUpperCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"RESULT_SET_LOCATOR"
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|RS_LOCATOR
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
name|Var
operator|.
name|DERIVED_TYPE
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DERIVED_TYPE
return|;
block|}
return|return
name|Type
operator|.
name|NULL
return|;
block|}
comment|/**    * Define the data type from JDBC type code    */
specifier|public
specifier|static
name|Type
name|defineType
parameter_list|(
name|int
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|CHAR
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
condition|)
block|{
return|return
name|Type
operator|.
name|STRING
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|INTEGER
operator|||
name|type
operator|==
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|BIGINT
condition|)
block|{
return|return
name|Type
operator|.
name|BIGINT
return|;
block|}
return|return
name|Type
operator|.
name|NULL
return|;
block|}
comment|/** 	 * Remove value 	 */
specifier|public
name|void
name|removeValue
parameter_list|()
block|{
name|type
operator|=
name|Type
operator|.
name|NULL
expr_stmt|;
name|name
operator|=
literal|null
expr_stmt|;
name|value
operator|=
literal|null
expr_stmt|;
name|len
operator|=
literal|0
expr_stmt|;
name|scale
operator|=
literal|0
expr_stmt|;
block|}
comment|/** 	 * Compare values 	 */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Var
name|var
init|=
operator|(
name|Var
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|this
operator|==
name|var
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|var
operator|==
literal|null
operator|||
name|var
operator|.
name|value
operator|==
literal|null
operator|||
name|this
operator|.
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
if|if
condition|(
name|var
operator|.
name|type
operator|==
name|Type
operator|.
name|BIGINT
operator|&&
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
operator|==
operator|(
operator|(
name|Long
operator|)
name|var
operator|.
name|value
operator|)
operator|.
name|longValue
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|var
operator|.
name|type
operator|==
name|Type
operator|.
name|DECIMAL
condition|)
block|{
return|return
name|equals
argument_list|(
operator|(
name|BigDecimal
operator|)
name|var
operator|.
name|value
argument_list|,
operator|(
name|Long
operator|)
name|value
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING
operator|&&
name|var
operator|.
name|type
operator|==
name|Type
operator|.
name|STRING
operator|&&
operator|(
operator|(
name|String
operator|)
name|value
operator|)
operator|.
name|equals
argument_list|(
operator|(
name|String
operator|)
name|var
operator|.
name|value
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DECIMAL
operator|&&
name|var
operator|.
name|type
operator|==
name|Type
operator|.
name|DECIMAL
operator|&&
operator|(
operator|(
name|BigDecimal
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|BigDecimal
operator|)
name|var
operator|.
name|value
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DOUBLE
condition|)
block|{
if|if
condition|(
name|var
operator|.
name|type
operator|==
name|Type
operator|.
name|DOUBLE
operator|&&
operator|(
operator|(
name|Double
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Double
operator|)
name|var
operator|.
name|value
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|var
operator|.
name|type
operator|==
name|Type
operator|.
name|DECIMAL
operator|&&
operator|(
operator|(
name|Double
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
operator|(
name|BigDecimal
operator|)
name|var
operator|.
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
operator|==
literal|0
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
comment|/**    * Check if variables of different data types are equal    */
specifier|public
name|boolean
name|equals
parameter_list|(
name|BigDecimal
name|d
parameter_list|,
name|Long
name|i
parameter_list|)
block|{
if|if
condition|(
name|d
operator|.
name|compareTo
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|i
argument_list|)
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Compare values    */
specifier|public
name|int
name|compareTo
parameter_list|(
name|Var
name|v
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|v
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|v
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
operator|&&
name|v
operator|.
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
return|return
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|Long
operator|)
name|v
operator|.
name|value
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING
operator|&&
name|v
operator|.
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
return|return
operator|(
operator|(
name|String
operator|)
name|value
operator|)
operator|.
name|compareTo
argument_list|(
operator|(
name|String
operator|)
name|v
operator|.
name|value
argument_list|)
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * Calculate difference between values in percent    */
specifier|public
name|BigDecimal
name|percentDiff
parameter_list|(
name|Var
name|var
parameter_list|)
block|{
name|BigDecimal
name|d1
init|=
operator|new
name|Var
argument_list|(
name|Var
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|)
operator|.
name|cast
argument_list|(
name|this
argument_list|)
operator|.
name|decimalValue
argument_list|()
decl_stmt|;
name|BigDecimal
name|d2
init|=
operator|new
name|Var
argument_list|(
name|Var
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|)
operator|.
name|cast
argument_list|(
name|var
argument_list|)
operator|.
name|decimalValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|d1
operator|!=
literal|null
operator|&&
name|d2
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|d1
operator|.
name|compareTo
argument_list|(
name|BigDecimal
operator|.
name|ZERO
argument_list|)
operator|!=
literal|0
condition|)
block|{
return|return
name|d1
operator|.
name|subtract
argument_list|(
name|d2
argument_list|)
operator|.
name|abs
argument_list|()
operator|.
name|multiply
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|100
argument_list|)
argument_list|)
operator|.
name|divide
argument_list|(
name|d1
argument_list|,
literal|2
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Increment an integer value    */
specifier|public
name|Var
name|increment
parameter_list|(
name|Long
name|i
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
name|value
operator|=
operator|new
name|Long
argument_list|(
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**   * Decrement an integer value   */
specifier|public
name|Var
name|decrement
parameter_list|(
name|Long
name|i
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
name|value
operator|=
operator|new
name|Long
argument_list|(
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
operator|-
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/** 	 * Return an integer value 	 */
specifier|public
name|int
name|intValue
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
return|return
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|intValue
argument_list|()
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * Return a long integer value    */
specifier|public
name|long
name|longValue
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
return|return
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * Return a decimal value    */
specifier|public
name|BigDecimal
name|decimalValue
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DECIMAL
condition|)
block|{
return|return
operator|(
name|BigDecimal
operator|)
name|value
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Return a double value    */
specifier|public
name|double
name|doubleValue
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DOUBLE
condition|)
block|{
return|return
operator|(
operator|(
name|Double
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
return|return
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DECIMAL
condition|)
block|{
return|return
operator|(
operator|(
name|BigDecimal
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/** 	 * Return true/false for BOOL type 	 */
specifier|public
name|boolean
name|isTrue
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BOOL
operator|&&
name|value
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
operator|(
name|Boolean
operator|)
name|value
operator|)
operator|.
name|booleanValue
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/** 	 * Negate the boolean value 	 */
specifier|public
name|void
name|negate
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BOOL
operator|&&
name|value
operator|!=
literal|null
condition|)
block|{
name|boolean
name|v
init|=
operator|(
operator|(
name|Boolean
operator|)
name|value
operator|)
operator|.
name|booleanValue
argument_list|()
decl_stmt|;
name|value
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
operator|!
name|v
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** 	 * Check if the variable contains NULL 	 */
specifier|public
name|boolean
name|isNull
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|NULL
operator|||
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/** 	 * Convert value to String 	 */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|IDENT
condition|)
block|{
return|return
name|name
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|BIGINT
condition|)
block|{
return|return
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|toString
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
return|return
operator|(
name|String
operator|)
name|value
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|DATE
condition|)
block|{
return|return
operator|(
operator|(
name|Date
operator|)
name|value
operator|)
operator|.
name|toString
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|TIMESTAMP
condition|)
block|{
name|int
name|len
init|=
literal|19
decl_stmt|;
name|String
name|t
init|=
operator|(
operator|(
name|Timestamp
operator|)
name|value
operator|)
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// .0 returned if the fractional part not set
if|if
condition|(
name|scale
operator|>
literal|0
condition|)
block|{
name|len
operator|+=
name|scale
operator|+
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|t
operator|.
name|length
argument_list|()
operator|>
name|len
condition|)
block|{
name|t
operator|=
name|t
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
return|return
name|t
return|;
block|}
return|return
name|value
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Convert value to SQL string - string literals are quoted and escaped, ab'c -> 'ab''c'    */
specifier|public
name|String
name|toSqlString
parameter_list|()
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|"NULL"
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|STRING
condition|)
block|{
return|return
name|Utils
operator|.
name|quoteString
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
return|;
block|}
return|return
name|toString
argument_list|()
return|;
block|}
comment|/**    * Set variable name    */
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/** 	 * Get variable name 	 */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
end_class

end_unit


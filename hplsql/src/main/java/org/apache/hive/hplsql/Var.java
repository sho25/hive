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
name|DEC
block|,
name|FILE
block|,
name|IDENT
block|,
name|BIGINT
block|,
name|INTERVAL
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
name|DEC
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
comment|/**    * Set the new value from a result set    */
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
comment|/* 	 * Compare values 	 */
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
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|obj
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
elseif|else
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
name|type
operator|==
name|Type
operator|.
name|BIGINT
operator|&&
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
return|return
literal|false
return|;
block|}
comment|/*    * Compare values    */
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


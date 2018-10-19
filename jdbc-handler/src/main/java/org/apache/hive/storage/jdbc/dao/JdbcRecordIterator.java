begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
operator|.
name|dao
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|hive
operator|.
name|conf
operator|.
name|Constants
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
name|serde
operator|.
name|serdeConstants
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
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
name|sql
operator|.
name|Connection
import|;
end_import

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
name|SQLDataException
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
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * An iterator that allows iterating through a SQL resultset. Includes methods to clear up resources.  */
end_comment

begin_class
specifier|public
class|class
name|JdbcRecordIterator
implements|implements
name|Iterator
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|JdbcRecordIterator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|private
name|PreparedStatement
name|ps
decl_stmt|;
specifier|private
name|ResultSet
name|rs
decl_stmt|;
specifier|private
name|String
index|[]
name|hiveColumnNames
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|hiveColumnTypesList
decl_stmt|;
specifier|public
name|JdbcRecordIterator
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|PreparedStatement
name|ps
parameter_list|,
name|ResultSet
name|rs
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
name|this
operator|.
name|ps
operator|=
name|ps
expr_stmt|;
name|this
operator|.
name|rs
operator|=
name|rs
expr_stmt|;
name|String
name|fieldNamesProperty
decl_stmt|;
name|String
name|fieldTypesProperty
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_TABLE
argument_list|)
operator|!=
literal|null
operator|&&
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_QUERY
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|fieldNamesProperty
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_QUERY_FIELD_NAMES
argument_list|)
argument_list|)
expr_stmt|;
name|fieldTypesProperty
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|JDBC_QUERY_FIELD_TYPES
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fieldNamesProperty
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
argument_list|)
expr_stmt|;
name|fieldTypesProperty
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|hiveColumnNames
operator|=
name|fieldNamesProperty
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|hiveColumnTypesList
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|fieldTypesProperty
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
try|try
block|{
return|return
name|rs
operator|.
name|next
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|se
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"hasNext() threw exception"
argument_list|,
name|se
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|next
parameter_list|()
block|{
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|record
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|(
name|hiveColumnNames
operator|.
name|length
argument_list|)
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
name|hiveColumnNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|hiveColumnNames
index|[
name|i
index|]
decl_stmt|;
name|Object
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|hiveColumnTypesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|PrimitiveTypeInfo
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"date type of column "
operator|+
name|hiveColumnNames
index|[
name|i
index|]
operator|+
literal|":"
operator|+
name|hiveColumnTypesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is not supported"
argument_list|)
throw|;
block|}
try|try
block|{
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|hiveColumnTypesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|INT
case|:
case|case
name|SHORT
case|:
case|case
name|BYTE
case|:
name|value
operator|=
name|rs
operator|.
name|getInt
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|value
operator|=
name|rs
operator|.
name|getLong
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|value
operator|=
name|rs
operator|.
name|getFloat
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|value
operator|=
name|rs
operator|.
name|getDouble
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|value
operator|=
name|rs
operator|.
name|getBigDecimal
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|value
operator|=
name|rs
operator|.
name|getBoolean
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
name|value
operator|=
name|rs
operator|.
name|getString
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|value
operator|=
name|rs
operator|.
name|getDate
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|value
operator|=
name|rs
operator|.
name|getTimestamp
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
break|break;
default|default:
name|LOGGER
operator|.
name|error
argument_list|(
literal|"date type of column "
operator|+
name|hiveColumnNames
index|[
name|i
index|]
operator|+
literal|":"
operator|+
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|hiveColumnTypesList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|+
literal|" is not supported"
argument_list|)
expr_stmt|;
name|value
operator|=
literal|null
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|value
operator|!=
literal|null
operator|&&
operator|!
name|rs
operator|.
name|wasNull
argument_list|()
condition|)
block|{
name|record
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|record
operator|.
name|put
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLDataException
name|e
parameter_list|)
block|{
name|record
operator|.
name|put
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|record
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"next() threw exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
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
argument_list|(
literal|"Remove is not supported"
argument_list|)
throw|;
block|}
comment|/**    * Release all DB resources    */
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
name|ps
operator|.
name|close
argument_list|()
expr_stmt|;
name|conn
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOGGER
operator|.
name|warn
argument_list|(
literal|"Caught exception while trying to close database objects"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


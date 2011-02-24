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
name|Arrays
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
name|Properties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|Schema
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
name|serde2
operator|.
name|SerDe
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
name|lazy
operator|.
name|LazySimpleSerDe
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
name|ObjectInspector
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
name|ObjectInspectorUtils
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
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|StructField
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
name|StructObjectInspector
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
name|service
operator|.
name|HiveInterface
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
name|service
operator|.
name|HiveServerException
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
name|io
operator|.
name|BytesWritable
import|;
end_import

begin_comment
comment|/**  * HiveQueryResultSet.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveQueryResultSet
extends|extends
name|HiveBaseResultSet
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveQueryResultSet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HiveInterface
name|client
decl_stmt|;
specifier|private
name|SerDe
name|serde
decl_stmt|;
specifier|private
name|int
name|maxRows
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|rowsFetched
init|=
literal|0
decl_stmt|;
specifier|public
name|HiveQueryResultSet
parameter_list|(
name|HiveInterface
name|client
parameter_list|,
name|int
name|maxRows
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|maxRows
operator|=
name|maxRows
expr_stmt|;
name|initSerde
argument_list|()
expr_stmt|;
name|row
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[
name|columnNames
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveQueryResultSet
parameter_list|(
name|HiveInterface
name|client
parameter_list|)
throws|throws
name|SQLException
block|{
name|this
argument_list|(
name|client
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiate the serde used to deserialize the result rows.    */
specifier|private
name|void
name|initSerde
parameter_list|()
throws|throws
name|SQLException
block|{
try|try
block|{
name|Schema
name|fullSchema
init|=
name|client
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
init|=
name|fullSchema
operator|.
name|getFieldSchemas
argument_list|()
decl_stmt|;
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|columnTypes
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|StringBuilder
name|namesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|typesSb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|schema
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|schema
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|schema
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
literal|0
condition|)
block|{
name|namesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|columnNames
operator|.
name|add
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|columnTypes
operator|.
name|add
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|namesSb
operator|.
name|append
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|typesSb
operator|.
name|append
argument_list|(
name|schema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|names
init|=
name|namesSb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|types
init|=
name|typesSb
operator|.
name|toString
argument_list|()
decl_stmt|;
name|serde
operator|=
operator|new
name|LazySimpleSerDe
argument_list|()
expr_stmt|;
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Column names: "
operator|+
name|names
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|names
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|types
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Column types: "
operator|+
name|types
argument_list|)
expr_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|types
argument_list|)
expr_stmt|;
block|}
name|serde
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|props
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Could not create ResultSet: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|SQLException
block|{
name|client
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Moves the cursor down one row from its current position.    *    * @see java.sql.ResultSet#next()    * @throws SQLException    *           if a database access error occurs.    */
specifier|public
name|boolean
name|next
parameter_list|()
throws|throws
name|SQLException
block|{
if|if
condition|(
name|maxRows
operator|>
literal|0
operator|&&
name|rowsFetched
operator|>=
name|maxRows
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|rowStr
init|=
literal|""
decl_stmt|;
try|try
block|{
name|rowStr
operator|=
operator|(
name|String
operator|)
name|client
operator|.
name|fetchOne
argument_list|()
expr_stmt|;
name|rowsFetched
operator|++
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Fetched row string: "
operator|+
name|rowStr
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|Object
name|data
init|=
name|serde
operator|.
name|deserialize
argument_list|(
operator|new
name|BytesWritable
argument_list|(
name|rowStr
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
assert|assert
name|row
operator|.
name|size
argument_list|()
operator|==
name|fieldRefs
operator|.
name|size
argument_list|()
operator|:
name|row
operator|.
name|size
argument_list|()
operator|+
literal|", "
operator|+
name|fieldRefs
operator|.
name|size
argument_list|()
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|StructField
name|fieldRef
init|=
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|fieldRef
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|Object
name|obj
init|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|data
argument_list|,
name|fieldRef
argument_list|)
decl_stmt|;
name|row
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|convertLazyToJava
argument_list|(
name|obj
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Deserialized row: "
operator|+
name|row
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveServerException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getErrorCode
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// error code == 0 means reached the EOF
return|return
literal|false
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Error retrieving next row"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|ex
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Error retrieving next row"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
comment|// NOTE: fetchOne dosn't throw new SQLException("Method not supported").
return|return
literal|true
return|;
block|}
comment|/**    * Convert a LazyObject to a standard Java object in compliance with JDBC 3.0 (see JDBC 3.0    * Specification, Table B-3: Mapping from JDBC Types to Java Object Types).    *    * This method is kept consistent with {@link HiveResultSetMetaData#hiveTypeToSqlType}.    */
specifier|private
specifier|static
name|Object
name|convertLazyToJava
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
name|Object
name|obj
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|o
argument_list|,
name|oi
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
decl_stmt|;
comment|// for now, expose non-primitive as a string
comment|// TODO: expose non-primitive as a structured object while maintaining JDBC compliance
if|if
condition|(
name|obj
operator|!=
literal|null
operator|&&
name|oi
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|obj
operator|=
name|obj
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
return|return
name|obj
return|;
block|}
block|}
end_class

end_unit


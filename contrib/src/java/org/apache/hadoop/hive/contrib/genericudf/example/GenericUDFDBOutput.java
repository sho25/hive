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
name|contrib
operator|.
name|genericudf
operator|.
name|example
package|;
end_package

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
name|DriverManager
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
name|SQLException
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
name|udf
operator|.
name|UDFType
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
name|exec
operator|.
name|UDF
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
name|exec
operator|.
name|description
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
name|udf
operator|.
name|generic
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentTypeException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
operator|.
name|DeferredObject
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|primitive
operator|.
name|StringObjectInspector
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
name|IntWritable
import|;
end_import

begin_comment
comment|/** * GenericUDFDBOutput is designed to output data directly from Hive to a JDBC datastore.  * This UDF is useful for exporting small to medium summaries that have a unique key. *  * Due to the nature of hadoop, individual mappers, reducers or entire jobs can fail.  * If a failure occurs a mapper or reducer may be retried. This UDF has no way of  * detecting failures or rolling back a transaction. Consequently, you should only  * only use this to export to a table with a unique key. The unique key should safeguard  * against duplicate data. *  * Use hive's ADD JAR feature to add your JDBC Driver to the distributed cache, * otherwise GenericUDFDBoutput will fail. */
end_comment

begin_class
annotation|@
name|description
argument_list|(
name|name
operator|=
literal|"dboutput"
argument_list|,
name|value
operator|=
literal|"_FUNC_(jdbcstring,username,password,preparedstatement,[arguments]) - sends data to a jdbc driver"
argument_list|,
name|extended
operator|=
literal|"argument 0 is the JDBC connection string\n"
operator|+
literal|"argument 1 is the user name\n"
operator|+
literal|"argument 2 is the password\n"
operator|+
literal|"argument 3 is an SQL query to be used in the PreparedStatement\n"
operator|+
literal|"argument (4-n) The remaining arguments must be primitive and are passed to the PreparedStatement object\n"
argument_list|)
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|false
argument_list|)
specifier|public
class|class
name|GenericUDFDBOutput
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenericUDFDBOutput
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|argumentOI
decl_stmt|;
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
name|returnOIResolver
decl_stmt|;
name|Connection
name|connection
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|url
decl_stmt|;
specifier|private
name|String
name|user
decl_stmt|;
specifier|private
name|String
name|pass
decl_stmt|;
specifier|private
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|/**   * @param arguments    * argument 0 is the JDBC connection string   * argument 1 is the user name   * argument 2 is the password   * argument 3 is an SQL query to be used in the PreparedStatement   * argument (4-n) The remaining arguments must be primitive and are passed to the PreparedStatement object   */
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|this
operator|.
name|argumentOI
operator|=
name|arguments
expr_stmt|;
comment|//this should be connection url,username,password,query,column1[,columnn]*
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|4
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
operator|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"The argument of function  should be \""
operator|+
name|Constants
operator|.
name|STRING_TYPE_NAME
operator|+
literal|"\", but \""
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|4
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|i
index|]
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
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"The argument of function should be primative"
operator|+
literal|", but \""
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
return|;
block|}
comment|/**   * @return 0 on success -1 on failure   */
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|url
operator|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|argumentOI
index|[
literal|0
index|]
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|user
operator|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|argumentOI
index|[
literal|1
index|]
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|pass
operator|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|argumentOI
index|[
literal|2
index|]
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
literal|2
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|connection
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|,
name|user
argument_list|,
name|pass
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Driver loading or connection issue"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|connection
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|PreparedStatement
name|ps
init|=
name|connection
operator|.
name|prepareStatement
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|argumentOI
index|[
literal|3
index|]
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
literal|3
index|]
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|4
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|argumentOI
index|[
name|i
index|]
operator|)
decl_stmt|;
name|ps
operator|.
name|setObject
argument_list|(
name|i
operator|-
literal|3
argument_list|,
name|poi
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ps
operator|.
name|execute
argument_list|()
expr_stmt|;
name|ps
operator|.
name|close
argument_list|()
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Underlying SQL exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Underlying SQL exception during close"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"dboutput("
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit


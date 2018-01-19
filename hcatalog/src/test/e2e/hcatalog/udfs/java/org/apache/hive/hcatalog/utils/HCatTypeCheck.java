begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|utils
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|EvalFunc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DataBag
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|Tuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|logicalLayer
operator|.
name|schema
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
name|pig
operator|.
name|impl
operator|.
name|util
operator|.
name|Utils
import|;
end_import

begin_comment
comment|/**  * This UDF can be used to check that a tuple presented by HCatLoader has the  * right types for the fields  *  * Usage is :  *  * register testudf.jar;  * a = load 'numbers' using HCatLoader(...);  * b = foreach a generate HCatTypeCheck('intnum1000:int,id:int,intnum5:int,intnum100:int,intnum:int,longnum:long,floatnum:float,doublenum:double', *);  * store b into 'output';  *  * The schema string (the first argument to the UDF) is of the form one would provide in a   * pig load statement.  *  * The output should only contain the value '1' in all rows. (This UDF returns  * the integer value 1 if all fields have the right type, else throws IOException)  *  */
end_comment

begin_class
specifier|public
class|class
name|HCatTypeCheck
extends|extends
name|EvalFunc
argument_list|<
name|Integer
argument_list|>
block|{
specifier|static
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|typeMap
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Integer
name|exec
parameter_list|(
name|Tuple
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|schemaStr
init|=
operator|(
name|String
operator|)
name|input
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Schema
name|s
init|=
literal|null
decl_stmt|;
try|try
block|{
name|s
operator|=
name|getSchemaFromString
argument_list|(
name|schemaStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|s
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|check
argument_list|(
name|s
operator|.
name|getField
argument_list|(
name|i
argument_list|)
operator|.
name|type
argument_list|,
name|input
operator|.
name|get
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// input.get(i+1) since input.get(0) is the schema;
block|}
return|return
literal|1
return|;
block|}
static|static
block|{
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|INTEGER
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|LONG
argument_list|,
name|Long
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|FLOAT
argument_list|,
name|Float
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|DOUBLE
argument_list|,
name|Double
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|CHARARRAY
argument_list|,
name|String
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|TUPLE
argument_list|,
name|Tuple
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|MAP
argument_list|,
name|Map
operator|.
name|class
argument_list|)
expr_stmt|;
name|typeMap
operator|.
name|put
argument_list|(
name|DataType
operator|.
name|BAG
argument_list|,
name|DataBag
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|die
parameter_list|(
name|String
name|expectedType
parameter_list|,
name|Object
name|o
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expected "
operator|+
name|expectedType
operator|+
literal|", got "
operator|+
name|o
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
specifier|private
name|String
name|check
parameter_list|(
name|Byte
name|type
parameter_list|,
name|Object
name|o
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
if|if
condition|(
name|check
argument_list|(
name|typeMap
operator|.
name|get
argument_list|(
name|type
argument_list|)
argument_list|,
name|o
argument_list|)
condition|)
block|{
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|DataType
operator|.
name|MAP
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|o
decl_stmt|;
name|check
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|DataType
operator|.
name|BAG
argument_list|)
condition|)
block|{
name|DataBag
name|bg
init|=
operator|(
name|DataBag
operator|)
name|o
decl_stmt|;
for|for
control|(
name|Tuple
name|tuple
range|:
name|bg
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|tuple
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|check
argument_list|(
name|m
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|DataType
operator|.
name|TUPLE
argument_list|)
condition|)
block|{
name|Tuple
name|t
init|=
operator|(
name|Tuple
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|check
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|||
operator|!
name|check
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|||
operator|!
name|check
argument_list|(
name|Double
operator|.
name|class
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
condition|)
block|{
name|die
argument_list|(
literal|"t:tuple(num:int,str:string,dbl:double)"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|die
argument_list|(
name|typeMap
operator|.
name|get
argument_list|(
name|type
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|o
argument_list|)
expr_stmt|;
block|}
return|return
name|o
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @param m    * @throws IOException    */
specifier|private
name|void
name|check
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|m
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// just access key and value to ensure they are correct
if|if
condition|(
operator|!
name|check
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|die
argument_list|(
literal|"String"
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|check
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|die
argument_list|(
literal|"String"
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|boolean
name|check
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|expected
parameter_list|,
name|Object
name|actual
parameter_list|)
block|{
if|if
condition|(
name|actual
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
name|expected
operator|.
name|isAssignableFrom
argument_list|(
name|actual
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
block|}
name|Schema
name|getSchemaFromString
parameter_list|(
name|String
name|schemaString
parameter_list|)
throws|throws
name|Exception
block|{
comment|/** ByteArrayInputStream stream = new ByteArrayInputStream(schemaString.getBytes()) ;      QueryParser queryParser = new QueryParser(stream) ;      Schema schema = queryParser.TupleSchema() ;      Schema.setSchemaDefaultType(schema, org.apache.pig.data.DataType.BYTEARRAY);      return schema;      */
return|return
name|Utils
operator|.
name|getSchemaFromString
argument_list|(
name|schemaString
argument_list|)
return|;
block|}
block|}
end_class

end_unit


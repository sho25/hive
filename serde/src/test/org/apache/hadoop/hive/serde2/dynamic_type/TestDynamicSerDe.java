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
name|serde2
operator|.
name|dynamic_type
package|;
end_package

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
name|HashMap
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
name|dynamic_type
operator|.
name|DynamicSerDe
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
name|thrift
operator|.
name|TCTLSeparatedProtocol
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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

begin_class
specifier|public
class|class
name|TestDynamicSerDe
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testDynamicSerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Try to construct an object
name|ArrayList
argument_list|<
name|String
argument_list|>
name|bye
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|bye
operator|.
name|add
argument_list|(
literal|"firstString"
argument_list|)
expr_stmt|;
name|bye
operator|.
name|add
argument_list|(
literal|"secondString"
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|another
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|another
operator|.
name|put
argument_list|(
literal|"firstKey"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|another
operator|.
name|put
argument_list|(
literal|"secondKey"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|struct
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|struct
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|234
argument_list|)
argument_list|)
expr_stmt|;
name|struct
operator|.
name|add
argument_list|(
name|bye
argument_list|)
expr_stmt|;
name|struct
operator|.
name|add
argument_list|(
name|another
argument_list|)
expr_stmt|;
comment|// All protocols
name|ArrayList
argument_list|<
name|String
argument_list|>
name|protocols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Boolean
argument_list|>
name|isBinaries
init|=
operator|new
name|ArrayList
argument_list|<
name|Boolean
argument_list|>
argument_list|()
decl_stmt|;
name|protocols
operator|.
name|add
argument_list|(
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|isBinaries
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|protocols
operator|.
name|add
argument_list|(
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TJSONProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|isBinaries
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// TSimpleJSONProtocol does not support deserialization.
comment|// protocols.add(com.facebook.thrift.protocol.TSimpleJSONProtocol.class.getName());
comment|// isBinaries.add(false);
comment|// TCTLSeparatedProtocol is not done yet.
name|protocols
operator|.
name|add
argument_list|(
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
name|TCTLSeparatedProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|isBinaries
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"input struct = "
operator|+
name|struct
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|pp
init|=
literal|0
init|;
name|pp
operator|<
name|protocols
operator|.
name|size
argument_list|()
condition|;
name|pp
operator|++
control|)
block|{
name|String
name|protocol
init|=
name|protocols
operator|.
name|get
argument_list|(
name|pp
argument_list|)
decl_stmt|;
name|boolean
name|isBinary
init|=
name|isBinaries
operator|.
name|get
argument_list|(
name|pp
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Testing protocol: "
operator|+
name|protocol
argument_list|)
expr_stmt|;
name|Properties
name|schema
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
name|protocol
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
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
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
literal|"struct test { i32 hello, list<string> bye, map<string,i32> another}"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LIB
argument_list|,
operator|new
name|DynamicSerDe
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|DynamicSerDe
name|serde
init|=
operator|new
name|DynamicSerDe
argument_list|()
decl_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
comment|// Try getObjectInspector
name|ObjectInspector
name|oi
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"TypeName = "
operator|+
name|oi
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try to serialize
name|BytesWritable
name|bytes
init|=
operator|(
name|BytesWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|struct
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|bytes
operator|.
name|getSize
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
operator|.
name|get
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|v
init|=
operator|(
name|b
operator|<
literal|0
condition|?
literal|256
operator|+
name|b
else|:
name|b
operator|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"x%02x"
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"bytes ="
operator|+
name|sb
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isBinary
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"bytes in text ="
operator|+
operator|new
name|String
argument_list|(
name|bytes
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Try to deserialize
name|Object
name|o
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o class = "
operator|+
name|o
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|olist
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|o
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o size = "
operator|+
name|olist
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o[0] class = "
operator|+
name|olist
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o[1] class = "
operator|+
name|olist
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o[2] class = "
operator|+
name|olist
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o = "
operator|+
name|o
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|o
argument_list|,
name|struct
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testConfigurableTCTLSeparated
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Try to construct an object
name|ArrayList
argument_list|<
name|String
argument_list|>
name|bye
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|bye
operator|.
name|add
argument_list|(
literal|"firstString"
argument_list|)
expr_stmt|;
name|bye
operator|.
name|add
argument_list|(
literal|"secondString"
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|another
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|another
operator|.
name|put
argument_list|(
literal|"firstKey"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|another
operator|.
name|put
argument_list|(
literal|"secondKey"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|struct
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|struct
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|234
argument_list|)
argument_list|)
expr_stmt|;
name|struct
operator|.
name|add
argument_list|(
name|bye
argument_list|)
expr_stmt|;
name|struct
operator|.
name|add
argument_list|(
name|another
argument_list|)
expr_stmt|;
name|Properties
name|schema
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
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
name|TCTLSeparatedProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
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
name|Constants
operator|.
name|META_TABLE_NAME
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_DDL
argument_list|,
literal|"struct test { i32 hello, list<string> bye, map<string,i32> another}"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_LIB
argument_list|,
operator|new
name|DynamicSerDe
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|FIELD_DELIM
argument_list|,
literal|"9"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|COLLECTION_DELIM
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LINE_DELIM
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|MAPKEY_DELIM
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
name|DynamicSerDe
name|serde
init|=
operator|new
name|DynamicSerDe
argument_list|()
decl_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|TCTLSeparatedProtocol
name|prot
init|=
operator|(
name|TCTLSeparatedProtocol
operator|)
name|serde
operator|.
name|oprot_
decl_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|getPrimarySeparator
argument_list|()
operator|==
literal|9
argument_list|)
expr_stmt|;
name|ObjectInspector
name|oi
init|=
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
comment|// Try to serialize
name|BytesWritable
name|bytes
init|=
operator|(
name|BytesWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|struct
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|bytes
operator|.
name|getSize
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
operator|.
name|get
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|v
init|=
operator|(
name|b
operator|<
literal|0
condition|?
literal|256
operator|+
name|b
else|:
name|b
operator|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"x%02x"
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"bytes ="
operator|+
name|sb
argument_list|)
expr_stmt|;
name|String
name|compare
init|=
literal|"234"
operator|+
literal|"\u0009"
operator|+
literal|"firstString"
operator|+
literal|"\u0001"
operator|+
literal|"secondString"
operator|+
literal|"\u0009"
operator|+
literal|"firstKey"
operator|+
literal|"\u0004"
operator|+
literal|"1"
operator|+
literal|"\u0001"
operator|+
literal|"secondKey"
operator|+
literal|"\u0004"
operator|+
literal|"2"
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"bytes in text ="
operator|+
operator|new
name|String
argument_list|(
name|bytes
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|getSize
argument_list|()
argument_list|)
operator|+
literal|">"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"compare to    ="
operator|+
name|compare
operator|+
literal|">"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|compare
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Try to deserialize
name|Object
name|o
init|=
name|serde
operator|.
name|deserialize
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o class = "
operator|+
name|o
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|olist
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|o
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o size = "
operator|+
name|olist
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o[0] class = "
operator|+
name|olist
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o[1] class = "
operator|+
name|olist
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o[2] class = "
operator|+
name|olist
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"o = "
operator|+
name|o
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|o
argument_list|,
name|struct
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit


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
package|;
end_package

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
name|thrift
operator|.
name|protocol
operator|.
name|TField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TStruct
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TMemoryBuffer
import|;
end_import

begin_class
specifier|public
class|class
name|TestTCTLSeparatedProtocol
extends|extends
name|TestCase
block|{
specifier|public
name|TestTCTLSeparatedProtocol
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|public
name|void
name|testReads
parameter_list|()
throws|throws
name|Exception
block|{
name|TMemoryBuffer
name|trans
init|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|String
name|foo
init|=
literal|"Hello"
decl_stmt|;
name|String
name|bar
init|=
literal|"World!"
decl_stmt|;
name|String
name|key
init|=
literal|"22"
decl_stmt|;
name|String
name|value
init|=
literal|"TheValue"
decl_stmt|;
name|String
name|key2
init|=
literal|"24"
decl_stmt|;
name|String
name|value2
init|=
literal|"TheValueAgain"
decl_stmt|;
name|byte
name|columnSeparator
index|[]
init|=
block|{
literal|1
block|}
decl_stmt|;
name|byte
name|elementSeparator
index|[]
init|=
block|{
literal|2
block|}
decl_stmt|;
name|byte
name|kvSeparator
index|[]
init|=
block|{
literal|3
block|}
decl_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|foo
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|foo
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|columnSeparator
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|columnSeparator
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|bar
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bar
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|columnSeparator
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|kvSeparator
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|value
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|elementSeparator
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|key2
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|key2
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|kvSeparator
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|value2
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|value2
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// use 3 as the row buffer size to force lots of re-buffering.
name|TCTLSeparatedProtocol
name|prot
init|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
name|prot
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|String
name|hello
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|hello
operator|.
name|equals
argument_list|(
name|foo
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
name|bar
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|TMap
name|mapHeader
init|=
name|prot
operator|.
name|readMapBegin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|mapHeader
operator|.
name|size
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readI32
argument_list|()
operator|==
literal|22
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readI32
argument_list|()
operator|==
literal|24
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
name|value2
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readMapEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|hello
operator|=
name|prot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|hello
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testWrites
parameter_list|()
throws|throws
name|Exception
block|{
name|TMemoryBuffer
name|trans
init|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|TCTLSeparatedProtocol
name|prot
init|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
name|prot
operator|.
name|writeStructBegin
argument_list|(
operator|new
name|TStruct
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeI32
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeListBegin
argument_list|(
operator|new
name|TList
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeDouble
argument_list|(
literal|348.55
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeDouble
argument_list|(
literal|234.22
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"hello world!"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeMapBegin
argument_list|(
operator|new
name|TMap
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"key1"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"val1"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"key2"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"val2"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"key3"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"val3"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeMapEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeListBegin
argument_list|(
operator|new
name|TList
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"elem1"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"elem2"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"bye!"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
name|trans
operator|.
name|flush
argument_list|()
expr_stmt|;
name|byte
name|b
index|[]
init|=
operator|new
name|byte
index|[
literal|3
operator|*
literal|1024
index|]
decl_stmt|;
name|int
name|len
init|=
name|trans
operator|.
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
decl_stmt|;
name|String
name|test
init|=
operator|new
name|String
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|String
name|testRef
init|=
literal|"100
literal|348.55
literal|234.22
literal|hello world!
literal|key1
literal|val1
literal|key2
literal|val2
literal|key3
literal|val3
literal|elem1
literal|elem2
literal|bye!"
decl_stmt|;
name|assertTrue
argument_list|(
name|test
operator|.
name|equals
argument_list|(
name|testRef
argument_list|)
argument_list|)
expr_stmt|;
name|trans
operator|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|1023
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
comment|//
comment|// read back!
comment|//
name|prot
operator|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|prot
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
expr_stmt|;
comment|// 100 is the start
name|prot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readI32
argument_list|()
operator|==
literal|100
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// let's see if doubles work ok
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|TList
name|l
init|=
name|prot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|l
operator|.
name|size
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readDouble
argument_list|()
operator|==
literal|348.55
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readDouble
argument_list|()
operator|==
literal|234.22
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// nice message
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hello world!"
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// 3 element map
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|TMap
name|m
init|=
name|prot
operator|.
name|readMapBegin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|m
operator|.
name|size
operator|==
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"key1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"val1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"key2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"val2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"key3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"val3"
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readMapEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// the 2 element list
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|l
operator|=
name|prot
operator|.
name|readListBegin
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|l
operator|.
name|size
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"elem1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"elem2"
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// final string
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"bye!"
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// shouldl return nulls at end
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// shouldl return nulls at end
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testQuotedWrites
parameter_list|()
throws|throws
name|Exception
block|{
name|TMemoryBuffer
name|trans
init|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|4096
argument_list|)
decl_stmt|;
name|TCTLSeparatedProtocol
name|prot
init|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|4096
argument_list|)
decl_stmt|;
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
name|QUOTE_CHAR
argument_list|,
literal|"\""
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
literal|","
argument_list|)
expr_stmt|;
name|prot
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
name|String
name|testStr
init|=
literal|"\"hello, world!\""
decl_stmt|;
name|prot
operator|.
name|writeStructBegin
argument_list|(
operator|new
name|TStruct
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
name|testStr
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeListBegin
argument_list|(
operator|new
name|TList
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"elem1"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"elem2"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeListEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
name|trans
operator|.
name|flush
argument_list|()
expr_stmt|;
name|byte
name|b
index|[]
init|=
operator|new
name|byte
index|[
literal|4096
index|]
decl_stmt|;
name|int
name|len
init|=
name|trans
operator|.
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
decl_stmt|;
name|trans
operator|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|4096
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|prot
operator|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|1024
argument_list|)
expr_stmt|;
name|prot
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
name|prot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|String
name|firstRead
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|testStr
operator|=
name|testStr
operator|.
name|replace
argument_list|(
literal|"\""
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testStr
argument_list|,
name|firstRead
argument_list|)
expr_stmt|;
comment|// the 2 element list
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|TList
name|l
init|=
name|prot
operator|.
name|readListBegin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|l
operator|.
name|size
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"elem1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"elem2"
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readListEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// shouldl return nulls at end
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
comment|// shouldl return nulls at end
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
block|}
comment|/**    * Tests a sample apache log format. This is actually better done in general with a more TRegexLike protocol, but for this    * case, TCTLSeparatedProtocol can do it.     */
specifier|public
name|void
name|test1ApacheLogFormat
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|sample
init|=
literal|"127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326"
decl_stmt|;
name|TMemoryBuffer
name|trans
init|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|4096
argument_list|)
decl_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|sample
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|sample
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|trans
operator|.
name|flush
argument_list|()
expr_stmt|;
name|TCTLSeparatedProtocol
name|prot
init|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|4096
argument_list|)
decl_stmt|;
name|Properties
name|schema
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// this is a hacky way of doing the quotes since it will match any 2 of these, so
comment|// "[ hello this is something to split [" would be considered to be quoted.
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|QUOTE_CHAR
argument_list|,
literal|"(\"|\\[|\\])"
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
literal|" "
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"-"
argument_list|)
expr_stmt|;
name|prot
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
name|prot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
comment|// ip address
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|String
name|ip
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|ip
argument_list|)
expr_stmt|;
comment|//  identd
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|String
name|identd
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|identd
argument_list|)
expr_stmt|;
comment|//  user
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|String
name|user
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"frank"
argument_list|,
name|user
argument_list|)
expr_stmt|;
comment|//  finishTime
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|String
name|finishTime
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"10/Oct/2000:13:55:36 -0700"
argument_list|,
name|finishTime
argument_list|)
expr_stmt|;
comment|//  requestLine
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|String
name|requestLine
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"GET /apache_pb.gif HTTP/1.0"
argument_list|,
name|requestLine
argument_list|)
expr_stmt|;
comment|//  returncode
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|int
name|returnCode
init|=
name|prot
operator|.
name|readI32
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|returnCode
argument_list|)
expr_stmt|;
comment|//  return size
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
specifier|final
name|int
name|returnSize
init|=
name|prot
operator|.
name|readI32
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2326
argument_list|,
name|returnSize
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readStructEnd
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testNulls
parameter_list|()
throws|throws
name|Exception
block|{
name|TMemoryBuffer
name|trans
init|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|TCTLSeparatedProtocol
name|prot
init|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|prot
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeStructBegin
argument_list|(
operator|new
name|TStruct
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeI32
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldBegin
argument_list|(
operator|new
name|TField
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeMapBegin
argument_list|(
operator|new
name|TMap
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"key2"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeString
argument_list|(
literal|"val3"
argument_list|)
expr_stmt|;
name|prot
operator|.
name|writeMapEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|writeStructEnd
argument_list|()
expr_stmt|;
name|byte
name|b
index|[]
init|=
operator|new
name|byte
index|[
literal|3
operator|*
literal|1024
index|]
decl_stmt|;
name|int
name|len
init|=
name|trans
operator|.
name|read
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|)
decl_stmt|;
name|String
name|written
init|=
operator|new
name|String
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
decl_stmt|;
name|String
name|testRef
init|=
literal|"\\N
literal|\\N
literal|100
literal|\\N
literal|\\N
literal|\\N
literal|key2
literal|\\N
literal|\\N
literal|val3"
decl_stmt|;
name|assertTrue
argument_list|(
name|testRef
operator|.
name|equals
argument_list|(
name|written
argument_list|)
argument_list|)
expr_stmt|;
name|trans
operator|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|1023
argument_list|)
expr_stmt|;
name|trans
operator|.
name|write
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|prot
operator|=
operator|new
name|TCTLSeparatedProtocol
argument_list|(
name|trans
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|prot
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readStructBegin
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|String
name|ret
init|=
name|prot
operator|.
name|readString
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|ret
operator|=
name|prot
operator|.
name|readString
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|ret
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|int
name|ret1
init|=
name|prot
operator|.
name|readI32
argument_list|()
decl_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|ret1
operator|==
literal|100
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|ret1
operator|=
name|prot
operator|.
name|readI32
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldBegin
argument_list|()
expr_stmt|;
name|TMap
name|map
init|=
name|prot
operator|.
name|readMapBegin
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|map
operator|.
name|size
operator|==
literal|3
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"key2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prot
operator|.
name|readString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"val3"
argument_list|)
argument_list|)
expr_stmt|;
name|prot
operator|.
name|readMapEnd
argument_list|()
expr_stmt|;
name|prot
operator|.
name|readFieldEnd
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|ret1
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


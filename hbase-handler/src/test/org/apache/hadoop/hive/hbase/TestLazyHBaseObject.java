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
name|hbase
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|Cell
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
name|hbase
operator|.
name|io
operator|.
name|HbaseMapWritable
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
name|hbase
operator|.
name|io
operator|.
name|RowResult
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
name|SerDeUtils
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
name|LazyFactory
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
name|LazyString
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
name|objectinspector
operator|.
name|LazyMapObjectInspector
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
name|objectinspector
operator|.
name|LazySimpleStructObjectInspector
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|IntWritable
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
name|Text
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

begin_comment
comment|/**  * TestLazyHBaseObject is a test for the LazyHBaseXXX classes.  */
end_comment

begin_class
specifier|public
class|class
name|TestLazyHBaseObject
extends|extends
name|TestCase
block|{
comment|/**    * Test the LazyMap class with Integer-to-String.    */
specifier|public
name|void
name|testLazyHBaseCellMap1
parameter_list|()
block|{
comment|// Map of Integer to String
name|Text
name|nullSequence
init|=
operator|new
name|Text
argument_list|(
literal|"\\N"
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|LazyFactory
operator|.
name|createLazyObjectInspector
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
literal|"map<int,string>"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|1
block|,
operator|(
name|byte
operator|)
literal|2
block|}
argument_list|,
literal|0
argument_list|,
name|nullSequence
argument_list|,
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
decl_stmt|;
name|LazyHBaseCellMap
name|b
init|=
operator|new
name|LazyHBaseCellMap
argument_list|(
operator|(
name|LazyMapObjectInspector
operator|)
name|oi
argument_list|)
decl_stmt|;
comment|// Intialize a row result
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:col1"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"cfacol1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:col2"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"cfacol2"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:2"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"def"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:-1"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:0"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"0"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:8"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"abc"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfc:col3"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"cfccol3"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|RowResult
name|rr
init|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
decl_stmt|;
name|b
operator|.
name|init
argument_list|(
name|rr
argument_list|,
literal|"cfb:"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Text
argument_list|(
literal|"def"
argument_list|)
argument_list|,
operator|(
operator|(
name|LazyString
operator|)
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|)
operator|.
name|getWritableObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|IntWritable
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
argument_list|,
operator|(
operator|(
name|LazyString
operator|)
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|)
operator|.
name|getWritableObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Text
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
operator|(
operator|(
name|LazyString
operator|)
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|8
argument_list|)
argument_list|)
operator|)
operator|.
name|getWritableObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|12345
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{0:'0',2:'def',8:'abc'}"
operator|.
name|replace
argument_list|(
literal|'\''
argument_list|,
literal|'\"'
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|b
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the LazyMap class with String-to-String.    */
specifier|public
name|void
name|testLazyHBaseCellMap2
parameter_list|()
block|{
comment|// Map of String to String
name|Text
name|nullSequence
init|=
operator|new
name|Text
argument_list|(
literal|"\\N"
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|LazyFactory
operator|.
name|createLazyObjectInspector
argument_list|(
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
literal|"map<string,string>"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
literal|'#'
block|,
operator|(
name|byte
operator|)
literal|'\t'
block|}
argument_list|,
literal|0
argument_list|,
name|nullSequence
argument_list|,
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
decl_stmt|;
name|LazyHBaseCellMap
name|b
init|=
operator|new
name|LazyHBaseCellMap
argument_list|(
operator|(
name|LazyMapObjectInspector
operator|)
name|oi
argument_list|)
decl_stmt|;
comment|// Intialize a row result
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:col1"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"cfacol1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:col2"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"cfacol2"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:2"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"d\tf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:-1"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:0"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"0"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:8"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"abc"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfc:col3"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"cfccol3"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|RowResult
name|rr
init|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
decl_stmt|;
name|b
operator|.
name|init
argument_list|(
name|rr
argument_list|,
literal|"cfb:"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Text
argument_list|(
literal|"d\tf"
argument_list|)
argument_list|,
operator|(
operator|(
name|LazyString
operator|)
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|Text
argument_list|(
literal|"2"
argument_list|)
argument_list|)
operator|)
operator|.
name|getWritableObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|Text
argument_list|(
literal|"-1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
argument_list|,
operator|(
operator|(
name|LazyString
operator|)
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|)
operator|.
name|getWritableObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|Text
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
operator|(
operator|(
name|LazyString
operator|)
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|Text
argument_list|(
literal|"8"
argument_list|)
argument_list|)
operator|)
operator|.
name|getWritableObject
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|b
operator|.
name|getMapValueElement
argument_list|(
operator|new
name|Text
argument_list|(
literal|"-"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{'0':'0','2':'d\\tf','8':'abc'}"
operator|.
name|replace
argument_list|(
literal|'\''
argument_list|,
literal|'\"'
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|b
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the LazyHBaseRow class with one-for-one mappings between    * Hive fields and HBase columns.    */
specifier|public
name|void
name|testLazyHBaseRow1
parameter_list|()
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
literal|"string,int,array<string>,map<string,string>,string"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"key"
block|,
literal|"a"
block|,
literal|"b"
block|,
literal|"c"
block|,
literal|"d"
block|}
argument_list|)
decl_stmt|;
name|Text
name|nullSequence
init|=
operator|new
name|Text
argument_list|(
literal|"\\N"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"cfa:a"
block|,
literal|"cfa:b"
block|,
literal|"cfb:c"
block|,
literal|"cfb:d"
block|}
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|LazyFactory
operator|.
name|createLazyStructInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypeInfos
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|' '
block|,
literal|':'
block|,
literal|'='
block|}
argument_list|,
name|nullSequence
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
decl_stmt|;
name|LazyHBaseRow
name|o
init|=
operator|new
name|LazyHBaseRow
argument_list|(
operator|(
name|LazySimpleStructObjectInspector
operator|)
name|oi
argument_list|)
decl_stmt|;
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:a"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"a:b:c"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:c"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"d=e:f=g"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"hi"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|RowResult
name|rr
init|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
decl_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':123,'b':['a','b','c'],"
operator|+
literal|"'c':{'d':'e','f':'g'},'d':'hi'}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:a"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:c"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"d=e:f=g"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':123,'b':null,"
operator|+
literal|"'c':{'d':'e','f':'g'},'d':null}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"a"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:c"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"d=\\N:f=g:h"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"no"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':null,'b':['a'],"
operator|+
literal|"'c':{'d':null,'f':'g','h':null},'d':'no'}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|":a::"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"no"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':null,'b':['','a','',''],"
operator|+
literal|"'c':null,'d':'no'}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:a"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:c"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{'key':'test-row','a':123,'b':[],'c':{},'d':''}"
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test the LazyHBaseRow class with a mapping from a Hive field to    * an HBase column family.    */
specifier|public
name|void
name|testLazyHBaseRow2
parameter_list|()
block|{
comment|// column family is mapped to Map<string,string>
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|fieldTypeInfos
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
literal|"string,int,array<string>,map<string,string>,string"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"key"
block|,
literal|"a"
block|,
literal|"b"
block|,
literal|"c"
block|,
literal|"d"
block|}
argument_list|)
decl_stmt|;
name|Text
name|nullSequence
init|=
operator|new
name|Text
argument_list|(
literal|"\\N"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hbaseColumnNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"cfa:a"
block|,
literal|"cfa:b"
block|,
literal|"cfb:"
block|,
literal|"cfc:d"
block|}
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|LazyFactory
operator|.
name|createLazyStructInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypeInfos
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|' '
block|,
literal|':'
block|,
literal|'='
block|}
argument_list|,
name|nullSequence
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
decl_stmt|;
name|LazyHBaseRow
name|o
init|=
operator|new
name|LazyHBaseRow
argument_list|(
operator|(
name|LazySimpleStructObjectInspector
operator|)
name|oi
argument_list|)
decl_stmt|;
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:a"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"a:b:c"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"e"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:f"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"g"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfc:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"hi"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|RowResult
name|rr
init|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
decl_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':123,'b':['a','b','c'],"
operator|+
literal|"'c':{'d':'e','f':'g'},'d':'hi'}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:a"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"e"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:f"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"g"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':123,'b':null,"
operator|+
literal|"'c':{'d':'e','f':'g'},'d':null}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"a"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfb:f"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"g"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfc:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"no"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':null,'b':['a'],"
operator|+
literal|"'c':{'f':'g'},'d':'no'}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|":a::"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfc:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"no"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
literal|"{'key':'test-row','a':null,'b':['','a','',''],"
operator|+
literal|"'c':{},'d':'no'}"
operator|)
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:a"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfa:b"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|cells
operator|.
name|put
argument_list|(
literal|"cfc:d"
operator|.
name|getBytes
argument_list|()
argument_list|,
operator|new
name|Cell
argument_list|(
literal|""
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|rr
operator|=
operator|new
name|RowResult
argument_list|(
literal|"test-row"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|o
operator|.
name|init
argument_list|(
name|rr
argument_list|,
name|hbaseColumnNames
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{'key':'test-row','a':123,'b':[],'c':{},'d':''}"
operator|.
name|replace
argument_list|(
literal|"'"
argument_list|,
literal|"\""
argument_list|)
argument_list|,
name|SerDeUtils
operator|.
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
import|;
end_import

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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|DefaultHCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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

begin_class
specifier|public
class|class
name|TestDefaultHCatRecord
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testRYW
parameter_list|()
throws|throws
name|IOException
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
literal|"binary.dat"
argument_list|)
decl_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
name|f
operator|.
name|createNewFile
argument_list|()
expr_stmt|;
name|f
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|OutputStream
name|fileOutStream
init|=
operator|new
name|FileOutputStream
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|DataOutput
name|outStream
init|=
operator|new
name|DataOutputStream
argument_list|(
name|fileOutStream
argument_list|)
decl_stmt|;
name|HCatRecord
index|[]
name|recs
init|=
name|getHCatRecords
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
name|recs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|recs
index|[
name|i
index|]
operator|.
name|write
argument_list|(
name|outStream
argument_list|)
expr_stmt|;
block|}
name|fileOutStream
operator|.
name|flush
argument_list|()
expr_stmt|;
name|fileOutStream
operator|.
name|close
argument_list|()
expr_stmt|;
name|InputStream
name|fInStream
init|=
operator|new
name|FileInputStream
argument_list|(
name|f
argument_list|)
decl_stmt|;
name|DataInput
name|inpStream
init|=
operator|new
name|DataInputStream
argument_list|(
name|fInStream
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
name|recs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|HCatRecord
name|rec
init|=
operator|new
name|DefaultHCatRecord
argument_list|()
decl_stmt|;
name|rec
operator|.
name|readFields
argument_list|(
name|inpStream
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|recs
index|[
name|i
index|]
argument_list|,
name|rec
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
name|fInStream
operator|.
name|available
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|fInStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testCompareTo
parameter_list|()
block|{
name|HCatRecord
index|[]
name|recs
init|=
name|getHCatRecords
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|recs
index|[
literal|0
index|]
operator|.
name|compareTo
argument_list|(
name|recs
index|[
literal|1
index|]
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testEqualsObject
parameter_list|()
block|{
name|HCatRecord
index|[]
name|recs
init|=
name|getHCatRecords
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|recs
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
name|recs
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HCatRecord
index|[]
name|getHCatRecords
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rec_1
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
operator|new
name|Short
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
operator|new
name|Long
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
literal|"howl and hadoop"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_1
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_2
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
operator|new
name|Short
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
operator|new
name|Long
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
literal|"howl and hadoop"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_2
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_3
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
operator|new
name|Short
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
operator|new
name|Long
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
literal|"howl and hadoop"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|innerList
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|innerList
operator|.
name|add
argument_list|(
literal|314
argument_list|)
expr_stmt|;
name|innerList
operator|.
name|add
argument_list|(
literal|007
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
name|innerList
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Short
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|Short
argument_list|,
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|Short
argument_list|(
literal|"2"
argument_list|)
argument_list|,
literal|"howl is cool"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|Short
argument_list|(
literal|"3"
argument_list|)
argument_list|,
literal|"is it?"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|Short
argument_list|(
literal|"4"
argument_list|)
argument_list|,
literal|"or is it not?"
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_3
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_3
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_4
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
operator|new
name|Short
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
operator|new
name|Long
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
literal|"howl and hadoop"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Short
argument_list|,
name|String
argument_list|>
name|map2
init|=
operator|new
name|HashMap
argument_list|<
name|Short
argument_list|,
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|map2
operator|.
name|put
argument_list|(
operator|new
name|Short
argument_list|(
literal|"2"
argument_list|)
argument_list|,
literal|"howl is cool"
argument_list|)
expr_stmt|;
name|map2
operator|.
name|put
argument_list|(
operator|new
name|Short
argument_list|(
literal|"3"
argument_list|)
argument_list|,
literal|"is it?"
argument_list|)
expr_stmt|;
name|map2
operator|.
name|put
argument_list|(
operator|new
name|Short
argument_list|(
literal|"4"
argument_list|)
argument_list|,
literal|"or is it not?"
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
name|map2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|innerList2
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|innerList2
operator|.
name|add
argument_list|(
literal|314
argument_list|)
expr_stmt|;
name|innerList2
operator|.
name|add
argument_list|(
literal|007
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
name|innerList2
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_4
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_4
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatRecord
index|[]
block|{
name|tup_1
block|,
name|tup_2
block|,
name|tup_3
block|,
name|tup_4
block|}
return|;
block|}
block|}
end_class

end_unit


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
name|util
operator|.
name|Calendar
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
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|Date
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
name|common
operator|.
name|type
operator|.
name|HiveChar
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|common
operator|.
name|type
operator|.
name|HiveVarchar
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
name|common
operator|.
name|type
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchemaUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|parser
operator|.
name|AliasMasker
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestDefaultHCatRecord.  */
end_comment

begin_class
specifier|public
class|class
name|TestDefaultHCatRecord
block|{
comment|/**    * test that we properly serialize/deserialize HCatRecordS    * @throws IOException    */
annotation|@
name|Test
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
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"recs["
operator|+
name|i
operator|+
literal|"]='"
operator|+
name|recs
index|[
name|i
index|]
operator|+
literal|"' rec='"
operator|+
name|rec
operator|+
literal|"'"
argument_list|)
decl_stmt|;
name|boolean
name|isEqual
init|=
name|HCatDataCheckUtil
operator|.
name|recordsEqual
argument_list|(
name|recs
index|[
name|i
index|]
argument_list|,
name|rec
argument_list|,
name|msg
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|msg
operator|.
name|toString
argument_list|()
argument_list|,
name|isEqual
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
annotation|@
name|Test
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
name|assertTrue
argument_list|(
name|HCatDataCheckUtil
operator|.
name|compareRecords
argument_list|(
name|recs
index|[
literal|0
index|]
argument_list|,
name|recs
index|[
literal|1
index|]
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatDataCheckUtil
operator|.
name|compareRecords
argument_list|(
name|recs
index|[
literal|4
index|]
argument_list|,
name|recs
index|[
literal|5
index|]
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
name|HCatDataCheckUtil
operator|.
name|recordsEqual
argument_list|(
name|recs
index|[
literal|0
index|]
argument_list|,
name|recs
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatDataCheckUtil
operator|.
name|recordsEqual
argument_list|(
name|recs
index|[
literal|4
index|]
argument_list|,
name|recs
index|[
literal|5
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test get and set calls with type    * @throws HCatException    */
annotation|@
name|Test
specifier|public
name|void
name|testGetSetByType1
parameter_list|()
throws|throws
name|HCatException
block|{
name|HCatRecord
name|inpRec
init|=
name|getHCatRecords
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|HCatRecord
name|newRec
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|inpRec
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|HCatSchema
name|hsch
init|=
name|HCatSchemaUtils
operator|.
name|getHCatSchema
argument_list|(
literal|"a:tinyint,b:smallint,c:int,d:bigint,e:float,f:double,g:boolean,h:string,i:binary,j:string"
argument_list|)
decl_stmt|;
name|newRec
operator|.
name|setByte
argument_list|(
literal|"a"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getByte
argument_list|(
literal|"a"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setShort
argument_list|(
literal|"b"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getShort
argument_list|(
literal|"b"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setInteger
argument_list|(
literal|"c"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getInteger
argument_list|(
literal|"c"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setLong
argument_list|(
literal|"d"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getLong
argument_list|(
literal|"d"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setFloat
argument_list|(
literal|"e"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getFloat
argument_list|(
literal|"e"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setDouble
argument_list|(
literal|"f"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getDouble
argument_list|(
literal|"f"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setBoolean
argument_list|(
literal|"g"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getBoolean
argument_list|(
literal|"g"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setString
argument_list|(
literal|"h"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getString
argument_list|(
literal|"h"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setByteArray
argument_list|(
literal|"i"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getByteArray
argument_list|(
literal|"i"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setString
argument_list|(
literal|"j"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getString
argument_list|(
literal|"j"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatDataCheckUtil
operator|.
name|recordsEqual
argument_list|(
name|newRec
argument_list|,
name|inpRec
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test get and set calls with type    * @throws HCatException    */
annotation|@
name|Test
specifier|public
name|void
name|testGetSetByType2
parameter_list|()
throws|throws
name|HCatException
block|{
name|HCatRecord
name|inpRec
init|=
name|getGetSet2InpRec
argument_list|()
decl_stmt|;
name|HCatRecord
name|newRec
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|inpRec
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|HCatSchema
name|hsch
init|=
name|HCatSchemaUtils
operator|.
name|getHCatSchema
argument_list|(
literal|"a:binary,b:map<string,string>,c:array<int>,d:struct<i:int>"
argument_list|)
decl_stmt|;
name|newRec
operator|.
name|setByteArray
argument_list|(
literal|"a"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getByteArray
argument_list|(
literal|"a"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setMap
argument_list|(
literal|"b"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getMap
argument_list|(
literal|"b"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setList
argument_list|(
literal|"c"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getList
argument_list|(
literal|"c"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setStruct
argument_list|(
literal|"d"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getStruct
argument_list|(
literal|"d"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HCatDataCheckUtil
operator|.
name|recordsEqual
argument_list|(
name|newRec
argument_list|,
name|inpRec
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test type specific get/set methods on HCatRecord types added in Hive 13    * @throws HCatException    */
annotation|@
name|Test
specifier|public
name|void
name|testGetSetByType3
parameter_list|()
throws|throws
name|HCatException
block|{
name|HCatRecord
name|inpRec
init|=
name|getHCat13TypesRecord
argument_list|()
decl_stmt|;
name|HCatRecord
name|newRec
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|inpRec
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|HCatSchema
name|hsch
init|=
name|HCatSchemaUtils
operator|.
name|getHCatSchema
argument_list|(
literal|"a:decimal(5,2),b:char(10),c:varchar(20),d:date,e:timestamp"
argument_list|)
decl_stmt|;
name|newRec
operator|.
name|setDecimal
argument_list|(
literal|"a"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getDecimal
argument_list|(
literal|"a"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setChar
argument_list|(
literal|"b"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getChar
argument_list|(
literal|"b"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setVarchar
argument_list|(
literal|"c"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getVarchar
argument_list|(
literal|"c"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setDate
argument_list|(
literal|"d"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getDate
argument_list|(
literal|"d"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
name|newRec
operator|.
name|setTimestamp
argument_list|(
literal|"e"
argument_list|,
name|hsch
argument_list|,
name|inpRec
operator|.
name|getTimestamp
argument_list|(
literal|"e"
argument_list|,
name|hsch
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HCatRecord
name|getGetSet2InpRec
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rlist
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|rlist
operator|.
name|add
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Short
argument_list|,
name|String
argument_list|>
name|mapcol
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
name|mapcol
operator|.
name|put
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"2"
argument_list|)
argument_list|,
literal|"hcat is cool"
argument_list|)
expr_stmt|;
name|mapcol
operator|.
name|put
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"3"
argument_list|)
argument_list|,
literal|"is it?"
argument_list|)
expr_stmt|;
name|mapcol
operator|.
name|put
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"4"
argument_list|)
argument_list|,
literal|"or is it not?"
argument_list|)
expr_stmt|;
name|rlist
operator|.
name|add
argument_list|(
name|mapcol
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|listcol
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|listcol
operator|.
name|add
argument_list|(
literal|314
argument_list|)
expr_stmt|;
name|listcol
operator|.
name|add
argument_list|(
literal|007
argument_list|)
expr_stmt|;
name|rlist
operator|.
name|add
argument_list|(
name|listcol
argument_list|)
expr_stmt|;
comment|//list
name|rlist
operator|.
name|add
argument_list|(
name|listcol
argument_list|)
expr_stmt|;
comment|//struct
return|return
operator|new
name|DefaultHCatRecord
argument_list|(
name|rlist
argument_list|)
return|;
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
name|Byte
operator|.
name|valueOf
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
name|Float
operator|.
name|valueOf
argument_list|(
literal|5.3F
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|rec_1
operator|.
name|add
argument_list|(
literal|"hcat and hadoop"
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
name|Byte
operator|.
name|valueOf
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
name|Float
operator|.
name|valueOf
argument_list|(
literal|5.3F
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|rec_2
operator|.
name|add
argument_list|(
literal|"hcat and hadoop"
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
name|Byte
operator|.
name|valueOf
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_3
operator|.
name|add
argument_list|(
literal|"hcat and hadoop"
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
name|Short
operator|.
name|valueOf
argument_list|(
literal|"2"
argument_list|)
argument_list|,
literal|"hcat is cool"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|Short
operator|.
name|valueOf
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
name|Short
operator|.
name|valueOf
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
name|Byte
operator|.
name|valueOf
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
name|Short
operator|.
name|valueOf
argument_list|(
literal|"456"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|789
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
literal|5.3D
argument_list|)
argument_list|)
expr_stmt|;
name|rec_4
operator|.
name|add
argument_list|(
literal|"hcat and hadoop"
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
name|Short
operator|.
name|valueOf
argument_list|(
literal|"2"
argument_list|)
argument_list|,
literal|"hcat is cool"
argument_list|)
expr_stmt|;
name|map2
operator|.
name|put
argument_list|(
name|Short
operator|.
name|valueOf
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
name|Short
operator|.
name|valueOf
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
name|List
argument_list|<
name|Object
argument_list|>
name|rec_5
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|rec_5
operator|.
name|add
argument_list|(
name|getByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|rec_5
operator|.
name|add
argument_list|(
name|getStruct
argument_list|()
argument_list|)
expr_stmt|;
name|rec_5
operator|.
name|add
argument_list|(
name|getList
argument_list|()
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_5
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_5
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_6
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|rec_6
operator|.
name|add
argument_list|(
name|getByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|rec_6
operator|.
name|add
argument_list|(
name|getStruct
argument_list|()
argument_list|)
expr_stmt|;
name|rec_6
operator|.
name|add
argument_list|(
name|getList
argument_list|()
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_6
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_6
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
block|,
name|tup_5
block|,
name|tup_6
block|,
name|getHCat13TypesRecord
argument_list|()
block|,
name|getHCat13TypesComplexRecord
argument_list|()
block|}
return|;
block|}
specifier|private
specifier|static
name|HCatRecord
name|getHCat13TypesRecord
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rec_hcat13types
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|rec_hcat13types
operator|.
name|add
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"123.45"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//prec 5, scale 2
name|rec_hcat13types
operator|.
name|add
argument_list|(
operator|new
name|HiveChar
argument_list|(
literal|"hive_char"
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|rec_hcat13types
operator|.
name|add
argument_list|(
operator|new
name|HiveVarchar
argument_list|(
literal|"hive_varchar"
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|rec_hcat13types
operator|.
name|add
argument_list|(
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2014-01-06"
argument_list|)
argument_list|)
expr_stmt|;
name|rec_hcat13types
operator|.
name|add
argument_list|(
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_hcat13types
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|HCatRecord
name|getHCat13TypesComplexRecord
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rec_hcat13ComplexTypes
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|HiveDecimal
argument_list|,
name|String
argument_list|>
name|m
init|=
operator|new
name|HashMap
argument_list|<
name|HiveDecimal
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|m
operator|.
name|put
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"1234.12"
argument_list|)
argument_list|)
argument_list|,
literal|"1234.12"
argument_list|)
expr_stmt|;
name|m
operator|.
name|put
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|new
name|BigDecimal
argument_list|(
literal|"1234.13"
argument_list|)
argument_list|)
argument_list|,
literal|"1234.13"
argument_list|)
expr_stmt|;
name|rec_hcat13ComplexTypes
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Timestamp
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|m2
init|=
operator|new
name|HashMap
argument_list|<
name|Timestamp
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|Date
operator|.
name|valueOf
argument_list|(
literal|"2014-01-05"
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|HashMap
argument_list|<
name|HiveDecimal
argument_list|,
name|String
argument_list|>
argument_list|(
name|m
argument_list|)
argument_list|)
expr_stmt|;
name|m2
operator|.
name|put
argument_list|(
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|rec_hcat13ComplexTypes
operator|.
name|add
argument_list|(
name|m2
argument_list|)
expr_stmt|;
return|return
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_hcat13ComplexTypes
argument_list|)
return|;
block|}
specifier|private
name|Object
name|getList
parameter_list|()
block|{
return|return
name|getStruct
argument_list|()
return|;
block|}
specifier|private
name|Object
name|getByteArray
parameter_list|()
block|{
return|return
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
return|;
block|}
specifier|private
name|List
argument_list|<
name|?
argument_list|>
name|getStruct
parameter_list|()
block|{
name|List
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
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|struct
operator|.
name|add
argument_list|(
literal|"x"
argument_list|)
expr_stmt|;
return|return
name|struct
return|;
block|}
block|}
end_class

end_unit


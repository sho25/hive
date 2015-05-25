begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|serde
package|;
end_package

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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|serde
operator|.
name|primitive
operator|.
name|ParquetPrimitiveInspectorFactory
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
name|io
operator|.
name|ShortWritable
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
name|io
operator|.
name|ArrayWritable
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
name|Writable
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

begin_class
specifier|public
class|class
name|TestDeepParquetHiveMapInspector
extends|extends
name|TestCase
block|{
specifier|private
name|DeepParquetHiveMapInspector
name|inspector
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|inspector
operator|=
operator|new
name|DeepParquetHiveMapInspector
argument_list|(
name|ParquetPrimitiveInspectorFactory
operator|.
name|parquetShortInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullMap
parameter_list|()
block|{
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
literal|null
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNullContainer
parameter_list|()
block|{
specifier|final
name|ArrayWritable
name|map
init|=
operator|new
name|ArrayWritable
argument_list|(
name|ArrayWritable
operator|.
name|class
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmptyContainer
parameter_list|()
block|{
specifier|final
name|ArrayWritable
name|map
init|=
operator|new
name|ArrayWritable
argument_list|(
name|ArrayWritable
operator|.
name|class
argument_list|,
operator|new
name|ArrayWritable
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegularMap
parameter_list|()
block|{
specifier|final
name|Writable
index|[]
name|entry1
init|=
operator|new
name|Writable
index|[]
block|{
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
block|}
decl_stmt|;
specifier|final
name|Writable
index|[]
name|entry2
init|=
operator|new
name|Writable
index|[]
block|{
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
block|}
decl_stmt|;
specifier|final
name|ArrayWritable
name|map
init|=
operator|new
name|ArrayWritable
argument_list|(
name|ArrayWritable
operator|.
name|class
argument_list|,
operator|new
name|Writable
index|[]
block|{
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|entry1
argument_list|)
block|,
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|entry2
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashMap
parameter_list|()
block|{
specifier|final
name|Map
argument_list|<
name|Writable
argument_list|,
name|Writable
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|Writable
argument_list|,
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|6
argument_list|)
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|7
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|6
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
operator|new
name|IntWritable
argument_list|(
literal|7
argument_list|)
argument_list|,
name|inspector
operator|.
name|getMapValueElement
argument_list|(
name|map
argument_list|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|6
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
comment|/**  * TestParquetHiveArrayInspector.  */
end_comment

begin_class
specifier|public
class|class
name|TestParquetHiveArrayInspector
block|{
specifier|private
name|ParquetHiveArrayInspector
name|inspector
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|inspector
operator|=
operator|new
name|ParquetHiveArrayInspector
argument_list|(
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
name|testNullArray
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"Wrong size"
argument_list|,
operator|-
literal|1
argument_list|,
name|inspector
operator|.
name|getListLength
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getList
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getListElement
argument_list|(
literal|null
argument_list|,
literal|0
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
name|list
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
name|assertEquals
argument_list|(
literal|"Wrong size"
argument_list|,
operator|-
literal|1
argument_list|,
name|inspector
operator|.
name|getListLength
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getList
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getListElement
argument_list|(
name|list
argument_list|,
literal|0
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
name|list
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
name|assertEquals
argument_list|(
literal|"Wrong size"
argument_list|,
literal|0
argument_list|,
name|inspector
operator|.
name|getListLength
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Should not be null"
argument_list|,
name|inspector
operator|.
name|getList
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getListElement
argument_list|(
name|list
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegularList
parameter_list|()
block|{
specifier|final
name|ArrayWritable
name|list
init|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
operator|new
name|Writable
index|[]
block|{
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
block|}
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Writable
argument_list|>
name|expected
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|.
name|add
argument_list|(
operator|new
name|IntWritable
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong size"
argument_list|,
literal|3
argument_list|,
name|inspector
operator|.
name|getListLength
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
name|expected
argument_list|,
name|inspector
operator|.
name|getList
argument_list|(
name|list
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|"Wrong result of inspection"
argument_list|,
name|expected
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|inspector
operator|.
name|getListElement
argument_list|(
name|list
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNull
argument_list|(
literal|"Should be null"
argument_list|,
name|inspector
operator|.
name|getListElement
argument_list|(
name|list
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


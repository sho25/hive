begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|storage
operator|.
name|jdbc
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
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
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|mapred
operator|.
name|InputSplit
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
name|mapred
operator|.
name|JobConf
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
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
name|storage
operator|.
name|jdbc
operator|.
name|dao
operator|.
name|DatabaseAccessor
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
name|storage
operator|.
name|jdbc
operator|.
name|exception
operator|.
name|HiveJdbcDatabaseAccessException
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

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|JdbcInputFormatTest
block|{
annotation|@
name|Mock
specifier|private
name|DatabaseAccessor
name|mockDatabaseAccessor
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSplitLogic_noSpillOver
parameter_list|()
throws|throws
name|HiveJdbcDatabaseAccessException
throws|,
name|IOException
block|{
name|JdbcInputFormat
name|f
init|=
operator|new
name|JdbcInputFormat
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|mockDatabaseAccessor
operator|.
name|getTotalNumberOfRecords
argument_list|(
name|any
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|f
operator|.
name|setDbAccessor
argument_list|(
name|mockDatabaseAccessor
argument_list|)
expr_stmt|;
name|JobConf
name|conf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.input.dir"
argument_list|,
literal|"/temp"
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|f
operator|.
name|getSplits
argument_list|(
name|conf
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|splits
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|splits
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|splits
index|[
literal|0
index|]
operator|.
name|getLength
argument_list|()
argument_list|,
name|is
argument_list|(
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplitLogic_withSpillOver
parameter_list|()
throws|throws
name|HiveJdbcDatabaseAccessException
throws|,
name|IOException
block|{
name|JdbcInputFormat
name|f
init|=
operator|new
name|JdbcInputFormat
argument_list|()
decl_stmt|;
name|when
argument_list|(
name|mockDatabaseAccessor
operator|.
name|getTotalNumberOfRecords
argument_list|(
name|any
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|f
operator|.
name|setDbAccessor
argument_list|(
name|mockDatabaseAccessor
argument_list|)
expr_stmt|;
name|JobConf
name|conf
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.input.dir"
argument_list|,
literal|"/temp"
argument_list|)
expr_stmt|;
name|InputSplit
index|[]
name|splits
init|=
name|f
operator|.
name|getSplits
argument_list|(
name|conf
argument_list|,
literal|6
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|splits
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|splits
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|6
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|splits
index|[
name|i
index|]
operator|.
name|getLength
argument_list|()
argument_list|,
name|is
argument_list|(
literal|3L
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|3
init|;
name|i
operator|<
literal|6
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|splits
index|[
name|i
index|]
operator|.
name|getLength
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


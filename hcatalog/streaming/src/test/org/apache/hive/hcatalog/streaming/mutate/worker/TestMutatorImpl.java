begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|is
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
name|Matchers
operator|.
name|eq
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
name|never
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
name|verify
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|fs
operator|.
name|Path
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
name|conf
operator|.
name|HiveConf
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
name|AcidOutputFormat
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
name|AcidOutputFormat
operator|.
name|Options
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
name|RecordUpdater
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
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Captor
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
name|TestMutatorImpl
block|{
specifier|private
specifier|static
specifier|final
name|Object
name|RECORD
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RECORD_ID_COLUMN
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BUCKET_ID
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Path
name|PATH
init|=
operator|new
name|Path
argument_list|(
literal|"X"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|WRITE_ID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|AcidOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|mockOutputFormat
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|ObjectInspector
name|mockObjectInspector
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|RecordUpdater
name|mockRecordUpdater
decl_stmt|;
annotation|@
name|Captor
specifier|private
name|ArgumentCaptor
argument_list|<
name|AcidOutputFormat
operator|.
name|Options
argument_list|>
name|captureOptions
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|configuration
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
name|Mutator
name|mutator
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|injectMocks
parameter_list|()
throws|throws
name|IOException
block|{
name|when
argument_list|(
name|mockOutputFormat
operator|.
name|getRecordUpdater
argument_list|(
name|eq
argument_list|(
name|PATH
argument_list|)
argument_list|,
name|any
argument_list|(
name|Options
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRecordUpdater
argument_list|)
expr_stmt|;
name|mutator
operator|=
operator|new
name|MutatorImpl
argument_list|(
name|configuration
argument_list|,
name|RECORD_ID_COLUMN
argument_list|,
name|mockObjectInspector
argument_list|,
name|mockOutputFormat
argument_list|,
name|WRITE_ID
argument_list|,
name|PATH
argument_list|,
name|BUCKET_ID
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCreatesRecordReader
parameter_list|()
throws|throws
name|IOException
block|{
name|verify
argument_list|(
name|mockOutputFormat
argument_list|)
operator|.
name|getRecordUpdater
argument_list|(
name|eq
argument_list|(
name|PATH
argument_list|)
argument_list|,
name|captureOptions
operator|.
name|capture
argument_list|()
argument_list|)
expr_stmt|;
name|Options
name|options
init|=
name|captureOptions
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|options
operator|.
name|getBucketId
argument_list|()
argument_list|,
name|is
argument_list|(
name|BUCKET_ID
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|options
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|is
argument_list|(
operator|(
name|Configuration
operator|)
name|configuration
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|options
operator|.
name|getInspector
argument_list|()
argument_list|,
name|is
argument_list|(
name|mockObjectInspector
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|options
operator|.
name|getRecordIdColumn
argument_list|()
argument_list|,
name|is
argument_list|(
name|RECORD_ID_COLUMN
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|options
operator|.
name|getMinimumWriteId
argument_list|()
argument_list|,
name|is
argument_list|(
name|WRITE_ID
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|options
operator|.
name|getMaximumWriteId
argument_list|()
argument_list|,
name|is
argument_list|(
name|WRITE_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInsertDelegates
parameter_list|()
throws|throws
name|IOException
block|{
name|mutator
operator|.
name|insert
argument_list|(
name|RECORD
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockRecordUpdater
argument_list|)
operator|.
name|insert
argument_list|(
name|WRITE_ID
argument_list|,
name|RECORD
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUpdateDelegates
parameter_list|()
throws|throws
name|IOException
block|{
name|mutator
operator|.
name|update
argument_list|(
name|RECORD
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockRecordUpdater
argument_list|)
operator|.
name|update
argument_list|(
name|WRITE_ID
argument_list|,
name|RECORD
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteDelegates
parameter_list|()
throws|throws
name|IOException
block|{
name|mutator
operator|.
name|delete
argument_list|(
name|RECORD
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockRecordUpdater
argument_list|)
operator|.
name|delete
argument_list|(
name|WRITE_ID
argument_list|,
name|RECORD
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCloseDelegates
parameter_list|()
throws|throws
name|IOException
block|{
name|mutator
operator|.
name|close
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|mockRecordUpdater
argument_list|)
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFlushDoesNothing
parameter_list|()
throws|throws
name|IOException
block|{
name|mutator
operator|.
name|flush
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|mockRecordUpdater
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|BucketCodec
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
name|RecordIdentifier
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
name|ObjectInspectorFactory
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
name|streaming
operator|.
name|mutate
operator|.
name|MutableRecord
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
name|TestBucketIdResolverImpl
block|{
specifier|private
specifier|static
specifier|final
name|int
name|TOTAL_BUCKETS
init|=
literal|12
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RECORD_ID_COLUMN
init|=
literal|2
decl_stmt|;
comment|// id - TODO: use a non-zero index to check for offset errors.
specifier|private
specifier|static
specifier|final
name|int
index|[]
name|BUCKET_COLUMN_INDEXES
init|=
operator|new
name|int
index|[]
block|{
literal|0
block|}
decl_stmt|;
specifier|private
name|BucketIdResolver
name|capturingBucketIdResolver
init|=
operator|new
name|BucketIdResolverImpl
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|MutableRecord
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
argument_list|,
name|RECORD_ID_COLUMN
argument_list|,
name|TOTAL_BUCKETS
argument_list|,
name|BUCKET_COLUMN_INDEXES
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testAttachBucketIdToRecord
parameter_list|()
block|{
name|MutableRecord
name|record
init|=
operator|new
name|MutableRecord
argument_list|(
literal|1
argument_list|,
literal|"hello"
argument_list|)
decl_stmt|;
name|capturingBucketIdResolver
operator|.
name|attachBucketIdToRecord
argument_list|(
name|record
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|record
operator|.
name|rowId
argument_list|,
name|is
argument_list|(
operator|new
name|RecordIdentifier
argument_list|(
operator|-
literal|1L
argument_list|,
name|BucketCodec
operator|.
name|V1
operator|.
name|encode
argument_list|(
operator|new
name|AcidOutputFormat
operator|.
name|Options
argument_list|(
literal|null
argument_list|)
operator|.
name|bucket
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
operator|-
literal|1L
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|record
operator|.
name|id
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|record
operator|.
name|msg
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"hello"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNoBucketColumns
parameter_list|()
block|{
operator|new
name|BucketIdResolverImpl
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|MutableRecord
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
argument_list|,
name|RECORD_ID_COLUMN
argument_list|,
name|TOTAL_BUCKETS
argument_list|,
operator|new
name|int
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
package|;
end_package

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
name|worker
operator|.
name|BucketIdResolver
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
name|worker
operator|.
name|BucketIdResolverImpl
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
name|worker
operator|.
name|Mutator
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
name|worker
operator|.
name|MutatorFactory
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
name|worker
operator|.
name|MutatorImpl
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
name|worker
operator|.
name|RecordInspector
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
name|worker
operator|.
name|RecordInspectorImpl
import|;
end_import

begin_class
specifier|public
class|class
name|ReflectiveMutatorFactory
implements|implements
name|MutatorFactory
block|{
specifier|private
specifier|final
name|int
name|recordIdColumn
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|objectInspector
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|configuration
decl_stmt|;
specifier|private
specifier|final
name|int
index|[]
name|bucketColumnIndexes
decl_stmt|;
specifier|public
name|ReflectiveMutatorFactory
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|recordClass
parameter_list|,
name|int
name|recordIdColumn
parameter_list|,
name|int
index|[]
name|bucketColumnIndexes
parameter_list|)
block|{
name|this
operator|.
name|configuration
operator|=
name|configuration
expr_stmt|;
name|this
operator|.
name|recordIdColumn
operator|=
name|recordIdColumn
expr_stmt|;
name|this
operator|.
name|bucketColumnIndexes
operator|=
name|bucketColumnIndexes
expr_stmt|;
name|objectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|recordClass
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Mutator
name|newMutator
parameter_list|(
name|AcidOutputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|outputFormat
parameter_list|,
name|long
name|writeId
parameter_list|,
name|Path
name|partitionPath
parameter_list|,
name|int
name|bucketId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|MutatorImpl
argument_list|(
name|configuration
argument_list|,
name|recordIdColumn
argument_list|,
name|objectInspector
argument_list|,
name|outputFormat
argument_list|,
name|writeId
argument_list|,
name|partitionPath
argument_list|,
name|bucketId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|RecordInspector
name|newRecordInspector
parameter_list|()
block|{
return|return
operator|new
name|RecordInspectorImpl
argument_list|(
name|objectInspector
argument_list|,
name|recordIdColumn
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BucketIdResolver
name|newBucketIdResolver
parameter_list|(
name|int
name|totalBuckets
parameter_list|)
block|{
return|return
operator|new
name|BucketIdResolverImpl
argument_list|(
name|objectInspector
argument_list|,
name|recordIdColumn
argument_list|,
name|totalBuckets
argument_list|,
name|bucketColumnIndexes
argument_list|)
return|;
block|}
block|}
end_class

end_unit


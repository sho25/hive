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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|tez
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|fast
operator|.
name|VectorMapJoinFastTableContainer
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
name|metadata
operator|.
name|HiveException
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
name|optimizer
operator|.
name|ConvertJoinMapJoin
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
name|plan
operator|.
name|MapJoinDesc
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
name|plan
operator|.
name|Statistics
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
name|plan
operator|.
name|VectorMapJoinDesc
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
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|HashTableImplementationType
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
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|HashTableKeyType
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
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|HashTableKind
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
name|ByteStream
operator|.
name|Output
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
name|SerDeException
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
name|binarysortable
operator|.
name|fast
operator|.
name|BinarySortableSerializeWrite
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
name|BytesWritable
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestVectorMapJoinFastHashTable
block|{
name|long
name|keyCount
init|=
literal|15_000_000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestVectorMapJoinFastHashTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|checkFast2estimations
parameter_list|()
throws|throws
name|Exception
block|{
name|runEstimationCheck
argument_list|(
name|HashTableKeyType
operator|.
name|LONG
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|checkFast3estimations
parameter_list|()
throws|throws
name|Exception
block|{
name|runEstimationCheck
argument_list|(
name|HashTableKeyType
operator|.
name|MULTI_KEY
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runEstimationCheck
parameter_list|(
name|HashTableKeyType
name|l
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|IOException
throws|,
name|HiveException
block|{
name|MapJoinDesc
name|desc
init|=
operator|new
name|MapJoinDesc
argument_list|()
decl_stmt|;
name|VectorMapJoinDesc
name|vectorDesc
init|=
operator|new
name|VectorMapJoinDesc
argument_list|()
decl_stmt|;
name|vectorDesc
operator|.
name|setHashTableKeyType
argument_list|(
name|l
argument_list|)
expr_stmt|;
name|vectorDesc
operator|.
name|setIsFastHashTableEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|vectorDesc
operator|.
name|setHashTableImplementationType
argument_list|(
name|HashTableImplementationType
operator|.
name|FAST
argument_list|)
expr_stmt|;
name|vectorDesc
operator|.
name|setHashTableKind
argument_list|(
name|HashTableKind
operator|.
name|HASH_MAP
argument_list|)
expr_stmt|;
name|desc
operator|.
name|setVectorDesc
argument_list|(
name|vectorDesc
argument_list|)
expr_stmt|;
name|Configuration
name|hconf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|VectorMapJoinFastTableContainer
name|container
init|=
operator|new
name|VectorMapJoinFastTableContainer
argument_list|(
name|desc
argument_list|,
name|hconf
argument_list|,
name|keyCount
argument_list|)
decl_stmt|;
name|container
operator|.
name|setSerde
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|long
name|dataSize
init|=
literal|0
decl_stmt|;
name|BinarySortableSerializeWrite
name|bsw
init|=
operator|new
name|BinarySortableSerializeWrite
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Output
name|outp
init|=
operator|new
name|Output
argument_list|()
decl_stmt|;
name|BytesWritable
name|key
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|BytesWritable
name|value
init|=
operator|new
name|BytesWritable
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
name|keyCount
condition|;
name|i
operator|++
control|)
block|{
name|bsw
operator|.
name|set
argument_list|(
name|outp
argument_list|)
expr_stmt|;
name|bsw
operator|.
name|writeLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|key
operator|=
operator|new
name|BytesWritable
argument_list|(
name|outp
operator|.
name|getData
argument_list|()
argument_list|,
name|outp
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|bsw
operator|.
name|set
argument_list|(
name|outp
argument_list|)
expr_stmt|;
name|bsw
operator|.
name|writeLong
argument_list|(
name|i
operator|*
literal|2
argument_list|)
expr_stmt|;
name|value
operator|=
operator|new
name|BytesWritable
argument_list|(
name|outp
operator|.
name|getData
argument_list|()
argument_list|,
name|outp
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|container
operator|.
name|putRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|dataSize
operator|+=
literal|8
expr_stmt|;
name|dataSize
operator|+=
literal|8
expr_stmt|;
block|}
name|Statistics
name|stat
init|=
operator|new
name|Statistics
argument_list|(
name|keyCount
argument_list|,
name|dataSize
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Long
name|realObjectSize
init|=
name|getObjectSize
argument_list|(
name|container
argument_list|)
decl_stmt|;
name|Long
name|executionEstimate
init|=
name|container
operator|.
name|getEstimatedMemorySize
argument_list|()
decl_stmt|;
name|Long
name|compilerEstimate
init|=
literal|null
decl_stmt|;
name|ConvertJoinMapJoin
name|cjm
init|=
operator|new
name|ConvertJoinMapJoin
argument_list|()
decl_stmt|;
name|cjm
operator|.
name|hashTableLoadFactor
operator|=
literal|.75f
expr_stmt|;
switch|switch
condition|(
name|l
condition|)
block|{
case|case
name|MULTI_KEY
case|:
name|compilerEstimate
operator|=
name|cjm
operator|.
name|computeOnlineDataSizeFastCompositeKeyed
argument_list|(
name|stat
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|compilerEstimate
operator|=
name|cjm
operator|.
name|computeOnlineDataSizeFastLongKeyed
argument_list|(
name|stat
argument_list|)
expr_stmt|;
break|break;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"stats: {}"
argument_list|,
name|stat
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"realObjectSize: {}"
argument_list|,
name|realObjectSize
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"executionEstimate : {}"
argument_list|,
name|executionEstimate
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"compilerEstimate: {}"
argument_list|,
name|compilerEstimate
argument_list|)
expr_stmt|;
name|checkRelativeError
argument_list|(
name|realObjectSize
argument_list|,
name|executionEstimate
argument_list|,
literal|.05
argument_list|)
expr_stmt|;
name|checkRelativeError
argument_list|(
name|realObjectSize
argument_list|,
name|compilerEstimate
argument_list|,
literal|.05
argument_list|)
expr_stmt|;
name|checkRelativeError
argument_list|(
name|compilerEstimate
argument_list|,
name|executionEstimate
argument_list|,
literal|.05
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkRelativeError
parameter_list|(
name|Long
name|v1
parameter_list|,
name|Long
name|v2
parameter_list|,
name|double
name|err
parameter_list|)
block|{
if|if
condition|(
name|v1
operator|==
literal|null
operator|||
name|v2
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|double
name|d
init|=
operator|(
name|double
operator|)
name|v1
operator|/
name|v2
decl_stmt|;
name|assertEquals
argument_list|(
literal|"error is outside of tolerance margin"
argument_list|,
literal|1.0
argument_list|,
name|d
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
comment|// jdk.nashorn.internal.ir.debug.ObjectSizeCalculator is only present in hotspot
specifier|private
name|Long
name|getObjectSize
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
literal|"jdk.nashorn.internal.ir.debug.ObjectSizeCalculator"
argument_list|)
decl_stmt|;
name|Method
name|method
init|=
name|clazz
operator|.
name|getMethod
argument_list|(
literal|"getObjectSize"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
name|long
name|l
init|=
operator|(
name|long
operator|)
name|method
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|o
argument_list|)
decl_stmt|;
return|return
name|l
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Nashorn estimator not found"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit


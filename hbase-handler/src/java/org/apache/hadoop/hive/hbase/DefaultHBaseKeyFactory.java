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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|LazyObjectBase
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
name|LazySerDeParameters
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
name|StructField
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
operator|.
name|ObjectInspectorOptions
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

begin_class
specifier|public
class|class
name|DefaultHBaseKeyFactory
extends|extends
name|AbstractHBaseKeyFactory
implements|implements
name|HBaseKeyFactory
block|{
specifier|protected
name|LazySerDeParameters
name|serdeParams
decl_stmt|;
specifier|protected
name|HBaseRowSerializer
name|serializer
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|HBaseSerDeParameters
name|hbaseParam
parameter_list|,
name|Properties
name|properties
parameter_list|)
throws|throws
name|SerDeException
block|{
name|super
operator|.
name|init
argument_list|(
name|hbaseParam
argument_list|,
name|properties
argument_list|)
expr_stmt|;
name|this
operator|.
name|serdeParams
operator|=
name|hbaseParam
operator|.
name|getSerdeParams
argument_list|()
expr_stmt|;
name|this
operator|.
name|serializer
operator|=
operator|new
name|HBaseRowSerializer
argument_list|(
name|hbaseParam
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|createKeyObjectInspector
parameter_list|(
name|TypeInfo
name|type
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|LazyFactory
operator|.
name|createLazyObjectInspector
argument_list|(
name|type
argument_list|,
literal|1
argument_list|,
name|serdeParams
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|LazyObjectBase
name|createKey
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
return|return
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|inspector
argument_list|,
name|keyMapping
operator|.
name|binaryStorage
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|serializeKey
parameter_list|(
name|Object
name|object
parameter_list|,
name|StructField
name|field
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|serializer
operator|.
name|serializeKeyField
argument_list|(
name|object
argument_list|,
name|field
argument_list|,
name|keyMapping
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|static
name|DefaultHBaseKeyFactory
name|forTest
parameter_list|(
name|LazySerDeParameters
name|params
parameter_list|,
name|ColumnMappings
name|mappings
parameter_list|)
block|{
name|DefaultHBaseKeyFactory
name|factory
init|=
operator|new
name|DefaultHBaseKeyFactory
argument_list|()
decl_stmt|;
name|factory
operator|.
name|serdeParams
operator|=
name|params
expr_stmt|;
name|factory
operator|.
name|keyMapping
operator|=
name|mappings
operator|.
name|getKeyMapping
argument_list|()
expr_stmt|;
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit


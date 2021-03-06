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
name|accumulo
operator|.
name|serde
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
name|Collections
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
name|accumulo
operator|.
name|columns
operator|.
name|ColumnEncoding
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
name|accumulo
operator|.
name|columns
operator|.
name|HiveAccumuloRowIdColumnMapping
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
name|JarUtils
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
name|typeinfo
operator|.
name|TypeInfo
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
name|TypeInfoFactory
import|;
end_import

begin_comment
comment|/**  * Default implementation of the AccumuloRowIdFactory which uses the normal  * {@link AccumuloRowSerializer} methods to serialize the field for storage into Accumulo.  */
end_comment

begin_class
specifier|public
class|class
name|DefaultAccumuloRowIdFactory
implements|implements
name|AccumuloRowIdFactory
block|{
specifier|protected
name|AccumuloSerDeParameters
name|accumuloSerDeParams
decl_stmt|;
specifier|protected
name|LazySerDeParameters
name|serdeParams
decl_stmt|;
specifier|protected
name|Properties
name|properties
decl_stmt|;
specifier|protected
name|HiveAccumuloRowIdColumnMapping
name|rowIdMapping
decl_stmt|;
specifier|protected
name|AccumuloRowSerializer
name|serializer
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|AccumuloSerDeParameters
name|accumuloSerDeParams
parameter_list|,
name|Properties
name|properties
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
operator|.
name|accumuloSerDeParams
operator|=
name|accumuloSerDeParams
expr_stmt|;
name|this
operator|.
name|serdeParams
operator|=
name|accumuloSerDeParams
operator|.
name|getSerDeParameters
argument_list|()
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
name|this
operator|.
name|serializer
operator|=
operator|new
name|AccumuloRowSerializer
argument_list|(
name|accumuloSerDeParams
operator|.
name|getRowIdOffset
argument_list|()
argument_list|,
name|serdeParams
argument_list|,
name|accumuloSerDeParams
operator|.
name|getColumnMappings
argument_list|()
argument_list|,
name|accumuloSerDeParams
operator|.
name|getTableVisibilityLabel
argument_list|()
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|rowIdMapping
operator|=
name|accumuloSerDeParams
operator|.
name|getRowIdColumnMapping
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addDependencyJars
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|JarUtils
operator|.
name|addDependencyJars
argument_list|(
name|conf
argument_list|,
name|Collections
operator|.
expr|<
name|Class
argument_list|<
name|?
argument_list|>
operator|>
name|singletonList
argument_list|(
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|createRowIdObjectInspector
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
name|serdeParams
operator|.
name|getSeparators
argument_list|()
argument_list|,
literal|1
argument_list|,
name|serdeParams
operator|.
name|getNullSequence
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|isEscaped
argument_list|()
argument_list|,
name|serdeParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|LazyObjectBase
name|createRowId
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// LazyObject can only be binary when it's not a string as well
comment|//    return LazyFactory.createLazyObject(inspector,
comment|//            ColumnEncoding.BINARY == rowIdMapping.getEncoding());
return|return
name|LazyFactory
operator|.
name|createLazyObject
argument_list|(
name|inspector
argument_list|,
operator|!
name|TypeInfoFactory
operator|.
name|stringTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|.
name|equals
argument_list|(
name|inspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
operator|&&
name|ColumnEncoding
operator|.
name|BINARY
operator|==
name|rowIdMapping
operator|.
name|getEncoding
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|serializeRowId
parameter_list|(
name|Object
name|object
parameter_list|,
name|StructField
name|field
parameter_list|,
name|ByteStream
operator|.
name|Output
name|output
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|serializer
operator|.
name|serializeRowId
argument_list|(
name|object
argument_list|,
name|field
argument_list|,
name|rowIdMapping
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|hbase
operator|.
name|struct
operator|.
name|HBaseValueFactory
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
name|lazy
operator|.
name|objectinspector
operator|.
name|LazyObjectInspectorFactory
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
name|Arrays
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
name|List
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

begin_comment
comment|// Does same thing with LazyFactory#createLazyObjectInspector except that this replaces
end_comment

begin_comment
comment|// original keyOI with OI which is create by HBaseKeyFactory provided by serde property for hbase
end_comment

begin_class
specifier|public
class|class
name|HBaseLazyObjectFactory
block|{
specifier|public
specifier|static
name|ObjectInspector
name|createLazyHBaseStructInspector
parameter_list|(
name|LazySerDeParameters
name|serdeParams
parameter_list|,
name|int
name|index
parameter_list|,
name|HBaseKeyFactory
name|keyFactory
parameter_list|,
name|List
argument_list|<
name|HBaseValueFactory
argument_list|>
name|valueFactories
parameter_list|)
throws|throws
name|SerDeException
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|serdeParams
operator|.
name|getColumnTypes
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|columnObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columnTypes
operator|.
name|size
argument_list|()
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
name|columnTypes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|index
condition|)
block|{
name|columnObjectInspectors
operator|.
name|add
argument_list|(
name|keyFactory
operator|.
name|createKeyObjectInspector
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|columnObjectInspectors
operator|.
name|add
argument_list|(
name|valueFactories
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|createValueObjectInspector
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazySimpleStructObjectInspector
argument_list|(
name|serdeParams
operator|.
name|getColumnNames
argument_list|()
argument_list|,
name|columnObjectInspectors
argument_list|,
literal|null
argument_list|,
name|serdeParams
operator|.
name|getSeparators
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|serdeParams
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ObjectInspector
name|createLazyHBaseStructInspector
parameter_list|(
name|HBaseSerDeParameters
name|hSerdeParams
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|hSerdeParams
operator|.
name|getColumnTypes
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|columnObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columnTypes
operator|.
name|size
argument_list|()
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
name|columnTypes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|hSerdeParams
operator|.
name|getKeyIndex
argument_list|()
condition|)
block|{
name|columnObjectInspectors
operator|.
name|add
argument_list|(
name|hSerdeParams
operator|.
name|getKeyFactory
argument_list|()
operator|.
name|createKeyObjectInspector
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|columnObjectInspectors
operator|.
name|add
argument_list|(
name|hSerdeParams
operator|.
name|getValueFactories
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|createValueObjectInspector
argument_list|(
name|columnTypes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|structFieldComments
init|=
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns.comments"
argument_list|)
argument_list|)
condition|?
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Collections
operator|.
name|nCopies
argument_list|(
name|columnTypes
operator|.
name|size
argument_list|()
argument_list|,
literal|""
argument_list|)
argument_list|)
else|:
name|Arrays
operator|.
name|asList
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns.comments"
argument_list|)
operator|.
name|split
argument_list|(
literal|"\0"
argument_list|,
name|columnTypes
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|LazyObjectInspectorFactory
operator|.
name|getLazySimpleStructObjectInspector
argument_list|(
name|hSerdeParams
operator|.
name|getColumnNames
argument_list|()
argument_list|,
name|columnObjectInspectors
argument_list|,
name|structFieldComments
argument_list|,
name|hSerdeParams
operator|.
name|getSerdeParams
argument_list|()
operator|.
name|getSeparators
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|hSerdeParams
operator|.
name|getSerdeParams
argument_list|()
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
return|;
block|}
block|}
end_class

end_unit


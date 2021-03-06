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
operator|.
name|struct
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|hbase
operator|.
name|ColumnMappings
operator|.
name|ColumnMapping
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
name|objectinspector
operator|.
name|LazySimpleStructObjectInspector
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

begin_comment
comment|/**  * Implementation of {@link HBaseValueFactory} to consume a custom struct  * */
end_comment

begin_class
specifier|public
class|class
name|StructHBaseValueFactory
parameter_list|<
name|T
extends|extends
name|HBaseStructValue
parameter_list|>
extends|extends
name|DefaultHBaseValueFactory
block|{
specifier|private
specifier|final
name|int
name|fieldID
decl_stmt|;
specifier|private
specifier|final
name|Constructor
name|constructor
decl_stmt|;
specifier|public
name|StructHBaseValueFactory
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|structValueClass
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|fieldID
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldID
operator|=
name|fieldID
expr_stmt|;
name|this
operator|.
name|constructor
operator|=
name|structValueClass
operator|.
name|getDeclaredConstructor
argument_list|(
name|LazySimpleStructObjectInspector
operator|.
name|class
argument_list|,
name|Properties
operator|.
name|class
argument_list|,
name|Configuration
operator|.
name|class
argument_list|,
name|ColumnMapping
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|LazyObjectBase
name|createValueObject
parameter_list|(
name|ObjectInspector
name|inspector
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
return|return
operator|(
name|T
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|inspector
argument_list|,
name|properties
argument_list|,
name|hbaseParams
operator|.
name|getBaseConfiguration
argument_list|()
argument_list|,
name|hbaseParams
operator|.
name|getColumnMappings
argument_list|()
operator|.
name|getColumnsMapping
argument_list|()
index|[
name|fieldID
index|]
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


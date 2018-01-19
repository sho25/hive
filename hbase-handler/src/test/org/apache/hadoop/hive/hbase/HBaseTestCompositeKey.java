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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|ByteArrayRef
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_class
specifier|public
class|class
name|HBaseTestCompositeKey
extends|extends
name|HBaseCompositeKey
block|{
name|byte
index|[]
name|bytes
decl_stmt|;
name|String
name|bytesAsString
decl_stmt|;
name|Properties
name|tbl
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|HBaseTestCompositeKey
parameter_list|(
name|LazySimpleStructObjectInspector
name|oi
parameter_list|,
name|Properties
name|tbl
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|this
operator|.
name|tbl
operator|=
name|tbl
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
operator|.
name|getData
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getField
parameter_list|(
name|int
name|fieldID
parameter_list|)
block|{
if|if
condition|(
name|bytesAsString
operator|==
literal|null
condition|)
block|{
name|bytesAsString
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|bytes
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
comment|// Randomly pick the character corresponding to the field id and convert it to byte array
name|byte
index|[]
name|fieldBytes
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|bytesAsString
operator|.
name|charAt
argument_list|(
name|fieldID
argument_list|)
block|}
decl_stmt|;
return|return
name|toLazyObject
argument_list|(
name|fieldID
argument_list|,
name|fieldBytes
argument_list|)
return|;
block|}
block|}
end_class

end_unit


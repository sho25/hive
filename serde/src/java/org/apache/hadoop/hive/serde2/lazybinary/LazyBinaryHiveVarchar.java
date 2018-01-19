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
name|serde2
operator|.
name|lazybinary
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
name|hive
operator|.
name|serde2
operator|.
name|io
operator|.
name|HiveVarcharWritable
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
name|objectinspector
operator|.
name|primitive
operator|.
name|WritableHiveVarcharObjectInspector
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
name|VarcharTypeInfo
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
name|Text
import|;
end_import

begin_class
specifier|public
class|class
name|LazyBinaryHiveVarchar
extends|extends
name|LazyBinaryPrimitive
argument_list|<
name|WritableHiveVarcharObjectInspector
argument_list|,
name|HiveVarcharWritable
argument_list|>
block|{
specifier|protected
name|int
name|maxLength
init|=
operator|-
literal|1
decl_stmt|;
name|LazyBinaryHiveVarchar
parameter_list|(
name|WritableHiveVarcharObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|maxLength
operator|=
operator|(
operator|(
name|VarcharTypeInfo
operator|)
name|oi
operator|.
name|getTypeInfo
argument_list|()
operator|)
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|data
operator|=
operator|new
name|HiveVarcharWritable
argument_list|()
expr_stmt|;
block|}
name|LazyBinaryHiveVarchar
parameter_list|(
name|LazyBinaryHiveVarchar
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|maxLength
operator|=
name|copy
operator|.
name|maxLength
expr_stmt|;
name|data
operator|=
operator|new
name|HiveVarcharWritable
argument_list|(
name|copy
operator|.
name|data
argument_list|)
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
comment|// re-use existing text member in varchar writable
name|Text
name|textValue
init|=
name|data
operator|.
name|getTextValue
argument_list|()
decl_stmt|;
name|textValue
operator|.
name|set
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|data
operator|.
name|enforceMaxLength
argument_list|(
name|maxLength
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


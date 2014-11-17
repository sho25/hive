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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|expressions
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|ColumnVector
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
name|serde2
operator|.
name|io
operator|.
name|HiveDecimalWritable
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Interface used to create Writable objects from vector expression primitives.  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|VectorExpressionWriter
block|{
name|ObjectInspector
name|getObjectInspector
parameter_list|()
function_decl|;
name|Object
name|writeValue
parameter_list|(
name|ColumnVector
name|column
parameter_list|,
name|int
name|row
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|writeValue
parameter_list|(
name|long
name|value
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|writeValue
parameter_list|(
name|double
name|value
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|writeValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|writeValue
parameter_list|(
name|HiveDecimalWritable
name|value
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|writeValue
parameter_list|(
name|HiveDecimal
name|value
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|setValue
parameter_list|(
name|Object
name|row
parameter_list|,
name|ColumnVector
name|column
parameter_list|,
name|int
name|columnRow
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Object
name|initValue
parameter_list|(
name|Object
name|ost
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
end_interface

end_unit


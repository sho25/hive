begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|objectinspector
operator|.
name|primitive
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
name|TimestampTZ
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
name|TimestampTZWritable
import|;
end_import

begin_interface
specifier|public
interface|interface
name|SettableTimestampTZObjectInspector
extends|extends
name|TimestampTZObjectInspector
block|{
name|Object
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
function_decl|;
name|Object
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|TimestampTZ
name|t
parameter_list|)
function_decl|;
name|Object
name|set
parameter_list|(
name|Object
name|o
parameter_list|,
name|TimestampTZWritable
name|t
parameter_list|)
function_decl|;
name|Object
name|create
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|)
function_decl|;
name|Object
name|create
parameter_list|(
name|TimestampTZ
name|t
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

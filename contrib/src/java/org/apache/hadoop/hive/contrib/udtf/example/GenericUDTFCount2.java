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
name|contrib
operator|.
name|udtf
operator|.
name|example
package|;
end_package

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
name|UDFArgumentException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDTF
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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
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
name|PrimitiveObjectInspectorFactory
import|;
end_import

begin_comment
comment|/**  * GenericUDTFCount2 outputs the number of rows seen, twice. It's output twice  * to test outputting of rows on close with lateral view.  *  */
end_comment

begin_class
specifier|public
class|class
name|GenericUDTFCount2
extends|extends
name|GenericUDTF
block|{
specifier|private
specifier|transient
name|Integer
name|count
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|transient
name|Object
name|forwardObj
index|[]
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveException
block|{
name|forwardObj
index|[
literal|0
index|]
operator|=
name|count
expr_stmt|;
name|forward
argument_list|(
name|forwardObj
argument_list|)
expr_stmt|;
name|forward
argument_list|(
name|forwardObj
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|StructObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|argOIs
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|fieldNames
operator|.
name|add
argument_list|(
literal|"col1"
argument_list|)
expr_stmt|;
name|fieldOIs
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
argument_list|)
expr_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldOIs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|HiveException
block|{
name|count
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|count
operator|.
name|intValue
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


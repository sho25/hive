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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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
name|plan
operator|.
name|collectDesc
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
name|InspectableObject
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
name|ObjectInspectorUtils
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
name|mapred
operator|.
name|Reporter
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

begin_comment
comment|/**  * Buffers rows emitted by other operators  **/
end_comment

begin_class
specifier|public
class|class
name|CollectOperator
extends|extends
name|Operator
argument_list|<
name|collectDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|transient
specifier|protected
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|rowList
decl_stmt|;
specifier|transient
specifier|protected
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|rowInspectorList
decl_stmt|;
specifier|transient
name|int
name|maxSize
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|rowList
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
name|rowInspectorList
operator|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
expr_stmt|;
name|maxSize
operator|=
name|conf
operator|.
name|getBufferSize
argument_list|()
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|rowList
operator|.
name|size
argument_list|()
operator|<
name|maxSize
condition|)
block|{
comment|// Create a standard copy of the object.
comment|// In the future we can optimize this by doing copy-on-write.
comment|// Here we always copy the object so that other operators can reuse the object for the next row.
name|Object
name|o
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|rowInspector
argument_list|)
decl_stmt|;
name|rowList
operator|.
name|add
argument_list|(
name|o
argument_list|)
expr_stmt|;
name|rowInspectorList
operator|.
name|add
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
name|forward
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|retrieve
parameter_list|(
name|InspectableObject
name|result
parameter_list|)
block|{
assert|assert
operator|(
name|result
operator|!=
literal|null
operator|)
assert|;
if|if
condition|(
name|rowList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|result
operator|.
name|o
operator|=
literal|null
expr_stmt|;
name|result
operator|.
name|oi
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|o
operator|=
name|rowList
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|result
operator|.
name|oi
operator|=
name|rowInspectorList
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


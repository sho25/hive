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
name|persistence
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
name|SerDe
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|MapJoinObjectSerDeContext
block|{
specifier|private
specifier|final
name|ObjectInspector
name|standardOI
decl_stmt|;
specifier|private
specifier|final
name|SerDe
name|serde
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|hasFilter
decl_stmt|;
specifier|public
name|MapJoinObjectSerDeContext
parameter_list|(
name|SerDe
name|serde
parameter_list|,
name|boolean
name|hasFilter
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
operator|.
name|serde
operator|=
name|serde
expr_stmt|;
name|this
operator|.
name|hasFilter
operator|=
name|hasFilter
expr_stmt|;
name|this
operator|.
name|standardOI
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the standardOI    */
specifier|public
name|ObjectInspector
name|getStandardOI
parameter_list|()
block|{
return|return
name|standardOI
return|;
block|}
comment|/**    * @return the serde    */
specifier|public
name|SerDe
name|getSerDe
parameter_list|()
block|{
return|return
name|serde
return|;
block|}
specifier|public
name|boolean
name|hasFilterTag
parameter_list|()
block|{
return|return
name|hasFilter
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MapJoinObjectSerDeContext [standardOI="
operator|+
name|standardOI
operator|+
literal|", serde="
operator|+
name|serde
operator|+
literal|", hasFilter="
operator|+
name|hasFilter
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit


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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|TimestampWritable
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
name|LazyTimestamp
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
name|TimestampObjectInspector
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|TimestampParser
import|;
end_import

begin_class
specifier|public
class|class
name|LazyTimestampObjectInspector
extends|extends
name|AbstractPrimitiveLazyObjectInspector
argument_list|<
name|TimestampWritable
argument_list|>
implements|implements
name|TimestampObjectInspector
block|{
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|timestampFormats
init|=
literal|null
decl_stmt|;
specifier|protected
name|TimestampParser
name|timestampParser
init|=
literal|null
decl_stmt|;
name|LazyTimestampObjectInspector
parameter_list|()
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
expr_stmt|;
name|timestampParser
operator|=
operator|new
name|TimestampParser
argument_list|()
expr_stmt|;
block|}
name|LazyTimestampObjectInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|tsFormats
parameter_list|)
block|{
name|super
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestampFormats
operator|=
name|tsFormats
expr_stmt|;
name|timestampParser
operator|=
operator|new
name|TimestampParser
argument_list|(
name|tsFormats
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Object
name|copyObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|LazyTimestamp
argument_list|(
operator|(
name|LazyTimestamp
operator|)
name|o
argument_list|)
return|;
block|}
specifier|public
name|Timestamp
name|getPrimitiveJavaObject
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
operator|(
name|LazyTimestamp
operator|)
name|o
operator|)
operator|.
name|getWritableObject
argument_list|()
operator|.
name|getTimestamp
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTimestampFormats
parameter_list|()
block|{
return|return
name|timestampFormats
return|;
block|}
specifier|public
name|TimestampParser
name|getTimestampParser
parameter_list|()
block|{
return|return
name|timestampParser
return|;
block|}
block|}
end_class

end_unit


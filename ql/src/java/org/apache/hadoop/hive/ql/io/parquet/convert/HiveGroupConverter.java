begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
operator|.
name|convert
package|;
end_package

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
name|typeinfo
operator|.
name|TypeInfo
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

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Converter
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|GroupConverter
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|Type
operator|.
name|Repetition
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HiveGroupConverter
extends|extends
name|GroupConverter
block|{
specifier|protected
specifier|static
name|Converter
name|getConverterFromDescription
parameter_list|(
specifier|final
name|Type
name|type
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|HiveGroupConverter
name|parent
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|hiveSchemaTypeInfos
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|type
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
return|return
name|ETypeConverter
operator|.
name|getNewConverter
argument_list|(
name|type
operator|.
name|asPrimitiveType
argument_list|()
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveSchemaTypeInfos
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
name|type
operator|.
name|asGroupType
argument_list|()
operator|.
name|getRepetition
argument_list|()
operator|==
name|Repetition
operator|.
name|REPEATED
condition|)
block|{
return|return
operator|new
name|ArrayWritableGroupConverter
argument_list|(
name|type
operator|.
name|asGroupType
argument_list|()
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|hiveSchemaTypeInfos
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|DataWritableGroupConverter
argument_list|(
name|type
operator|.
name|asGroupType
argument_list|()
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|hiveSchemaTypeInfos
argument_list|)
return|;
block|}
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|Writable
name|value
parameter_list|)
function_decl|;
specifier|protected
specifier|abstract
name|void
name|add
parameter_list|(
name|int
name|index
parameter_list|,
name|Writable
name|value
parameter_list|)
function_decl|;
block|}
end_class

end_unit


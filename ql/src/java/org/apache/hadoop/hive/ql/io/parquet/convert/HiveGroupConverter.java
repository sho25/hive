begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
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
name|org
operator|.
name|apache
operator|.
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
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|PrimitiveConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|GroupType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|OriginalType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HiveGroupConverter
extends|extends
name|GroupConverter
implements|implements
name|ConverterParent
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metadata
decl_stmt|;
specifier|public
name|void
name|setMetadata
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metadata
parameter_list|)
block|{
name|this
operator|.
name|metadata
operator|=
name|metadata
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getMetadata
parameter_list|()
block|{
return|return
name|metadata
return|;
block|}
specifier|protected
specifier|static
name|PrimitiveConverter
name|getConverterFromDescription
parameter_list|(
name|PrimitiveType
name|type
parameter_list|,
name|int
name|index
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
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
return|return
name|ETypeConverter
operator|.
name|getNewConverter
argument_list|(
name|type
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|HiveGroupConverter
name|getConverterFromDescription
parameter_list|(
name|GroupType
name|type
parameter_list|,
name|int
name|index
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
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
name|OriginalType
name|annotation
init|=
name|type
operator|.
name|getOriginalType
argument_list|()
decl_stmt|;
if|if
condition|(
name|annotation
operator|==
name|OriginalType
operator|.
name|LIST
condition|)
block|{
return|return
name|HiveCollectionConverter
operator|.
name|forList
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|annotation
operator|==
name|OriginalType
operator|.
name|MAP
operator|||
name|annotation
operator|==
name|OriginalType
operator|.
name|MAP_KEY_VALUE
condition|)
block|{
return|return
name|HiveCollectionConverter
operator|.
name|forMap
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
return|return
operator|new
name|HiveStructConverter
argument_list|(
name|type
argument_list|,
name|parent
argument_list|,
name|index
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
specifier|protected
specifier|static
name|Converter
name|getConverterFromDescription
parameter_list|(
name|Type
name|type
parameter_list|,
name|int
name|index
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|TypeInfo
name|hiveTypeInfo
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
name|getConverterFromDescription
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
name|hiveTypeInfo
argument_list|)
return|;
block|}
return|return
name|getConverterFromDescription
argument_list|(
name|type
operator|.
name|asGroupType
argument_list|()
argument_list|,
name|index
argument_list|,
name|parent
argument_list|,
name|hiveTypeInfo
argument_list|)
return|;
block|}
specifier|public
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
block|}
end_class

end_unit


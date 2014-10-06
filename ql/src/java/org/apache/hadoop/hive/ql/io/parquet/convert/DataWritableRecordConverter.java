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
name|ArrayWritable
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
name|io
operator|.
name|api
operator|.
name|RecordMaterializer
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|GroupType
import|;
end_import

begin_comment
comment|/**  *  * A MapWritableReadSupport, encapsulates the tuples  *  */
end_comment

begin_class
specifier|public
class|class
name|DataWritableRecordConverter
extends|extends
name|RecordMaterializer
argument_list|<
name|ArrayWritable
argument_list|>
block|{
specifier|private
specifier|final
name|DataWritableGroupConverter
name|root
decl_stmt|;
specifier|public
name|DataWritableRecordConverter
parameter_list|(
specifier|final
name|GroupType
name|requestedSchema
parameter_list|,
specifier|final
name|GroupType
name|tableSchema
parameter_list|,
specifier|final
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|hiveColumnTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|root
operator|=
operator|new
name|DataWritableGroupConverter
argument_list|(
name|requestedSchema
argument_list|,
name|tableSchema
argument_list|,
name|hiveColumnTypeInfos
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ArrayWritable
name|getCurrentRecord
parameter_list|()
block|{
return|return
name|root
operator|.
name|getCurrentArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|GroupConverter
name|getRootConverter
parameter_list|()
block|{
return|return
name|root
return|;
block|}
block|}
end_class

end_unit


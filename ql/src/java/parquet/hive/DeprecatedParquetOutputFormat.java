begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|parquet
operator|.
name|hive
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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|MapredParquetOutputFormat
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|OutputFormat
import|;
end_import

begin_comment
comment|/**  * Deprecated name of the parquet-hive output format. This class exists  * simply to provide backwards compatibility with users who specified  * this name in the Hive metastore. All users should now use  * STORED AS PARQUET  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|DeprecatedParquetOutputFormat
extends|extends
name|MapredParquetOutputFormat
block|{
specifier|public
name|DeprecatedParquetOutputFormat
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|DeprecatedParquetOutputFormat
parameter_list|(
specifier|final
name|OutputFormat
argument_list|<
name|Void
argument_list|,
name|ArrayWritable
argument_list|>
name|mapreduceOutputFormat
parameter_list|)
block|{
name|super
argument_list|(
name|mapreduceOutputFormat
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


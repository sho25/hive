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
name|serde
package|;
end_package

begin_class
specifier|public
class|class
name|ParquetTableUtils
block|{
comment|// Parquet table properties
specifier|public
specifier|static
specifier|final
name|String
name|PARQUET_INT96_WRITE_ZONE_PROPERTY
init|=
literal|"parquet.mr.int96.write.zone"
decl_stmt|;
comment|// This is not a TimeZone we convert into and print out, rather a delta, an adjustment we use.
comment|// More precisely the lack of an adjustment in case of UTC
specifier|public
specifier|static
specifier|final
name|String
name|PARQUET_INT96_NO_ADJUSTMENT_ZONE
init|=
literal|"UTC"
decl_stmt|;
block|}
end_class

end_unit


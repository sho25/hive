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
name|serde2
package|;
end_package

begin_class
specifier|public
class|class
name|SerDeStats
block|{
comment|/**    * Class used to pass statistics information from serializer/deserializer to the tasks.    * A SerDeStats object is returned by calling SerDe.getStats().    */
comment|// currently we support only raw data size stat
specifier|private
name|long
name|rawDataSize
decl_stmt|;
specifier|private
name|long
name|rowCount
decl_stmt|;
specifier|public
name|SerDeStats
parameter_list|()
block|{
name|rawDataSize
operator|=
literal|0
expr_stmt|;
name|rowCount
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * Return the raw data size    * @return raw data size    */
specifier|public
name|long
name|getRawDataSize
parameter_list|()
block|{
return|return
name|rawDataSize
return|;
block|}
comment|/**    * Set the raw data size    * @param uSize - size to be set    */
specifier|public
name|void
name|setRawDataSize
parameter_list|(
name|long
name|uSize
parameter_list|)
block|{
name|rawDataSize
operator|=
name|uSize
expr_stmt|;
block|}
comment|/**    * Return the row count    * @return row count    */
specifier|public
name|long
name|getRowCount
parameter_list|()
block|{
return|return
name|rowCount
return|;
block|}
comment|/**    * Set the row count    * @param rowCount - count of rows    */
specifier|public
name|void
name|setRowCount
parameter_list|(
name|long
name|rowCount
parameter_list|)
block|{
name|this
operator|.
name|rowCount
operator|=
name|rowCount
expr_stmt|;
block|}
block|}
end_class

end_unit


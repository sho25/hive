begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|InputSplit
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|impl
operator|.
name|HCatInputFormatReader
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|impl
operator|.
name|HCatOutputFormatWriter
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|state
operator|.
name|DefaultStateProvider
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
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|state
operator|.
name|StateProvider
import|;
end_import

begin_comment
comment|/**  * Use this factory to get instances of {@link HCatReader} or {@link HCatWriter}  * at master and slave nodes.  */
end_comment

begin_class
specifier|public
class|class
name|DataTransferFactory
block|{
comment|/**    * This should be called once from master node to obtain an instance of    * {@link HCatReader}.    *    * @param re    *          ReadEntity built using {@link ReadEntity.Builder}    * @param config    *          any configuration which master node wants to pass to HCatalog    * @return {@link HCatReader}    */
specifier|public
specifier|static
name|HCatReader
name|getHCatReader
parameter_list|(
specifier|final
name|ReadEntity
name|re
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|)
block|{
comment|// In future, this may examine ReadEntity and/or config to return
comment|// appropriate HCatReader
return|return
operator|new
name|HCatInputFormatReader
argument_list|(
name|re
argument_list|,
name|config
argument_list|)
return|;
block|}
comment|/**    * This should only be called once from every slave node to obtain an instance    * of {@link HCatReader}.    *    * @param split    *          input split obtained at master node    * @param config    *          configuration obtained at master node    * @return {@link HCatReader}    */
specifier|public
specifier|static
name|HCatReader
name|getHCatReader
parameter_list|(
specifier|final
name|InputSplit
name|split
parameter_list|,
specifier|final
name|Configuration
name|config
parameter_list|)
block|{
comment|// In future, this may examine config to return appropriate HCatReader
return|return
name|getHCatReader
argument_list|(
name|split
argument_list|,
name|config
argument_list|,
name|DefaultStateProvider
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * This should only be called once from every slave node to obtain an instance    * of {@link HCatReader}. This should be called if an external system has some    * state to provide to HCatalog.    *    * @param split    *          input split obtained at master node    * @param config    *          configuration obtained at master node    * @param sp    *          {@link StateProvider}    * @return {@link HCatReader}    */
specifier|public
specifier|static
name|HCatReader
name|getHCatReader
parameter_list|(
specifier|final
name|InputSplit
name|split
parameter_list|,
specifier|final
name|Configuration
name|config
parameter_list|,
name|StateProvider
name|sp
parameter_list|)
block|{
comment|// In future, this may examine config to return appropriate HCatReader
return|return
operator|new
name|HCatInputFormatReader
argument_list|(
name|split
argument_list|,
name|config
argument_list|,
name|sp
argument_list|)
return|;
block|}
comment|/**    * This should be called at master node to obtain an instance of    * {@link HCatWriter}.    *    * @param we    *          WriteEntity built using {@link WriteEntity.Builder}    * @param config    *          any configuration which master wants to pass to HCatalog    * @return {@link HCatWriter}    */
specifier|public
specifier|static
name|HCatWriter
name|getHCatWriter
parameter_list|(
specifier|final
name|WriteEntity
name|we
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|)
block|{
comment|// In future, this may examine WriteEntity and/or config to return
comment|// appropriate HCatWriter
return|return
operator|new
name|HCatOutputFormatWriter
argument_list|(
name|we
argument_list|,
name|config
argument_list|)
return|;
block|}
comment|/**    * This should be called at slave nodes to obtain an instance of    * {@link HCatWriter}.    *    * @param cntxt    *          {@link WriterContext} obtained at master node    * @return {@link HCatWriter}    */
specifier|public
specifier|static
name|HCatWriter
name|getHCatWriter
parameter_list|(
specifier|final
name|WriterContext
name|cntxt
parameter_list|)
block|{
comment|// In future, this may examine context to return appropriate HCatWriter
return|return
name|getHCatWriter
argument_list|(
name|cntxt
argument_list|,
name|DefaultStateProvider
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * This should be called at slave nodes to obtain an instance of    * {@link HCatWriter}. If an external system has some mechanism for providing    * state to HCatalog, this constructor can be used.    *    * @param cntxt    *          {@link WriterContext} obtained at master node    * @param sp    *          {@link StateProvider}    * @return {@link HCatWriter}    */
specifier|public
specifier|static
name|HCatWriter
name|getHCatWriter
parameter_list|(
specifier|final
name|WriterContext
name|cntxt
parameter_list|,
specifier|final
name|StateProvider
name|sp
parameter_list|)
block|{
comment|// In future, this may examine context to return appropriate HCatWriter
return|return
operator|new
name|HCatOutputFormatWriter
argument_list|(
name|cntxt
operator|.
name|getConf
argument_list|()
argument_list|,
name|sp
argument_list|)
return|;
block|}
block|}
end_class

end_unit


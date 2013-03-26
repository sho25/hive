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
name|Iterator
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
comment|/** This abstraction is internal to HCatalog. This is to facilitate writing to HCatalog from external  * systems. Don't try to instantiate this directly. Instead, use {@link DataTransferFactory}  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatWriter
block|{
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|WriteEntity
name|we
decl_stmt|;
comment|// This will be null at slave nodes.
specifier|protected
name|WriterContext
name|info
decl_stmt|;
specifier|protected
name|StateProvider
name|sp
decl_stmt|;
comment|/** External system should invoke this method exactly once from a master node. 	 * @return {@link WriterContext} This should be serialized and sent to slave nodes to  	 * construct HCatWriter there. 	 * @throws HCatException 	 */
specifier|public
specifier|abstract
name|WriterContext
name|prepareWrite
parameter_list|()
throws|throws
name|HCatException
function_decl|;
comment|/** This method should be used at slave needs to perform writes.  	 * @param {@link Iterator} records to be written into HCatalog. 	 * @throws {@link HCatException} 	 */
specifier|public
specifier|abstract
name|void
name|write
parameter_list|(
specifier|final
name|Iterator
argument_list|<
name|HCatRecord
argument_list|>
name|recordItr
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/** This method should be called at master node. Primary purpose of this is to do metadata commit. 	 * @throws {@link HCatException} 	 */
specifier|public
specifier|abstract
name|void
name|commit
parameter_list|(
specifier|final
name|WriterContext
name|context
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/** This method should be called at master node. Primary purpose of this is to do cleanups in case  	 * of failures. 	 * @throws {@link HCatException}	 *  	 */
specifier|public
specifier|abstract
name|void
name|abort
parameter_list|(
specifier|final
name|WriterContext
name|context
parameter_list|)
throws|throws
name|HCatException
function_decl|;
comment|/** 	 * This constructor will be used at master node 	 * @param we WriteEntity defines where in storage records should be written to. 	 * @param config Any configuration which external system wants to communicate to HCatalog  	 * for performing writes. 	 */
specifier|protected
name|HCatWriter
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
name|this
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|this
operator|.
name|we
operator|=
name|we
expr_stmt|;
block|}
comment|/** This constructor will be used at slave nodes. 	 * @param config 	 */
specifier|protected
name|HCatWriter
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|,
specifier|final
name|StateProvider
name|sp
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|config
expr_stmt|;
name|this
operator|.
name|sp
operator|=
name|sp
expr_stmt|;
block|}
specifier|private
name|HCatWriter
parameter_list|(
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
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
if|if
condition|(
name|config
operator|!=
literal|null
condition|)
block|{
comment|// user is providing config, so it could be null.
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kv
range|:
name|config
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|kv
operator|.
name|getKey
argument_list|()
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
block|}
end_class

end_unit


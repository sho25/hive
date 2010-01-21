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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|ql
operator|.
name|hooks
operator|.
name|ReadEntity
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
name|ql
operator|.
name|hooks
operator|.
name|WriteEntity
import|;
end_import

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Move Operator"
argument_list|)
specifier|public
class|class
name|moveWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|loadTableDesc
name|loadTableWork
decl_stmt|;
specifier|private
name|loadFileDesc
name|loadFileWork
decl_stmt|;
specifier|private
name|boolean
name|checkFileFormat
decl_stmt|;
comment|/**    * ReadEntitites that are passed to the hooks.    */
specifier|protected
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
comment|/**    * List of WriteEntities that are passed to the hooks.    */
specifier|protected
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
specifier|public
name|moveWork
parameter_list|()
block|{   }
specifier|public
name|moveWork
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
block|}
specifier|public
name|moveWork
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
specifier|final
name|loadTableDesc
name|loadTableWork
parameter_list|,
specifier|final
name|loadFileDesc
name|loadFileWork
parameter_list|,
name|boolean
name|checkFileFormat
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|this
operator|.
name|loadTableWork
operator|=
name|loadTableWork
expr_stmt|;
name|this
operator|.
name|loadFileWork
operator|=
name|loadFileWork
expr_stmt|;
name|this
operator|.
name|checkFileFormat
operator|=
name|checkFileFormat
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"tables"
argument_list|)
specifier|public
name|loadTableDesc
name|getLoadTableWork
parameter_list|()
block|{
return|return
name|loadTableWork
return|;
block|}
specifier|public
name|void
name|setLoadTableWork
parameter_list|(
specifier|final
name|loadTableDesc
name|loadTableWork
parameter_list|)
block|{
name|this
operator|.
name|loadTableWork
operator|=
name|loadTableWork
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"files"
argument_list|)
specifier|public
name|loadFileDesc
name|getLoadFileWork
parameter_list|()
block|{
return|return
name|loadFileWork
return|;
block|}
specifier|public
name|void
name|setLoadFileWork
parameter_list|(
specifier|final
name|loadFileDesc
name|loadFileWork
parameter_list|)
block|{
name|this
operator|.
name|loadFileWork
operator|=
name|loadFileWork
expr_stmt|;
block|}
specifier|public
name|boolean
name|getCheckFileFormat
parameter_list|()
block|{
return|return
name|checkFileFormat
return|;
block|}
specifier|public
name|void
name|setCheckFileFormat
parameter_list|(
name|boolean
name|checkFileFormat
parameter_list|)
block|{
name|this
operator|.
name|checkFileFormat
operator|=
name|checkFileFormat
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|inputs
return|;
block|}
specifier|public
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|getOutputs
parameter_list|()
block|{
return|return
name|outputs
return|;
block|}
specifier|public
name|void
name|setInputs
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|)
block|{
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
block|}
specifier|public
name|void
name|setOutputs
parameter_list|(
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
block|}
block|}
end_class

end_unit


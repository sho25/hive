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
name|HashSet
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
name|metastore
operator|.
name|api
operator|.
name|Partition
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
name|exec
operator|.
name|Utilities
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * MoveWork.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Move Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|MoveWork
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
name|LoadTableDesc
name|loadTableWork
decl_stmt|;
specifier|private
name|LoadFileDesc
name|loadFileWork
decl_stmt|;
specifier|private
name|LoadMultiFilesDesc
name|loadMultiFilesWork
decl_stmt|;
specifier|private
name|boolean
name|checkFileFormat
decl_stmt|;
specifier|private
name|boolean
name|srcLocal
decl_stmt|;
comment|/**    * ReadEntitites that are passed to the hooks.    */
specifier|protected
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
comment|/**    * List of WriteEntities that are passed to the hooks.    */
specifier|protected
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
comment|/**    * List of inserted partitions    */
specifier|protected
name|List
argument_list|<
name|Partition
argument_list|>
name|movedParts
decl_stmt|;
specifier|private
name|boolean
name|isNoop
decl_stmt|;
specifier|public
name|MoveWork
parameter_list|()
block|{   }
specifier|private
name|MoveWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
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
name|MoveWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
specifier|final
name|LoadTableDesc
name|loadTableWork
parameter_list|,
specifier|final
name|LoadFileDesc
name|loadFileWork
parameter_list|,
name|boolean
name|checkFileFormat
parameter_list|,
name|boolean
name|srcLocal
parameter_list|)
block|{
name|this
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|LOG14535
operator|.
name|info
argument_list|(
literal|"Creating MoveWork "
operator|+
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
operator|+
literal|" with "
operator|+
name|loadTableWork
operator|+
literal|"; "
operator|+
name|loadFileWork
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
name|this
operator|.
name|srcLocal
operator|=
name|srcLocal
expr_stmt|;
block|}
specifier|public
name|MoveWork
parameter_list|(
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
specifier|final
name|LoadTableDesc
name|loadTableWork
parameter_list|,
specifier|final
name|LoadFileDesc
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
argument_list|,
name|loadTableWork
argument_list|,
name|loadFileWork
argument_list|,
name|checkFileFormat
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"tables"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|LoadTableDesc
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
name|LoadTableDesc
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
name|Explain
argument_list|(
name|displayName
operator|=
literal|"files"
argument_list|)
specifier|public
name|LoadFileDesc
name|getLoadFileWork
parameter_list|()
block|{
return|return
name|loadFileWork
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"files"
argument_list|)
specifier|public
name|LoadMultiFilesDesc
name|getLoadMultiFilesWork
parameter_list|()
block|{
return|return
name|loadMultiFilesWork
return|;
block|}
specifier|public
name|void
name|setMultiFilesDesc
parameter_list|(
name|LoadMultiFilesDesc
name|lmfd
parameter_list|)
block|{
name|this
operator|.
name|loadMultiFilesWork
operator|=
name|lmfd
expr_stmt|;
block|}
specifier|public
name|void
name|setLoadFileWork
parameter_list|(
specifier|final
name|LoadFileDesc
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
name|HashSet
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
name|HashSet
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
name|HashSet
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
name|HashSet
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
specifier|public
name|boolean
name|isSrcLocal
parameter_list|()
block|{
return|return
name|srcLocal
return|;
block|}
specifier|public
name|void
name|setSrcLocal
parameter_list|(
name|boolean
name|srcLocal
parameter_list|)
block|{
name|this
operator|.
name|srcLocal
operator|=
name|srcLocal
expr_stmt|;
block|}
comment|// TODO# temporary test flag
specifier|public
name|void
name|setNoop
parameter_list|(
name|boolean
name|b
parameter_list|)
block|{
name|this
operator|.
name|isNoop
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNoop
parameter_list|()
block|{
return|return
name|this
operator|.
name|isNoop
return|;
block|}
block|}
end_class

end_unit


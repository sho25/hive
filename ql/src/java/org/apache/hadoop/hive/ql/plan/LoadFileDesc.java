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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|PTFUtils
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

begin_comment
comment|/**  * LoadFileDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|LoadFileDesc
extends|extends
name|LoadDesc
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
specifier|transient
name|Path
name|targetDir
decl_stmt|;
specifier|private
name|boolean
name|isDfsDir
decl_stmt|;
comment|// list of columns, comma separated
specifier|private
name|String
name|columns
decl_stmt|;
specifier|private
name|String
name|columnTypes
decl_stmt|;
specifier|private
name|String
name|destinationCreateTable
decl_stmt|;
specifier|public
name|LoadFileDesc
parameter_list|()
block|{   }
specifier|public
name|LoadFileDesc
parameter_list|(
specifier|final
name|CreateTableDesc
name|createTableDesc
parameter_list|,
specifier|final
name|CreateViewDesc
name|createViewDesc
parameter_list|,
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
name|Path
name|targetDir
parameter_list|,
specifier|final
name|boolean
name|isDfsDir
parameter_list|,
specifier|final
name|String
name|columns
parameter_list|,
specifier|final
name|String
name|columnTypes
parameter_list|)
block|{
name|this
argument_list|(
name|sourcePath
argument_list|,
name|targetDir
argument_list|,
name|isDfsDir
argument_list|,
name|columns
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
if|if
condition|(
name|createTableDesc
operator|!=
literal|null
operator|&&
name|createTableDesc
operator|.
name|getDatabaseName
argument_list|()
operator|!=
literal|null
operator|&&
name|createTableDesc
operator|.
name|getTableName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|destinationCreateTable
operator|=
operator|(
name|createTableDesc
operator|.
name|getTableName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"."
argument_list|)
condition|?
literal|""
else|:
name|createTableDesc
operator|.
name|getDatabaseName
argument_list|()
operator|+
literal|"."
operator|)
operator|+
name|createTableDesc
operator|.
name|getTableName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|createViewDesc
operator|!=
literal|null
condition|)
block|{
comment|// The work is already done in analyzeCreateView to assure that the view name is fully
comment|// qualified.
name|destinationCreateTable
operator|=
name|createViewDesc
operator|.
name|getViewName
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|LoadFileDesc
parameter_list|(
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
name|Path
name|targetDir
parameter_list|,
specifier|final
name|boolean
name|isDfsDir
parameter_list|,
specifier|final
name|String
name|columns
parameter_list|,
specifier|final
name|String
name|columnTypes
parameter_list|)
block|{
name|super
argument_list|(
name|sourcePath
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|LOG14535
operator|.
name|info
argument_list|(
literal|"creating LFD from "
operator|+
name|sourcePath
operator|+
literal|" to "
operator|+
name|targetDir
argument_list|,
operator|new
name|Exception
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|targetDir
operator|=
name|targetDir
expr_stmt|;
name|this
operator|.
name|isDfsDir
operator|=
name|isDfsDir
expr_stmt|;
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
name|this
operator|.
name|columnTypes
operator|=
name|columnTypes
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"destination"
argument_list|)
specifier|public
name|Path
name|getTargetDir
parameter_list|()
block|{
return|return
name|targetDir
return|;
block|}
specifier|public
name|void
name|setTargetDir
parameter_list|(
specifier|final
name|Path
name|targetDir
parameter_list|)
block|{
name|this
operator|.
name|targetDir
operator|=
name|targetDir
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"hdfs directory"
argument_list|)
specifier|public
name|boolean
name|getIsDfsDir
parameter_list|()
block|{
return|return
name|isDfsDir
return|;
block|}
specifier|public
name|void
name|setIsDfsDir
parameter_list|(
specifier|final
name|boolean
name|isDfsDir
parameter_list|)
block|{
name|this
operator|.
name|isDfsDir
operator|=
name|isDfsDir
expr_stmt|;
block|}
comment|/**    * @return the columns    */
specifier|public
name|String
name|getColumns
parameter_list|()
block|{
return|return
name|columns
return|;
block|}
comment|/**    * @param columns    *          the columns to set    */
specifier|public
name|void
name|setColumns
parameter_list|(
name|String
name|columns
parameter_list|)
block|{
name|this
operator|.
name|columns
operator|=
name|columns
expr_stmt|;
block|}
comment|/**    * @return the columnTypes    */
specifier|public
name|String
name|getColumnTypes
parameter_list|()
block|{
return|return
name|columnTypes
return|;
block|}
comment|/**    * @param columnTypes    *          the columnTypes to set    */
specifier|public
name|void
name|setColumnTypes
parameter_list|(
name|String
name|columnTypes
parameter_list|)
block|{
name|this
operator|.
name|columnTypes
operator|=
name|columnTypes
expr_stmt|;
block|}
comment|/**    * @return the destinationCreateTable    */
specifier|public
name|String
name|getDestinationCreateTable
parameter_list|()
block|{
return|return
name|destinationCreateTable
return|;
block|}
block|}
end_class

end_unit


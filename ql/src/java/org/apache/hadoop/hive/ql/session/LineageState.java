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
name|session
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|ColumnInfo
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
name|Operator
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
name|LineageInfo
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
name|LineageInfo
operator|.
name|DataContainer
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
name|optimizer
operator|.
name|lineage
operator|.
name|LineageCtx
operator|.
name|Index
import|;
end_import

begin_comment
comment|/**  * LineageState. Contains all the information used to generate the  * lineage information for the post execution hooks.  *  */
end_comment

begin_class
specifier|public
class|class
name|LineageState
implements|implements
name|Serializable
block|{
comment|/**    * Mapping from the directory name to FileSinkOperator (may not be FileSinkOperator for views). This    * mapping is generated at the filesink operator creation    * time and is then later used to created the mapping from    * movetask to the set of filesink operators.    */
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|>
name|dirToFop
decl_stmt|;
comment|/**    * The lineage context index for this query.    */
specifier|private
name|Index
name|index
decl_stmt|;
comment|/**    * The lineage info structure that is used to pass the lineage    * information to the hooks.    */
specifier|private
specifier|final
name|LineageInfo
name|linfo
decl_stmt|;
comment|/**    * Constructor.    */
name|LineageState
parameter_list|()
block|{
name|dirToFop
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|linfo
operator|=
operator|new
name|LineageInfo
argument_list|()
expr_stmt|;
name|index
operator|=
operator|new
name|Index
argument_list|()
expr_stmt|;
block|}
comment|/**    * Adds a mapping from the load work to the file sink operator.    *    * @param dir The directory name.    * @param fop The sink operator.    */
specifier|public
specifier|synchronized
name|void
name|mapDirToOp
parameter_list|(
name|Path
name|dir
parameter_list|,
name|Operator
name|fop
parameter_list|)
block|{
name|dirToFop
operator|.
name|put
argument_list|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|fop
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the path of the captured lineage information in case the    * conditional input path and the linked MoveWork were merged into one MoveWork.    * This should only happen for Blobstore systems with optimization turned on.    * @param newPath conditional input path    * @param oldPath path of the old linked MoveWork    */
specifier|public
specifier|synchronized
name|void
name|updateDirToOpMap
parameter_list|(
name|Path
name|newPath
parameter_list|,
name|Path
name|oldPath
parameter_list|)
block|{
name|Operator
name|op
init|=
name|dirToFop
operator|.
name|get
argument_list|(
name|oldPath
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|!=
literal|null
condition|)
block|{
name|dirToFop
operator|.
name|put
argument_list|(
name|newPath
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Set the lineage information for the associated directory.    *    * @param dir The directory containing the query results.    * @param dc The associated data container.    * @param cols The list of columns.    */
specifier|public
specifier|synchronized
name|void
name|setLineage
parameter_list|(
name|Path
name|dir
parameter_list|,
name|DataContainer
name|dc
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
parameter_list|)
block|{
comment|// First lookup the file sink operator from the load work.
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|dirToFop
operator|.
name|get
argument_list|(
name|dir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// Go over the associated fields and look up the dependencies
comment|// by position in the row schema of the filesink operator.
if|if
condition|(
name|op
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
init|=
name|op
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fs
range|:
name|cols
control|)
block|{
name|linfo
operator|.
name|putDependency
argument_list|(
name|dc
argument_list|,
name|fs
argument_list|,
name|index
operator|.
name|getDependency
argument_list|(
name|op
argument_list|,
name|signature
operator|.
name|get
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Gets the lineage information.    *    * @return LineageInfo.    */
specifier|public
name|LineageInfo
name|getLineageInfo
parameter_list|()
block|{
return|return
name|linfo
return|;
block|}
comment|/**    * Gets the index for the lineage state.    *    * @return Index.    */
specifier|public
name|Index
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
comment|/**    * Clear all lineage states    */
specifier|public
specifier|synchronized
name|void
name|clear
parameter_list|()
block|{
name|dirToFop
operator|.
name|clear
argument_list|()
expr_stmt|;
name|linfo
operator|.
name|clear
argument_list|()
expr_stmt|;
name|index
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


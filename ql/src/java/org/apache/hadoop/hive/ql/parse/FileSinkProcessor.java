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
name|parse
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|HiveConf
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
name|FileSinkOperator
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|GenMapRedUtils
import|;
end_import

begin_comment
comment|/**  * FileSinkProcessor handles addition of merge, move and stats tasks for filesinks  *  */
end_comment

begin_class
specifier|public
class|class
name|FileSinkProcessor
implements|implements
name|NodeProcessor
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FileSinkProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
comment|/*    * (non-Javadoc)    * we should ideally not modify the tree we traverse.    * However, since we need to walk the tree at any time when we modify the    * operator, we might as well do it here.    */
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenTezProcContext
name|context
init|=
operator|(
name|GenTezProcContext
operator|)
name|procCtx
decl_stmt|;
name|FileSinkOperator
name|fileSink
init|=
operator|(
name|FileSinkOperator
operator|)
name|nd
decl_stmt|;
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|parseContext
decl_stmt|;
name|boolean
name|isInsertTable
init|=
comment|// is INSERT OVERWRITE TABLE
name|GenMapRedUtils
operator|.
name|isInsertInto
argument_list|(
name|parseContext
argument_list|,
name|fileSink
argument_list|)
decl_stmt|;
name|HiveConf
name|hconf
init|=
name|parseContext
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|boolean
name|chDir
init|=
name|GenMapRedUtils
operator|.
name|isMergeRequired
argument_list|(
name|context
operator|.
name|moveTask
argument_list|,
name|hconf
argument_list|,
name|fileSink
argument_list|,
name|context
operator|.
name|currentTask
argument_list|,
name|isInsertTable
argument_list|)
decl_stmt|;
name|String
name|finalName
init|=
name|GenMapRedUtils
operator|.
name|createMoveTask
argument_list|(
name|context
operator|.
name|currentTask
argument_list|,
name|chDir
argument_list|,
name|fileSink
argument_list|,
name|parseContext
argument_list|,
name|context
operator|.
name|moveTask
argument_list|,
name|hconf
argument_list|,
name|context
operator|.
name|dependencyTask
argument_list|)
decl_stmt|;
if|if
condition|(
name|chDir
condition|)
block|{
comment|// Merge the files in the destination table/partitions by creating Map-only merge job
comment|// If underlying data is RCFile a RCFileBlockMerge task would be created.
name|LOG
operator|.
name|info
argument_list|(
literal|"using CombineHiveInputformat for the merge job"
argument_list|)
expr_stmt|;
name|GenMapRedUtils
operator|.
name|createMRWorkForMergingFiles
argument_list|(
name|fileSink
argument_list|,
name|finalName
argument_list|,
name|context
operator|.
name|dependencyTask
argument_list|,
name|context
operator|.
name|moveTask
argument_list|,
name|hconf
argument_list|,
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit


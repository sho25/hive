begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ddl
operator|.
name|table
operator|.
name|storage
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|CompilationOpContext
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
name|DriverContext
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
name|ddl
operator|.
name|DDLOperation
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
name|ddl
operator|.
name|DDLOperationContext
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
name|exec
operator|.
name|OperatorFactory
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
name|Task
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
name|tez
operator|.
name|TezTask
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
name|io
operator|.
name|RCFileInputFormat
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
name|io
operator|.
name|merge
operator|.
name|MergeFileTask
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
name|io
operator|.
name|merge
operator|.
name|MergeFileWork
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
name|metadata
operator|.
name|HiveException
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
name|FileMergeDesc
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
name|ListBucketingCtx
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
name|OperatorDesc
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
name|OrcFileMergeDesc
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
name|RCFileMergeDesc
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
name|TezWork
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Operation process of concatenating the files of a table/partition.  */
end_comment

begin_class
specifier|public
class|class
name|AlterTableConcatenateOperation
extends|extends
name|DDLOperation
argument_list|<
name|AlterTableConcatenateDesc
argument_list|>
block|{
specifier|public
name|AlterTableConcatenateOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|AlterTableConcatenateDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|CompilationOpContext
name|opContext
init|=
name|context
operator|.
name|getDriverContext
argument_list|()
operator|.
name|getCtx
argument_list|()
operator|.
name|getOpContext
argument_list|()
decl_stmt|;
name|MergeFileWork
name|mergeWork
init|=
name|getMergeFileWork
argument_list|(
name|opContext
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
name|getTask
argument_list|(
name|mergeWork
argument_list|)
decl_stmt|;
return|return
name|executeTask
argument_list|(
name|opContext
argument_list|,
name|task
argument_list|)
return|;
block|}
specifier|private
name|MergeFileWork
name|getMergeFileWork
parameter_list|(
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|inputDirList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|desc
operator|.
name|getInputDir
argument_list|()
argument_list|)
decl_stmt|;
comment|// merge work only needs input and output.
name|MergeFileWork
name|mergeWork
init|=
operator|new
name|MergeFileWork
argument_list|(
name|inputDirList
argument_list|,
name|desc
operator|.
name|getOutputDir
argument_list|()
argument_list|,
name|desc
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|desc
operator|.
name|getTableDesc
argument_list|()
argument_list|)
decl_stmt|;
name|mergeWork
operator|.
name|setListBucketingCtx
argument_list|(
name|desc
operator|.
name|getLbCtx
argument_list|()
argument_list|)
expr_stmt|;
name|mergeWork
operator|.
name|resolveConcatenateMerge
argument_list|(
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|mergeWork
operator|.
name|setMapperCannotSpanPartns
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|mergeWork
operator|.
name|setSourceTableInputFormat
argument_list|(
name|desc
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|inputDirStr
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|inputDirList
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
name|desc
operator|.
name|getInputDir
argument_list|()
argument_list|,
name|inputDirStr
argument_list|)
expr_stmt|;
name|mergeWork
operator|.
name|setPathToAliases
argument_list|(
name|pathToAliases
argument_list|)
expr_stmt|;
name|FileMergeDesc
name|fmd
init|=
name|getFileMergeDesc
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|mergeOp
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|opContext
argument_list|,
name|fmd
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToWork
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|aliasToWork
operator|.
name|put
argument_list|(
name|inputDirList
operator|.
name|toString
argument_list|()
argument_list|,
name|mergeOp
argument_list|)
expr_stmt|;
name|mergeWork
operator|.
name|setAliasToWork
argument_list|(
name|aliasToWork
argument_list|)
expr_stmt|;
return|return
name|mergeWork
return|;
block|}
specifier|private
name|FileMergeDesc
name|getFileMergeDesc
parameter_list|()
block|{
comment|// safe to assume else is ORC as semantic analyzer will check for RC/ORC
name|FileMergeDesc
name|fmd
init|=
operator|(
name|desc
operator|.
name|getInputFormatClass
argument_list|()
operator|.
name|equals
argument_list|(
name|RCFileInputFormat
operator|.
name|class
argument_list|)
operator|)
condition|?
operator|new
name|RCFileMergeDesc
argument_list|()
else|:
operator|new
name|OrcFileMergeDesc
argument_list|()
decl_stmt|;
name|ListBucketingCtx
name|lbCtx
init|=
name|desc
operator|.
name|getLbCtx
argument_list|()
decl_stmt|;
name|boolean
name|lbatc
init|=
name|lbCtx
operator|==
literal|null
condition|?
literal|false
else|:
name|lbCtx
operator|.
name|isSkewedStoredAsDir
argument_list|()
decl_stmt|;
name|int
name|lbd
init|=
name|lbCtx
operator|==
literal|null
condition|?
literal|0
else|:
name|lbCtx
operator|.
name|calculateListBucketingLevel
argument_list|()
decl_stmt|;
name|fmd
operator|.
name|setDpCtx
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fmd
operator|.
name|setHasDynamicPartitions
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|fmd
operator|.
name|setListBucketingAlterTableConcatenate
argument_list|(
name|lbatc
argument_list|)
expr_stmt|;
name|fmd
operator|.
name|setListBucketingDepth
argument_list|(
name|lbd
argument_list|)
expr_stmt|;
name|fmd
operator|.
name|setOutputPath
argument_list|(
name|desc
operator|.
name|getOutputDir
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|fmd
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|getTask
parameter_list|(
name|MergeFileWork
name|mergeWork
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"tez"
argument_list|)
condition|)
block|{
name|TezWork
name|tezWork
init|=
operator|new
name|TezWork
argument_list|(
name|context
operator|.
name|getQueryState
argument_list|()
operator|.
name|getQueryId
argument_list|()
argument_list|,
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|mergeWork
operator|.
name|setName
argument_list|(
literal|"File Merge"
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|mergeWork
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
operator|new
name|TezTask
argument_list|()
decl_stmt|;
operator|(
operator|(
name|TezTask
operator|)
name|task
operator|)
operator|.
name|setWork
argument_list|(
name|tezWork
argument_list|)
expr_stmt|;
return|return
name|task
return|;
block|}
else|else
block|{
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
operator|new
name|MergeFileTask
argument_list|()
decl_stmt|;
operator|(
operator|(
name|MergeFileTask
operator|)
name|task
operator|)
operator|.
name|setWork
argument_list|(
name|mergeWork
argument_list|)
expr_stmt|;
return|return
name|task
return|;
block|}
block|}
specifier|private
name|int
name|executeTask
parameter_list|(
name|CompilationOpContext
name|opContext
parameter_list|,
name|Task
argument_list|<
name|?
argument_list|>
name|task
parameter_list|)
block|{
name|DriverContext
name|driverCxt
init|=
operator|new
name|DriverContext
argument_list|()
decl_stmt|;
name|task
operator|.
name|initialize
argument_list|(
name|context
operator|.
name|getQueryState
argument_list|()
argument_list|,
name|context
operator|.
name|getQueryPlan
argument_list|()
argument_list|,
name|driverCxt
argument_list|,
name|opContext
argument_list|)
expr_stmt|;
name|int
name|ret
init|=
name|task
operator|.
name|execute
argument_list|(
name|driverCxt
argument_list|)
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|getException
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|getTask
argument_list|()
operator|.
name|setException
argument_list|(
name|task
operator|.
name|getException
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit


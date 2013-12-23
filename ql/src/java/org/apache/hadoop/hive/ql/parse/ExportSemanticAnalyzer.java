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
name|io
operator|.
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|java
operator|.
name|net
operator|.
name|URI
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
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
name|FileStatus
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
name|FileSystem
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
name|ErrorMsg
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
name|TaskFactory
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
name|metadata
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
name|plan
operator|.
name|CopyWork
import|;
end_import

begin_comment
comment|/**  * ExportSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExportSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|ExportSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Tree
name|tableTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Tree
name|toTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// initialize export path
name|String
name|tmpPath
init|=
name|stripQuotes
argument_list|(
name|toTree
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|URI
name|toURI
init|=
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|conf
argument_list|,
name|tmpPath
argument_list|)
decl_stmt|;
comment|// initialize source table/partition
name|tableSpec
name|ts
init|=
operator|new
name|tableSpec
argument_list|(
name|db
argument_list|,
name|conf
argument_list|,
operator|(
name|ASTNode
operator|)
name|tableTree
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|EximUtil
operator|.
name|validateTable
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
expr_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|toURI
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Path
name|toPath
init|=
operator|new
name|Path
argument_list|(
name|toURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|toURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|toURI
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|FileStatus
name|tgt
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|toPath
argument_list|)
decl_stmt|;
comment|// target exists
if|if
condition|(
operator|!
name|tgt
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|,
literal|"Target is not a directory : "
operator|+
name|toURI
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|toPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|,
literal|"Target is not an empty directory : "
operator|+
name|toURI
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{       }
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
literal|null
decl_stmt|;
try|try
block|{
name|partitions
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|ts
operator|.
name|tableHandle
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|partitions
operator|=
operator|(
name|ts
operator|.
name|partitions
operator|!=
literal|null
operator|)
condition|?
name|ts
operator|.
name|partitions
else|:
name|db
operator|.
name|getPartitions
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
expr_stmt|;
block|}
name|String
name|tmpfile
init|=
name|ctx
operator|.
name|getLocalTmpFileURI
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|tmpfile
argument_list|,
literal|"_metadata"
argument_list|)
decl_stmt|;
name|EximUtil
operator|.
name|createExportDump
argument_list|(
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
argument_list|,
name|path
argument_list|,
name|ts
operator|.
name|tableHandle
argument_list|,
name|partitions
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|CopyWork
argument_list|(
name|path
argument_list|,
operator|new
name|Path
argument_list|(
name|toURI
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|rTask
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"_metadata file written into "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" and then copied to "
operator|+
name|toURI
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|GENERIC_ERROR
operator|.
name|getMsg
argument_list|(
literal|"Exception while writing out the local file"
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Path
name|parentPath
init|=
operator|new
name|Path
argument_list|(
name|toURI
argument_list|)
decl_stmt|;
if|if
condition|(
name|ts
operator|.
name|tableHandle
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
name|URI
name|fromURI
init|=
name|partition
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
name|Path
name|toPartPath
init|=
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
name|partition
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|CopyWork
argument_list|(
operator|new
name|Path
argument_list|(
name|fromURI
argument_list|)
argument_list|,
name|toPartPath
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|rTask
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|partition
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|URI
name|fromURI
init|=
name|ts
operator|.
name|tableHandle
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
name|Path
name|toDataPath
init|=
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
literal|"data"
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|CopyWork
argument_list|(
operator|new
name|Path
argument_list|(
name|fromURI
argument_list|)
argument_list|,
name|toDataPath
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|rTask
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|parentPath
argument_list|,
name|toURI
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hdfs"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


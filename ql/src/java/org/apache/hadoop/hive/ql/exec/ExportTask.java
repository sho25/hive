begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|exec
package|;
end_package

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
name|metadata
operator|.
name|Hive
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|TableExport
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
name|ExportWork
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
name|api
operator|.
name|StageType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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

begin_class
specifier|public
class|class
name|ExportTask
extends|extends
name|Task
argument_list|<
name|ExportWork
argument_list|>
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ExportTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|ExportTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"EXPORT"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
try|try
block|{
comment|// Also creates the root directory
name|TableExport
operator|.
name|Paths
name|exportPaths
init|=
operator|new
name|TableExport
operator|.
name|Paths
argument_list|(
name|work
operator|.
name|getAstRepresentationForErrorMsg
argument_list|()
argument_list|,
name|work
operator|.
name|getExportRootDir
argument_list|()
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Hive
name|db
init|=
name|getHive
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exporting data to: {}"
argument_list|,
name|exportPaths
operator|.
name|getExportRootDir
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|TableExport
argument_list|(
name|exportPaths
argument_list|,
name|work
operator|.
name|getTableSpec
argument_list|()
argument_list|,
name|work
operator|.
name|getReplicationSpec
argument_list|()
argument_list|,
name|db
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|)
operator|.
name|write
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
comment|// TODO: Modify Thrift IDL to generate export stage if needed
return|return
name|StageType
operator|.
name|REPL_DUMP
return|;
block|}
block|}
end_class

end_unit


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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|filesystem
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|DatabaseEvent
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
name|EximUtil
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
name|SemanticException
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
name|load
operator|.
name|MetaData
import|;
end_import

begin_class
specifier|public
class|class
name|FSDatabaseEvent
implements|implements
name|DatabaseEvent
block|{
specifier|private
specifier|final
name|Path
name|dbMetadataFile
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fileSystem
decl_stmt|;
name|FSDatabaseEvent
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|dbDumpDirectory
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|dbMetadataFile
operator|=
operator|new
name|Path
argument_list|(
name|dbDumpDirectory
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|fileSystem
operator|=
name|dbMetadataFile
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|message
init|=
literal|"Error while identifying the filesystem for db "
operator|+
literal|"metadata file in "
operator|+
name|dbDumpDirectory
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|message
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Database
name|dbInMetadata
parameter_list|(
name|String
name|dbNameToOverride
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|MetaData
name|rv
init|=
name|EximUtil
operator|.
name|readMetaData
argument_list|(
name|fileSystem
argument_list|,
name|dbMetadataFile
argument_list|)
decl_stmt|;
name|Database
name|dbObj
init|=
name|rv
operator|.
name|getDatabase
argument_list|()
decl_stmt|;
if|if
condition|(
name|dbObj
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"_metadata file read did not contain a db object - invalid dump."
argument_list|)
throw|;
block|}
comment|// override the db name if provided in repl load command
if|if
condition|(
name|StringUtils
operator|.
name|isNotBlank
argument_list|(
name|dbNameToOverride
argument_list|)
condition|)
block|{
name|dbObj
operator|.
name|setName
argument_list|(
name|dbNameToOverride
argument_list|)
expr_stmt|;
block|}
return|return
name|dbObj
return|;
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
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|State
name|toState
parameter_list|()
block|{
return|return
operator|new
name|FSDBState
argument_list|(
name|dbMetadataFile
operator|.
name|getParent
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|EventType
name|eventType
parameter_list|()
block|{
return|return
name|EventType
operator|.
name|Database
return|;
block|}
specifier|static
class|class
name|FSDBState
implements|implements
name|DatabaseEvent
operator|.
name|State
block|{
specifier|final
name|String
name|dbDumpDirectory
decl_stmt|;
name|FSDBState
parameter_list|(
name|String
name|dbDumpDirectory
parameter_list|)
block|{
name|this
operator|.
name|dbDumpDirectory
operator|=
name|dbDumpDirectory
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DatabaseEvent
name|toEvent
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
return|return
operator|new
name|FSDatabaseEvent
argument_list|(
name|hiveConf
argument_list|,
name|dbDumpDirectory
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


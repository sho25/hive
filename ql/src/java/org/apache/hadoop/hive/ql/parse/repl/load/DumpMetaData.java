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
operator|.
name|repl
operator|.
name|load
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
name|ReplChangeManager
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
name|dump
operator|.
name|Utils
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
name|DumpType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_class
specifier|public
class|class
name|DumpMetaData
block|{
comment|// wrapper class for reading and writing metadata about a dump
comment|// responsible for _dumpmetadata files
specifier|private
specifier|static
specifier|final
name|String
name|DUMP_METADATA
init|=
literal|"_dumpmetadata"
decl_stmt|;
specifier|private
name|DumpType
name|dumpType
decl_stmt|;
specifier|private
name|Long
name|eventFrom
init|=
literal|null
decl_stmt|;
specifier|private
name|Long
name|eventTo
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|payload
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|Path
name|dumpFile
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
name|Path
name|cmRoot
decl_stmt|;
specifier|public
name|DumpMetaData
parameter_list|(
name|Path
name|dumpRoot
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|dumpFile
operator|=
operator|new
name|Path
argument_list|(
name|dumpRoot
argument_list|,
name|DUMP_METADATA
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DumpMetaData
parameter_list|(
name|Path
name|dumpRoot
parameter_list|,
name|DumpType
name|lvl
parameter_list|,
name|Long
name|eventFrom
parameter_list|,
name|Long
name|eventTo
parameter_list|,
name|Path
name|cmRoot
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|this
argument_list|(
name|dumpRoot
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|setDump
argument_list|(
name|lvl
argument_list|,
name|eventFrom
argument_list|,
name|eventTo
argument_list|,
name|cmRoot
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDump
parameter_list|(
name|DumpType
name|lvl
parameter_list|,
name|Long
name|eventFrom
parameter_list|,
name|Long
name|eventTo
parameter_list|,
name|Path
name|cmRoot
parameter_list|)
block|{
name|this
operator|.
name|dumpType
operator|=
name|lvl
expr_stmt|;
name|this
operator|.
name|eventFrom
operator|=
name|eventFrom
expr_stmt|;
name|this
operator|.
name|eventTo
operator|=
name|eventTo
expr_stmt|;
name|this
operator|.
name|initialized
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|cmRoot
operator|=
name|cmRoot
expr_stmt|;
block|}
specifier|private
name|void
name|loadDumpFromFile
parameter_list|()
throws|throws
name|SemanticException
block|{
name|BufferedReader
name|br
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// read from dumpfile and instantiate self
name|FileSystem
name|fs
init|=
name|dumpFile
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|br
operator|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|dumpFile
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|line
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|(
name|line
operator|=
name|br
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|lineContents
init|=
name|line
operator|.
name|split
argument_list|(
literal|"\t"
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|setDump
argument_list|(
name|DumpType
operator|.
name|valueOf
argument_list|(
name|lineContents
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|lineContents
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|lineContents
index|[
literal|2
index|]
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|lineContents
index|[
literal|3
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|setPayload
argument_list|(
name|lineContents
index|[
literal|4
index|]
operator|.
name|equals
argument_list|(
name|Utilities
operator|.
name|nullStringOutput
argument_list|)
condition|?
literal|null
else|:
name|lineContents
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
name|ReplChangeManager
operator|.
name|setCmRoot
argument_list|(
name|cmRoot
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to read valid values from dumpFile:"
operator|+
name|dumpFile
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|br
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|br
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|public
name|DumpType
name|getDumpType
parameter_list|()
throws|throws
name|SemanticException
block|{
name|initializeIfNot
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|dumpType
return|;
block|}
specifier|public
name|String
name|getPayload
parameter_list|()
throws|throws
name|SemanticException
block|{
name|initializeIfNot
argument_list|()
expr_stmt|;
return|return
name|this
operator|.
name|payload
return|;
block|}
specifier|public
name|void
name|setPayload
parameter_list|(
name|String
name|payload
parameter_list|)
block|{
name|this
operator|.
name|payload
operator|=
name|payload
expr_stmt|;
block|}
specifier|public
name|Long
name|getEventFrom
parameter_list|()
throws|throws
name|SemanticException
block|{
name|initializeIfNot
argument_list|()
expr_stmt|;
return|return
name|eventFrom
return|;
block|}
specifier|public
name|Long
name|getEventTo
parameter_list|()
throws|throws
name|SemanticException
block|{
name|initializeIfNot
argument_list|()
expr_stmt|;
return|return
name|eventTo
return|;
block|}
specifier|public
name|Path
name|getDumpFilePath
parameter_list|()
block|{
return|return
name|dumpFile
return|;
block|}
specifier|public
name|boolean
name|isIncrementalDump
parameter_list|()
throws|throws
name|SemanticException
block|{
name|initializeIfNot
argument_list|()
expr_stmt|;
return|return
operator|(
name|this
operator|.
name|dumpType
operator|==
name|DumpType
operator|.
name|INCREMENTAL
operator|)
return|;
block|}
specifier|private
name|void
name|initializeIfNot
parameter_list|()
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
name|loadDumpFromFile
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|()
throws|throws
name|SemanticException
block|{
name|Utils
operator|.
name|writeOutput
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|dumpType
operator|.
name|toString
argument_list|()
argument_list|,
name|eventFrom
operator|.
name|toString
argument_list|()
argument_list|,
name|eventTo
operator|.
name|toString
argument_list|()
argument_list|,
name|cmRoot
operator|.
name|toString
argument_list|()
argument_list|,
name|payload
argument_list|)
argument_list|,
name|dumpFile
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


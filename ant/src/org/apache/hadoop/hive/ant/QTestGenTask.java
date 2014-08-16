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
name|ant
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileFilter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileWriter
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
name|Set
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Splitter
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
name|Sets
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
name|lang
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
name|tools
operator|.
name|ant
operator|.
name|BuildException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tools
operator|.
name|ant
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
name|velocity
operator|.
name|app
operator|.
name|VelocityEngine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|Template
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|VelocityContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|exception
operator|.
name|MethodInvocationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|exception
operator|.
name|ParseErrorException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|exception
operator|.
name|ResourceNotFoundException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|velocity
operator|.
name|runtime
operator|.
name|RuntimeConstants
import|;
end_import

begin_class
specifier|public
class|class
name|QTestGenTask
extends|extends
name|Task
block|{
specifier|private
specifier|static
specifier|final
name|Splitter
name|CSV_SPLITTER
init|=
name|Splitter
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|trimResults
argument_list|()
operator|.
name|omitEmptyStrings
argument_list|()
decl_stmt|;
specifier|public
class|class
name|IncludeFilter
implements|implements
name|FileFilter
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|includeOnly
decl_stmt|;
specifier|public
name|IncludeFilter
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|includeOnly
parameter_list|)
block|{
name|this
operator|.
name|includeOnly
operator|=
name|includeOnly
expr_stmt|;
block|}
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|fpath
parameter_list|)
block|{
return|return
name|includeOnly
operator|==
literal|null
operator|||
name|includeOnly
operator|.
name|contains
argument_list|(
name|fpath
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|public
class|class
name|QFileFilter
extends|extends
name|IncludeFilter
block|{
specifier|public
name|QFileFilter
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|includeOnly
parameter_list|)
block|{
name|super
argument_list|(
name|includeOnly
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|fpath
parameter_list|)
block|{
if|if
condition|(
operator|!
name|super
operator|.
name|accept
argument_list|(
name|fpath
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|fpath
operator|.
name|isDirectory
argument_list|()
operator|||
operator|!
name|fpath
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".q"
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
specifier|public
class|class
name|DisabledQFileFilter
extends|extends
name|IncludeFilter
block|{
specifier|public
name|DisabledQFileFilter
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|includeOnly
parameter_list|)
block|{
name|super
argument_list|(
name|includeOnly
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|fpath
parameter_list|)
block|{
if|if
condition|(
operator|!
name|super
operator|.
name|accept
argument_list|(
name|fpath
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|!
name|fpath
operator|.
name|isDirectory
argument_list|()
operator|&&
name|fpath
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".q.disabled"
argument_list|)
return|;
block|}
block|}
specifier|public
class|class
name|QFileRegexFilter
implements|implements
name|FileFilter
block|{
name|Pattern
name|filterPattern
decl_stmt|;
specifier|public
name|QFileRegexFilter
parameter_list|(
name|String
name|filter
parameter_list|)
block|{
name|filterPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|filePath
parameter_list|)
block|{
if|if
condition|(
name|filePath
operator|.
name|isDirectory
argument_list|()
operator|||
operator|!
name|filePath
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".q"
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|testName
init|=
name|StringUtils
operator|.
name|chomp
argument_list|(
name|filePath
operator|.
name|getName
argument_list|()
argument_list|,
literal|".q"
argument_list|)
decl_stmt|;
return|return
name|filterPattern
operator|.
name|matcher
argument_list|(
name|testName
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|templatePaths
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|String
name|hiveRootDirectory
decl_stmt|;
specifier|private
name|String
name|outputDirectory
decl_stmt|;
specifier|private
name|String
name|queryDirectory
decl_stmt|;
specifier|private
name|String
name|queryFile
decl_stmt|;
specifier|private
name|String
name|includeQueryFile
decl_stmt|;
specifier|private
name|String
name|excludeQueryFile
decl_stmt|;
specifier|private
name|String
name|queryFileRegex
decl_stmt|;
specifier|private
name|String
name|resultsDirectory
decl_stmt|;
specifier|private
name|String
name|logDirectory
decl_stmt|;
specifier|private
name|String
name|template
decl_stmt|;
specifier|private
name|String
name|className
decl_stmt|;
specifier|private
name|String
name|logFile
decl_stmt|;
specifier|private
name|String
name|clusterMode
decl_stmt|;
specifier|private
name|String
name|hiveConfDir
decl_stmt|;
specifier|private
name|String
name|runDisabled
decl_stmt|;
specifier|private
name|String
name|hadoopVersion
decl_stmt|;
specifier|private
name|String
name|initScript
decl_stmt|;
specifier|private
name|String
name|cleanupScript
decl_stmt|;
specifier|public
name|void
name|setHadoopVersion
parameter_list|(
name|String
name|ver
parameter_list|)
block|{
name|this
operator|.
name|hadoopVersion
operator|=
name|ver
expr_stmt|;
block|}
specifier|public
name|String
name|getHadoopVersion
parameter_list|()
block|{
return|return
name|hadoopVersion
return|;
block|}
specifier|public
name|void
name|setHiveConfDir
parameter_list|(
name|String
name|hiveConfDir
parameter_list|)
block|{
name|this
operator|.
name|hiveConfDir
operator|=
name|hiveConfDir
expr_stmt|;
block|}
specifier|public
name|String
name|getHiveConfDir
parameter_list|()
block|{
return|return
name|hiveConfDir
return|;
block|}
specifier|public
name|void
name|setClusterMode
parameter_list|(
name|String
name|clusterMode
parameter_list|)
block|{
name|this
operator|.
name|clusterMode
operator|=
name|clusterMode
expr_stmt|;
block|}
specifier|public
name|String
name|getClusterMode
parameter_list|()
block|{
return|return
name|clusterMode
return|;
block|}
specifier|public
name|void
name|setRunDisabled
parameter_list|(
name|String
name|runDisabled
parameter_list|)
block|{
name|this
operator|.
name|runDisabled
operator|=
name|runDisabled
expr_stmt|;
block|}
specifier|public
name|String
name|getRunDisabled
parameter_list|()
block|{
return|return
name|runDisabled
return|;
block|}
specifier|public
name|void
name|setLogFile
parameter_list|(
name|String
name|logFile
parameter_list|)
block|{
name|this
operator|.
name|logFile
operator|=
name|logFile
expr_stmt|;
block|}
specifier|public
name|String
name|getLogFile
parameter_list|()
block|{
return|return
name|logFile
return|;
block|}
specifier|public
name|void
name|setClassName
parameter_list|(
name|String
name|className
parameter_list|)
block|{
name|this
operator|.
name|className
operator|=
name|className
expr_stmt|;
block|}
specifier|public
name|String
name|getClassName
parameter_list|()
block|{
return|return
name|className
return|;
block|}
specifier|public
name|void
name|setTemplate
parameter_list|(
name|String
name|template
parameter_list|)
block|{
name|this
operator|.
name|template
operator|=
name|template
expr_stmt|;
block|}
specifier|public
name|String
name|getTemplate
parameter_list|()
block|{
return|return
name|template
return|;
block|}
specifier|public
name|String
name|getInitScript
parameter_list|()
block|{
return|return
name|initScript
return|;
block|}
specifier|public
name|void
name|setInitScript
parameter_list|(
name|String
name|initScript
parameter_list|)
block|{
name|this
operator|.
name|initScript
operator|=
name|initScript
expr_stmt|;
block|}
specifier|public
name|String
name|getCleanupScript
parameter_list|()
block|{
return|return
name|cleanupScript
return|;
block|}
specifier|public
name|void
name|setCleanupScript
parameter_list|(
name|String
name|cleanupScript
parameter_list|)
block|{
name|this
operator|.
name|cleanupScript
operator|=
name|cleanupScript
expr_stmt|;
block|}
specifier|public
name|void
name|setHiveRootDirectory
parameter_list|(
name|File
name|hiveRootDirectory
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|hiveRootDirectory
operator|=
name|hiveRootDirectory
operator|.
name|getCanonicalPath
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|getHiveRootDirectory
parameter_list|()
block|{
return|return
name|hiveRootDirectory
return|;
block|}
specifier|public
name|void
name|setTemplatePath
parameter_list|(
name|String
name|templatePath
parameter_list|)
throws|throws
name|Exception
block|{
name|templatePaths
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|relativePath
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|templatePath
argument_list|)
control|)
block|{
name|templatePaths
operator|.
name|add
argument_list|(
name|project
operator|.
name|resolveFile
argument_list|(
name|relativePath
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Template Path:"
operator|+
name|getTemplatePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getTemplatePath
parameter_list|()
block|{
return|return
name|StringUtils
operator|.
name|join
argument_list|(
name|templatePaths
argument_list|,
literal|","
argument_list|)
return|;
block|}
specifier|public
name|void
name|setOutputDirectory
parameter_list|(
name|File
name|outputDirectory
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|outputDirectory
operator|=
name|outputDirectory
operator|.
name|getCanonicalPath
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|getOutputDirectory
parameter_list|()
block|{
return|return
name|outputDirectory
return|;
block|}
specifier|public
name|void
name|setLogDirectory
parameter_list|(
name|String
name|logDirectory
parameter_list|)
block|{
name|this
operator|.
name|logDirectory
operator|=
name|logDirectory
expr_stmt|;
block|}
specifier|public
name|String
name|getLogDirectory
parameter_list|()
block|{
return|return
name|logDirectory
return|;
block|}
specifier|public
name|void
name|setResultsDirectory
parameter_list|(
name|String
name|resultsDirectory
parameter_list|)
block|{
name|this
operator|.
name|resultsDirectory
operator|=
name|resultsDirectory
expr_stmt|;
block|}
specifier|public
name|String
name|getResultsDirectory
parameter_list|()
block|{
return|return
name|resultsDirectory
return|;
block|}
specifier|public
name|void
name|setQueryDirectory
parameter_list|(
name|String
name|queryDirectory
parameter_list|)
block|{
name|this
operator|.
name|queryDirectory
operator|=
name|queryDirectory
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryDirectory
parameter_list|()
block|{
return|return
name|queryDirectory
return|;
block|}
specifier|public
name|void
name|setQueryFile
parameter_list|(
name|String
name|queryFile
parameter_list|)
block|{
name|this
operator|.
name|queryFile
operator|=
name|queryFile
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryFile
parameter_list|()
block|{
return|return
name|queryFile
return|;
block|}
specifier|public
name|String
name|getIncludeQueryFile
parameter_list|()
block|{
return|return
name|includeQueryFile
return|;
block|}
specifier|public
name|void
name|setIncludeQueryFile
parameter_list|(
name|String
name|includeQueryFile
parameter_list|)
block|{
name|this
operator|.
name|includeQueryFile
operator|=
name|includeQueryFile
expr_stmt|;
block|}
specifier|public
name|void
name|setExcludeQueryFile
parameter_list|(
name|String
name|excludeQueryFile
parameter_list|)
block|{
name|this
operator|.
name|excludeQueryFile
operator|=
name|excludeQueryFile
expr_stmt|;
block|}
specifier|public
name|String
name|getExcludeQueryFile
parameter_list|()
block|{
return|return
name|excludeQueryFile
return|;
block|}
specifier|public
name|void
name|setQueryFileRegex
parameter_list|(
name|String
name|queryFileRegex
parameter_list|)
block|{
name|this
operator|.
name|queryFileRegex
operator|=
name|queryFileRegex
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryFileRegex
parameter_list|()
block|{
return|return
name|queryFileRegex
return|;
block|}
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|BuildException
block|{
if|if
condition|(
name|getTemplatePath
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"No templatePath attribute specified"
argument_list|)
throw|;
block|}
if|if
condition|(
name|template
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"No template attribute specified"
argument_list|)
throw|;
block|}
if|if
condition|(
name|outputDirectory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"No outputDirectory specified"
argument_list|)
throw|;
block|}
if|if
condition|(
name|queryDirectory
operator|==
literal|null
operator|&&
name|queryFile
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"No queryDirectory or queryFile specified"
argument_list|)
throw|;
block|}
if|if
condition|(
name|logDirectory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"No logDirectory specified"
argument_list|)
throw|;
block|}
if|if
condition|(
name|className
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"No className specified"
argument_list|)
throw|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|includeOnly
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|includeQueryFile
operator|!=
literal|null
operator|&&
operator|!
name|includeQueryFile
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|includeOnly
operator|=
name|Sets
operator|.
expr|<
name|String
operator|>
name|newHashSet
argument_list|(
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|includeQueryFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|File
argument_list|>
name|qFiles
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|qFilesMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|File
name|hiveRootDir
init|=
literal|null
decl_stmt|;
name|File
name|queryDir
init|=
literal|null
decl_stmt|;
name|File
name|outDir
init|=
literal|null
decl_stmt|;
name|File
name|resultsDir
init|=
literal|null
decl_stmt|;
name|File
name|logDir
init|=
literal|null
decl_stmt|;
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting Generation of: "
operator|+
name|className
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Include Files: "
operator|+
name|includeQueryFile
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Excluded Files: "
operator|+
name|excludeQueryFile
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Query Files: "
operator|+
name|queryFile
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Query Files Regex: "
operator|+
name|queryFileRegex
argument_list|)
expr_stmt|;
comment|// queryDirectory should not be null
name|queryDir
operator|=
operator|new
name|File
argument_list|(
name|queryDirectory
argument_list|)
expr_stmt|;
comment|// dedup file list
name|Set
argument_list|<
name|File
argument_list|>
name|testFiles
init|=
operator|new
name|HashSet
argument_list|<
name|File
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryFile
operator|!=
literal|null
operator|&&
operator|!
name|queryFile
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
comment|// The user may have passed a list of files - comma separated
for|for
control|(
name|String
name|qFile
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|queryFile
argument_list|)
control|)
block|{
if|if
condition|(
literal|null
operator|!=
name|queryDir
condition|)
block|{
name|testFiles
operator|.
name|add
argument_list|(
operator|new
name|File
argument_list|(
name|queryDir
argument_list|,
name|qFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|testFiles
operator|.
name|add
argument_list|(
operator|new
name|File
argument_list|(
name|qFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|queryFileRegex
operator|!=
literal|null
operator|&&
operator|!
name|queryFileRegex
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
for|for
control|(
name|String
name|regex
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|queryFileRegex
argument_list|)
control|)
block|{
name|testFiles
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|queryDir
operator|.
name|listFiles
argument_list|(
operator|new
name|QFileRegexFilter
argument_list|(
name|regex
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|runDisabled
operator|!=
literal|null
operator|&&
name|runDisabled
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|testFiles
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|queryDir
operator|.
name|listFiles
argument_list|(
operator|new
name|DisabledQFileFilter
argument_list|(
name|includeOnly
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|testFiles
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|queryDir
operator|.
name|listFiles
argument_list|(
operator|new
name|QFileFilter
argument_list|(
name|includeOnly
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|excludeQueryFile
operator|!=
literal|null
operator|&&
operator|!
name|excludeQueryFile
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
comment|// Exclude specified query files, comma separated
for|for
control|(
name|String
name|qFile
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|excludeQueryFile
argument_list|)
control|)
block|{
if|if
condition|(
literal|null
operator|!=
name|queryDir
condition|)
block|{
name|testFiles
operator|.
name|remove
argument_list|(
operator|new
name|File
argument_list|(
name|queryDir
argument_list|,
name|qFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|testFiles
operator|.
name|remove
argument_list|(
operator|new
name|File
argument_list|(
name|qFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|hiveRootDir
operator|=
operator|new
name|File
argument_list|(
name|hiveRootDirectory
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|hiveRootDir
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Hive Root Directory "
operator|+
name|hiveRootDir
operator|.
name|getCanonicalPath
argument_list|()
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
name|qFiles
operator|=
operator|new
name|ArrayList
argument_list|<
name|File
argument_list|>
argument_list|(
name|testFiles
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|qFiles
argument_list|)
expr_stmt|;
for|for
control|(
name|File
name|qFile
range|:
name|qFiles
control|)
block|{
name|qFilesMap
operator|.
name|put
argument_list|(
name|qFile
operator|.
name|getName
argument_list|()
argument_list|,
name|relativePath
argument_list|(
name|hiveRootDir
argument_list|,
name|qFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Make sure the output directory exists, if it doesn't then create it.
name|outDir
operator|=
operator|new
name|File
argument_list|(
name|outputDirectory
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|outDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|outDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
name|logDir
operator|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|logDir
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Log Directory "
operator|+
name|logDir
operator|.
name|getCanonicalPath
argument_list|()
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
if|if
condition|(
name|resultsDirectory
operator|!=
literal|null
condition|)
block|{
name|resultsDir
operator|=
operator|new
name|File
argument_list|(
name|resultsDirectory
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|resultsDir
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Results Directory "
operator|+
name|resultsDir
operator|.
name|getCanonicalPath
argument_list|()
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|BuildException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|VelocityEngine
name|ve
init|=
operator|new
name|VelocityEngine
argument_list|()
decl_stmt|;
try|try
block|{
name|ve
operator|.
name|setProperty
argument_list|(
name|RuntimeConstants
operator|.
name|FILE_RESOURCE_LOADER_PATH
argument_list|,
name|getTemplatePath
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|logFile
operator|!=
literal|null
condition|)
block|{
name|File
name|lf
init|=
operator|new
name|File
argument_list|(
name|logFile
argument_list|)
decl_stmt|;
if|if
condition|(
name|lf
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|lf
operator|.
name|delete
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Could not delete log file "
operator|+
name|lf
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|ve
operator|.
name|setProperty
argument_list|(
name|RuntimeConstants
operator|.
name|RUNTIME_LOG
argument_list|,
name|logFile
argument_list|)
expr_stmt|;
block|}
name|ve
operator|.
name|init
argument_list|()
expr_stmt|;
name|Template
name|t
init|=
name|ve
operator|.
name|getTemplate
argument_list|(
name|template
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterMode
operator|==
literal|null
condition|)
block|{
name|clusterMode
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|hadoopVersion
operator|==
literal|null
condition|)
block|{
name|hadoopVersion
operator|=
literal|""
expr_stmt|;
block|}
if|if
condition|(
name|hiveConfDir
operator|==
literal|null
condition|)
block|{
name|hiveConfDir
operator|=
literal|""
expr_stmt|;
block|}
comment|// For each of the qFiles generate the test
name|VelocityContext
name|ctx
init|=
operator|new
name|VelocityContext
argument_list|()
decl_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"className"
argument_list|,
name|className
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"hiveRootDir"
argument_list|,
name|escapePath
argument_list|(
name|hiveRootDir
operator|.
name|getCanonicalPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"queryDir"
argument_list|,
name|relativePath
argument_list|(
name|hiveRootDir
argument_list|,
name|queryDir
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"qfiles"
argument_list|,
name|qFiles
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"qfilesMap"
argument_list|,
name|qFilesMap
argument_list|)
expr_stmt|;
if|if
condition|(
name|resultsDir
operator|!=
literal|null
condition|)
block|{
name|ctx
operator|.
name|put
argument_list|(
literal|"resultsDir"
argument_list|,
name|relativePath
argument_list|(
name|hiveRootDir
argument_list|,
name|resultsDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|put
argument_list|(
literal|"logDir"
argument_list|,
name|relativePath
argument_list|(
name|hiveRootDir
argument_list|,
name|logDir
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"clusterMode"
argument_list|,
name|clusterMode
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"hiveConfDir"
argument_list|,
name|escapePath
argument_list|(
name|hiveConfDir
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"hadoopVersion"
argument_list|,
name|hadoopVersion
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"initScript"
argument_list|,
name|initScript
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"cleanupScript"
argument_list|,
name|cleanupScript
argument_list|)
expr_stmt|;
name|File
name|outFile
init|=
operator|new
name|File
argument_list|(
name|outDir
argument_list|,
name|className
operator|+
literal|".java"
argument_list|)
decl_stmt|;
name|FileWriter
name|writer
init|=
operator|new
name|FileWriter
argument_list|(
name|outFile
argument_list|)
decl_stmt|;
name|t
operator|.
name|merge
argument_list|(
name|ctx
argument_list|,
name|writer
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Generated "
operator|+
name|outFile
operator|.
name|getCanonicalPath
argument_list|()
operator|+
literal|" from template "
operator|+
name|template
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BuildException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|MethodInvocationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Exception thrown by '"
operator|+
name|e
operator|.
name|getReferenceName
argument_list|()
operator|+
literal|"."
operator|+
name|e
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"'"
argument_list|,
name|e
operator|.
name|getWrappedThrowable
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ParseErrorException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Velocity syntax error"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ResourceNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Resource not found"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|BuildException
argument_list|(
literal|"Generation failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|relativePath
parameter_list|(
name|File
name|hiveRootDir
parameter_list|,
name|File
name|file
parameter_list|)
block|{
return|return
name|escapePath
argument_list|(
name|hiveRootDir
operator|.
name|toURI
argument_list|()
operator|.
name|relativize
argument_list|(
name|file
operator|.
name|toURI
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|escapePath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.name"
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"win"
argument_list|)
condition|)
block|{
comment|// Escape the backward slash in CanonicalPath if the unit test runs on windows
comment|// e.g. dir.getCanonicalPath() gets the absolute path of local
comment|// directory. When we embed it directly in the generated java class it results
comment|// in compiler error in windows. Reason : the canonical path contains backward
comment|// slashes "C:\temp\etc\" and it is not a valid string in Java
comment|// unless we escape the backward slashes.
return|return
name|path
operator|.
name|replace
argument_list|(
literal|"\\"
argument_list|,
literal|"\\\\"
argument_list|)
return|;
block|}
return|return
name|path
return|;
block|}
block|}
end_class

end_unit


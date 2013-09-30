begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|conf
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
name|FileInputStream
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|Preconditions
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
name|Strings
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
name|Maps
import|;
end_import

begin_class
specifier|public
class|class
name|TestConfiguration
block|{
specifier|public
specifier|static
specifier|final
name|String
name|REPOSITORY
init|=
literal|"repository"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPOSITORY_NAME
init|=
literal|"repositoryName"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BRANCH
init|=
literal|"branch"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JAVA_HOME
init|=
literal|"javaHome"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JAVA_HOME_TEST
init|=
literal|"javaHomeForTests"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ANT_ENV_OPTS
init|=
literal|"antEnvOpts"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|REPOSITORY_TYPE
init|=
literal|"repositoryType"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GIT
init|=
literal|"git"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SVN
init|=
literal|"svn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANT_ARGS
init|=
literal|"antArgs"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JIRA_URL
init|=
literal|"jiraUrl"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JIRA_USER
init|=
literal|"jiraUser"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JIRA_PASSWORD
init|=
literal|"jiraPassword"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|JENKINS_URL
init|=
literal|"jenkinsURL"
decl_stmt|;
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
name|String
name|antArgs
decl_stmt|;
specifier|private
name|String
name|antEnvOpts
decl_stmt|;
specifier|private
name|String
name|repositoryType
decl_stmt|;
specifier|private
name|String
name|repository
decl_stmt|;
specifier|private
name|String
name|repositoryName
decl_stmt|;
specifier|private
name|String
name|patch
decl_stmt|;
specifier|private
name|String
name|javaHome
decl_stmt|;
specifier|private
name|String
name|javaHomeForTests
decl_stmt|;
specifier|private
name|String
name|branch
decl_stmt|;
specifier|private
specifier|final
name|String
name|jenkinsURL
decl_stmt|;
specifier|private
specifier|final
name|String
name|jiraUrl
decl_stmt|;
specifier|private
specifier|final
name|String
name|jiraUser
decl_stmt|;
specifier|private
specifier|final
name|String
name|jiraPassword
decl_stmt|;
specifier|private
name|String
name|jiraName
decl_stmt|;
specifier|private
name|boolean
name|clearLibraryCache
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|public
name|TestConfiguration
parameter_list|(
name|Context
name|context
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|repositoryType
operator|=
name|context
operator|.
name|getString
argument_list|(
name|REPOSITORY_TYPE
argument_list|,
name|GIT
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|repository
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|REPOSITORY
argument_list|)
argument_list|,
name|REPOSITORY
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|repositoryName
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|REPOSITORY_NAME
argument_list|)
argument_list|,
name|REPOSITORY_NAME
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|GIT
operator|.
name|equals
argument_list|(
name|repositoryType
argument_list|)
condition|)
block|{
name|branch
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|BRANCH
argument_list|)
argument_list|,
name|BRANCH
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|SVN
operator|.
name|equals
argument_list|(
name|repositoryType
argument_list|)
condition|)
block|{
name|branch
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unkown repository type '"
operator|+
name|repositoryType
operator|+
literal|"'"
argument_list|)
throw|;
block|}
name|antArgs
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|context
operator|.
name|getString
argument_list|(
name|ANT_ARGS
argument_list|)
argument_list|,
name|ANT_ARGS
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|antEnvOpts
operator|=
name|context
operator|.
name|getString
argument_list|(
name|ANT_ENV_OPTS
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|javaHome
operator|=
name|context
operator|.
name|getString
argument_list|(
name|JAVA_HOME
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|javaHomeForTests
operator|=
name|context
operator|.
name|getString
argument_list|(
name|JAVA_HOME_TEST
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|patch
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|jiraName
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|jiraUrl
operator|=
name|context
operator|.
name|getString
argument_list|(
name|JIRA_URL
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|jiraUser
operator|=
name|context
operator|.
name|getString
argument_list|(
name|JIRA_USER
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|jiraPassword
operator|=
name|context
operator|.
name|getString
argument_list|(
name|JIRA_PASSWORD
argument_list|,
literal|""
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
name|jenkinsURL
operator|=
name|context
operator|.
name|getString
argument_list|(
name|JENKINS_URL
argument_list|,
literal|"https://builds.apache.org/job"
argument_list|)
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Context
name|getContext
parameter_list|()
block|{
return|return
name|context
return|;
block|}
specifier|public
name|String
name|getJenkinsURL
parameter_list|()
block|{
return|return
name|jenkinsURL
return|;
block|}
specifier|public
name|String
name|getJiraName
parameter_list|()
block|{
return|return
name|jiraName
return|;
block|}
specifier|public
name|void
name|setJiraName
parameter_list|(
name|String
name|jiraName
parameter_list|)
block|{
name|this
operator|.
name|jiraName
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|jiraName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isClearLibraryCache
parameter_list|()
block|{
return|return
name|clearLibraryCache
return|;
block|}
specifier|public
name|void
name|setClearLibraryCache
parameter_list|(
name|boolean
name|clearLibraryCache
parameter_list|)
block|{
name|this
operator|.
name|clearLibraryCache
operator|=
name|clearLibraryCache
expr_stmt|;
block|}
specifier|public
name|String
name|getJiraUrl
parameter_list|()
block|{
return|return
name|jiraUrl
return|;
block|}
specifier|public
name|String
name|getJiraUser
parameter_list|()
block|{
return|return
name|jiraUser
return|;
block|}
specifier|public
name|String
name|getJiraPassword
parameter_list|()
block|{
return|return
name|jiraPassword
return|;
block|}
specifier|public
name|String
name|getRepositoryType
parameter_list|()
block|{
return|return
name|repositoryType
return|;
block|}
specifier|public
name|String
name|getRepositoryName
parameter_list|()
block|{
return|return
name|repositoryName
return|;
block|}
specifier|public
name|String
name|getRepository
parameter_list|()
block|{
return|return
name|repository
return|;
block|}
specifier|public
name|String
name|getBranch
parameter_list|()
block|{
return|return
name|branch
return|;
block|}
specifier|public
name|String
name|getAntArgs
parameter_list|()
block|{
return|return
name|antArgs
return|;
block|}
specifier|public
name|String
name|getJavaHome
parameter_list|()
block|{
return|return
name|javaHome
return|;
block|}
specifier|public
name|String
name|getJavaHomeForTests
parameter_list|()
block|{
return|return
name|javaHomeForTests
return|;
block|}
specifier|public
name|String
name|getPatch
parameter_list|()
block|{
return|return
name|patch
return|;
block|}
specifier|public
name|void
name|setPatch
parameter_list|(
name|String
name|patch
parameter_list|)
block|{
name|this
operator|.
name|patch
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|patch
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRepository
parameter_list|(
name|String
name|repository
parameter_list|)
block|{
name|this
operator|.
name|repository
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|repository
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setRepositoryName
parameter_list|(
name|String
name|repositoryName
parameter_list|)
block|{
name|this
operator|.
name|repositoryName
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|repositoryName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setBranch
parameter_list|(
name|String
name|branch
parameter_list|)
block|{
name|this
operator|.
name|branch
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|branch
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setJavaHome
parameter_list|(
name|String
name|javaHome
parameter_list|)
block|{
name|this
operator|.
name|javaHome
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|javaHome
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setJavaHomeForTests
parameter_list|(
name|String
name|javaHomeForTests
parameter_list|)
block|{
name|this
operator|.
name|javaHomeForTests
operator|=
name|javaHomeForTests
expr_stmt|;
block|}
specifier|public
name|void
name|setAntArgs
parameter_list|(
name|String
name|antArgs
parameter_list|)
block|{
name|this
operator|.
name|antArgs
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|antArgs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getAntEnvOpts
parameter_list|()
block|{
return|return
name|antEnvOpts
return|;
block|}
specifier|public
name|void
name|setAntEnvOpts
parameter_list|(
name|String
name|antEnvOpts
parameter_list|)
block|{
name|this
operator|.
name|antEnvOpts
operator|=
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|antEnvOpts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"TestConfiguration [antArgs="
operator|+
name|antArgs
operator|+
literal|", antEnvOpts="
operator|+
name|antEnvOpts
operator|+
literal|", repositoryType="
operator|+
name|repositoryType
operator|+
literal|", repository="
operator|+
name|repository
operator|+
literal|", repositoryName="
operator|+
name|repositoryName
operator|+
literal|", patch="
operator|+
name|patch
operator|+
literal|", javaHome="
operator|+
name|javaHome
operator|+
literal|", javaHomeForTests="
operator|+
name|javaHomeForTests
operator|+
literal|", branch="
operator|+
name|branch
operator|+
literal|", jenkinsURL="
operator|+
name|jenkinsURL
operator|+
literal|", jiraUrl="
operator|+
name|jiraUrl
operator|+
literal|", jiraUser="
operator|+
name|jiraUser
operator|+
literal|", jiraName="
operator|+
name|jiraName
operator|+
literal|", clearLibraryCache="
operator|+
name|clearLibraryCache
operator|+
literal|"]"
return|;
block|}
specifier|public
specifier|static
name|TestConfiguration
name|fromInputStream
parameter_list|(
name|InputStream
name|inputStream
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|properties
operator|.
name|load
argument_list|(
name|inputStream
argument_list|)
expr_stmt|;
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|Maps
operator|.
name|fromProperties
argument_list|(
name|properties
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|TestConfiguration
argument_list|(
name|context
argument_list|,
name|logger
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TestConfiguration
name|fromFile
parameter_list|(
name|String
name|file
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromFile
argument_list|(
operator|new
name|File
argument_list|(
name|file
argument_list|)
argument_list|,
name|logger
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TestConfiguration
name|fromFile
parameter_list|(
name|File
name|file
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStream
name|in
init|=
operator|new
name|FileInputStream
argument_list|(
name|file
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|fromInputStream
argument_list|(
name|in
argument_list|,
name|logger
argument_list|)
return|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


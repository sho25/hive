begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|har
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
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
name|mapreduce
operator|.
name|JobContext
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
name|tools
operator|.
name|HadoopArchives
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_class
specifier|public
class|class
name|HarOutputCommitterPostProcessor
block|{
name|boolean
name|isEnabled
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|isEnabled
parameter_list|()
block|{
return|return
name|isEnabled
return|;
block|}
specifier|public
name|void
name|setEnabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|isEnabled
operator|=
name|enabled
expr_stmt|;
block|}
specifier|public
name|void
name|exec
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|Partition
name|partition
parameter_list|,
name|Path
name|partPath
parameter_list|)
throws|throws
name|IOException
block|{
comment|//    LOG.info("Archiving partition ["+partPath.toString()+"]");
name|makeHar
argument_list|(
name|context
argument_list|,
name|partPath
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|harFile
argument_list|(
name|partPath
argument_list|)
argument_list|)
expr_stmt|;
name|partition
operator|.
name|getParameters
argument_list|()
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|IS_ARCHIVED
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|harFile
parameter_list|(
name|Path
name|ptnPath
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|harFile
init|=
name|ptnPath
operator|.
name|toString
argument_list|()
operator|.
name|replaceFirst
argument_list|(
literal|"/+$"
argument_list|,
literal|""
argument_list|)
operator|+
literal|".har"
decl_stmt|;
comment|//    LOG.info("har file : " + harFile);
return|return
name|harFile
return|;
block|}
specifier|public
name|String
name|getParentFSPath
parameter_list|(
name|Path
name|ptnPath
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ptnPath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
operator|.
name|replaceFirst
argument_list|(
literal|"/+$"
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|public
name|String
name|getProcessedLocation
parameter_list|(
name|Path
name|ptnPath
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|harLocn
init|=
operator|(
literal|"har://"
operator|+
name|ptnPath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
operator|)
operator|.
name|replaceFirst
argument_list|(
literal|"/+$"
argument_list|,
literal|""
argument_list|)
operator|+
literal|".har"
operator|+
name|Path
operator|.
name|SEPARATOR
decl_stmt|;
comment|//    LOG.info("har location : " + harLocn);
return|return
name|harLocn
return|;
block|}
comment|/**      * Creates a har file from the contents of a given directory, using that as root.      * @param dir Directory to archive      * @param harFile The HAR file to create      */
specifier|public
specifier|static
name|void
name|makeHar
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|String
name|dir
parameter_list|,
name|String
name|harFile
parameter_list|)
throws|throws
name|IOException
block|{
comment|//    Configuration conf = context.getConfiguration();
comment|//    Credentials creds = context.getCredentials();
comment|//    HCatUtil.logAllTokens(LOG,context);
name|int
name|lastSep
init|=
name|harFile
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR_CHAR
argument_list|)
decl_stmt|;
name|Path
name|archivePath
init|=
operator|new
name|Path
argument_list|(
name|harFile
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|lastSep
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|String
index|[]
name|args
init|=
block|{
literal|"-archiveName"
block|,
name|harFile
operator|.
name|substring
argument_list|(
name|lastSep
operator|+
literal|1
argument_list|,
name|harFile
operator|.
name|length
argument_list|()
argument_list|)
block|,
literal|"-p"
block|,
name|dir
block|,
literal|"*"
block|,
name|archivePath
operator|.
name|toString
argument_list|()
block|}
decl_stmt|;
comment|//    for (String arg : args){
comment|//      LOG.info("Args to har : "+ arg);
comment|//    }
try|try
block|{
name|Configuration
name|newConf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|archivePath
operator|.
name|getFileSystem
argument_list|(
name|newConf
argument_list|)
decl_stmt|;
name|String
name|hadoopTokenFileLocationEnvSetting
init|=
name|System
operator|.
name|getenv
argument_list|(
name|HCatConstants
operator|.
name|SYSENV_HADOOP_TOKEN_FILE_LOCATION
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|hadoopTokenFileLocationEnvSetting
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|hadoopTokenFileLocationEnvSetting
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|newConf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|CONF_MAPREDUCE_JOB_CREDENTIALS_BINARY
argument_list|,
name|hadoopTokenFileLocationEnvSetting
argument_list|)
expr_stmt|;
comment|//      LOG.info("System.getenv(\"HADOOP_TOKEN_FILE_LOCATION\") =["+  System.getenv("HADOOP_TOKEN_FILE_LOCATION")+"]");
block|}
comment|//      for (FileStatus ds : fs.globStatus(new Path(dir, "*"))){
comment|//        LOG.info("src : "+ds.getPath().toUri().toString());
comment|//      }
specifier|final
name|HadoopArchives
name|har
init|=
operator|new
name|HadoopArchives
argument_list|(
name|newConf
argument_list|)
decl_stmt|;
name|int
name|rc
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|har
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|rc
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Har returned error code "
operator|+
name|rc
argument_list|)
throw|;
block|}
comment|//      for (FileStatus hs : fs.globStatus(new Path(harFile, "*"))){
comment|//        LOG.info("dest : "+hs.getPath().toUri().toString());
comment|//      }
comment|//      doHarCheck(fs,harFile);
comment|//      LOG.info("Nuking " + dir);
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|dir
argument_list|)
argument_list|,
literal|true
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
name|HCatException
argument_list|(
literal|"Error creating Har ["
operator|+
name|harFile
operator|+
literal|"] from ["
operator|+
name|dir
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


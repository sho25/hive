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
name|hcatalog
operator|.
name|data
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
name|Map
operator|.
name|Entry
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
name|cli
operator|.
name|CliSessionState
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
name|CommandNeedRetryException
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
name|Driver
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|MiniCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatOutputFormat
import|;
end_import

begin_comment
comment|/**  * Helper class for Other Data Testers  */
end_comment

begin_class
specifier|public
class|class
name|HCatDataCheckUtil
block|{
specifier|public
specifier|static
name|Driver
name|instantiateDriver
parameter_list|(
name|MiniCluster
name|cluster
parameter_list|)
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|HCatDataCheckUtil
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Entry
name|e
range|:
name|cluster
operator|.
name|getProperties
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|hiveConf
operator|.
name|set
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|Log
name|logger
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HCatOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
name|HCatUtil
operator|.
name|logHiveConf
argument_list|(
name|logger
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|driver
return|;
block|}
specifier|public
specifier|static
name|void
name|generateDataFile
parameter_list|(
name|MiniCluster
name|cluster
parameter_list|,
name|String
name|fileName
parameter_list|)
throws|throws
name|IOException
block|{
name|MiniCluster
operator|.
name|deleteFile
argument_list|(
name|cluster
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
name|String
index|[]
name|input
init|=
operator|new
name|String
index|[
literal|50
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|50
condition|;
name|i
operator|++
control|)
block|{
name|input
index|[
name|i
index|]
operator|=
operator|(
name|i
operator|%
literal|5
operator|)
operator|+
literal|"\t"
operator|+
name|i
operator|+
literal|"\t"
operator|+
literal|"_S"
operator|+
name|i
operator|+
literal|"S_"
expr_stmt|;
block|}
name|MiniCluster
operator|.
name|createInputFile
argument_list|(
name|cluster
argument_list|,
name|fileName
argument_list|,
name|input
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|createTable
parameter_list|(
name|Driver
name|driver
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|createTableArgs
parameter_list|)
throws|throws
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|String
name|createTable
init|=
literal|"create table "
operator|+
name|tableName
operator|+
name|createTableArgs
decl_stmt|;
name|int
name|retCode
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|retCode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create table. ["
operator|+
name|createTable
operator|+
literal|"], return code from hive driver : ["
operator|+
name|retCode
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|dropTable
parameter_list|(
name|Driver
name|driver
parameter_list|,
name|String
name|tablename
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists "
operator|+
name|tablename
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|String
argument_list|>
name|formattedRun
parameter_list|(
name|Driver
name|driver
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|selectCmd
parameter_list|)
throws|throws
name|CommandNeedRetryException
throws|,
name|IOException
block|{
name|driver
operator|.
name|run
argument_list|(
name|selectCmd
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|src_values
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|src_values
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|src_values
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|name
operator|+
literal|":"
operator|+
name|s
argument_list|)
expr_stmt|;
block|}
return|return
name|src_values
return|;
block|}
block|}
end_class

end_unit


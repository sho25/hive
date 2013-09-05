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
name|hbase
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
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
name|HashMap
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
name|processors
operator|.
name|CommandProcessorResponse
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
name|hadoop
operator|.
name|mapreduce
operator|.
name|Job
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
name|cli
operator|.
name|HCatDriver
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
name|cli
operator|.
name|SemanticAnalysis
operator|.
name|HCatSemanticAnalyzer
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
name|HCatUtil
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
name|hbase
operator|.
name|snapshot
operator|.
name|TableSnapshot
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
name|mapreduce
operator|.
name|HCatInputFormat
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
name|mapreduce
operator|.
name|InputJobInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestSnapshots
extends|extends
name|SkeletonHBaseTest
block|{
specifier|private
specifier|static
name|HiveConf
name|hcatConf
decl_stmt|;
specifier|private
specifier|static
name|HCatDriver
name|hcatDriver
decl_stmt|;
specifier|public
name|void
name|Initialize
parameter_list|()
throws|throws
name|Exception
block|{
name|hcatConf
operator|=
name|getHiveConf
argument_list|()
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|SEMANTIC_ANALYZER_HOOK
operator|.
name|varname
argument_list|,
name|HCatSemanticAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|URI
name|fsuri
init|=
name|getFileSystem
argument_list|()
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|Path
name|whPath
init|=
operator|new
name|Path
argument_list|(
name|fsuri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fsuri
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|getTestDir
argument_list|()
argument_list|)
decl_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPFS
operator|.
name|varname
argument_list|,
name|fsuri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hcatConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|whPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|//Add hbase properties
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|el
range|:
name|getHbaseConf
argument_list|()
control|)
block|{
if|if
condition|(
name|el
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"hbase."
argument_list|)
condition|)
block|{
name|hcatConf
operator|.
name|set
argument_list|(
name|el
operator|.
name|getKey
argument_list|()
argument_list|,
name|el
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hcatConf
argument_list|)
argument_list|)
expr_stmt|;
name|hcatDriver
operator|=
operator|new
name|HCatDriver
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestSnapshotConversion
parameter_list|()
throws|throws
name|Exception
block|{
name|Initialize
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
name|newTableName
argument_list|(
literal|"mytableOne"
argument_list|)
decl_stmt|;
name|String
name|databaseName
init|=
name|newTableName
argument_list|(
literal|"mydatabase"
argument_list|)
decl_stmt|;
name|String
name|fullyQualTableName
init|=
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
decl_stmt|;
name|String
name|db_dir
init|=
operator|new
name|Path
argument_list|(
name|getTestDir
argument_list|()
argument_list|,
literal|"hbasedb"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|dbquery
init|=
literal|"CREATE DATABASE IF NOT EXISTS "
operator|+
name|databaseName
operator|+
literal|" LOCATION '"
operator|+
name|db_dir
operator|+
literal|"'"
decl_stmt|;
name|String
name|tableQuery
init|=
literal|"CREATE TABLE "
operator|+
name|fullyQualTableName
operator|+
literal|"(key string, value1 string, value2 string) STORED BY "
operator|+
literal|"'org.apache.hive.hcatalog.hbase.HBaseHCatStorageHandler'"
operator|+
literal|"TBLPROPERTIES ('hbase.columns.mapping'=':key,cf1:q1,cf2:q2')"
decl_stmt|;
name|CommandProcessorResponse
name|cmdResponse
init|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|dbquery
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cmdResponse
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|cmdResponse
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|tableQuery
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cmdResponse
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|hcatConf
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_HIVE_CONF
argument_list|,
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|getHiveConf
argument_list|()
operator|.
name|getAllProperties
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|HBaseConstants
operator|.
name|PROPERTY_TABLE_SNAPSHOT_KEY
argument_list|,
literal|"dummysnapshot"
argument_list|)
expr_stmt|;
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|databaseName
argument_list|,
name|tableName
argument_list|)
operator|.
name|setProperties
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|String
name|modifiedInputInfo
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_JOB_INFO
argument_list|)
decl_stmt|;
name|InputJobInfo
name|inputInfo
init|=
operator|(
name|InputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|modifiedInputInfo
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|revMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|revMap
operator|.
name|put
argument_list|(
literal|"cf1"
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
name|revMap
operator|.
name|put
argument_list|(
literal|"cf2"
argument_list|,
literal|5L
argument_list|)
expr_stmt|;
name|TableSnapshot
name|hbaseSnapshot
init|=
operator|new
name|TableSnapshot
argument_list|(
name|fullyQualTableName
argument_list|,
name|revMap
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|HCatTableSnapshot
name|hcatSnapshot
init|=
name|HBaseRevisionManagerUtil
operator|.
name|convertSnapshot
argument_list|(
name|hbaseSnapshot
argument_list|,
name|inputInfo
operator|.
name|getTableInfo
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hcatSnapshot
operator|.
name|getRevision
argument_list|(
literal|"value1"
argument_list|)
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcatSnapshot
operator|.
name|getRevision
argument_list|(
literal|"value2"
argument_list|)
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|String
name|dropTable
init|=
literal|"DROP TABLE "
operator|+
name|fullyQualTableName
decl_stmt|;
name|cmdResponse
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|dropTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cmdResponse
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|tableName
operator|=
name|newTableName
argument_list|(
literal|"mytableTwo"
argument_list|)
expr_stmt|;
name|fullyQualTableName
operator|=
name|databaseName
operator|+
literal|"."
operator|+
name|tableName
expr_stmt|;
name|tableQuery
operator|=
literal|"CREATE TABLE "
operator|+
name|fullyQualTableName
operator|+
literal|"(key string, value1 string, value2 string) STORED BY "
operator|+
literal|"'org.apache.hive.hcatalog.hbase.HBaseHCatStorageHandler'"
operator|+
literal|"TBLPROPERTIES ('hbase.columns.mapping'=':key,cf1:q1,cf1:q2')"
expr_stmt|;
name|cmdResponse
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|tableQuery
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cmdResponse
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|revMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|revMap
operator|.
name|put
argument_list|(
literal|"cf1"
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
name|hbaseSnapshot
operator|=
operator|new
name|TableSnapshot
argument_list|(
name|fullyQualTableName
argument_list|,
name|revMap
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|HCatInputFormat
operator|.
name|setInput
argument_list|(
name|job
argument_list|,
name|databaseName
argument_list|,
name|tableName
argument_list|)
operator|.
name|setProperties
argument_list|(
name|properties
argument_list|)
expr_stmt|;
name|modifiedInputInfo
operator|=
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_JOB_INFO
argument_list|)
expr_stmt|;
name|inputInfo
operator|=
operator|(
name|InputJobInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|modifiedInputInfo
argument_list|)
expr_stmt|;
name|hcatSnapshot
operator|=
name|HBaseRevisionManagerUtil
operator|.
name|convertSnapshot
argument_list|(
name|hbaseSnapshot
argument_list|,
name|inputInfo
operator|.
name|getTableInfo
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcatSnapshot
operator|.
name|getRevision
argument_list|(
literal|"value1"
argument_list|)
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hcatSnapshot
operator|.
name|getRevision
argument_list|(
literal|"value2"
argument_list|)
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|dropTable
operator|=
literal|"DROP TABLE "
operator|+
name|fullyQualTableName
expr_stmt|;
name|cmdResponse
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|dropTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cmdResponse
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|dropDatabase
init|=
literal|"DROP DATABASE IF EXISTS "
operator|+
name|databaseName
operator|+
literal|"CASCADE"
decl_stmt|;
name|cmdResponse
operator|=
name|hcatDriver
operator|.
name|run
argument_list|(
name|dropDatabase
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cmdResponse
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


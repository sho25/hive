begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|druid
package|;
end_package

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
name|Throwables
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
name|ImmutableMap
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

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|indexer
operator|.
name|JobHelper
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|indexer
operator|.
name|SQLMetadataStorageUpdaterJobHandler
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|metadata
operator|.
name|MetadataStorageTablesConfig
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|metadata
operator|.
name|SQLMetadataSegmentManager
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|segment
operator|.
name|loading
operator|.
name|SegmentLoadingException
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|timeline
operator|.
name|DataSegment
import|;
end_import

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|timeline
operator|.
name|partition
operator|.
name|NoneShardSpec
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
name|LocalFileSystem
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
name|Constants
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
name|MetaException
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
name|StorageDescriptor
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
name|Table
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Interval
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TemporaryFolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|skife
operator|.
name|jdbi
operator|.
name|v2
operator|.
name|Handle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|skife
operator|.
name|jdbi
operator|.
name|v2
operator|.
name|StatementContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|skife
operator|.
name|jdbi
operator|.
name|v2
operator|.
name|tweak
operator|.
name|HandleCallback
import|;
end_import

begin_import
import|import
name|org
operator|.
name|skife
operator|.
name|jdbi
operator|.
name|v2
operator|.
name|tweak
operator|.
name|ResultSetMapper
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_class
specifier|public
class|class
name|TestDruidStorageHandler
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|DerbyConnectorTestUtility
operator|.
name|DerbyConnectorRule
name|derbyConnectorRule
init|=
operator|new
name|DerbyConnectorTestUtility
operator|.
name|DerbyConnectorRule
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TemporaryFolder
name|temporaryFolder
init|=
operator|new
name|TemporaryFolder
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DATA_SOURCE_NAME
init|=
literal|"testName"
decl_stmt|;
specifier|private
name|String
name|segmentsTable
decl_stmt|;
specifier|private
name|String
name|tableWorkingPath
decl_stmt|;
specifier|private
name|DataSegment
name|dataSegment
init|=
name|DataSegment
operator|.
name|builder
argument_list|()
operator|.
name|dataSource
argument_list|(
name|DATA_SOURCE_NAME
argument_list|)
operator|.
name|version
argument_list|(
literal|"v1"
argument_list|)
operator|.
name|interval
argument_list|(
operator|new
name|Interval
argument_list|(
literal|100
argument_list|,
literal|170
argument_list|)
argument_list|)
operator|.
name|shardSpec
argument_list|(
name|NoneShardSpec
operator|.
name|instance
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Throwable
block|{
name|tableWorkingPath
operator|=
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
expr_stmt|;
name|segmentsTable
operator|=
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getSegmentsTable
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mockMap
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|Constants
operator|.
name|DRUID_DATA_SOURCE
argument_list|,
name|DATA_SOURCE_NAME
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableMock
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockMap
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableMock
operator|.
name|getPartitionKeysSize
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|storageDes
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|StorageDescriptor
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|storageDes
operator|.
name|getBucketColsSize
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableMock
operator|.
name|getSd
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|storageDes
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|tableMock
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|DATA_SOURCE_NAME
argument_list|)
expr_stmt|;
block|}
name|Table
name|tableMock
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testPreCreateTableWillCreateSegmentsTable
parameter_list|()
throws|throws
name|MetaException
block|{
name|DruidStorageHandler
name|druidStorageHandler
init|=
operator|new
name|DruidStorageHandler
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
init|(
name|Handle
name|handle
init|=
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
operator|.
name|getDBI
argument_list|()
operator|.
name|open
argument_list|()
init|)
block|{
name|Assert
operator|.
name|assertFalse
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
operator|.
name|tableExists
argument_list|(
name|handle
argument_list|,
name|segmentsTable
argument_list|)
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
operator|.
name|tableExists
argument_list|(
name|handle
argument_list|,
name|segmentsTable
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testPreCreateTableWhenDataSourceExists
parameter_list|()
throws|throws
name|MetaException
block|{
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
operator|.
name|createSegmentTable
argument_list|()
expr_stmt|;
name|SQLMetadataStorageUpdaterJobHandler
name|sqlMetadataStorageUpdaterJobHandler
init|=
operator|new
name|SQLMetadataStorageUpdaterJobHandler
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|)
decl_stmt|;
name|sqlMetadataStorageUpdaterJobHandler
operator|.
name|publishSegments
argument_list|(
name|segmentsTable
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|dataSegment
argument_list|)
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
argument_list|)
expr_stmt|;
name|DruidStorageHandler
name|druidStorageHandler
init|=
operator|new
name|DruidStorageHandler
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|druidStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCommitCreateTablePlusCommitDropTableWithoutPurge
parameter_list|()
throws|throws
name|MetaException
throws|,
name|IOException
block|{
name|DruidStorageHandler
name|druidStorageHandler
init|=
operator|new
name|DruidStorageHandler
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|druidStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|config
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DRUID_WORKING_DIR
argument_list|)
argument_list|,
name|tableWorkingPath
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|setConf
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|LocalFileSystem
name|localFileSystem
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|Path
name|taskDirPath
init|=
operator|new
name|Path
argument_list|(
name|tableWorkingPath
argument_list|,
name|druidStorageHandler
operator|.
name|makeStagingName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|descriptorPath
init|=
name|DruidStorageHandlerUtils
operator|.
name|makeSegmentDescriptorOutputPath
argument_list|(
name|dataSegment
argument_list|,
operator|new
name|Path
argument_list|(
name|taskDirPath
argument_list|,
name|DruidStorageHandler
operator|.
name|SEGMENTS_DESCRIPTOR_DIR_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|DruidStorageHandlerUtils
operator|.
name|writeSegmentDescriptor
argument_list|(
name|localFileSystem
argument_list|,
name|dataSegment
argument_list|,
name|descriptorPath
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|commitCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DATA_SOURCE_NAME
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|getAllDataSourceNames
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|commitDropTable
argument_list|(
name|tableMock
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|getAllDataSourceNames
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCommitInsertTable
parameter_list|()
throws|throws
name|MetaException
throws|,
name|IOException
block|{
name|DruidStorageHandler
name|druidStorageHandler
init|=
operator|new
name|DruidStorageHandler
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|druidStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|config
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DRUID_WORKING_DIR
argument_list|)
argument_list|,
name|tableWorkingPath
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|setConf
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|LocalFileSystem
name|localFileSystem
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|Path
name|taskDirPath
init|=
operator|new
name|Path
argument_list|(
name|tableWorkingPath
argument_list|,
name|druidStorageHandler
operator|.
name|makeStagingName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|descriptorPath
init|=
name|DruidStorageHandlerUtils
operator|.
name|makeSegmentDescriptorOutputPath
argument_list|(
name|dataSegment
argument_list|,
operator|new
name|Path
argument_list|(
name|taskDirPath
argument_list|,
name|DruidStorageHandler
operator|.
name|SEGMENTS_DESCRIPTOR_DIR_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|DruidStorageHandlerUtils
operator|.
name|writeSegmentDescriptor
argument_list|(
name|localFileSystem
argument_list|,
name|dataSegment
argument_list|,
name|descriptorPath
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|commitCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DATA_SOURCE_NAME
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|getAllDataSourceNames
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDeleteSegment
parameter_list|()
throws|throws
name|IOException
throws|,
name|SegmentLoadingException
block|{
name|DruidStorageHandler
name|druidStorageHandler
init|=
operator|new
name|DruidStorageHandler
argument_list|(
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
argument_list|,
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|segmentRootPath
init|=
name|temporaryFolder
operator|.
name|newFolder
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|druidStorageHandler
operator|.
name|setConf
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|LocalFileSystem
name|localFileSystem
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|Path
name|segmentOutputPath
init|=
name|JobHelper
operator|.
name|makeSegmentOutputPath
argument_list|(
operator|new
name|Path
argument_list|(
name|segmentRootPath
argument_list|)
argument_list|,
name|localFileSystem
argument_list|,
name|dataSegment
argument_list|)
decl_stmt|;
name|Path
name|indexPath
init|=
operator|new
name|Path
argument_list|(
name|segmentOutputPath
argument_list|,
literal|"index.zip"
argument_list|)
decl_stmt|;
name|DataSegment
name|dataSegmentWithLoadspect
init|=
name|DataSegment
operator|.
name|builder
argument_list|(
name|dataSegment
argument_list|)
operator|.
name|loadSpec
argument_list|(
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|of
argument_list|(
literal|"path"
argument_list|,
name|indexPath
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|OutputStream
name|outputStream
init|=
name|localFileSystem
operator|.
name|create
argument_list|(
name|indexPath
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|outputStream
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"index file is not created ??"
argument_list|,
name|localFileSystem
operator|.
name|exists
argument_list|(
name|indexPath
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|localFileSystem
operator|.
name|exists
argument_list|(
name|segmentOutputPath
argument_list|)
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|deleteSegment
argument_list|(
name|dataSegmentWithLoadspect
argument_list|)
expr_stmt|;
comment|// path format --> .../dataSource/interval/version/partitionNum/xxx.zip
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"Index file still there ??"
argument_list|,
name|localFileSystem
operator|.
name|exists
argument_list|(
name|indexPath
argument_list|)
argument_list|)
expr_stmt|;
comment|// path format of segmentOutputPath --> .../dataSource/interval/version/partitionNum/
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"PartitionNum directory still there ??"
argument_list|,
name|localFileSystem
operator|.
name|exists
argument_list|(
name|segmentOutputPath
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"Version directory still there ??"
argument_list|,
name|localFileSystem
operator|.
name|exists
argument_list|(
name|segmentOutputPath
operator|.
name|getParent
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"Interval directory still there ??"
argument_list|,
name|localFileSystem
operator|.
name|exists
argument_list|(
name|segmentOutputPath
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"Data source directory still there ??"
argument_list|,
name|localFileSystem
operator|.
name|exists
argument_list|(
name|segmentOutputPath
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCommitInsertOverwriteTable
parameter_list|()
throws|throws
name|MetaException
throws|,
name|IOException
block|{
name|DerbyConnectorTestUtility
name|connector
init|=
name|derbyConnectorRule
operator|.
name|getConnector
argument_list|()
decl_stmt|;
name|MetadataStorageTablesConfig
name|metadataStorageTablesConfig
init|=
name|derbyConnectorRule
operator|.
name|metadataTablesConfigSupplier
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|DruidStorageHandler
name|druidStorageHandler
init|=
operator|new
name|DruidStorageHandler
argument_list|(
name|connector
argument_list|,
name|metadataStorageTablesConfig
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|druidStorageHandler
operator|.
name|preCreateTable
argument_list|(
name|tableMock
argument_list|)
expr_stmt|;
name|Configuration
name|config
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|config
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|config
operator|.
name|set
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DRUID_WORKING_DIR
argument_list|)
argument_list|,
name|tableWorkingPath
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|setConf
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|LocalFileSystem
name|localFileSystem
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|config
argument_list|)
decl_stmt|;
name|Path
name|taskDirPath
init|=
operator|new
name|Path
argument_list|(
name|tableWorkingPath
argument_list|,
name|druidStorageHandler
operator|.
name|makeStagingName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|descriptorPath
init|=
name|DruidStorageHandlerUtils
operator|.
name|makeSegmentDescriptorOutputPath
argument_list|(
name|dataSegment
argument_list|,
operator|new
name|Path
argument_list|(
name|taskDirPath
argument_list|,
name|DruidStorageHandler
operator|.
name|SEGMENTS_DESCRIPTOR_DIR_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|DataSegment
argument_list|>
name|existingSegments
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|DataSegment
operator|.
name|builder
argument_list|()
operator|.
name|dataSource
argument_list|(
name|DATA_SOURCE_NAME
argument_list|)
operator|.
name|version
argument_list|(
literal|"v0"
argument_list|)
operator|.
name|interval
argument_list|(
operator|new
name|Interval
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
operator|.
name|shardSpec
argument_list|(
name|NoneShardSpec
operator|.
name|instance
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|DruidStorageHandlerUtils
operator|.
name|publishSegments
argument_list|(
name|connector
argument_list|,
name|metadataStorageTablesConfig
argument_list|,
name|DATA_SOURCE_NAME
argument_list|,
name|existingSegments
argument_list|,
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|DruidStorageHandlerUtils
operator|.
name|writeSegmentDescriptor
argument_list|(
name|localFileSystem
argument_list|,
name|dataSegment
argument_list|,
name|descriptorPath
argument_list|)
expr_stmt|;
name|druidStorageHandler
operator|.
name|commitInsertTable
argument_list|(
name|tableMock
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DATA_SOURCE_NAME
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|DruidStorageHandlerUtils
operator|.
name|getAllDataSourceNames
argument_list|(
name|connector
argument_list|,
name|metadataStorageTablesConfig
argument_list|)
argument_list|)
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|DataSegment
argument_list|>
name|dataSegmentList
init|=
name|connector
operator|.
name|getDBI
argument_list|()
operator|.
name|withHandle
argument_list|(
operator|new
name|HandleCallback
argument_list|<
name|List
argument_list|<
name|DataSegment
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|DataSegment
argument_list|>
name|withHandle
parameter_list|(
name|Handle
name|handle
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|handle
operator|.
name|createQuery
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"SELECT payload FROM %s WHERE used=true"
argument_list|,
name|metadataStorageTablesConfig
operator|.
name|getSegmentsTable
argument_list|()
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
operator|new
name|ResultSetMapper
argument_list|<
name|DataSegment
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DataSegment
name|map
parameter_list|(
name|int
name|i
parameter_list|,
name|ResultSet
name|resultSet
parameter_list|,
name|StatementContext
name|statementContext
parameter_list|)
throws|throws
name|SQLException
block|{
try|try
block|{
return|return
name|DruidStorageHandlerUtils
operator|.
name|JSON_MAPPER
operator|.
name|readValue
argument_list|(
name|resultSet
operator|.
name|getBytes
argument_list|(
literal|"payload"
argument_list|)
argument_list|,
name|DataSegment
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|)
operator|.
name|list
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|dataSegmentList
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


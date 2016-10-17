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
name|pig
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assume
operator|.
name|assumeTrue
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
name|Collection
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
name|HashSet
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
name|Set
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
name|io
operator|.
name|IOConstants
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
name|io
operator|.
name|StorageFormats
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
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHCatStorer
extends|extends
name|AbstractHCatStorerTest
block|{
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestHCatStorer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|allTests
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
block|{
name|add
argument_list|(
literal|"testBagNStruct"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testDateCharTypes"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testDynamicPartitioningMultiPartColsInDataNoSpec"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testDynamicPartitioningMultiPartColsInDataPartialSpec"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testDynamicPartitioningMultiPartColsNoDataInDataNoSpec"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testEmptyStore"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testMultiPartColsInData"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testNoAlias"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testPartColsInData"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testPartitionPublish"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreFuncAllSimpleTypes"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreFuncSimple"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreInPartiitonedTbl"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreMultiTables"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreWithNoCtorArgs"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreWithNoSchema"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteChar"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteDate"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteDate2"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteDate3"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteDecimal"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteDecimalX"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteDecimalXY"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteSmallint"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteTimestamp"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteTinyint"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testWriteVarchar"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|/**    * We're disabling these tests as they're going to be run from their individual    * Test<FileFormat>HCatStorer classes. However, we're still leaving this test in case new file    * formats in future are added.    */
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|DISABLED_STORAGE_FORMATS
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
block|{
name|put
parameter_list|(
name|IOConstants
operator|.
name|AVRO
parameter_list|,
name|allTests
parameter_list|)
constructor_decl|;
name|put
parameter_list|(
name|IOConstants
operator|.
name|ORCFILE
parameter_list|,
name|allTests
parameter_list|)
constructor_decl|;
name|put
parameter_list|(
name|IOConstants
operator|.
name|PARQUETFILE
parameter_list|,
name|allTests
parameter_list|)
constructor_decl|;
name|put
parameter_list|(
name|IOConstants
operator|.
name|RCFILE
parameter_list|,
name|allTests
parameter_list|)
constructor_decl|;
name|put
parameter_list|(
name|IOConstants
operator|.
name|SEQUENCEFILE
parameter_list|,
name|allTests
parameter_list|)
constructor_decl|;
name|put
parameter_list|(
name|IOConstants
operator|.
name|TEXTFILE
parameter_list|,
name|allTests
parameter_list|)
constructor_decl|;
block|}
block|}
decl_stmt|;
specifier|private
name|String
name|storageFormat
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|generateParameters
parameter_list|()
block|{
return|return
name|StorageFormats
operator|.
name|names
argument_list|()
return|;
block|}
specifier|public
name|TestHCatStorer
parameter_list|(
name|String
name|storageFormat
parameter_list|)
block|{
name|this
operator|.
name|storageFormat
operator|=
name|storageFormat
expr_stmt|;
block|}
annotation|@
name|Override
name|String
name|getStorageFormat
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteTinyint
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteTinyint
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteSmallint
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteSmallint
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteChar
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteChar
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteVarchar
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteVarchar
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteDecimalXY
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteDecimalXY
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteDecimalX
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteDecimalX
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteDecimal
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteDecimal
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteDate
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteDate
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteDate3
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteDate3
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteDate2
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteDate2
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWriteTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testWriteTimestamp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testDateCharTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testDateCharTypes
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testPartColsInData
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testPartColsInData
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testMultiPartColsInData
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testMultiPartColsInData
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testStoreInPartiitonedTbl
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testStoreInPartiitonedTbl
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testNoAlias
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testNoAlias
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testStoreMultiTables
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testStoreMultiTables
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testStoreWithNoSchema
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testStoreWithNoSchema
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testStoreWithNoCtorArgs
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testStoreWithNoCtorArgs
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testEmptyStore
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testEmptyStore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testBagNStruct
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testBagNStruct
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testStoreFuncAllSimpleTypes
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testStoreFuncAllSimpleTypes
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testStoreFuncSimple
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testStoreFuncSimple
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testDynamicPartitioningMultiPartColsInDataPartialSpec
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testDynamicPartitioningMultiPartColsInDataPartialSpec
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testDynamicPartitioningMultiPartColsInDataNoSpec
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testDynamicPartitioningMultiPartColsInDataNoSpec
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testDynamicPartitioningMultiPartColsNoDataInDataNoSpec
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testDynamicPartitioningMultiPartColsNoDataInDataNoSpec
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testPartitionPublish
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|testPartitionPublish
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


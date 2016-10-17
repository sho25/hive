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
name|junit
operator|.
name|Ignore
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
specifier|public
class|class
name|TestAvroHCatStorer
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
name|TestAvroHCatStorer
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
name|String
name|getStorageFormat
parameter_list|()
block|{
return|return
name|IOConstants
operator|.
name|AVRO
return|;
block|}
annotation|@
name|Test
annotation|@
name|Override
annotation|@
name|Ignore
argument_list|(
literal|"Temporarily disable until fixed"
argument_list|)
comment|// incorrect precision: expected:<0 xxxxx yyy 5.2[]> but was:<0 xxxxx yyy 5.2[0]>
specifier|public
name|void
name|testDateCharTypes
parameter_list|()
throws|throws
name|Exception
block|{
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
annotation|@
name|Ignore
argument_list|(
literal|"Temporarily disable until fixed"
argument_list|)
comment|// incorrect precision: expected:<1.2[]> but was:<1.2[0]>
specifier|public
name|void
name|testWriteDecimalXY
parameter_list|()
throws|throws
name|Exception
block|{
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
annotation|@
name|Ignore
argument_list|(
literal|"Temporarily disable until fixed"
argument_list|)
comment|// doesn't have a notion of small, and saves the full value as an int, so no overflow
comment|// expected:<null> but was:<32768>
specifier|public
name|void
name|testWriteSmallint
parameter_list|()
throws|throws
name|Exception
block|{
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
annotation|@
name|Ignore
argument_list|(
literal|"Temporarily disable until fixed"
argument_list|)
comment|// does not support timestamp
comment|// TypeInfoToSchema.createAvroPrimitive : UnsupportedOperationException
specifier|public
name|void
name|testWriteTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
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
annotation|@
name|Ignore
argument_list|(
literal|"Temporarily disable until fixed"
argument_list|)
comment|// doesn't have a notion of tiny, and saves the full value as an int, so no overflow
comment|// expected:<null> but was:<300>
specifier|public
name|void
name|testWriteTinyint
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testWriteTinyint
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


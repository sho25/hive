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
name|hive
operator|.
name|service
operator|.
name|auth
operator|.
name|ldap
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
name|javax
operator|.
name|naming
operator|.
name|NamingException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
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
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
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
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestChainFilter
block|{
specifier|private
name|FilterFactory
name|factory
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
annotation|@
name|Mock
specifier|public
name|Filter
name|filter1
decl_stmt|;
annotation|@
name|Mock
specifier|public
name|Filter
name|filter2
decl_stmt|;
annotation|@
name|Mock
specifier|public
name|Filter
name|filter3
decl_stmt|;
annotation|@
name|Mock
specifier|public
name|FilterFactory
name|factory1
decl_stmt|;
annotation|@
name|Mock
specifier|public
name|FilterFactory
name|factory2
decl_stmt|;
annotation|@
name|Mock
specifier|public
name|FilterFactory
name|factory3
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|DirSearch
name|search
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|conf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|factory
operator|=
operator|new
name|ChainFilterFactory
argument_list|(
name|factory1
argument_list|,
name|factory2
argument_list|,
name|factory3
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFactoryAllNull
parameter_list|()
block|{
name|assertNull
argument_list|(
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFactoryAllEmpty
parameter_list|()
block|{
name|FilterFactory
name|emptyFactory
init|=
operator|new
name|ChainFilterFactory
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|emptyFactory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFactory
parameter_list|()
throws|throws
name|AuthenticationException
block|{
name|when
argument_list|(
name|factory1
operator|.
name|getInstance
argument_list|(
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|filter1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|factory2
operator|.
name|getInstance
argument_list|(
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|filter2
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|factory3
operator|.
name|getInstance
argument_list|(
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|filter3
argument_list|)
expr_stmt|;
name|Filter
name|filter
init|=
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"User"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|filter1
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"User"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|filter2
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"User"
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|filter3
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"User"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|AuthenticationException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testApplyNegative
parameter_list|()
throws|throws
name|AuthenticationException
throws|,
name|NamingException
throws|,
name|IOException
block|{
name|doThrow
argument_list|(
name|AuthenticationException
operator|.
name|class
argument_list|)
operator|.
name|when
argument_list|(
name|filter3
argument_list|)
operator|.
name|apply
argument_list|(
operator|(
name|DirSearch
operator|)
name|anyObject
argument_list|()
argument_list|,
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|factory1
operator|.
name|getInstance
argument_list|(
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|filter1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|factory3
operator|.
name|getInstance
argument_list|(
name|any
argument_list|(
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|filter3
argument_list|)
expr_stmt|;
name|Filter
name|filter
init|=
name|factory
operator|.
name|getInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
name|search
argument_list|,
literal|"User"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


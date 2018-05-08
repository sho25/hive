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
name|hadoop
operator|.
name|hive
operator|.
name|shims
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
name|junit
operator|.
name|Test
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_class
specifier|public
class|class
name|TestHadoop23Shims
block|{
annotation|@
name|Test
specifier|public
name|void
name|testConstructDistCpParams
parameter_list|()
block|{
name|Path
name|copySrc
init|=
operator|new
name|Path
argument_list|(
literal|"copySrc"
argument_list|)
decl_stmt|;
name|Path
name|copyDst
init|=
operator|new
name|Path
argument_list|(
literal|"copyDst"
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Hadoop23Shims
name|shims
init|=
operator|new
name|Hadoop23Shims
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|paramsDefault
init|=
name|shims
operator|.
name|constructDistCpParams
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|copySrc
argument_list|)
argument_list|,
name|copyDst
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|paramsDefault
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Distcp -update set by default"
argument_list|,
name|paramsDefault
operator|.
name|contains
argument_list|(
literal|"-update"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Distcp -pbx set by default"
argument_list|,
name|paramsDefault
operator|.
name|contains
argument_list|(
literal|"-pbx"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|copySrc
operator|.
name|toString
argument_list|()
argument_list|,
name|paramsDefault
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|copyDst
operator|.
name|toString
argument_list|()
argument_list|,
name|paramsDefault
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"distcp.options.foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
comment|// should set "-foo bar"
name|conf
operator|.
name|set
argument_list|(
literal|"distcp.options.blah"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
comment|// should set "-blah"
name|conf
operator|.
name|set
argument_list|(
literal|"dummy"
argument_list|,
literal|"option"
argument_list|)
expr_stmt|;
comment|// should be ignored.
name|List
argument_list|<
name|String
argument_list|>
name|paramsWithCustomParamInjection
init|=
name|shims
operator|.
name|constructDistCpParams
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|copySrc
argument_list|)
argument_list|,
name|copyDst
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// check that the defaults did not remain.
name|assertTrue
argument_list|(
literal|"Distcp -update not set if not requested"
argument_list|,
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"-update"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Distcp -skipcrccheck not set if not requested"
argument_list|,
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"-skipcrccheck"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Distcp -pbx not set if not requested"
argument_list|,
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"-pbx"
argument_list|)
argument_list|)
expr_stmt|;
comment|// the "-foo bar" and "-blah" params order is not guaranteed
name|String
name|firstParam
init|=
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstParam
operator|.
name|equals
argument_list|(
literal|"-foo"
argument_list|)
condition|)
block|{
comment|// "-foo bar -blah"  form
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-blah"
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// "-blah -foo bar" form
name|assertEquals
argument_list|(
literal|"-blah"
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"-foo"
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// the dummy option should not have made it either - only options
comment|// beginning with distcp.options. should be honoured
name|assertTrue
argument_list|(
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"dummy"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"-dummy"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"option"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|paramsWithCustomParamInjection
operator|.
name|contains
argument_list|(
literal|"-option"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|copySrc
operator|.
name|toString
argument_list|()
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|copyDst
operator|.
name|toString
argument_list|()
argument_list|,
name|paramsWithCustomParamInjection
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


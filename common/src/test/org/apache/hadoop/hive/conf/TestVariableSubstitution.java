begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|conf
package|;
end_package

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
name|Test
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

begin_class
specifier|public
class|class
name|TestVariableSubstitution
block|{
specifier|private
specifier|static
class|class
name|LocalMySource
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|put
parameter_list|(
name|String
name|k
parameter_list|,
name|String
name|v
parameter_list|)
block|{
name|map
operator|.
name|put
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|get
parameter_list|(
name|String
name|k
parameter_list|)
block|{
return|return
name|map
operator|.
name|get
argument_list|(
name|k
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|LocalMySource
name|getMySource
parameter_list|()
block|{
return|return
name|localSource
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|LocalMySource
argument_list|>
name|localSource
init|=
operator|new
name|ThreadLocal
argument_list|<
name|LocalMySource
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|LocalMySource
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|LocalMySource
argument_list|()
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testVariableSource
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|VariableSubstitution
name|variableSubstitution
init|=
operator|new
name|VariableSubstitution
argument_list|(
operator|new
name|HiveVariableSource
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVariable
parameter_list|()
block|{
return|return
name|TestVariableSubstitution
operator|.
name|getMySource
argument_list|()
operator|.
name|map
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|String
name|v
init|=
name|variableSubstitution
operator|.
name|substitute
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|,
literal|"${a}"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"${a}"
argument_list|,
name|v
argument_list|)
expr_stmt|;
name|TestVariableSubstitution
operator|.
name|getMySource
argument_list|()
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|v
operator|=
name|variableSubstitution
operator|.
name|substitute
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|,
literal|"${a}"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|v
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


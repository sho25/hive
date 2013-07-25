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

begin_class
specifier|public
class|class
name|UnitTestBatch
implements|implements
name|TestBatch
block|{
specifier|private
specifier|final
name|String
name|testName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isParallel
decl_stmt|;
specifier|public
name|UnitTestBatch
parameter_list|(
name|String
name|testName
parameter_list|,
name|boolean
name|isParallel
parameter_list|)
block|{
name|this
operator|.
name|testName
operator|=
name|testName
expr_stmt|;
name|this
operator|.
name|isParallel
operator|=
name|isParallel
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTestArguments
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"-Dtestcase=%s"
argument_list|,
name|testName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|testName
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"UnitTestBatch [testName="
operator|+
name|testName
operator|+
literal|", isParallel="
operator|+
name|isParallel
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isParallel
parameter_list|()
block|{
return|return
name|isParallel
return|;
block|}
block|}
end_class

end_unit


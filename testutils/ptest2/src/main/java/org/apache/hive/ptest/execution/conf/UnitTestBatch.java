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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|base
operator|.
name|Joiner
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|UnitTestBatch
extends|extends
name|TestBatch
block|{
specifier|private
specifier|final
name|String
name|testCasePropertyName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|testList
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isParallel
decl_stmt|;
specifier|private
specifier|final
name|String
name|moduleName
decl_stmt|;
specifier|private
specifier|final
name|String
name|batchName
decl_stmt|;
specifier|public
name|UnitTestBatch
parameter_list|(
name|AtomicInteger
name|batchIdCounter
parameter_list|,
name|String
name|testCasePropertyName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tests
parameter_list|,
name|String
name|moduleName
parameter_list|,
name|boolean
name|isParallel
parameter_list|)
block|{
name|super
argument_list|(
name|batchIdCounter
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|testCasePropertyName
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|tests
operator|!=
literal|null
operator|&&
operator|!
name|tests
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|testCasePropertyName
operator|=
name|testCasePropertyName
expr_stmt|;
name|this
operator|.
name|testList
operator|=
name|tests
expr_stmt|;
name|this
operator|.
name|isParallel
operator|=
name|isParallel
expr_stmt|;
name|this
operator|.
name|moduleName
operator|=
name|moduleName
expr_stmt|;
if|if
condition|(
name|tests
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|batchName
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"%d_%s"
argument_list|,
name|getBatchId
argument_list|()
argument_list|,
name|tests
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|batchName
operator|=
name|String
operator|.
name|format
argument_list|(
literal|"%d_UTBatch_%s_%d_tests"
argument_list|,
name|getBatchId
argument_list|()
argument_list|,
operator|(
name|moduleName
operator|.
name|replace
argument_list|(
literal|"/"
argument_list|,
literal|"__"
argument_list|)
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"__"
argument_list|)
operator|)
argument_list|,
name|tests
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTestArguments
parameter_list|()
block|{
name|String
name|testArg
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|join
argument_list|(
name|testList
argument_list|)
decl_stmt|;
return|return
name|String
operator|.
name|format
argument_list|(
literal|"-D%s=%s"
argument_list|,
name|testCasePropertyName
argument_list|,
name|testArg
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
comment|// Used for logDir, failure messages etc.
return|return
name|batchName
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
literal|"UnitTestBatch [name="
operator|+
name|batchName
operator|+
literal|", id="
operator|+
name|getBatchId
argument_list|()
operator|+
literal|", moduleName="
operator|+
name|moduleName
operator|+
literal|", batchSize="
operator|+
name|testList
operator|.
name|size
argument_list|()
operator|+
literal|", isParallel="
operator|+
name|isParallel
operator|+
literal|", testList="
operator|+
name|testList
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
annotation|@
name|Override
specifier|public
name|String
name|getTestModuleRelativeDir
parameter_list|()
block|{
return|return
name|moduleName
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getNumTestsInBatch
parameter_list|()
block|{
return|return
name|testList
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getTestClasses
parameter_list|()
block|{
return|return
name|testList
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|UnitTestBatch
name|that
init|=
operator|(
name|UnitTestBatch
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|isParallel
operator|!=
name|that
operator|.
name|isParallel
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|testList
operator|!=
literal|null
condition|?
operator|!
name|testList
operator|.
name|equals
argument_list|(
name|that
operator|.
name|testList
argument_list|)
else|:
name|that
operator|.
name|testList
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|moduleName
operator|!=
literal|null
condition|?
operator|!
name|moduleName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|moduleName
argument_list|)
else|:
name|that
operator|.
name|moduleName
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|batchName
operator|!=
literal|null
condition|?
name|batchName
operator|.
name|equals
argument_list|(
name|that
operator|.
name|batchName
argument_list|)
else|:
name|that
operator|.
name|batchName
operator|==
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|testList
operator|!=
literal|null
condition|?
name|testList
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|isParallel
condition|?
literal|1
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|moduleName
operator|!=
literal|null
condition|?
name|moduleName
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|batchName
operator|!=
literal|null
condition|?
name|batchName
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit


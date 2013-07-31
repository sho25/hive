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
package|;
end_package

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
name|org
operator|.
name|slf4j
operator|.
name|Logger
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

begin_comment
comment|/**  * Creates a tar.gz of the TEST-*.xml test results  */
end_comment

begin_class
specifier|public
class|class
name|ReportingPhase
extends|extends
name|Phase
block|{
specifier|public
name|ReportingPhase
parameter_list|(
name|List
argument_list|<
name|HostExecutor
argument_list|>
name|hostExecutors
parameter_list|,
name|LocalCommandFactory
name|localCommandFactory
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateDefaults
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|hostExecutors
argument_list|,
name|localCommandFactory
argument_list|,
name|templateDefaults
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
name|execLocally
argument_list|(
literal|"mkdir $logDir/test-results"
argument_list|)
expr_stmt|;
name|execLocally
argument_list|(
literal|"find $logDir/{failed,succeeded} -maxdepth 2 -name 'TEST*.xml' -exec cp {} $logDir/test-results \\; 2>/dev/null"
argument_list|)
expr_stmt|;
name|execLocally
argument_list|(
literal|"cd $logDir/&& tar -zvcf test-results.tar.gz test-results/"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


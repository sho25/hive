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
name|ql
operator|.
name|txn
operator|.
name|compactor
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
name|Table
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
name|txn
operator|.
name|CompactionInfo
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
name|AcidUtils
import|;
end_import

begin_comment
comment|/**  * Simple factory class, which returns an instance of {@link QueryCompactor}.  */
end_comment

begin_class
specifier|final
class|class
name|QueryCompactorFactory
block|{
comment|/**    * Factory class, no need to expose constructor.    */
specifier|private
name|QueryCompactorFactory
parameter_list|()
block|{   }
comment|/**    * Get an instance of {@link QueryCompactor}. At the moment the following implementors can be fetched:    *<p>    * {@link MajorQueryCompactor} - handles query based major compaction    *<br>    * {@link MinorQueryCompactor} - handles query based minor compaction    *<br>    * {@link MmMajorQueryCompactor} - handles query based minor compaction for micro-managed tables    *<br>    *</p>    * @param table the table, on which the compaction should be running, must be not null.    * @param configuration the hive configuration, must be not null.    * @param compactionInfo provides insight about the type of compaction, must be not null.    * @return {@link QueryCompactor} or null.    */
specifier|static
name|QueryCompactor
name|getQueryCompactor
parameter_list|(
name|Table
name|table
parameter_list|,
name|HiveConf
name|configuration
parameter_list|,
name|CompactionInfo
name|compactionInfo
parameter_list|)
block|{
if|if
condition|(
operator|!
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|COMPACTOR_CRUD_QUERY_BASED
argument_list|)
condition|)
block|{
if|if
condition|(
name|compactionInfo
operator|.
name|isMajorCompaction
argument_list|()
condition|)
block|{
return|return
operator|new
name|MajorQueryCompactor
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|compactionInfo
operator|.
name|isMajorCompaction
argument_list|()
operator|&&
literal|"tez"
operator|.
name|equalsIgnoreCase
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
argument_list|)
condition|)
block|{
comment|// query based minor compaction is only supported on tez
return|return
operator|new
name|MinorQueryCompactor
argument_list|()
return|;
block|}
block|}
if|if
condition|(
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|configuration
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPACTOR_COMPACT_MM
argument_list|)
condition|)
block|{
return|return
operator|new
name|MmMajorQueryCompactor
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


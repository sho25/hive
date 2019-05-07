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
name|ddl
operator|.
name|process
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|metastore
operator|.
name|api
operator|.
name|ShowCompactResponse
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
name|ShowCompactResponseElement
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
name|ddl
operator|.
name|DDLOperation
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
name|ddl
operator|.
name|DDLOperationContext
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
name|ddl
operator|.
name|DDLUtils
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
name|exec
operator|.
name|Utilities
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Operation process of showing compactions.  */
end_comment

begin_class
specifier|public
class|class
name|ShowCompactionsOperation
extends|extends
name|DDLOperation
block|{
specifier|private
specifier|final
name|ShowCompactionsDesc
name|desc
decl_stmt|;
specifier|public
name|ShowCompactionsOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|ShowCompactionsDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// Call the metastore to get the status of all known compactions (completed get purged eventually)
name|ShowCompactResponse
name|rsp
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|showCompactions
argument_list|()
decl_stmt|;
comment|// Write the results into the file
try|try
init|(
name|DataOutputStream
name|os
init|=
name|DDLUtils
operator|.
name|getOutputStream
argument_list|(
operator|new
name|Path
argument_list|(
name|desc
operator|.
name|getResFile
argument_list|()
argument_list|)
argument_list|,
name|context
argument_list|)
init|)
block|{
comment|// Write a header
name|writeHeader
argument_list|(
name|os
argument_list|)
expr_stmt|;
if|if
condition|(
name|rsp
operator|.
name|getCompacts
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ShowCompactResponseElement
name|e
range|:
name|rsp
operator|.
name|getCompacts
argument_list|()
control|)
block|{
name|writeRow
argument_list|(
name|os
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"show compactions: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|writeHeader
parameter_list|(
name|DataOutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
name|os
operator|.
name|writeBytes
argument_list|(
literal|"CompactionId"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Database"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Table"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Partition"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Type"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"State"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Hostname"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Worker"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Start Time"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"Duration(ms)"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
literal|"HadoopJobId"
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|NO_VAL
init|=
literal|" --- "
decl_stmt|;
specifier|private
name|void
name|writeRow
parameter_list|(
name|DataOutputStream
name|os
parameter_list|,
name|ShowCompactResponseElement
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|os
operator|.
name|writeBytes
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|getDbname
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|String
name|part
init|=
name|e
operator|.
name|getPartitionname
argument_list|()
decl_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|part
operator|==
literal|null
condition|?
name|NO_VAL
else|:
name|part
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|getType
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|String
name|wid
init|=
name|e
operator|.
name|getWorkerid
argument_list|()
decl_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|wid
operator|==
literal|null
condition|?
name|NO_VAL
else|:
name|wid
operator|.
name|split
argument_list|(
literal|"-"
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|wid
operator|==
literal|null
condition|?
name|NO_VAL
else|:
name|wid
operator|.
name|split
argument_list|(
literal|"-"
argument_list|)
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|isSetStart
argument_list|()
condition|?
name|Long
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getStart
argument_list|()
argument_list|)
else|:
name|NO_VAL
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|isSetEndTime
argument_list|()
condition|?
name|Long
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getEndTime
argument_list|()
operator|-
name|e
operator|.
name|getStart
argument_list|()
argument_list|)
else|:
name|NO_VAL
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|tabCode
argument_list|)
expr_stmt|;
name|os
operator|.
name|writeBytes
argument_list|(
name|e
operator|.
name|isSetHadoopJobId
argument_list|()
condition|?
name|e
operator|.
name|getHadoopJobId
argument_list|()
else|:
name|NO_VAL
argument_list|)
expr_stmt|;
name|os
operator|.
name|write
argument_list|(
name|Utilities
operator|.
name|newLineCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Flushable
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

begin_comment
comment|/**  * Interface for submitting mutation events to a given partition and bucket in an ACID table. Requires records to arrive  * in the order defined by the {@link SequenceValidator}.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Mutator
extends|extends
name|Closeable
extends|,
name|Flushable
block|{
name|void
name|insert
parameter_list|(
name|Object
name|record
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|update
parameter_list|(
name|Object
name|record
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|delete
parameter_list|(
name|Object
name|record
parameter_list|)
throws|throws
name|IOException
function_decl|;
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit


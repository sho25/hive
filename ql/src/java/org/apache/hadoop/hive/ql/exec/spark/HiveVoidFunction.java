begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|exec
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|function
operator|.
name|VoidFunction
import|;
end_import

begin_comment
comment|/**  * Implementation of a voidFunction that does nothing.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveVoidFunction
implements|implements
name|VoidFunction
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
name|HiveVoidFunction
name|instance
init|=
operator|new
name|HiveVoidFunction
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|HiveVoidFunction
name|getInstance
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
specifier|private
name|HiveVoidFunction
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|Object
name|arg0
parameter_list|)
throws|throws
name|Exception
block|{   }
block|}
end_class

end_unit


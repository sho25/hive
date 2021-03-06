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
name|udf
operator|.
name|generic
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
name|ql
operator|.
name|exec
operator|.
name|Description
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
comment|// this function is for internal use only
end_comment

begin_class
specifier|public
class|class
name|GenericUDFOPNotEqualNS
extends|extends
name|GenericUDFOPNotEqual
block|{
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"IS DISTINCT FROM"
argument_list|,
name|value
operator|=
literal|"a _FUNC_ b - Returns same result with NOTEQUALNS (IS DISTINCT "
operator|+
literal|"FROM) operator for non-null operands, but returns FALSE if both are NULL, TRUE if one of the them is NULL"
argument_list|)
specifier|public
name|GenericUDFOPNotEqualNS
parameter_list|()
block|{
name|this
operator|.
name|opName
operator|=
literal|"NOTEQUALNS"
expr_stmt|;
name|this
operator|.
name|opDisplayName
operator|=
literal|"IS DISTINCT FROM"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
name|o0
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|Object
name|o1
init|=
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|o0
operator|==
literal|null
operator|&&
name|o1
operator|==
literal|null
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
if|if
condition|(
name|o0
operator|==
literal|null
operator|||
name|o1
operator|==
literal|null
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
return|return
name|super
operator|.
name|evaluate
argument_list|(
name|arguments
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDF
name|negative
parameter_list|()
block|{
return|return
operator|new
name|GenericUDFOPEqualNS
argument_list|()
return|;
block|}
block|}
end_class

end_unit


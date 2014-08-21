begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
operator|.
name|columns
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveColumn
block|{
comment|// The name of this column in the Hive schema
specifier|protected
specifier|final
name|String
name|columnName
decl_stmt|;
comment|// The Hive type of this column
specifier|protected
specifier|final
name|TypeInfo
name|columnType
decl_stmt|;
specifier|public
name|HiveColumn
parameter_list|(
name|String
name|columnName
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|columnType
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
block|}
comment|/**    * Get the name of the Hive column    */
specifier|public
name|String
name|getColumnName
parameter_list|()
block|{
return|return
name|columnName
return|;
block|}
comment|/**    * The Hive type of this column    */
specifier|public
name|TypeInfo
name|getColumnType
parameter_list|()
block|{
return|return
name|columnType
return|;
block|}
block|}
end_class

end_unit


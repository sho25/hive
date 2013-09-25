begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|templeton
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|annotation
operator|.
name|XmlRootElement
import|;
end_import

begin_comment
comment|/**  * A description of the table to create that's like another table.  */
end_comment

begin_class
annotation|@
name|XmlRootElement
specifier|public
class|class
name|TableLikeDesc
extends|extends
name|GroupPermissionsDesc
block|{
specifier|public
name|boolean
name|external
init|=
literal|false
decl_stmt|;
specifier|public
name|boolean
name|ifNotExists
init|=
literal|false
decl_stmt|;
specifier|public
name|String
name|location
decl_stmt|;
specifier|public
name|String
name|existingTable
decl_stmt|;
specifier|public
name|String
name|newTable
decl_stmt|;
specifier|public
name|TableLikeDesc
parameter_list|()
block|{   }
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"TableLikeDesc(existingTable=%s, newTable=%s, location=%s"
argument_list|,
name|existingTable
argument_list|,
name|newTable
argument_list|,
name|location
argument_list|)
return|;
block|}
block|}
end_class

end_unit


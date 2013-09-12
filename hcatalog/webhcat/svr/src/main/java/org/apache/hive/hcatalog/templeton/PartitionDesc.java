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
comment|/**  * A description of the partition to create.  */
end_comment

begin_class
annotation|@
name|XmlRootElement
specifier|public
class|class
name|PartitionDesc
extends|extends
name|GroupPermissionsDesc
block|{
specifier|public
name|String
name|partition
decl_stmt|;
specifier|public
name|String
name|location
decl_stmt|;
specifier|public
name|boolean
name|ifNotExists
init|=
literal|false
decl_stmt|;
specifier|public
name|PartitionDesc
parameter_list|()
block|{}
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
literal|"PartitionDesc(partition=%s, location=%s, ifNotExists=%s)"
argument_list|,
name|partition
argument_list|,
name|location
argument_list|,
name|ifNotExists
argument_list|)
return|;
block|}
block|}
end_class

end_unit


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
name|druid
operator|.
name|json
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonSubTypes
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|annotation
operator|.
name|JsonTypeInfo
import|;
end_import

begin_comment
comment|/**  * This class is copied from druid source code  * in order to avoid adding additional dependencies on druid-indexing-service.  */
end_comment

begin_interface
annotation|@
name|JsonTypeInfo
argument_list|(
name|use
operator|=
name|JsonTypeInfo
operator|.
name|Id
operator|.
name|NAME
argument_list|,
name|property
operator|=
literal|"type"
argument_list|)
annotation|@
name|JsonSubTypes
argument_list|(
name|value
operator|=
block|{
annotation|@
name|JsonSubTypes
operator|.
name|Type
argument_list|(
name|name
operator|=
literal|"schema_inline"
argument_list|,
name|value
operator|=
name|InlineSchemaAvroBytesDecoder
operator|.
name|class
argument_list|)
block|}
argument_list|)
specifier|public
interface|interface
name|AvroBytesDecoder
block|{ }
end_interface

end_unit


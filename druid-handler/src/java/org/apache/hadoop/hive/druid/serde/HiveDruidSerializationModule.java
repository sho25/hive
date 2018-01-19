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
name|serde
package|;
end_package

begin_import
import|import
name|io
operator|.
name|druid
operator|.
name|java
operator|.
name|util
operator|.
name|common
operator|.
name|granularity
operator|.
name|PeriodGranularity
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
name|core
operator|.
name|util
operator|.
name|VersionUtil
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
name|databind
operator|.
name|module
operator|.
name|SimpleModule
import|;
end_import

begin_comment
comment|/**  * This class is used to define/override any serde behavior for classes from druid.  * Currently it is used to override the default behavior when serializing PeriodGranularity to include user timezone.  */
end_comment

begin_class
specifier|public
class|class
name|HiveDruidSerializationModule
extends|extends
name|SimpleModule
block|{
specifier|private
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"HiveDruidSerializationModule"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|VersionUtil
name|VERSION_UTIL
init|=
operator|new
name|VersionUtil
argument_list|()
block|{}
decl_stmt|;
specifier|public
name|HiveDruidSerializationModule
parameter_list|()
block|{
name|super
argument_list|(
name|NAME
argument_list|,
name|VERSION_UTIL
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|addSerializer
argument_list|(
name|PeriodGranularity
operator|.
name|class
argument_list|,
operator|new
name|PeriodGranularitySerializer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


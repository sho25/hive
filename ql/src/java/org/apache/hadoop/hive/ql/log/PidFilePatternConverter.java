begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|log
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|plugins
operator|.
name|PluginFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|pattern
operator|.
name|AbstractPatternConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|pattern
operator|.
name|ArrayPatternConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|pattern
operator|.
name|ConverterKeys
import|;
end_import

begin_comment
comment|/**  * FilePattern converter that converts %pid pattern to<process-id>@<hostname> information  * obtained at runtime.  *  * Example usage:  *<RollingFile name="Rolling-default" fileName="test.log" filePattern="test.log.%pid.gz">  *  * Will generate output file with name containing<process-id>@<hostname> like below  * test.log.95232@localhost.gz  */
end_comment

begin_class
annotation|@
name|Plugin
argument_list|(
name|name
operator|=
literal|"PidFilePatternConverter"
argument_list|,
name|category
operator|=
literal|"FileConverter"
argument_list|)
annotation|@
name|ConverterKeys
argument_list|(
block|{
literal|"pid"
block|}
argument_list|)
specifier|public
class|class
name|PidFilePatternConverter
extends|extends
name|AbstractPatternConverter
implements|implements
name|ArrayPatternConverter
block|{
comment|/**    * Private constructor.    */
specifier|private
name|PidFilePatternConverter
parameter_list|()
block|{
name|super
argument_list|(
literal|"pid"
argument_list|,
literal|"pid"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|PluginFactory
specifier|public
specifier|static
name|PidFilePatternConverter
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|PidFilePatternConverter
argument_list|()
return|;
block|}
specifier|public
name|void
name|format
parameter_list|(
name|StringBuilder
name|toAppendTo
parameter_list|,
name|Object
modifier|...
name|objects
parameter_list|)
block|{
name|toAppendTo
operator|.
name|append
argument_list|(
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|format
parameter_list|(
name|Object
name|obj
parameter_list|,
name|StringBuilder
name|toAppendTo
parameter_list|)
block|{
name|toAppendTo
operator|.
name|append
argument_list|(
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


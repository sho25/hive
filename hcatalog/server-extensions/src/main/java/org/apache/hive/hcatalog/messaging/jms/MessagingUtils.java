begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|messaging
operator|.
name|jms
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|HCatEventMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|messaging
operator|.
name|MessageFactory
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|JMSException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|jms
operator|.
name|TextMessage
import|;
end_import

begin_comment
comment|/**  * Helper Utility to assist consumers of HCat Messages in extracting  * message-content from JMS messages.  */
end_comment

begin_class
specifier|public
class|class
name|MessagingUtils
block|{
comment|/**    * Method to return HCatEventMessage contained in the JMS message.    * @param message The JMS Message instance    * @return The contained HCatEventMessage    */
specifier|public
specifier|static
name|HCatEventMessage
name|getMessage
parameter_list|(
name|Message
name|message
parameter_list|)
block|{
try|try
block|{
name|String
name|messageBody
init|=
operator|(
operator|(
name|TextMessage
operator|)
name|message
operator|)
operator|.
name|getText
argument_list|()
decl_stmt|;
name|String
name|eventType
init|=
name|message
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_EVENT
argument_list|)
decl_stmt|;
name|String
name|messageVersion
init|=
name|message
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_MESSAGE_VERSION
argument_list|)
decl_stmt|;
name|String
name|messageFormat
init|=
name|message
operator|.
name|getStringProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_MESSAGE_FORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|messageBody
argument_list|)
operator|||
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|eventType
argument_list|)
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not extract HCatEventMessage. "
operator|+
literal|"EventType and/or MessageBody is null/empty."
argument_list|)
throw|;
return|return
name|MessageFactory
operator|.
name|getDeserializer
argument_list|(
name|messageFormat
argument_list|,
name|messageVersion
argument_list|)
operator|.
name|getHCatEventMessage
argument_list|(
name|eventType
argument_list|,
name|messageBody
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|JMSException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not extract HCatEventMessage. "
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
comment|// Prevent construction.
specifier|private
name|MessagingUtils
parameter_list|()
block|{}
block|}
end_class

end_unit


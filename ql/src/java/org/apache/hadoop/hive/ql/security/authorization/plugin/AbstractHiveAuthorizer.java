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
name|security
operator|.
name|authorization
operator|.
name|plugin
package|;
end_package

begin_comment
comment|/**  * Abstract class that extends HiveAuthorizer. This will help to shield  * Hive authorization implementations from some of the changes to HiveAuthorizer  * interface by providing default implementation of new methods in HiveAuthorizer  * when possible.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractHiveAuthorizer
implements|implements
name|HiveAuthorizer
block|{
comment|/* (non-Javadoc)    * @see org.apache.hadoop.hive.ql.security.authorization.plugin.HiveAuthorizer#getHiveAuthorizationTranslator()    */
annotation|@
name|Override
specifier|public
name|HiveAuthorizationTranslator
name|getHiveAuthorizationTranslator
parameter_list|()
throws|throws
name|HiveAuthzPluginException
block|{
comment|// No customization of this API is done for most Authorization implementations. It is meant
comment|// to be used for special cases in Apache Sentry (incubating)
comment|// null is to be returned when no customization is needed for the translator
comment|// see javadoc in interface for details.
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


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
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|conf
operator|.
name|Configuration
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|AuthorizationException
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
name|Partition
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
name|Table
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
name|security
operator|.
name|HiveAuthenticationProvider
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
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
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
name|security
operator|.
name|authorization
operator|.
name|Privilege
import|;
end_import

begin_comment
comment|/**  * This class is an implementation of HiveAuthorizationProvider to provide  * authorization functionality for HBase tables.  */
end_comment

begin_class
class|class
name|HBaseAuthorizationProvider
implements|implements
name|HiveAuthorizationProvider
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{     }
comment|/*     * (non-Javadoc)     *     * @see     * org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider     * #init(org.apache.hadoop.conf.Configuration)     */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{     }
annotation|@
name|Override
specifier|public
name|HiveAuthenticationProvider
name|getAuthenticator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setAuthenticator
parameter_list|(
name|HiveAuthenticationProvider
name|authenticator
parameter_list|)
block|{     }
comment|/*     * (non-Javadoc)     *     * @see     * org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider     * #authorize(org.apache.hadoop.hive.ql.security.authorization.Privilege[],     * org.apache.hadoop.hive.ql.security.authorization.Privilege[])     */
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{     }
comment|/*     * (non-Javadoc)     *     * @see     * org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider     * #authorize(org.apache.hadoop.hive.metastore.api.Database,     * org.apache.hadoop.hive.ql.security.authorization.Privilege[],     * org.apache.hadoop.hive.ql.security.authorization.Privilege[])     */
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Database
name|db
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{     }
comment|/*     * (non-Javadoc)     *     * @see     * org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider     * #authorize(org.apache.hadoop.hive.ql.metadata.Table,     * org.apache.hadoop.hive.ql.security.authorization.Privilege[],     * org.apache.hadoop.hive.ql.security.authorization.Privilege[])     */
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{     }
comment|/*     * (non-Javadoc)     *     * @see     * org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider     * #authorize(org.apache.hadoop.hive.ql.metadata.Partition,     * org.apache.hadoop.hive.ql.security.authorization.Privilege[],     * org.apache.hadoop.hive.ql.security.authorization.Privilege[])     */
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Partition
name|part
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{     }
comment|/*     * (non-Javadoc)     *     * @see     * org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider     * #authorize(org.apache.hadoop.hive.ql.metadata.Table,     * org.apache.hadoop.hive.ql.metadata.Partition, java.util.List,     * org.apache.hadoop.hive.ql.security.authorization.Privilege[],     * org.apache.hadoop.hive.ql.security.authorization.Privilege[])     */
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{     }
block|}
end_class

end_unit


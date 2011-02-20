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
name|contrib
operator|.
name|serde2
operator|.
name|s3
package|;
end_package

begin_comment
comment|/**  * S3LogStruct.  *  */
end_comment

begin_class
specifier|public
class|class
name|S3LogStruct
block|{
specifier|public
name|String
name|bucketowner
decl_stmt|;
specifier|public
name|String
name|bucketname
decl_stmt|;
specifier|public
name|String
name|rdatetime
decl_stmt|;
comment|// public Long rdatetimeepoch; // The format Hive understands by default,
comment|// should we convert?
specifier|public
name|String
name|rip
decl_stmt|;
specifier|public
name|String
name|requester
decl_stmt|;
specifier|public
name|String
name|requestid
decl_stmt|;
specifier|public
name|String
name|operation
decl_stmt|;
specifier|public
name|String
name|rkey
decl_stmt|;
specifier|public
name|String
name|requesturi
decl_stmt|;
specifier|public
name|Integer
name|httpstatus
decl_stmt|;
specifier|public
name|String
name|errorcode
decl_stmt|;
specifier|public
name|Integer
name|bytessent
decl_stmt|;
specifier|public
name|Integer
name|objsize
decl_stmt|;
specifier|public
name|Integer
name|totaltime
decl_stmt|;
specifier|public
name|Integer
name|turnaroundtime
decl_stmt|;
specifier|public
name|String
name|referer
decl_stmt|;
specifier|public
name|String
name|useragent
decl_stmt|;
comment|// public String rid; // Specific Zemanta use
block|}
end_class

end_unit


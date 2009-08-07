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
name|udf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|exec
operator|.
name|UDF
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
name|exec
operator|.
name|description
import|;
end_import

begin_comment
comment|/**  * UDF to extract specfic parts from URL For example,  * parse_url('http://facebook.com/path/p1.php?query=1', 'HOST') will return  * 'facebook.com' For example,  * parse_url('http://facebook.com/path/p1.php?query=1', 'PATH') will return  * '/path/p1.php' parse_url('http://facebook.com/path/p1.php?query=1', 'QUERY')  * will return 'query=1'  * parse_url('http://facebook.com/path/p1.php?query=1#Ref', 'REF') will return  * 'Ref' parse_url('http://facebook.com/path/p1.php?query=1#Ref', 'PROTOCOL')  * will return 'http' Possible values are  * HOST,PATH,QUERY,REF,PROTOCOL,AUTHORITY,FILE,USERINFO Also you can get a value  * of particular key in QUERY, using syntax QUERY:<KEY_NAME> eg: QUERY:k1.  */
end_comment

begin_class
annotation|@
name|description
argument_list|(
name|name
operator|=
literal|"parse_url"
argument_list|,
name|value
operator|=
literal|"_FUNC_(url, partToExtract[, key]) - extracts a part from a URL"
argument_list|,
name|extended
operator|=
literal|"Parts: HOST, PATH, QUERY, REF, PROTOCOL, AUTHORITY, FILE, "
operator|+
literal|"USERINFO\nkey specifies which query to extract\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('http://facebook.com/path/p1.php?query=1', "
operator|+
literal|"'HOST') FROM src LIMIT 1;\n"
operator|+
literal|"  'facebook.com'\n"
operator|+
literal|"> SELECT _FUNC_('http://facebook.com/path/p1.php?query=1', "
operator|+
literal|"'QUERY') FROM src LIMIT 1;\n"
operator|+
literal|"  'query=1'\n"
operator|+
literal|"> SELECT _FUNC_('http://facebook.com/path/p1.php?query=1', "
operator|+
literal|"'QUERY', 'query') FROM src LIMIT 1;\n"
operator|+
literal|"  '1'"
argument_list|)
specifier|public
class|class
name|UDFParseUrl
extends|extends
name|UDF
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|UDFParseUrl
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|String
name|lastUrlStr
init|=
literal|null
decl_stmt|;
specifier|private
name|URL
name|url
init|=
literal|null
decl_stmt|;
specifier|private
name|Pattern
name|p
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|lastKey
init|=
literal|null
decl_stmt|;
specifier|public
name|UDFParseUrl
parameter_list|()
block|{   }
specifier|public
name|String
name|evaluate
parameter_list|(
name|String
name|urlStr
parameter_list|,
name|String
name|partToExtract
parameter_list|)
block|{
if|if
condition|(
name|urlStr
operator|==
literal|null
operator|||
name|partToExtract
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|lastUrlStr
operator|==
literal|null
operator|||
operator|!
name|urlStr
operator|.
name|equals
argument_list|(
name|lastUrlStr
argument_list|)
condition|)
block|{
try|try
block|{
name|url
operator|=
operator|new
name|URL
argument_list|(
name|urlStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|lastUrlStr
operator|=
name|urlStr
expr_stmt|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"HOST"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getHost
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"PATH"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getPath
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"QUERY"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getQuery
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"REF"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getRef
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"PROTOCOL"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getProtocol
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"FILE"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getFile
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"AUTHORITY"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getAuthority
argument_list|()
return|;
if|if
condition|(
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"USERINFO"
argument_list|)
condition|)
return|return
name|url
operator|.
name|getUserInfo
argument_list|()
return|;
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|evaluate
parameter_list|(
name|String
name|urlStr
parameter_list|,
name|String
name|partToExtract
parameter_list|,
name|String
name|key
parameter_list|)
block|{
if|if
condition|(
operator|!
name|partToExtract
operator|.
name|equals
argument_list|(
literal|"QUERY"
argument_list|)
condition|)
return|return
literal|null
return|;
name|String
name|query
init|=
name|this
operator|.
name|evaluate
argument_list|(
name|urlStr
argument_list|,
name|partToExtract
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|==
literal|null
condition|)
return|return
literal|null
return|;
if|if
condition|(
operator|!
name|key
operator|.
name|equals
argument_list|(
name|lastKey
argument_list|)
condition|)
block|{
name|p
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(&|^)"
operator|+
name|key
operator|+
literal|"=([^&]*)"
argument_list|)
expr_stmt|;
block|}
name|lastKey
operator|=
name|key
expr_stmt|;
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
return|return
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


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

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|serde2
operator|.
name|Deserializer
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|SerDeStats
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorFactory
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ReflectionStructObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructField
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
name|io
operator|.
name|BytesWritable
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
name|io
operator|.
name|Text
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * S3LogDeserializer.  *  */
end_comment

begin_class
specifier|public
class|class
name|S3LogDeserializer
implements|implements
name|Deserializer
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|S3LogDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
static|static
block|{
name|StackTraceElement
index|[]
name|sTrace
init|=
operator|new
name|Exception
argument_list|()
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
name|sTrace
index|[
literal|0
index|]
operator|.
name|getClassName
argument_list|()
expr_stmt|;
block|}
specifier|private
name|ObjectInspector
name|cachedObjectInspector
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"S3ZemantaDeserializer[]"
return|;
block|}
specifier|public
name|S3LogDeserializer
parameter_list|()
throws|throws
name|SerDeException
block|{   }
comment|// This regex is a bit lax in order to compensate for lack of any escaping
comment|// done by Amazon S3 ... for example useragent string can have double quotes
comment|// inside!
specifier|static
name|Pattern
name|regexpat
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(\\S+) (\\S+) \\[(.*?)\\] (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) \"(.+)\" (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) \"(.*)\" \"(.*)\""
argument_list|)
decl_stmt|;
comment|// static Pattern regexrid = Pattern.compile("x-id=([-0-9a-f]{36})");
comment|// static SimpleDateFormat dateparser = new
comment|// SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss ZZZZZ");
name|S3LogStruct
name|deserializeCache
init|=
operator|new
name|S3LogStruct
argument_list|()
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|cachedObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|S3LogStruct
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": initialized"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Integer
name|toInt
parameter_list|(
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
name|s
operator|.
name|compareTo
argument_list|(
literal|"-"
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|s
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|Object
name|deserialize
parameter_list|(
name|S3LogStruct
name|c
parameter_list|,
name|String
name|row
parameter_list|)
throws|throws
name|Exception
block|{
name|Matcher
name|match
init|=
name|regexpat
operator|.
name|matcher
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|int
name|t
init|=
literal|1
decl_stmt|;
try|try
block|{
name|match
operator|.
name|matches
argument_list|()
expr_stmt|;
name|c
operator|.
name|bucketowner
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|bucketname
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"S3 Log Regex did not match:"
operator|+
name|row
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|c
operator|.
name|rdatetime
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
comment|// Should we convert the datetime to the format Hive understands by default
comment|// - either yyyy-mm-dd HH:MM:SS or seconds since epoch?
comment|// Date d = dateparser.parse(c.rdatetime);
comment|// c.rdatetimeepoch = d.getTime() / 1000;
name|c
operator|.
name|rip
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|requester
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|requestid
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|operation
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|rkey
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|requesturi
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
comment|// System.err.println(c.requesturi);
comment|/*      * // Zemanta specific data extractor try { Matcher m2 =      * regexrid.matcher(c.requesturi); m2.find(); c.rid = m2.group(1); } catch      * (Exception e) { c.rid = null; }      */
name|c
operator|.
name|httpstatus
operator|=
name|toInt
argument_list|(
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|errorcode
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|bytessent
operator|=
name|toInt
argument_list|(
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|objsize
operator|=
name|toInt
argument_list|(
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|totaltime
operator|=
name|toInt
argument_list|(
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|turnaroundtime
operator|=
name|toInt
argument_list|(
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|referer
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
name|c
operator|.
name|useragent
operator|=
name|match
operator|.
name|group
argument_list|(
name|t
operator|++
argument_list|)
expr_stmt|;
return|return
operator|(
name|c
operator|)
return|;
block|}
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|field
parameter_list|)
throws|throws
name|SerDeException
block|{
name|String
name|row
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|field
operator|instanceof
name|BytesWritable
condition|)
block|{
name|BytesWritable
name|b
init|=
operator|(
name|BytesWritable
operator|)
name|field
decl_stmt|;
try|try
block|{
name|row
operator|=
name|Text
operator|.
name|decode
argument_list|(
name|b
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|field
operator|instanceof
name|Text
condition|)
block|{
name|row
operator|=
name|field
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|deserialize
argument_list|(
name|deserializeCache
argument_list|,
name|row
argument_list|)
expr_stmt|;
return|return
name|deserializeCache
return|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" expects Text or BytesWritable"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|cachedObjectInspector
return|;
block|}
comment|/**    * @param args    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"This is only a test run"
argument_list|)
expr_stmt|;
try|try
block|{
name|S3LogDeserializer
name|serDe
init|=
operator|new
name|S3LogDeserializer
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Some nasty examples that show how S3 log format is broken ... and to
comment|// test the regex
comment|// These are all sourced from genuine S3 logs
comment|// Text sample = new
comment|// Text("04ff331638adc13885d6c42059584deabbdeabcd55bf0bee491172a79a87b196 img.zemanta.com [09/Apr/2009:22:00:07 +0000] 190.225.84.114 65a011a29cdf8ec533ec3d1ccaae921c F4FC3FEAD8C00024 REST.GET.OBJECT pixy.gif \"GET /pixy.gif?x-id=23d25db1-160b-48bb-a932-e7dc1e88c321 HTTP/1.1\" 304 - - 828 3 - \"http://www.viamujer.com/2009/03/horoscopo-acuario-abril-mayo-y-junio-2009/\" \"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)\"");
comment|// Text sample = new
comment|// Text("04ff331638adc13885d6c42059584deabbdeabcd55bf0bee491172a79a87b196 img.zemanta.com [09/Apr/2009:22:19:49 +0000] 60.28.204.7 65a011a29cdf8ec533ec3d1ccaae921c 7D87B6835125671E REST.GET.OBJECT pixy.gif \"GET /pixy.gif?x-id=b50a4544-938b-4a63-992c-721d1a644b28 HTTP/1.1\" 200 - 828 828 4 3 \"\" \"ZhuaXia.com\"");
comment|// Text sample = new
comment|// Text("04ff331638adc13885d6c42059584deabbdeabcd55bf0bee491172a79a87b196 static.zemanta.com [09/Apr/2009:23:12:39 +0000] 65.94.12.181 65a011a29cdf8ec533ec3d1ccaae921c EEE6FFE9B9F9EA29 REST.HEAD.OBJECT readside/loader.js%22+defer%3D%22defer \"HEAD /readside/loader.js\"+defer=\"defer HTTP/1.0\" 403 AccessDenied 231 - 7 - \"-\" \"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)\"");
name|Text
name|sample
init|=
operator|new
name|Text
argument_list|(
literal|"04ff331638adc13885d6c42059584deabbdeabcd55bf0bee491172a79a87b196 img.zemanta.com [10/Apr/2009:05:34:01 +0000] 70.32.81.92 65a011a29cdf8ec533ec3d1ccaae921c F939A7D698D27C63 REST.GET.OBJECT reblog_b.png \"GET /reblog_b.png?x-id=79ca9376-6326-41b7-9257-eea43d112eb2 HTTP/1.0\" 200 - 1250 1250 160 159 \"-\" \"Firefox 0.8 (Linux)\" useragent=\"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.6) Gecko/20040614 Firefox/0.8\""
argument_list|)
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|sample
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|serDe
operator|.
name|getObjectInspector
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ReflectionStructObjectInspector
name|oi
init|=
operator|(
name|ReflectionStructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Object
name|fieldData
init|=
name|oi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldData
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"null"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|fieldData
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Caught: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


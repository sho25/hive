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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|serde
operator|.
name|Constants
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
name|SerDe
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
name|ObjectInspectorUtils
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
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_class
specifier|public
class|class
name|TestRegexSerDe
extends|extends
name|TestCase
block|{
specifier|private
name|SerDe
name|createSerDe
parameter_list|(
name|String
name|fieldNames
parameter_list|,
name|String
name|fieldTypes
parameter_list|,
name|String
name|inputRegex
parameter_list|,
name|String
name|outputFormatString
parameter_list|)
throws|throws
name|Throwable
block|{
name|Properties
name|schema
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMNS
argument_list|,
name|fieldNames
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|fieldTypes
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
literal|"input.regex"
argument_list|,
name|inputRegex
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
literal|"output.format.string"
argument_list|,
name|outputFormatString
argument_list|)
expr_stmt|;
name|RegexSerDe
name|serde
init|=
operator|new
name|RegexSerDe
argument_list|()
decl_stmt|;
name|serde
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
return|return
name|serde
return|;
block|}
comment|/**    * Test the LazySimpleSerDe class.    */
specifier|public
name|void
name|testRegexSerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Create the SerDe
name|SerDe
name|serDe
init|=
name|createSerDe
argument_list|(
literal|"host,identity,user,time,request,status,size,referer,agent"
argument_list|,
literal|"string,string,string,string,string,string,string,string,string"
argument_list|,
literal|"([^ ]*) ([^ ]*) ([^ ]*) (-|\\[[^\\]]*\\]) ([^ \"]*|\"[^\"]*\") ([0-9]*) ([0-9]*) ([^ \"]*|\"[^\"]*\") ([^ \"]*|\"[^\"]*\")"
argument_list|,
literal|"%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s"
argument_list|)
decl_stmt|;
comment|// Data
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"127.0.0.1 - - [26/May/2009:00:00:00 +0000] "
operator|+
literal|"\"GET /someurl/?track=Blabla(Main) HTTP/1.1\" 200 5864 - "
operator|+
literal|"\"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/525.19 (KHTML, like Gecko) Chrome/1.0.154.65 Safari/525.19\""
argument_list|)
decl_stmt|;
comment|// Deserialize
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|ObjectInspector
name|rowOI
init|=
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Deserialized row: "
operator|+
name|row
argument_list|)
expr_stmt|;
comment|// Serialize
name|Text
name|serialized
init|=
operator|(
name|Text
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|rowOI
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|t
argument_list|,
name|serialized
argument_list|)
expr_stmt|;
comment|// Do some changes (optional)
name|ObjectInspector
name|standardWritableRowOI
init|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|rowOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|Object
name|standardWritableRow
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|row
argument_list|,
name|rowOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
comment|// Serialize
name|serialized
operator|=
operator|(
name|Text
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|standardWritableRow
argument_list|,
name|standardWritableRowOI
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t
argument_list|,
name|serialized
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit


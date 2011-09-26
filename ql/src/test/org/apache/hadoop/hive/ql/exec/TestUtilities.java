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
name|exec
package|;
end_package

begin_import
import|import static
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
name|Utilities
operator|.
name|getFileExtension
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
name|io
operator|.
name|HiveIgnoreKeyTextOutputFormat
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_class
specifier|public
class|class
name|TestUtilities
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testGetFileExtension
parameter_list|()
block|{
name|JobConf
name|jc
init|=
operator|new
name|JobConf
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for uncompressed unknown format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for compressed unknown format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for uncompressed text format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deflate for uncompressed text format"
argument_list|,
literal|".deflate"
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"No extension for uncompressed default format"
argument_list|,
literal|""
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Deflate for uncompressed default format"
argument_list|,
literal|".deflate"
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|extension
init|=
literal|".myext"
decl_stmt|;
name|jc
operator|.
name|set
argument_list|(
literal|"hive.output.file.extension"
argument_list|,
name|extension
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for uncompressed unknown format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for compressed unknown format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for uncompressed text format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|false
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Custom extension for uncompressed text format"
argument_list|,
name|extension
argument_list|,
name|getFileExtension
argument_list|(
name|jc
argument_list|,
literal|true
argument_list|,
operator|new
name|HiveIgnoreKeyTextOutputFormat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


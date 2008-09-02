begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_comment
comment|/* Generated By:JJTree&JavaCC: Do not edit this line. thrift_grammarConstants.java */
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
name|serde
operator|.
name|dynamic_type
package|;
end_package

begin_interface
specifier|public
interface|interface
name|thrift_grammarConstants
block|{
name|int
name|EOF
init|=
literal|0
decl_stmt|;
name|int
name|tok_const
init|=
literal|8
decl_stmt|;
name|int
name|tok_namespace
init|=
literal|9
decl_stmt|;
name|int
name|tok_cpp_namespace
init|=
literal|10
decl_stmt|;
name|int
name|tok_cpp_include
init|=
literal|11
decl_stmt|;
name|int
name|tok_cpp_type
init|=
literal|12
decl_stmt|;
name|int
name|tok_java_package
init|=
literal|13
decl_stmt|;
name|int
name|tok_cocoa_prefix
init|=
literal|14
decl_stmt|;
name|int
name|tok_csharp_namespace
init|=
literal|15
decl_stmt|;
name|int
name|tok_php_namespace
init|=
literal|16
decl_stmt|;
name|int
name|tok_py_module
init|=
literal|17
decl_stmt|;
name|int
name|tok_perl_package
init|=
literal|18
decl_stmt|;
name|int
name|tok_ruby_namespace
init|=
literal|19
decl_stmt|;
name|int
name|tok_smalltalk_category
init|=
literal|20
decl_stmt|;
name|int
name|tok_smalltalk_prefix
init|=
literal|21
decl_stmt|;
name|int
name|tok_xsd_all
init|=
literal|22
decl_stmt|;
name|int
name|tok_xsd_optional
init|=
literal|23
decl_stmt|;
name|int
name|tok_xsd_nillable
init|=
literal|24
decl_stmt|;
name|int
name|tok_xsd_namespace
init|=
literal|25
decl_stmt|;
name|int
name|tok_xsd_attrs
init|=
literal|26
decl_stmt|;
name|int
name|tok_include
init|=
literal|27
decl_stmt|;
name|int
name|tok_void
init|=
literal|28
decl_stmt|;
name|int
name|tok_bool
init|=
literal|29
decl_stmt|;
name|int
name|tok_byte
init|=
literal|30
decl_stmt|;
name|int
name|tok_i16
init|=
literal|31
decl_stmt|;
name|int
name|tok_i32
init|=
literal|32
decl_stmt|;
name|int
name|tok_i64
init|=
literal|33
decl_stmt|;
name|int
name|tok_double
init|=
literal|34
decl_stmt|;
name|int
name|tok_string
init|=
literal|35
decl_stmt|;
name|int
name|tok_slist
init|=
literal|36
decl_stmt|;
name|int
name|tok_senum
init|=
literal|37
decl_stmt|;
name|int
name|tok_map
init|=
literal|38
decl_stmt|;
name|int
name|tok_list
init|=
literal|39
decl_stmt|;
name|int
name|tok_set
init|=
literal|40
decl_stmt|;
name|int
name|tok_async
init|=
literal|41
decl_stmt|;
name|int
name|tok_typedef
init|=
literal|42
decl_stmt|;
name|int
name|tok_struct
init|=
literal|43
decl_stmt|;
name|int
name|tok_exception
init|=
literal|44
decl_stmt|;
name|int
name|tok_extends
init|=
literal|45
decl_stmt|;
name|int
name|tok_throws
init|=
literal|46
decl_stmt|;
name|int
name|tok_service
init|=
literal|47
decl_stmt|;
name|int
name|tok_enum
init|=
literal|48
decl_stmt|;
name|int
name|tok_required
init|=
literal|49
decl_stmt|;
name|int
name|tok_optional
init|=
literal|50
decl_stmt|;
name|int
name|tok_int_constant
init|=
literal|51
decl_stmt|;
name|int
name|tok_double_constant
init|=
literal|52
decl_stmt|;
name|int
name|IDENTIFIER
init|=
literal|53
decl_stmt|;
name|int
name|LETTER
init|=
literal|54
decl_stmt|;
name|int
name|DIGIT
init|=
literal|55
decl_stmt|;
name|int
name|tok_literal
init|=
literal|56
decl_stmt|;
name|int
name|tok_st_identifier
init|=
literal|57
decl_stmt|;
name|int
name|DEFAULT
init|=
literal|0
decl_stmt|;
name|String
index|[]
name|tokenImage
init|=
block|{
literal|"<EOF>"
block|,
literal|"\" \""
block|,
literal|"\"\\t\""
block|,
literal|"\"\\n\""
block|,
literal|"\"\\r\""
block|,
literal|"<token of kind 5>"
block|,
literal|"<token of kind 6>"
block|,
literal|"<token of kind 7>"
block|,
literal|"\"const\""
block|,
literal|"\"namespace\""
block|,
literal|"\"cpp_namespace\""
block|,
literal|"\"cpp_include\""
block|,
literal|"\"cpp_type\""
block|,
literal|"\"java_package\""
block|,
literal|"\"cocoa_prefix\""
block|,
literal|"\"csharp_namespace\""
block|,
literal|"\"php_namespace\""
block|,
literal|"\"py_module\""
block|,
literal|"\"perl_package\""
block|,
literal|"\"ruby_namespace\""
block|,
literal|"\"smalltalk_category\""
block|,
literal|"\"smalltalk_prefix\""
block|,
literal|"\"xsd_all\""
block|,
literal|"\"xsd_optional\""
block|,
literal|"\"xsd_nillable\""
block|,
literal|"\"xsd_namespace\""
block|,
literal|"\"xsd_attrs\""
block|,
literal|"\"include\""
block|,
literal|"\"void\""
block|,
literal|"\"bool\""
block|,
literal|"\"byte\""
block|,
literal|"\"i16\""
block|,
literal|"\"i32\""
block|,
literal|"\"i64\""
block|,
literal|"\"double\""
block|,
literal|"\"string\""
block|,
literal|"\"slist\""
block|,
literal|"\"senum\""
block|,
literal|"\"map\""
block|,
literal|"\"list\""
block|,
literal|"\"set\""
block|,
literal|"\"async\""
block|,
literal|"\"typedef\""
block|,
literal|"\"struct\""
block|,
literal|"\"exception\""
block|,
literal|"\"extends\""
block|,
literal|"\"throws\""
block|,
literal|"\"service\""
block|,
literal|"\"enum\""
block|,
literal|"\"required\""
block|,
literal|"\"optional\""
block|,
literal|"<tok_int_constant>"
block|,
literal|"<tok_double_constant>"
block|,
literal|"<IDENTIFIER>"
block|,
literal|"<LETTER>"
block|,
literal|"<DIGIT>"
block|,
literal|"<tok_literal>"
block|,
literal|"<tok_st_identifier>"
block|,
literal|"\",\""
block|,
literal|"\";\""
block|,
literal|"\"{\""
block|,
literal|"\"}\""
block|,
literal|"\"=\""
block|,
literal|"\"[\""
block|,
literal|"\"]\""
block|,
literal|"\":\""
block|,
literal|"\"(\""
block|,
literal|"\")\""
block|,
literal|"\"<\""
block|,
literal|"\">\""
block|,   }
decl_stmt|;
block|}
end_interface

end_unit


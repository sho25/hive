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
name|hcatalog
operator|.
name|data
operator|.
name|schema
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatFieldSchema
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestHCatSchemaUtils
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestHCatSchemaUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|void
name|testSimpleOperation
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|typeString
init|=
literal|"struct<name:string,studentid:int,"
operator|+
literal|"contact:struct<phno:string,email:string>,"
operator|+
literal|"currently_registered_courses:array<string>,"
operator|+
literal|"current_grades:map<string,string>,"
operator|+
literal|"phnos:array<struct<phno:string,type:string>>,blah:array<int>>"
decl_stmt|;
name|TypeInfo
name|ti
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromTypeString
argument_list|(
name|typeString
argument_list|)
decl_stmt|;
name|HCatSchema
name|hsch
init|=
name|HCatSchemaUtils
operator|.
name|getHCatSchemaFromTypeString
argument_list|(
name|typeString
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Type name : {}"
argument_list|,
name|ti
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HCatSchema : {}"
argument_list|,
name|hsch
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hsch
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ti
operator|.
name|getTypeName
argument_list|()
argument_list|,
name|hsch
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTypeString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hsch
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTypeString
argument_list|()
argument_list|,
name|typeString
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|void
name|pretty_print
parameter_list|(
name|PrintStream
name|pout
parameter_list|,
name|HCatSchema
name|hsch
parameter_list|)
throws|throws
name|HCatException
block|{
name|pretty_print
argument_list|(
name|pout
argument_list|,
name|hsch
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|pretty_print
parameter_list|(
name|PrintStream
name|pout
parameter_list|,
name|HCatSchema
name|hsch
parameter_list|,
name|String
name|prefix
parameter_list|)
throws|throws
name|HCatException
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HCatFieldSchema
name|field
range|:
name|hsch
operator|.
name|getFields
argument_list|()
control|)
block|{
name|pretty_print
argument_list|(
name|pout
argument_list|,
name|field
argument_list|,
name|prefix
operator|+
literal|"."
operator|+
operator|(
name|field
operator|.
name|getName
argument_list|()
operator|==
literal|null
condition|?
name|i
else|:
name|field
operator|.
name|getName
argument_list|()
operator|)
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|pretty_print
parameter_list|(
name|PrintStream
name|pout
parameter_list|,
name|HCatFieldSchema
name|hfsch
parameter_list|,
name|String
name|prefix
parameter_list|)
throws|throws
name|HCatException
block|{
name|Category
name|tcat
init|=
name|hfsch
operator|.
name|getCategory
argument_list|()
decl_stmt|;
if|if
condition|(
name|Category
operator|.
name|STRUCT
operator|==
name|tcat
condition|)
block|{
name|pretty_print
argument_list|(
name|pout
argument_list|,
name|hfsch
operator|.
name|getStructSubSchema
argument_list|()
argument_list|,
name|prefix
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Category
operator|.
name|ARRAY
operator|==
name|tcat
condition|)
block|{
name|pretty_print
argument_list|(
name|pout
argument_list|,
name|hfsch
operator|.
name|getArrayElementSchema
argument_list|()
argument_list|,
name|prefix
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Category
operator|.
name|MAP
operator|==
name|tcat
condition|)
block|{
name|pout
operator|.
name|println
argument_list|(
name|prefix
operator|+
literal|".mapkey:\t"
operator|+
name|hfsch
operator|.
name|getMapKeyType
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|pretty_print
argument_list|(
name|pout
argument_list|,
name|hfsch
operator|.
name|getMapValueSchema
argument_list|()
argument_list|,
name|prefix
operator|+
literal|".mapvalue:"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pout
operator|.
name|println
argument_list|(
name|prefix
operator|+
literal|"\t"
operator|+
name|hfsch
operator|.
name|getType
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


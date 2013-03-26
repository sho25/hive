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
name|hcatalog
operator|.
name|utils
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|utils
operator|.
name|TypeDataCheck
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
name|util
operator|.
name|ProgramDriver
import|;
end_import

begin_comment
comment|/**  * A description of an example program based on its class and a   * human-readable description.  */
end_comment

begin_class
specifier|public
class|class
name|HCatTestDriver
block|{
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|argv
index|[]
parameter_list|)
block|{
name|int
name|exitCode
init|=
operator|-
literal|1
decl_stmt|;
name|ProgramDriver
name|pgd
init|=
operator|new
name|ProgramDriver
argument_list|()
decl_stmt|;
try|try
block|{
name|pgd
operator|.
name|addClass
argument_list|(
literal|"typedatacheck"
argument_list|,
name|TypeDataCheck
operator|.
name|class
argument_list|,
literal|"A map/reduce program that checks the type of each field and"
operator|+
literal|" outputs the entire table (to test hcat)."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
literal|"sumnumbers"
argument_list|,
name|SumNumbers
operator|.
name|class
argument_list|,
literal|"A map/reduce program that performs a group by on the first column and a "
operator|+
literal|"SUM operation on the other columns of the \"numbers\" table."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
literal|"storenumbers"
argument_list|,
name|StoreNumbers
operator|.
name|class
argument_list|,
literal|"A map/reduce program that "
operator|+
literal|"reads from the \"numbers\" table and adds 10 to each fields and writes "
operator|+
literal|"to the \"numbers_partitioned\" table into the datestamp=20100101 "
operator|+
literal|"partition OR the \"numbers_empty_initially\" table based on a "
operator|+
literal|"cmdline arg"
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
literal|"storecomplex"
argument_list|,
name|StoreComplex
operator|.
name|class
argument_list|,
literal|"A map/reduce program that "
operator|+
literal|"reads from the \"complex\" table and stores as-is into the "
operator|+
literal|"\"complex_empty_initially\" table."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|addClass
argument_list|(
literal|"storedemo"
argument_list|,
name|StoreDemo
operator|.
name|class
argument_list|,
literal|"demo prog."
argument_list|)
expr_stmt|;
name|pgd
operator|.
name|driver
argument_list|(
name|argv
argument_list|)
expr_stmt|;
comment|// Success
name|exitCode
operator|=
literal|0
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
block|}
name|System
operator|.
name|exit
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


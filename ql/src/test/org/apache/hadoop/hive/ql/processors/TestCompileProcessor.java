begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|processors
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestCompileProcessor
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSyntax
parameter_list|()
throws|throws
name|Exception
block|{
name|CompileProcessor
name|cp
init|=
operator|new
name|CompileProcessor
argument_list|()
decl_stmt|;
name|cp
operator|.
name|run
argument_list|(
literal|"` public class x { \n }` AS GROOVY NAMED x.groovy"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"GROOVY"
argument_list|,
name|cp
operator|.
name|getLang
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|" public class x { \n }"
argument_list|,
name|cp
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"x.groovy"
argument_list|,
name|cp
operator|.
name|getNamed
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|cp
operator|.
name|run
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|cp
operator|.
name|run
argument_list|(
literal|"bla bla "
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CommandProcessorException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|CompileProcessor
name|cp2
init|=
operator|new
name|CompileProcessor
argument_list|()
decl_stmt|;
name|CommandProcessorResponse
name|response
init|=
name|cp2
operator|.
name|run
argument_list|(
literal|"` import org.apache.hadoop.hive.ql.exec.UDF \n public class x { \n }` AS GROOVY NAMED x.groovy"
argument_list|)
decl_stmt|;
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|response
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|f
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


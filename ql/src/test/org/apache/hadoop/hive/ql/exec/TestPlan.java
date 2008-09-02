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
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|TextInputFormat
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
name|parse
operator|.
name|SemanticAnalyzer
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
name|parse
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
name|ql
operator|.
name|plan
operator|.
name|*
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
name|Utilities
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
name|IgnoreKeyTextOutputFormat
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
name|thrift
operator|.
name|columnsetSerDe
import|;
end_import

begin_class
specifier|public
class|class
name|TestPlan
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testPlan
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|F1
init|=
literal|"#affiliations"
decl_stmt|;
specifier|final
name|String
name|F2
init|=
literal|"friends[0].friendid"
decl_stmt|;
try|try
block|{
comment|// initialize a complete map reduce configuration
name|exprNodeDesc
name|expr1
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|TypeInfo
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|F1
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|expr2
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|TypeInfo
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|F2
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|filterExpr
init|=
name|SemanticAnalyzer
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"=="
argument_list|,
name|expr1
argument_list|,
name|expr2
argument_list|)
decl_stmt|;
name|filterDesc
name|filterCtx
init|=
operator|new
name|filterDesc
argument_list|(
name|filterExpr
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|filterDesc
argument_list|>
name|op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|filterDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|op
operator|.
name|setConf
argument_list|(
name|filterCtx
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliasList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|aliasList
operator|.
name|add
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pa
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|pa
operator|.
name|put
argument_list|(
literal|"/tmp/testfolder"
argument_list|,
name|aliasList
argument_list|)
expr_stmt|;
name|tableDesc
name|tblDesc
init|=
operator|new
name|tableDesc
argument_list|(
name|columnsetSerDe
operator|.
name|class
argument_list|,
name|TextInputFormat
operator|.
name|class
argument_list|,
name|IgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|,
operator|new
name|Properties
argument_list|()
argument_list|)
decl_stmt|;
name|partitionDesc
name|partDesc
init|=
operator|new
name|partitionDesc
argument_list|(
name|tblDesc
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|partitionDesc
argument_list|>
name|pt
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|partitionDesc
argument_list|>
argument_list|()
decl_stmt|;
name|pt
operator|.
name|put
argument_list|(
literal|"/tmp/testfolder"
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|ao
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|ao
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
name|op
argument_list|)
expr_stmt|;
name|mapredWork
name|mrwork
init|=
operator|new
name|mapredWork
argument_list|()
decl_stmt|;
name|mrwork
operator|.
name|setPathToAliases
argument_list|(
name|pa
argument_list|)
expr_stmt|;
name|mrwork
operator|.
name|setPathToPartitionInfo
argument_list|(
name|pt
argument_list|)
expr_stmt|;
name|mrwork
operator|.
name|setAliasToWork
argument_list|(
name|ao
argument_list|)
expr_stmt|;
comment|// serialize the configuration once ..
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|serializeMapRedWork
argument_list|(
name|mrwork
argument_list|,
name|baos
argument_list|)
expr_stmt|;
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
name|String
name|v1
init|=
name|baos
operator|.
name|toString
argument_list|()
decl_stmt|;
comment|// store into configuration
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|TestPlan
operator|.
name|class
argument_list|)
decl_stmt|;
name|job
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
literal|"file:///"
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|job
argument_list|,
name|mrwork
argument_list|)
expr_stmt|;
name|mapredWork
name|mrwork2
init|=
name|Utilities
operator|.
name|getMapRedWork
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|clearMapRedWork
argument_list|(
name|job
argument_list|)
expr_stmt|;
comment|// over here we should have some checks of the deserialized object against the orginal object
comment|//System.out.println(v1);
comment|// serialize again
name|baos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|Utilities
operator|.
name|serializeMapRedWork
argument_list|(
name|mrwork2
argument_list|,
name|baos
argument_list|)
expr_stmt|;
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// verify that the two are equal
name|assertEquals
argument_list|(
name|v1
argument_list|,
name|baos
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|excp
parameter_list|)
block|{
name|excp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|excp
throw|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Serialization/Deserialization of plan successful"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


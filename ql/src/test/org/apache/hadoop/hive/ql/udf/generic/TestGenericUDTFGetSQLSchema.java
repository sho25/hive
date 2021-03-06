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
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|List
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
name|conf
operator|.
name|HiveConf
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
name|metadata
operator|.
name|HiveException
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
name|session
operator|.
name|SessionState
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * TestGenericUDTFGetSQLSchema.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDTFGetSQLSchema
block|{
specifier|private
specifier|static
name|SessionState
name|sessionState
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hive.security.authorization.manager"
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.DefaultHiveAuthorizationProvider"
argument_list|)
expr_stmt|;
name|sessionState
operator|=
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|SessionState
operator|.
name|endStart
argument_list|(
name|sessionState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithComplexTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|invokeUDTFAndTest
argument_list|(
literal|"select array('val1','val2') c1,"
operator|+
literal|" named_struct('a',1,'b','2') c2, "
operator|+
literal|" array(array(1)) c3,"
operator|+
literal|" array(named_struct('a',1,'b','2')) c4,"
operator|+
literal|" map(1,1) c5"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"c1"
block|,
literal|"array<string>"
block|,
literal|"c2"
block|,
literal|"struct<a:int,b:string>"
block|,
literal|"c3"
block|,
literal|"array<array<int>>"
block|,
literal|"c4"
block|,
literal|"array<struct<a:int,b:string>>"
block|,
literal|"c5"
block|,
literal|"map<int,int>"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithSimpleTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|invokeUDTFAndTest
argument_list|(
literal|"select 1 as c1, 'Happy Valentines Day' as c2, 2.2 as c3, cast(2.2 as float) c4, "
operator|+
literal|"cast(2.2 as double) c5, "
operator|+
literal|"cast('2019-02-14' as date) c6"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"c1"
block|,
literal|"int"
block|,
literal|"c2"
block|,
literal|"string"
block|,
literal|"c3"
block|,
literal|"decimal(2,1)"
block|,
literal|"c4"
block|,
literal|"float"
block|,
literal|"c5"
block|,
literal|"double"
block|,
literal|"c6"
block|,
literal|"date"
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithDDL
parameter_list|()
throws|throws
name|Exception
block|{
name|invokeUDTFAndTest
argument_list|(
literal|"show tables"
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|invokeUDTFAndTest
parameter_list|(
name|String
name|query
parameter_list|,
name|String
index|[]
name|expected
parameter_list|)
throws|throws
name|HiveException
block|{
name|GenericUDTFGetSQLSchema
name|genericUDTFGetSQLSchema
init|=
operator|new
name|GenericUDTFGetSQLSchema
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|actual
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|genericUDTFGetSQLSchema
operator|.
name|collector
operator|=
name|input
lambda|->
block|{
if|if
condition|(
name|input
operator|!=
literal|null
condition|)
block|{
name|Object
index|[]
name|udfOutput
init|=
operator|(
name|Object
index|[]
operator|)
name|input
decl_stmt|;
name|actual
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|udfOutput
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|actual
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|udfOutput
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
name|genericUDTFGetSQLSchema
operator|.
name|initialize
argument_list|(
operator|new
name|ObjectInspector
index|[]
block|{
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
block|}
argument_list|)
expr_stmt|;
name|genericUDTFGetSQLSchema
operator|.
name|process
argument_list|(
operator|new
name|Object
index|[]
block|{
name|query
block|}
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|length
argument_list|,
name|actual
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Failed for query: "
operator|+
name|query
operator|+
literal|". Expected: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
operator|+
literal|". Actual: "
operator|+
name|actual
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|expected
argument_list|,
name|actual
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


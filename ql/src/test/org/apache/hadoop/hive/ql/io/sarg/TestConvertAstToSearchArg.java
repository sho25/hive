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
name|io
operator|.
name|sarg
package|;
end_package

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertNull
import|;
end_import

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|beans
operator|.
name|XMLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|Set
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
name|SerializationUtilities
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
name|parquet
operator|.
name|read
operator|.
name|ParquetFilterPredicateConverter
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
name|sarg
operator|.
name|SearchArgument
operator|.
name|TruthValue
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
name|ExprNodeGenericFuncDesc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|filter2
operator|.
name|predicate
operator|.
name|FilterPredicate
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|schema
operator|.
name|MessageTypeParser
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
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * These tests cover the conversion from Hive's AST to SearchArguments.  */
end_comment

begin_class
specifier|public
class|class
name|TestConvertAstToSearchArg
block|{
specifier|private
specifier|static
name|void
name|assertNoSharedNodes
parameter_list|(
name|ExpressionTree
name|tree
parameter_list|,
name|Set
argument_list|<
name|ExpressionTree
argument_list|>
name|seen
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|tree
argument_list|)
operator|&&
name|tree
operator|.
name|getOperator
argument_list|()
operator|!=
name|ExpressionTree
operator|.
name|Operator
operator|.
name|LEAF
condition|)
block|{
name|assertTrue
argument_list|(
literal|"repeated node in expression "
operator|+
name|tree
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|seen
operator|.
name|add
argument_list|(
name|tree
argument_list|)
expr_stmt|;
if|if
condition|(
name|tree
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExpressionTree
name|child
range|:
name|tree
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|assertNoSharedNodes
argument_list|(
name|child
argument_list|,
name|seen
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|ExprNodeGenericFuncDesc
name|getFuncDesc
parameter_list|(
name|String
name|xmlSerialized
parameter_list|)
block|{
name|byte
index|[]
name|bytes
decl_stmt|;
try|try
block|{
name|bytes
operator|=
name|xmlSerialized
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"UTF-8 support required"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|XMLDecoder
name|decoder
init|=
operator|new
name|XMLDecoder
argument_list|(
name|bais
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
return|return
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|decoder
operator|.
name|readObject
argument_list|()
return|;
block|}
finally|finally
block|{
name|decoder
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression1
parameter_list|()
throws|throws
name|Exception
block|{
comment|// first_name = 'john' or
comment|//  'greg'< first_name or
comment|//  'alan'> first_name or
comment|//  id> 12 or
comment|//  13< id or
comment|//  id< 15 or
comment|//  16> id or
comment|//  (id<=> 30 and first_name<=> 'owen')
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>string</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>john</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>greg</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>alan</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPGreaterThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo2\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>12</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPGreaterThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>13</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>15</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>16</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPGreaterThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>30</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualNS\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>owen</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualNS\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> \n"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|9
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
index|[]
name|conditions
init|=
operator|new
name|String
index|[]
block|{
literal|"eq(first_name, Binary{\"john\"})"
block|,
comment|/* first_name = 'john' */
literal|"not(lteq(first_name, Binary{\"greg\"}))"
block|,
comment|/* 'greg'< first_name */
literal|"lt(first_name, Binary{\"alan\"})"
block|,
comment|/* 'alan'> first_name */
literal|"not(lteq(id, 12))"
block|,
comment|/* id> 12 or */
literal|"not(lteq(id, 13))"
block|,
comment|/* 13< id or */
literal|"lt(id, 15)"
block|,
comment|/* id< 15 or */
literal|"lt(id, 16)"
block|,
comment|/* 16> id or */
literal|"eq(id, 30)"
block|,
comment|/* id<=> 30 */
literal|"eq(first_name, Binary{\"owen\"})"
comment|/* first_name<=> 'owen' */
block|}
decl_stmt|;
name|String
name|expected
init|=
name|String
operator|.
name|format
argument_list|(
literal|"and(or(or(or(or(or(or(or(%1$s, %2$s), %3$s), %4$s), %5$s), %6$s), %7$s), %8$s), "
operator|+
literal|"or(or(or(or(or(or(or(%1$s, %2$s), %3$s), %4$s), %5$s), %6$s), %7$s), %9$s))"
argument_list|,
name|conditions
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"john"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN_EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"greg"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"alan"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN_EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN_EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|13L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|15L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|16L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|7
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|NULL_SAFE_EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|30L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|NULL_SAFE_EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"owen"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(and (or leaf-0 (not leaf-1) leaf-2 (not leaf-3)"
operator|+
literal|" (not leaf-4) leaf-5 leaf-6 leaf-7)"
operator|+
literal|" (or leaf-0 (not leaf-1) leaf-2 (not leaf-3)"
operator|+
literal|" (not leaf-4) leaf-5 leaf-6 leaf-8))"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoSharedNodes
argument_list|(
name|sarg
operator|.
name|getExpression
argument_list|()
argument_list|,
name|Sets
operator|.
expr|<
name|ExpressionTree
operator|>
name|newIdentityHashSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression2
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* first_name is null or        first_name<> 'sue' or        id>= 12 or        id<= 4; */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>string</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPNull\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>sue</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPNotEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo2\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>12</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualOrGreaterThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>4</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualOrLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> \n"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|conditions
init|=
operator|new
name|String
index|[]
block|{
literal|"eq(first_name, null)"
block|,
comment|/* first_name is null  */
literal|"not(eq(first_name, Binary{\"sue\"}))"
block|,
comment|/* first_name<> 'sue' */
literal|"not(lt(id, 12))"
block|,
comment|/* id>= 12            */
literal|"lteq(id, 4)"
comment|/* id<= 4             */
block|}
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
name|String
operator|.
name|format
argument_list|(
literal|"or(or(or(%1$s, %2$s), %3$s), %4$s)"
argument_list|,
name|conditions
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|IS_NULL
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"sue"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN_EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(or leaf-0 (not leaf-1) (not leaf-2) leaf-3)"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoSharedNodes
argument_list|(
name|sarg
operator|.
name|getExpression
argument_list|()
argument_list|,
name|Sets
operator|.
expr|<
name|ExpressionTree
operator|>
name|newIdentityHashSet
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NULL
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES_NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES_NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES_NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO_NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES_NULL
argument_list|,
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO_NULL
argument_list|,
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression3
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* (id between 23 and 45) and        first_name = 'alan' and        substr('xxxxx', 3) == first_name and        'smith' = last_name and        substr(first_name, 3) == 'yyy' */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<boolean>false</boolean> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>23</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>45</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBetween\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo2\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>string</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>alan</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>xxxxx</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>3</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBridge\"> \n"
operator|+
literal|"<void property=\"udfClassName\"> \n"
operator|+
literal|"<string>org.apache.hadoop.hive.ql.udf.UDFSubstr</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfName\"> \n"
operator|+
literal|"<string>substr</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>smith</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>last_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>3</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBridge\"> \n"
operator|+
literal|"<void property=\"udfClassName\"> \n"
operator|+
literal|"<string>org.apache.hadoop.hive.ql.udf.UDFSubstr</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfName\"> \n"
operator|+
literal|"<string>substr</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>yyy</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> \n"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|conditions
init|=
operator|new
name|String
index|[]
block|{
literal|"lt(id, 45)"
block|,
comment|/* id between 23 and 45 */
literal|"not(lteq(id, 23))"
block|,
comment|/* id between 23 and 45 */
literal|"eq(first_name, Binary{\"alan\"})"
block|,
comment|/* first_name = 'alan'  */
literal|"eq(last_name, Binary{\"smith\"})"
comment|/* 'smith' = last_name  */
block|}
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; required binary last_name;}"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
name|String
operator|.
name|format
argument_list|(
literal|"and(and(and(%1$s, %2$s), %3$s), %4$s)"
argument_list|,
name|conditions
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|BETWEEN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|23L
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|45L
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"alan"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"last_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"smith"
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(and leaf-0 leaf-1 leaf-2)"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoSharedNodes
argument_list|(
name|sarg
operator|.
name|getExpression
argument_list|()
argument_list|,
name|Sets
operator|.
expr|<
name|ExpressionTree
operator|>
name|newIdentityHashSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression4
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* id<> 12 and        first_name in ('john', 'sue') and        id in (34,50) */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>12</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPNotEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo2\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>string</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>john</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo2\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>sue</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFIn\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>34</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>50</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFIn\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> \n"
operator|+
literal|"\n"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
index|[]
name|conditions
init|=
operator|new
name|String
index|[]
block|{
literal|"not(eq(id, 12))"
block|,
comment|/* id<> 12 */
literal|"or(eq(first_name, Binary{\"john\"}), eq(first_name, Binary{\"sue\"}))"
block|,
comment|/* first_name in       ('john', 'sue') */
literal|"or(eq(id, 34), eq(id, 50))"
comment|/* id in (34,50) */
block|}
decl_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
name|String
operator|.
name|format
argument_list|(
literal|"and(and(%1$s, %2$s), %3$s)"
argument_list|,
name|conditions
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|EQUALS
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|IN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"john"
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"sue"
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|IN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|34L
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50L
argument_list|,
name|leaf
operator|.
name|getLiteralList
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(and (not leaf-0) leaf-1 leaf-2)"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoSharedNodes
argument_list|(
name|sarg
operator|.
name|getExpression
argument_list|()
argument_list|,
name|Sets
operator|.
expr|<
name|ExpressionTree
operator|>
name|newIdentityHashSet
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|NULL
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES_NULL
argument_list|,
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|NULL
argument_list|,
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|TruthValue
operator|.
name|YES
argument_list|,
name|TruthValue
operator|.
name|NO_NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression5
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* (first_name< 'owen' or 'foobar' = substr(last_name, 4)) and     first_name between 'david' and 'greg' */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>string</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>owen</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>foobar</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>last_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>4</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBridge\"> \n"
operator|+
literal|"<void property=\"udfClassName\"> \n"
operator|+
literal|"<string>org.apache.hadoop.hive.ql.udf.UDFSubstr</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfName\"> \n"
operator|+
literal|"<string>substr</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<boolean>false</boolean> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>david</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<string>greg</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBetween\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> \n"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"and(lt(first_name, Binary{\"greg\"}), not(lteq(first_name, Binary{\"david\"})))"
decl_stmt|;
name|assertEquals
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|expected
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|BETWEEN
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"first_name"
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoSharedNodes
argument_list|(
name|sarg
operator|.
name|getExpression
argument_list|()
argument_list|,
name|Sets
operator|.
expr|<
name|ExpressionTree
operator|>
name|newIdentityHashSet
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression7
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* (id< 10 and id< 11 and id< 12) or (id< 13 and id< 14 and id< 15) or        (id< 16 and id< 17) or id< 18 */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>10</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>11</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>12</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>13</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>14</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>15</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>16</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>17</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>18</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPOr\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object>\n"
operator|+
literal|"</java>"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|9
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"and(and(and(and(and(and(and(and(and(and(and(and(and(and(and(and(and("
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 10)), lt(id, 13)), lt(id, 16)), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 11)), lt(id, 13)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 12)), lt(id, 13)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 10)), lt(id, 14)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 11)), lt(id, 14)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 12)), lt(id, 14)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 10)), lt(id, 15)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 11)), lt(id, 15)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 12)), lt(id, 15)), lt(id, 16))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 10)), lt(id, 13)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 11)), lt(id, 13)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 12)), lt(id, 13)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 10)), lt(id, 14)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 11)), lt(id, 14)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 12)), lt(id, 14)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 10)), lt(id, 15)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 11)), lt(id, 15)), lt(id, 17))), "
operator|+
literal|"or(or(or(lt(id, 18), lt(id, 12)), lt(id, 15)), lt(id, 17)))"
decl_stmt|;
name|assertEquals
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|expected
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|18L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|13L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|16L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|11L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|12L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|14L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|7
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|15L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|leaves
operator|.
name|get
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaf
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaf
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|17L
argument_list|,
name|leaf
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(and"
operator|+
literal|" (or leaf-0 leaf-1 leaf-2 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-4 leaf-2 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-5 leaf-2 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-1 leaf-6 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-4 leaf-6 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-5 leaf-6 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-1 leaf-7 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-4 leaf-7 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-5 leaf-7 leaf-3)"
operator|+
literal|" (or leaf-0 leaf-1 leaf-2 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-4 leaf-2 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-5 leaf-2 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-1 leaf-6 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-4 leaf-6 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-5 leaf-6 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-1 leaf-7 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-4 leaf-7 leaf-8)"
operator|+
literal|" (or leaf-0 leaf-5 leaf-7 leaf-8))"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression8
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* first_name = last_name */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>first_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>string</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>last_name</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> "
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"YES_NO_NULL"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression9
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* first_name = last_name */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>1</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>3</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBridge\"> \n"
operator|+
literal|"<void property=\"operator\"> \n"
operator|+
literal|"<boolean>true</boolean> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfClassName\"> \n"
operator|+
literal|"<string>org.apache.hadoop.hive.ql.udf.UDFOPPlus</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfName\"> \n"
operator|+
literal|"<string>+</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>4</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFBridge\"> \n"
operator|+
literal|"<void property=\"operator\"> \n"
operator|+
literal|"<boolean>true</boolean> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfClassName\"> \n"
operator|+
literal|"<string>org.apache.hadoop.hive.ql.udf.UDFOPPlus</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"udfName\"> \n"
operator|+
literal|"<string>+</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqual\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java> "
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"YES_NO_NULL"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpression10
parameter_list|()
throws|throws
name|Exception
block|{
comment|/* id>= 10 and not (10> id) */
name|String
name|exprStr
init|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
operator|+
literal|"<java version=\"1.6.0_31\" class=\"java.beans.XMLDecoder\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo0\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>int</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>10</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPEqualOrGreaterThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object id=\"PrimitiveTypeInfo1\" class=\"org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo\"> \n"
operator|+
literal|"<void property=\"typeName\"> \n"
operator|+
literal|"<string>boolean</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeGenericFuncDesc\"> \n"
operator|+
literal|"<void property=\"children\"> \n"
operator|+
literal|"<object class=\"java.util.ArrayList\"> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeColumnDesc\"> \n"
operator|+
literal|"<void property=\"column\"> \n"
operator|+
literal|"<string>id</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"tabAlias\"> \n"
operator|+
literal|"<string>orc_people</string> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void method=\"add\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.plan.ExprNodeConstantDesc\"> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo0\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"value\"> \n"
operator|+
literal|"<int>10</int> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPLessThan\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPNot\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"genericUDF\"> \n"
operator|+
literal|"<object class=\"org.apache.hadoop.hive.ql.udf.generic.GenericUDFOPAnd\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"<void property=\"typeInfo\"> \n"
operator|+
literal|"<object idref=\"PrimitiveTypeInfo1\"/> \n"
operator|+
literal|"</void> \n"
operator|+
literal|"</object> \n"
operator|+
literal|"</java>"
decl_stmt|;
name|SearchArgumentImpl
name|sarg
init|=
operator|(
name|SearchArgumentImpl
operator|)
name|ConvertAstToSearchArg
operator|.
name|create
argument_list|(
name|getFuncDesc
argument_list|(
name|exprStr
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|MessageType
name|schema
init|=
name|MessageTypeParser
operator|.
name|parseMessageType
argument_list|(
literal|"message test { required int32 id;"
operator|+
literal|" required binary first_name; }"
argument_list|)
decl_stmt|;
name|FilterPredicate
name|p
init|=
name|ParquetFilterPredicateConverter
operator|.
name|toFilterPredicate
argument_list|(
name|sarg
argument_list|,
name|schema
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"and(not(lt(id, 10)), not(lt(id, 10)))"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Operator
operator|.
name|LESS_THAN
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOperator
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10L
argument_list|,
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(and (not leaf-0) (not leaf-0))"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoSharedNodes
argument_list|(
name|sarg
operator|.
name|getExpression
argument_list|()
argument_list|,
name|Sets
operator|.
expr|<
name|ExpressionTree
operator|>
name|newIdentityHashSet
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES_NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|NO_NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NO
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES_NO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|,
name|sarg
operator|.
name|evaluate
argument_list|(
name|values
argument_list|(
name|TruthValue
operator|.
name|YES_NO_NULL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|TruthValue
index|[]
name|values
parameter_list|(
name|TruthValue
modifier|...
name|vals
parameter_list|)
block|{
return|return
name|vals
return|;
block|}
comment|// The following tests use serialized ASTs that I generated using Hive from
comment|// branch-0.14.
annotation|@
name|Test
specifier|public
name|void
name|TestTimestampSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLn"
operator|+
literal|"BsYW4uRXhwck5vZGVDb2x1bW5EZXPjAQF08wAAAWJpZ29y4wECb3JnLmFwYWNoZS5o"
operator|+
literal|"YWRvb3AuaGl2ZS5zZXJkZTIudHlwZWluZm8uUHJpbWl0aXZlVHlwZUluZu8BAXRpbW"
operator|+
literal|"VzdGFt8AEDb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5xbC5wbGFuLkV4cHJOb2RlQ29u"
operator|+
literal|"c3RhbnREZXPjAQECAQFzdHJpbucDATIwMTUtMDMtMTcgMTI6MzQ6NbYBBG9yZy5hcG"
operator|+
literal|"FjaGUuaGFkb29wLmhpdmUucWwudWRmLmdlbmVyaWMuR2VuZXJpY1VERk9QRXF1YewB"
operator|+
literal|"AAABgj0BRVFVQcwBBW9yZy5hcGFjaGUuaGFkb29wLmlvLkJvb2xlYW5Xcml0YWJs5Q"
operator|+
literal|"EAAAECAQFib29sZWHu"
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|TIMESTAMP
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS ts 2015-03-17 12:34:56.0)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestDateSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQFk9AAAAWJpZ29y4wECb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5zZXJkZT"
operator|+
literal|"IudHlwZWluZm8uUHJpbWl0aXZlVHlwZUluZu8BAWRhdOUBA29yZy5hcGFjaGUuaGFkb29wLmhpdmUuc"
operator|+
literal|"WwucGxhbi5FeHByTm9kZUNvbnN0YW50RGVz4wEBAgEBc3RyaW7nAwEyMDE1LTA1LTC1AQRvcmcuYXBh"
operator|+
literal|"Y2hlLmhhZG9vcC5oaXZlLnFsLnVkZi5nZW5lcmljLkdlbmVyaWNVREZPUEVxdWHsAQAAAYI9AUVRVUH"
operator|+
literal|"MAQVvcmcuYXBhY2hlLmhhZG9vcC5pby5Cb29sZWFuV3JpdGFibOUBAAABAgEBYm9vbGVh7g=="
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DATE
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS dt 2015-05-05)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestDecimalSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQFkZeMAAAFiaWdvcuMBAm9yZy5hcGFjaGUuaGFkb29wLmhpdmUuc2VyZG"
operator|+
literal|"UyLnR5cGVpbmZvLkRlY2ltYWxUeXBlSW5m7wEUAAFkZWNpbWHsAQNvcmcuYXBhY2hlLmhhZG9vcC5oa"
operator|+
literal|"XZlLnFsLnBsYW4uRXhwck5vZGVDb25zdGFudERlc+MBAQRvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnNl"
operator|+
literal|"cmRlMi50eXBlaW5mby5QcmltaXRpdmVUeXBlSW5m7wEBaW70AvYBAQVvcmcuYXBhY2hlLmhhZG9vcC5"
operator|+
literal|"oaXZlLnFsLnVkZi5nZW5lcmljLkdlbmVyaWNVREZPUEVxdWHsAQAAAYI9AUVRVUHMAQZvcmcuYXBhY2"
operator|+
literal|"hlLmhhZG9vcC5pby5Cb29sZWFuV3JpdGFibOUBAAABBAEBYm9vbGVh7g=="
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|DECIMAL
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS dec 123)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestCharSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQFj6AAAAWJpZ29y4wECb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5zZXJkZT"
operator|+
literal|"IudHlwZWluZm8uQ2hhclR5cGVJbmbvARQBY2hh8gEDb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5xbC5wb"
operator|+
literal|"GFuLkV4cHJOb2RlQ29uc3RhbnREZXPjAQEEb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5zZXJkZTIudHlw"
operator|+
literal|"ZWluZm8uUHJpbWl0aXZlVHlwZUluZu8BAXN0cmlu5wMBY2hhciAgICAgoAEFb3JnLmFwYWNoZS5oYWR"
operator|+
literal|"vb3AuaGl2ZS5xbC51ZGYuZ2VuZXJpYy5HZW5lcmljVURGT1BFcXVh7AEAAAGCPQFFUVVBzAEGb3JnLm"
operator|+
literal|"FwYWNoZS5oYWRvb3AuaW8uQm9vbGVhbldyaXRhYmzlAQAAAQQBAWJvb2xlYe4="
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS ch char      )"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestVarcharSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQF24wAAAWJpZ29y4wECb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5zZXJkZT"
operator|+
literal|"IudHlwZWluZm8uVmFyY2hhclR5cGVJbmbvAcgBAXZhcmNoYfIBA29yZy5hcGFjaGUuaGFkb29wLmhpd"
operator|+
literal|"mUucWwucGxhbi5FeHByTm9kZUNvbnN0YW50RGVz4wEBBG9yZy5hcGFjaGUuaGFkb29wLmhpdmUuc2Vy"
operator|+
literal|"ZGUyLnR5cGVpbmZvLlByaW1pdGl2ZVR5cGVJbmbvAQFzdHJpbucDAXZhcmlhYmzlAQVvcmcuYXBhY2h"
operator|+
literal|"lLmhhZG9vcC5oaXZlLnFsLnVkZi5nZW5lcmljLkdlbmVyaWNVREZPUEVxdWHsAQAAAYI9AUVRVUHMAQ"
operator|+
literal|"ZvcmcuYXBhY2hlLmhhZG9vcC5pby5Cb29sZWFuV3JpdGFibOUBAAABBAEBYm9vbGVh7g=="
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|STRING
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS vc variable)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestBigintSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQFi6QAAAWJpZ29y4wECb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5zZXJkZT"
operator|+
literal|"IudHlwZWluZm8uUHJpbWl0aXZlVHlwZUluZu8BAWJpZ2lu9AEDb3JnLmFwYWNoZS5oYWRvb3AuaGl2Z"
operator|+
literal|"S5xbC5wbGFuLkV4cHJOb2RlQ29uc3RhbnREZXPjAQECBwnywAEBBG9yZy5hcGFjaGUuaGFkb29wLmhp"
operator|+
literal|"dmUucWwudWRmLmdlbmVyaWMuR2VuZXJpY1VERk9QRXF1YewBAAABgj0BRVFVQcwBBW9yZy5hcGFjaGU"
operator|+
literal|"uaGFkb29wLmlvLkJvb2xlYW5Xcml0YWJs5QEAAAECAQFib29sZWHu"
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|LONG
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS bi 12345)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestBooleanSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVHZW5lcmljRnVuY0Rlc+MBAQABAgECb3JnLmFwYWNoZS5oYWRvb3AuaGl2ZS5xbC5wbGFuLk"
operator|+
literal|"V4cHJOb2RlQ29sdW1uRGVz4wEBYrEAAAFib29sb3LjAQNvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnNlc"
operator|+
literal|"mRlMi50eXBlaW5mby5QcmltaXRpdmVUeXBlSW5m7wEBYm9vbGVh7gEEb3JnLmFwYWNoZS5oYWRvb3Au"
operator|+
literal|"aGl2ZS5xbC5wbGFuLkV4cHJOb2RlQ29uc3RhbnREZXPjAQEDCQUBAQVvcmcuYXBhY2hlLmhhZG9vcC5"
operator|+
literal|"oaXZlLnFsLnVkZi5nZW5lcmljLkdlbmVyaWNVREZPUEVxdWHsAQAAAYI9AUVRVUHMAQZvcmcuYXBhY2"
operator|+
literal|"hlLmhhZG9vcC5pby5Cb29sZWFuV3JpdGFibOUBAAABAwkBAgEBYrIAAAgBAwkBB29yZy5hcGFjaGUua"
operator|+
literal|"GFkb29wLmhpdmUucWwudWRmLmdlbmVyaWMuR2VuZXJpY1VERk9QQW7kAQEGAQAAAQMJ"
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"(and leaf-0 leaf-1)"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|BOOLEAN
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS b1 true)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|BOOLEAN
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS b2 true)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestFloatSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQFmbPQAAAFiaWdvcuMBAm9yZy5hcGFjaGUuaGFkb29wLmhpdmUuc2VyZG"
operator|+
literal|"UyLnR5cGVpbmZvLlByaW1pdGl2ZVR5cGVJbmbvAQFmbG9h9AEDb3JnLmFwYWNoZS5oYWRvb3AuaGl2Z"
operator|+
literal|"S5xbC5wbGFuLkV4cHJOb2RlQ29uc3RhbnREZXPjAQECBwQ/jMzNAQRvcmcuYXBhY2hlLmhhZG9vcC5o"
operator|+
literal|"aXZlLnFsLnVkZi5nZW5lcmljLkdlbmVyaWNVREZPUEVxdWHsAQAAAYI9AUVRVUHMAQVvcmcuYXBhY2h"
operator|+
literal|"lLmhhZG9vcC5pby5Cb29sZWFuV3JpdGFibOUBAAABAgEBYm9vbGVh7g=="
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|FLOAT
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS flt 1.1)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestDoubleSarg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|serialAst
init|=
literal|"AQEAamF2YS51dGlsLkFycmF5TGlz9AECAQFvcmcuYXBhY2hlLmhhZG9vcC5oaXZlLnFsLnBsYW4uRXh"
operator|+
literal|"wck5vZGVDb2x1bW5EZXPjAQFkYuwAAAFiaWdvcuMBAm9yZy5hcGFjaGUuaGFkb29wLmhpdmUuc2VyZG"
operator|+
literal|"UyLnR5cGVpbmZvLlByaW1pdGl2ZVR5cGVJbmbvAQFkb3VibOUBA29yZy5hcGFjaGUuaGFkb29wLmhpd"
operator|+
literal|"mUucWwucGxhbi5FeHByTm9kZUNvbnN0YW50RGVz4wEBAgcKQAGZmZmZmZoBBG9yZy5hcGFjaGUuaGFk"
operator|+
literal|"b29wLmhpdmUucWwudWRmLmdlbmVyaWMuR2VuZXJpY1VERk9QRXF1YewBAAABgj0BRVFVQcwBBW9yZy5"
operator|+
literal|"hcGFjaGUuaGFkb29wLmlvLkJvb2xlYW5Xcml0YWJs5QEAAAECAQFib29sZWHu"
decl_stmt|;
name|SearchArgument
name|sarg
init|=
operator|new
name|ConvertAstToSearchArg
argument_list|(
name|SerializationUtilities
operator|.
name|deserializeExpression
argument_list|(
name|serialAst
argument_list|)
argument_list|)
operator|.
name|buildSearchArgument
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"leaf-0"
argument_list|,
name|sarg
operator|.
name|getExpression
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|PredicateLeaf
name|leaf
init|=
name|sarg
operator|.
name|getLeaves
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|PredicateLeaf
operator|.
name|Type
operator|.
name|FLOAT
argument_list|,
name|leaf
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"(EQUALS dbl 2.2)"
argument_list|,
name|leaf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


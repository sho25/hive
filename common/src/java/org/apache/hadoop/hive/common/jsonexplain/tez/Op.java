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
name|common
operator|.
name|jsonexplain
operator|.
name|tez
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|Op
block|{
specifier|public
specifier|final
name|String
name|name
decl_stmt|;
comment|//tezJsonParser
specifier|public
specifier|final
name|TezJsonParser
name|parser
decl_stmt|;
specifier|public
specifier|final
name|String
name|operatorId
decl_stmt|;
specifier|public
name|Op
name|parent
decl_stmt|;
specifier|public
specifier|final
name|List
argument_list|<
name|Op
argument_list|>
name|children
decl_stmt|;
specifier|public
specifier|final
name|List
argument_list|<
name|Attr
argument_list|>
name|attrs
decl_stmt|;
comment|// the jsonObject for this operator
specifier|public
specifier|final
name|JSONObject
name|opObject
decl_stmt|;
comment|// the vertex that this operator belongs to
specifier|public
specifier|final
name|Vertex
name|vertex
decl_stmt|;
comment|// the vertex that this operator output to if this operator is a
comment|// ReduceOutputOperator
specifier|public
specifier|final
name|String
name|outputVertexName
decl_stmt|;
specifier|public
name|Op
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|outputVertexName
parameter_list|,
name|List
argument_list|<
name|Op
argument_list|>
name|children
parameter_list|,
name|List
argument_list|<
name|Attr
argument_list|>
name|attrs
parameter_list|,
name|JSONObject
name|opObject
parameter_list|,
name|Vertex
name|vertex
parameter_list|,
name|TezJsonParser
name|tezJsonParser
parameter_list|)
throws|throws
name|JSONException
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|operatorId
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|outputVertexName
operator|=
name|outputVertexName
expr_stmt|;
name|this
operator|.
name|children
operator|=
name|children
expr_stmt|;
name|this
operator|.
name|attrs
operator|=
name|attrs
expr_stmt|;
name|this
operator|.
name|opObject
operator|=
name|opObject
expr_stmt|;
name|this
operator|.
name|vertex
operator|=
name|vertex
expr_stmt|;
name|this
operator|.
name|parser
operator|=
name|tezJsonParser
expr_stmt|;
block|}
specifier|private
name|void
name|inlineJoinOp
parameter_list|()
throws|throws
name|Exception
block|{
comment|// inline map join operator
if|if
condition|(
name|this
operator|.
name|name
operator|.
name|equals
argument_list|(
literal|"Map Join Operator"
argument_list|)
condition|)
block|{
name|JSONObject
name|mapjoinObj
init|=
name|opObject
operator|.
name|getJSONObject
argument_list|(
literal|"Map Join Operator"
argument_list|)
decl_stmt|;
comment|// get the map for posToVertex
name|JSONObject
name|verticeObj
init|=
name|mapjoinObj
operator|.
name|getJSONObject
argument_list|(
literal|"input vertices:"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|posToVertex
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|pos
range|:
name|JSONObject
operator|.
name|getNames
argument_list|(
name|verticeObj
argument_list|)
control|)
block|{
name|String
name|vertexName
init|=
name|verticeObj
operator|.
name|getString
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|posToVertex
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|vertexName
argument_list|)
expr_stmt|;
comment|// update the connection
name|Connection
name|c
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Connection
name|connection
range|:
name|vertex
operator|.
name|parentConnections
control|)
block|{
if|if
condition|(
name|connection
operator|.
name|from
operator|.
name|name
operator|.
name|equals
argument_list|(
name|vertexName
argument_list|)
condition|)
block|{
name|c
operator|=
name|connection
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|addInline
argument_list|(
name|this
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
block|}
comment|// update the attrs
name|removeAttr
argument_list|(
literal|"input vertices:"
argument_list|)
expr_stmt|;
comment|// update the keys to use vertex name
name|JSONObject
name|keys
init|=
name|mapjoinObj
operator|.
name|getJSONObject
argument_list|(
literal|"keys:"
argument_list|)
decl_stmt|;
if|if
condition|(
name|keys
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|JSONObject
name|newKeys
init|=
operator|new
name|JSONObject
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|JSONObject
operator|.
name|getNames
argument_list|(
name|keys
argument_list|)
control|)
block|{
name|String
name|vertexName
init|=
name|posToVertex
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|vertexName
operator|!=
literal|null
condition|)
block|{
name|newKeys
operator|.
name|put
argument_list|(
name|vertexName
argument_list|,
name|keys
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newKeys
operator|.
name|put
argument_list|(
name|this
operator|.
name|vertex
operator|.
name|name
argument_list|,
name|keys
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// update the attrs
name|removeAttr
argument_list|(
literal|"keys:"
argument_list|)
expr_stmt|;
name|this
operator|.
name|attrs
operator|.
name|add
argument_list|(
operator|new
name|Attr
argument_list|(
literal|"keys:"
argument_list|,
name|newKeys
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// inline merge join operator in a self-join
else|else
block|{
if|if
condition|(
name|this
operator|.
name|vertex
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Vertex
name|v
range|:
name|this
operator|.
name|vertex
operator|.
name|mergeJoinDummyVertexs
control|)
block|{
name|parser
operator|.
name|addInline
argument_list|(
name|this
argument_list|,
operator|new
name|Connection
argument_list|(
literal|null
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|String
name|getNameWithOpId
parameter_list|()
block|{
if|if
condition|(
name|operatorId
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|name
operator|+
literal|" ["
operator|+
name|operatorId
operator|+
literal|"]"
return|;
block|}
else|else
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
block|}
comment|/**    * @param out    * @param indentFlag    * @param branchOfJoinOp    *          This parameter is used to show if it is a branch of a Join    *          operator so that we can decide the corresponding indent.    * @throws Exception    */
specifier|public
name|void
name|print
parameter_list|(
name|Printer
name|printer
parameter_list|,
name|List
argument_list|<
name|Boolean
argument_list|>
name|indentFlag
parameter_list|,
name|boolean
name|branchOfJoinOp
parameter_list|)
throws|throws
name|Exception
block|{
comment|// print name
if|if
condition|(
name|parser
operator|.
name|printSet
operator|.
name|contains
argument_list|(
name|this
argument_list|)
condition|)
block|{
name|printer
operator|.
name|println
argument_list|(
name|TezJsonParser
operator|.
name|prefixString
argument_list|(
name|indentFlag
argument_list|)
operator|+
literal|" Please refer to the previous "
operator|+
name|this
operator|.
name|getNameWithOpId
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|parser
operator|.
name|printSet
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|branchOfJoinOp
condition|)
block|{
name|printer
operator|.
name|println
argument_list|(
name|TezJsonParser
operator|.
name|prefixString
argument_list|(
name|indentFlag
argument_list|)
operator|+
name|this
operator|.
name|getNameWithOpId
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|printer
operator|.
name|println
argument_list|(
name|TezJsonParser
operator|.
name|prefixString
argument_list|(
name|indentFlag
argument_list|,
literal|"|<-"
argument_list|)
operator|+
name|this
operator|.
name|getNameWithOpId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|branchOfJoinOp
operator|=
literal|false
expr_stmt|;
comment|// if this operator is a Map Join Operator or a Merge Join Operator
if|if
condition|(
name|this
operator|.
name|name
operator|.
name|equals
argument_list|(
literal|"Map Join Operator"
argument_list|)
operator|||
name|this
operator|.
name|name
operator|.
name|equals
argument_list|(
literal|"Merge Join Operator"
argument_list|)
condition|)
block|{
name|inlineJoinOp
argument_list|()
expr_stmt|;
name|branchOfJoinOp
operator|=
literal|true
expr_stmt|;
block|}
comment|// if this operator is the last operator, we summarize the non-inlined
comment|// vertex
name|List
argument_list|<
name|Connection
argument_list|>
name|noninlined
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|parent
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|vertex
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Connection
name|connection
range|:
name|this
operator|.
name|vertex
operator|.
name|parentConnections
control|)
block|{
if|if
condition|(
operator|!
name|parser
operator|.
name|isInline
argument_list|(
name|connection
operator|.
name|from
argument_list|)
condition|)
block|{
name|noninlined
operator|.
name|add
argument_list|(
name|connection
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// print attr
name|List
argument_list|<
name|Boolean
argument_list|>
name|attFlag
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|attFlag
operator|.
name|addAll
argument_list|(
name|indentFlag
argument_list|)
expr_stmt|;
comment|// should print | if (1) it is branchOfJoinOp or (2) it is the last op and
comment|// has following non-inlined vertex
if|if
condition|(
name|branchOfJoinOp
operator|||
operator|(
name|this
operator|.
name|parent
operator|==
literal|null
operator|&&
operator|!
name|noninlined
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|attFlag
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|attFlag
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|attrs
argument_list|)
expr_stmt|;
for|for
control|(
name|Attr
name|attr
range|:
name|attrs
control|)
block|{
name|printer
operator|.
name|println
argument_list|(
name|TezJsonParser
operator|.
name|prefixString
argument_list|(
name|attFlag
argument_list|)
operator|+
name|attr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// print inline vertex
if|if
condition|(
name|parser
operator|.
name|inlineMap
operator|.
name|containsKey
argument_list|(
name|this
argument_list|)
condition|)
block|{
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|parser
operator|.
name|inlineMap
operator|.
name|get
argument_list|(
name|this
argument_list|)
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|Connection
name|connection
init|=
name|parser
operator|.
name|inlineMap
operator|.
name|get
argument_list|(
name|this
argument_list|)
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Boolean
argument_list|>
name|vertexFlag
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|vertexFlag
operator|.
name|addAll
argument_list|(
name|indentFlag
argument_list|)
expr_stmt|;
if|if
condition|(
name|branchOfJoinOp
condition|)
block|{
name|vertexFlag
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// if there is an inline vertex but the operator itself is not on a join
comment|// branch,
comment|// then it means it is from a vertex created by an operator tree,
comment|// e.g., fetch operator, etc.
else|else
block|{
name|vertexFlag
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|connection
operator|.
name|from
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|vertexFlag
argument_list|,
name|connection
operator|.
name|type
argument_list|,
name|this
operator|.
name|vertex
argument_list|)
expr_stmt|;
block|}
block|}
comment|// print parent op, i.e., where data comes from
if|if
condition|(
name|this
operator|.
name|parent
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Boolean
argument_list|>
name|parentFlag
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|parentFlag
operator|.
name|addAll
argument_list|(
name|indentFlag
argument_list|)
expr_stmt|;
name|parentFlag
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|parentFlag
argument_list|,
name|branchOfJoinOp
argument_list|)
expr_stmt|;
block|}
comment|// print next vertex
else|else
block|{
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|noninlined
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|Vertex
name|v
init|=
name|noninlined
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|from
decl_stmt|;
name|List
argument_list|<
name|Boolean
argument_list|>
name|vertexFlag
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|vertexFlag
operator|.
name|addAll
argument_list|(
name|indentFlag
argument_list|)
expr_stmt|;
if|if
condition|(
name|index
operator|!=
name|noninlined
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|)
block|{
name|vertexFlag
operator|.
name|add
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|vertexFlag
operator|.
name|add
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|v
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|vertexFlag
argument_list|,
name|noninlined
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|type
argument_list|,
name|this
operator|.
name|vertex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|removeAttr
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|int
name|removeIndex
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|attrs
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|attrs
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|name
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|removeIndex
operator|=
name|index
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|removeIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|attrs
operator|.
name|remove
argument_list|(
name|removeIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|common
operator|.
name|jsonexplain
operator|.
name|Vertex
operator|.
name|VertexType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONArray
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
comment|// tezJsonParser
specifier|public
specifier|final
name|DagJsonParser
name|parser
decl_stmt|;
specifier|public
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
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
comment|// the vertex that this operator output to
specifier|public
name|String
name|outputVertexName
decl_stmt|;
comment|// the Operator type
specifier|public
name|OpType
name|type
decl_stmt|;
specifier|public
enum|enum
name|OpType
block|{
name|MAPJOIN
block|,
name|MERGEJOIN
block|,
name|RS
block|,
name|OTHERS
block|}
empty_stmt|;
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
name|Op
name|parent
parameter_list|,
name|List
argument_list|<
name|Op
argument_list|>
name|children
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attrs
parameter_list|,
name|JSONObject
name|opObject
parameter_list|,
name|Vertex
name|vertex
parameter_list|,
name|DagJsonParser
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
name|type
operator|=
name|deriveOpType
argument_list|(
name|operatorId
argument_list|)
expr_stmt|;
name|this
operator|.
name|outputVertexName
operator|=
name|outputVertexName
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
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
name|OpType
name|deriveOpType
parameter_list|(
name|String
name|operatorId
parameter_list|)
block|{
if|if
condition|(
name|operatorId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|operatorId
operator|.
name|startsWith
argument_list|(
name|OpType
operator|.
name|MAPJOIN
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|OpType
operator|.
name|MAPJOIN
return|;
block|}
elseif|else
if|if
condition|(
name|operatorId
operator|.
name|startsWith
argument_list|(
name|OpType
operator|.
name|MERGEJOIN
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|OpType
operator|.
name|MERGEJOIN
return|;
block|}
elseif|else
if|if
condition|(
name|operatorId
operator|.
name|startsWith
argument_list|(
name|OpType
operator|.
name|RS
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|OpType
operator|.
name|RS
return|;
block|}
else|else
block|{
return|return
name|OpType
operator|.
name|OTHERS
return|;
block|}
block|}
else|else
block|{
return|return
name|OpType
operator|.
name|OTHERS
return|;
block|}
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
name|type
operator|==
name|OpType
operator|.
name|MAPJOIN
condition|)
block|{
comment|// get the map for posToVertex
name|Map
argument_list|<
name|String
argument_list|,
name|Vertex
argument_list|>
name|posToVertex
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|opObject
operator|.
name|has
argument_list|(
literal|"input vertices:"
argument_list|)
condition|)
block|{
name|JSONObject
name|verticeObj
init|=
name|opObject
operator|.
name|getJSONObject
argument_list|(
literal|"input vertices:"
argument_list|)
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
name|posToVertex
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|connection
operator|.
name|from
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|attrs
operator|.
name|remove
argument_list|(
literal|"input vertices:"
argument_list|)
expr_stmt|;
block|}
comment|// update the keys to use operator name
name|JSONObject
name|keys
init|=
name|opObject
operator|.
name|getJSONObject
argument_list|(
literal|"keys:"
argument_list|)
decl_stmt|;
comment|// find out the vertex for the big table
name|Set
argument_list|<
name|Vertex
argument_list|>
name|parentVertexes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
name|parentVertexes
operator|.
name|add
argument_list|(
name|connection
operator|.
name|from
argument_list|)
expr_stmt|;
block|}
name|parentVertexes
operator|.
name|removeAll
argument_list|(
name|posToVertex
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|posToOpId
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
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
comment|// first search from the posToVertex
if|if
condition|(
name|posToVertex
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|Vertex
name|v
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
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|v
operator|.
name|outputOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|v
operator|.
name|vertexType
operator|==
name|VertexType
operator|.
name|UNION
operator|)
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|v
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Op
name|joinRSOp
init|=
name|v
operator|.
name|getJoinRSOp
argument_list|(
name|vertex
argument_list|)
decl_stmt|;
if|if
condition|(
name|joinRSOp
operator|!=
literal|null
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|joinRSOp
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find join reduceSinkOp for "
operator|+
name|v
operator|.
name|name
operator|+
literal|" to join "
operator|+
name|vertex
operator|.
name|name
operator|+
literal|" when hive explain user is trying to identify the operator id."
argument_list|)
throw|;
block|}
block|}
block|}
comment|// then search from parent
elseif|else
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|parent
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
comment|// then assume it is from its own vertex
elseif|else
if|if
condition|(
name|parentVertexes
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|Vertex
name|v
init|=
name|parentVertexes
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|parentVertexes
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|v
operator|.
name|outputOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|v
operator|.
name|vertexType
operator|==
name|VertexType
operator|.
name|UNION
operator|)
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|v
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Op
name|joinRSOp
init|=
name|v
operator|.
name|getJoinRSOp
argument_list|(
name|vertex
argument_list|)
decl_stmt|;
if|if
condition|(
name|joinRSOp
operator|!=
literal|null
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|joinRSOp
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find join reduceSinkOp for "
operator|+
name|v
operator|.
name|name
operator|+
literal|" to join "
operator|+
name|vertex
operator|.
name|name
operator|+
literal|" when hive explain user is trying to identify the operator id."
argument_list|)
throw|;
block|}
block|}
block|}
comment|// finally throw an exception
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find the source operator on one of the branches of map join."
argument_list|)
throw|;
block|}
block|}
block|}
name|this
operator|.
name|attrs
operator|.
name|remove
argument_list|(
literal|"keys:"
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|JSONArray
name|conditionMap
init|=
name|opObject
operator|.
name|getJSONArray
argument_list|(
literal|"condition map:"
argument_list|)
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
name|conditionMap
operator|.
name|length
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|JSONObject
name|cond
init|=
name|conditionMap
operator|.
name|getJSONObject
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|String
name|k
init|=
operator|(
name|String
operator|)
name|cond
operator|.
name|keys
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|JSONObject
name|condObject
init|=
operator|new
name|JSONObject
argument_list|(
operator|(
name|String
operator|)
name|cond
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|condObject
operator|.
name|getString
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|String
name|left
init|=
name|condObject
operator|.
name|getString
argument_list|(
literal|"left"
argument_list|)
decl_stmt|;
name|String
name|right
init|=
name|condObject
operator|.
name|getString
argument_list|(
literal|"right"
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
name|sb
operator|.
name|append
argument_list|(
name|posToOpId
operator|.
name|get
argument_list|(
name|left
argument_list|)
operator|+
literal|"."
operator|+
name|keys
operator|.
name|get
argument_list|(
name|left
argument_list|)
operator|+
literal|"="
operator|+
name|posToOpId
operator|.
name|get
argument_list|(
name|right
argument_list|)
operator|+
literal|"."
operator|+
name|keys
operator|.
name|get
argument_list|(
name|right
argument_list|)
operator|+
literal|"("
operator|+
name|type
operator|+
literal|"),"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// probably a cross product
name|sb
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|type
operator|+
literal|"),"
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|attrs
operator|.
name|remove
argument_list|(
literal|"condition map:"
argument_list|)
expr_stmt|;
name|this
operator|.
name|attrs
operator|.
name|put
argument_list|(
literal|"Conds:"
argument_list|,
name|sb
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// should be merge join
else|else
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|posToOpId
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|vertex
operator|.
name|mergeJoinDummyVertexs
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|vertex
operator|.
name|tagToInput
operator|.
name|entrySet
argument_list|()
control|)
block|{
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
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|Vertex
name|v
init|=
name|connection
operator|.
name|from
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|v
operator|.
name|outputOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|v
operator|.
name|vertexType
operator|==
name|VertexType
operator|.
name|UNION
operator|)
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|v
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Op
name|joinRSOp
init|=
name|v
operator|.
name|getJoinRSOp
argument_list|(
name|vertex
argument_list|)
decl_stmt|;
if|if
condition|(
name|joinRSOp
operator|!=
literal|null
condition|)
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|joinRSOp
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find join reduceSinkOp for "
operator|+
name|v
operator|.
name|name
operator|+
literal|" to join "
operator|+
name|vertex
operator|.
name|name
operator|+
literal|" when hive explain user is trying to identify the operator id."
argument_list|)
throw|;
block|}
block|}
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
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find "
operator|+
name|entry
operator|.
name|getValue
argument_list|()
operator|+
literal|" while parsing keys of merge join operator"
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
name|posToOpId
operator|.
name|put
argument_list|(
name|vertex
operator|.
name|tag
argument_list|,
name|this
operator|.
name|parent
operator|.
name|operatorId
argument_list|)
expr_stmt|;
for|for
control|(
name|Vertex
name|v
range|:
name|vertex
operator|.
name|mergeJoinDummyVertexs
control|)
block|{
if|if
condition|(
name|v
operator|.
name|outputOps
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find a single root operators in a single vertex "
operator|+
name|v
operator|.
name|name
operator|+
literal|" when hive explain user is trying to identify the operator id."
argument_list|)
throw|;
block|}
name|posToOpId
operator|.
name|put
argument_list|(
name|v
operator|.
name|tag
argument_list|,
name|v
operator|.
name|outputOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
block|}
comment|// update the keys to use operator name
name|JSONObject
name|keys
init|=
name|opObject
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
if|if
condition|(
operator|!
name|posToOpId
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find the source operator on one of the branches of merge join."
argument_list|)
throw|;
block|}
block|}
comment|// inline merge join operator in a self-join
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
comment|// update the attrs
name|this
operator|.
name|attrs
operator|.
name|remove
argument_list|(
literal|"keys:"
argument_list|)
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|JSONArray
name|conditionMap
init|=
name|opObject
operator|.
name|getJSONArray
argument_list|(
literal|"condition map:"
argument_list|)
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
name|conditionMap
operator|.
name|length
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|JSONObject
name|cond
init|=
name|conditionMap
operator|.
name|getJSONObject
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|String
name|k
init|=
operator|(
name|String
operator|)
name|cond
operator|.
name|keys
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|JSONObject
name|condObject
init|=
operator|new
name|JSONObject
argument_list|(
operator|(
name|String
operator|)
name|cond
operator|.
name|get
argument_list|(
name|k
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|condObject
operator|.
name|getString
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|String
name|left
init|=
name|condObject
operator|.
name|getString
argument_list|(
literal|"left"
argument_list|)
decl_stmt|;
name|String
name|right
init|=
name|condObject
operator|.
name|getString
argument_list|(
literal|"right"
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
name|sb
operator|.
name|append
argument_list|(
name|posToOpId
operator|.
name|get
argument_list|(
name|left
argument_list|)
operator|+
literal|"."
operator|+
name|keys
operator|.
name|get
argument_list|(
name|left
argument_list|)
operator|+
literal|"="
operator|+
name|posToOpId
operator|.
name|get
argument_list|(
name|right
argument_list|)
operator|+
literal|"."
operator|+
name|keys
operator|.
name|get
argument_list|(
name|right
argument_list|)
operator|+
literal|"("
operator|+
name|type
operator|+
literal|"),"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// probably a cross product
name|sb
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|type
operator|+
literal|"),"
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|attrs
operator|.
name|remove
argument_list|(
literal|"condition map:"
argument_list|)
expr_stmt|;
name|this
operator|.
name|attrs
operator|.
name|put
argument_list|(
literal|"Conds:"
argument_list|,
name|sb
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|getNameWithOpIdStats
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|DagJsonParserUtils
operator|.
name|renameReduceOutputOperator
argument_list|(
name|name
argument_list|,
name|vertex
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|operatorId
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" ["
operator|+
name|operatorId
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|DagJsonParserUtils
operator|.
name|OperatorNoStats
operator|.
name|contains
argument_list|(
name|name
argument_list|)
operator|&&
name|attrs
operator|.
name|containsKey
argument_list|(
literal|"Statistics:"
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" ("
operator|+
name|attrs
operator|.
name|get
argument_list|(
literal|"Statistics:"
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|attrs
operator|.
name|remove
argument_list|(
literal|"Statistics:"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @param printer    * @param indentFlag    * @param branchOfJoinOp    *          This parameter is used to show if it is a branch of a Join    *          operator so that we can decide the corresponding indent.    * @throws Exception    */
specifier|public
name|void
name|print
parameter_list|(
name|Printer
name|printer
parameter_list|,
name|int
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
name|DagJsonParser
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
name|getNameWithOpIdStats
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
name|DagJsonParser
operator|.
name|prefixString
argument_list|(
name|indentFlag
argument_list|)
operator|+
name|this
operator|.
name|getNameWithOpIdStats
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
name|DagJsonParser
operator|.
name|prefixString
argument_list|(
name|indentFlag
argument_list|,
literal|"<-"
argument_list|)
operator|+
name|this
operator|.
name|getNameWithOpIdStats
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
name|type
operator|==
name|OpType
operator|.
name|MAPJOIN
operator|||
name|this
operator|.
name|type
operator|==
name|OpType
operator|.
name|MERGEJOIN
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
name|indentFlag
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|attrs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|printer
operator|.
name|println
argument_list|(
name|DagJsonParser
operator|.
name|prefixString
argument_list|(
name|indentFlag
argument_list|)
operator|+
name|DagJsonParserUtils
operator|.
name|attrsToString
argument_list|(
name|attrs
argument_list|)
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
name|List
argument_list|<
name|Connection
argument_list|>
name|connections
init|=
name|parser
operator|.
name|inlineMap
operator|.
name|get
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|connections
argument_list|)
expr_stmt|;
for|for
control|(
name|Connection
name|connection
range|:
name|connections
control|)
block|{
name|connection
operator|.
name|from
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|indentFlag
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
name|this
operator|.
name|parent
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|indentFlag
argument_list|,
name|branchOfJoinOp
argument_list|)
expr_stmt|;
block|}
comment|// print next vertex
else|else
block|{
name|Collections
operator|.
name|sort
argument_list|(
name|noninlined
argument_list|)
expr_stmt|;
for|for
control|(
name|Connection
name|connection
range|:
name|noninlined
control|)
block|{
name|connection
operator|.
name|from
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|indentFlag
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
block|}
specifier|public
name|void
name|setOperatorId
parameter_list|(
name|String
name|operatorId
parameter_list|)
block|{
name|this
operator|.
name|operatorId
operator|=
name|operatorId
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|deriveOpType
argument_list|(
name|operatorId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


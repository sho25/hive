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
name|io
operator|.
name|IOException
import|;
end_import

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
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|Op
operator|.
name|OpType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|JsonMappingException
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
specifier|final
class|class
name|Vertex
implements|implements
name|Comparable
argument_list|<
name|Vertex
argument_list|>
block|{
specifier|public
specifier|final
name|String
name|name
decl_stmt|;
comment|// the stage that this vertex belongs to
specifier|public
specifier|final
name|Stage
name|stage
decl_stmt|;
comment|//tezJsonParser
specifier|public
specifier|final
name|DagJsonParser
name|parser
decl_stmt|;
comment|// vertex's parent connections.
specifier|public
specifier|final
name|List
argument_list|<
name|Connection
argument_list|>
name|parentConnections
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// vertex's children vertex.
specifier|public
specifier|final
name|List
argument_list|<
name|Vertex
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// the jsonObject for this vertex
specifier|public
specifier|final
name|JSONObject
name|vertexObject
decl_stmt|;
comment|// whether this vertex is dummy (which does not really exists but is created),
comment|// e.g., a dummy vertex for a mergejoin branch
specifier|public
name|boolean
name|dummy
decl_stmt|;
comment|// the outputOps in this vertex.
specifier|public
specifier|final
name|List
argument_list|<
name|Op
argument_list|>
name|outputOps
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// the inputOps in this vertex.
specifier|public
specifier|final
name|List
argument_list|<
name|Op
argument_list|>
name|inputOps
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// we create a dummy vertex for a mergejoin branch for a self join if this
comment|// vertex is a mergejoin
specifier|public
specifier|final
name|List
argument_list|<
name|Vertex
argument_list|>
name|mergeJoinDummyVertexs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// this vertex has multiple reduce operators
specifier|public
name|int
name|numReduceOp
init|=
literal|0
decl_stmt|;
comment|// execution mode
specifier|public
name|String
name|executionMode
init|=
literal|""
decl_stmt|;
comment|// tagToInput for reduce work
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tagToInput
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|// tag
specifier|public
name|String
name|tag
decl_stmt|;
specifier|protected
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|VertexType
block|{
name|MAP
block|,
name|REDUCE
block|,
name|UNION
block|,
name|UNKNOWN
block|}
empty_stmt|;
specifier|public
name|VertexType
name|vertexType
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|EdgeType
block|{
name|BROADCAST
block|,
name|SHUFFLE
block|,
name|MULTICAST
block|,
name|PARTITION_ONLY_SHUFFLE
block|,
name|FORWARD
block|,
name|XPROD_EDGE
block|,
name|UNKNOWN
block|}
empty_stmt|;
specifier|public
name|String
name|edgeType
decl_stmt|;
specifier|public
name|Vertex
parameter_list|(
name|String
name|name
parameter_list|,
name|JSONObject
name|vertexObject
parameter_list|,
name|Stage
name|stage
parameter_list|,
name|DagJsonParser
name|dagJsonParser
parameter_list|)
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
if|if
condition|(
name|this
operator|.
name|name
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|"Map"
argument_list|)
condition|)
block|{
name|this
operator|.
name|vertexType
operator|=
name|VertexType
operator|.
name|MAP
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|"Reduce"
argument_list|)
condition|)
block|{
name|this
operator|.
name|vertexType
operator|=
name|VertexType
operator|.
name|REDUCE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|"Union"
argument_list|)
condition|)
block|{
name|this
operator|.
name|vertexType
operator|=
name|VertexType
operator|.
name|UNION
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|vertexType
operator|=
name|VertexType
operator|.
name|UNKNOWN
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|vertexType
operator|=
name|VertexType
operator|.
name|UNKNOWN
expr_stmt|;
block|}
name|this
operator|.
name|dummy
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|vertexObject
operator|=
name|vertexObject
expr_stmt|;
name|this
operator|.
name|stage
operator|=
name|stage
expr_stmt|;
name|this
operator|.
name|parser
operator|=
name|dagJsonParser
expr_stmt|;
block|}
specifier|public
name|void
name|addDependency
parameter_list|(
name|Connection
name|connection
parameter_list|)
throws|throws
name|JSONException
block|{
name|this
operator|.
name|parentConnections
operator|.
name|add
argument_list|(
name|connection
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws JSONException    * @throws JsonParseException    * @throws JsonMappingException    * @throws IOException    * @throws Exception    *           We assume that there is a single top-level Map Operator Tree or a    *           Reduce Operator Tree in a vertex    */
specifier|public
name|void
name|extractOpTree
parameter_list|()
throws|throws
name|JSONException
throws|,
name|JsonParseException
throws|,
name|JsonMappingException
throws|,
name|IOException
throws|,
name|Exception
block|{
if|if
condition|(
name|vertexObject
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
name|vertexObject
argument_list|)
control|)
block|{
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"Map Operator Tree:"
argument_list|)
condition|)
block|{
name|extractOp
argument_list|(
name|vertexObject
operator|.
name|getJSONArray
argument_list|(
name|key
argument_list|)
operator|.
name|getJSONObject
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"Reduce Operator Tree:"
argument_list|)
operator|||
name|key
operator|.
name|equals
argument_list|(
literal|"Processor Tree:"
argument_list|)
condition|)
block|{
name|extractOp
argument_list|(
name|vertexObject
operator|.
name|getJSONObject
argument_list|(
name|key
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"Join:"
argument_list|)
condition|)
block|{
comment|// this is the case when we have a map-side SMB join
comment|// one input of the join is treated as a dummy vertex
name|JSONArray
name|array
init|=
name|vertexObject
operator|.
name|getJSONArray
argument_list|(
name|key
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
name|array
operator|.
name|length
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|JSONObject
name|mpOpTree
init|=
name|array
operator|.
name|getJSONObject
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|Vertex
name|v
init|=
operator|new
name|Vertex
argument_list|(
literal|null
argument_list|,
name|mpOpTree
argument_list|,
name|this
operator|.
name|stage
argument_list|,
name|parser
argument_list|)
decl_stmt|;
name|v
operator|.
name|extractOpTree
argument_list|()
expr_stmt|;
name|v
operator|.
name|dummy
operator|=
literal|true
expr_stmt|;
name|mergeJoinDummyVertexs
operator|.
name|add
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"Merge File Operator"
argument_list|)
condition|)
block|{
name|JSONObject
name|opTree
init|=
name|vertexObject
operator|.
name|getJSONObject
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|opTree
operator|.
name|has
argument_list|(
literal|"Map Operator Tree:"
argument_list|)
condition|)
block|{
name|extractOp
argument_list|(
name|opTree
operator|.
name|getJSONArray
argument_list|(
literal|"Map Operator Tree:"
argument_list|)
operator|.
name|getJSONObject
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Merge File Operator does not have a Map Operator Tree"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"Execution mode:"
argument_list|)
condition|)
block|{
name|executionMode
operator|=
literal|" "
operator|+
name|vertexObject
operator|.
name|getString
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"tagToInput:"
argument_list|)
condition|)
block|{
name|JSONObject
name|tagToInput
init|=
name|vertexObject
operator|.
name|getJSONObject
argument_list|(
name|key
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tag
range|:
name|JSONObject
operator|.
name|getNames
argument_list|(
name|tagToInput
argument_list|)
control|)
block|{
name|this
operator|.
name|tagToInput
operator|.
name|put
argument_list|(
name|tag
argument_list|,
operator|(
name|String
operator|)
name|tagToInput
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"tag:"
argument_list|)
condition|)
block|{
name|this
operator|.
name|tag
operator|=
name|vertexObject
operator|.
name|getString
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
literal|"Local Work:"
argument_list|)
condition|)
block|{
name|extractOp
argument_list|(
name|vertexObject
operator|.
name|getJSONObject
argument_list|(
name|key
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Skip unsupported "
operator|+
name|key
operator|+
literal|" in vertex "
operator|+
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * @param object    * @param isInput    * @param parent    * @return    * @throws JSONException    * @throws JsonParseException    * @throws JsonMappingException    * @throws IOException    * @throws Exception    *           assumption: each operator only has one parent but may have many    *           children    */
name|Op
name|extractOp
parameter_list|(
name|JSONObject
name|object
parameter_list|,
name|Op
name|parent
parameter_list|)
throws|throws
name|JSONException
throws|,
name|JsonParseException
throws|,
name|JsonMappingException
throws|,
name|IOException
throws|,
name|Exception
block|{
name|String
index|[]
name|names
init|=
name|JSONObject
operator|.
name|getNames
argument_list|(
name|object
argument_list|)
decl_stmt|;
if|if
condition|(
name|names
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Expect only one operator in "
operator|+
name|object
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
name|String
name|opName
init|=
name|names
index|[
literal|0
index|]
decl_stmt|;
name|JSONObject
name|attrObj
init|=
operator|(
name|JSONObject
operator|)
name|object
operator|.
name|get
argument_list|(
name|opName
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attrs
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Op
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Op
name|op
init|=
operator|new
name|Op
argument_list|(
name|opName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|parent
argument_list|,
name|children
argument_list|,
name|attrs
argument_list|,
name|attrObj
argument_list|,
name|this
argument_list|,
name|parser
argument_list|)
decl_stmt|;
if|if
condition|(
name|JSONObject
operator|.
name|getNames
argument_list|(
name|attrObj
argument_list|)
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|attrName
range|:
name|JSONObject
operator|.
name|getNames
argument_list|(
name|attrObj
argument_list|)
control|)
block|{
if|if
condition|(
name|attrName
operator|.
name|equals
argument_list|(
literal|"children"
argument_list|)
condition|)
block|{
name|Object
name|childrenObj
init|=
name|attrObj
operator|.
name|get
argument_list|(
name|attrName
argument_list|)
decl_stmt|;
if|if
condition|(
name|childrenObj
operator|instanceof
name|JSONObject
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|JSONObject
operator|)
name|childrenObj
operator|)
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|children
operator|.
name|add
argument_list|(
name|extractOp
argument_list|(
operator|(
name|JSONObject
operator|)
name|childrenObj
argument_list|,
name|op
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|childrenObj
operator|instanceof
name|JSONArray
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|JSONArray
operator|)
name|childrenObj
operator|)
operator|.
name|length
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|JSONArray
name|array
init|=
operator|(
operator|(
name|JSONArray
operator|)
name|childrenObj
operator|)
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
name|array
operator|.
name|length
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|children
operator|.
name|add
argument_list|(
name|extractOp
argument_list|(
name|array
operator|.
name|getJSONObject
argument_list|(
name|index
argument_list|)
argument_list|,
name|op
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Unsupported operator "
operator|+
name|this
operator|.
name|name
operator|+
literal|"'s children operator is neither a jsonobject nor a jsonarray"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|attrName
operator|.
name|equals
argument_list|(
literal|"OperatorId:"
argument_list|)
condition|)
block|{
name|op
operator|.
name|setOperatorId
argument_list|(
name|attrObj
operator|.
name|get
argument_list|(
name|attrName
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|attrName
operator|.
name|equals
argument_list|(
literal|"outputname:"
argument_list|)
condition|)
block|{
name|op
operator|.
name|outputVertexName
operator|=
name|attrObj
operator|.
name|get
argument_list|(
name|attrName
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|attrObj
operator|.
name|get
argument_list|(
name|attrName
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|attrs
operator|.
name|put
argument_list|(
name|attrName
argument_list|,
name|attrObj
operator|.
name|get
argument_list|(
name|attrName
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|parent
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|inputOps
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|children
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|outputOps
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
return|return
name|op
return|;
block|}
block|}
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
name|String
name|type
parameter_list|,
name|Vertex
name|callingVertex
parameter_list|)
throws|throws
name|JSONException
throws|,
name|Exception
block|{
comment|// print vertexname
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
operator|&&
name|numReduceOp
operator|<=
literal|1
condition|)
block|{
if|if
condition|(
name|type
operator|!=
literal|null
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
argument_list|,
literal|"<-"
argument_list|)
operator|+
literal|" Please refer to the previous "
operator|+
name|this
operator|.
name|name
operator|+
literal|" ["
operator|+
name|type
operator|+
literal|"]"
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
literal|" Please refer to the previous "
operator|+
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
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
name|type
operator|!=
literal|null
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
argument_list|,
literal|"<-"
argument_list|)
operator|+
name|this
operator|.
name|name
operator|+
literal|" ["
operator|+
name|type
operator|+
literal|"]"
operator|+
name|this
operator|.
name|executionMode
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|name
operator|!=
literal|null
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
name|name
operator|+
name|this
operator|.
name|executionMode
argument_list|)
expr_stmt|;
block|}
comment|// print operators
if|if
condition|(
name|numReduceOp
operator|>
literal|1
operator|&&
operator|!
operator|(
name|callingVertex
operator|.
name|vertexType
operator|==
name|VertexType
operator|.
name|UNION
operator|)
condition|)
block|{
comment|// find the right op
name|Op
name|choose
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Op
name|op
range|:
name|this
operator|.
name|outputOps
control|)
block|{
if|if
condition|(
name|op
operator|.
name|outputVertexName
operator|.
name|equals
argument_list|(
name|callingVertex
operator|.
name|name
argument_list|)
condition|)
block|{
name|choose
operator|=
name|op
expr_stmt|;
block|}
block|}
if|if
condition|(
name|choose
operator|!=
literal|null
condition|)
block|{
name|choose
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|indentFlag
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Can not find the right reduce output operator for vertex "
operator|+
name|this
operator|.
name|name
argument_list|)
throw|;
block|}
block|}
else|else
block|{
for|for
control|(
name|Op
name|op
range|:
name|this
operator|.
name|outputOps
control|)
block|{
comment|// dummy vertex is treated as a branch of a join operator
if|if
condition|(
name|this
operator|.
name|dummy
condition|)
block|{
name|op
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|indentFlag
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|op
operator|.
name|print
argument_list|(
name|printer
argument_list|,
name|indentFlag
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|vertexType
operator|==
name|VertexType
operator|.
name|UNION
condition|)
block|{
comment|// print dependent vertexs
name|indentFlag
operator|++
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|this
operator|.
name|parentConnections
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
name|this
operator|.
name|parentConnections
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
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
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * We check if a vertex has multiple reduce operators.    * @throws JSONException     */
specifier|public
name|void
name|checkMultiReduceOperator
parameter_list|(
name|boolean
name|rewriteObject
parameter_list|)
throws|throws
name|JSONException
block|{
comment|// check if it is a reduce vertex and its children is more than 1;
comment|// check if all the child ops are reduce output operators
name|numReduceOp
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Op
name|op
range|:
name|this
operator|.
name|outputOps
control|)
block|{
if|if
condition|(
name|op
operator|.
name|type
operator|==
name|OpType
operator|.
name|RS
condition|)
block|{
if|if
condition|(
name|rewriteObject
condition|)
block|{
name|Vertex
name|outputVertex
init|=
name|this
operator|.
name|stage
operator|.
name|vertexs
operator|.
name|get
argument_list|(
name|op
operator|.
name|outputVertexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|outputVertex
operator|!=
literal|null
operator|&&
name|outputVertex
operator|.
name|inputOps
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|JSONArray
name|array
init|=
operator|new
name|JSONArray
argument_list|()
decl_stmt|;
for|for
control|(
name|Op
name|inputOp
range|:
name|outputVertex
operator|.
name|inputOps
control|)
block|{
name|array
operator|.
name|put
argument_list|(
name|inputOp
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
name|op
operator|.
name|opObject
operator|.
name|put
argument_list|(
literal|"outputOperator:"
argument_list|,
name|array
argument_list|)
expr_stmt|;
block|}
block|}
name|numReduceOp
operator|++
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|edgeType
operator|=
name|this
operator|.
name|parser
operator|.
name|mapEdgeType
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
comment|// The following code should be gone after HIVE-11075 using topological order
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Vertex
name|o
parameter_list|)
block|{
comment|// we print the vertex that has more rs before the vertex that has fewer rs.
if|if
condition|(
name|numReduceOp
operator|!=
name|o
operator|.
name|numReduceOp
condition|)
block|{
return|return
operator|-
operator|(
name|numReduceOp
operator|-
name|o
operator|.
name|numReduceOp
operator|)
return|;
block|}
else|else
block|{
return|return
name|this
operator|.
name|name
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|name
argument_list|)
return|;
block|}
block|}
specifier|public
name|Op
name|getJoinRSOp
parameter_list|(
name|Vertex
name|joinVertex
parameter_list|)
block|{
if|if
condition|(
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|outputOps
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
if|if
condition|(
name|outputOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|type
operator|==
name|OpType
operator|.
name|RS
condition|)
block|{
return|return
name|outputOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
for|for
control|(
name|Op
name|op
range|:
name|outputOps
control|)
block|{
if|if
condition|(
name|op
operator|.
name|type
operator|==
name|OpType
operator|.
name|RS
condition|)
block|{
if|if
condition|(
name|op
operator|.
name|outputVertexName
operator|.
name|equals
argument_list|(
name|joinVertex
operator|.
name|name
argument_list|)
condition|)
block|{
return|return
name|op
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit


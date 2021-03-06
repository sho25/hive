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
name|serde2
operator|.
name|json
package|;
end_package

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
name|EnumSet
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
name|SerDeException
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
name|ListObjectInspector
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
name|MapObjectInspector
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
name|PrimitiveObjectInspector
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
name|StructField
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
name|StructObjectInspector
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
name|UnionObjectInspector
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
name|BinaryObjectInspector
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
name|BooleanObjectInspector
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
name|ByteObjectInspector
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
name|DateObjectInspector
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
name|DoubleObjectInspector
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
name|FloatObjectInspector
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
name|HiveCharObjectInspector
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
name|HiveDecimalObjectInspector
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
name|HiveVarcharObjectInspector
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
name|IntObjectInspector
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
name|LongObjectInspector
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
name|ShortObjectInspector
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
name|StringObjectInspector
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
name|TimestampObjectInspector
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
name|io
operator|.
name|Text
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

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonProcessingException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectWriter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|node
operator|.
name|ArrayNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|node
operator|.
name|ContainerNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|node
operator|.
name|JsonNodeFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|node
operator|.
name|ObjectNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|node
operator|.
name|ValueNode
import|;
end_import

begin_comment
comment|/**  * A class which takes Java Objects and a ObjectInspector to produce a JSON  * string representation of the structure. The Java Object can be a Collection  * containing other Collections and therefore can be thought of as a tree or as  * a directed acyclic graph that must be walked and each node recorded as JSON.  */
end_comment

begin_class
specifier|public
class|class
name|HiveJsonWriter
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
name|HiveJsonWriter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|EnumSet
argument_list|<
name|Feature
argument_list|>
name|features
init|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|Feature
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|rootStructFieldNames
decl_stmt|;
specifier|private
specifier|final
name|JsonNodeFactory
name|nodeFactory
decl_stmt|;
specifier|private
specifier|final
name|ObjectMapper
name|mapper
decl_stmt|;
specifier|private
name|BinaryEncoding
name|binaryEncoding
decl_stmt|;
comment|/**    * Enumeration that defines all on/off features for this writer.    */
specifier|public
enum|enum
name|Feature
block|{
name|PRETTY_PRINT
block|}
comment|/**    * Default constructor. Uses base-64 mime encoding for any binary data.    */
specifier|public
name|HiveJsonWriter
parameter_list|()
block|{
name|this
argument_list|(
name|BinaryEncoding
operator|.
name|BASE64
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructs a JSON writer.    *    * @param encoding The encoding to use for binary data    * @param rootStructFieldNames The names of the fields at the root level    */
specifier|public
name|HiveJsonWriter
parameter_list|(
specifier|final
name|BinaryEncoding
name|encoding
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|rootStructFieldNames
parameter_list|)
block|{
name|this
operator|.
name|binaryEncoding
operator|=
name|encoding
expr_stmt|;
name|this
operator|.
name|rootStructFieldNames
operator|=
name|rootStructFieldNames
expr_stmt|;
name|this
operator|.
name|nodeFactory
operator|=
name|JsonNodeFactory
operator|.
name|instance
expr_stmt|;
name|this
operator|.
name|mapper
operator|=
operator|new
name|ObjectMapper
argument_list|()
expr_stmt|;
block|}
comment|/**    * Given an Object and an ObjectInspector, convert the structure into a JSON    * text string.    *    * @param o The object to convert    * @param objInspector The ObjectInspector describing the object    * @return A String containing the JSON text    * @throws SerDeException The object cannot be transformed    */
specifier|public
name|String
name|write
parameter_list|(
specifier|final
name|Object
name|o
parameter_list|,
specifier|final
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|JsonNode
name|rootNode
init|=
name|walkObjectGraph
argument_list|(
name|objInspector
argument_list|,
name|o
argument_list|,
name|rootStructFieldNames
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Create JSON tree from Object tree: {}"
argument_list|,
name|rootNode
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|ObjectWriter
name|ow
init|=
name|isEnabled
argument_list|(
name|Feature
operator|.
name|PRETTY_PRINT
argument_list|)
condition|?
name|this
operator|.
name|mapper
operator|.
name|writerWithDefaultPrettyPrinter
argument_list|()
else|:
name|this
operator|.
name|mapper
operator|.
name|writer
argument_list|()
decl_stmt|;
return|return
name|ow
operator|.
name|writeValueAsString
argument_list|(
name|rootNode
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|JsonProcessingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Walk the object graph.    *    * @param oi The ObjectInspector describing the Object    * @param o The object to convert    * @param fieldNames List of field names to use, default names used otherwise    * @return A JsonNode representation of the Object    * @throws SerDeException The Object cannot be parsed    */
specifier|private
name|JsonNode
name|walkObjectGraph
parameter_list|(
specifier|final
name|ObjectInspector
name|oi
parameter_list|,
specifier|final
name|Object
name|o
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|o
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|LIST
case|:
return|return
name|visitList
argument_list|(
name|oi
argument_list|,
name|o
argument_list|)
return|;
case|case
name|STRUCT
case|:
return|return
name|visitStruct
argument_list|(
name|oi
argument_list|,
name|o
argument_list|,
name|fieldNames
argument_list|)
return|;
case|case
name|MAP
case|:
return|return
name|visitMap
argument_list|(
name|oi
argument_list|,
name|o
argument_list|)
return|;
case|case
name|PRIMITIVE
case|:
return|return
name|primitiveToNode
argument_list|(
name|oi
argument_list|,
name|o
argument_list|)
return|;
case|case
name|UNION
case|:
return|return
name|visitUnion
argument_list|(
name|oi
argument_list|,
name|o
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Parsing of: "
operator|+
name|oi
operator|.
name|getCategory
argument_list|()
operator|+
literal|" is not supported"
argument_list|)
throw|;
block|}
block|}
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|nullNode
argument_list|()
return|;
block|}
comment|/**    * Visit a vertex in the graph that is a Java Map.    *    * @param oi The map's OjectInspector    * @param o The Map object    * @return A JsonNode representation of the Map    * @throws SerDeException The Map cannot be parsed    */
specifier|private
name|ObjectNode
name|visitMap
parameter_list|(
specifier|final
name|ObjectInspector
name|oi
parameter_list|,
specifier|final
name|Object
name|o
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|oi
decl_stmt|;
specifier|final
name|ObjectInspector
name|mapKeyInspector
init|=
name|moi
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|ObjectInspector
name|mapValueInspector
init|=
name|moi
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|map
init|=
name|moi
operator|.
name|getMap
argument_list|(
name|o
argument_list|)
decl_stmt|;
specifier|final
name|ObjectNode
name|objectNode
init|=
name|this
operator|.
name|nodeFactory
operator|.
name|objectNode
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|JsonNode
name|keyNode
init|=
name|primitiveToNode
argument_list|(
name|mapKeyInspector
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|JsonNode
name|valueNode
init|=
name|walkObjectGraph
argument_list|(
name|mapValueInspector
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|objectNode
operator|.
name|set
argument_list|(
name|keyNode
operator|.
name|asText
argument_list|()
argument_list|,
name|valueNode
argument_list|)
expr_stmt|;
block|}
return|return
name|objectNode
return|;
block|}
comment|/**    * Visit a vertex in the graph that is a Java List.    *    * @param oi The list's OjectInspector    * @param o The List object    * @return A JsonNode representation of the List    * @throws SerDeException The List cannot be parsed    */
specifier|private
name|ContainerNode
argument_list|<
name|?
argument_list|>
name|visitList
parameter_list|(
specifier|final
name|ObjectInspector
name|oi
parameter_list|,
specifier|final
name|Object
name|o
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|oi
decl_stmt|;
specifier|final
name|List
argument_list|<
name|?
argument_list|>
name|list
init|=
name|loi
operator|.
name|getList
argument_list|(
name|o
argument_list|)
decl_stmt|;
specifier|final
name|ArrayNode
name|arrayNode
init|=
name|this
operator|.
name|nodeFactory
operator|.
name|arrayNode
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|Object
name|item
range|:
name|list
control|)
block|{
specifier|final
name|JsonNode
name|valueNode
init|=
name|walkObjectGraph
argument_list|(
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|,
name|item
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|arrayNode
operator|.
name|add
argument_list|(
name|valueNode
argument_list|)
expr_stmt|;
block|}
return|return
name|arrayNode
return|;
block|}
comment|/**    * Visit a vertex in the graph that is a struct data type. A struct is    * represented as a Java List where the name associated with each element in    * the list is stored in the ObjectInspector.    *    * @param oi The struct's OjectInspector    * @param o The List object    * @param fieldNames List of names to override the default field names    * @return A JsonNode representation of the List    * @throws SerDeException The struct cannot be parsed    */
specifier|private
name|ObjectNode
name|visitStruct
parameter_list|(
specifier|final
name|ObjectInspector
name|oi
parameter_list|,
specifier|final
name|Object
name|o
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|StructObjectInspector
name|structInspector
init|=
operator|(
name|StructObjectInspector
operator|)
name|oi
decl_stmt|;
specifier|final
name|ObjectNode
name|structNode
init|=
name|this
operator|.
name|nodeFactory
operator|.
name|objectNode
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|StructField
name|field
range|:
name|structInspector
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
specifier|final
name|Object
name|fieldValue
init|=
name|structInspector
operator|.
name|getStructFieldData
argument_list|(
name|o
argument_list|,
name|field
argument_list|)
decl_stmt|;
specifier|final
name|ObjectInspector
name|coi
init|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
specifier|final
name|JsonNode
name|fieldNode
init|=
name|walkObjectGraph
argument_list|(
name|coi
argument_list|,
name|fieldValue
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
comment|// Map field names to something else if require
specifier|final
name|String
name|fieldName
init|=
operator|(
name|fieldNames
operator|.
name|isEmpty
argument_list|()
operator|)
condition|?
name|field
operator|.
name|getFieldName
argument_list|()
else|:
name|fieldNames
operator|.
name|get
argument_list|(
name|field
operator|.
name|getFieldID
argument_list|()
argument_list|)
decl_stmt|;
name|structNode
operator|.
name|set
argument_list|(
name|fieldName
argument_list|,
name|fieldNode
argument_list|)
expr_stmt|;
block|}
return|return
name|structNode
return|;
block|}
comment|/**    * Visit a vertex in the graph that is a union data type.    *    * @param oi The union's OjectInspector    * @param o The Union object    * @return A JsonNode representation of the union    * @throws SerDeException The union cannot be parsed    */
specifier|private
name|ObjectNode
name|visitUnion
parameter_list|(
specifier|final
name|ObjectInspector
name|oi
parameter_list|,
specifier|final
name|Object
name|o
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|UnionObjectInspector
name|unionInspector
init|=
operator|(
name|UnionObjectInspector
operator|)
name|oi
decl_stmt|;
specifier|final
name|ObjectNode
name|unionNode
init|=
name|this
operator|.
name|nodeFactory
operator|.
name|objectNode
argument_list|()
decl_stmt|;
specifier|final
name|byte
name|tag
init|=
name|unionInspector
operator|.
name|getTag
argument_list|(
name|o
argument_list|)
decl_stmt|;
specifier|final
name|String
name|tagText
init|=
name|Byte
operator|.
name|toString
argument_list|(
name|tag
argument_list|)
decl_stmt|;
specifier|final
name|JsonNode
name|valueNode
init|=
name|walkObjectGraph
argument_list|(
name|unionInspector
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
argument_list|,
name|unionInspector
operator|.
name|getField
argument_list|(
name|o
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|unionNode
operator|.
name|set
argument_list|(
name|tagText
argument_list|,
name|valueNode
argument_list|)
expr_stmt|;
return|return
name|unionNode
return|;
block|}
comment|/**    * Convert a primitive Java object to a JsonNode.    *    * @param oi The primitive ObjectInspector    * @param o The primitive Object    * @return A JSON node containing the value of the primitive Object    * @throws SerDeException The primitive value cannot be parsed    */
specifier|private
name|ValueNode
name|primitiveToNode
parameter_list|(
specifier|final
name|ObjectInspector
name|oi
parameter_list|,
specifier|final
name|Object
name|o
parameter_list|)
throws|throws
name|SerDeException
block|{
specifier|final
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
decl_stmt|;
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BINARY
case|:
return|return
name|getByteValue
argument_list|(
operator|(
operator|(
name|BinaryObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|BOOLEAN
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|booleanNode
argument_list|(
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|BYTE
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|DATE
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|textNode
argument_list|(
operator|(
operator|(
name|DateObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
operator|.
name|bigDecimalValue
argument_list|()
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|INT
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|IntObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|LONG
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|LongObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|SHORT
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|numberNode
argument_list|(
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|STRING
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|textNode
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
case|case
name|CHAR
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|textNode
argument_list|(
operator|(
operator|(
name|HiveCharObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
case|case
name|VARCHAR
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|textNode
argument_list|(
operator|(
operator|(
name|HiveVarcharObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|textNode
argument_list|(
operator|(
operator|(
name|TimestampObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unsupported type: "
operator|+
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert a byte array into a text representation.    *    * @param o The byte array to convert to text    * @return A JsonNode with the binary data encoded    * @throws SerDeException The struct cannot be parsed    */
specifier|private
name|ValueNode
name|getByteValue
parameter_list|(
specifier|final
name|byte
index|[]
name|buf
parameter_list|)
throws|throws
name|SerDeException
block|{
switch|switch
condition|(
name|this
operator|.
name|binaryEncoding
condition|)
block|{
case|case
name|RAWSTRING
case|:
specifier|final
name|Text
name|txt
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|txt
operator|.
name|set
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|textNode
argument_list|(
name|txt
operator|.
name|toString
argument_list|()
argument_list|)
return|;
case|case
name|BASE64
case|:
return|return
name|this
operator|.
name|nodeFactory
operator|.
name|binaryNode
argument_list|(
name|buf
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Error generating JSON binary type from record."
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|enable
parameter_list|(
name|Feature
name|feature
parameter_list|)
block|{
name|this
operator|.
name|features
operator|.
name|add
argument_list|(
name|feature
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|disable
parameter_list|(
name|Feature
name|feature
parameter_list|)
block|{
name|this
operator|.
name|features
operator|.
name|remove
argument_list|(
name|feature
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|Feature
argument_list|>
name|getFeatures
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|this
operator|.
name|features
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isEnabled
parameter_list|(
name|Feature
name|feature
parameter_list|)
block|{
return|return
name|this
operator|.
name|features
operator|.
name|contains
argument_list|(
name|feature
argument_list|)
return|;
block|}
specifier|public
name|BinaryEncoding
name|getBinaryEncodingType
parameter_list|()
block|{
return|return
name|binaryEncoding
return|;
block|}
specifier|public
name|void
name|setBinaryEncoding
parameter_list|(
name|BinaryEncoding
name|encoding
parameter_list|)
block|{
name|this
operator|.
name|binaryEncoding
operator|=
name|encoding
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HiveJsonWriter [features="
operator|+
name|features
operator|+
literal|", rootStructFieldNames="
operator|+
name|rootStructFieldNames
operator|+
literal|", binaryEncoding="
operator|+
name|binaryEncoding
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit


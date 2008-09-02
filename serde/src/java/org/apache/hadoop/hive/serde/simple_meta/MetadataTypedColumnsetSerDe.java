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
name|serde
operator|.
name|simple_meta
package|;
end_package

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
name|serde
operator|.
name|thrift
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TBase
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TSerializer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|*
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
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
name|conf
operator|.
name|Configuration
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
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|MetadataTypedColumnsetSerDe
extends|extends
name|ByteStreamTypedSerDe
implements|implements
name|SerDe
block|{
specifier|protected
name|TIOStreamTransport
name|outTransport
decl_stmt|,
name|inTransport
decl_stmt|;
specifier|protected
name|TProtocol
name|outProtocol
decl_stmt|,
name|inProtocol
decl_stmt|;
specifier|public
name|String
name|getShortName
parameter_list|()
block|{
return|return
name|shortName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|shortName
parameter_list|()
block|{
return|return
literal|"simple_meta"
return|;
block|}
static|static
block|{
name|StackTraceElement
index|[]
name|sTrace
init|=
operator|new
name|Exception
argument_list|()
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
name|String
name|className
init|=
name|sTrace
index|[
literal|0
index|]
operator|.
name|getClassName
argument_list|()
decl_stmt|;
try|try
block|{
name|SerDeUtils
operator|.
name|registerSerDe
argument_list|(
name|shortName
argument_list|()
argument_list|,
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|final
specifier|public
specifier|static
name|String
name|DefaultSeparator
init|=
literal|"\001"
decl_stmt|;
specifier|protected
name|boolean
name|inStreaming
decl_stmt|;
specifier|private
name|String
name|separator
decl_stmt|;
comment|// constant for now, will make it configurable later.
specifier|private
name|String
name|nullString
init|=
literal|"\\N"
decl_stmt|;
specifier|private
name|ColumnSet
name|cachedObj
decl_stmt|;
comment|// stores the column name and its position in the input
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|java
operator|.
name|lang
operator|.
name|Integer
argument_list|>
name|_columns
decl_stmt|;
comment|// stores the columns in order
specifier|private
name|String
name|_columns_list
index|[]
decl_stmt|;
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MetaDataTypedColumnsetSerDe["
operator|+
name|separator
operator|+
literal|","
operator|+
name|_columns
operator|+
literal|"]"
return|;
block|}
specifier|public
name|MetadataTypedColumnsetSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{
name|this
argument_list|(
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
name|ColumnSet
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetadataTypedColumnsetSerDe
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|argType
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
argument_list|(
name|argType
argument_list|,
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
argument_list|,
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MetadataTypedColumnsetSerDe
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|argType
parameter_list|,
name|TProtocolFactory
name|inFactory
parameter_list|,
name|TProtocolFactory
name|outFactory
parameter_list|)
throws|throws
name|SerDeException
block|{
name|super
argument_list|(
name|argType
argument_list|)
expr_stmt|;
name|cachedObj
operator|=
operator|new
name|ColumnSet
argument_list|()
expr_stmt|;
name|cachedObj
operator|.
name|col
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|separator
operator|=
name|DefaultSeparator
expr_stmt|;
name|outTransport
operator|=
operator|new
name|TIOStreamTransport
argument_list|(
name|bos
argument_list|)
expr_stmt|;
name|inTransport
operator|=
operator|new
name|TIOStreamTransport
argument_list|(
name|bis
argument_list|)
expr_stmt|;
name|outProtocol
operator|=
name|outFactory
operator|.
name|getProtocol
argument_list|(
name|outTransport
argument_list|)
expr_stmt|;
name|inProtocol
operator|=
name|inFactory
operator|.
name|getProtocol
argument_list|(
name|inTransport
argument_list|)
expr_stmt|;
name|json_serializer
operator|=
operator|new
name|TSerializer
argument_list|(
operator|new
name|TJSONProtocol
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|inStreaming
operator|=
name|job
operator|.
name|get
argument_list|(
literal|"hive.streaming.select"
argument_list|)
operator|!=
literal|null
expr_stmt|;
name|separator
operator|=
name|DefaultSeparator
expr_stmt|;
name|String
name|alt_sep
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
decl_stmt|;
if|if
condition|(
name|alt_sep
operator|!=
literal|null
operator|&&
name|alt_sep
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|byte
name|b
index|[]
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
name|b
index|[
literal|0
index|]
operator|=
name|Byte
operator|.
name|valueOf
argument_list|(
name|alt_sep
argument_list|)
operator|.
name|byteValue
argument_list|()
expr_stmt|;
name|separator
operator|=
operator|new
name|String
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|separator
operator|=
name|alt_sep
expr_stmt|;
block|}
block|}
name|_columns_list
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns"
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|_columns
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|java
operator|.
name|lang
operator|.
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|_columns_list
control|)
block|{
name|_columns
operator|.
name|put
argument_list|(
name|column
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|Object
name|deserialize
parameter_list|(
name|ColumnSet
name|c
parameter_list|,
name|String
name|row
parameter_list|,
name|String
name|sep
parameter_list|,
name|String
name|nullString
parameter_list|)
throws|throws
name|Exception
block|{
name|c
operator|.
name|col
operator|.
name|clear
argument_list|()
expr_stmt|;
name|String
index|[]
name|l1
init|=
name|row
operator|.
name|split
argument_list|(
name|sep
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|l1
control|)
block|{
if|if
condition|(
name|s
operator|.
name|equals
argument_list|(
name|nullString
argument_list|)
condition|)
block|{
name|c
operator|.
name|col
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|c
operator|.
name|col
operator|.
name|add
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|c
operator|)
return|;
block|}
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|field
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|/*       OK - if we had the runtime thrift here, we'd have the ddl in the serde and       just call runtime_thrift.deserialize(this.ddl,field);        pw 2/5/08      */
name|ColumnSet
name|c
init|=
name|cachedObj
decl_stmt|;
try|try
block|{
try|try
block|{
name|Text
name|tw
init|=
operator|(
name|Text
operator|)
name|field
decl_stmt|;
name|String
name|row
init|=
name|tw
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
operator|(
name|deserialize
argument_list|(
name|c
argument_list|,
name|row
argument_list|,
name|separator
argument_list|,
name|nullString
argument_list|)
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"columnsetSerDe  expects Text"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
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
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
comment|// don't want to crap out streaming jobs because of one error.
if|if
condition|(
name|inStreaming
condition|)
block|{
return|return
operator|(
name|c
operator|)
return|;
block|}
else|else
block|{
throw|throw
operator|(
name|e
operator|)
throw|;
block|}
block|}
block|}
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Do type conversion if necessary
name|ColumnSet
name|c
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|obj
operator|instanceof
name|ColumnSet
condition|)
block|{
name|c
operator|=
operator|(
name|ColumnSet
operator|)
name|obj
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|obj
operator|instanceof
name|List
condition|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|a
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// convert all obj to string
for|for
control|(
name|Object
name|o
range|:
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|obj
control|)
block|{
name|a
operator|.
name|add
argument_list|(
name|o
operator|==
literal|null
condition|?
name|nullString
else|:
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|c
operator|=
operator|new
name|ColumnSet
argument_list|(
name|a
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" can only serialize ColumnSet or java List."
argument_list|)
throw|;
block|}
comment|// Serialize columnSet
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|c
operator|.
name|col
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
name|separator
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|c
operator|.
name|col
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Text
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|SerDeField
name|getFieldFromExpression
parameter_list|(
name|SerDeField
name|parentField
parameter_list|,
name|String
name|fieldExpression
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|/* question:     //  should we have a HiveType ??      class HiveType {      public HiveType() { }     public HiveType getTypeFromExpression(fieldExpression);     public HiveType [] getSubtypes();      // does the below make sense ? where else would the hivefield be found for this type     public SerDeField getSerDeField( ) { }     // problem is if this isn't some base type or collection of base type...      // or should someone be able to do select u.affiliations_subtable from u insert into just_affiliations_subtable     // maybe ...      // But, how at runtime can i do a get of an affiliations_subtable Object in Java???     // The question is what is this used for. If by streaming, I just need a way of serlializing it     // to stream it into the stream.      // Another question is could we take advantage so we can supply Python with this.      // what about a python constructed class for reading/writing these types? If we had the runtime thing, no problem     // at all.      // What is the reason for not keeping it in the SerDe?  Just makes sense to have the following abstractions:      // 1. RosettaObject(implements Writable)  - to get and set data. Could be a full row or subtype      // one question is serialize/deserialize - we presumably do this once on the entire object, and     // then it gets passed to us      // 2. RosettaType - represents a "schema" / type     // 3. RosettaSerDe - thin wrapper around the other 2     // 4. RosettaField implements SerDeField      The SerDe should return an overall RosettaObject which is then passed to the various RosettaField get     methods.      // Looking at SerDe, we need to implement:     1. serialize     2. deserialize     3. getFields     4. getFieldFromExpression     5. toJSONString      IMPLEMENTATIONS:      1. serialize -     SDObject obj = new SDObject(tableOrColumnName, SDType, rawData);     return obj (where SDObject implements the Writable interface      2. deserialize(Object obj);     SDObject obj = new SDObject(tableOrColumnName, SDType, rawData);     return obj;      // how does one implement RosettaField.get(RosettaObject obj) ?     //       3. getFields: call RosettaType.getFields     4. getFieldFromExpression: call RosettaType.getFields     5. toJSONString - make serialize type a format.      // Are we going to allow people to define these types and then re-use them?      // Another question - does every type have a SerDe??      }       */
if|if
condition|(
name|_columns
operator|.
name|containsKey
argument_list|(
name|fieldExpression
argument_list|)
condition|)
block|{
comment|// for now ignore parentField for MetaDataTyped fields since no subtypes
try|try
block|{
return|return
operator|new
name|MetadataTypedSerDeField
argument_list|(
name|fieldExpression
argument_list|,
name|Class
operator|.
name|forName
argument_list|(
literal|"java.lang.String"
argument_list|)
argument_list|,
name|_columns
operator|.
name|get
argument_list|(
name|fieldExpression
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
elseif|else
if|if
condition|(
name|ExpressionUtils
operator|.
name|isComplexExpression
argument_list|(
name|fieldExpression
argument_list|)
condition|)
block|{
comment|// for backwards compatability...
return|return
operator|new
name|ComplexSerDeField
argument_list|(
name|parentField
argument_list|,
name|fieldExpression
argument_list|,
name|this
argument_list|)
return|;
block|}
else|else
block|{
comment|// again for backwards compatability - the ComplexSerDeField calls this to get the type of the "col" in columnset
name|String
name|className
init|=
name|type
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|new
name|ReflectionSerDeField
argument_list|(
name|className
argument_list|,
name|fieldExpression
argument_list|)
return|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|SerDeField
argument_list|>
name|getFields
parameter_list|(
name|SerDeField
name|parentField
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// ignore parent for now - famous last words :)
name|ArrayList
argument_list|<
name|SerDeField
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<
name|SerDeField
argument_list|>
argument_list|(
name|_columns_list
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|column
range|:
name|_columns_list
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|this
operator|.
name|getFieldFromExpression
argument_list|(
literal|null
argument_list|,
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|fields
return|;
block|}
specifier|public
name|String
name|toJSONString
parameter_list|(
name|Object
name|obj
parameter_list|,
name|SerDeField
name|hf
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
if|if
condition|(
name|hf
operator|==
literal|null
condition|)
block|{
comment|// if this is a top level Thrift object
return|return
name|json_serializer
operator|.
name|toString
argument_list|(
operator|(
name|TBase
operator|)
name|obj
argument_list|)
return|;
block|}
if|if
condition|(
name|hf
operator|.
name|isList
argument_list|()
operator|||
name|hf
operator|.
name|isMap
argument_list|()
condition|)
block|{
comment|// pretty print a list
return|return
name|SerDeUtils
operator|.
name|toJSONString
argument_list|(
name|obj
argument_list|,
name|hf
argument_list|,
name|this
argument_list|)
return|;
block|}
if|if
condition|(
name|hf
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
comment|// escape string before printing
return|return
name|SerDeUtils
operator|.
name|lightEscapeString
argument_list|(
name|obj
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
comment|// anything else must be a top level thrift object as well
return|return
name|json_serializer
operator|.
name|toString
argument_list|(
operator|(
name|TBase
operator|)
name|obj
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"toJSONString:TJSONProtocol error"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


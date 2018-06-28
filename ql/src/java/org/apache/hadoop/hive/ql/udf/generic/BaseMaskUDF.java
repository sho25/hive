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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|type
operator|.
name|Date
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
name|type
operator|.
name|HiveChar
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
name|type
operator|.
name|HiveVarchar
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
name|UDFArgumentException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
operator|.
name|DeferredObject
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
name|io
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
name|primitive
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
name|io
operator|.
name|IntWritable
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
name|LongWritable
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

begin_class
specifier|public
specifier|abstract
class|class
name|BaseMaskUDF
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BaseMaskUDF
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|protected
name|AbstractTransformer
name|transformer
decl_stmt|;
specifier|final
specifier|protected
name|String
name|displayName
decl_stmt|;
specifier|protected
name|AbstractTransformerAdapter
name|transformerAdapter
init|=
literal|null
decl_stmt|;
specifier|protected
name|BaseMaskUDF
parameter_list|(
name|AbstractTransformer
name|transformer
parameter_list|,
name|String
name|displayName
parameter_list|)
block|{
name|this
operator|.
name|transformer
operator|=
name|transformer
expr_stmt|;
name|this
operator|.
name|displayName
operator|=
name|displayName
expr_stmt|;
block|}
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"==> BaseMaskUDF.initialize()"
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// first argument is the column to be transformed
name|PrimitiveObjectInspector
name|columnType
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
operator|)
decl_stmt|;
name|transformer
operator|.
name|init
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|transformerAdapter
operator|=
name|AbstractTransformerAdapter
operator|.
name|getTransformerAdapter
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
name|ObjectInspector
name|ret
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|columnType
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"<== BaseMaskUDF.initialize()"
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
name|ret
init|=
name|transformerAdapter
operator|.
name|getTransformedWritable
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|getStandardDisplayString
argument_list|(
name|displayName
argument_list|,
name|children
argument_list|)
return|;
block|}
block|}
end_class

begin_comment
comment|/**  * Interface to be implemented by transformers which transform a given value according to its specification.  */
end_comment

begin_class
specifier|abstract
class|class
name|AbstractTransformer
block|{
comment|/**    * Initialzie the transformer object    * @param arguments arguments given to GenericUDF.initialzie()    * @param startIdx index into array, from which the transformer should read values    */
specifier|abstract
name|void
name|init
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|startIdx
parameter_list|)
function_decl|;
comment|/**    * Transform a String value    * @param value value to transform    * @return transformed value    */
specifier|abstract
name|String
name|transform
parameter_list|(
name|String
name|value
parameter_list|)
function_decl|;
comment|/**    * Transform a Byte value    * @param value value to transform    * @return transformed value    */
specifier|abstract
name|Byte
name|transform
parameter_list|(
name|Byte
name|value
parameter_list|)
function_decl|;
comment|/**    * Transform a Short value    * @param value value to transform    * @return transformed value    */
specifier|abstract
name|Short
name|transform
parameter_list|(
name|Short
name|value
parameter_list|)
function_decl|;
comment|/**    * Transform a Integer value    * @param value value to transform    * @return transformed value    */
specifier|abstract
name|Integer
name|transform
parameter_list|(
name|Integer
name|value
parameter_list|)
function_decl|;
comment|/**    * Transform a Long value    * @param value value to transform    * @return transformed value    */
specifier|abstract
name|Long
name|transform
parameter_list|(
name|Long
name|value
parameter_list|)
function_decl|;
comment|/**    * Transform a Date value    * @param value value to transform    * @return transformed value    */
specifier|abstract
name|Date
name|transform
parameter_list|(
name|Date
name|value
parameter_list|)
function_decl|;
block|}
end_class

begin_comment
comment|/**  * Interface to be implemented by datatype specific adapters that handle necessary conversion of the transformed value  * into appropriate Writable object, which GenericUDF.evaluate() is expected to return.  */
end_comment

begin_class
specifier|abstract
class|class
name|AbstractTransformerAdapter
block|{
specifier|final
name|AbstractTransformer
name|transformer
decl_stmt|;
name|AbstractTransformerAdapter
parameter_list|(
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
operator|.
name|transformer
operator|=
name|transformer
expr_stmt|;
block|}
specifier|abstract
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|value
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|static
name|AbstractTransformerAdapter
name|getTransformerAdapter
parameter_list|(
name|PrimitiveObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
specifier|final
name|AbstractTransformerAdapter
name|ret
decl_stmt|;
switch|switch
condition|(
name|columnType
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|STRING
case|:
name|ret
operator|=
operator|new
name|StringTransformerAdapter
argument_list|(
operator|(
name|StringObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|ret
operator|=
operator|new
name|HiveCharTransformerAdapter
argument_list|(
operator|(
name|HiveCharObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|ret
operator|=
operator|new
name|HiveVarcharTransformerAdapter
argument_list|(
operator|(
name|HiveVarcharObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|ret
operator|=
operator|new
name|ByteTransformerAdapter
argument_list|(
operator|(
name|ByteObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|ret
operator|=
operator|new
name|ShortTransformerAdapter
argument_list|(
operator|(
name|ShortObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|ret
operator|=
operator|new
name|IntegerTransformerAdapter
argument_list|(
operator|(
name|IntObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|ret
operator|=
operator|new
name|LongTransformerAdapter
argument_list|(
operator|(
name|LongObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|ret
operator|=
operator|new
name|DateTransformerAdapter
argument_list|(
operator|(
name|DateObjectInspector
operator|)
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
default|default:
name|ret
operator|=
operator|new
name|UnsupportedDatatypeTransformAdapter
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

begin_class
class|class
name|ByteTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|ByteObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|ByteWritable
name|writable
decl_stmt|;
specifier|public
name|ByteTransformerAdapter
parameter_list|(
name|ByteObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|ByteWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ByteTransformerAdapter
parameter_list|(
name|ByteObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|ByteWritable
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|Byte
name|value
init|=
operator|(
name|Byte
operator|)
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|Byte
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|DateTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|DateObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|DateWritableV2
name|writable
decl_stmt|;
specifier|public
name|DateTransformerAdapter
parameter_list|(
name|DateObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|DateWritableV2
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DateTransformerAdapter
parameter_list|(
name|DateObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|DateWritableV2
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|Date
name|value
init|=
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|Date
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|HiveCharTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|HiveCharObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|HiveCharWritable
name|writable
decl_stmt|;
specifier|public
name|HiveCharTransformerAdapter
parameter_list|(
name|HiveCharObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|HiveCharWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveCharTransformerAdapter
parameter_list|(
name|HiveCharObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|HiveCharWritable
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveChar
name|value
init|=
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|String
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|HiveVarcharTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|HiveVarcharObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|HiveVarcharWritable
name|writable
decl_stmt|;
specifier|public
name|HiveVarcharTransformerAdapter
parameter_list|(
name|HiveVarcharObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|HiveVarcharWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveVarcharTransformerAdapter
parameter_list|(
name|HiveVarcharObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|HiveVarcharWritable
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveVarchar
name|value
init|=
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|String
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|IntegerTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|IntObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|IntWritable
name|writable
decl_stmt|;
specifier|public
name|IntegerTransformerAdapter
parameter_list|(
name|IntObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|IntWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IntegerTransformerAdapter
parameter_list|(
name|IntObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|IntWritable
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|Integer
name|value
init|=
operator|(
name|Integer
operator|)
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|Integer
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|LongTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|LongObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|LongWritable
name|writable
decl_stmt|;
specifier|public
name|LongTransformerAdapter
parameter_list|(
name|LongObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|LongWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LongTransformerAdapter
parameter_list|(
name|LongObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|LongWritable
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|Long
name|value
init|=
operator|(
name|Long
operator|)
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|Long
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|ShortTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|ShortObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|ShortWritable
name|writable
decl_stmt|;
specifier|public
name|ShortTransformerAdapter
parameter_list|(
name|ShortObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|ShortWritable
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ShortTransformerAdapter
parameter_list|(
name|ShortObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|ShortWritable
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|Short
name|value
init|=
operator|(
name|Short
operator|)
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|Short
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|StringTransformerAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|StringObjectInspector
name|columnType
decl_stmt|;
specifier|final
name|Text
name|writable
decl_stmt|;
specifier|public
name|StringTransformerAdapter
parameter_list|(
name|StringObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|this
argument_list|(
name|columnType
argument_list|,
name|transformer
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StringTransformerAdapter
parameter_list|(
name|StringObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|,
name|Text
name|writable
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|this
operator|.
name|writable
operator|=
name|writable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|value
init|=
name|columnType
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|object
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|String
name|transformedValue
init|=
name|transformer
operator|.
name|transform
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|transformedValue
operator|!=
literal|null
condition|)
block|{
name|writable
operator|.
name|set
argument_list|(
name|transformedValue
argument_list|)
expr_stmt|;
return|return
name|writable
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

begin_class
class|class
name|UnsupportedDatatypeTransformAdapter
extends|extends
name|AbstractTransformerAdapter
block|{
specifier|final
name|PrimitiveObjectInspector
name|columnType
decl_stmt|;
specifier|public
name|UnsupportedDatatypeTransformAdapter
parameter_list|(
name|PrimitiveObjectInspector
name|columnType
parameter_list|,
name|AbstractTransformer
name|transformer
parameter_list|)
block|{
name|super
argument_list|(
name|transformer
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getTransformedWritable
parameter_list|(
name|DeferredObject
name|object
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit


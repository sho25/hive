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
name|List
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
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|UDFArgumentTypeException
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
name|parse
operator|.
name|SemanticException
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
name|ObjectInspectorFactory
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
name|StandardListObjectInspector
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
name|PrimitiveObjectInspectorUtils
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
name|typeinfo
operator|.
name|ListTypeInfo
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Estimates the top-k contextual n-grams in arbitrary sequential data using a heuristic.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"context_ngrams"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr, array<string1, string2, ...>, k, pf) estimates the top-k most "
operator|+
literal|"frequent n-grams that fit into the specified context. The second parameter specifies "
operator|+
literal|"a string of words that specify the positions of the n-gram elements, with a null value "
operator|+
literal|"standing in for a 'blank' that must be filled by an n-gram element."
argument_list|,
name|extended
operator|=
literal|"The primary expression must be an array of strings, or an array of arrays of "
operator|+
literal|"strings, such as the return type of the sentences() UDF. The second parameter specifies "
operator|+
literal|"the context -- for example, array(\"i\", \"love\", null) -- which would estimate the top "
operator|+
literal|"'k' words that follow the phrase \"i love\" in the primary expression. The optional "
operator|+
literal|"fourth parameter 'pf' controls the memory used by the heuristic. Larger values will "
operator|+
literal|"yield better accuracy, but use more memory. Example usage:\n"
operator|+
literal|"  SELECT context_ngrams(sentences(lower(review)), array(\"i\", \"love\", null, null), 10)"
operator|+
literal|" FROM movies\n"
operator|+
literal|"would attempt to determine the 10 most common two-word phrases that follow \"i love\" "
operator|+
literal|"in a database of free-form natural language movie reviews."
argument_list|)
specifier|public
class|class
name|GenericUDAFContextNGrams
implements|implements
name|GenericUDAFResolver
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenericUDAFContextNGrams
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|parameters
operator|.
name|length
operator|!=
literal|3
operator|&&
name|parameters
operator|.
name|length
operator|!=
literal|4
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|parameters
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Please specify either three or four arguments."
argument_list|)
throw|;
block|}
comment|// Validate the first parameter, which is the expression to compute over. This should be an
comment|// array of strings type, or an array of arrays of strings.
name|PrimitiveTypeInfo
name|pti
decl_stmt|;
if|if
condition|(
name|parameters
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|LIST
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only list type arguments are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|ListTypeInfo
operator|)
name|parameters
index|[
literal|0
index|]
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
comment|// Parameter 1 was an array of primitives, so make sure the primitives are strings.
name|pti
operator|=
call|(
name|PrimitiveTypeInfo
call|)
argument_list|(
operator|(
name|ListTypeInfo
operator|)
name|parameters
index|[
literal|0
index|]
argument_list|)
operator|.
name|getListElementTypeInfo
argument_list|()
expr_stmt|;
break|break;
case|case
name|LIST
case|:
comment|// Parameter 1 was an array of arrays, so make sure that the inner arrays contain
comment|// primitive strings.
name|ListTypeInfo
name|lti
init|=
call|(
name|ListTypeInfo
call|)
argument_list|(
operator|(
name|ListTypeInfo
operator|)
name|parameters
index|[
literal|0
index|]
argument_list|)
operator|.
name|getListElementTypeInfo
argument_list|()
decl_stmt|;
name|pti
operator|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|lti
operator|.
name|getListElementTypeInfo
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only arrays of strings or arrays of arrays of strings are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
if|if
condition|(
name|pti
operator|.
name|getPrimitiveCategory
argument_list|()
operator|!=
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only array<string> or array<array<string>> is allowed, but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
comment|// Validate the second parameter, which should be an array of strings
if|if
condition|(
name|parameters
index|[
literal|1
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|LIST
operator|||
operator|(
operator|(
name|ListTypeInfo
operator|)
name|parameters
index|[
literal|1
index|]
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only arrays of strings are accepted but "
operator|+
name|parameters
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 2."
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
call|(
name|PrimitiveTypeInfo
call|)
argument_list|(
operator|(
name|ListTypeInfo
operator|)
name|parameters
index|[
literal|1
index|]
argument_list|)
operator|.
name|getListElementTypeInfo
argument_list|()
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|!=
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only arrays of strings are accepted but "
operator|+
name|parameters
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 2."
argument_list|)
throw|;
block|}
comment|// Validate the third parameter, which should be an integer to represent 'k'
if|if
condition|(
name|parameters
index|[
literal|2
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"Only integers are accepted but "
operator|+
name|parameters
index|[
literal|2
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 3."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|parameters
index|[
literal|2
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|TIMESTAMP
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"Only integers are accepted but "
operator|+
name|parameters
index|[
literal|2
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 3."
argument_list|)
throw|;
block|}
comment|// If the fourth parameter -- precision factor 'pf' -- has been specified, make sure it's
comment|// an integer.
if|if
condition|(
name|parameters
operator|.
name|length
operator|==
literal|4
condition|)
block|{
if|if
condition|(
name|parameters
index|[
literal|3
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|3
argument_list|,
literal|"Only integers are accepted but "
operator|+
name|parameters
index|[
literal|3
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 4."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|parameters
index|[
literal|3
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|TIMESTAMP
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|3
argument_list|,
literal|"Only integers are accepted but "
operator|+
name|parameters
index|[
literal|3
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 4."
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|GenericUDAFContextNGramEvaluator
argument_list|()
return|;
block|}
comment|/**    * A constant-space heuristic to estimate the top-k contextual n-grams.    */
specifier|public
specifier|static
class|class
name|GenericUDAFContextNGramEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// For PARTIAL1 and COMPLETE: ObjectInspectors for original data
specifier|private
specifier|transient
name|ListObjectInspector
name|outerInputOI
decl_stmt|;
specifier|private
specifier|transient
name|StandardListObjectInspector
name|innerInputOI
decl_stmt|;
specifier|private
specifier|transient
name|ListObjectInspector
name|contextListOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|contextOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|kOI
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|pOI
decl_stmt|;
comment|// For PARTIAL2 and FINAL: ObjectInspectors for partial aggregations
specifier|private
specifier|transient
name|ListObjectInspector
name|loi
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|init
parameter_list|(
name|Mode
name|m
parameter_list|,
name|ObjectInspector
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
comment|// Init input object inspectors
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|m
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|outerInputOI
operator|=
operator|(
name|ListObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|outerInputOI
operator|.
name|getListElementObjectInspector
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|LIST
condition|)
block|{
comment|// We're dealing with input that is an array of arrays of strings
name|innerInputOI
operator|=
operator|(
name|StandardListObjectInspector
operator|)
name|outerInputOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|innerInputOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// We're dealing with input that is an array of strings
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|outerInputOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|innerInputOI
operator|=
literal|null
expr_stmt|;
block|}
name|contextListOI
operator|=
operator|(
name|ListObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
expr_stmt|;
name|contextOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|contextListOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|kOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|2
index|]
expr_stmt|;
if|if
condition|(
name|parameters
operator|.
name|length
operator|==
literal|4
condition|)
block|{
name|pOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|3
index|]
expr_stmt|;
block|}
else|else
block|{
name|pOI
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Init the list object inspector for handling partial aggregations
name|loi
operator|=
operator|(
name|ListObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
block|}
comment|// Init output object inspectors.
comment|//
comment|// The return type for a partial aggregation is still a list of strings.
comment|//
comment|// The return type for FINAL and COMPLETE is a full aggregation result, which is
comment|// an array of structures containing the n-gram and its estimated frequency.
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|m
operator|==
name|Mode
operator|.
name|PARTIAL2
condition|)
block|{
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
return|;
block|}
else|else
block|{
comment|// Final return type that goes back to Hive: a list of structs with n-grams and their
comment|// estimated frequencies.
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|foi
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
argument_list|)
expr_stmt|;
name|foi
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fname
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"ngram"
argument_list|)
expr_stmt|;
name|fname
operator|.
name|add
argument_list|(
literal|"estfrequency"
argument_list|)
expr_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fname
argument_list|,
name|foi
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
name|obj
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|NGramAggBuf
name|myagg
init|=
operator|(
name|NGramAggBuf
operator|)
name|agg
decl_stmt|;
name|List
name|partial
init|=
operator|(
name|List
operator|)
name|loi
operator|.
name|getList
argument_list|(
name|obj
argument_list|)
decl_stmt|;
comment|// remove the context words from the end of the list
name|int
name|contextSize
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|partial
operator|.
name|get
argument_list|(
name|partial
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|partial
operator|.
name|remove
argument_list|(
name|partial
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|myagg
operator|.
name|context
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|contextSize
operator|!=
name|myagg
operator|.
name|context
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": found a mismatch in the"
operator|+
literal|" context string lengths. This is usually caused by passing a non-constant"
operator|+
literal|" expression for the context."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
name|partial
operator|.
name|size
argument_list|()
operator|-
name|contextSize
init|;
name|i
operator|<
name|partial
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|word
init|=
name|partial
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|word
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|myagg
operator|.
name|context
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|myagg
operator|.
name|context
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
block|}
block|}
name|partial
operator|.
name|subList
argument_list|(
name|partial
operator|.
name|size
argument_list|()
operator|-
name|contextSize
argument_list|,
name|partial
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
name|myagg
operator|.
name|nge
operator|.
name|merge
argument_list|(
name|partial
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminatePartial
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|NGramAggBuf
name|myagg
init|=
operator|(
name|NGramAggBuf
operator|)
name|agg
decl_stmt|;
name|ArrayList
argument_list|<
name|Text
argument_list|>
name|result
init|=
name|myagg
operator|.
name|nge
operator|.
name|serialize
argument_list|()
decl_stmt|;
comment|// push the context on to the end of the serialized n-gram estimation
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|myagg
operator|.
name|context
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
name|myagg
operator|.
name|context
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
literal|null
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
name|myagg
operator|.
name|context
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|result
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|myagg
operator|.
name|context
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Finds all contextual n-grams in a sequence of words, and passes the n-grams to the
comment|// n-gram estimator object
specifier|private
name|void
name|processNgrams
parameter_list|(
name|NGramAggBuf
name|agg
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|seq
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// generate n-grams wherever the context matches
assert|assert
operator|(
name|agg
operator|.
name|context
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
assert|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|ng
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|seq
operator|.
name|size
argument_list|()
operator|-
name|agg
operator|.
name|context
operator|.
name|size
argument_list|()
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
comment|// check if the context matches
name|boolean
name|contextMatches
init|=
literal|true
decl_stmt|;
name|ng
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|agg
operator|.
name|context
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|String
name|contextWord
init|=
name|agg
operator|.
name|context
operator|.
name|get
argument_list|(
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
name|contextWord
operator|==
literal|null
condition|)
block|{
name|ng
operator|.
name|add
argument_list|(
name|seq
operator|.
name|get
argument_list|(
name|i
operator|+
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|contextWord
operator|.
name|equals
argument_list|(
name|seq
operator|.
name|get
argument_list|(
name|i
operator|+
name|j
argument_list|)
argument_list|)
condition|)
block|{
name|contextMatches
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
block|}
comment|// add to n-gram estimation only if the context matches
if|if
condition|(
name|contextMatches
condition|)
block|{
name|agg
operator|.
name|nge
operator|.
name|add
argument_list|(
name|ng
argument_list|)
expr_stmt|;
name|ng
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|iterate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
literal|3
operator|||
name|parameters
operator|.
name|length
operator|==
literal|4
operator|)
assert|;
if|if
condition|(
name|parameters
index|[
literal|0
index|]
operator|==
literal|null
operator|||
name|parameters
index|[
literal|1
index|]
operator|==
literal|null
operator|||
name|parameters
index|[
literal|2
index|]
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|NGramAggBuf
name|myagg
init|=
operator|(
name|NGramAggBuf
operator|)
name|agg
decl_stmt|;
comment|// Parse out the context and 'k' if we haven't already done so, and while we're at it,
comment|// also parse out the precision factor 'pf' if the user has supplied one.
if|if
condition|(
operator|!
name|myagg
operator|.
name|nge
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|int
name|k
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|parameters
index|[
literal|2
index|]
argument_list|,
name|kOI
argument_list|)
decl_stmt|;
name|int
name|pf
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|k
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" needs 'k' to be at least 1, "
operator|+
literal|"but you supplied "
operator|+
name|k
argument_list|)
throw|;
block|}
if|if
condition|(
name|parameters
operator|.
name|length
operator|==
literal|4
condition|)
block|{
name|pf
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|parameters
index|[
literal|3
index|]
argument_list|,
name|pOI
argument_list|)
expr_stmt|;
if|if
condition|(
name|pf
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" needs 'pf' to be at least 1, "
operator|+
literal|"but you supplied "
operator|+
name|pf
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|pf
operator|=
literal|1
expr_stmt|;
comment|// placeholder; minimum pf value is enforced in NGramEstimator
block|}
comment|// Parse out the context and make sure it isn't empty
name|myagg
operator|.
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Text
argument_list|>
name|context
init|=
operator|(
name|List
argument_list|<
name|Text
argument_list|>
operator|)
name|contextListOI
operator|.
name|getList
argument_list|(
name|parameters
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|int
name|contextNulls
init|=
literal|0
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
name|context
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|word
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|context
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|contextOI
argument_list|)
decl_stmt|;
if|if
condition|(
name|word
operator|==
literal|null
condition|)
block|{
name|contextNulls
operator|++
expr_stmt|;
block|}
name|myagg
operator|.
name|context
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|context
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" needs a context array "
operator|+
literal|"with at least one element."
argument_list|)
throw|;
block|}
if|if
condition|(
name|contextNulls
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" the context array needs to "
operator|+
literal|"contain at least one 'null' value to indicate what should be counted."
argument_list|)
throw|;
block|}
comment|// Set parameters in the n-gram estimator object
name|myagg
operator|.
name|nge
operator|.
name|initialize
argument_list|(
name|k
argument_list|,
name|pf
argument_list|,
name|contextNulls
argument_list|)
expr_stmt|;
block|}
comment|// get the input expression
name|List
argument_list|<
name|Text
argument_list|>
name|outer
init|=
operator|(
name|List
argument_list|<
name|Text
argument_list|>
operator|)
name|outerInputOI
operator|.
name|getList
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerInputOI
operator|!=
literal|null
condition|)
block|{
comment|// we're dealing with an array of arrays of strings
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|outer
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|Text
argument_list|>
name|inner
init|=
operator|(
name|List
argument_list|<
name|Text
argument_list|>
operator|)
name|innerInputOI
operator|.
name|getList
argument_list|(
name|outer
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|words
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|inner
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|String
name|word
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|inner
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
name|words
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
block|}
comment|// parse out n-grams, update frequency counts
name|processNgrams
argument_list|(
name|myagg
argument_list|,
name|words
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// we're dealing with an array of strings
name|ArrayList
argument_list|<
name|String
argument_list|>
name|words
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
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
name|outer
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|word
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getString
argument_list|(
name|outer
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
name|words
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
block|}
comment|// parse out n-grams, update frequency counts
name|processNgrams
argument_list|(
name|myagg
argument_list|,
name|words
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|NGramAggBuf
name|myagg
init|=
operator|(
name|NGramAggBuf
operator|)
name|agg
decl_stmt|;
return|return
name|myagg
operator|.
name|nge
operator|.
name|getNGrams
argument_list|()
return|;
block|}
comment|// Aggregation buffer methods.
specifier|static
class|class
name|NGramAggBuf
extends|extends
name|AbstractAggregationBuffer
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|context
decl_stmt|;
name|NGramEstimator
name|nge
decl_stmt|;
block|}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|NGramAggBuf
name|result
init|=
operator|new
name|NGramAggBuf
argument_list|()
decl_stmt|;
name|result
operator|.
name|nge
operator|=
operator|new
name|NGramEstimator
argument_list|()
expr_stmt|;
name|result
operator|.
name|context
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|reset
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|NGramAggBuf
name|result
init|=
operator|(
name|NGramAggBuf
operator|)
name|agg
decl_stmt|;
name|result
operator|.
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
name|result
operator|.
name|nge
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


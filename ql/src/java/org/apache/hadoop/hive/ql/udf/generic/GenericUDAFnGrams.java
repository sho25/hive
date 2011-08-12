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
name|io
operator|.
name|DoubleWritable
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
name|StandardMapObjectInspector
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
name|WritableDoubleObjectInspector
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
name|util
operator|.
name|StringUtils
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
comment|/**  * Estimates the top-k n-grams in arbitrary sequential data using a heuristic.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"ngrams"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr, n, k, pf) - Estimates the top-k n-grams in rows that consist of "
operator|+
literal|"sequences of strings, represented as arrays of strings, or arrays of arrays of "
operator|+
literal|"strings. 'pf' is an optional precision factor that controls memory usage."
argument_list|,
name|extended
operator|=
literal|"The parameter 'n' specifies what type of n-grams are being estimated. Unigrams "
operator|+
literal|"are n = 1, and bigrams are n = 2. Generally, n will not be greater than about 5. "
operator|+
literal|"The 'k' parameter specifies how many of the highest-frequency n-grams will be "
operator|+
literal|"returned by the UDAF. The optional precision factor 'pf' specifies how much "
operator|+
literal|"memory to use for estimation; more memory will give more accurate frequency "
operator|+
literal|"counts, but could crash the JVM. The default value is 20, which internally "
operator|+
literal|"maintains 20*k n-grams, but only returns the k highest frequency ones. "
operator|+
literal|"The output is an array of structs with the top-k n-grams. It might be convenient "
operator|+
literal|"to explode() the output of this UDAF."
argument_list|)
specifier|public
class|class
name|GenericUDAFnGrams
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
name|GenericUDAFnGrams
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
comment|// Validate the second parameter, which should be an integer
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
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Only integers are accepted but "
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
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|parameters
index|[
literal|1
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
literal|1
argument_list|,
literal|"Only integers are accepted but "
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
comment|// Validate the third parameter, which should also be an integer
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
comment|// If we have the optional fourth parameter, make sure it's also an integer
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
name|GenericUDAFnGramEvaluator
argument_list|()
return|;
block|}
comment|/**    * A constant-space heuristic to estimate the top-k n-grams.    */
specifier|public
specifier|static
class|class
name|GenericUDAFnGramEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// For PARTIAL1 and COMPLETE: ObjectInspectors for original data
specifier|private
name|StandardListObjectInspector
name|outerInputOI
decl_stmt|;
specifier|private
name|StandardListObjectInspector
name|innerInputOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|nOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|kOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|pOI
decl_stmt|;
comment|// For PARTIAL2 and FINAL: ObjectInspectors for partial aggregations
specifier|private
name|StandardListObjectInspector
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
name|StandardListObjectInspector
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
name|nOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|1
index|]
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
name|StandardListObjectInspector
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
name|partial
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|partial
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
argument_list|<
name|Text
argument_list|>
name|partialNGrams
init|=
operator|(
name|List
argument_list|<
name|Text
argument_list|>
operator|)
name|loi
operator|.
name|getList
argument_list|(
name|partial
argument_list|)
decl_stmt|;
name|int
name|n
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|partialNGrams
operator|.
name|get
argument_list|(
name|partialNGrams
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
if|if
condition|(
name|myagg
operator|.
name|n
operator|>
literal|0
operator|&&
name|myagg
operator|.
name|n
operator|!=
name|n
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
literal|": mismatch in value for 'n'"
operator|+
literal|", which usually is caused by a non-constant expression. Found '"
operator|+
name|n
operator|+
literal|"' and '"
operator|+
name|myagg
operator|.
name|n
operator|+
literal|"'."
argument_list|)
throw|;
block|}
name|myagg
operator|.
name|n
operator|=
name|n
expr_stmt|;
name|partialNGrams
operator|.
name|remove
argument_list|(
name|partialNGrams
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|nge
operator|.
name|merge
argument_list|(
name|partialNGrams
argument_list|)
expr_stmt|;
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
name|n
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
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
name|n
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|ngram
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
name|agg
operator|.
name|n
condition|;
name|j
operator|++
control|)
block|{
name|ngram
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
name|agg
operator|.
name|nge
operator|.
name|add
argument_list|(
name|ngram
argument_list|)
expr_stmt|;
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
comment|// Parse out 'n' and 'k' if we haven't already done so, and while we're at it,
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
name|n
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|parameters
index|[
literal|1
index|]
argument_list|,
name|nOI
argument_list|)
decl_stmt|;
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
name|n
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
literal|" needs 'n' to be at least 1, "
operator|+
literal|"but you supplied "
operator|+
name|n
argument_list|)
throw|;
block|}
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
comment|// Set the parameters
name|myagg
operator|.
name|n
operator|=
name|n
expr_stmt|;
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
name|n
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
implements|implements
name|AggregationBuffer
block|{
name|NGramEstimator
name|nge
decl_stmt|;
name|int
name|n
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
name|nge
operator|.
name|reset
argument_list|()
expr_stmt|;
name|result
operator|.
name|n
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


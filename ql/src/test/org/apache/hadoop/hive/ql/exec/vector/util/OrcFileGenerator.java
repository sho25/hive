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
name|exec
operator|.
name|vector
operator|.
name|util
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|orc
operator|.
name|CompressionKind
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
name|orc
operator|.
name|Writer
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
name|orc
operator|.
name|OrcFile
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
name|orc
operator|.
name|TestVectorizedORCReader
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

begin_comment
comment|/**  * This class generates an orc file from a specified record class. The orc file  * will contain 3 batches worth of rows for each column for all kinds of data distribution:  * all values, no nulls, repeating value, and repeating null.  *  */
end_comment

begin_class
specifier|public
class|class
name|OrcFileGenerator
block|{
enum|enum
name|BatchDataDistribution
block|{
name|AllValues
block|,
name|NoNulls
block|,
name|RepeatingValue
block|,
name|RepeatingNull
block|}
comment|/**    * Base class for type specific batch generators. Each type specific batch generator implements    * generateRandomNonNullValue to generate random values, and initializeFixedPointValues to    * specify a set of fixed values within the data (this is useful when defining query predicates)    */
specifier|private
specifier|abstract
specifier|static
class|class
name|BatchGenerator
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
specifier|final
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
literal|0xfa57
argument_list|)
decl_stmt|;
specifier|private
name|int
name|possibleNonRandomValueGenerated
init|=
name|rand
operator|.
name|nextInt
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|T
index|[]
name|fixedPointValues
decl_stmt|;
specifier|public
name|BatchGenerator
parameter_list|()
block|{
name|fixedPointValues
operator|=
name|initializeFixedPointValues
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|T
index|[]
name|initializeFixedPointValues
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|T
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
function_decl|;
specifier|public
name|T
index|[]
name|generateBatch
parameter_list|(
name|BatchDataDistribution
name|dist
parameter_list|)
block|{
name|Object
index|[]
name|batch
init|=
operator|new
name|Object
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
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
name|batch
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
switch|switch
condition|(
name|dist
condition|)
block|{
case|case
name|AllValues
case|:
if|if
condition|(
name|possibleNonRandomValueGenerated
operator|%
literal|73
operator|==
literal|0
condition|)
block|{
name|batch
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fixedPointValues
operator|!=
literal|null
operator|&&
name|possibleNonRandomValueGenerated
operator|%
literal|233
operator|==
literal|0
condition|)
block|{
name|batch
index|[
name|i
index|]
operator|=
name|fixedPointValues
index|[
name|rand
operator|.
name|nextInt
argument_list|(
name|fixedPointValues
operator|.
name|length
argument_list|)
index|]
expr_stmt|;
block|}
else|else
block|{
name|batch
index|[
name|i
index|]
operator|=
name|generateRandomNonNullValue
argument_list|(
name|rand
argument_list|)
expr_stmt|;
block|}
name|possibleNonRandomValueGenerated
operator|++
expr_stmt|;
break|break;
case|case
name|NoNulls
case|:
if|if
condition|(
name|fixedPointValues
operator|!=
literal|null
operator|&&
name|possibleNonRandomValueGenerated
operator|%
literal|233
operator|==
literal|0
condition|)
block|{
name|batch
index|[
name|i
index|]
operator|=
name|fixedPointValues
index|[
name|rand
operator|.
name|nextInt
argument_list|(
name|fixedPointValues
operator|.
name|length
argument_list|)
index|]
expr_stmt|;
block|}
else|else
block|{
name|batch
index|[
name|i
index|]
operator|=
name|generateRandomNonNullValue
argument_list|(
name|rand
argument_list|)
expr_stmt|;
block|}
name|possibleNonRandomValueGenerated
operator|++
expr_stmt|;
break|break;
case|case
name|RepeatingNull
case|:
name|batch
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
break|break;
case|case
name|RepeatingValue
case|:
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|batch
index|[
name|i
index|]
operator|=
name|generateRandomNonNullValue
argument_list|(
name|rand
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|batch
index|[
name|i
index|]
operator|=
name|batch
index|[
literal|0
index|]
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|dist
operator|.
name|toString
argument_list|()
operator|+
literal|" data distribution is not implemented."
argument_list|)
throw|;
block|}
block|}
return|return
operator|(
name|T
index|[]
operator|)
name|batch
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ByteBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Byte
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Byte
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
call|(
name|byte
call|)
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|(
operator|(
name|Byte
operator|.
name|MAX_VALUE
operator|-
name|Byte
operator|.
name|MIN_VALUE
operator|)
operator|/
literal|2
argument_list|)
operator|-
name|Math
operator|.
name|abs
argument_list|(
name|Byte
operator|.
name|MIN_VALUE
operator|/
literal|2
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Byte
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|Byte
index|[]
block|{
operator|-
literal|23
block|,
operator|-
literal|1
block|,
literal|17
block|,
literal|33
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|ShortBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Short
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Short
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|(
operator|(
name|Short
operator|.
name|MAX_VALUE
operator|-
name|Short
operator|.
name|MIN_VALUE
operator|)
operator|/
literal|2
argument_list|)
operator|+
operator|(
name|Short
operator|.
name|MIN_VALUE
operator|/
literal|2
operator|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Short
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|Short
index|[]
block|{
operator|-
literal|257
block|,
operator|-
literal|75
block|,
literal|197
block|,
literal|359
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|IntegerBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Integer
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Integer
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
name|rand
operator|.
name|nextInt
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|+
operator|(
name|Integer
operator|.
name|MIN_VALUE
operator|/
literal|2
operator|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Integer
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|Integer
index|[]
block|{
operator|-
literal|3728
block|,
operator|-
literal|563
block|,
literal|762
block|,
literal|6981
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|LongBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Long
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Long
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
operator|(
name|long
operator|)
name|rand
operator|.
name|nextInt
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Long
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|Long
index|[]
block|{
operator|(
name|long
operator|)
operator|-
literal|89010
block|,
operator|(
name|long
operator|)
operator|-
literal|6432
block|,
operator|(
name|long
operator|)
literal|3569
block|,
operator|(
name|long
operator|)
literal|988888
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FloatBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Float
argument_list|>
block|{
specifier|private
specifier|final
name|ByteBatchGenerator
name|byteGenerator
init|=
operator|new
name|ByteBatchGenerator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Float
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
operator|(
name|float
operator|)
name|byteGenerator
operator|.
name|generateRandomNonNullValue
argument_list|(
name|rand
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Float
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|Float
index|[]
block|{
operator|(
name|float
operator|)
operator|-
literal|26.28
block|,
operator|(
name|float
operator|)
operator|-
literal|1.389
block|,
operator|(
name|float
operator|)
literal|10.175
block|,
operator|(
name|float
operator|)
literal|79.553
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|DoubleBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Double
argument_list|>
block|{
specifier|private
specifier|final
name|ShortBatchGenerator
name|shortGenerator
init|=
operator|new
name|ShortBatchGenerator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Double
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
operator|(
name|double
operator|)
name|shortGenerator
operator|.
name|generateRandomNonNullValue
argument_list|(
name|rand
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Double
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|Double
index|[]
block|{
operator|-
literal|5638.15
block|,
operator|-
literal|863.257
block|,
literal|2563.58
block|,
literal|9763215.5639
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|BooleanBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Boolean
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|Boolean
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
name|rand
operator|.
name|nextBoolean
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Boolean
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|StringBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|String
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
name|int
name|length
init|=
name|rand
operator|.
name|nextInt
argument_list|(
literal|20
argument_list|)
operator|+
literal|5
decl_stmt|;
name|char
index|[]
name|values
init|=
operator|new
name|char
index|[
name|length
index|]
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
name|length
condition|;
name|j
operator|++
control|)
block|{
switch|switch
condition|(
name|rand
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|values
index|[
name|j
index|]
operator|=
call|(
name|char
call|)
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
literal|'z'
operator|-
operator|(
name|int
operator|)
literal|'a'
argument_list|)
operator|+
operator|(
name|int
operator|)
literal|'a'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|values
index|[
name|j
index|]
operator|=
call|(
name|char
call|)
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
literal|'Z'
operator|-
operator|(
name|int
operator|)
literal|'A'
argument_list|)
operator|+
operator|(
name|int
operator|)
literal|'A'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|values
index|[
name|j
index|]
operator|=
call|(
name|char
call|)
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|(
operator|(
name|int
operator|)
literal|'9'
operator|-
operator|(
name|int
operator|)
literal|'0'
argument_list|)
operator|+
operator|(
name|int
operator|)
literal|'0'
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|return
operator|new
name|String
argument_list|(
name|values
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"b"
block|,
literal|"ss"
block|,
literal|"10"
block|}
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TimestampBatchGenerator
extends|extends
name|BatchGenerator
argument_list|<
name|Timestamp
argument_list|>
block|{
specifier|private
specifier|final
name|ShortBatchGenerator
name|shortGen
init|=
operator|new
name|ShortBatchGenerator
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|Timestamp
name|generateRandomNonNullValue
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
operator|new
name|Timestamp
argument_list|(
name|shortGen
operator|.
name|generateRandomNonNullValue
argument_list|(
name|rand
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Timestamp
index|[]
name|initializeFixedPointValues
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
operator|new
name|Timestamp
index|[]
block|{
operator|new
name|Timestamp
argument_list|(
operator|-
literal|29071
argument_list|)
block|,
operator|new
name|Timestamp
argument_list|(
operator|-
literal|10669
argument_list|)
block|,
operator|new
name|Timestamp
argument_list|(
literal|16558
argument_list|)
block|,
operator|new
name|Timestamp
argument_list|(
literal|31808
argument_list|)
block|}
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|,
name|BatchGenerator
argument_list|>
name|TYPE_TO_BATCH_GEN_MAP
decl_stmt|;
static|static
block|{
name|TYPE_TO_BATCH_GEN_MAP
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|,
name|BatchGenerator
argument_list|>
argument_list|()
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Boolean
operator|.
name|class
argument_list|,
operator|new
name|BooleanBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Byte
operator|.
name|class
argument_list|,
operator|new
name|ByteBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
operator|new
name|IntegerBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Long
operator|.
name|class
argument_list|,
operator|new
name|LongBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Short
operator|.
name|class
argument_list|,
operator|new
name|ShortBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Float
operator|.
name|class
argument_list|,
operator|new
name|FloatBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Double
operator|.
name|class
argument_list|,
operator|new
name|DoubleBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|String
operator|.
name|class
argument_list|,
operator|new
name|StringBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|put
argument_list|(
name|Timestamp
operator|.
name|class
argument_list|,
operator|new
name|TimestampBatchGenerator
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generates an orc file based on the provided record class in the specified file system    * at the output path.    *    * @param conf the configuration used to initialize the orc writer    * @param fs the file system to which will contain the generated orc file    * @param outputPath the path where the generated orc will be placed    * @param recordClass a class the defines the record format for the generated orc file, this    * class must have exactly one constructor.    */
specifier|public
specifier|static
name|void
name|generateOrcFile
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|outputPath
parameter_list|,
name|Class
name|recordClass
parameter_list|)
throws|throws
name|IOException
throws|,
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|InvocationTargetException
block|{
name|ObjectInspector
name|inspector
decl_stmt|;
synchronized|synchronized
init|(
name|TestVectorizedORCReader
operator|.
name|class
init|)
block|{
name|inspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|recordClass
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
name|Writer
name|writer
init|=
name|OrcFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|outputPath
argument_list|,
name|conf
argument_list|,
name|inspector
argument_list|,
literal|100000
argument_list|,
name|CompressionKind
operator|.
name|ZLIB
argument_list|,
literal|10000
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
try|try
block|{
name|Constructor
index|[]
name|constructors
init|=
name|recordClass
operator|.
name|getConstructors
argument_list|()
decl_stmt|;
if|if
condition|(
name|constructors
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"The provided recordClass must have exactly one constructor."
argument_list|)
throw|;
block|}
name|BatchDataDistribution
index|[]
name|dataDist
init|=
name|BatchDataDistribution
operator|.
name|values
argument_list|()
decl_stmt|;
name|Class
index|[]
name|columns
init|=
name|constructors
index|[
literal|0
index|]
operator|.
name|getParameterTypes
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
name|dataDist
operator|.
name|length
operator|*
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
index|[]
name|rows
init|=
operator|new
name|Object
index|[
name|columns
operator|.
name|length
index|]
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
operator|.
name|length
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|containsKey
argument_list|(
name|columns
index|[
name|c
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"No batch generator defined for type "
operator|+
name|columns
index|[
name|c
index|]
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|rows
index|[
name|c
index|]
operator|=
name|TYPE_TO_BATCH_GEN_MAP
operator|.
name|get
argument_list|(
name|columns
index|[
name|c
index|]
argument_list|)
operator|.
name|generateBatch
argument_list|(
name|dataDist
index|[
operator|(
name|i
operator|+
name|c
operator|)
operator|%
name|dataDist
operator|.
name|length
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
condition|;
name|r
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
operator|new
name|Object
index|[
name|columns
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columns
operator|.
name|length
condition|;
name|c
operator|++
control|)
block|{
name|row
index|[
name|c
index|]
operator|=
name|rows
index|[
name|c
index|]
index|[
name|r
index|]
expr_stmt|;
block|}
name|writer
operator|.
name|addRow
argument_list|(
name|constructors
index|[
literal|0
index|]
operator|.
name|newInstance
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit


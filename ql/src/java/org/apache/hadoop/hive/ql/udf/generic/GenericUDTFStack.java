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
name|ArrayList
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
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
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
name|ConstantObjectInspector
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
name|primitive
operator|.
name|WritableConstantIntObjectInspector
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

begin_comment
comment|/**  * Takes a row of size k of data and splits it into n rows of data.  For  * example, if n is 3 then the rest of the arguments are split in order into 3  * rows, each of which has k/3 columns in it (the first emitted row has the  * first k/3, the second has the second, etc).  If n does not divide k then the  * remaining columns are padded with NULLs.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"stack"
argument_list|,
name|value
operator|=
literal|"_FUNC_(n, cols...) - turns k columns into n rows of size k/n each"
argument_list|)
specifier|public
class|class
name|GenericUDTFStack
extends|extends
name|GenericUDTF
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|HiveException
block|{   }
specifier|private
specifier|transient
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|argOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|Object
index|[]
name|forwardObj
init|=
literal|null
decl_stmt|;
specifier|private
specifier|transient
name|ArrayList
argument_list|<
name|ReturnObjectInspectorResolver
argument_list|>
name|returnOIResolvers
init|=
operator|new
name|ArrayList
argument_list|<
name|ReturnObjectInspectorResolver
argument_list|>
argument_list|()
decl_stmt|;
name|IntWritable
name|numRows
init|=
literal|null
decl_stmt|;
name|Integer
name|numCols
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|StructObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|args
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"STACK() expects at least two arguments."
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
operator|(
name|args
index|[
literal|0
index|]
operator|instanceof
name|ConstantObjectInspector
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"The first argument to STACK() must be a constant integer (got "
operator|+
name|args
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" instead)."
argument_list|)
throw|;
block|}
name|numRows
operator|=
call|(
name|IntWritable
call|)
argument_list|(
operator|(
name|ConstantObjectInspector
operator|)
name|args
index|[
literal|0
index|]
argument_list|)
operator|.
name|getWritableConstantValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|numRows
operator|==
literal|null
operator|||
name|numRows
operator|.
name|get
argument_list|()
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"STACK() expects its first argument to be>= 1."
argument_list|)
throw|;
block|}
comment|// Divide and round up.
name|numCols
operator|=
operator|(
name|args
operator|.
name|length
operator|-
literal|1
operator|+
name|numRows
operator|.
name|get
argument_list|()
operator|-
literal|1
operator|)
operator|/
name|numRows
operator|.
name|get
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|jj
init|=
literal|0
init|;
name|jj
operator|<
name|numCols
condition|;
operator|++
name|jj
control|)
block|{
name|returnOIResolvers
operator|.
name|add
argument_list|(
operator|new
name|ReturnObjectInspectorResolver
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|numRows
operator|.
name|get
argument_list|()
condition|;
operator|++
name|ii
control|)
block|{
name|int
name|index
init|=
name|ii
operator|*
name|numCols
operator|+
name|jj
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|index
operator|<
name|args
operator|.
name|length
operator|&&
operator|!
name|returnOIResolvers
operator|.
name|get
argument_list|(
name|jj
argument_list|)
operator|.
name|update
argument_list|(
name|args
index|[
name|index
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"Argument "
operator|+
operator|(
name|jj
operator|+
literal|1
operator|)
operator|+
literal|"'s type ("
operator|+
name|args
index|[
name|jj
operator|+
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|") should be equal to argument "
operator|+
name|index
operator|+
literal|"'s type ("
operator|+
name|args
index|[
name|index
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|")"
argument_list|)
throw|;
block|}
block|}
block|}
name|forwardObj
operator|=
operator|new
name|Object
index|[
name|numCols
index|]
expr_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|args
operator|.
name|length
condition|;
operator|++
name|ii
control|)
block|{
name|argOIs
operator|.
name|add
argument_list|(
name|args
index|[
name|ii
index|]
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|numCols
condition|;
operator|++
name|ii
control|)
block|{
name|fieldNames
operator|.
name|add
argument_list|(
literal|"col"
operator|+
name|ii
argument_list|)
expr_stmt|;
name|fieldOIs
operator|.
name|add
argument_list|(
name|returnOIResolvers
operator|.
name|get
argument_list|(
name|ii
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fieldNames
argument_list|,
name|fieldOIs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|HiveException
throws|,
name|UDFArgumentException
block|{
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|numRows
operator|.
name|get
argument_list|()
condition|;
operator|++
name|ii
control|)
block|{
for|for
control|(
name|int
name|jj
init|=
literal|0
init|;
name|jj
operator|<
name|numCols
condition|;
operator|++
name|jj
control|)
block|{
name|int
name|index
init|=
name|ii
operator|*
name|numCols
operator|+
name|jj
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|index
operator|<
name|args
operator|.
name|length
condition|)
block|{
name|forwardObj
index|[
name|jj
index|]
operator|=
name|returnOIResolvers
operator|.
name|get
argument_list|(
name|jj
argument_list|)
operator|.
name|convertIfNecessary
argument_list|(
name|args
index|[
name|index
index|]
argument_list|,
name|argOIs
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|forwardObj
index|[
name|ii
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|forward
argument_list|(
name|forwardObj
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"stack"
return|;
block|}
block|}
end_class

end_unit


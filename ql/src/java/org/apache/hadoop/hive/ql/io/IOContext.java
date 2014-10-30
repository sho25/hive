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
name|io
package|;
end_package

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
name|conf
operator|.
name|HiveConf
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
name|Utilities
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
name|optimizer
operator|.
name|ConvertJoinMapJoin
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * IOContext basically contains the position information of the current  * key/value. For blockCompressed files, isBlockPointer should return true,  * and currentBlockStart refers to the RCFile Block or SequenceFile Block. For  * non compressed files, isBlockPointer should return false, and  * currentBlockStart refers to the beginning offset of the current row,  * nextBlockStart refers the end of current row and beginning of next row.  */
end_comment

begin_class
specifier|public
class|class
name|IOContext
block|{
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|IOContext
argument_list|>
name|threadLocal
init|=
operator|new
name|ThreadLocal
argument_list|<
name|IOContext
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
specifier|synchronized
name|IOContext
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|IOContext
argument_list|()
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|IOContext
argument_list|>
name|inputNameIOContextMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|IOContext
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|IOContext
argument_list|>
name|getMap
parameter_list|()
block|{
return|return
name|inputNameIOContextMap
return|;
block|}
specifier|public
specifier|static
name|IOContext
name|get
parameter_list|()
block|{
return|return
name|IOContext
operator|.
name|threadLocal
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|IOContext
name|get
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
return|return
name|get
argument_list|()
return|;
block|}
name|String
name|inputName
init|=
name|conf
operator|.
name|get
argument_list|(
name|Utilities
operator|.
name|INPUT_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputNameIOContextMap
operator|.
name|containsKey
argument_list|(
name|inputName
argument_list|)
operator|==
literal|false
condition|)
block|{
name|IOContext
name|ioContext
init|=
operator|new
name|IOContext
argument_list|()
decl_stmt|;
name|inputNameIOContextMap
operator|.
name|put
argument_list|(
name|inputName
argument_list|,
name|ioContext
argument_list|)
expr_stmt|;
block|}
return|return
name|inputNameIOContextMap
operator|.
name|get
argument_list|(
name|inputName
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|clear
parameter_list|()
block|{
name|IOContext
operator|.
name|threadLocal
operator|.
name|remove
argument_list|()
expr_stmt|;
name|inputNameIOContextMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|long
name|currentBlockStart
decl_stmt|;
name|long
name|nextBlockStart
decl_stmt|;
name|long
name|currentRow
decl_stmt|;
name|boolean
name|isBlockPointer
decl_stmt|;
name|boolean
name|ioExceptions
decl_stmt|;
comment|// Are we using the fact the input is sorted
name|boolean
name|useSorted
init|=
literal|false
decl_stmt|;
comment|// Are we currently performing a binary search
name|boolean
name|isBinarySearching
init|=
literal|false
decl_stmt|;
comment|// Do we want to end the binary search
name|boolean
name|endBinarySearch
init|=
literal|false
decl_stmt|;
comment|// The result of the comparison of the last row processed
name|Comparison
name|comparison
init|=
literal|null
decl_stmt|;
comment|// The class name of the generic UDF being used by the filter
name|String
name|genericUDFClassName
init|=
literal|null
decl_stmt|;
comment|/**    * supports {@link org.apache.hadoop.hive.ql.metadata.VirtualColumn#ROWID}    */
specifier|public
name|RecordIdentifier
name|ri
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|Comparison
block|{
name|GREATER
block|,
name|LESS
block|,
name|EQUAL
block|,
name|UNKNOWN
block|}
name|Path
name|inputPath
decl_stmt|;
specifier|public
name|IOContext
parameter_list|()
block|{
name|this
operator|.
name|currentBlockStart
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|nextBlockStart
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|currentRow
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|isBlockPointer
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|ioExceptions
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Copy all fields values from orig to dest, all existing fields in dest will be overwritten.    *    * @param dest the IOContext to copy to    * @param orig the IOContext to copy from    */
specifier|public
specifier|static
name|void
name|copy
parameter_list|(
name|IOContext
name|dest
parameter_list|,
name|IOContext
name|orig
parameter_list|)
block|{
name|dest
operator|.
name|currentBlockStart
operator|=
name|orig
operator|.
name|currentBlockStart
expr_stmt|;
name|dest
operator|.
name|nextBlockStart
operator|=
name|orig
operator|.
name|nextBlockStart
expr_stmt|;
name|dest
operator|.
name|currentRow
operator|=
name|orig
operator|.
name|currentRow
expr_stmt|;
name|dest
operator|.
name|isBlockPointer
operator|=
name|orig
operator|.
name|isBlockPointer
expr_stmt|;
name|dest
operator|.
name|ioExceptions
operator|=
name|orig
operator|.
name|ioExceptions
expr_stmt|;
name|dest
operator|.
name|useSorted
operator|=
name|orig
operator|.
name|useSorted
expr_stmt|;
name|dest
operator|.
name|isBinarySearching
operator|=
name|orig
operator|.
name|isBinarySearching
expr_stmt|;
name|dest
operator|.
name|endBinarySearch
operator|=
name|orig
operator|.
name|endBinarySearch
expr_stmt|;
name|dest
operator|.
name|comparison
operator|=
name|orig
operator|.
name|comparison
expr_stmt|;
name|dest
operator|.
name|genericUDFClassName
operator|=
name|orig
operator|.
name|genericUDFClassName
expr_stmt|;
name|dest
operator|.
name|ri
operator|=
name|orig
operator|.
name|ri
expr_stmt|;
name|dest
operator|.
name|inputPath
operator|=
name|orig
operator|.
name|inputPath
expr_stmt|;
block|}
specifier|public
name|long
name|getCurrentBlockStart
parameter_list|()
block|{
return|return
name|currentBlockStart
return|;
block|}
specifier|public
name|void
name|setCurrentBlockStart
parameter_list|(
name|long
name|currentBlockStart
parameter_list|)
block|{
name|this
operator|.
name|currentBlockStart
operator|=
name|currentBlockStart
expr_stmt|;
block|}
specifier|public
name|long
name|getNextBlockStart
parameter_list|()
block|{
return|return
name|nextBlockStart
return|;
block|}
specifier|public
name|void
name|setNextBlockStart
parameter_list|(
name|long
name|nextBlockStart
parameter_list|)
block|{
name|this
operator|.
name|nextBlockStart
operator|=
name|nextBlockStart
expr_stmt|;
block|}
specifier|public
name|long
name|getCurrentRow
parameter_list|()
block|{
return|return
name|currentRow
return|;
block|}
specifier|public
name|void
name|setCurrentRow
parameter_list|(
name|long
name|currentRow
parameter_list|)
block|{
name|this
operator|.
name|currentRow
operator|=
name|currentRow
expr_stmt|;
block|}
specifier|public
name|boolean
name|isBlockPointer
parameter_list|()
block|{
return|return
name|isBlockPointer
return|;
block|}
specifier|public
name|void
name|setBlockPointer
parameter_list|(
name|boolean
name|isBlockPointer
parameter_list|)
block|{
name|this
operator|.
name|isBlockPointer
operator|=
name|isBlockPointer
expr_stmt|;
block|}
specifier|public
name|Path
name|getInputPath
parameter_list|()
block|{
return|return
name|inputPath
return|;
block|}
specifier|public
name|void
name|setInputPath
parameter_list|(
name|Path
name|inputPath
parameter_list|)
block|{
name|this
operator|.
name|inputPath
operator|=
name|inputPath
expr_stmt|;
block|}
specifier|public
name|void
name|setIOExceptions
parameter_list|(
name|boolean
name|ioe
parameter_list|)
block|{
name|this
operator|.
name|ioExceptions
operator|=
name|ioe
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIOExceptions
parameter_list|()
block|{
return|return
name|ioExceptions
return|;
block|}
specifier|public
name|boolean
name|useSorted
parameter_list|()
block|{
return|return
name|useSorted
return|;
block|}
specifier|public
name|void
name|setUseSorted
parameter_list|(
name|boolean
name|useSorted
parameter_list|)
block|{
name|this
operator|.
name|useSorted
operator|=
name|useSorted
expr_stmt|;
block|}
specifier|public
name|boolean
name|isBinarySearching
parameter_list|()
block|{
return|return
name|isBinarySearching
return|;
block|}
specifier|public
name|void
name|setIsBinarySearching
parameter_list|(
name|boolean
name|isBinarySearching
parameter_list|)
block|{
name|this
operator|.
name|isBinarySearching
operator|=
name|isBinarySearching
expr_stmt|;
block|}
specifier|public
name|boolean
name|shouldEndBinarySearch
parameter_list|()
block|{
return|return
name|endBinarySearch
return|;
block|}
specifier|public
name|void
name|setEndBinarySearch
parameter_list|(
name|boolean
name|endBinarySearch
parameter_list|)
block|{
name|this
operator|.
name|endBinarySearch
operator|=
name|endBinarySearch
expr_stmt|;
block|}
specifier|public
name|Comparison
name|getComparison
parameter_list|()
block|{
return|return
name|comparison
return|;
block|}
specifier|public
name|void
name|setComparison
parameter_list|(
name|Integer
name|comparison
parameter_list|)
block|{
if|if
condition|(
name|comparison
operator|==
literal|null
operator|&&
name|this
operator|.
name|isBinarySearching
condition|)
block|{
comment|// Nothing we can do here, so just proceed normally from now on
name|endBinarySearch
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|comparison
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|comparison
operator|=
name|Comparison
operator|.
name|UNKNOWN
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|comparison
operator|.
name|intValue
argument_list|()
operator|<
literal|0
condition|)
block|{
name|this
operator|.
name|comparison
operator|=
name|Comparison
operator|.
name|LESS
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|comparison
operator|.
name|intValue
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|comparison
operator|=
name|Comparison
operator|.
name|GREATER
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|comparison
operator|=
name|Comparison
operator|.
name|EQUAL
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|String
name|getGenericUDFClassName
parameter_list|()
block|{
return|return
name|genericUDFClassName
return|;
block|}
specifier|public
name|void
name|setGenericUDFClassName
parameter_list|(
name|String
name|genericUDFClassName
parameter_list|)
block|{
name|this
operator|.
name|genericUDFClassName
operator|=
name|genericUDFClassName
expr_stmt|;
block|}
comment|/**    * The thread local IOContext is static, we may need to restart the search if, for instance,    * multiple files are being searched as part of a CombinedHiveRecordReader    */
specifier|public
name|void
name|resetSortingValues
parameter_list|()
block|{
name|this
operator|.
name|useSorted
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|isBinarySearching
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|endBinarySearch
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|comparison
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|genericUDFClassName
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit


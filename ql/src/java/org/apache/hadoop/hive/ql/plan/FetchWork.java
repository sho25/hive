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
name|plan
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|Arrays
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
name|CompilationOpContext
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
name|ListSinkOperator
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
name|Operator
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
name|OperatorFactory
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
name|SplitSample
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
name|plan
operator|.
name|Explain
operator|.
name|Level
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

begin_comment
comment|/**  * FetchWork.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Fetch Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|FetchWork
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|Path
name|tblDir
decl_stmt|;
specifier|private
name|TableDesc
name|tblDesc
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|partDir
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
name|partDesc
decl_stmt|;
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|source
decl_stmt|;
specifier|private
name|ListSinkOperator
name|sink
decl_stmt|;
specifier|private
name|int
name|limit
decl_stmt|;
specifier|private
name|int
name|leastNumRows
decl_stmt|;
specifier|private
name|SplitSample
name|splitSample
decl_stmt|;
specifier|private
specifier|transient
name|List
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rowsComputedFromStats
decl_stmt|;
specifier|private
specifier|transient
name|StructObjectInspector
name|statRowOI
decl_stmt|;
comment|/**    * Serialization Null Format for the serde used to fetch data.    */
specifier|private
name|String
name|serializationNullFormat
init|=
literal|"NULL"
decl_stmt|;
specifier|public
name|FetchWork
parameter_list|()
block|{   }
specifier|public
name|FetchWork
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rowsComputedFromStats
parameter_list|,
name|StructObjectInspector
name|statRowOI
parameter_list|)
block|{
name|this
operator|.
name|rowsComputedFromStats
operator|=
name|rowsComputedFromStats
expr_stmt|;
name|this
operator|.
name|statRowOI
operator|=
name|statRowOI
expr_stmt|;
block|}
specifier|public
name|StructObjectInspector
name|getStatRowOI
parameter_list|()
block|{
return|return
name|statRowOI
return|;
block|}
specifier|public
name|List
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|getRowsComputedUsingStats
parameter_list|()
block|{
return|return
name|rowsComputedFromStats
return|;
block|}
specifier|public
name|FetchWork
parameter_list|(
name|Path
name|tblDir
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|tblDir
argument_list|,
name|tblDesc
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FetchWork
parameter_list|(
name|Path
name|tblDir
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|tblDir
operator|=
name|tblDir
expr_stmt|;
name|this
operator|.
name|tblDesc
operator|=
name|tblDesc
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|FetchWork
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|partDir
parameter_list|,
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|partDesc
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|)
block|{
name|this
argument_list|(
name|partDir
argument_list|,
name|partDesc
argument_list|,
name|tblDesc
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FetchWork
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|partDir
parameter_list|,
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|partDesc
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|tblDesc
operator|=
name|tblDesc
expr_stmt|;
name|this
operator|.
name|partDir
operator|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|(
name|partDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|partDesc
operator|=
operator|new
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
argument_list|(
name|partDesc
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|void
name|initializeForFetch
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|ListSinkDesc
name|desc
init|=
operator|new
name|ListSinkDesc
argument_list|(
name|serializationNullFormat
argument_list|)
decl_stmt|;
name|sink
operator|=
operator|(
name|ListSinkOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|ctx
argument_list|,
name|desc
argument_list|)
expr_stmt|;
name|source
operator|=
name|sink
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getSerializationNullFormat
parameter_list|()
block|{
return|return
name|serializationNullFormat
return|;
block|}
specifier|public
name|void
name|setSerializationNullFormat
parameter_list|(
name|String
name|format
parameter_list|)
block|{
name|serializationNullFormat
operator|=
name|format
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNotPartitioned
parameter_list|()
block|{
return|return
name|tblDir
operator|!=
literal|null
return|;
block|}
specifier|public
name|boolean
name|isPartitioned
parameter_list|()
block|{
return|return
name|tblDir
operator|==
literal|null
return|;
block|}
comment|/**    * @return the tblDir    */
specifier|public
name|Path
name|getTblDir
parameter_list|()
block|{
return|return
name|tblDir
return|;
block|}
comment|/**    * @param tblDir    *          the tblDir to set    */
specifier|public
name|void
name|setTblDir
parameter_list|(
name|Path
name|tblDir
parameter_list|)
block|{
name|this
operator|.
name|tblDir
operator|=
name|tblDir
expr_stmt|;
block|}
comment|/**    * @return the tblDesc    */
specifier|public
name|TableDesc
name|getTblDesc
parameter_list|()
block|{
return|return
name|tblDesc
return|;
block|}
comment|/**    * @param tblDesc    *          the tblDesc to set    */
specifier|public
name|void
name|setTblDesc
parameter_list|(
name|TableDesc
name|tblDesc
parameter_list|)
block|{
name|this
operator|.
name|tblDesc
operator|=
name|tblDesc
expr_stmt|;
block|}
comment|/**    * @return the partDir    */
specifier|public
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|getPartDir
parameter_list|()
block|{
return|return
name|partDir
return|;
block|}
comment|/**    * @param partDir    *          the partDir to set    */
specifier|public
name|void
name|setPartDir
parameter_list|(
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|partDir
parameter_list|)
block|{
name|this
operator|.
name|partDir
operator|=
name|partDir
expr_stmt|;
block|}
comment|/**    * @return the partDesc    */
specifier|public
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
name|getPartDesc
parameter_list|()
block|{
return|return
name|partDesc
return|;
block|}
specifier|public
name|List
argument_list|<
name|Path
argument_list|>
name|getPathLists
parameter_list|()
block|{
return|return
name|isPartitioned
argument_list|()
condition|?
name|partDir
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|(
name|partDir
argument_list|)
else|:
name|Arrays
operator|.
name|asList
argument_list|(
name|tblDir
argument_list|)
return|;
block|}
comment|/**    * Get Partition descriptors in sorted (ascending) order of partition directory    *    * @return the partDesc array list    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Partition Description"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
name|getPartDescOrderedByPartDir
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
name|partDescOrdered
init|=
name|partDesc
decl_stmt|;
if|if
condition|(
name|partDir
operator|!=
literal|null
operator|&&
name|partDir
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
if|if
condition|(
name|partDesc
operator|==
literal|null
operator|||
name|partDir
operator|.
name|size
argument_list|()
operator|!=
name|partDesc
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Partiton Directory list size doesn't match Partition Descriptor list size"
argument_list|)
throw|;
block|}
comment|// Construct a sorted Map of Partition Dir - Partition Descriptor; ordering is based on
comment|// patition dir (map key)
comment|// Assumption: there is a 1-1 mapping between partition dir and partition descriptor lists
name|TreeMap
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|partDirToPartSpecMap
init|=
operator|new
name|TreeMap
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
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
name|partDir
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|partDirToPartSpecMap
operator|.
name|put
argument_list|(
name|partDir
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|partDesc
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Extract partition desc from sorted map (ascending order of part dir)
name|partDescOrdered
operator|=
operator|new
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
argument_list|(
name|partDirToPartSpecMap
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|partDescOrdered
return|;
block|}
comment|/**    * @return the partDescs for paths    */
specifier|public
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|getPartDescs
parameter_list|(
name|List
argument_list|<
name|Path
argument_list|>
name|paths
parameter_list|)
block|{
name|List
argument_list|<
name|PartitionDesc
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
argument_list|(
name|paths
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
name|parts
operator|.
name|add
argument_list|(
name|partDesc
operator|.
name|get
argument_list|(
name|partDir
operator|.
name|indexOf
argument_list|(
name|path
operator|.
name|getParent
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|parts
return|;
block|}
comment|/**    * @param partDesc    *          the partDesc to set    */
specifier|public
name|void
name|setPartDesc
parameter_list|(
name|ArrayList
argument_list|<
name|PartitionDesc
argument_list|>
name|partDesc
parameter_list|)
block|{
name|this
operator|.
name|partDesc
operator|=
name|partDesc
expr_stmt|;
block|}
comment|/**    * @return the limit    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"limit"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|int
name|getLimit
parameter_list|()
block|{
return|return
name|limit
return|;
block|}
comment|/**    * @param limit    *          the limit to set    */
specifier|public
name|void
name|setLimit
parameter_list|(
name|int
name|limit
parameter_list|)
block|{
name|this
operator|.
name|limit
operator|=
name|limit
expr_stmt|;
block|}
specifier|public
name|int
name|getLeastNumRows
parameter_list|()
block|{
return|return
name|leastNumRows
return|;
block|}
specifier|public
name|void
name|setLeastNumRows
parameter_list|(
name|int
name|leastNumRows
parameter_list|)
block|{
name|this
operator|.
name|leastNumRows
operator|=
name|leastNumRows
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Processor Tree"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
specifier|public
name|void
name|setSource
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
specifier|public
name|ListSinkOperator
name|getSink
parameter_list|()
block|{
return|return
name|sink
return|;
block|}
specifier|public
name|void
name|setSink
parameter_list|(
name|ListSinkOperator
name|sink
parameter_list|)
block|{
name|this
operator|.
name|sink
operator|=
name|sink
expr_stmt|;
block|}
specifier|public
name|void
name|setSplitSample
parameter_list|(
name|SplitSample
name|splitSample
parameter_list|)
block|{
name|this
operator|.
name|splitSample
operator|=
name|splitSample
expr_stmt|;
block|}
specifier|public
name|SplitSample
name|getSplitSample
parameter_list|()
block|{
return|return
name|splitSample
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|tblDir
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|String
argument_list|(
literal|"table = "
operator|+
name|tblDir
argument_list|)
return|;
block|}
if|if
condition|(
name|partDir
operator|==
literal|null
condition|)
block|{
return|return
literal|"null fetchwork"
return|;
block|}
name|String
name|ret
init|=
literal|"partition = "
decl_stmt|;
for|for
control|(
name|Path
name|part
range|:
name|partDir
control|)
block|{
name|ret
operator|=
name|ret
operator|.
name|concat
argument_list|(
name|part
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
block|}
end_class

end_unit


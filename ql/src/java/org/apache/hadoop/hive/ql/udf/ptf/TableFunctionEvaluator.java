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
name|ptf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|PTFOperator
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
name|PTFPartition
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
name|PTFPartition
operator|.
name|PTFPartitionIterator
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
name|PTFUtils
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
name|plan
operator|.
name|PTFDesc
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
name|ptf
operator|.
name|PartitionedTableFunctionDef
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
name|GenericUDAFEvaluator
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
comment|/**  * Based on Hive {@link GenericUDAFEvaluator}. Break up the responsibility of the old AsbtractTableFunction  * class into a Resolver and Evaluator.  *<p>  * The Evaluator also holds onto the {@link TableFunctionDef}. This provides information  * about the arguments to the function, the shape of the Input partition and the Partitioning details.  * The Evaluator is responsible for providing the 2 execute methods:  *<ol>  *<li><b>execute:</b> which is invoked after the input is partitioned; the contract  * is, it is given an input Partition and must return an output Partition. The shape of the output  * Partition is obtained from the getOutputOI call.  *<li><b>transformRawInput:</b> In the case where this function indicates that it will transform the raw input  * before it is fed through the partitioning mechanics, this function is called. Again the contract is  * t is given an input Partition and must return an Partition. The shape of the output Partition is  * obtained from getRawInputOI() call.  *</ol>  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TableFunctionEvaluator
block|{
comment|/*    * how is this different from the OutpuShape set on the TableDef.    * This is the OI of the object coming out of the PTF.    * It is put in an output Partition whose Serde is usually LazyBinarySerde.    * So the next PTF (or Operator) in the chain gets a LazyBinaryStruct.    */
specifier|transient
specifier|protected
name|StructObjectInspector
name|OI
decl_stmt|;
comment|/*    * same comment as OI applies here.    */
specifier|transient
specifier|protected
name|StructObjectInspector
name|rawInputOI
decl_stmt|;
specifier|protected
name|PartitionedTableFunctionDef
name|tableDef
decl_stmt|;
specifier|protected
name|PTFDesc
name|ptfDesc
decl_stmt|;
name|boolean
name|transformsRawInput
decl_stmt|;
specifier|transient
specifier|protected
name|PTFPartition
name|outputPartition
decl_stmt|;
static|static
block|{
comment|//TODO is this a bug? The field is not named outputOI it is named OI
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|TableFunctionEvaluator
operator|.
name|class
argument_list|,
literal|"outputOI"
argument_list|,
literal|"rawInputOI"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|StructObjectInspector
name|getOutputOI
parameter_list|()
block|{
return|return
name|OI
return|;
block|}
specifier|protected
name|void
name|setOutputOI
parameter_list|(
name|StructObjectInspector
name|outputOI
parameter_list|)
block|{
name|OI
operator|=
name|outputOI
expr_stmt|;
block|}
specifier|public
name|PartitionedTableFunctionDef
name|getTableDef
parameter_list|()
block|{
return|return
name|tableDef
return|;
block|}
specifier|public
name|void
name|setTableDef
parameter_list|(
name|PartitionedTableFunctionDef
name|tDef
parameter_list|)
block|{
name|this
operator|.
name|tableDef
operator|=
name|tDef
expr_stmt|;
block|}
specifier|protected
name|PTFDesc
name|getQueryDef
parameter_list|()
block|{
return|return
name|ptfDesc
return|;
block|}
specifier|protected
name|void
name|setQueryDef
parameter_list|(
name|PTFDesc
name|ptfDesc
parameter_list|)
block|{
name|this
operator|.
name|ptfDesc
operator|=
name|ptfDesc
expr_stmt|;
block|}
specifier|public
name|StructObjectInspector
name|getRawInputOI
parameter_list|()
block|{
return|return
name|rawInputOI
return|;
block|}
specifier|protected
name|void
name|setRawInputOI
parameter_list|(
name|StructObjectInspector
name|rawInputOI
parameter_list|)
block|{
name|this
operator|.
name|rawInputOI
operator|=
name|rawInputOI
expr_stmt|;
block|}
specifier|public
name|boolean
name|isTransformsRawInput
parameter_list|()
block|{
return|return
name|transformsRawInput
return|;
block|}
specifier|public
name|void
name|setTransformsRawInput
parameter_list|(
name|boolean
name|transformsRawInput
parameter_list|)
block|{
name|this
operator|.
name|transformsRawInput
operator|=
name|transformsRawInput
expr_stmt|;
block|}
specifier|public
name|PTFPartition
name|execute
parameter_list|(
name|PTFPartition
name|iPart
parameter_list|)
throws|throws
name|HiveException
block|{
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
init|=
name|iPart
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|PTFOperator
operator|.
name|connectLeadLagFunctionsToPartition
argument_list|(
name|ptfDesc
argument_list|,
name|pItr
argument_list|)
expr_stmt|;
if|if
condition|(
name|outputPartition
operator|==
literal|null
condition|)
block|{
name|outputPartition
operator|=
name|PTFPartition
operator|.
name|create
argument_list|(
name|ptfDesc
operator|.
name|getCfg
argument_list|()
argument_list|,
name|tableDef
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getSerde
argument_list|()
argument_list|,
name|OI
argument_list|,
name|tableDef
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getOI
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputPartition
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|execute
argument_list|(
name|pItr
argument_list|,
name|outputPartition
argument_list|)
expr_stmt|;
return|return
name|outputPartition
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|execute
parameter_list|(
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
parameter_list|,
name|PTFPartition
name|oPart
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|public
name|PTFPartition
name|transformRawInput
parameter_list|(
name|PTFPartition
name|iPart
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|isTransformsRawInput
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Internal Error: mapExecute called on function (%s)that has no Map Phase"
argument_list|,
name|tableDef
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|_transformRawInput
argument_list|(
name|iPart
argument_list|)
return|;
block|}
specifier|protected
name|PTFPartition
name|_transformRawInput
parameter_list|(
name|PTFPartition
name|iPart
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
literal|null
return|;
block|}
comment|/*    * A TableFunction may be able to provide its Output as an Iterator.    * In case it can then for Map-side processing and for the last PTF in a Reduce-side chain    * we can forward rows one by one. This will save the time/space to populate and read an Output    * Partition.    */
specifier|public
name|boolean
name|canIterateOutput
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|(
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|canIterateOutput
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Internal error: iterator called on a PTF that cannot provide its output as an Iterator"
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Internal error: PTF %s, provides no iterator method"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|transformRawInputIterator
parameter_list|(
name|PTFPartitionIterator
argument_list|<
name|Object
argument_list|>
name|pItr
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|canIterateOutput
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Internal error: iterator called on a PTF that cannot provide its output as an Iterator"
argument_list|)
throw|;
block|}
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Internal error: PTF %s, provides no iterator method"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|outputPartition
operator|!=
literal|null
condition|)
block|{
name|outputPartition
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|outputPartition
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit


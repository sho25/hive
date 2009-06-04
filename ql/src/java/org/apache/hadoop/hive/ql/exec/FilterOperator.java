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
name|exec
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
name|filterDesc
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
name|InspectableObject
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
name|io
operator|.
name|BooleanWritable
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
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_comment
comment|/**  * Filter operator implementation  **/
end_comment

begin_class
specifier|public
class|class
name|FilterOperator
extends|extends
name|Operator
argument_list|<
name|filterDesc
argument_list|>
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
specifier|public
specifier|static
enum|enum
name|Counter
block|{
name|FILTERED
block|,
name|PASSED
block|}
specifier|transient
specifier|private
specifier|final
name|LongWritable
name|filtered_count
decl_stmt|,
name|passed_count
decl_stmt|;
specifier|transient
specifier|private
name|ExprNodeEvaluator
name|conditionEvaluator
decl_stmt|;
specifier|transient
specifier|private
name|PrimitiveObjectInspector
name|conditionInspector
decl_stmt|;
specifier|public
name|FilterOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|filtered_count
operator|=
operator|new
name|LongWritable
argument_list|()
expr_stmt|;
name|passed_count
operator|=
operator|new
name|LongWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Reporter
name|reporter
parameter_list|,
name|ObjectInspector
index|[]
name|inputObjInspector
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|this
operator|.
name|conditionEvaluator
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|conf
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|FILTERED
argument_list|,
name|filtered_count
argument_list|)
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|PASSED
argument_list|,
name|passed_count
argument_list|)
expr_stmt|;
name|conditionInspector
operator|=
literal|null
expr_stmt|;
name|initializeChildren
argument_list|(
name|hconf
argument_list|,
name|reporter
argument_list|,
name|inputObjInspector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|conditionInspector
operator|==
literal|null
condition|)
block|{
name|conditionInspector
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|conditionEvaluator
operator|.
name|initialize
argument_list|(
name|rowInspector
argument_list|)
expr_stmt|;
block|}
name|Object
name|condition
init|=
name|conditionEvaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Boolean
name|ret
init|=
operator|(
name|Boolean
operator|)
name|conditionInspector
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|condition
argument_list|)
decl_stmt|;
if|if
condition|(
name|Boolean
operator|.
name|TRUE
operator|.
name|equals
argument_list|(
name|ret
argument_list|)
condition|)
block|{
name|forward
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
name|passed_count
operator|.
name|set
argument_list|(
name|passed_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|filtered_count
operator|.
name|set
argument_list|(
name|filtered_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
operator|new
name|String
argument_list|(
literal|"FIL"
argument_list|)
return|;
block|}
block|}
end_class

end_unit


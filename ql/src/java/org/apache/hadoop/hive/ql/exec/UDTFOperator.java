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
name|UDTFDesc
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
name|api
operator|.
name|OperatorType
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
name|UDTFCollector
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
name|StandardStructObjectInspector
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
name|StructObjectInspector
import|;
end_import

begin_comment
comment|/**  * UDTFOperator.  *  */
end_comment

begin_class
specifier|public
class|class
name|UDTFOperator
extends|extends
name|Operator
argument_list|<
name|UDTFDesc
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
specifier|protected
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|udtfInputOIs
init|=
literal|null
decl_stmt|;
name|Object
index|[]
name|objToSendToUDTF
init|=
literal|null
decl_stmt|;
name|Object
index|[]
name|forwardObj
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
comment|/**    * sends periodic reports back to the tracker.    */
specifier|transient
name|AutoProgressor
name|autoProgressor
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|conf
operator|.
name|getGenericUDTF
argument_list|()
operator|.
name|setCollector
argument_list|(
operator|new
name|UDTFCollector
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make an object inspector [] of the arguments to the UDTF
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|inputFields
init|=
operator|(
operator|(
name|StandardStructObjectInspector
operator|)
name|inputObjInspectors
index|[
literal|0
index|]
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|udtfInputOIs
operator|=
operator|new
name|ObjectInspector
index|[
name|inputFields
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inputFields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|udtfInputOIs
index|[
name|i
index|]
operator|=
name|inputFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
expr_stmt|;
block|}
name|objToSendToUDTF
operator|=
operator|new
name|Object
index|[
name|inputFields
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|StructObjectInspector
name|udtfOutputOI
init|=
name|conf
operator|.
name|getGenericUDTF
argument_list|()
operator|.
name|initialize
argument_list|(
name|udtfInputOIs
argument_list|)
decl_stmt|;
comment|// Since we're passing the object output by the UDTF directly to the next
comment|// operator, we can use the same OI.
name|outputObjInspector
operator|=
name|udtfOutputOI
expr_stmt|;
comment|// Set up periodic progress reporting in case the UDTF doesn't output rows
comment|// for a while
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEUDTFAUTOPROGRESS
argument_list|)
condition|)
block|{
name|autoProgressor
operator|=
operator|new
name|AutoProgressor
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|reporter
argument_list|,
name|Utilities
operator|.
name|getDefaultNotificationInterval
argument_list|(
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
name|autoProgressor
operator|.
name|go
argument_list|()
expr_stmt|;
block|}
comment|// Initialize the rest of the operator DAG
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// The UDTF expects arguments in an object[]
name|StandardStructObjectInspector
name|soi
init|=
operator|(
name|StandardStructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|tag
index|]
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|objToSendToUDTF
index|[
name|i
index|]
operator|=
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|getGenericUDTF
argument_list|()
operator|.
name|process
argument_list|(
name|objToSendToUDTF
argument_list|)
expr_stmt|;
block|}
comment|/**    * forwardUDTFOutput is typically called indirectly by the GenericUDTF when    * the GenericUDTF has generated output rows that should be passed on to the    * next operator(s) in the DAG.    *    * @param o    * @throws HiveException    */
specifier|public
name|void
name|forwardUDTFOutput
parameter_list|(
name|Object
name|o
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Since the output of the UDTF is a struct, we can just forward that
name|forward
argument_list|(
name|o
argument_list|,
name|outputObjInspector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"UDTF"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|UDTF
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
name|conf
operator|.
name|getGenericUDTF
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit


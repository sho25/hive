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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|Void
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
name|FunctionInfo
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
name|ObjectInspectorUtils
import|;
end_import

begin_class
specifier|public
class|class
name|FunctionRegistry
block|{
specifier|private
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hive.ql.exec.FunctionRegistry"
argument_list|)
decl_stmt|;
comment|/**    * The mapping from expression function names to expression classes.    */
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|FunctionInfo
argument_list|>
name|mFunctions
decl_stmt|;
static|static
block|{
name|mFunctions
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|FunctionInfo
argument_list|>
argument_list|()
expr_stmt|;
name|registerUDF
argument_list|(
literal|"default_sample_hashfn"
argument_list|,
name|UDFDefaultSampleHashFn
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"concat"
argument_list|,
name|UDFConcat
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"substr"
argument_list|,
name|UDFSubstr
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"str_eq"
argument_list|,
name|UDFStrEq
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"str_ne"
argument_list|,
name|UDFStrNe
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"str_gt"
argument_list|,
name|UDFStrGt
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"str_lt"
argument_list|,
name|UDFStrLt
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"str_ge"
argument_list|,
name|UDFStrGe
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"str_le"
argument_list|,
name|UDFStrLe
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"upper"
argument_list|,
name|UDFUpper
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"lower"
argument_list|,
name|UDFLower
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"ucase"
argument_list|,
name|UDFUpper
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"lcase"
argument_list|,
name|UDFLower
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"trim"
argument_list|,
name|UDFTrim
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"ltrim"
argument_list|,
name|UDFLTrim
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"rtrim"
argument_list|,
name|UDFRTrim
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"like"
argument_list|,
name|UDFLike
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"rlike"
argument_list|,
name|UDFRegExp
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"regexp"
argument_list|,
name|UDFRegExp
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"regexp_replace"
argument_list|,
name|UDFRegExpReplace
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"+"
argument_list|,
name|UDFOPPlus
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"-"
argument_list|,
name|UDFOPMinus
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"*"
argument_list|,
name|UDFOPMultiply
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"/"
argument_list|,
name|UDFOPDivide
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"%"
argument_list|,
name|UDFOPMod
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"&"
argument_list|,
name|UDFOPBitAnd
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"|"
argument_list|,
name|UDFOPBitOr
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"^"
argument_list|,
name|UDFOPBitXor
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"~"
argument_list|,
name|UDFOPBitNot
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"="
argument_list|,
name|UDFOPEqual
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"=="
argument_list|,
name|UDFOPEqual
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|,
literal|"="
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"<>"
argument_list|,
name|UDFOPNotEqual
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"<"
argument_list|,
name|UDFOPLessThan
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"<="
argument_list|,
name|UDFOPEqualOrLessThan
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|">"
argument_list|,
name|UDFOPGreaterThan
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|">="
argument_list|,
name|UDFOPEqualOrGreaterThan
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"and"
argument_list|,
name|UDFOPAnd
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"&&"
argument_list|,
name|UDFOPAnd
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|,
literal|"and"
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"or"
argument_list|,
name|UDFOPOr
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"||"
argument_list|,
name|UDFOPOr
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|,
literal|"or"
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"not"
argument_list|,
name|UDFOPNot
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"!"
argument_list|,
name|UDFOPNot
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|INFIX
argument_list|,
literal|true
argument_list|,
literal|"not"
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"isnull"
argument_list|,
name|UDFOPNull
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|POSTFIX
argument_list|,
literal|true
argument_list|,
literal|"is null"
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
literal|"isnotnull"
argument_list|,
name|UDFOPNotNull
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|POSTFIX
argument_list|,
literal|true
argument_list|,
literal|"is not null"
argument_list|)
expr_stmt|;
comment|// Aliases for Java Class Names
comment|// These are used in getImplicitConvertUDFMethod
name|registerUDF
argument_list|(
name|Boolean
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToBoolean
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToBoolean
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|Byte
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToByte
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToByte
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|Integer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToInteger
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToInteger
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|Long
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToLong
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToLong
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|Float
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToFloat
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToFloat
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|Double
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToDouble
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToDouble
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|String
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToString
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToString
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|registerUDF
argument_list|(
name|java
operator|.
name|sql
operator|.
name|Date
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|UDFToDate
operator|.
name|class
argument_list|,
name|OperatorType
operator|.
name|PREFIX
argument_list|,
literal|false
argument_list|,
name|UDFToDate
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Aggregate functions
name|registerUDAF
argument_list|(
literal|"sum"
argument_list|,
name|UDAFSum
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerUDAF
argument_list|(
literal|"count"
argument_list|,
name|UDAFCount
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerUDAF
argument_list|(
literal|"max"
argument_list|,
name|UDAFMax
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerUDAF
argument_list|(
literal|"min"
argument_list|,
name|UDAFMin
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerUDAF
argument_list|(
literal|"avg"
argument_list|,
name|UDAFAvg
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|FunctionInfo
name|getInfo
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|fClass
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|FunctionInfo
argument_list|>
name|ent
range|:
name|mFunctions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|FunctionInfo
name|val
init|=
name|ent
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|.
name|getUDFClass
argument_list|()
operator|==
name|fClass
operator|||
name|val
operator|.
name|getUDAFClass
argument_list|()
operator|==
name|fClass
condition|)
block|{
return|return
name|val
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|void
name|registerUDF
parameter_list|(
name|String
name|functionName
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|UDFClass
parameter_list|,
name|FunctionInfo
operator|.
name|OperatorType
name|opt
parameter_list|,
name|boolean
name|isOperator
parameter_list|)
block|{
if|if
condition|(
name|UDF
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|UDFClass
argument_list|)
condition|)
block|{
name|FunctionInfo
name|fI
init|=
operator|new
name|FunctionInfo
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|UDFClass
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|fI
operator|.
name|setIsOperator
argument_list|(
name|isOperator
argument_list|)
expr_stmt|;
name|fI
operator|.
name|setOpType
argument_list|(
name|opt
argument_list|)
expr_stmt|;
name|mFunctions
operator|.
name|put
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|fI
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Registering UDF Class "
operator|+
name|UDFClass
operator|+
literal|" which does not extends "
operator|+
name|UDF
operator|.
name|class
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|registerUDF
parameter_list|(
name|String
name|functionName
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|UDFClass
parameter_list|,
name|FunctionInfo
operator|.
name|OperatorType
name|opt
parameter_list|,
name|boolean
name|isOperator
parameter_list|,
name|String
name|displayName
parameter_list|)
block|{
if|if
condition|(
name|UDF
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|UDFClass
argument_list|)
condition|)
block|{
name|FunctionInfo
name|fI
init|=
operator|new
name|FunctionInfo
argument_list|(
name|displayName
argument_list|,
name|UDFClass
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|fI
operator|.
name|setIsOperator
argument_list|(
name|isOperator
argument_list|)
expr_stmt|;
name|fI
operator|.
name|setOpType
argument_list|(
name|opt
argument_list|)
expr_stmt|;
name|mFunctions
operator|.
name|put
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|fI
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Registering UDF Class "
operator|+
name|UDFClass
operator|+
literal|" which does not extends "
operator|+
name|UDF
operator|.
name|class
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|getUDFClass
parameter_list|(
name|String
name|functionName
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking up: "
operator|+
name|functionName
argument_list|)
expr_stmt|;
name|FunctionInfo
name|finfo
init|=
name|mFunctions
operator|.
name|get
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|finfo
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|result
init|=
name|finfo
operator|.
name|getUDFClass
argument_list|()
decl_stmt|;
return|return
name|result
return|;
block|}
specifier|static
name|Map
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Integer
argument_list|>
name|numericTypes
decl_stmt|;
static|static
block|{
name|numericTypes
operator|=
operator|new
name|HashMap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|numericTypes
operator|.
name|put
argument_list|(
name|Byte
operator|.
name|class
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|numericTypes
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|class
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|numericTypes
operator|.
name|put
argument_list|(
name|Long
operator|.
name|class
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|numericTypes
operator|.
name|put
argument_list|(
name|Float
operator|.
name|class
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|numericTypes
operator|.
name|put
argument_list|(
name|Double
operator|.
name|class
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|numericTypes
operator|.
name|put
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|6
argument_list|)
expr_stmt|;
block|}
comment|/**    * Find a common class that objects of both Class a and Class b can convert to.    * @return null if no common class could be found.    */
specifier|public
specifier|static
name|Class
argument_list|<
name|?
argument_list|>
name|getCommonClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|a
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|b
parameter_list|)
block|{
comment|// Equal
if|if
condition|(
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
condition|)
return|return
name|a
return|;
comment|// Java class inheritance hierarchy
if|if
condition|(
name|a
operator|.
name|isAssignableFrom
argument_list|(
name|b
argument_list|)
condition|)
return|return
name|a
return|;
if|if
condition|(
name|b
operator|.
name|isAssignableFrom
argument_list|(
name|a
argument_list|)
condition|)
return|return
name|b
return|;
comment|// Prefer String to Number conversion before implicit conversions
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|a
argument_list|)
operator|&&
name|b
operator|.
name|equals
argument_list|(
name|String
operator|.
name|class
argument_list|)
condition|)
return|return
name|Double
operator|.
name|class
return|;
if|if
condition|(
name|Number
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|b
argument_list|)
operator|&&
name|a
operator|.
name|equals
argument_list|(
name|String
operator|.
name|class
argument_list|)
condition|)
return|return
name|Double
operator|.
name|class
return|;
comment|// implicit conversions
if|if
condition|(
name|FunctionRegistry
operator|.
name|implicitConvertable
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
condition|)
return|return
name|b
return|;
if|if
condition|(
name|FunctionRegistry
operator|.
name|implicitConvertable
argument_list|(
name|b
argument_list|,
name|a
argument_list|)
condition|)
return|return
name|a
return|;
return|return
literal|null
return|;
block|}
comment|/** Returns whether it is possible to implicitly convert an object of Class from to Class to.    */
specifier|public
specifier|static
name|boolean
name|implicitConvertable
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|from
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|to
parameter_list|)
block|{
assert|assert
operator|(
operator|!
name|from
operator|.
name|equals
argument_list|(
name|to
argument_list|)
operator|)
assert|;
comment|// Allow implicit String to Double conversion
if|if
condition|(
name|from
operator|.
name|equals
argument_list|(
name|String
operator|.
name|class
argument_list|)
operator|&&
name|to
operator|.
name|equals
argument_list|(
name|Double
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|from
operator|.
name|equals
argument_list|(
name|String
operator|.
name|class
argument_list|)
operator|&&
name|to
operator|.
name|equals
argument_list|(
name|java
operator|.
name|sql
operator|.
name|Date
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|from
operator|.
name|equals
argument_list|(
name|java
operator|.
name|sql
operator|.
name|Date
operator|.
name|class
argument_list|)
operator|&&
name|to
operator|.
name|equals
argument_list|(
name|String
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Allow implicit conversion from Byte -> Integer -> Long -> Float -> Double -> String
name|Integer
name|f
init|=
name|numericTypes
operator|.
name|get
argument_list|(
name|from
argument_list|)
decl_stmt|;
name|Integer
name|t
init|=
name|numericTypes
operator|.
name|get
argument_list|(
name|to
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|==
literal|null
operator|||
name|t
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|f
operator|.
name|intValue
argument_list|()
operator|>
name|t
operator|.
name|intValue
argument_list|()
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
comment|/**    * Get the UDF method for the name and argumentClasses.     * @param name the name of the UDF    * @param argumentClasses     * @param exact  if true, we don't allow implicit type conversions.     * @return    */
specifier|public
specifier|static
name|Method
name|getUDFMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|exact
parameter_list|,
name|List
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|argumentClasses
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|udf
init|=
name|getUDFClass
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|udf
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|getMethodInternal
argument_list|(
name|udf
argument_list|,
literal|"evaluate"
argument_list|,
name|exact
argument_list|,
name|argumentClasses
argument_list|)
return|;
block|}
comment|/**    * This method is shared between UDFRegistry and UDAFRegistry.    * methodName will be "evaluate" for UDFRegistry, and "aggregate" for UDAFRegistry.     */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Method
name|getMethodInternal
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|udfClass
parameter_list|,
name|String
name|methodName
parameter_list|,
name|boolean
name|exact
parameter_list|,
name|List
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|argumentClasses
parameter_list|)
block|{
name|int
name|leastImplicitConversions
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
name|Method
name|udfMethod
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Method
name|m
range|:
name|Arrays
operator|.
name|asList
argument_list|(
name|udfClass
operator|.
name|getMethods
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|methodName
argument_list|)
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|argumentTypeInfos
init|=
name|m
operator|.
name|getParameterTypes
argument_list|()
decl_stmt|;
name|boolean
name|match
init|=
operator|(
name|argumentTypeInfos
operator|.
name|length
operator|==
name|argumentClasses
operator|.
name|size
argument_list|()
operator|)
decl_stmt|;
name|int
name|implicitConversions
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
name|argumentClasses
operator|.
name|size
argument_list|()
operator|&&
name|match
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|argumentClasses
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
name|Void
operator|.
name|class
condition|)
continue|continue;
name|Class
argument_list|<
name|?
argument_list|>
name|accepted
init|=
name|ObjectInspectorUtils
operator|.
name|generalizePrimitive
argument_list|(
name|argumentTypeInfos
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|accepted
operator|.
name|isAssignableFrom
argument_list|(
name|argumentClasses
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
comment|// do nothing if match
block|}
elseif|else
if|if
condition|(
operator|!
name|exact
operator|&&
name|implicitConvertable
argument_list|(
name|argumentClasses
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|accepted
argument_list|)
condition|)
block|{
name|implicitConversions
operator|++
expr_stmt|;
block|}
else|else
block|{
name|match
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|match
condition|)
block|{
comment|// Always choose the function with least implicit conversions.
if|if
condition|(
name|implicitConversions
operator|<
name|leastImplicitConversions
condition|)
block|{
name|udfMethod
operator|=
name|m
expr_stmt|;
name|leastImplicitConversions
operator|=
name|implicitConversions
expr_stmt|;
comment|// Found an exact match
if|if
condition|(
name|leastImplicitConversions
operator|==
literal|0
condition|)
break|break;
block|}
elseif|else
if|if
condition|(
name|implicitConversions
operator|==
name|leastImplicitConversions
condition|)
block|{
comment|// Ambiguous call: two methods with the same number of implicit conversions
name|udfMethod
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
comment|// do nothing if implicitConversions> leastImplicitConversions
block|}
block|}
block|}
block|}
return|return
name|udfMethod
return|;
block|}
specifier|public
specifier|static
name|Method
name|getUDFMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|exact
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|argumentClasses
parameter_list|)
block|{
return|return
name|getUDFMethod
argument_list|(
name|name
argument_list|,
name|exact
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|argumentClasses
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|registerUDAF
parameter_list|(
name|String
name|functionName
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
name|UDAFClass
parameter_list|)
block|{
if|if
condition|(
name|UDAF
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|UDAFClass
argument_list|)
condition|)
block|{
name|mFunctions
operator|.
name|put
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
operator|new
name|FunctionInfo
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
literal|null
argument_list|,
name|UDAFClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Registering UDAF Class "
operator|+
name|UDAFClass
operator|+
literal|" which does not extends "
operator|+
name|UDAF
operator|.
name|class
argument_list|)
throw|;
block|}
name|mFunctions
operator|.
name|put
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
operator|new
name|FunctionInfo
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|,
literal|null
argument_list|,
name|UDAFClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
name|getUDAF
parameter_list|(
name|String
name|functionName
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Looking up UDAF: "
operator|+
name|functionName
argument_list|)
expr_stmt|;
name|FunctionInfo
name|finfo
init|=
name|mFunctions
operator|.
name|get
argument_list|(
name|functionName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|finfo
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
name|result
init|=
name|finfo
operator|.
name|getUDAFClass
argument_list|()
decl_stmt|;
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|Method
name|getUDAFMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|argumentClasses
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
name|udaf
init|=
name|getUDAF
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|udaf
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|FunctionRegistry
operator|.
name|getMethodInternal
argument_list|(
name|udaf
argument_list|,
literal|"aggregate"
argument_list|,
literal|false
argument_list|,
name|argumentClasses
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Method
name|getUDAFMethod
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|argumentClasses
parameter_list|)
block|{
return|return
name|getUDAFMethod
argument_list|(
name|name
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|argumentClasses
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit


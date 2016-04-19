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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TStatusCode
import|;
end_import

begin_comment
comment|/**  * HiveSQLException.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveSQLException
extends|extends
name|SQLException
block|{
comment|/**    *    */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|6095254671958748094L
decl_stmt|;
comment|/**    *    */
specifier|public
name|HiveSQLException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * @param reason    */
specifier|public
name|HiveSQLException
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|super
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param cause    */
specifier|public
name|HiveSQLException
parameter_list|(
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reason    * @param sqlState    */
specifier|public
name|HiveSQLException
parameter_list|(
name|String
name|reason
parameter_list|,
name|String
name|sqlState
parameter_list|)
block|{
name|super
argument_list|(
name|reason
argument_list|,
name|sqlState
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reason    * @param cause    */
specifier|public
name|HiveSQLException
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|reason
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reason    * @param sqlState    * @param vendorCode    */
specifier|public
name|HiveSQLException
parameter_list|(
name|String
name|reason
parameter_list|,
name|String
name|sqlState
parameter_list|,
name|int
name|vendorCode
parameter_list|)
block|{
name|super
argument_list|(
name|reason
argument_list|,
name|sqlState
argument_list|,
name|vendorCode
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reason    * @param sqlState    * @param cause    */
specifier|public
name|HiveSQLException
parameter_list|(
name|String
name|reason
parameter_list|,
name|String
name|sqlState
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|reason
argument_list|,
name|sqlState
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param reason    * @param sqlState    * @param vendorCode    * @param cause    */
specifier|public
name|HiveSQLException
parameter_list|(
name|String
name|reason
parameter_list|,
name|String
name|sqlState
parameter_list|,
name|int
name|vendorCode
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|reason
argument_list|,
name|sqlState
argument_list|,
name|vendorCode
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveSQLException
parameter_list|(
name|TStatus
name|status
parameter_list|)
block|{
comment|// TODO: set correct vendorCode field
name|super
argument_list|(
name|status
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|status
operator|.
name|getSqlState
argument_list|()
argument_list|,
name|status
operator|.
name|getErrorCode
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|status
operator|.
name|getInfoMessages
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|initCause
argument_list|(
name|toCause
argument_list|(
name|status
operator|.
name|getInfoMessages
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Converts current object to a {@link TStatus} object    * @return	a {@link TStatus} object    */
specifier|public
name|TStatus
name|toTStatus
parameter_list|()
block|{
comment|// TODO: convert sqlState, etc.
name|TStatus
name|tStatus
init|=
operator|new
name|TStatus
argument_list|(
name|TStatusCode
operator|.
name|ERROR_STATUS
argument_list|)
decl_stmt|;
name|tStatus
operator|.
name|setSqlState
argument_list|(
name|getSQLState
argument_list|()
argument_list|)
expr_stmt|;
name|tStatus
operator|.
name|setErrorCode
argument_list|(
name|getErrorCode
argument_list|()
argument_list|)
expr_stmt|;
name|tStatus
operator|.
name|setErrorMessage
argument_list|(
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|tStatus
operator|.
name|setInfoMessages
argument_list|(
name|toString
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|tStatus
return|;
block|}
comment|/**    * Converts the specified {@link Exception} object into a {@link TStatus} object    * @param e	a {@link Exception} object    * @return	a {@link TStatus} object    */
specifier|public
specifier|static
name|TStatus
name|toTStatus
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|HiveSQLException
condition|)
block|{
return|return
operator|(
operator|(
name|HiveSQLException
operator|)
name|e
operator|)
operator|.
name|toTStatus
argument_list|()
return|;
block|}
name|TStatus
name|tStatus
init|=
operator|new
name|TStatus
argument_list|(
name|TStatusCode
operator|.
name|ERROR_STATUS
argument_list|)
decl_stmt|;
name|tStatus
operator|.
name|setErrorMessage
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|tStatus
operator|.
name|setInfoMessages
argument_list|(
name|toString
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|tStatus
return|;
block|}
comment|/**    * Converts a {@link Throwable} object into a flattened list of texts including its stack trace    * and the stack traces of the nested causes.    * @param ex  a {@link Throwable} object    * @return    a flattened list of texts including the {@link Throwable} object's stack trace    *            and the stack traces of the nested causes.    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|toString
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
return|return
name|toString
argument_list|(
name|ex
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|toString
parameter_list|(
name|Throwable
name|cause
parameter_list|,
name|StackTraceElement
index|[]
name|parent
parameter_list|)
block|{
name|StackTraceElement
index|[]
name|trace
init|=
name|cause
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
name|int
name|m
init|=
name|trace
operator|.
name|length
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
name|int
name|n
init|=
name|parent
operator|.
name|length
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|m
operator|>=
literal|0
operator|&&
name|n
operator|>=
literal|0
operator|&&
name|trace
index|[
name|m
index|]
operator|.
name|equals
argument_list|(
name|parent
index|[
name|n
index|]
argument_list|)
condition|)
block|{
name|m
operator|--
expr_stmt|;
name|n
operator|--
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|detail
init|=
name|enroll
argument_list|(
name|cause
argument_list|,
name|trace
argument_list|,
name|m
argument_list|)
decl_stmt|;
name|cause
operator|=
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
if|if
condition|(
name|cause
operator|!=
literal|null
condition|)
block|{
name|detail
operator|.
name|addAll
argument_list|(
name|toString
argument_list|(
name|cause
argument_list|,
name|trace
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|detail
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|enroll
parameter_list|(
name|Throwable
name|ex
parameter_list|,
name|StackTraceElement
index|[]
name|trace
parameter_list|,
name|int
name|max
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|details
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|'*'
argument_list|)
operator|.
name|append
argument_list|(
name|ex
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|trace
operator|.
name|length
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
operator|.
name|append
argument_list|(
name|max
argument_list|)
expr_stmt|;
name|details
operator|.
name|add
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|max
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|trace
index|[
name|i
index|]
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|trace
index|[
name|i
index|]
operator|.
name|getMethodName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|String
name|fileName
init|=
name|trace
index|[
name|i
index|]
operator|.
name|getFileName
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|fileName
operator|==
literal|null
condition|?
literal|""
else|:
name|fileName
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|trace
index|[
name|i
index|]
operator|.
name|getLineNumber
argument_list|()
argument_list|)
expr_stmt|;
name|details
operator|.
name|add
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|details
return|;
block|}
comment|/**    * Converts a flattened list of texts including the stack trace and the stack    * traces of the nested causes into a {@link Throwable} object.    * @param details a flattened list of texts including the stack trace and the stack    *                traces of the nested causes    * @return        a {@link Throwable} object    */
specifier|public
specifier|static
name|Throwable
name|toCause
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|details
parameter_list|)
block|{
return|return
name|toStackTrace
argument_list|(
name|details
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Throwable
name|toStackTrace
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|details
parameter_list|,
name|StackTraceElement
index|[]
name|parent
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|String
name|detail
init|=
name|details
operator|.
name|get
argument_list|(
name|index
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|detail
operator|.
name|startsWith
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
comment|// should not be happened. ignore remaining
block|}
name|int
name|i1
init|=
name|detail
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|int
name|i3
init|=
name|detail
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|int
name|i2
init|=
name|detail
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|i3
argument_list|)
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|String
name|exceptionClass
init|=
name|detail
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|i1
argument_list|)
decl_stmt|;
name|String
name|exceptionMessage
init|=
name|detail
operator|.
name|substring
argument_list|(
name|i1
operator|+
literal|1
argument_list|,
name|i2
argument_list|)
decl_stmt|;
name|Throwable
name|ex
init|=
name|newInstance
argument_list|(
name|exceptionClass
argument_list|,
name|exceptionMessage
argument_list|)
decl_stmt|;
name|int
name|length
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|detail
operator|.
name|substring
argument_list|(
name|i2
operator|+
literal|1
argument_list|,
name|i3
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|unique
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|detail
operator|.
name|substring
argument_list|(
name|i3
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|StackTraceElement
index|[]
name|trace
init|=
operator|new
name|StackTraceElement
index|[
name|length
index|]
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<=
name|unique
condition|;
name|i
operator|++
control|)
block|{
name|detail
operator|=
name|details
operator|.
name|get
argument_list|(
name|index
operator|++
argument_list|)
expr_stmt|;
name|int
name|j1
init|=
name|detail
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|int
name|j3
init|=
name|detail
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|int
name|j2
init|=
name|detail
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|j3
argument_list|)
operator|.
name|lastIndexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|String
name|className
init|=
name|detail
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|j1
argument_list|)
decl_stmt|;
name|String
name|methodName
init|=
name|detail
operator|.
name|substring
argument_list|(
name|j1
operator|+
literal|1
argument_list|,
name|j2
argument_list|)
decl_stmt|;
name|String
name|fileName
init|=
name|detail
operator|.
name|substring
argument_list|(
name|j2
operator|+
literal|1
argument_list|,
name|j3
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fileName
operator|=
literal|null
expr_stmt|;
block|}
name|int
name|lineNumber
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|detail
operator|.
name|substring
argument_list|(
name|j3
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|trace
index|[
name|i
index|]
operator|=
operator|new
name|StackTraceElement
argument_list|(
name|className
argument_list|,
name|methodName
argument_list|,
name|fileName
argument_list|,
name|lineNumber
argument_list|)
expr_stmt|;
block|}
name|int
name|common
init|=
name|trace
operator|.
name|length
operator|-
name|i
decl_stmt|;
if|if
condition|(
name|common
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|parent
argument_list|,
name|parent
operator|.
name|length
operator|-
name|common
argument_list|,
name|trace
argument_list|,
name|trace
operator|.
name|length
operator|-
name|common
argument_list|,
name|common
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|details
operator|.
name|size
argument_list|()
operator|>
name|index
condition|)
block|{
name|ex
operator|.
name|initCause
argument_list|(
name|toStackTrace
argument_list|(
name|details
argument_list|,
name|trace
argument_list|,
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ex
operator|.
name|setStackTrace
argument_list|(
name|trace
argument_list|)
expr_stmt|;
return|return
name|ex
return|;
block|}
specifier|private
specifier|static
name|Throwable
name|newInstance
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|message
parameter_list|)
block|{
try|try
block|{
return|return
operator|(
name|Throwable
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|message
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|RuntimeException
argument_list|(
name|className
operator|+
literal|":"
operator|+
name|message
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit


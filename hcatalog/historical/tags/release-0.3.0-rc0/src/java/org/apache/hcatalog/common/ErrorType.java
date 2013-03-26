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
name|hcatalog
operator|.
name|common
package|;
end_package

begin_comment
comment|/**  * Enum type representing the various errors throws by HCat.  */
end_comment

begin_enum
specifier|public
enum|enum
name|ErrorType
block|{
comment|/* HCat Input Format related errors 1000 - 1999 */
name|ERROR_DB_INIT
argument_list|(
literal|1000
argument_list|,
literal|"Error initializing database session"
argument_list|)
block|,
name|ERROR_EXCEED_MAXPART
argument_list|(
literal|1001
argument_list|,
literal|"Query result exceeded maximum number of partitions allowed"
argument_list|)
block|,
comment|/* HCat Output Format related errors 2000 - 2999 */
name|ERROR_INVALID_TABLE
argument_list|(
literal|2000
argument_list|,
literal|"Table specified does not exist"
argument_list|)
block|,
name|ERROR_SET_OUTPUT
argument_list|(
literal|2001
argument_list|,
literal|"Error setting output information"
argument_list|)
block|,
name|ERROR_DUPLICATE_PARTITION
argument_list|(
literal|2002
argument_list|,
literal|"Partition already present with given partition key values"
argument_list|)
block|,
name|ERROR_NON_EMPTY_TABLE
argument_list|(
literal|2003
argument_list|,
literal|"Non-partitioned table already contains data"
argument_list|)
block|,
name|ERROR_NOT_INITIALIZED
argument_list|(
literal|2004
argument_list|,
literal|"HCatOutputFormat not initialized, setOutput has to be called"
argument_list|)
block|,
name|ERROR_INIT_STORAGE_DRIVER
argument_list|(
literal|2005
argument_list|,
literal|"Error initializing output storage driver instance"
argument_list|)
block|,
name|ERROR_PUBLISHING_PARTITION
argument_list|(
literal|2006
argument_list|,
literal|"Error adding partition to metastore"
argument_list|)
block|,
name|ERROR_SCHEMA_COLUMN_MISMATCH
argument_list|(
literal|2007
argument_list|,
literal|"Invalid column position in partition schema"
argument_list|)
block|,
name|ERROR_SCHEMA_PARTITION_KEY
argument_list|(
literal|2008
argument_list|,
literal|"Partition key cannot be present in the partition data"
argument_list|)
block|,
name|ERROR_SCHEMA_TYPE_MISMATCH
argument_list|(
literal|2009
argument_list|,
literal|"Invalid column type in partition schema"
argument_list|)
block|,
name|ERROR_INVALID_PARTITION_VALUES
argument_list|(
literal|2010
argument_list|,
literal|"Invalid partition values specified"
argument_list|)
block|,
name|ERROR_MISSING_PARTITION_KEY
argument_list|(
literal|2011
argument_list|,
literal|"Partition key value not provided for publish"
argument_list|)
block|,
name|ERROR_MOVE_FAILED
argument_list|(
literal|2012
argument_list|,
literal|"Moving of data failed during commit"
argument_list|)
block|,
name|ERROR_TOO_MANY_DYNAMIC_PTNS
argument_list|(
literal|2013
argument_list|,
literal|"Attempt to create too many dynamic partitions"
argument_list|)
block|,
name|ERROR_INIT_LOADER
argument_list|(
literal|2014
argument_list|,
literal|"Error initializing Pig loader"
argument_list|)
block|,
name|ERROR_INIT_STORER
argument_list|(
literal|2015
argument_list|,
literal|"Error initializing Pig storer"
argument_list|)
block|,
comment|/* Authorization Errors 3000 - 3999 */
name|ERROR_ACCESS_CONTROL
argument_list|(
literal|3000
argument_list|,
literal|"Permission denied"
argument_list|)
block|,
comment|/* Miscellaneous errors, range 9000 - 9998 */
name|ERROR_UNIMPLEMENTED
argument_list|(
literal|9000
argument_list|,
literal|"Functionality currently unimplemented"
argument_list|)
block|,
name|ERROR_INTERNAL_EXCEPTION
argument_list|(
literal|9001
argument_list|,
literal|"Exception occurred while processing HCat request"
argument_list|)
block|;
comment|/** The error code. */
specifier|private
name|int
name|errorCode
decl_stmt|;
comment|/** The error message. */
specifier|private
name|String
name|errorMessage
decl_stmt|;
comment|/** Should the causal exception message be appended to the error message, yes by default*/
specifier|private
name|boolean
name|appendCauseMessage
init|=
literal|true
decl_stmt|;
comment|/** Is this a retriable error, no by default. */
specifier|private
name|boolean
name|isRetriable
init|=
literal|false
decl_stmt|;
comment|/**      * Instantiates a new error type.      * @param errorCode the error code      * @param errorMessage the error message      */
specifier|private
name|ErrorType
parameter_list|(
name|int
name|errorCode
parameter_list|,
name|String
name|errorMessage
parameter_list|)
block|{
name|this
operator|.
name|errorCode
operator|=
name|errorCode
expr_stmt|;
name|this
operator|.
name|errorMessage
operator|=
name|errorMessage
expr_stmt|;
block|}
comment|/**      * Instantiates a new error type.      * @param errorCode the error code      * @param errorMessage the error message      * @param appendCauseMessage should causal exception message be appended to error message      */
specifier|private
name|ErrorType
parameter_list|(
name|int
name|errorCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|boolean
name|appendCauseMessage
parameter_list|)
block|{
name|this
operator|.
name|errorCode
operator|=
name|errorCode
expr_stmt|;
name|this
operator|.
name|errorMessage
operator|=
name|errorMessage
expr_stmt|;
name|this
operator|.
name|appendCauseMessage
operator|=
name|appendCauseMessage
expr_stmt|;
block|}
comment|/**      * Instantiates a new error type.      * @param errorCode the error code      * @param errorMessage the error message      * @param appendCauseMessage should causal exception message be appended to error message      * @param isRetriable is this a retriable error      */
specifier|private
name|ErrorType
parameter_list|(
name|int
name|errorCode
parameter_list|,
name|String
name|errorMessage
parameter_list|,
name|boolean
name|appendCauseMessage
parameter_list|,
name|boolean
name|isRetriable
parameter_list|)
block|{
name|this
operator|.
name|errorCode
operator|=
name|errorCode
expr_stmt|;
name|this
operator|.
name|errorMessage
operator|=
name|errorMessage
expr_stmt|;
name|this
operator|.
name|appendCauseMessage
operator|=
name|appendCauseMessage
expr_stmt|;
name|this
operator|.
name|isRetriable
operator|=
name|isRetriable
expr_stmt|;
block|}
comment|/**      * Gets the error code.      * @return the error code      */
specifier|public
name|int
name|getErrorCode
parameter_list|()
block|{
return|return
name|errorCode
return|;
block|}
comment|/**      * Gets the error message.      * @return the error message      */
specifier|public
name|String
name|getErrorMessage
parameter_list|()
block|{
return|return
name|errorMessage
return|;
block|}
comment|/**      * Checks if this is a retriable error.      * @return true, if is a retriable error, false otherwise      */
specifier|public
name|boolean
name|isRetriable
parameter_list|()
block|{
return|return
name|isRetriable
return|;
block|}
comment|/**      * Whether the cause of the exception should be added to the error message.      * @return true, if the cause should be added to the message, false otherwise      */
specifier|public
name|boolean
name|appendCauseMessage
parameter_list|()
block|{
return|return
name|appendCauseMessage
return|;
block|}
block|}
end_enum

end_unit


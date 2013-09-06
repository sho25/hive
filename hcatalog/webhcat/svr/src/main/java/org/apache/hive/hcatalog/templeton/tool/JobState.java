begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
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

begin_comment
comment|/**  * The persistent state of a job.  The state is stored in one of the  * supported storage systems.  */
end_comment

begin_class
specifier|public
class|class
name|JobState
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|JobState
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|String
name|id
decl_stmt|;
comment|// Storage is instantiated in the constructor
specifier|private
name|TempletonStorage
name|storage
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|TempletonStorage
operator|.
name|Type
name|type
init|=
name|TempletonStorage
operator|.
name|Type
operator|.
name|JOB
decl_stmt|;
specifier|private
name|Configuration
name|config
init|=
literal|null
decl_stmt|;
specifier|public
name|JobState
parameter_list|(
name|String
name|id
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|config
operator|=
name|conf
expr_stmt|;
name|storage
operator|=
name|getStorage
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|delete
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|storage
operator|.
name|delete
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Error getting children of node -- probably node has been deleted
name|LOG
operator|.
name|info
argument_list|(
literal|"Couldn't delete "
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get an instance of the selected storage class.  Defaults to    * HDFS storage if none is specified.    */
specifier|public
specifier|static
name|TempletonStorage
name|getStorageInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|TempletonStorage
name|storage
init|=
literal|null
decl_stmt|;
try|try
block|{
name|storage
operator|=
operator|(
name|TempletonStorage
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TempletonStorage
operator|.
name|STORAGE_CLASS
argument_list|)
argument_list|)
operator|.
name|newInstance
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No storage method found: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|storage
operator|=
operator|new
name|HDFSStorage
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Couldn't create storage."
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|storage
return|;
block|}
comment|/**    * Get an open instance of the selected storage class.  Defaults    * to HDFS storage if none is specified.    */
specifier|public
specifier|static
name|TempletonStorage
name|getStorage
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|TempletonStorage
name|storage
init|=
name|getStorageInstance
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|storage
operator|.
name|openStorage
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|storage
return|;
block|}
comment|/**    * For storage methods that require a connection, this is a hint    * that it's time to close the connection.    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|storage
operator|.
name|closeStorage
argument_list|()
expr_stmt|;
block|}
comment|//
comment|// Properties
comment|//
comment|/**    * This job id.    */
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**    * The percent complete of a job    */
specifier|public
name|String
name|getPercentComplete
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getField
argument_list|(
literal|"percentComplete"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setPercentComplete
parameter_list|(
name|String
name|percent
parameter_list|)
throws|throws
name|IOException
block|{
name|setField
argument_list|(
literal|"percentComplete"
argument_list|,
name|percent
argument_list|)
expr_stmt|;
block|}
comment|/**    * The child id of TempletonControllerJob    */
specifier|public
name|String
name|getChildId
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getField
argument_list|(
literal|"childid"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setChildId
parameter_list|(
name|String
name|childid
parameter_list|)
throws|throws
name|IOException
block|{
name|setField
argument_list|(
literal|"childid"
argument_list|,
name|childid
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a jobid to the list of children of this job.    *    * @param jobid    * @throws IOException    */
specifier|public
name|void
name|addChild
parameter_list|(
name|String
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|jobids
init|=
literal|""
decl_stmt|;
try|try
block|{
name|jobids
operator|=
name|getField
argument_list|(
literal|"children"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// There are none or they're not readable.
block|}
if|if
condition|(
operator|!
name|jobids
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|jobids
operator|+=
literal|","
expr_stmt|;
block|}
name|jobids
operator|+=
name|jobid
expr_stmt|;
name|setField
argument_list|(
literal|"children"
argument_list|,
name|jobids
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a list of jobstates for jobs that are children of this job.    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|JobState
argument_list|>
name|getChildren
parameter_list|()
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|JobState
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|JobState
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|jobid
range|:
name|getField
argument_list|(
literal|"children"
argument_list|)
operator|.
name|split
argument_list|(
literal|","
argument_list|)
control|)
block|{
name|children
operator|.
name|add
argument_list|(
operator|new
name|JobState
argument_list|(
name|jobid
argument_list|,
name|config
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|children
return|;
block|}
comment|/**    * Save a comma-separated list of jobids that are children    * of this job.    * @param jobids    * @throws IOException    */
specifier|public
name|void
name|setChildren
parameter_list|(
name|String
name|jobids
parameter_list|)
throws|throws
name|IOException
block|{
name|setField
argument_list|(
literal|"children"
argument_list|,
name|jobids
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the list of child jobs of this job    * @param children    */
specifier|public
name|void
name|setChildren
parameter_list|(
name|List
argument_list|<
name|JobState
argument_list|>
name|children
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|val
init|=
literal|""
decl_stmt|;
for|for
control|(
name|JobState
name|jobstate
range|:
name|children
control|)
block|{
if|if
condition|(
operator|!
name|val
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|val
operator|+=
literal|","
expr_stmt|;
block|}
name|val
operator|+=
name|jobstate
operator|.
name|getId
argument_list|()
expr_stmt|;
block|}
name|setField
argument_list|(
literal|"children"
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
comment|/**    * The system exit value of the job.    */
specifier|public
name|Long
name|getExitValue
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getLongField
argument_list|(
literal|"exitValue"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setExitValue
parameter_list|(
name|long
name|exitValue
parameter_list|)
throws|throws
name|IOException
block|{
name|setLongField
argument_list|(
literal|"exitValue"
argument_list|,
name|exitValue
argument_list|)
expr_stmt|;
block|}
comment|/**    * When this job was created.    */
specifier|public
name|Long
name|getCreated
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getLongField
argument_list|(
literal|"created"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setCreated
parameter_list|(
name|long
name|created
parameter_list|)
throws|throws
name|IOException
block|{
name|setLongField
argument_list|(
literal|"created"
argument_list|,
name|created
argument_list|)
expr_stmt|;
block|}
comment|/**    * The user who started this job.    */
specifier|public
name|String
name|getUser
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getField
argument_list|(
literal|"user"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setUser
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|setField
argument_list|(
literal|"user"
argument_list|,
name|user
argument_list|)
expr_stmt|;
block|}
comment|/**    * The url callback    */
specifier|public
name|String
name|getCallback
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getField
argument_list|(
literal|"callback"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setCallback
parameter_list|(
name|String
name|callback
parameter_list|)
throws|throws
name|IOException
block|{
name|setField
argument_list|(
literal|"callback"
argument_list|,
name|callback
argument_list|)
expr_stmt|;
block|}
comment|/**    * The status of a job once it is completed.    */
specifier|public
name|String
name|getCompleteStatus
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getField
argument_list|(
literal|"completed"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setCompleteStatus
parameter_list|(
name|String
name|complete
parameter_list|)
throws|throws
name|IOException
block|{
name|setField
argument_list|(
literal|"completed"
argument_list|,
name|complete
argument_list|)
expr_stmt|;
block|}
comment|/**    * The time when the callback was sent.    */
specifier|public
name|Long
name|getNotifiedTime
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getLongField
argument_list|(
literal|"notified"
argument_list|)
return|;
block|}
specifier|public
name|void
name|setNotifiedTime
parameter_list|(
name|long
name|notified
parameter_list|)
throws|throws
name|IOException
block|{
name|setLongField
argument_list|(
literal|"notified"
argument_list|,
name|notified
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// Helpers
comment|//
comment|/**    * Fetch an integer field from the store.    */
specifier|public
name|Long
name|getLongField
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|s
init|=
name|storage
operator|.
name|getField
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|s
operator|==
literal|null
condition|)
return|return
literal|null
return|;
else|else
block|{
try|try
block|{
return|return
operator|new
name|Long
argument_list|(
name|s
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"templeton: bug "
operator|+
name|name
operator|+
literal|" "
operator|+
name|s
operator|+
literal|" : "
operator|+
name|e
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
comment|/**    * Store a String field from the store.    */
specifier|public
name|void
name|setField
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|val
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|storage
operator|.
name|saveField
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|name
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotFoundException
name|ne
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ne
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|getField
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|storage
operator|.
name|getField
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|name
argument_list|)
return|;
block|}
comment|/**    * Store a long field.    *    * @param name    * @param val    * @throws IOException    */
specifier|public
name|void
name|setLongField
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|val
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|storage
operator|.
name|saveField
argument_list|(
name|type
argument_list|,
name|id
argument_list|,
name|name
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotFoundException
name|ne
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Job "
operator|+
name|id
operator|+
literal|" was not found: "
operator|+
name|ne
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Get an id for each currently existing job, which can be used to create    * a JobState object.    *    * @param conf    * @throws IOException    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getJobs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|getStorage
argument_list|(
name|conf
argument_list|)
operator|.
name|getAllForType
argument_list|(
name|type
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't get jobs"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit


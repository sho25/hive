begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
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
name|util
operator|.
name|Properties
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
name|fs
operator|.
name|FileStatus
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
name|FileSystem
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
name|FsStatus
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
name|common
operator|.
name|FileUtils
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
name|Writable
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
name|WritableComparable
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|JobStatus
operator|.
name|State
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
name|mapreduce
operator|.
name|OutputCommitter
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
name|mapreduce
operator|.
name|OutputFormat
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|FileOutputCommitter
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|FileOutputFormat
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
name|security
operator|.
name|AccessControlException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_comment
comment|/** The abstract class to be implemented by underlying storage drivers to enable data access from HCat through  *  HCatOutputFormat.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatOutputStorageDriver
block|{
comment|/**    * Initialize the storage driver with specified properties, default implementation does nothing.    * @param context the job context object    * @param hcatProperties the properties for the storage driver    * @throws IOException Signals that an I/O exception has occurred.    */
specifier|public
name|void
name|initialize
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|Properties
name|hcatProperties
parameter_list|)
throws|throws
name|IOException
block|{     }
comment|/**      * Returns the OutputFormat to use with this Storage Driver.      * @return the OutputFormat instance      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
specifier|abstract
name|OutputFormat
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|getOutputFormat
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Set the data location for the output.      * @param jobContext the job context object      * @param location the data location      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
specifier|abstract
name|void
name|setOutputPath
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Set the schema for the data being written out.      * @param jobContext the job context object      * @param schema the data schema      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
specifier|abstract
name|void
name|setSchema
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|HCatSchema
name|schema
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Sets the partition key values for the partition being written.      * @param jobContext the job context object      * @param partitionValues the partition values      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
specifier|abstract
name|void
name|setPartitionValues
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Generate the key for the underlying outputformat. The value given to HCatOutputFormat is passed as the      * argument. The key given to HCatOutputFormat is ignored..      * @param value the value given to HCatOutputFormat      * @return a key instance      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
specifier|abstract
name|WritableComparable
argument_list|<
name|?
argument_list|>
name|generateKey
parameter_list|(
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Convert the given HCatRecord value to the actual value type.      * @param value the HCatRecord value to convert      * @return a value instance      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
specifier|abstract
name|Writable
name|convertValue
parameter_list|(
name|HCatRecord
name|value
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Gets the location to use for the specified partition values.      *  The storage driver can override as required.      * @param jobContext the job context object      * @param tableLocation the location of the table      * @param partitionValues the partition values      * @param dynHash A unique hash value that represents the dynamic partitioning job used      * @return the location String.      * @throws IOException Signals that an I/O exception has occurred.      */
specifier|public
name|String
name|getOutputLocation
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|String
name|tableLocation
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partitionCols
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionValues
parameter_list|,
name|String
name|dynHash
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|parentPath
init|=
name|tableLocation
decl_stmt|;
comment|// For dynamic partitioned writes without all keyvalues specified,
comment|// we create a temp dir for the associated write job
if|if
condition|(
name|dynHash
operator|!=
literal|null
condition|)
block|{
name|parentPath
operator|=
operator|new
name|Path
argument_list|(
name|tableLocation
argument_list|,
name|HCatOutputFormat
operator|.
name|DYNTEMP_DIR_NAME
operator|+
name|dynHash
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
comment|// For non-partitioned tables, we send them to the temp dir
if|if
condition|(
operator|(
name|dynHash
operator|==
literal|null
operator|)
operator|&&
operator|(
name|partitionValues
operator|==
literal|null
operator|||
name|partitionValues
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|tableLocation
argument_list|,
name|HCatOutputFormat
operator|.
name|TEMP_DIR_NAME
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|partitionCol
range|:
name|partitionCols
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|partitionValues
operator|.
name|get
argument_list|(
name|partitionCol
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|partitionLocation
init|=
name|FileUtils
operator|.
name|makePartName
argument_list|(
name|partitionCols
argument_list|,
name|values
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
name|partitionLocation
argument_list|)
decl_stmt|;
return|return
name|path
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/** Default implementation assumes FileOutputFormat. Storage drivers wrapping      * other OutputFormats should override this method.      */
specifier|public
name|Path
name|getWorkFilePath
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|,
name|String
name|outputLoc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Path
argument_list|(
operator|new
name|FileOutputCommitter
argument_list|(
operator|new
name|Path
argument_list|(
name|outputLoc
argument_list|)
argument_list|,
name|context
argument_list|)
operator|.
name|getWorkPath
argument_list|()
argument_list|,
name|FileOutputFormat
operator|.
name|getUniqueFile
argument_list|(
name|context
argument_list|,
literal|"part"
argument_list|,
literal|""
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Implementation that calls the underlying output committer's setupJob,       * used in lieu of underlying committer's setupJob when using dynamic partitioning      * The default implementation should be overriden by underlying implementations      * that do not use FileOutputCommitter.      * The reason this function exists is so as to allow a storage driver implementor to      * override underlying OutputCommitter's setupJob implementation to allow for      * being called multiple times in a job, to make it idempotent.      * This should be written in a manner that is callable multiple times       * from individual tasks without stepping on each others' toes      *       * @param context      * @throws InterruptedException       * @throws IOException       */
specifier|public
name|void
name|setupOutputCommitterJob
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|getOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
operator|.
name|setupJob
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**      * Implementation that calls the underlying output committer's cleanupJob,       * used in lieu of underlying committer's cleanupJob when using dynamic partitioning      * This should be written in a manner that is okay to call after having had      * multiple underlying outputcommitters write to task dirs inside it.      * While the base MR cleanupJob should have sufficed normally, this is provided      * in order to let people implementing setupOutputCommitterJob to cleanup properly      *       * @param context      * @throws IOException       */
specifier|public
name|void
name|cleanupOutputCommitterJob
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|getOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
operator|.
name|cleanupJob
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**      * Implementation that calls the underlying output committer's abortJob,       * used in lieu of underlying committer's abortJob when using dynamic partitioning      * This should be written in a manner that is okay to call after having had      * multiple underlying outputcommitters write to task dirs inside it.      * While the base MR cleanupJob should have sufficed normally, this is provided      * in order to let people implementing setupOutputCommitterJob to abort properly      *       * @param context      * @param state      * @throws IOException       */
specifier|public
name|void
name|abortOutputCommitterJob
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|,
name|State
name|state
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|getOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|context
argument_list|)
operator|.
name|abortJob
argument_list|(
name|context
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


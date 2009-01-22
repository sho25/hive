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
name|metadata
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
name|net
operator|.
name|URI
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
name|LinkedHashMap
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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|metastore
operator|.
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|StorageDescriptor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TMemoryBuffer
import|;
end_import

begin_comment
comment|/**  * A Hive Table Partition: is a fundamental storage unit within a Table  */
end_comment

begin_class
specifier|public
class|class
name|Partition
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.ql.metadata.Partition"
argument_list|)
decl_stmt|;
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Partition
name|tPartition
decl_stmt|;
comment|/**      * @return the tPartition      */
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Partition
name|getTPartition
parameter_list|()
block|{
return|return
name|tPartition
return|;
block|}
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
decl_stmt|;
comment|/**      * @return      * @see org.apache.hadoop.hive.metastore.api.Partition#getValues()      */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getValues
parameter_list|()
block|{
return|return
name|tPartition
operator|.
name|getValues
argument_list|()
return|;
block|}
specifier|private
name|Path
name|partPath
decl_stmt|;
specifier|private
name|URI
name|partURI
decl_stmt|;
specifier|public
name|Partition
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Partition
name|tp
parameter_list|)
throws|throws
name|HiveException
block|{
name|initialize
argument_list|(
name|tbl
argument_list|,
name|tp
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create partition object with the given info.      * @param tbl Table the partition will be in.      * @param partSpec Partition specifications.      * @param location Location of the partition, relative to the table.      * @throws HiveException Thrown if we could not create the partition.      */
specifier|public
name|Partition
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|Path
name|location
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|pvals
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
name|FieldSchema
name|field
range|:
name|tbl
operator|.
name|getPartCols
argument_list|()
control|)
block|{
name|pvals
operator|.
name|add
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Partition
name|tpart
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Partition
argument_list|()
decl_stmt|;
name|tpart
operator|.
name|setDbName
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|setTableName
argument_list|(
name|tbl
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|setValues
argument_list|(
name|pvals
argument_list|)
expr_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|()
decl_stmt|;
try|try
block|{
comment|//replace with THRIFT-138
name|TMemoryBuffer
name|buffer
init|=
operator|new
name|TMemoryBuffer
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|TBinaryProtocol
name|prot
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
operator|.
name|write
argument_list|(
name|prot
argument_list|)
expr_stmt|;
name|sd
operator|.
name|read
argument_list|(
name|prot
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not create a copy of StorageDescription"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Could not create a copy of StorageDescription"
argument_list|)
throw|;
block|}
name|tpart
operator|.
name|setSd
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|tpart
operator|.
name|getSd
argument_list|()
operator|.
name|setLocation
argument_list|(
name|location
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|tbl
argument_list|,
name|tpart
argument_list|)
expr_stmt|;
block|}
comment|/**      * Initializes this object with the given variables      * @param tbl Table the partition belongs to      * @param tp Thrift Partition object      * @throws HiveException Thrown if we cannot initialize the partition      */
specifier|private
name|void
name|initialize
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Partition
name|tp
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|table
operator|=
name|tbl
expr_stmt|;
name|this
operator|.
name|tPartition
operator|=
name|tp
expr_stmt|;
name|this
operator|.
name|partName
operator|=
literal|""
expr_stmt|;
if|if
condition|(
name|tbl
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
try|try
block|{
name|this
operator|.
name|partName
operator|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|tbl
operator|.
name|getPartCols
argument_list|()
argument_list|,
name|tp
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Invalid partition for table "
operator|+
name|tbl
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|partPath
operator|=
operator|new
name|Path
argument_list|(
name|tp
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We are in the HACK territory.
comment|// SemanticAnalyzer expects a single partition whose schema
comment|// is same as the table partition.
name|this
operator|.
name|partPath
operator|=
name|table
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|spec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|tbl
operator|.
name|createSpec
argument_list|(
name|tp
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|partURI
operator|=
name|partPath
operator|.
name|toUri
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|partName
return|;
block|}
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|table
operator|)
return|;
block|}
specifier|public
name|Path
index|[]
name|getPath
parameter_list|()
block|{
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
literal|1
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|this
operator|.
name|partPath
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
specifier|public
name|Path
name|getPartitionPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|partPath
return|;
block|}
specifier|final
specifier|public
name|URI
name|getDataLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|partURI
return|;
block|}
comment|/**      * The number of buckets is a property of the partition. However - internally we are just      * storing it as a property of the table as a short term measure.      */
specifier|public
name|int
name|getBucketCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
operator|.
name|getNumBuckets
argument_list|()
return|;
comment|/*       TODO: Keeping this code around for later use when we will support       sampling on tables which are not created with CLUSTERED INTO clause         // read from table meta data       int numBuckets = this.table.getNumBuckets();       if (numBuckets == -1) {         // table meta data does not have bucket information         // check if file system has multiple buckets(files) in this partition         String pathPattern = this.partPath.toString() + "/*";         try {           FileSystem fs = FileSystem.get(this.table.getDataLocation(), Hive.get().getConf());           FileStatus srcs[] = fs.globStatus(new Path(pathPattern));           numBuckets = srcs.length;         }         catch (Exception e) {           throw new RuntimeException("Cannot get bucket count for table " + this.table.getName(), e);         }       }       return numBuckets;       */
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getBucketCols
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
operator|.
name|getBucketCols
argument_list|()
return|;
block|}
comment|/**      * mapping from bucket number to bucket path      */
comment|//TODO: add test case and clean it up
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|Path
name|getBucketPath
parameter_list|(
name|int
name|bucketNum
parameter_list|)
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|this
operator|.
name|table
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|pathPattern
init|=
name|this
operator|.
name|partPath
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|getBucketCount
argument_list|()
operator|>
literal|0
condition|)
block|{
name|pathPattern
operator|=
name|pathPattern
operator|+
literal|"/*"
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Path pattern = "
operator|+
name|pathPattern
argument_list|)
expr_stmt|;
name|FileStatus
name|srcs
index|[]
init|=
name|fs
operator|.
name|globStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|pathPattern
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|src
range|:
name|srcs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got file: "
operator|+
name|src
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|srcs
index|[
name|bucketNum
index|]
operator|.
name|getPath
argument_list|()
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
name|RuntimeException
argument_list|(
literal|"Cannot get bucket path for bucket "
operator|+
name|bucketNum
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// return new Path(this.partPath, String.format("part-%1$05d", bucketNum));
block|}
comment|/**      * mapping from a Path to the bucket number if any      */
specifier|private
specifier|static
name|Pattern
name|bpattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"part-([0-9][0-9][0-9][0-9][0-9])"
argument_list|)
decl_stmt|;
specifier|private
name|String
name|partName
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
specifier|static
name|int
name|getBucketNum
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|bpattern
operator|.
name|matcher
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
name|String
name|bnum_str
init|=
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
return|return
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|bnum_str
argument_list|)
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected error parsing: "
operator|+
name|p
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|bnum_str
argument_list|)
throw|;
block|}
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
name|Path
index|[]
name|getPath
parameter_list|(
name|Sample
name|s
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
return|return
name|getPath
argument_list|()
return|;
block|}
else|else
block|{
name|int
name|bcount
init|=
name|this
operator|.
name|getBucketCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|bcount
operator|==
literal|0
condition|)
block|{
return|return
name|getPath
argument_list|()
return|;
block|}
name|Dimension
name|d
init|=
name|s
operator|.
name|getSampleDimension
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|d
operator|.
name|getDimensionId
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|table
operator|.
name|getBucketingDimensionId
argument_list|()
argument_list|)
condition|)
block|{
comment|// if the bucket dimension is not the same as the sampling dimension
comment|// we must scan all the data
return|return
name|getPath
argument_list|()
return|;
block|}
name|int
name|scount
init|=
name|s
operator|.
name|getSampleFraction
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Path
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|bcount
operator|==
name|scount
condition|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|getBucketPath
argument_list|(
name|s
operator|.
name|getSampleNum
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|bcount
operator|<
name|scount
condition|)
block|{
if|if
condition|(
operator|(
name|scount
operator|/
name|bcount
operator|)
operator|*
name|bcount
operator|!=
name|scount
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Sample Count"
operator|+
name|scount
operator|+
literal|" is not a multiple of bucket count "
operator|+
name|bcount
operator|+
literal|" for table "
operator|+
name|this
operator|.
name|table
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
comment|// undersampling a bucket
name|ret
operator|.
name|add
argument_list|(
name|getBucketPath
argument_list|(
operator|(
name|s
operator|.
name|getSampleNum
argument_list|()
operator|-
literal|1
operator|)
operator|%
name|bcount
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|bcount
operator|>
name|scount
condition|)
block|{
if|if
condition|(
operator|(
name|bcount
operator|/
name|scount
operator|)
operator|*
name|scount
operator|!=
name|bcount
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Sample Count"
operator|+
name|scount
operator|+
literal|" is not a divisor of bucket count "
operator|+
name|bcount
operator|+
literal|" for table "
operator|+
name|this
operator|.
name|table
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
comment|// sampling multiple buckets
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|bcount
operator|/
name|scount
condition|;
name|i
operator|++
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
name|getBucketPath
argument_list|(
name|i
operator|*
name|scount
operator|+
operator|(
name|s
operator|.
name|getSampleNum
argument_list|()
operator|-
literal|1
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|ret
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
name|ret
operator|.
name|size
argument_list|()
index|]
argument_list|)
operator|)
return|;
block|}
block|}
specifier|public
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSpec
parameter_list|()
block|{
return|return
name|this
operator|.
name|spec
return|;
block|}
comment|/**      * Replaces files in the partition with new data set specified by srcf. Works by moving files      *      * @param srcf Files to be moved. Leaf Directories or Globbed File Paths      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|protected
name|void
name|replaceFiles
parameter_list|(
name|Path
name|srcf
parameter_list|)
throws|throws
name|HiveException
block|{
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|table
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|replaceFiles
argument_list|(
name|srcf
argument_list|,
name|partPath
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"addFiles: filesystem error in check phase"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Inserts files specified into the partition. Works by moving files      *      * @param srcf Files to be moved. Leaf Directories or Globbed File Paths      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|protected
name|void
name|copyFiles
parameter_list|(
name|Path
name|srcf
parameter_list|)
throws|throws
name|HiveException
block|{
name|FileSystem
name|fs
decl_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|table
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|copyFiles
argument_list|(
name|srcf
argument_list|,
name|partPath
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"addFiles: filesystem error in check phase"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|pn
init|=
literal|"Invalid Partition"
decl_stmt|;
try|try
block|{
name|pn
operator|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|spec
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
comment|// ignore as we most probably in an exception path already otherwise this error wouldn't occur
block|}
return|return
name|table
operator|.
name|toString
argument_list|()
operator|+
literal|"("
operator|+
name|pn
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit


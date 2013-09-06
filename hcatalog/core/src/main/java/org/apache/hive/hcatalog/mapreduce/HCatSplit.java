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
name|mapreduce
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|WritableUtils
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
name|InputSplit
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
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
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/** The HCatSplit wrapper around the InputSplit returned by the underlying InputFormat */
end_comment

begin_class
specifier|public
class|class
name|HCatSplit
extends|extends
name|InputSplit
implements|implements
name|Writable
implements|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HCatSplit
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The partition info for the split. */
specifier|private
name|PartInfo
name|partitionInfo
decl_stmt|;
comment|/** The split returned by the underlying InputFormat split. */
specifier|private
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|baseMapRedSplit
decl_stmt|;
comment|/** The schema for the HCatTable */
specifier|private
name|HCatSchema
name|tableSchema
decl_stmt|;
specifier|private
name|HiveConf
name|hiveConf
decl_stmt|;
comment|/**    * Instantiates a new hcat split.    */
specifier|public
name|HCatSplit
parameter_list|()
block|{   }
comment|/**    * Instantiates a new hcat split.    *    * @param partitionInfo the partition info    * @param baseMapRedSplit the base mapred split    * @param tableSchema the table level schema    */
specifier|public
name|HCatSplit
parameter_list|(
name|PartInfo
name|partitionInfo
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|baseMapRedSplit
parameter_list|,
name|HCatSchema
name|tableSchema
parameter_list|)
block|{
name|this
operator|.
name|partitionInfo
operator|=
name|partitionInfo
expr_stmt|;
comment|// dataSchema can be obtained from partitionInfo.getPartitionSchema()
name|this
operator|.
name|baseMapRedSplit
operator|=
name|baseMapRedSplit
expr_stmt|;
name|this
operator|.
name|tableSchema
operator|=
name|tableSchema
expr_stmt|;
block|}
comment|/**    * Gets the partition info.    * @return the partitionInfo    */
specifier|public
name|PartInfo
name|getPartitionInfo
parameter_list|()
block|{
return|return
name|partitionInfo
return|;
block|}
comment|/**    * Gets the underlying InputSplit.    * @return the baseMapRedSplit    */
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|getBaseSplit
parameter_list|()
block|{
return|return
name|baseMapRedSplit
return|;
block|}
comment|/**    * Gets the data schema.    * @return the table schema    */
specifier|public
name|HCatSchema
name|getDataSchema
parameter_list|()
block|{
return|return
name|this
operator|.
name|partitionInfo
operator|.
name|getPartitionSchema
argument_list|()
return|;
block|}
comment|/**    * Gets the table schema.    * @return the table schema    */
specifier|public
name|HCatSchema
name|getTableSchema
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableSchema
return|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.mapreduce.InputSplit#getLength()    */
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
block|{
try|try
block|{
return|return
name|baseMapRedSplit
operator|.
name|getLength
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception in HCatSplit"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
comment|// we errored
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.mapreduce.InputSplit#getLocations()    */
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
block|{
try|try
block|{
return|return
name|baseMapRedSplit
operator|.
name|getLocations
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception in HCatSplit"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|String
index|[
literal|0
index|]
return|;
comment|// we errored
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.io.Writable#readFields(java.io.DataInput)    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|partitionInfoString
init|=
name|WritableUtils
operator|.
name|readString
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|partitionInfo
operator|=
operator|(
name|PartInfo
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|partitionInfoString
argument_list|)
expr_stmt|;
name|String
name|baseSplitClassName
init|=
name|WritableUtils
operator|.
name|readString
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|split
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
argument_list|>
name|splitClass
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|baseSplitClassName
argument_list|)
decl_stmt|;
comment|//Class.forName().newInstance() does not work if the underlying
comment|//InputSplit has package visibility
name|Constructor
argument_list|<
name|?
extends|extends
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
argument_list|>
name|constructor
init|=
name|splitClass
operator|.
name|getDeclaredConstructor
argument_list|(
operator|new
name|Class
index|[]
block|{}
argument_list|)
decl_stmt|;
name|constructor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|split
operator|=
name|constructor
operator|.
name|newInstance
argument_list|()
expr_stmt|;
comment|// read baseSplit from input
operator|(
operator|(
name|Writable
operator|)
name|split
operator|)
operator|.
name|readFields
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|this
operator|.
name|baseMapRedSplit
operator|=
name|split
expr_stmt|;
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
literal|"Exception from "
operator|+
name|baseSplitClassName
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|String
name|tableSchemaString
init|=
name|WritableUtils
operator|.
name|readString
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|tableSchema
operator|=
operator|(
name|HCatSchema
operator|)
name|HCatUtil
operator|.
name|deserialize
argument_list|(
name|tableSchemaString
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.io.Writable#write(java.io.DataOutput)    */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|output
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|partitionInfoString
init|=
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|partitionInfo
argument_list|)
decl_stmt|;
comment|// write partitionInfo into output
name|WritableUtils
operator|.
name|writeString
argument_list|(
name|output
argument_list|,
name|partitionInfoString
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeString
argument_list|(
name|output
argument_list|,
name|baseMapRedSplit
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Writable
name|baseSplitWritable
init|=
operator|(
name|Writable
operator|)
name|baseMapRedSplit
decl_stmt|;
comment|//write  baseSplit into output
name|baseSplitWritable
operator|.
name|write
argument_list|(
name|output
argument_list|)
expr_stmt|;
comment|//write the table schema into output
name|String
name|tableSchemaString
init|=
name|HCatUtil
operator|.
name|serialize
argument_list|(
name|tableSchema
argument_list|)
decl_stmt|;
name|WritableUtils
operator|.
name|writeString
argument_list|(
name|output
argument_list|,
name|tableSchemaString
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


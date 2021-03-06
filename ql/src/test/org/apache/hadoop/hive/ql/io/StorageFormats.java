begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|io
package|;
end_package

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
name|Collection
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
name|ServiceLoader
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
name|io
operator|.
name|RCFileInputFormat
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
name|io
operator|.
name|RCFileOutputFormat
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
name|io
operator|.
name|RCFileStorageFormatDescriptor
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
name|io
operator|.
name|StorageFormatDescriptor
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
name|io
operator|.
name|avro
operator|.
name|AvroContainerInputFormat
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
name|io
operator|.
name|avro
operator|.
name|AvroContainerOutputFormat
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
name|io
operator|.
name|orc
operator|.
name|OrcSerde
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
name|avro
operator|.
name|AvroSerDe
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
name|columnar
operator|.
name|ColumnarSerDe
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
name|lazy
operator|.
name|LazySimpleSerDe
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Utility class for enumerating Hive native storage formats for testing. Native Storage formats  * are registered via {@link org.apache.hadoop.hive.ql.io.StorageFormatDescriptor}.  */
end_comment

begin_class
specifier|public
class|class
name|StorageFormats
block|{
comment|/**    * Table of additional storage formats. These are SerDes or combinations of SerDe with    * InputFormat and OutputFormat that are not registered as a native Hive storage format.    *    * Each row in this table has the following fields:    *  - formatName - A string name for the storage format. This is used to give the table created    *    for the test a unique name.    *  - serdeClass - The name of the SerDe class used by the storage format.    *  - inputFormatClass - The name of the InputFormat class.    *  - outputFormatClass - The name of the OutputFormat class.    */
specifier|public
specifier|static
specifier|final
name|Object
index|[]
index|[]
name|ADDITIONAL_STORAGE_FORMATS
init|=
operator|new
name|Object
index|[]
index|[]
block|{
block|{
literal|"rcfile_columnar"
block|,
name|ColumnarSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,
name|RCFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,     }
block|}
decl_stmt|;
comment|/**    * Create an array of Objects used to populate the test paramters.    *    * @param name Name of the storage format.    * @param serdeClass Name of the SerDe class.    * @param inputFormatClass Name of the InputFormat class.    * @param outputFormatClass Name of the OutputFormat class.    * @return Object array containing the arguments.    */
specifier|protected
specifier|static
name|Object
index|[]
name|createTestArguments
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|serdeClass
parameter_list|,
name|String
name|inputFormatClass
parameter_list|,
name|String
name|outputFormatClass
parameter_list|)
block|{
name|Object
index|[]
name|args
init|=
block|{
name|name
block|,
name|serdeClass
block|,
name|inputFormatClass
block|,
name|outputFormatClass
block|}
decl_stmt|;
return|return
name|args
return|;
block|}
comment|/**    * Generates a collection of parameters that can be used as paramters for a JUnit test fixture.    * Each parameter represents one storage format that the fixture will run against. The list    * includes both native Hive storage formats as well as those enumerated in the    * ADDITIONAL_STORAGE_FORMATS table.    *    * @return List of storage format as a Collection of Object arrays, each containing (in order):    *         Storage format name, SerDe class name, InputFormat class name, OutputFormat class name.    *         This list is used as the parameters to JUnit parameterized tests.    */
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|asParameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
comment|// Add test parameters from official storage formats registered with Hive via
comment|// StorageFormatDescriptor.
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
for|for
control|(
name|StorageFormatDescriptor
name|descriptor
range|:
name|ServiceLoader
operator|.
name|load
argument_list|(
name|StorageFormatDescriptor
operator|.
name|class
argument_list|)
control|)
block|{
name|String
name|serdeClass
init|=
name|descriptor
operator|.
name|getSerde
argument_list|()
decl_stmt|;
if|if
condition|(
name|serdeClass
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|descriptor
operator|instanceof
name|RCFileStorageFormatDescriptor
condition|)
block|{
name|serdeClass
operator|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTRCFILESERDE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serdeClass
operator|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEDEFAULTSERDE
argument_list|)
expr_stmt|;
block|}
block|}
name|String
index|[]
name|names
init|=
operator|new
name|String
index|[
name|descriptor
operator|.
name|getNames
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|names
operator|=
name|descriptor
operator|.
name|getNames
argument_list|()
operator|.
name|toArray
argument_list|(
name|names
argument_list|)
expr_stmt|;
name|Object
index|[]
name|arguments
init|=
name|createTestArguments
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|,
name|serdeClass
argument_list|,
name|descriptor
operator|.
name|getInputFormat
argument_list|()
argument_list|,
name|descriptor
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
decl_stmt|;
name|parameters
operator|.
name|add
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
block|}
comment|// Add test parameters from storage formats specified in ADDITIONAL_STORAGE_FORMATS table.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ADDITIONAL_STORAGE_FORMATS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|serdeClass
init|=
operator|(
name|String
operator|)
name|ADDITIONAL_STORAGE_FORMATS
index|[
name|i
index|]
index|[
literal|1
index|]
decl_stmt|;
name|String
name|name
init|=
operator|(
name|String
operator|)
name|ADDITIONAL_STORAGE_FORMATS
index|[
name|i
index|]
index|[
literal|0
index|]
decl_stmt|;
name|String
name|inputFormatClass
init|=
operator|(
name|String
operator|)
name|ADDITIONAL_STORAGE_FORMATS
index|[
name|i
index|]
index|[
literal|2
index|]
decl_stmt|;
name|String
name|outputFormatClass
init|=
operator|(
name|String
operator|)
name|ADDITIONAL_STORAGE_FORMATS
index|[
name|i
index|]
index|[
literal|3
index|]
decl_stmt|;
name|assertTrue
argument_list|(
literal|"InputFormat for storage format not set"
argument_list|,
name|inputFormatClass
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"OutputFormat for storage format not set"
argument_list|,
name|outputFormatClass
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Object
index|[]
name|arguments
init|=
name|createTestArguments
argument_list|(
name|name
argument_list|,
name|serdeClass
argument_list|,
name|inputFormatClass
argument_list|,
name|outputFormatClass
argument_list|)
decl_stmt|;
name|parameters
operator|.
name|add
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
block|}
return|return
name|parameters
return|;
block|}
comment|/**    * Returns a list of the names of storage formats.    *    * @return List of names of storage formats.    */
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|names
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|StorageFormatDescriptor
name|descriptor
range|:
name|ServiceLoader
operator|.
name|load
argument_list|(
name|StorageFormatDescriptor
operator|.
name|class
argument_list|)
control|)
block|{
name|String
index|[]
name|formatNames
init|=
operator|new
name|String
index|[
name|descriptor
operator|.
name|getNames
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|formatNames
operator|=
name|descriptor
operator|.
name|getNames
argument_list|()
operator|.
name|toArray
argument_list|(
name|formatNames
argument_list|)
expr_stmt|;
name|String
index|[]
name|params
init|=
block|{
name|formatNames
index|[
literal|0
index|]
block|}
decl_stmt|;
name|names
operator|.
name|add
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|names
return|;
block|}
block|}
end_class

end_unit


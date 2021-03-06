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
name|Properties
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
name|ql
operator|.
name|exec
operator|.
name|FileSinkOperator
operator|.
name|RecordWriter
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
name|mapred
operator|.
name|JobConf
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
name|util
operator|.
name|Progressable
import|;
end_import

begin_comment
comment|/**  *<code>HiveOutputFormat</code> describes the output-specification for Hive's  * operators. It has a method  * {@link #getHiveRecordWriter(JobConf, Path, Class, boolean, Properties, Progressable)}  * , with various parameters used to create the final out file and get some  * specific settings.  *  * @see org.apache.hadoop.mapred.OutputFormat  * @see RecordWriter  * @see JobConf  */
end_comment

begin_interface
specifier|public
interface|interface
name|HiveOutputFormat
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|OutputFormat
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
comment|/**    * create the final out file and get some specific settings.    *    * @param jc    *          the job configuration file    * @param finalOutPath    *          the final output file to be created    * @param valueClass    *          the value class used for create    * @param isCompressed    *          whether the content is compressed or not    * @param tableProperties    *          the table properties of this file's corresponding table    * @param progress    *          progress used for status report    * @return the RecordWriter for the output file    */
name|RecordWriter
name|getHiveRecordWriter
parameter_list|(
name|JobConf
name|jc
parameter_list|,
name|Path
name|finalOutPath
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|valueClass
parameter_list|,
name|boolean
name|isCompressed
parameter_list|,
name|Properties
name|tableProperties
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit


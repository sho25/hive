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
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestHCatExternalDynamicPartitioned
extends|extends
name|TestHCatDynamicPartitioned
block|{
specifier|public
name|TestHCatExternalDynamicPartitioned
parameter_list|(
name|String
name|formatName
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
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|formatName
argument_list|,
name|serdeClass
argument_list|,
name|inputFormatClass
argument_list|,
name|outputFormatClass
argument_list|)
expr_stmt|;
name|tableName
operator|=
literal|"testHCatExternalDynamicPartitionedTable_"
operator|+
name|formatName
expr_stmt|;
name|generateWriteRecords
argument_list|(
name|NUM_RECORDS
argument_list|,
name|NUM_PARTITIONS
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|generateDataColumns
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Boolean
name|isTableExternal
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Run the external dynamic partitioning test but with single map task    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testHCatExternalDynamicCustomLocation
parameter_list|()
throws|throws
name|Exception
block|{
name|runHCatDynamicPartitionedTable
argument_list|(
literal|true
argument_list|,
literal|"mapred/externalDynamicOutput/${p1}"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


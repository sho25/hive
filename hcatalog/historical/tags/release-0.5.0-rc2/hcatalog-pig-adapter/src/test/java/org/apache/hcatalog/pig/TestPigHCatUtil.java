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
name|hcatalog
operator|.
name|pig
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|common
operator|.
name|HCatConstants
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
name|HCatFieldSchema
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ResourceSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ResourceSchema
operator|.
name|ResourceFieldSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|data
operator|.
name|DataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|impl
operator|.
name|util
operator|.
name|UDFContext
import|;
end_import

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
name|TestPigHCatUtil
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetBagSubSchema
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Define the expected schema.
name|ResourceFieldSchema
index|[]
name|bagSubFieldSchemas
init|=
operator|new
name|ResourceFieldSchema
index|[
literal|1
index|]
decl_stmt|;
name|bagSubFieldSchemas
index|[
literal|0
index|]
operator|=
operator|new
name|ResourceFieldSchema
argument_list|()
operator|.
name|setName
argument_list|(
literal|"innertuple"
argument_list|)
operator|.
name|setDescription
argument_list|(
literal|"The tuple in the bag"
argument_list|)
operator|.
name|setType
argument_list|(
name|DataType
operator|.
name|TUPLE
argument_list|)
expr_stmt|;
name|ResourceFieldSchema
index|[]
name|innerTupleFieldSchemas
init|=
operator|new
name|ResourceFieldSchema
index|[
literal|1
index|]
decl_stmt|;
name|innerTupleFieldSchemas
index|[
literal|0
index|]
operator|=
operator|new
name|ResourceFieldSchema
argument_list|()
operator|.
name|setName
argument_list|(
literal|"innerfield"
argument_list|)
operator|.
name|setType
argument_list|(
name|DataType
operator|.
name|CHARARRAY
argument_list|)
expr_stmt|;
name|bagSubFieldSchemas
index|[
literal|0
index|]
operator|.
name|setSchema
argument_list|(
operator|new
name|ResourceSchema
argument_list|()
operator|.
name|setFields
argument_list|(
name|innerTupleFieldSchemas
argument_list|)
argument_list|)
expr_stmt|;
name|ResourceSchema
name|expected
init|=
operator|new
name|ResourceSchema
argument_list|()
operator|.
name|setFields
argument_list|(
name|bagSubFieldSchemas
argument_list|)
decl_stmt|;
comment|// Get the actual converted schema.
name|HCatSchema
name|hCatSchema
init|=
operator|new
name|HCatSchema
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"innerLlama"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|HCatFieldSchema
name|hCatFieldSchema
init|=
operator|new
name|HCatFieldSchema
argument_list|(
literal|"llama"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|ARRAY
argument_list|,
name|hCatSchema
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ResourceSchema
name|actual
init|=
name|PigHCatUtil
operator|.
name|getBagSubSchema
argument_list|(
name|hCatFieldSchema
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|toString
argument_list|()
argument_list|,
name|actual
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetBagSubSchemaConfigured
parameter_list|()
throws|throws
name|Exception
block|{
comment|// NOTE: pig-0.8 sets client system properties by actually getting the client
comment|// system properties. Starting in pig-0.9 you must pass the properties in.
comment|// When updating our pig dependency this will need updated.
name|System
operator|.
name|setProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_INNER_TUPLE_NAME
argument_list|,
literal|"t"
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|HCatConstants
operator|.
name|HCAT_PIG_INNER_FIELD_NAME
argument_list|,
literal|"FIELDNAME_tuple"
argument_list|)
expr_stmt|;
name|UDFContext
operator|.
name|getUDFContext
argument_list|()
operator|.
name|setClientSystemProps
argument_list|()
expr_stmt|;
comment|// Define the expected schema.
name|ResourceFieldSchema
index|[]
name|bagSubFieldSchemas
init|=
operator|new
name|ResourceFieldSchema
index|[
literal|1
index|]
decl_stmt|;
name|bagSubFieldSchemas
index|[
literal|0
index|]
operator|=
operator|new
name|ResourceFieldSchema
argument_list|()
operator|.
name|setName
argument_list|(
literal|"t"
argument_list|)
operator|.
name|setDescription
argument_list|(
literal|"The tuple in the bag"
argument_list|)
operator|.
name|setType
argument_list|(
name|DataType
operator|.
name|TUPLE
argument_list|)
expr_stmt|;
name|ResourceFieldSchema
index|[]
name|innerTupleFieldSchemas
init|=
operator|new
name|ResourceFieldSchema
index|[
literal|1
index|]
decl_stmt|;
name|innerTupleFieldSchemas
index|[
literal|0
index|]
operator|=
operator|new
name|ResourceFieldSchema
argument_list|()
operator|.
name|setName
argument_list|(
literal|"llama_tuple"
argument_list|)
operator|.
name|setType
argument_list|(
name|DataType
operator|.
name|CHARARRAY
argument_list|)
expr_stmt|;
name|bagSubFieldSchemas
index|[
literal|0
index|]
operator|.
name|setSchema
argument_list|(
operator|new
name|ResourceSchema
argument_list|()
operator|.
name|setFields
argument_list|(
name|innerTupleFieldSchemas
argument_list|)
argument_list|)
expr_stmt|;
name|ResourceSchema
name|expected
init|=
operator|new
name|ResourceSchema
argument_list|()
operator|.
name|setFields
argument_list|(
name|bagSubFieldSchemas
argument_list|)
decl_stmt|;
comment|// Get the actual converted schema.
name|HCatSchema
name|actualHCatSchema
init|=
operator|new
name|HCatSchema
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|new
name|HCatFieldSchema
argument_list|(
literal|"innerLlama"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|STRING
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|HCatFieldSchema
name|actualHCatFieldSchema
init|=
operator|new
name|HCatFieldSchema
argument_list|(
literal|"llama"
argument_list|,
name|HCatFieldSchema
operator|.
name|Type
operator|.
name|ARRAY
argument_list|,
name|actualHCatSchema
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ResourceSchema
name|actual
init|=
name|PigHCatUtil
operator|.
name|getBagSubSchema
argument_list|(
name|actualHCatFieldSchema
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expected
operator|.
name|toString
argument_list|()
argument_list|,
name|actual
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|TestTableName
block|{
annotation|@
name|Test
specifier|public
name|void
name|fullName
parameter_list|()
block|{
name|TableName
name|name
init|=
operator|new
name|TableName
argument_list|(
literal|"cat"
argument_list|,
literal|"db"
argument_list|,
literal|"t"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cat"
argument_list|,
name|name
operator|.
name|getCat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db"
argument_list|,
name|name
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"t"
argument_list|,
name|name
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cat.db.t"
argument_list|,
name|name
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db.t"
argument_list|,
name|name
operator|.
name|getDbTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|fromString
parameter_list|()
block|{
name|TableName
name|name
init|=
name|TableName
operator|.
name|fromString
argument_list|(
literal|"cat.db.tab"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cat"
argument_list|,
name|name
operator|.
name|getCat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db"
argument_list|,
name|name
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"tab"
argument_list|,
name|name
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|name
operator|=
name|TableName
operator|.
name|fromString
argument_list|(
literal|"db.tab"
argument_list|,
literal|"cat"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cat"
argument_list|,
name|name
operator|.
name|getCat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db"
argument_list|,
name|name
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"tab"
argument_list|,
name|name
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|name
operator|=
name|TableName
operator|.
name|fromString
argument_list|(
literal|"tab"
argument_list|,
literal|"cat"
argument_list|,
literal|"db"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"cat"
argument_list|,
name|name
operator|.
name|getCat
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db"
argument_list|,
name|name
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"tab"
argument_list|,
name|name
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|TableName
operator|.
name|fromString
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"Name can't be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNotEmptyDbTable
parameter_list|()
block|{
name|TableName
name|name
init|=
operator|new
name|TableName
argument_list|(
literal|"cat"
argument_list|,
literal|"db"
argument_list|,
literal|"t"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"db.t"
argument_list|,
name|name
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|)
expr_stmt|;
name|name
operator|=
operator|new
name|TableName
argument_list|(
literal|"cat"
argument_list|,
literal|null
argument_list|,
literal|"t"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"t"
argument_list|,
name|name
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|)
expr_stmt|;
name|name
operator|=
operator|new
name|TableName
argument_list|(
literal|"cat"
argument_list|,
literal|""
argument_list|,
literal|"t"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"t"
argument_list|,
name|name
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


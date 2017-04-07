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
name|lockmgr
package|;
end_package

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
name|lockmgr
operator|.
name|HiveLockObject
operator|.
name|HiveLockObjectData
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
name|TestHiveLockObject
block|{
specifier|private
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testEqualsAndHashCode
parameter_list|()
block|{
name|HiveLockObjectData
name|data1
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|"select * from mytable"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HiveLockObjectData
name|data2
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|"select * from mytable"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|data1
argument_list|,
name|data2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|data1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|data2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|HiveLockObject
name|obj1
init|=
operator|new
name|HiveLockObject
argument_list|(
literal|"mytable"
argument_list|,
name|data1
argument_list|)
decl_stmt|;
name|HiveLockObject
name|obj2
init|=
operator|new
name|HiveLockObject
argument_list|(
literal|"mytable"
argument_list|,
name|data2
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|obj1
argument_list|,
name|obj2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|obj1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|obj2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTruncate
parameter_list|()
block|{
name|conf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_QUERY_STRING_MAX_LENGTH
argument_list|,
literal|1000000
argument_list|)
expr_stmt|;
name|HiveLockObjectData
name|data0
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|"01234567890"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"With default settings query string should not be truncated"
argument_list|,
name|data0
operator|.
name|getQueryStr
argument_list|()
operator|.
name|length
argument_list|()
argument_list|,
literal|11
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_LOCK_QUERY_STRING_MAX_LENGTH
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|HiveLockObjectData
name|data1
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|"01234567890"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HiveLockObjectData
name|data2
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|"0123456789"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HiveLockObjectData
name|data3
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|"012345678"
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|HiveLockObjectData
name|data4
init|=
operator|new
name|HiveLockObjectData
argument_list|(
literal|"ID1"
argument_list|,
literal|"SHARED"
argument_list|,
literal|"1997-07-01"
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Long string truncation failed"
argument_list|,
name|data1
operator|.
name|getQueryStr
argument_list|()
operator|.
name|length
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"String truncation failed"
argument_list|,
name|data2
operator|.
name|getQueryStr
argument_list|()
operator|.
name|length
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Short string should not be truncated"
argument_list|,
name|data3
operator|.
name|getQueryStr
argument_list|()
operator|.
name|length
argument_list|()
argument_list|,
literal|9
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
literal|"Null query string handling failed"
argument_list|,
name|data4
operator|.
name|getQueryStr
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


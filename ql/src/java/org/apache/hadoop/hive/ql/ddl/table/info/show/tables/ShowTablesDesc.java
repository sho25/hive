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
name|ddl
operator|.
name|table
operator|.
name|info
operator|.
name|show
operator|.
name|tables
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|TableType
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
name|ddl
operator|.
name|DDLDesc
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
name|plan
operator|.
name|Explain
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
name|plan
operator|.
name|Explain
operator|.
name|Level
import|;
end_import

begin_comment
comment|/**  * DDL task description for SHOW TABLES commands.  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Show Tables"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|ShowTablesDesc
implements|implements
name|DDLDesc
implements|,
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLES_VIEWS_SCHEMA
init|=
literal|"tab_name#string"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EXTENDED_TABLES_SCHEMA
init|=
literal|"tab_name,table_type#string,string"
decl_stmt|;
specifier|private
specifier|final
name|String
name|resFile
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbName
decl_stmt|;
specifier|private
specifier|final
name|String
name|pattern
decl_stmt|;
specifier|private
specifier|final
name|TableType
name|typeFilter
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isExtended
decl_stmt|;
specifier|public
name|ShowTablesDesc
parameter_list|(
name|Path
name|resFile
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|pattern
parameter_list|,
name|TableType
name|typeFilter
parameter_list|,
name|boolean
name|isExtended
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
name|this
operator|.
name|typeFilter
operator|=
name|typeFilter
expr_stmt|;
name|this
operator|.
name|isExtended
operator|=
name|isExtended
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"pattern"
argument_list|)
specifier|public
name|String
name|getPattern
parameter_list|()
block|{
return|return
name|pattern
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"result file"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"database name"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"extended"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isExtended
parameter_list|()
block|{
return|return
name|isExtended
return|;
block|}
comment|/** For explain only. */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table type filter"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getTypeFilterString
parameter_list|()
block|{
return|return
name|typeFilter
operator|.
name|name
argument_list|()
return|;
block|}
specifier|public
name|TableType
name|getTypeFilter
parameter_list|()
block|{
return|return
name|typeFilter
return|;
block|}
specifier|public
name|String
name|getSchema
parameter_list|()
block|{
return|return
name|isExtended
condition|?
name|EXTENDED_TABLES_SCHEMA
else|:
name|TABLES_VIEWS_SCHEMA
return|;
block|}
block|}
end_class

end_unit


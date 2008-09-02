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
name|metastore
package|;
end_package

begin_comment
comment|// hadoop stuff
end_comment

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
name|metastore
operator|.
name|api
operator|.
name|MetaException
import|;
end_import

begin_comment
comment|/**  *  * MetaStore  *  * A MetaStore on top of HDFS. The goal is to expose Tables/Schemas to users rather than flat files and  * Wiki that describes their structure and contents.  * The MetaStore is used in conjunction with org.apache.hadoop.contrib.hive.contrib.serde.SerDe and flat files for  * storing other schema information in addition to the SerDe library for each table.  *  *  * The store has the concept of a db. The db is assumed to be optionally prefixed to the tableName and followed by a dot.  * e.g., falcon.view_photo, default.tmp_pete.  * In the schema store on disk, these dbs are stored as SCHEMA_STORE_PATH/<name>.db/ and then schemas under it are<tname>.dir/schema.  * The schema store should have a symbolic link - ln -s SCHEMA_STORE_PATH SCHEMA_STORE_PATH/default.db .  * All of a db's tables are stored in $ROOT_WAREHOUSE/dbname/tablename(s). The default db is special cased to  * $ROOT_WAREHOUSE since dfs does not have symbolic links and since our data is already there.  *  * Internally, almost everywhere, the code ignores the db name - that is other than the following conditions:  *  * 1. When looking up the schema file from the table name (s/\./.db/)  * 2. When deriving the table's intended location on DFS (s/^(.+?)\.(.+)$/$1/$2/)  * 3. When calling getFields(db.table.field1.field2). Here it peels off the prefix and checks if it's a db name  *  *  * TODOs:  *  * Think about making "db" an object and schema too.  * Try to abstract away how we store dbs in the metastore and the warehouse. The latter is hard because the hive  * runtime needs a way to lookup a table's schema from looking at the path to a specific partition that the map  * is running on.  *  *  *  */
end_comment

begin_class
specifier|public
class|class
name|MetaStore
block|{
comment|/**    * Every schema must have a name, location and a serde    */
specifier|protected
specifier|static
specifier|final
name|String
index|[]
name|RequiredSchemaKeys
init|=
block|{
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
name|Constants
operator|.
name|META_TABLE_NAME
block|,
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
name|Constants
operator|.
name|META_TABLE_LOCATION
block|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde
operator|.
name|Constants
operator|.
name|SERIALIZATION_LIB
block|,   }
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LogKey
init|=
literal|"hive.log"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DefaultDB
init|=
literal|"default"
decl_stmt|;
specifier|static
specifier|public
name|boolean
name|dbExists
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|FileStore
argument_list|(
name|conf
argument_list|)
operator|.
name|dbExists
argument_list|(
name|dbName
argument_list|)
return|;
block|}
specifier|static
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getDbs
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|FileStore
argument_list|(
name|conf
argument_list|)
operator|.
name|getDatabases
argument_list|()
return|;
block|}
block|}
end_class

begin_empty_stmt
empty_stmt|;
end_empty_stmt

end_unit


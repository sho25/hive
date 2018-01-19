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
name|api
operator|.
name|repl
operator|.
name|commands
package|;
end_package

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
name|api
operator|.
name|repl
operator|.
name|Command
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
name|api
operator|.
name|repl
operator|.
name|ReplicationUtils
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
name|ReaderWriter
import|;
end_import

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
name|util
operator|.
name|Collections
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

begin_class
specifier|public
class|class
name|ExportCommand
implements|implements
name|Command
block|{
specifier|private
name|String
name|exportLocation
decl_stmt|;
specifier|private
name|String
name|dbName
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|tableName
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ptnDesc
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|eventId
decl_stmt|;
specifier|private
name|boolean
name|isMetadataOnly
init|=
literal|false
decl_stmt|;
specifier|public
name|ExportCommand
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ptnDesc
parameter_list|,
name|String
name|exportLocation
parameter_list|,
name|boolean
name|isMetadataOnly
parameter_list|,
name|long
name|eventId
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|ptnDesc
operator|=
name|ptnDesc
expr_stmt|;
name|this
operator|.
name|exportLocation
operator|=
name|exportLocation
expr_stmt|;
name|this
operator|.
name|isMetadataOnly
operator|=
name|isMetadataOnly
expr_stmt|;
name|this
operator|.
name|eventId
operator|=
name|eventId
expr_stmt|;
block|}
comment|/**    * Trivial ctor to support Writable reflections instantiation    * do not expect to use this object as-is, unless you call    * readFields after using this ctor    */
specifier|public
name|ExportCommand
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|get
parameter_list|()
block|{
comment|// EXPORT TABLE tablename [PARTITION (part_column="value"[, ...])]
comment|// TO 'export_target_path'
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"EXPORT TABLE "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"."
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
comment|// TODO: Handle quoted tablenames
name|sb
operator|.
name|append
argument_list|(
name|ReplicationUtils
operator|.
name|partitionDescriptor
argument_list|(
name|ptnDesc
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" TO '"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|exportLocation
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\' FOR "
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMetadataOnly
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"METADATA "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"REPLICATION(\'"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|eventId
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\')"
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRetriable
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// Export is trivially retriable (after clearing out the staging dir provided.)
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isUndoable
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// Export is trivially undoable - in that nothing needs doing to undo it.
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getUndo
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|cleanupLocationsPerRetry
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|exportLocation
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|cleanupLocationsAfterEvent
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|exportLocation
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEventId
parameter_list|()
block|{
return|return
name|eventId
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|dataOutput
parameter_list|)
throws|throws
name|IOException
block|{
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|ptnDesc
argument_list|)
expr_stmt|;
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|exportLocation
argument_list|)
expr_stmt|;
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|isMetadataOnly
argument_list|)
expr_stmt|;
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|eventId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|dataInput
parameter_list|)
throws|throws
name|IOException
block|{
name|dbName
operator|=
operator|(
name|String
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
name|tableName
operator|=
operator|(
name|String
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
name|ptnDesc
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
name|exportLocation
operator|=
operator|(
name|String
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
name|isMetadataOnly
operator|=
operator|(
name|Boolean
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
name|eventId
operator|=
operator|(
name|Long
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


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
name|common
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|fs
operator|.
name|PathFilter
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

begin_class
specifier|public
class|class
name|ValidWriteIds
block|{
specifier|public
specifier|static
specifier|final
name|ValidWriteIds
name|NO_WRITE_IDS
init|=
operator|new
name|ValidWriteIds
argument_list|(
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MM_PREFIX
init|=
literal|"mm"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CURRENT_SUFFIX
init|=
literal|".current"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ValidWriteIds
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VALID_WRITEIDS_PREFIX
init|=
literal|"hive.valid.write.ids."
decl_stmt|;
specifier|private
specifier|final
name|long
name|lowWatermark
decl_stmt|,
name|highWatermark
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|areIdsValid
decl_stmt|;
specifier|private
specifier|final
name|HashSet
argument_list|<
name|Long
argument_list|>
name|ids
decl_stmt|;
specifier|private
name|String
name|source
init|=
literal|null
decl_stmt|;
specifier|public
name|ValidWriteIds
parameter_list|(
name|long
name|lowWatermark
parameter_list|,
name|long
name|highWatermark
parameter_list|,
name|boolean
name|areIdsValid
parameter_list|,
name|HashSet
argument_list|<
name|Long
argument_list|>
name|ids
parameter_list|)
block|{
name|this
operator|.
name|lowWatermark
operator|=
name|lowWatermark
expr_stmt|;
name|this
operator|.
name|highWatermark
operator|=
name|highWatermark
expr_stmt|;
name|this
operator|.
name|areIdsValid
operator|=
name|areIdsValid
expr_stmt|;
name|this
operator|.
name|ids
operator|=
name|ids
expr_stmt|;
block|}
specifier|public
specifier|static
name|ValidWriteIds
name|createFromConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|)
block|{
return|return
name|createFromConf
argument_list|(
name|conf
argument_list|,
name|dbName
operator|+
literal|"."
operator|+
name|tblName
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ValidWriteIds
name|createFromConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|fullTblName
parameter_list|)
block|{
name|String
name|key
init|=
name|createConfKey
argument_list|(
name|fullTblName
argument_list|)
decl_stmt|;
name|String
name|idStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|String
name|current
init|=
name|conf
operator|.
name|get
argument_list|(
name|key
operator|+
name|CURRENT_SUFFIX
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|idStr
operator|==
literal|null
operator|||
name|idStr
operator|.
name|isEmpty
argument_list|()
condition|)
return|return
literal|null
return|;
return|return
operator|new
name|ValidWriteIds
argument_list|(
name|idStr
argument_list|,
name|current
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|createConfKey
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|)
block|{
return|return
name|createConfKey
argument_list|(
name|dbName
operator|+
literal|"."
operator|+
name|tblName
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|createConfKey
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
return|return
name|VALID_WRITEIDS_PREFIX
operator|+
name|fullName
return|;
block|}
specifier|private
name|ValidWriteIds
parameter_list|(
name|String
name|src
parameter_list|,
name|String
name|current
parameter_list|)
block|{
comment|// TODO: lifted from ACID config implementation... optimize if needed? e.g. ranges, base64
name|String
index|[]
name|values
init|=
name|src
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|highWatermark
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|values
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|lowWatermark
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|values
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|values
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|areIdsValid
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|values
index|[
literal|2
index|]
argument_list|)
operator|>
literal|0
expr_stmt|;
name|ids
operator|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|3
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|ids
operator|.
name|add
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|long
name|currentId
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|current
argument_list|)
decl_stmt|;
if|if
condition|(
name|areIdsValid
condition|)
block|{
name|ids
operator|.
name|add
argument_list|(
name|currentId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ids
operator|.
name|remove
argument_list|(
name|currentId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|long
name|currentId
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|current
argument_list|)
decl_stmt|;
name|areIdsValid
operator|=
literal|true
expr_stmt|;
name|ids
operator|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|ids
operator|.
name|add
argument_list|(
name|currentId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|areIdsValid
operator|=
literal|false
expr_stmt|;
name|ids
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|addCurrentToConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|long
name|mmWriteId
parameter_list|)
block|{
name|String
name|key
init|=
name|createConfKey
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
operator|+
name|CURRENT_SUFFIX
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting "
operator|+
name|key
operator|+
literal|" => "
operator|+
name|mmWriteId
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|key
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|mmWriteId
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addToConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|)
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|source
operator|=
name|toString
argument_list|()
expr_stmt|;
block|}
name|String
name|key
init|=
name|createConfKey
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting "
operator|+
name|key
operator|+
literal|" => "
operator|+
name|source
operator|+
literal|" (old value was "
operator|+
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|key
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|clearConf
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Unsetting "
operator|+
name|createConfKey
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|conf
operator|.
name|unset
argument_list|(
name|createConfKey
argument_list|(
name|dbName
argument_list|,
name|tblName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
comment|// TODO: lifted from ACID config implementation... optimize if needed? e.g. ranges, base64
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|highWatermark
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|lowWatermark
argument_list|)
expr_stmt|;
if|if
condition|(
name|ids
operator|!=
literal|null
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|areIdsValid
condition|?
literal|1
else|:
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|id
range|:
name|ids
control|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|buf
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isValid
parameter_list|(
name|long
name|writeId
parameter_list|)
block|{
if|if
condition|(
name|writeId
operator|<
literal|0
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Incorrect write ID "
operator|+
name|writeId
argument_list|)
throw|;
if|if
condition|(
name|writeId
operator|<=
name|lowWatermark
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|writeId
operator|>=
name|highWatermark
condition|)
return|return
literal|false
return|;
return|return
name|ids
operator|!=
literal|null
operator|&&
operator|(
name|areIdsValid
operator|==
name|ids
operator|.
name|contains
argument_list|(
name|writeId
argument_list|)
operator|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getMmFilePrefix
parameter_list|(
name|long
name|mmWriteId
parameter_list|)
block|{
return|return
name|MM_PREFIX
operator|+
literal|"_"
operator|+
name|mmWriteId
return|;
block|}
specifier|public
specifier|static
class|class
name|IdPathFilter
implements|implements
name|PathFilter
block|{
specifier|private
specifier|final
name|String
name|mmDirName
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isMatch
decl_stmt|,
name|isIgnoreTemp
decl_stmt|;
specifier|public
name|IdPathFilter
parameter_list|(
name|long
name|writeId
parameter_list|,
name|boolean
name|isMatch
parameter_list|)
block|{
name|this
argument_list|(
name|writeId
argument_list|,
name|isMatch
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IdPathFilter
parameter_list|(
name|long
name|writeId
parameter_list|,
name|boolean
name|isMatch
parameter_list|,
name|boolean
name|isIgnoreTemp
parameter_list|)
block|{
name|this
operator|.
name|mmDirName
operator|=
name|ValidWriteIds
operator|.
name|getMmFilePrefix
argument_list|(
name|writeId
argument_list|)
expr_stmt|;
name|this
operator|.
name|isMatch
operator|=
name|isMatch
expr_stmt|;
name|this
operator|.
name|isIgnoreTemp
operator|=
name|isIgnoreTemp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|String
name|name
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|mmDirName
argument_list|)
condition|)
block|{
return|return
name|isMatch
return|;
block|}
if|if
condition|(
name|isIgnoreTemp
operator|&&
name|name
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|char
name|c
init|=
name|name
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'.'
operator|||
name|c
operator|==
literal|'_'
condition|)
return|return
literal|false
return|;
comment|// Regardless of isMatch, ignore this.
block|}
return|return
operator|!
name|isMatch
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|AnyIdDirFilter
implements|implements
name|PathFilter
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|String
name|name
init|=
name|path
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|startsWith
argument_list|(
name|MM_PREFIX
operator|+
literal|"_"
argument_list|)
condition|)
return|return
literal|false
return|;
name|String
name|idStr
init|=
name|name
operator|.
name|substring
argument_list|(
name|MM_PREFIX
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
name|Long
operator|.
name|parseLong
argument_list|(
name|idStr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
specifier|public
specifier|static
name|Long
name|extractWriteId
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|String
name|fileName
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|fileName
operator|.
name|split
argument_list|(
literal|"_"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|<
literal|2
operator|||
operator|!
name|MM_PREFIX
operator|.
name|equals
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot extract write ID for a MM table: "
operator|+
name|file
operator|+
literal|" ("
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|parts
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|long
name|writeId
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|writeId
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Cannot extract write ID for a MM table: "
operator|+
name|file
operator|+
literal|"; parsing "
operator|+
name|parts
index|[
literal|1
index|]
operator|+
literal|" got "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|writeId
return|;
block|}
block|}
end_class

end_unit


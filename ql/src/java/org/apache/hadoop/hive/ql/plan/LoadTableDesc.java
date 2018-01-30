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
name|plan
package|;
end_package

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
name|Utilities
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
name|AcidUtils
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
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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

begin_comment
comment|/**  * LoadTableDesc.  *  */
end_comment

begin_class
specifier|public
class|class
name|LoadTableDesc
extends|extends
name|LoadDesc
implements|implements
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
name|LoadFileType
name|loadFileType
decl_stmt|;
specifier|private
name|DynamicPartitionCtx
name|dpCtx
decl_stmt|;
specifier|private
name|ListBucketingCtx
name|lbCtx
decl_stmt|;
specifier|private
name|boolean
name|inheritTableSpecs
init|=
literal|true
decl_stmt|;
comment|//For partitions, flag controlling whether the current
comment|//table specs are to be used
specifier|private
name|int
name|stmtId
decl_stmt|;
specifier|private
name|Long
name|currentTransactionId
decl_stmt|;
specifier|private
name|boolean
name|isInsertOverwrite
decl_stmt|;
comment|// TODO: the below seem like they should just be combined into partitionDesc
specifier|private
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
name|TableDesc
name|table
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
decl_stmt|;
comment|// NOTE: this partitionSpec has to be ordered map
specifier|public
enum|enum
name|LoadFileType
block|{
comment|/**      * This corresponds to INSERT OVERWRITE and REPL LOAD for INSERT OVERWRITE event.      * Remove all existing data before copy/move      */
name|REPLACE_ALL
block|,
comment|/**      * This corresponds to INSERT INTO and LOAD DATA.      * If any file exist while copy, then just duplicate the file      */
name|KEEP_EXISTING
block|,
comment|/**      * This corresponds to REPL LOAD where if we re-apply the same event then need to overwrite      * the file instead of making a duplicate copy.      * If any file exist while copy, then just overwrite the file      */
name|OVERWRITE_EXISTING
block|}
specifier|public
name|LoadTableDesc
parameter_list|(
specifier|final
name|LoadTableDesc
name|o
parameter_list|)
block|{
name|super
argument_list|(
name|o
operator|.
name|getSourcePath
argument_list|()
argument_list|,
name|o
operator|.
name|getWriteType
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|loadFileType
operator|=
name|o
operator|.
name|loadFileType
expr_stmt|;
name|this
operator|.
name|dpCtx
operator|=
name|o
operator|.
name|dpCtx
expr_stmt|;
name|this
operator|.
name|lbCtx
operator|=
name|o
operator|.
name|lbCtx
expr_stmt|;
name|this
operator|.
name|inheritTableSpecs
operator|=
name|o
operator|.
name|inheritTableSpecs
expr_stmt|;
name|this
operator|.
name|currentTransactionId
operator|=
name|o
operator|.
name|currentTransactionId
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|o
operator|.
name|table
expr_stmt|;
name|this
operator|.
name|partitionSpec
operator|=
name|o
operator|.
name|partitionSpec
expr_stmt|;
block|}
specifier|public
name|LoadTableDesc
parameter_list|(
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
specifier|final
name|LoadFileType
name|loadFileType
parameter_list|,
specifier|final
name|AcidUtils
operator|.
name|Operation
name|writeType
parameter_list|,
name|Long
name|currentTransactionId
parameter_list|)
block|{
name|super
argument_list|(
name|sourcePath
argument_list|,
name|writeType
argument_list|)
expr_stmt|;
if|if
condition|(
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"creating part LTD from "
operator|+
name|sourcePath
operator|+
literal|" to "
operator|+
operator|(
operator|(
name|table
operator|.
name|getProperties
argument_list|()
operator|==
literal|null
operator|)
condition|?
literal|"null"
else|:
name|table
operator|.
name|getTableName
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
name|init
argument_list|(
name|table
argument_list|,
name|partitionSpec
argument_list|,
name|loadFileType
argument_list|,
name|currentTransactionId
argument_list|)
expr_stmt|;
block|}
comment|/**    * For use with non-ACID compliant operations, such as LOAD    * @param sourcePath    * @param table    * @param partitionSpec    * @param loadFileType    */
specifier|public
name|LoadTableDesc
parameter_list|(
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
specifier|final
name|LoadFileType
name|loadFileType
parameter_list|,
specifier|final
name|Long
name|txnId
parameter_list|)
block|{
name|this
argument_list|(
name|sourcePath
argument_list|,
name|table
argument_list|,
name|partitionSpec
argument_list|,
name|loadFileType
argument_list|,
name|AcidUtils
operator|.
name|Operation
operator|.
name|NOT_ACID
argument_list|,
name|txnId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LoadTableDesc
parameter_list|(
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
specifier|final
name|AcidUtils
operator|.
name|Operation
name|writeType
parameter_list|,
name|Long
name|currentTransactionId
parameter_list|)
block|{
name|this
argument_list|(
name|sourcePath
argument_list|,
name|table
argument_list|,
name|partitionSpec
argument_list|,
name|LoadFileType
operator|.
name|REPLACE_ALL
argument_list|,
name|writeType
argument_list|,
name|currentTransactionId
argument_list|)
expr_stmt|;
block|}
comment|/**    * For DDL operations that are not ACID compliant.    * @param sourcePath    * @param table    * @param partitionSpec    */
specifier|public
name|LoadTableDesc
parameter_list|(
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
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
name|TableDesc
name|table
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|Long
name|txnId
parameter_list|)
block|{
name|this
argument_list|(
name|sourcePath
argument_list|,
name|table
argument_list|,
name|partitionSpec
argument_list|,
name|LoadFileType
operator|.
name|REPLACE_ALL
argument_list|,
name|AcidUtils
operator|.
name|Operation
operator|.
name|NOT_ACID
argument_list|,
name|txnId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LoadTableDesc
parameter_list|(
specifier|final
name|Path
name|sourcePath
parameter_list|,
specifier|final
name|TableDesc
name|table
parameter_list|,
specifier|final
name|DynamicPartitionCtx
name|dpCtx
parameter_list|,
specifier|final
name|AcidUtils
operator|.
name|Operation
name|writeType
parameter_list|,
name|boolean
name|isReplace
parameter_list|,
name|Long
name|txnId
parameter_list|)
block|{
name|super
argument_list|(
name|sourcePath
argument_list|,
name|writeType
argument_list|)
expr_stmt|;
if|if
condition|(
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"creating LTD from "
operator|+
name|sourcePath
operator|+
literal|" to "
operator|+
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|dpCtx
operator|=
name|dpCtx
expr_stmt|;
name|LoadFileType
name|lft
init|=
name|isReplace
condition|?
name|LoadFileType
operator|.
name|REPLACE_ALL
else|:
name|LoadFileType
operator|.
name|OVERWRITE_EXISTING
decl_stmt|;
if|if
condition|(
name|dpCtx
operator|!=
literal|null
operator|&&
name|dpCtx
operator|.
name|getPartSpec
argument_list|()
operator|!=
literal|null
operator|&&
name|partitionSpec
operator|==
literal|null
condition|)
block|{
name|init
argument_list|(
name|table
argument_list|,
name|dpCtx
operator|.
name|getPartSpec
argument_list|()
argument_list|,
name|lft
argument_list|,
name|txnId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|init
argument_list|(
name|table
argument_list|,
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
name|lft
argument_list|,
name|txnId
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|init
parameter_list|(
specifier|final
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
name|TableDesc
name|table
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
specifier|final
name|LoadFileType
name|loadFileType
parameter_list|,
name|Long
name|txnId
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
name|this
operator|.
name|loadFileType
operator|=
name|loadFileType
expr_stmt|;
name|this
operator|.
name|currentTransactionId
operator|=
name|txnId
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"table"
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
name|TableDesc
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
specifier|final
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
name|TableDesc
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"partition"
argument_list|)
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionSpec
parameter_list|()
block|{
return|return
name|partitionSpec
return|;
block|}
specifier|public
name|void
name|setPartitionSpec
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
block|{
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"replace"
argument_list|)
specifier|public
name|boolean
name|getReplace
parameter_list|()
block|{
return|return
operator|(
name|loadFileType
operator|==
name|LoadFileType
operator|.
name|REPLACE_ALL
operator|)
return|;
block|}
specifier|public
name|LoadFileType
name|getLoadFileType
parameter_list|()
block|{
return|return
name|loadFileType
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"micromanaged table"
argument_list|)
specifier|public
name|Boolean
name|isMmTableExplain
parameter_list|()
block|{
return|return
name|isMmTable
argument_list|()
condition|?
literal|true
else|:
literal|null
return|;
block|}
specifier|public
name|boolean
name|isMmTable
parameter_list|()
block|{
return|return
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|table
operator|.
name|getProperties
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|setLoadFileType
parameter_list|(
name|LoadFileType
name|loadFileType
parameter_list|)
block|{
name|this
operator|.
name|loadFileType
operator|=
name|loadFileType
expr_stmt|;
block|}
specifier|public
name|DynamicPartitionCtx
name|getDPCtx
parameter_list|()
block|{
return|return
name|dpCtx
return|;
block|}
specifier|public
name|void
name|setDPCtx
parameter_list|(
specifier|final
name|DynamicPartitionCtx
name|dpCtx
parameter_list|)
block|{
name|this
operator|.
name|dpCtx
operator|=
name|dpCtx
expr_stmt|;
block|}
specifier|public
name|boolean
name|getInheritTableSpecs
parameter_list|()
block|{
return|return
name|inheritTableSpecs
return|;
block|}
specifier|public
name|void
name|setInheritTableSpecs
parameter_list|(
name|boolean
name|inheritTableSpecs
parameter_list|)
block|{
name|this
operator|.
name|inheritTableSpecs
operator|=
name|inheritTableSpecs
expr_stmt|;
block|}
specifier|public
name|boolean
name|isInsertOverwrite
parameter_list|()
block|{
return|return
name|this
operator|.
name|isInsertOverwrite
return|;
block|}
specifier|public
name|void
name|setInsertOverwrite
parameter_list|(
name|boolean
name|v
parameter_list|)
block|{
name|this
operator|.
name|isInsertOverwrite
operator|=
name|v
expr_stmt|;
block|}
comment|/**    * @return the lbCtx    */
specifier|public
name|ListBucketingCtx
name|getLbCtx
parameter_list|()
block|{
return|return
name|lbCtx
return|;
block|}
comment|/**    * @param lbCtx the lbCtx to set    */
specifier|public
name|void
name|setLbCtx
parameter_list|(
name|ListBucketingCtx
name|lbCtx
parameter_list|)
block|{
name|this
operator|.
name|lbCtx
operator|=
name|lbCtx
expr_stmt|;
block|}
specifier|public
name|long
name|getTxnId
parameter_list|()
block|{
return|return
name|currentTransactionId
operator|==
literal|null
condition|?
literal|0
else|:
name|currentTransactionId
return|;
block|}
specifier|public
name|int
name|getStmtId
parameter_list|()
block|{
return|return
name|stmtId
return|;
block|}
comment|//todo: should this not be passed in the c'tor?
specifier|public
name|void
name|setStmtId
parameter_list|(
name|int
name|stmtId
parameter_list|)
block|{
name|this
operator|.
name|stmtId
operator|=
name|stmtId
expr_stmt|;
block|}
block|}
end_class

end_unit


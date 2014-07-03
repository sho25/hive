begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|optimizer
operator|.
name|optiq
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|LinkedList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FieldSchema
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
name|ColumnInfo
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
name|CommonJoinOperator
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
name|metadata
operator|.
name|Table
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
name|ColStatistics
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
name|Statistics
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
name|stats
operator|.
name|StatsUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|TableAccessRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptAbstractTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|relopt
operator|.
name|RelOptSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataType
import|;
end_import

begin_comment
comment|/*  * Fix Me:   * 1. Column Pruning  * 2. Partition Pruning  * 3. Stats  */
end_comment

begin_class
specifier|public
class|class
name|RelOptHiveTable
extends|extends
name|RelOptAbstractTable
block|{
specifier|private
specifier|final
name|Table
name|m_hiveTblMetadata
decl_stmt|;
specifier|private
name|double
name|m_rowCount
init|=
operator|-
literal|1
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|m_columnIdxToSizeMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|m_bucketingColMap
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|m_bucketingSortColMap
decl_stmt|;
name|Statistics
name|m_hiveStats
decl_stmt|;
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|m_hiveColStats
init|=
operator|new
name|ArrayList
argument_list|<
name|ColStatistics
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RelOptHiveTable
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// NOTE: name here is the table alias which may or may not be the real name in
comment|// metadata. Use
comment|// m_hiveTblMetadata.getTableName() for table name and
comment|// m_hiveTblMetadata.getDbName() for db name.
specifier|public
name|RelOptHiveTable
parameter_list|(
name|RelOptSchema
name|schema
parameter_list|,
name|String
name|name
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|Table
name|hiveTblMetadata
parameter_list|,
name|Statistics
name|stats
parameter_list|)
block|{
name|super
argument_list|(
name|schema
argument_list|,
name|name
argument_list|,
name|rowType
argument_list|)
expr_stmt|;
name|m_hiveTblMetadata
operator|=
name|hiveTblMetadata
expr_stmt|;
block|}
specifier|public
name|RelOptHiveTable
parameter_list|(
name|RelOptSchema
name|optiqSchema
parameter_list|,
name|String
name|name
parameter_list|,
name|RelDataType
name|rowType
parameter_list|,
name|Table
name|hiveTblMetadata
parameter_list|,
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|hiveSchema
parameter_list|)
block|{
name|super
argument_list|(
name|optiqSchema
argument_list|,
name|name
argument_list|,
name|rowType
argument_list|)
expr_stmt|;
name|m_hiveTblMetadata
operator|=
name|hiveTblMetadata
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|neededColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|ci
range|:
name|hiveSchema
control|)
block|{
name|neededColumns
operator|.
name|add
argument_list|(
name|ci
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//TODO: Fix below two stats
name|m_hiveColStats
operator|=
name|StatsUtils
operator|.
name|getTableColumnStats
argument_list|(
name|m_hiveTblMetadata
argument_list|,
name|hiveSchema
argument_list|,
name|neededColumns
argument_list|)
expr_stmt|;
name|m_rowCount
operator|=
name|StatsUtils
operator|.
name|getNumRows
argument_list|(
name|m_hiveTblMetadata
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isKey
parameter_list|(
name|BitSet
name|arg0
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelNode
name|toRel
parameter_list|(
name|ToRelContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|TableAccessRel
argument_list|(
name|context
operator|.
name|getCluster
argument_list|()
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|arg0
parameter_list|)
block|{
return|return
name|arg0
operator|.
name|isInstance
argument_list|(
name|this
argument_list|)
condition|?
name|arg0
operator|.
name|cast
argument_list|(
name|this
argument_list|)
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getRowCount
parameter_list|()
block|{
return|return
name|m_rowCount
return|;
block|}
specifier|public
name|Table
name|getHiveTableMD
parameter_list|()
block|{
return|return
name|m_hiveTblMetadata
return|;
block|}
specifier|public
name|Statistics
name|getHiveStats
parameter_list|()
block|{
return|return
name|m_hiveStats
return|;
block|}
specifier|private
name|String
name|getColNameList
parameter_list|(
name|Set
argument_list|<
name|Integer
argument_list|>
name|colLst
parameter_list|)
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|schema
init|=
name|m_hiveTblMetadata
operator|.
name|getAllCols
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|i
range|:
name|colLst
control|)
block|{
name|String
name|colName
init|=
operator|(
name|i
operator|<
name|schema
operator|.
name|size
argument_list|()
operator|)
condition|?
name|m_hiveTblMetadata
operator|.
name|getAllCols
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
else|:
literal|""
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
name|colName
argument_list|)
expr_stmt|;
else|else
name|sb
operator|.
name|append
argument_list|(
literal|", "
operator|+
name|colName
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|getColStat
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|projIndxLst
parameter_list|)
block|{
if|if
condition|(
name|projIndxLst
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|colsWithoutStats
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ColStatistics
argument_list|>
name|hiveColStatLst
init|=
operator|new
name|LinkedList
argument_list|<
name|ColStatistics
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|i
range|:
name|projIndxLst
control|)
block|{
if|if
condition|(
name|i
operator|>=
name|m_hiveColStats
operator|.
name|size
argument_list|()
condition|)
name|colsWithoutStats
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
else|else
name|hiveColStatLst
operator|.
name|add
argument_list|(
name|m_hiveColStats
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|colsWithoutStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|logMsg
init|=
literal|"No Stats for DB@Table "
operator|+
name|m_hiveTblMetadata
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|", Columns: "
operator|+
name|getColNameList
argument_list|(
name|colsWithoutStats
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|logMsg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|logMsg
argument_list|)
throw|;
block|}
return|return
name|hiveColStatLst
return|;
block|}
else|else
block|{
return|return
name|m_hiveColStats
return|;
block|}
block|}
block|}
end_class

end_unit


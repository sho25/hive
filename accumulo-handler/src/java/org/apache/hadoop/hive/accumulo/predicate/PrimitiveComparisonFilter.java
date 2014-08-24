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
name|accumulo
operator|.
name|predicate
package|;
end_package

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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|data
operator|.
name|Key
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|data
operator|.
name|Value
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|iterators
operator|.
name|IteratorEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|iterators
operator|.
name|SortedKeyValueIterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|accumulo
operator|.
name|core
operator|.
name|iterators
operator|.
name|user
operator|.
name|WholeRowIterator
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
name|codec
operator|.
name|binary
operator|.
name|Base64
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
name|accumulo
operator|.
name|columns
operator|.
name|ColumnEncoding
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
name|accumulo
operator|.
name|columns
operator|.
name|ColumnMappingFactory
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
name|accumulo
operator|.
name|columns
operator|.
name|HiveAccumuloColumnMapping
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
name|accumulo
operator|.
name|predicate
operator|.
name|compare
operator|.
name|CompareOp
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
name|accumulo
operator|.
name|predicate
operator|.
name|compare
operator|.
name|PrimitiveComparison
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

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

begin_comment
comment|/**  * Operates over a single qualifier.  *  * Delegates to PrimitiveCompare and CompareOpt instances for value acceptance.  *  * The PrimitiveCompare strategy assumes a consistent value type for the same column family and  * qualifier.  */
end_comment

begin_class
specifier|public
class|class
name|PrimitiveComparisonFilter
extends|extends
name|WholeRowIterator
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|PrimitiveComparisonFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILTER_PREFIX
init|=
literal|"accumulo.filter.compare.iterator."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|P_COMPARE_CLASS
init|=
literal|"accumulo.filter.iterator.p.compare.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COMPARE_OPT_CLASS
init|=
literal|"accumulo.filter.iterator.compare.opt.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONST_VAL
init|=
literal|"accumulo.filter.iterator.const.val"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN
init|=
literal|"accumulo.filter.iterator.qual"
decl_stmt|;
specifier|private
name|Text
name|cfHolder
decl_stmt|,
name|cqHolder
decl_stmt|,
name|columnMappingFamily
decl_stmt|,
name|columnMappingQualifier
decl_stmt|;
specifier|private
name|HiveAccumuloColumnMapping
name|columnMapping
decl_stmt|;
specifier|private
name|CompareOp
name|compOpt
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|boolean
name|filter
parameter_list|(
name|Text
name|currentRow
parameter_list|,
name|List
argument_list|<
name|Key
argument_list|>
name|keys
parameter_list|,
name|List
argument_list|<
name|Value
argument_list|>
name|values
parameter_list|)
block|{
name|SortedMap
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|items
decl_stmt|;
name|boolean
name|allow
decl_stmt|;
try|try
block|{
comment|// if key doesn't contain CF, it's an encoded value from a previous iterator.
while|while
condition|(
name|keys
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnFamily
argument_list|()
operator|.
name|getBytes
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|items
operator|=
name|decodeRow
argument_list|(
name|keys
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|keys
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|items
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|values
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|items
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|allow
operator|=
name|accept
argument_list|(
name|keys
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|allow
return|;
block|}
specifier|private
name|boolean
name|accept
parameter_list|(
name|Collection
argument_list|<
name|Key
argument_list|>
name|keys
parameter_list|,
name|Collection
argument_list|<
name|Value
argument_list|>
name|values
parameter_list|)
block|{
name|Iterator
argument_list|<
name|Key
argument_list|>
name|kIter
init|=
name|keys
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Value
argument_list|>
name|vIter
init|=
name|values
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|kIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Key
name|k
init|=
name|kIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|Value
name|v
init|=
name|vIter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|matchQualAndFam
argument_list|(
name|k
argument_list|)
condition|)
block|{
return|return
name|compOpt
operator|.
name|accept
argument_list|(
name|v
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|matchQualAndFam
parameter_list|(
name|Key
name|k
parameter_list|)
block|{
name|k
operator|.
name|getColumnFamily
argument_list|(
name|cfHolder
argument_list|)
expr_stmt|;
name|k
operator|.
name|getColumnQualifier
argument_list|(
name|cqHolder
argument_list|)
expr_stmt|;
return|return
name|cfHolder
operator|.
name|equals
argument_list|(
name|columnMappingFamily
argument_list|)
operator|&&
name|cqHolder
operator|.
name|equals
argument_list|(
name|columnMappingQualifier
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|SortedKeyValueIterator
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|source
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|options
parameter_list|,
name|IteratorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|init
argument_list|(
name|source
argument_list|,
name|options
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|String
name|serializedColumnMapping
init|=
name|options
operator|.
name|get
argument_list|(
name|COLUMN
argument_list|)
decl_stmt|;
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pair
init|=
name|ColumnMappingFactory
operator|.
name|parseMapping
argument_list|(
name|serializedColumnMapping
argument_list|)
decl_stmt|;
comment|// The ColumnEncoding, column name and type are all irrelevant at this point, just need the
comment|// cf:[cq]
name|columnMapping
operator|=
operator|new
name|HiveAccumuloColumnMapping
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|pair
operator|.
name|getValue
argument_list|()
argument_list|,
name|ColumnEncoding
operator|.
name|STRING
argument_list|,
literal|"column"
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|columnMappingFamily
operator|=
operator|new
name|Text
argument_list|(
name|columnMapping
operator|.
name|getColumnFamily
argument_list|()
argument_list|)
expr_stmt|;
name|columnMappingQualifier
operator|=
operator|new
name|Text
argument_list|(
name|columnMapping
operator|.
name|getColumnQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|cfHolder
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|cqHolder
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|pClass
init|=
name|Class
operator|.
name|forName
argument_list|(
name|options
operator|.
name|get
argument_list|(
name|P_COMPARE_CLASS
argument_list|)
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|cClazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|options
operator|.
name|get
argument_list|(
name|COMPARE_OPT_CLASS
argument_list|)
argument_list|)
decl_stmt|;
name|PrimitiveComparison
name|pCompare
init|=
name|pClass
operator|.
name|asSubclass
argument_list|(
name|PrimitiveComparison
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|compOpt
operator|=
name|cClazz
operator|.
name|asSubclass
argument_list|(
name|CompareOp
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|byte
index|[]
name|constant
init|=
name|getConstant
argument_list|(
name|options
argument_list|)
decl_stmt|;
name|pCompare
operator|.
name|init
argument_list|(
name|constant
argument_list|)
expr_stmt|;
name|compOpt
operator|.
name|setPrimitiveCompare
argument_list|(
name|pCompare
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|byte
index|[]
name|getConstant
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|options
parameter_list|)
block|{
name|String
name|b64Const
init|=
name|options
operator|.
name|get
argument_list|(
name|CONST_VAL
argument_list|)
decl_stmt|;
return|return
name|Base64
operator|.
name|decodeBase64
argument_list|(
name|b64Const
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit


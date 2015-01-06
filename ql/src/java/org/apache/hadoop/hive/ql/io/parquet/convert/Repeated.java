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
name|io
operator|.
name|parquet
operator|.
name|convert
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
name|io
operator|.
name|ArrayWritable
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
name|Writable
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|column
operator|.
name|Dictionary
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Binary
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|Converter
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|io
operator|.
name|api
operator|.
name|PrimitiveConverter
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|GroupType
import|;
end_import

begin_import
import|import
name|parquet
operator|.
name|schema
operator|.
name|PrimitiveType
import|;
end_import

begin_comment
comment|/**  * Converters for repeated fields need to know when the parent field starts and  * ends to correctly build lists from the repeated values.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Repeated
extends|extends
name|ConverterParent
block|{
specifier|public
name|void
name|parentStart
parameter_list|()
function_decl|;
specifier|public
name|void
name|parentEnd
parameter_list|()
function_decl|;
comment|/**    * Stands in for a PrimitiveConverter and accumulates multiple values as an    * ArrayWritable.    */
class|class
name|RepeatedPrimitiveConverter
extends|extends
name|PrimitiveConverter
implements|implements
name|Repeated
block|{
specifier|private
specifier|final
name|PrimitiveType
name|primitiveType
decl_stmt|;
specifier|private
specifier|final
name|PrimitiveConverter
name|wrapped
decl_stmt|;
specifier|private
specifier|final
name|ConverterParent
name|parent
decl_stmt|;
specifier|private
specifier|final
name|int
name|index
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Writable
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|RepeatedPrimitiveConverter
parameter_list|(
name|PrimitiveType
name|primitiveType
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|primitiveType
operator|=
name|primitiveType
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|wrapped
operator|=
name|HiveGroupConverter
operator|.
name|getConverterFromDescription
argument_list|(
name|primitiveType
argument_list|,
literal|0
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasDictionarySupport
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|hasDictionarySupport
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDictionary
parameter_list|(
name|Dictionary
name|dictionary
parameter_list|)
block|{
name|wrapped
operator|.
name|setDictionary
argument_list|(
name|dictionary
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addValueFromDictionary
parameter_list|(
name|int
name|dictionaryId
parameter_list|)
block|{
name|wrapped
operator|.
name|addValueFromDictionary
argument_list|(
name|dictionaryId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addBinary
parameter_list|(
name|Binary
name|value
parameter_list|)
block|{
name|wrapped
operator|.
name|addBinary
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addBoolean
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|wrapped
operator|.
name|addBoolean
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addDouble
parameter_list|(
name|double
name|value
parameter_list|)
block|{
name|wrapped
operator|.
name|addDouble
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addFloat
parameter_list|(
name|float
name|value
parameter_list|)
block|{
name|wrapped
operator|.
name|addFloat
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addInt
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|wrapped
operator|.
name|addInt
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addLong
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|wrapped
operator|.
name|addLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|parentStart
parameter_list|()
block|{
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|parentEnd
parameter_list|()
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|HiveGroupConverter
operator|.
name|wrapList
argument_list|(
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|Writable
index|[
name|list
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|,
name|Writable
name|value
parameter_list|)
block|{
name|list
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Stands in for a HiveGroupConverter and accumulates multiple values as an    * ArrayWritable.    */
class|class
name|RepeatedGroupConverter
extends|extends
name|HiveGroupConverter
implements|implements
name|Repeated
block|{
specifier|private
specifier|final
name|GroupType
name|groupType
decl_stmt|;
specifier|private
specifier|final
name|HiveGroupConverter
name|wrapped
decl_stmt|;
specifier|private
specifier|final
name|ConverterParent
name|parent
decl_stmt|;
specifier|private
specifier|final
name|int
name|index
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Writable
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<
name|Writable
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|RepeatedGroupConverter
parameter_list|(
name|GroupType
name|groupType
parameter_list|,
name|ConverterParent
name|parent
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|groupType
operator|=
name|groupType
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|wrapped
operator|=
name|HiveGroupConverter
operator|.
name|getConverterFromDescription
argument_list|(
name|groupType
argument_list|,
literal|0
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|int
name|fieldIndex
parameter_list|,
name|Writable
name|value
parameter_list|)
block|{
name|list
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Converter
name|getConverter
parameter_list|(
name|int
name|fieldIndex
parameter_list|)
block|{
comment|// delegate to the group's converters
return|return
name|wrapped
operator|.
name|getConverter
argument_list|(
name|fieldIndex
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|wrapped
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|end
parameter_list|()
block|{
name|wrapped
operator|.
name|end
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|parentStart
parameter_list|()
block|{
name|list
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|parentEnd
parameter_list|()
block|{
name|parent
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|wrapList
argument_list|(
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|Writable
index|[
name|list
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_interface

end_unit


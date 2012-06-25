begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.7.0)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  */
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
name|serde
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|ArrayList
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumMap
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
name|EnumSet
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
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

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
name|Constants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_LIB
init|=
literal|"serialization.lib"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_CLASS
init|=
literal|"serialization.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_FORMAT
init|=
literal|"serialization.format"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_DDL
init|=
literal|"serialization.ddl"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_NULL_FORMAT
init|=
literal|"serialization.null.format"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
init|=
literal|"serialization.last.column.takes.rest"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_SORT_ORDER
init|=
literal|"serialization.sort.order"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_USE_JSON_OBJECTS
init|=
literal|"serialization.use.json.object"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FIELD_DELIM
init|=
literal|"field.delim"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COLLECTION_DELIM
init|=
literal|"colelction.delim"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LINE_DELIM
init|=
literal|"line.delim"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAPKEY_DELIM
init|=
literal|"mapkey.delim"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|QUOTE_CHAR
init|=
literal|"quote.delim"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ESCAPE_CHAR
init|=
literal|"escape.delim"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|VOID_TYPE_NAME
init|=
literal|"void"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BOOLEAN_TYPE_NAME
init|=
literal|"boolean"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TINYINT_TYPE_NAME
init|=
literal|"tinyint"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SMALLINT_TYPE_NAME
init|=
literal|"smallint"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INT_TYPE_NAME
init|=
literal|"int"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BIGINT_TYPE_NAME
init|=
literal|"bigint"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FLOAT_TYPE_NAME
init|=
literal|"float"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DOUBLE_TYPE_NAME
init|=
literal|"double"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STRING_TYPE_NAME
init|=
literal|"string"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATE_TYPE_NAME
init|=
literal|"date"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATETIME_TYPE_NAME
init|=
literal|"datetime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TIMESTAMP_TYPE_NAME
init|=
literal|"timestamp"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BINARY_TYPE_NAME
init|=
literal|"binary"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_TYPE_NAME
init|=
literal|"array"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAP_TYPE_NAME
init|=
literal|"map"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STRUCT_TYPE_NAME
init|=
literal|"struct"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNION_TYPE_NAME
init|=
literal|"uniontype"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_COLUMNS
init|=
literal|"columns"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_COLUMN_TYPES
init|=
literal|"columns.types"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|PrimitiveTypes
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"void"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"boolean"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"tinyint"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"smallint"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"int"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"bigint"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"float"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"double"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"string"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"date"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"datetime"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"timestamp"
argument_list|)
expr_stmt|;
name|PrimitiveTypes
operator|.
name|add
argument_list|(
literal|"binary"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|CollectionTypes
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|CollectionTypes
operator|.
name|add
argument_list|(
literal|"array"
argument_list|)
expr_stmt|;
name|CollectionTypes
operator|.
name|add
argument_list|(
literal|"map"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit


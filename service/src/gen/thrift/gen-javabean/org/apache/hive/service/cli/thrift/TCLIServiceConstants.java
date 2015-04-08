begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift Compiler (0.9.0)  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  *  @generated  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|IScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|SchemeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|StandardScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|scheme
operator|.
name|TupleScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TTupleProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|EncodingUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
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
name|TCLIServiceConstants
block|{
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|TTypeId
argument_list|>
name|PRIMITIVE_TYPES
init|=
operator|new
name|HashSet
argument_list|<
name|TTypeId
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|BOOLEAN_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|TINYINT_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|SMALLINT_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|INT_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|BIGINT_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|FLOAT_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|DOUBLE_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|TIMESTAMP_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|BINARY_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|DECIMAL_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|NULL_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|DATE_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|VARCHAR_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|CHAR_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|INTERVAL_YEAR_MONTH_TYPE
argument_list|)
expr_stmt|;
name|PRIMITIVE_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|INTERVAL_DAY_TIME_TYPE
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|TTypeId
argument_list|>
name|COMPLEX_TYPES
init|=
operator|new
name|HashSet
argument_list|<
name|TTypeId
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|COMPLEX_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|ARRAY_TYPE
argument_list|)
expr_stmt|;
name|COMPLEX_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|MAP_TYPE
argument_list|)
expr_stmt|;
name|COMPLEX_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|STRUCT_TYPE
argument_list|)
expr_stmt|;
name|COMPLEX_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|UNION_TYPE
argument_list|)
expr_stmt|;
name|COMPLEX_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|USER_DEFINED_TYPE
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|TTypeId
argument_list|>
name|COLLECTION_TYPES
init|=
operator|new
name|HashSet
argument_list|<
name|TTypeId
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|COLLECTION_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|ARRAY_TYPE
argument_list|)
expr_stmt|;
name|COLLECTION_TYPES
operator|.
name|add
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|MAP_TYPE
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|TTypeId
argument_list|,
name|String
argument_list|>
name|TYPE_NAMES
init|=
operator|new
name|HashMap
argument_list|<
name|TTypeId
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|BOOLEAN_TYPE
argument_list|,
literal|"BOOLEAN"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|TINYINT_TYPE
argument_list|,
literal|"TINYINT"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|SMALLINT_TYPE
argument_list|,
literal|"SMALLINT"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|INT_TYPE
argument_list|,
literal|"INT"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|BIGINT_TYPE
argument_list|,
literal|"BIGINT"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|FLOAT_TYPE
argument_list|,
literal|"FLOAT"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|DOUBLE_TYPE
argument_list|,
literal|"DOUBLE"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|,
literal|"STRING"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|TIMESTAMP_TYPE
argument_list|,
literal|"TIMESTAMP"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|BINARY_TYPE
argument_list|,
literal|"BINARY"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|ARRAY_TYPE
argument_list|,
literal|"ARRAY"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|MAP_TYPE
argument_list|,
literal|"MAP"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|STRUCT_TYPE
argument_list|,
literal|"STRUCT"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|UNION_TYPE
argument_list|,
literal|"UNIONTYPE"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|DECIMAL_TYPE
argument_list|,
literal|"DECIMAL"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|NULL_TYPE
argument_list|,
literal|"NULL"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|DATE_TYPE
argument_list|,
literal|"DATE"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|VARCHAR_TYPE
argument_list|,
literal|"VARCHAR"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|CHAR_TYPE
argument_list|,
literal|"CHAR"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|INTERVAL_YEAR_MONTH_TYPE
argument_list|,
literal|"INTERVAL_YEAR_MONTH"
argument_list|)
expr_stmt|;
name|TYPE_NAMES
operator|.
name|put
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeId
operator|.
name|INTERVAL_DAY_TIME_TYPE
argument_list|,
literal|"INTERVAL_DAY_TIME"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|String
name|CHARACTER_MAXIMUM_LENGTH
init|=
literal|"characterMaximumLength"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PRECISION
init|=
literal|"precision"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SCALE
init|=
literal|"scale"
decl_stmt|;
block|}
end_class

end_unit


begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Thrift  *  * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING  */
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
operator|.
name|api
package|;
end_package

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
name|Collections
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
name|IntRangeSet
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

begin_class
specifier|public
class|class
name|OperatorType
block|{
specifier|public
specifier|static
specifier|final
name|int
name|JOIN
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAPJOIN
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|EXTRACT
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FILTER
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FORWARD
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|GROUPBY
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LIMIT
init|=
literal|6
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SCRIPT
init|=
literal|7
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SELECT
init|=
literal|8
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|TABLESCAN
init|=
literal|9
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FILESINK
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|REDUCESINK
init|=
literal|11
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|UNION
init|=
literal|12
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|UDTF
init|=
literal|13
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|IntRangeSet
name|VALID_VALUES
init|=
operator|new
name|IntRangeSet
argument_list|(
name|JOIN
argument_list|,
name|MAPJOIN
argument_list|,
name|EXTRACT
argument_list|,
name|FILTER
argument_list|,
name|FORWARD
argument_list|,
name|GROUPBY
argument_list|,
name|LIMIT
argument_list|,
name|SCRIPT
argument_list|,
name|SELECT
argument_list|,
name|TABLESCAN
argument_list|,
name|FILESINK
argument_list|,
name|REDUCESINK
argument_list|,
name|UNION
argument_list|,
name|UDTF
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|VALUES_TO_NAMES
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|JOIN
argument_list|,
literal|"JOIN"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|MAPJOIN
argument_list|,
literal|"MAPJOIN"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|EXTRACT
argument_list|,
literal|"EXTRACT"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|FILTER
argument_list|,
literal|"FILTER"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|FORWARD
argument_list|,
literal|"FORWARD"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|GROUPBY
argument_list|,
literal|"GROUPBY"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|LIMIT
argument_list|,
literal|"LIMIT"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|SCRIPT
argument_list|,
literal|"SCRIPT"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|SELECT
argument_list|,
literal|"SELECT"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|TABLESCAN
argument_list|,
literal|"TABLESCAN"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|FILESINK
argument_list|,
literal|"FILESINK"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|REDUCESINK
argument_list|,
literal|"REDUCESINK"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|UNION
argument_list|,
literal|"UNION"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|UDTF
argument_list|,
literal|"UDTF"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit


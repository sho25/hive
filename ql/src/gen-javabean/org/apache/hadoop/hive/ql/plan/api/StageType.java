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
name|StageType
block|{
specifier|public
specifier|static
specifier|final
name|int
name|CONDITIONAL
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|COPY
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DDL
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAPRED
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|EXPLAIN
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FETCH
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FUNC
init|=
literal|6
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAPREDLOCAL
init|=
literal|7
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MOVE
init|=
literal|8
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
name|CONDITIONAL
argument_list|,
name|COPY
argument_list|,
name|DDL
argument_list|,
name|MAPRED
argument_list|,
name|EXPLAIN
argument_list|,
name|FETCH
argument_list|,
name|FUNC
argument_list|,
name|MAPREDLOCAL
argument_list|,
name|MOVE
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
name|CONDITIONAL
argument_list|,
literal|"CONDITIONAL"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|COPY
argument_list|,
literal|"COPY"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|DDL
argument_list|,
literal|"DDL"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|MAPRED
argument_list|,
literal|"MAPRED"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|EXPLAIN
argument_list|,
literal|"EXPLAIN"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|FETCH
argument_list|,
literal|"FETCH"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|FUNC
argument_list|,
literal|"FUNC"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|MAPREDLOCAL
argument_list|,
literal|"MAPREDLOCAL"
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|MOVE
argument_list|,
literal|"MOVE"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit


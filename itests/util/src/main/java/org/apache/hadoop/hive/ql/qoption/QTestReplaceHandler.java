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
name|qoption
package|;
end_package

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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|QTestUtil
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

begin_comment
comment|/**  * QTest replacement directive handler  *  * Examples:  *  * --! qt:replace:/there/joe/  * select 'hello there!  * ===q.out  * hello joe!  *  * standard java regex; placeholders also work:  * --! qt:replace:/Hello (.*)!/$1 was here!/  *  * first char of regex pattern is used as separator; you may choose anything else than '/'  * --! qt:replace:#this#that#  */
end_comment

begin_class
specifier|public
class|class
name|QTestReplaceHandler
implements|implements
name|QTestOptionHandler
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|QTestReplaceHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|replacements
init|=
operator|new
name|HashMap
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|processArguments
parameter_list|(
name|String
name|arguments
parameter_list|)
block|{
name|arguments
operator|=
name|arguments
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
argument_list|()
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"illegal replacement expr: "
operator|+
name|arguments
operator|+
literal|" ; expected something like /this/that/"
argument_list|)
throw|;
block|}
name|String
name|sep
init|=
name|arguments
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|arguments
operator|.
name|split
argument_list|(
name|sep
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unexpected replacement expr: "
operator|+
name|arguments
operator|+
literal|" ; expected something like /this/that/"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Enabling replacement of: {} => {}"
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|replacements
operator|.
name|put
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeTest
parameter_list|(
name|QTestUtil
name|qt
parameter_list|)
throws|throws
name|Exception
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|afterTest
parameter_list|(
name|QTestUtil
name|qt
parameter_list|)
throws|throws
name|Exception
block|{
name|replacements
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|processLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|Pattern
argument_list|,
name|String
argument_list|>
name|r
range|:
name|replacements
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Matcher
name|m
init|=
name|r
operator|.
name|getKey
argument_list|()
operator|.
name|matcher
argument_list|(
name|line
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
name|line
operator|=
name|m
operator|.
name|replaceAll
argument_list|(
name|r
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|line
return|;
block|}
block|}
end_class

end_unit


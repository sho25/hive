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
name|common
operator|.
name|log
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
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

begin_import
import|import
name|jline
operator|.
name|TerminalFactory
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
name|lang
operator|.
name|StringUtils
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
name|fusesource
operator|.
name|jansi
operator|.
name|Ansi
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DecimalFormat
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
import|import static
name|org
operator|.
name|fusesource
operator|.
name|jansi
operator|.
name|Ansi
operator|.
name|ansi
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|fusesource
operator|.
name|jansi
operator|.
name|internal
operator|.
name|CLibrary
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Renders information from ProgressMonitor to the stream provided.  */
end_comment

begin_class
specifier|public
class|class
name|InPlaceUpdate
block|{
specifier|public
specifier|static
specifier|final
name|int
name|MIN_TERMINAL_WIDTH
init|=
literal|94
decl_stmt|;
comment|// keep this within 80 chars width. If more columns needs to be added then update min terminal
comment|// width requirement and SEPARATOR width accordingly
specifier|private
specifier|static
specifier|final
name|String
name|HEADER_FORMAT
init|=
literal|"%16s%10s %13s  %5s  %9s  %7s  %7s  %6s  %6s  "
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VERTEX_FORMAT
init|=
literal|"%-16s%10s %13s  %5s  %9s  %7s  %7s  %6s  %6s  "
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FOOTER_FORMAT
init|=
literal|"%-15s  %-30s %-4s  %-25s"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|PROGRESS_BAR_CHARS
init|=
literal|30
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SEPARATOR
init|=
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
name|MIN_TERMINAL_WIDTH
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"-"
argument_list|)
decl_stmt|;
comment|/* Pretty print the values */
specifier|private
specifier|final
name|DecimalFormat
name|secondsFormatter
init|=
operator|new
name|DecimalFormat
argument_list|(
literal|"#0.00"
argument_list|)
decl_stmt|;
specifier|private
name|int
name|lines
init|=
literal|0
decl_stmt|;
specifier|private
name|PrintStream
name|out
decl_stmt|;
specifier|public
name|InPlaceUpdate
parameter_list|(
name|PrintStream
name|out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
block|}
specifier|public
name|InPlaceUpdate
parameter_list|()
block|{
name|this
argument_list|(
name|System
operator|.
name|out
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|reprintLine
parameter_list|(
name|PrintStream
name|out
parameter_list|,
name|String
name|line
parameter_list|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|eraseLine
argument_list|(
name|Ansi
operator|.
name|Erase
operator|.
name|ALL
argument_list|)
operator|.
name|a
argument_list|(
name|line
argument_list|)
operator|.
name|a
argument_list|(
literal|'\n'
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|rePositionCursor
parameter_list|(
name|PrintStream
name|ps
parameter_list|)
block|{
name|ps
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|cursorUp
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ps
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
comment|/**    * NOTE: Use this method only if isUnixTerminal is true.    * Erases the current line and prints the given line.    *    * @param line - line to print    */
specifier|private
name|void
name|reprintLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|reprintLine
argument_list|(
name|out
argument_list|,
name|line
argument_list|)
expr_stmt|;
name|lines
operator|++
expr_stmt|;
block|}
comment|/**    * NOTE: Use this method only if isUnixTerminal is true.    * Erases the current line and prints the given line with the specified color.    *    * @param line  - line to print    * @param color - color for the line    */
specifier|private
name|void
name|reprintLineWithColorAsBold
parameter_list|(
name|String
name|line
parameter_list|,
name|Ansi
operator|.
name|Color
name|color
parameter_list|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|eraseLine
argument_list|(
name|Ansi
operator|.
name|Erase
operator|.
name|ALL
argument_list|)
operator|.
name|fg
argument_list|(
name|color
argument_list|)
operator|.
name|bold
argument_list|()
operator|.
name|a
argument_list|(
name|line
argument_list|)
operator|.
name|a
argument_list|(
literal|'\n'
argument_list|)
operator|.
name|boldOff
argument_list|()
operator|.
name|reset
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|lines
operator|++
expr_stmt|;
block|}
comment|/**    * NOTE: Use this method only if isUnixTerminal is true.    * Erases the current line and prints the given multiline. Make sure the specified line is not    * terminated by linebreak.    *    * @param line - line to print    */
specifier|private
name|void
name|reprintMultiLine
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|int
name|numLines
init|=
name|line
operator|.
name|split
argument_list|(
literal|"\r\n|\r|\n"
argument_list|)
operator|.
name|length
decl_stmt|;
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|eraseLine
argument_list|(
name|Ansi
operator|.
name|Erase
operator|.
name|ALL
argument_list|)
operator|.
name|a
argument_list|(
name|line
argument_list|)
operator|.
name|a
argument_list|(
literal|'\n'
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|lines
operator|+=
name|numLines
expr_stmt|;
block|}
comment|/**    * NOTE: Use this method only if isUnixTerminal is true.    * Repositions the cursor back to line 0.    */
specifier|private
name|void
name|repositionCursor
parameter_list|()
block|{
if|if
condition|(
name|lines
operator|>
literal|0
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ansi
argument_list|()
operator|.
name|cursorUp
argument_list|(
name|lines
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|lines
operator|=
literal|0
expr_stmt|;
block|}
block|}
comment|// [==================>>-----]
specifier|private
name|String
name|getInPlaceProgressBar
parameter_list|(
name|double
name|percent
parameter_list|)
block|{
name|StringWriter
name|bar
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|bar
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
expr_stmt|;
name|int
name|remainingChars
init|=
name|PROGRESS_BAR_CHARS
operator|-
literal|4
decl_stmt|;
name|int
name|completed
init|=
call|(
name|int
call|)
argument_list|(
name|remainingChars
operator|*
name|percent
argument_list|)
decl_stmt|;
name|int
name|pending
init|=
name|remainingChars
operator|-
name|completed
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|completed
condition|;
name|i
operator|++
control|)
block|{
name|bar
operator|.
name|append
argument_list|(
literal|"="
argument_list|)
expr_stmt|;
block|}
name|bar
operator|.
name|append
argument_list|(
literal|">>"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|pending
condition|;
name|i
operator|++
control|)
block|{
name|bar
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
block|}
name|bar
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
return|return
name|bar
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|render
parameter_list|(
name|ProgressMonitor
name|monitor
parameter_list|)
block|{
if|if
condition|(
name|monitor
operator|==
literal|null
condition|)
return|return;
comment|// position the cursor to line 0
name|repositionCursor
argument_list|()
expr_stmt|;
comment|// print header
comment|// -------------------------------------------------------------------------------
comment|//         VERTICES     STATUS  TOTAL  COMPLETED  RUNNING  PENDING  FAILED  KILLED
comment|// -------------------------------------------------------------------------------
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|reprintLineWithColorAsBold
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|HEADER_FORMAT
argument_list|,
name|monitor
operator|.
name|headers
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
argument_list|,
name|Ansi
operator|.
name|Color
operator|.
name|CYAN
argument_list|)
expr_stmt|;
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
comment|// Map 1 .......... container  SUCCEEDED      7          7        0        0       0       0
name|List
argument_list|<
name|String
argument_list|>
name|printReady
init|=
name|Lists
operator|.
name|transform
argument_list|(
name|monitor
operator|.
name|rows
argument_list|()
argument_list|,
operator|new
name|Function
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|apply
parameter_list|(
annotation|@
name|Nullable
name|List
argument_list|<
name|String
argument_list|>
name|row
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|VERTEX_FORMAT
argument_list|,
name|row
operator|.
name|toArray
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|reprintMultiLine
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|printReady
argument_list|,
literal|"\n"
argument_list|)
argument_list|)
expr_stmt|;
comment|// -------------------------------------------------------------------------------
comment|// VERTICES: 03/04            [=================>>-----] 86%  ELAPSED TIME: 1.71 s
comment|// -------------------------------------------------------------------------------
name|String
name|progressStr
init|=
literal|""
operator|+
call|(
name|int
call|)
argument_list|(
name|monitor
operator|.
name|progressedPercentage
argument_list|()
operator|*
literal|100
argument_list|)
operator|+
literal|"%"
decl_stmt|;
name|float
name|et
init|=
call|(
name|float
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|monitor
operator|.
name|startTime
argument_list|()
argument_list|)
operator|/
operator|(
name|float
operator|)
literal|1000
decl_stmt|;
name|String
name|elapsedTime
init|=
literal|"ELAPSED TIME: "
operator|+
name|secondsFormatter
operator|.
name|format
argument_list|(
name|et
argument_list|)
operator|+
literal|" s"
decl_stmt|;
name|String
name|footer
init|=
name|String
operator|.
name|format
argument_list|(
name|FOOTER_FORMAT
argument_list|,
name|monitor
operator|.
name|footerSummary
argument_list|()
argument_list|,
name|getInPlaceProgressBar
argument_list|(
name|monitor
operator|.
name|progressedPercentage
argument_list|()
argument_list|)
argument_list|,
name|progressStr
argument_list|,
name|elapsedTime
argument_list|)
decl_stmt|;
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
name|reprintLineWithColorAsBold
argument_list|(
name|footer
argument_list|,
name|Ansi
operator|.
name|Color
operator|.
name|RED
argument_list|)
expr_stmt|;
name|reprintLine
argument_list|(
name|SEPARATOR
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|canRenderInPlace
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|boolean
name|inPlaceUpdates
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|TEZ_EXEC_INPLACE_PROGRESS
argument_list|)
decl_stmt|;
comment|// we need at least 80 chars wide terminal to display in-place updates properly
return|return
name|inPlaceUpdates
operator|&&
name|isUnixTerminal
argument_list|()
operator|&&
name|TerminalFactory
operator|.
name|get
argument_list|()
operator|.
name|getWidth
argument_list|()
operator|>=
name|MIN_TERMINAL_WIDTH
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isUnixTerminal
parameter_list|()
block|{
name|String
name|os
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.name"
argument_list|)
decl_stmt|;
if|if
condition|(
name|os
operator|.
name|startsWith
argument_list|(
literal|"Windows"
argument_list|)
condition|)
block|{
comment|// we do not support Windows, we will revisit this if we really need it for windows.
return|return
literal|false
return|;
block|}
comment|// We must be on some unix variant..
comment|// check if standard out is a terminal
try|try
block|{
comment|// isatty system call will return 1 if the file descriptor is terminal else 0
if|if
condition|(
name|isatty
argument_list|(
name|STDOUT_FILENO
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|isatty
argument_list|(
name|STDERR_FILENO
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
catch|catch
parameter_list|(
name|NoClassDefFoundError
decl||
name|UnsatisfiedLinkError
name|ignore
parameter_list|)
block|{
comment|// These errors happen if the JNI lib is not available for your platform.
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit


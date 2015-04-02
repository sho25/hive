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
name|exec
package|;
end_package

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
name|Set
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
operator|.
name|ConfVars
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
name|BaseWork
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|LlapIoProxy
import|;
end_import

begin_class
specifier|public
class|class
name|GlobalWorkMapFactory
block|{
specifier|private
name|ThreadLocal
argument_list|<
name|Map
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
argument_list|>
name|threadLocalWorkMap
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
name|gWorkMap
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
class|class
name|DummyMap
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|containsKey
parameter_list|(
specifier|final
name|Object
name|key
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|containsValue
parameter_list|(
specifier|final
name|Object
name|value
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|get
parameter_list|(
specifier|final
name|Object
name|key
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|K
argument_list|>
name|keySet
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|put
parameter_list|(
specifier|final
name|K
name|key
parameter_list|,
specifier|final
name|V
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|putAll
parameter_list|(
specifier|final
name|Map
argument_list|<
name|?
extends|extends
name|K
argument_list|,
name|?
extends|extends
name|V
argument_list|>
name|t
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|V
name|remove
parameter_list|(
specifier|final
name|Object
name|key
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|V
argument_list|>
name|values
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
name|DummyMap
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
name|dummy
init|=
operator|new
name|DummyMap
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|Map
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
name|get
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|LlapIoProxy
operator|.
name|isDaemon
argument_list|()
operator|||
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
argument_list|)
operator|.
name|equals
argument_list|(
literal|"spark"
argument_list|)
condition|)
block|{
if|if
condition|(
name|threadLocalWorkMap
operator|==
literal|null
condition|)
block|{
name|threadLocalWorkMap
operator|=
operator|new
name|ThreadLocal
argument_list|<
name|Map
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Map
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|HashMap
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
return|;
block|}
block|}
expr_stmt|;
block|}
return|return
name|threadLocalWorkMap
operator|.
name|get
argument_list|()
return|;
block|}
if|if
condition|(
name|gWorkMap
operator|==
literal|null
condition|)
block|{
name|gWorkMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Path
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
block|}
return|return
name|gWorkMap
return|;
block|}
block|}
end_class

end_unit


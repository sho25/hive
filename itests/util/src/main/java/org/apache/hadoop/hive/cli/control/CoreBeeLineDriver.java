begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|cli
operator|.
name|control
package|;
end_package

begin_comment
comment|//beeline is excluded by default
end_comment

begin_comment
comment|//AFAIK contains broken tests
end_comment

begin_comment
comment|//and produces compile errors...i'll comment out this whole class for now...
end_comment

begin_comment
comment|/*  import static org.junit.Assert.fail; import static org.apache.hadoop.hive.conf.HiveConf.ConfVars.*;  import org.apache.hadoop.hive.conf.HiveConf; import org.apache.hadoop.hive.ql.QTestUtil; import org.apache.hive.beeline.util.QFileClient; import org.apache.hive.service.server.HiveServer2; import org.junit.AfterClass; import org.junit.BeforeClass; // HIVE-14444: i've dropped this: @RunWith(ConcurrentTestRunner.class) public class CoreBeeLineDriver extends CliAdapter {   private final String hiveRootDirectory = AbstractCliConfig.HIVE_ROOT;   private final String queryDirectory;   private final String logDirectory;   private final String resultsDirectory;   private boolean overwrite = false;   private static String scratchDirectory;   private static QTestUtil.QTestSetup miniZKCluster = null;    private static HiveServer2 hiveServer2;    public CoreBeeLineDriver(AbstractCliConfig testCliConfig) {     super(testCliConfig);     queryDirectory = testCliConfig.getQueryDirectory();     logDirectory = testCliConfig.getLogDir();     resultsDirectory = testCliConfig.getResultsDir();   }    @Override   @BeforeClass   public void beforeClass() throws Exception {     HiveConf hiveConf = new HiveConf();     hiveConf.logVars(System.err);     System.err.flush();      scratchDirectory = hiveConf.getVar(SCRATCHDIR);      String testOutputOverwrite = System.getProperty("test.output.overwrite");     if (testOutputOverwrite != null&& "true".equalsIgnoreCase(testOutputOverwrite)) {       overwrite = true;     }      miniZKCluster = new QTestUtil.QTestSetup();     miniZKCluster.preTest(hiveConf);      System.setProperty("hive.zookeeper.quorum",         hiveConf.get("hive.zookeeper.quorum"));     System.setProperty("hive.zookeeper.client.port",         hiveConf.get("hive.zookeeper.client.port"));      String disableserver = System.getProperty("test.service.disable.server");     if (null != disableserver&& disableserver.equalsIgnoreCase("true")) {       System.err.println("test.service.disable.server=true "         + "Skipping HiveServer2 initialization!");       return;     }      hiveServer2 = new HiveServer2();     hiveServer2.init(hiveConf);     System.err.println("Starting HiveServer2...");     hiveServer2.start();     Thread.sleep(5000);   }     @Override   @AfterClass   public void shutdown() {     try {       if (hiveServer2 != null) {         System.err.println("Stopping HiveServer2...");         hiveServer2.stop();       }     } catch (Throwable t) {       t.printStackTrace();     }      if (miniZKCluster != null) {       try {         miniZKCluster.tearDown();       } catch (Exception e) {         e.printStackTrace();       }     }   }    public void runTest(String qFileName) throws Exception {     QFileClient qClient = new QFileClient(new HiveConf(), hiveRootDirectory,         queryDirectory, logDirectory, resultsDirectory)     .setQFileName(qFileName)     .setUsername("user")     .setPassword("password")     .setJdbcUrl("jdbc:hive2://localhost:10000")     .setJdbcDriver("org.apache.hive.jdbc.HiveDriver")     .setTestDataDirectory(hiveRootDirectory + "/data/files")     .setTestScriptDirectory(hiveRootDirectory + "/data/scripts");      long startTime = System.currentTimeMillis();     System.err.println(">>> STARTED " + qFileName         + " (Thread " + Thread.currentThread().getName() + ")");     try {       qClient.run();     } catch (Exception e) {       System.err.println(">>> FAILED " + qFileName + " with exception:");       e.printStackTrace();       throw e;     }     long elapsedTime = (System.currentTimeMillis() - startTime)/1000;     String time = "(" + elapsedTime + "s)";      if (qClient.compareResults()) {       System.err.println(">>> PASSED " + qFileName + " " + time);     } else {       if (qClient.hasErrors()) {         System.err.println(">>> FAILED " + qFileName + " (ERROR) " + time);         fail();       }       if (overwrite) {         System.err.println(">>> PASSED " + qFileName + " (OVERWRITE) " + time);         qClient.overwriteResults();       } else {         System.err.println(">>> FAILED " + qFileName + " (DIFF) " + time);         fail();       }     }   }    @Override   public void setUp() {     // TODO Auto-generated method stub    }    @Override   public void tearDown() {     // TODO Auto-generated method stub    }    @Override   public void runTest(String name, String name2, String absolutePath) throws Exception {     runTest(name2);   }  }   */
end_comment

end_unit


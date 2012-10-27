package us.yuxin.ingest.cassandra;


import java.io.IOException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.lib.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CIngest extends Configured implements Tool {
  public final static String CONF_CASSANDRA_CONNECT_TOKEN = "ingest.cassandra.token";
  public final static String CONF_CASSANDRA_JAR_PATH = "ingest.cassandra.jar.path";
  public final static String CONF_INGEST_STORE_ATTR = "ingest.store.attr";


  protected void prepareClassPath(Configuration conf) throws IOException {

    FileSystem fs = FileSystem.get(conf);

    FileStatus[] fileStatuses = fs.listStatus(
      new Path(conf.get(CONF_CASSANDRA_JAR_PATH, "/is/app/ingest/cassandra/lib")));

    for (FileStatus fileStatus : fileStatuses) {
      if (fileStatus.getPath().toString().endsWith(".jar")) {
        DistributedCache.addArchiveToClassPath(fileStatus.getPath(), conf, fs);
      }
    }
    fs.close();
  }


  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    prepareClassPath(conf);

    JobConf job = new JobConf(conf);

    job.setJobName(String.format("ingest-cassandra-%d", System.currentTimeMillis()));
    job.setInputFormat(TextInputFormat.class);
    job.setOutputFormat(NullOutputFormat.class);

    job.setJarByClass(CIngestMapper.class);
    job.setMapperClass(CIngestMapper.class);

    job.setNumReduceTasks(0);
    FileInputFormat.setInputPaths(job, new Path(args[0]));
    JobClient.runJob(job);
    return 0;
  }


  protected static Configuration prepareConfiguration() {
    Configuration conf = new Configuration();

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      classLoader = CIngest.class.getClassLoader();
    }

    URL defaultURL = classLoader.getResource("ingest-default.xml");
    if (defaultURL != null)
      conf.addResource(defaultURL);

    URL siteURL = classLoader.getResource("ingest-site.xml");
    if (siteURL != null)
      conf.addResource(siteURL);
    return conf;
  }


  public static void main(String[] args) throws Exception {
    Configuration conf = prepareConfiguration();
    int res = ToolRunner.run(conf, new CIngest(), args);
    System.exit(res);
  }
}

package us.yuxin.examples.hbase;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import com.google.common.base.Splitter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

public class HBaseScanner {
  public static void main(String [] args) throws IOException {

    String connectToken = args[0];

    Iterator<String> tokens = Splitter.on("///").split(connectToken).iterator();

    String zooKeepers = tokens.next();
    String tableName = tokens.next();

    Configuration hbase = HBaseConfiguration.create();

    if (zooKeepers.contains(":")) {
      int off = zooKeepers.indexOf(":");
      hbase.set("hbase.zookeeper.quorum", zooKeepers.substring(0, off));
      hbase.set("hbase.zookeeper.property.clientPort", zooKeepers.substring(off + 1));
    } else {
      hbase.set("hbase.zookeeper.quorum", zooKeepers);
    }


    HTable hTable = new HTable(hbase, tableName);

    Scan scan = new Scan();
    scan.setBatch(20);
    scan.setCaching(1000);
    scan.setMaxVersions(1);
    scan.setStartRow(args[1].getBytes());
    scan.setStopRow(args[2].getBytes());
    scan.addColumn("mp".getBytes(), new byte[0]);

    ResultScanner rs = hTable.getScanner(scan);
    int c = 0;
    for (Result r: rs) {
      c += 1;
      if (c % 5000 == 0) {
        System.out.println("" + c + " Reached... " + new Date().toString());
      }
    }

    rs.close();
    hTable.close();
  }
}

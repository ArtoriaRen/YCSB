/**
 * Copyright (c) 2012 - 2015 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

/*
 * PbftDb client binding for YCSB.
 *
 * Submitted by Davina Ren on 5/11/2010.
 *
 */
package site.ycsb.db;

//import site.ycsb.ByteArrayByteIterator;
import site.ycsb.ByteIterator;
import site.ycsb.DB;
import site.ycsb.DBException;
import site.ycsb.Status;

//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * MongoDB binding for YCSB framework using the MongoDB Inc. <a
 * href="http://docs.mongodb.org/ecosystem/drivers/java/">driver</a>
 * <p>
 * See the <code>README.md</code> for configuration information.
 * </p>
 * 
 * @author ypai
 * @see <a href="http://docs.mongodb.org/ecosystem/drivers/java/">MongoDB Inc.
 *    driver</a>
*/
public class PbftDbClient extends DB {
  /** Count the number of times initialized to teardown on the last. */
  private static final AtomicInteger INIT_COUNT = new AtomicInteger(0);

  // Create a UDP socket to send requests
  private DatagramSocket sender; 
  private InetAddress ip;
  private byte[] sendBuf;

  // Create a socket to receive results 
  private DatagramSocket receiver;
  private byte[] recvBuf;
  private int printCnt;

  /**
   * Initialize any state for this DB. Called once per DB instance; there is one
   * DB instance per client thread.
   */
  @Override
  public void init() throws DBException {
    INIT_COUNT.incrementAndGet();
    synchronized (PbftDbClient.class) {
      if (receiver != null) {
        return;
      }
      try {
        sender = new DatagramSocket(); 
        ip = InetAddress.getByName("127.0.0.1");
        String test = "r w,23,m";
        sendBuf = test.getBytes();
        DatagramPacket pkt2Send = new DatagramPacket(sendBuf, sendBuf.length, ip, 8350);
        sender.send(pkt2Send);

        receiver = new DatagramSocket(12345);
        recvBuf = new byte[65535];
        printCnt = 3;
      } catch (SocketException e) {
        System.err.println("Error in opening sockets: " + e);
      } catch (UnknownHostException e) {
        System.err.println("Error in getting host ip: " + e);
      } catch (IOException e) {
        System.err.println("Error in sending packets: " + e);
      }

    }
  }

  /**
   * Cleanup any state for this DB. Called once per DB instance; there is one DB
   * instance per client thread.
   */
  @Override
  public void cleanup() throws DBException {
    if (INIT_COUNT.decrementAndGet() == 0) {
      sender.close();  
      receiver.close();
    }
  }

  /**
   * Delete a record from the database.
   * 
   * @param table
   *      The name of the table
   * @param key
   *      The record key of the record to delete.
   * @return Zero on success, a non-zero error code on error. See the {@link DB}
   *     class's description for a discussion of error codes.
   */
  @Override
  public Status delete(String table, String key) {
    System.out.println("delete table =" + table + ", key = " + key);
    //TODO: delete key
    return Status.OK;
  }


  /**
   * Insert a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key.
   * 
   * @param table
   *      The name of the table
   * @param key
   *      The record key of the record to insert.
   * @param values
   *      A HashMap of field/value pairs to insert in the record
   * @return Zero on success, a non-zero error code on error. See the {@link DB}
   *     class's description for a discussion of error codes.
   */
  @Override
  public Status insert(String table, String key,
      Map<String, ByteIterator> values) {
    if (printCnt-- > 0) {
      System.out.println("insert table =" + table + ", key = " + key);
      for (Map.Entry<String, ByteIterator> entry : values.entrySet()) {
        System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue().toArray());
      }
    }
    // TODO: client should receive f +1 consistent result and then return OK.
    return Status.OK;
  }

  /**
   * Read a record from the database. Each field/value pair from the result will
   * be stored in a HashMap.
   * 
   * @param table
   *      The name of the table
   * @param key
   *      The record key of the record to read.
   * @param fields
   *      The list of fields to read, or null for all of them
   * @param result
   *      A HashMap of field/value pairs for the result
   * @return Zero on success, a non-zero error code on error or "not found".
   */
  @Override
  public Status read(String table, String key, Set<String> fields,
      Map<String, ByteIterator> result) {
    System.out.println("read table =" + table + ", key = " + key);
    return Status.OK;
  }

  /**
   * Perform a range scan for a set of records in the database. Each field/value
   * pair from the result will be stored in a HashMap.
   * 
   * @param table
   *      The name of the table
   * @param startkey
   *      The record key of the first record to read.
   * @param recordcount
   *      The number of records to read
   * @param fields
   *      The list of fields to read, or null for all of them
   * @param result
   *      A Vector of HashMaps, where each HashMap is a set field/value
   *      pairs for one record
   * @return Zero on success, a non-zero error code on error. See the {@link DB}
   *     class's description for a discussion of error codes.
   */
  @Override
  public Status scan(String table, String startkey, int recordcount,
      Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
    System.out.println("scan table =" + table + ", startkey = " + startkey);
    return Status.OK;
  }

  /**
   * Update a record in the database. Any field/value pairs in the specified
   * values HashMap will be written into the record with the specified record
   * key, overwriting any existing values with the same field name.
   * 
   * @param table
   *      The name of the table
   * @param key
   *      The record key of the record to write.
   * @param values
   *      A HashMap of field/value pairs to update in the record
   * @return Zero on success, a non-zero error code on error. See this class's
   *     description for a discussion of error codes.
   */
  @Override
  public Status update(String table, String key,
      Map<String, ByteIterator> values) {
    System.out.println("update table =" + table + ", key = " + key);
    return Status.OK;
  }

}

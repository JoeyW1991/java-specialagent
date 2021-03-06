/* Copyright 2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent.rule.jdbc;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.h2.Driver;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.opentracing.Scope;
import io.opentracing.contrib.specialagent.AgentRunner;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;

@RunWith(AgentRunner.class)
public class JdbcTest {
  @Test
  public void test(final MockTracer tracer) throws Exception {
    // for withActiveSpanOnly and ignoreForTracing support
    System.setProperty(JdbcAgentIntercept.WITH_ACTIVE_SPAN_ONLY, "");
    System.setProperty(JdbcAgentIntercept.IGNORE_FOR_TRACING_SEPARATOR, "@@@");
    System.setProperty(JdbcAgentIntercept.IGNORE_FOR_TRACING, "SELECT 1 FROM dual @@@ SELECT 2 FROM dual");

    // DriverManager.setLogWriter(new PrintWriter(System.err));
    Driver.load();
    try (
      final Scope ignored = tracer.buildSpan("jdbc-test").startActive(true);
      final Connection connection = DriverManager.getConnection("jdbc:h2:mem:jdbc");
    ) {
      final Statement statement = connection.createStatement();
      statement.executeUpdate("CREATE TABLE employer (id INTEGER)");

      final List<MockSpan> spans = tracer.finishedSpans();
      assertEquals(2, spans.size());
    }

    // parent span closed
    assertEquals(3, tracer.finishedSpans().size());
    try (
      final Connection connection = DriverManager.getConnection("jdbc:h2:mem:jdbc");
    ) {
      final Scope ignored = tracer.buildSpan("jdbc-test").startActive(true);
      final Statement statement = connection.createStatement();

      // should be ignored as ignoreForTracing specified, spans no change
      statement.executeQuery("SELECT 1 FROM dual");
      statement.executeQuery("SELECT 2 FROM dual");
      assertEquals(3, tracer.finishedSpans().size());

      // not an ignored sql, spans increased
      statement.executeQuery("SELECT 3 FROM dual");
      assertEquals(4, tracer.finishedSpans().size());

      // parent span closed
      ignored.close();
      assertEquals(5, tracer.finishedSpans().size());

      // no more span created if no active span
      statement.executeQuery("SELECT 3 FROM dual");
      statement.executeQuery("SELECT 4 FROM dual");
      assertEquals(5, tracer.finishedSpans().size());
    }
  }
}
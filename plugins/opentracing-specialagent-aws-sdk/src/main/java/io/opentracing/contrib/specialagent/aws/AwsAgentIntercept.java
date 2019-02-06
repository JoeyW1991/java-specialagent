/* Copyright 2019 The OpenTracing Authors
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
package io.opentracing.contrib.specialagent.aws;


import io.opentracing.contrib.aws.TracingRequestHandler;

public class AwsAgentIntercept {
  public static void enter(final Object thiz) {
    final com.amazonaws.client.builder.AwsClientBuilder builder =
        (com.amazonaws.client.builder.AwsClientBuilder) thiz;
    final TracingRequestHandler requestHandler = new TracingRequestHandler();
    builder.withRequestHandlers(requestHandler);
  }
}
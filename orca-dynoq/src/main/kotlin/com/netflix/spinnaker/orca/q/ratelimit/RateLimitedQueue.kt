/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.orca.q.ratelimit

import com.netflix.spinnaker.config.RateLimitConfiguration
import com.netflix.spinnaker.orca.q.Message
import com.netflix.spinnaker.orca.q.Queue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.concurrent.TimeUnit

class RateLimitedQueue(
  val queue: Queue,
  val backend: RateLimitBackend,
  val rateLimitConfiguration: RateLimitConfiguration
) : Queue, Closeable {

  private val log: Logger = LoggerFactory.getLogger(javaClass)

  override fun poll(): Message? {
    var r: PolledMessageResult
    do {
      r = pollUntilMessage()
    } while (!r.shouldReturn)
    return r.message
  }

  private fun pollUntilMessage(): PolledMessageResult {
    val message = queue.poll()
    when (message) {
      null -> return PolledMessageResult(null, true)
      !is Message.ApplicationAware -> return PolledMessageResult(message, true)
      else -> {
        val rate = backend.getRate(message.application)
        if (rate.limiting) {
          queue.push(message, rateLimitConfiguration.delayMs, TimeUnit.MILLISECONDS)
          queue.ack(message)
          log.info("rate limited message (application: ${message.application}, capacity: ${rate.capacity}, windowMs: ${rate.windowMs}, delayMs: ${rateLimitConfiguration.delayMs} message: ${message.id})")
          return PolledMessageResult(null, false)
        }
        return PolledMessageResult(message, true)
      }
    }
  }

  override fun push(message: Message) {
    queue.push(message)
  }

  override fun push(message: Message, delay: Long, unit: TimeUnit) {
    queue.push(message, delay, unit)
  }

  override fun ack(message: Message) {
    queue.ack(message)
  }

  override fun close() {
    if (queue is Closeable) {
      queue.close()
    }
  }
}

private class PolledMessageResult(
  val message: Message?,
  val shouldReturn: Boolean
)

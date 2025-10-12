package io.github.srs.controller.protobuf.ping

import io.github.srs.protos.ping.*
import cats.effect.*
import io.grpc.*
import fs2.Stream

class PongerService extends PongerFs2Grpc[IO, Metadata]:

  override def ping(request: Stream[IO, PingPong], ctx: Metadata): Stream[IO, PingPongAck] =
    request.map { ping =>
      println(s"received ping from ${ping.from}")
      PingPongAck(from = "server", to = "client")
    }

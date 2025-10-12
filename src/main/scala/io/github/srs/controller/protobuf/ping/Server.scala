package io.github.srs.controller.protobuf.ping

import io.github.srs.protos.ping.PongerFs2Grpc
import io.grpc.ServerServiceDefinition
import cats.effect.kernel.Resource
import cats.effect.IO
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import fs2.grpc.syntax.all.*
import io.grpc.Server

object Server:

  private val pongerService: Resource[IO, ServerServiceDefinition] =
    PongerFs2Grpc.bindServiceResource[IO](new PongerService)

  private def runServer(service: ServerServiceDefinition): Resource[IO, Server] =
    NettyServerBuilder.forPort(50051).addService(service).resource[IO]

  val grpcServer: Resource[IO, Server] =
    pongerService
      .flatMap(x => runServer(x))

"""
gRPC Ping-Pong Client

A simple client for bidirectional streaming ping-pong with a gRPC server.
Sends ping messages and receives acknowledgments concurrently.
"""

import asyncio

import grpc

import ping_pb2
import ping_pb2_grpc


class PingPongClient:
    """Client for bidirectional ping-pong streaming with gRPC server"""

    def __init__(self, server_address: str, client_name: str):
        """
        Initialize the client

        Args:
            server_address: Server address (e.g., 'localhost:9999')
            client_name: Name to identify this client
        """
        self.server_address = server_address
        self.client_name = client_name
        self.channel = None
        self.stub = None

    async def connect(self):
        """Establish connection to the server"""
        self.channel = grpc.aio.insecure_channel(self.server_address)
        self.stub = ping_pb2_grpc.PongerStub(self.channel)

        # Test connection
        await asyncio.wait_for(self.channel.channel_ready(), timeout=5.0)
        print(f"✓ Connected to {self.server_address}\n")

    async def close(self):
        """Close the connection"""
        if self.channel:
            await self.channel.close()

    async def send_pings(
        self, request_queue: asyncio.Queue, count: int, interval: float
    ):
        """
        Send ping messages to the server

        Args:
            request_queue: Queue to put ping messages
            count: Number of pings to send
            interval: Delay between pings in seconds
        """
        for i in range(count):
            # Create ping message
            ping = ping_pb2.PingPong()
            setattr(ping, "from", self.client_name)

            print(f"→ Sending ping {i+1}/{count}")
            await request_queue.put(ping)
            await asyncio.sleep(interval)

        # Signal end of requests
        await request_queue.put(None)
        print("✓ All pings sent\n")

    async def generate_requests(self, request_queue: asyncio.Queue):
        """
        Generator that yields ping messages from the queue

        Args:
            request_queue: Queue containing ping messages
        """
        while True:
            item = await request_queue.get()
            if item is None:  # End signal
                break
            yield item

    async def receive_acks(self, call):
        """
        Receive acknowledgments from the server

        Args:
            call: The gRPC streaming call

        Returns:
            Number of acknowledgments received
        """
        ack_count = 0
        try:
            async for ack in call:
                ack_count += 1
                from_field = getattr(ack, "from")
                print(f"← Received ACK #{ack_count}: from={from_field}, to={ack.to}")
        except asyncio.CancelledError:
            pass  # Normal completion
        except grpc.aio.AioRpcError as e:
            if e.code() not in [grpc.StatusCode.CANCELLED, grpc.StatusCode.OK]:
                raise

        return ack_count

    async def run(self, count: int = 5, interval: float = 1.0):
        """
        Run the ping-pong exchange

        Args:
            count: Number of pings to send
            interval: Delay between pings in seconds
        """
        print(f"Starting ping-pong as '{self.client_name}'")
        print("-" * 60)

        # Create queue for managing requests
        request_queue = asyncio.Queue()

        # Start the bidirectional streaming call
        call = self.stub.Ping(self.generate_requests(request_queue))

        # Run send and receive concurrently
        send_task = asyncio.create_task(self.send_pings(request_queue, count, interval))
        receive_task = asyncio.create_task(self.receive_acks(call))

        # Wait for both to complete
        await send_task
        ack_count = await receive_task

        print("-" * 60)
        print(f"✓ Ping-pong completed! Sent {count} pings, received {ack_count} ACKs")


async def main():
    """Main entry point"""
    # Configuration
    SERVER_ADDRESS = "localhost:9999"
    CLIENT_NAME = "PythonClient"
    PING_COUNT = 5
    PING_INTERVAL = 1.0  # seconds

    # Create and run client
    client = PingPongClient(SERVER_ADDRESS, CLIENT_NAME)

    try:
        await client.connect()
        await client.run(count=PING_COUNT, interval=PING_INTERVAL)
    except asyncio.TimeoutError:
        print(f"✗ Connection timeout - server at {SERVER_ADDRESS} not responding")
    except grpc.aio.AioRpcError as e:
        print(f"✗ RPC failed: {e.code()}: {e.details()}")
    except Exception as e:
        print(f"✗ Error: {e}")
    finally:
        await client.close()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n\n✗ Interrupted by user")

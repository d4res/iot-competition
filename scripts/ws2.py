import asyncio
import websockets

async def hello():
    async with websockets.connect('wss://81.68.245.247:8888/ws/raspberry') as websocket:
        while True:
            greeting = await websocket.recv()
            print(f"< {greeting}")


if __name__  == "__main__":
    asyncio.get_event_loop().run_until_complete(hello())




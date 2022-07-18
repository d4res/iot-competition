import asyncio
import websockets

import asyncio
import websockets

async def hello():
    async with websockets.connect('ws://localhost:8888/ws') as websocket:
        while True:
            greeting = await websocket.recv()
            print(f"< {greeting}")

asyncio.get_event_loop().run_until_complete(hello())




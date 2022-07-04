package main

import (
	"log"
	"net/http"

	"github.com/gobwas/ws"
)

func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		conn, _, _, err := ws.UpgradeHTTP(r, w)
		if err != nil {
			panic(err)
		}

		go func() {
			defer conn.Close()

			for {

				//header, err := ws.ReadHeader(conn)
				//if err != nil {
				//	// handle error
				//}
				//
				//payload := make([]byte, header.Length)
				//_, err = io.ReadFull(conn, payload)
				//if err != nil {
				//	// handle error
				//}
				//if header.Masked {
				//	ws.Cipher(payload, header.Mask, 0)
				//}
				//
				//// Reset the Masked flag, server frames must not be masked as
				//// RFC6455 says.
				//header.Masked = false
				//
				//if err := ws.WriteHeader(conn, header); err != nil {
				//	// handle error
				//}
				//if _, err := conn.Write(payload); err != nil {
				//	// handle error
				//}
				//
				//if header.OpCode == ws.OpClose {
				//	return
				//}

				frame, err := ws.ReadFrame(conn)
				if err != nil {
					log.Println(err)
					return
				}

				if frame.Header.OpCode == ws.OpClose {
					log.Println("remote client close")
					return
				}
				if frame.Header.Masked {
					frame = ws.UnmaskFrame(frame)
				}
				log.Println(frame.Header.Masked, string(frame.Payload))

				_ = ws.WriteFrame(conn, frame)
			}
		}()
	})

	http.ListenAndServe(":8888", mux)
}

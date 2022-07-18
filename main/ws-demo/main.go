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

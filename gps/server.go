package gps

import (
	"encoding/json"
	"github.com/gobwas/ws"
	"io"
	"log"
	"net"
	"net/http"
)

type Server struct {
	Task chan Location
}

func NewServer() *Server {
	return &Server{Task: make(chan Location, 0)}
}

func (s *Server) Start() {
	mux := http.NewServeMux()

	mux.HandleFunc("/gps", func(w http.ResponseWriter, r *http.Request) {
		body := r.Body
		defer body.Close()
		contents, err := io.ReadAll(body)
		var location Location
		err = json.Unmarshal(contents, &location)
		if err != nil {
			log.Println(err)
		}
		log.Printf("receive gps %v", location)
		w.Write([]byte("success"))
		go func() {
			s.Task <- location
		}()
		//s.Task <- location
	})

	mux.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		log.Println("ws connect from ", r.RemoteAddr)
		conn, _, _, err := ws.UpgradeHTTP(r, w)
		if err != nil {
			log.Println(err)
		}

		// write loop
		go func(conn net.Conn) {
			for {
				// sending data back to client
				log.Println("sending data back to client ", conn.RemoteAddr())
				location := <-s.Task
				data, err := json.Marshal(location)
				if err != nil {
					log.Println(err)
					continue
				}
				frame := ws.NewFrame(ws.OpText, true, data)
				ws.WriteFrame(conn, frame)
			}
		}(conn)

		for {
			// read loop
			frame, err := ws.ReadFrame(conn)
			if err != nil {
				log.Println(err)
				return
			}

			if frame.Header.Masked {
				frame = ws.UnmaskFrame(frame)
			}
		}
	})

	log.Fatalln(http.ListenAndServe(":8888", mux))
}

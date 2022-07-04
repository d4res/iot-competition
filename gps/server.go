package gps

import (
	"encoding/json"
	"github.com/gobwas/ws"
	"io"
	"log"
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

	mux.HandleFunc("/target", func(w http.ResponseWriter, r *http.Request) {
		body := r.Body
		defer body.Close()
		contents, err := io.ReadAll(body)
		var location Location
		err = json.Unmarshal(contents, &location)
		if err != nil {
			log.Println(err)
		}
		s.Task <- location
	})

	mux.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		conn, _, _, err := ws.UpgradeHTTP(r, w)
		if err != nil {
			log.Println(err)
		}

		go func() {
			for {
				location := <-s.Task
				data, err := json.Marshal(location)
				if err != nil {
					log.Println(err)
					continue
				}
				frame := ws.NewFrame(ws.OpText, true, data)
				ws.WriteFrame(conn, frame)
			}
		}()

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

	http.ListenAndServe(":8888", mux)
}

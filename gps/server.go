package gps

import (
	"log"
	"net/http"
)

type Server struct {
	Task     chan Location
	TaskLine chan []Location
}

func NewServer() *Server {
	return &Server{Task: make(chan Location, 0)}
}

func (s *Server) Start() {
	mux := http.NewServeMux()

	mux.HandleFunc("/gps", s.GpsReceiver())
	mux.HandleFunc("/ws", s.WsConn())
	mux.HandleFunc("/gpsList", s.GpsList())

	log.Println("listening on :8888.\n/gps\n/ws\n/gpsList")
	log.Fatalln(http.ListenAndServe(":8888", mux))
}

package gps

import (
	"log"
	"net/http"
)

type Server struct {
	TaskLine chan []Location
	Arrive   chan struct{}
}

func NewServer() *Server {
	return &Server{TaskLine: make(chan []Location, 0), Arrive: make(chan struct{}, 0)}
}

func (s *Server) Start() {
	mux := http.NewServeMux()

	mux.HandleFunc("/gps", s.GpsReceiver())
	mux.HandleFunc("/ws/aircraft", s.WsConn(AIRCRAFT))
	mux.HandleFunc("/ws/raspberry", s.WsConn(RASPBERRY))
	mux.HandleFunc("/arrive", s.OnArrive())
	mux.HandleFunc("/gpsList", s.GpsList())

	log.Println("listening on :8888.\n/gps\n/ws\n/gpsList")
	log.Fatalln(http.ListenAndServe(":8888", mux))
}

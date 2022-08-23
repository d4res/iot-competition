package gps

import (
	"iot-backend/db"
	"log"
	"net/http"
)

type Server struct {
	TaskLine chan []db.Location
	Arrive   chan struct{}
	AcLoc    chan db.Location
}

func NewServer() *Server {
	return &Server{TaskLine: make(chan []db.Location, 1), Arrive: make(chan struct{}, 0), AcLoc: make(chan db.Location, 0)}
}

func (s *Server) Start() {
	mux := http.NewServeMux()

	mux.HandleFunc("/gps", s.GpsReceiver())
	mux.HandleFunc("/ws/aircraft", s.WsConn(AIRCRAFT))
	mux.HandleFunc("/ws/raspberry", s.WsConn(RASPBERRY))
	mux.HandleFunc("/ws/weapp", s.WsConn(WEAPP))
	mux.HandleFunc("/arrive", s.OnArrive())
	mux.HandleFunc("/gpsList", s.GpsList())
	mux.HandleFunc("/mission/log", s.MissionLog())

	banner()
	log.Fatalln(http.ListenAndServe(":8888", mux))
}

func banner() {
	log.Println("listening on :8888\n" +
		"/gps\n" +
		"/ws/aircraft\n" +
		"/ws/raspberry\n" +
		"/ws/weapp\n" +
		"/arrive\n" +
		"/gpsList\n" +
		"/mission/log",
	)
}

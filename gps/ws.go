package gps

import (
	"encoding/json"
	"github.com/gobwas/ws"
	"log"
	"net"
	"net/http"
	"time"
)

func (s *Server) WsConn() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		log.Println("ws connect from ", r.RemoteAddr)
		conn, _, _, err := ws.UpgradeHTTP(r, w)
		if err != nil {
			log.Println(err)
		}

		// read loop
		go s.readLoop(conn)

		// write loop
		go s.writeLoop(conn)

		for {
			time.Sleep(time.Second)
		}
	}
}

func (s *Server) readLoop(conn net.Conn) {
	for {
		frame, err := ws.ReadFrame(conn)
		if err != nil {
			log.Println(err.Error())
			return
		}

		if frame.Header.Masked {
			frame = ws.UnmaskFrame(frame)
		}

		if frame.Header.OpCode == ws.OpPing {
			newFrame := ws.NewFrame(ws.OpPong, true, frame.Payload)
			err := ws.WriteFrame(conn, newFrame)
			if err != nil {
				log.Println(err.Error())
			}
		}
	}
}

func (s *Server) writeLoop(conn net.Conn) {
	for {
		var data []byte
		var err error
		select {
		case location := <-s.Task:
			data, err = json.Marshal(location)
		case locationLine := <-s.TaskLine:
			data, err = json.Marshal(locationLine)
		}

		if err != nil {
			log.Println(err)
			continue
		}
		frame := ws.NewFrame(ws.OpText, true, data)
		log.Println("sending data back to client ", conn.RemoteAddr())
		err = ws.WriteFrame(conn, frame)
		if err != nil {
			log.Println(err.Error())
		}
	}
}

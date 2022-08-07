package gps

import (
	"context"
	"encoding/json"
	"github.com/gobwas/ws"
	"log"
	"net"
	"net/http"
	"time"
)

const (
	AIRCRAFT = iota
	RASPBERRY
)

func (s *Server) WsConn(typ int) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		log.Println("ws connect from ", r.RemoteAddr)
		body := r.Body
		defer body.Close()

		conn, _, _, err := ws.UpgradeHTTP(r, w)
		context.Background()
		if err != nil {
			log.Println(err)
		}

		// read loop
		go s.readLoop(conn)

		if typ == AIRCRAFT {
			// write loop
			go s.writeLoop(conn)
		} else {
			go s.writeLoop2(conn)
		}

		for {
			time.Sleep(time.Second)
		}
	}
}

func (s *Server) readLoop(conn net.Conn) {
	for {
		frame, err := ws.ReadFrame(conn)
		if err != nil {
			log.Println("err: ", err.Error())
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

func (s *Server) writeLoop2(conn net.Conn) {
	for {
		<-s.Arrive
		frame := ws.NewFrame(ws.OpText, true, []byte("confirm"))
		log.Println("sending confirm to the raspberry")
		err := ws.WriteFrame(conn, frame)
		if err != nil {
			log.Println(err.Error())
		}
	}
}

package gps

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/gobwas/ws"
	"iot-backend/db"
	"log"
	"net"
	"net/http"
	"time"
)

const (
	AIRCRAFT = iota
	RASPBERRY
	WEAPP
)

func (s *Server) WsConn(typ int) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		log.Println("ws connect from ", r.RemoteAddr)
		body := r.Body
		defer body.Close()

		conn, _, _, err := ws.UpgradeHTTP(r, w)
		ctx, cancel := context.WithCancel(context.Background())

		if err != nil {
			log.Println(err)
		}

		switch typ {
		case AIRCRAFT:
			log.Println("aircraft in")
		case WEAPP:
			log.Println("weapp in")
		case RASPBERRY:
			log.Println("raspberry in")
		}

		// readLoop
		switch typ {
		case AIRCRAFT:
			go s.readLoop(conn, cancel, func(frame ws.Frame) {
				fmt.Println(string(frame.Payload))
				var loc db.Location
				err := json.Unmarshal(frame.Payload, &loc)
				if err != nil {
					log.Println(err)
				}
				go func(location db.Location) {
					s.AcLoc <- location
				}(loc)
			})
		default:
			go s.readLoop(conn, cancel, func(frame ws.Frame) {
			})
		}

		// writeLoop
		switch typ {
		case AIRCRAFT:
			go s.writeLoopAC(conn, ctx)
		case WEAPP:
			go s.writeLoopWEAPP(conn, ctx)
		case RASPBERRY:
			go s.writeLoopRASP(conn, ctx)
		}

		for {
			time.Sleep(time.Second)
		}
	}
}

func (s *Server) readLoop(conn net.Conn, cancelFunc context.CancelFunc, handler func(frame ws.Frame)) {
	for {
		frame, err := ws.ReadFrame(conn)
		if err != nil {
			log.Println("err: ", err.Error())
			cancelFunc()
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

		handler(frame)
	}
}

func (s *Server) writeLoopAC(conn net.Conn, ctx context.Context) {
	for {
		var data []byte
		var err error
		select {
		case locationLine := <-s.TaskLine:
			data, err = json.Marshal(locationLine)
		case <-ctx.Done():
			log.Println("close writeLoopAC.")
			return
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

func (s *Server) writeLoopRASP(conn net.Conn, ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			log.Println("close writeLoopRASP")
			return
		case <-s.Arrive:
		}
		//<-s.Arrive
		frame := ws.NewFrame(ws.OpText, true, []byte("confirm"))
		log.Println("sending confirm to the raspberry")
		err := ws.WriteFrame(conn, frame)
		if err != nil {
			log.Println(err.Error())
		}
	}
}

func (s *Server) writeLoopWEAPP(conn net.Conn, ctx context.Context) {
	for {
		var loc db.Location
		var data []byte
		var err error
		select {
		case <-ctx.Done():
			log.Println("close writeLoopWEAPP")
			return
		case loc = <-s.AcLoc:
			loc = transGPS(loc)
			data, err = json.Marshal(loc)
			if err != nil {
				log.Println(err)
			}
		}

		frame := ws.NewFrame(ws.OpText, true, data)
		log.Println("sending data back to weapp")
		err = ws.WriteFrame(conn, frame)
		if err != nil {
			log.Println(err)
		}
	}
}

package gps

import (
	"encoding/json"
	"iot-backend/db"
	"net/http"
)

func (s *Server) MissionLog() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		missions, err := db.GetAllMissions()

		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte("error"))
			return
		}

		data, err := json.Marshal(missions)
		if err != nil {
			return
		}
		w.WriteHeader(http.StatusOK)
		w.Write(data)

	}
}

package gps

import (
	"fmt"
	"iot-backend/db"
	"testing"
)

func Test_transGPS(t *testing.T) {
	fmt.Println(transGPS(db.Location{
		Latitude:  45.63182034,
		Longitude: 126.64452565,
	}))

	// result: {45.633802113298856 126.65042280296394}
	fmt.Println(transGPS(db.Location{
		Latitude:  45.631819166045,
		Longitude: 126.644467986,
	}))
}

package db

import (
	"context"
	"fmt"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"os"
)

var dbConn *mongo.Client

func init() {
	DBHOST := os.Getenv("DBHOST")
	if DBHOST == "" {
		DBHOST = "127.0.0.1"
	}
	url := fmt.Sprintf("mongodb://root:r00tp4ss@%s:27017", DBHOST)
	client, err := mongo.Connect(context.TODO(), options.Client().ApplyURI(url))
	if err != nil {
		log.Println("can not connect to the database")
		panic(err)
	}

	if err := client.Ping(context.TODO(), nil); err != nil {
		log.Println("can not ping to the database")
		panic(err)
	}

	dbConn = client
}

func GetDB() *mongo.Client {
	return dbConn
}

func CloseDB() {
	if err := dbConn.Disconnect(context.TODO()); err != nil {
		panic(err)
	}
}

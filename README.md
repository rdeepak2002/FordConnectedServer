# Ford Connected Server

## Author

Deepak Ramalingam

[Email](mailto:rdeepak2002@gmail.com)

[Website](https://rdeepak2002.github.io/home)

## About

Hackathon project for the [Ford Smart Vehicle Connectivity Challenge](https://fordsmart.devpost.com/)

## Requirements (iOS)

- JAVA
- Maven

## Getting Started

Build the jar file

```sh
mvn clean install
```

Execute the jar file located in the target folder (IMPORTANT: make sure the environment variables below are defined)

```sh
java -jar target/fordconnected.server-1.0.jar
```

## Required Environment Variables
```
"DB_URI": "mongodb+srv://username:password@foo.mongodb.net/bar?retryWrites=true&w=majority",
"REDIS_ENDPOINT": "redis://localhost",
"REDIS_PORT": "6379",
"REDIS_PASSWORD": ""
```

DB_URI is the database URI of a MongoDB Atlas instance.
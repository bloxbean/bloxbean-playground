## BloxBean Playground

## Build Java Backend

```
$> ./gradlew clean build
```

### Install redis

### Run Java Backend

```
$> export REDIS_URI=redis://localhost:6379
$> export bf_testnet_project_id=<Blockfrost Testnet Project Id>

$> ./gradlew run
```

## Run Web App

Go to "app" folder

Check api_base_url value in config.js

```
$> cd app
$> npm install
$> npm run serve
```



## Using Docker

docker buildx build --platform=linux/amd64 -t bloxbean/bloxbean-playground-api:0.1 . --push

## Push to docker registry

docker push bloxbean/bloxbean-playground-api:0.1


## BloxBean Playground

## Build Java Backend

```
$> ./gradlew clean build
```

### Install redis

### Run Java Backend

```
$> export REDIS_URI=redis://localhost:6379
$> export BF_TESTNET_PROJECT_ID=<Blockfrost Testnet Project Id>

$> ./gradlew run
```

docker buildx build --platform=linux/amd64 -t bloxbean/bloxbean-playground-api:0.1 . --push

## Push to docker registry

docker push bloxbean/bloxbean-playground-api:0.1


## Run

### Dependencies
- Redis : Redis is used as cache.

### Start the api server

- Open run.sh
- Enter appropriate values for redis_uri, batch_node, web3rpc_url, rate_api_url

- Make sure the jar file name & version are correct.

```aidl
$> ./run.sh
```

### Run in Kubernetes Cluster
Refer to kubernetes scripts under "deploy" folder.

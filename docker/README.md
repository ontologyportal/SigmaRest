# Dockerized Sigma/SUMO/Vampire with REST for ITM

## Installation

1. Ensure that Docker is properly setup on your machine
2. Add any .kif files to the kb directory you want to be part of your installation
3. Update config.xml to point to any .kif files you want to be part of your installation
   1. NOTE: By default, only tinySUMO.kif and itm.kif are active
4. From this directory, build the sigma image
   * docker build -t sigma .
   * NOTE: This can take 5-10 minutes
5. Run a container with this new image, with ports exposed
   * docker run -p 8080:8080 --name itm-sigma sigma
   * NOTE: This will spawn a docker container with the image sigma, with the container name itm-sigma, and exposing the port 8080. You can change the port mapping if 8080 is already in use

## Using Sigma/SUMO

* Web Interface
  * The default web interface of Sigma can be reached at http://localhost:8080/sigma/login.html
* REST Interface
  * The REST interface can be reached at http://localhost:8080/sigmarest/resources

### REST Endpoints

* init
  * http: GET
  * url: http://localhost:8080/sigmarest/resources/init
  * This initializes Sigma/Sumo and takes a few minutes. This must be done once each time the container is reset
* tell
  * http: GET
  * url: http://localhost:8080/sigmarest/resources/tell
  * params: statement = str
  * This adds a single sumo statement to the knowledgebase, e.g., (instance John Human)
* ask
  * http: GET
  * url: http://localhost:8080/sigmarest/resources/ask
  * params: query: str, timeout: float
  * This queries sumo/sigma/vampire with the given query (e.g., (instance ?X Human))
  * response: json
    * bindings: {var: val} (e.g., {"?X": "John"})
    * proof: [str] (The output of vampire's proof, in text)
    * time: float (The time it took to run the query in seconds, e.g., 10.005)
    * error: str (Any errors that occured, currently only timeout)

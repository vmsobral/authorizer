# authorizer

This application is responsible for reading a file with json lines from stdin, 
representing account transaction commands, processing them, and printing the 
partial status of the account.

## Usage
#### Running for the example file
To run the application, one can have Docker installed and type
```shell script
docker build -t authorizer . 
```
And then
```shell script
docker run -p 3000:3000 authorizer
```
This will run the `operations` file located in the `json` folder.

#### Running with a different file
To run a different commands file, one can copy it to the `json` folder, remembering to
rename it to `operations` or changing the name in the Dockerfile and then re-running the above
docker commands.

Another option is to copy the file to the `json` folder, re-running only the `docker build`
and running docker run as follows:
```shell script
docker run -p 3000:3000 -it authorizer /bin/bash 
```
Once inside the docker container, one can type
```shell script
lein run < [filename]
```

#### Run tests
Inside docker container, run:
```shell script
lein test
```
## Project Structure

This project was developed in Clojure. The structure is basically separated in 3 namespaces:
- core: contains the main project login, as well as account structure creation and altering
- rules: contains the business logic, aka transaction authorization/denial rules
- utils: contains utilitary functions such as json handling and printing functions
## Project Decisions
- It is assumed that the first line of every json file is an account initialization command
- It is assumed that for each command the violations printed are specific of that command, 
i.e. violations are not printed in a cumulative way.
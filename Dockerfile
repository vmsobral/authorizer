FROM clojure:alpine

RUN mkdir /app
WORKDIR /app

COPY json ./json
COPY ./src ./src
COPY project.clj .

CMD lein run < json/operations

EXPOSE 3000
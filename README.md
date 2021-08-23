# Link preview service

[![Release](https://github.com/AMPnet/link-preview-service/actions/workflows/gradle-release.yml/badge.svg?branch=master)](https://github.com/AMPnet/link-preview-service/actions/workflows/gradle-release.yml)

Service collects [OpenGraph](https://ogp.me/) metadata for a given URL and returns the response in JSON format. Collected URLs are stored in the cache and reused for the same request param.

Service provides only one route: `GET localhost:8126/preview?url=example.org` which will return the following JSON:
```
{
  "url": String - requested url,
  "open_graph": {
    "title": String? - og:title,
    "description": String? - og:description,
    "image": {
      "url": String? - og:image,
      "height": String? - og:image:height,
      "width": String? - og:image:width
    }
  }
}

```

## Run

To start the application use:
```sh
./gradlew run
```
Which will spin up the application on `localhost:8126`.

## Docker

Service is avaialble as docker image:
```sh
docker run --rm -p 8126:8126 ampnet/link-preview-service:latest
```

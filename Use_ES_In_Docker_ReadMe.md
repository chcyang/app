## start up elasticsearch from docker

- prepare docker-compose file  
  [docker-compose.yml](docker-compose.yml)

- start up elasticsearch from docker
```
cd {PROJECT_ROOT_DIR}
docker-compose up  -d  elasticsearch
```

- install elasticserach plugin  
[Ingest Attachment Processor Plugin](https://www.elastic.co/guide/en/elasticsearch/plugins/master/ingest-attachment.html#ingest-attachment)

    - if you start up you elasticsearch from docker, you should run blow command in docker container terminal
```
bin/elasticsearch-plugin install ingest-attachment
```

- restart elasticsearch
```
docker container restart -t 1 elasticsearch
```

- Use an attachment processor to decode the string and extract the file’s properties  
create a pipeline
```
PUT _ingest/pipeline/attachment
{
  "description" : "Extract attachment information",
  "processors" : [
    {
      "attachment" : {
        "field" : "data"
      }
    }
  ]
}
```

- create index (all use the default config setting)
```
PUT /backlog-attachment-001
```

- create mapping for index
```
PUT /backlog-attachment-001/_mapping
{
      "_source":{
         "excludes":[
            "data",
            "attachment.content"
         ]
      },
      "properties":{
         "filename":{
            "type":"text"
         },
         "attachment":{
            "properties":{
               "date":{
                  "type":"date"
               },
               "content_type":{
                  "type":"text",
                  "fields":{
                     "keyword":{
                        "ignore_above":256,
                        "type":"keyword"
                     }
                  }
               },
               "author":{
                  "type":"text",
                  "fields":{
                     "keyword":{
                        "ignore_above":256,
                        "type":"keyword"
                     }
                  }
               },
               "title":{
                  "type":"text",
                  "fields":{
                     "keyword":{
                        "ignore_above":256,
                        "type":"keyword"
                     }
                  }
               },
               "content":{
                  "type":"text"
               },
               "content_length":{
                  "type":"long"
               }
            }
         },
         "data":{
            "type":"binary",
            "store":false
         },
         "filePath":{
            "type":"keyword"
         },
         "updateTimes":{
            "type":"long"
         },
         "source":{
            "type":"keyword"
         },
         "type":{
            "type":"keyword"
         },
         "uploadTime":{
            "type":"date"
         },
         "fileType":{
            "type":"keyword"
         },
         "fileId":{
            "type":"keyword"
         },
         "fileRelateType":{
            "type":"keyword"
         },
         "uploadUser":{
            "type":"keyword"
         }
      }
}
```
then you can add docs to index now
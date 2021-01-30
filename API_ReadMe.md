## Detail pages and apis

### Pages
//upload a file for create index doc  
GET         /upload              
POST        /upload              

// search file index  
GET         /filesearch          
POST        /filesearch          


### Apis
// show backlog space   
GET         /space    
// addd  a fixed index doc  
GET         /addIndex       
// get work hours of a certain user      
GET         /getAllIssue(assignee: String)  
Sample request
```
http://localhost:9000/getallissue?assignee=373653
```
Sample Response  
show the user's work hours of day(actualHours,estimateHours) when the day have a task assigned.
```
{
   "2021-01-26T00:00":[
      6.5,
      5.0
   ],
   "2021-01-25T00:00":[
      6.5,
      5.0
   ],
   "2021-01-19T00:00":[
      3.0,
      4.0
   ],
   "2021-01-24T00:00":[
      6.5,
      5.0
   ],
   "2021-01-21T00:00":[
      2.0,
      2.0
   ],
   "2021-01-23T00:00":[
      6.5,
      5.0
   ],
   "2021-01-20T00:00":[
      8.0,
      6.0
   ]
}
```
// get all shared files and create index  
not finish yet(from now on, elasticsearch can't process data stream from a api response content-type as  application/octet-stream)  
GET         /addIndexBulk(projectId: String, dirPath: String)
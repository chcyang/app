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
// get all shared files and create index
GET         /addIndexBulk(projectId: String, dirPath: String)
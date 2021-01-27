package models

case class FileUploadModel(
                            data: String,
                            fileType: String,
                            fileId: Int,
                            uploadUser: String,
                            fileName: String,
                            source: String
                          )

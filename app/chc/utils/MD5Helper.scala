package chc.utils

import play.shaded.oauth.org.apache.commons.codec.digest.DigestUtils

object MD5Helper {

  def md5Hex(s: String) = {
    DigestUtils.md5Hex(s)
  }

  def main(args: Array[String]): Unit = {
    println(md5Hex("Hello World"))
  }

}

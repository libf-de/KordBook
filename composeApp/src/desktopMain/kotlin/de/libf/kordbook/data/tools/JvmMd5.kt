package de.libf.kordbook.data.tools

import java.math.BigInteger
import java.security.MessageDigest

class JvmMd5 : Md5 {
    override fun fromString(string: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(string.toByteArray())).toString(16).padStart(32, '0')
    }
}